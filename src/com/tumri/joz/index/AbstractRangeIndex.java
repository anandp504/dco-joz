package com.tumri.joz.index;

import com.tumri.utils.data.*;
import com.tumri.utils.index.*;

import java.util.*;

/**
 * The purpose of this class is to index Range<V>-->Value mappings.  Where a query to the index for any given Range<V>
 * will return a SortedSet<Values> which contains all Value(s) which fall within the parameter Range<V>. The specific
 * implementation of V on which the Ranges are created will determine how this index functions.
 * <p/>
 * User: scbraun
 * Date: Feb 9, 2010
 * Time: 11:47:06 AM
 */
public abstract class AbstractRangeIndex<attr, V, Value> extends AbstractIndex<Value, attr, Range<V>, Value> {
	/**
	 * m_set houses all the min/max values for each of the ranges added, each of which are mapped to a set of Ranges under which they fall
	 */
	protected RWLockedSortedSet<RangeIndexDomainMapping<V>> m_set; //to allow for fast Range selection
	protected RWLockedTreeMap<Range<V>, RWLockedSortedSet<Value>> m_map;  //to keep Range-->Handle mapping

	protected RWLockedSortedSet<RangeIndexDomainMapping<V>> m_set_builder; //to allow for fast Range selection
	protected RWLockedTreeMap<Range<V>, RWLockedSortedSet<Value>> m_map_builder;  //to keep Range-->Handle mapping

	/**
	 * Constructs an empty index
	 */
	public AbstractRangeIndex() {
		m_set = new RWLockedSortedArraySet<RangeIndexDomainMapping<V>>();
		m_set_builder = new RWLockedSortedArraySet<RangeIndexDomainMapping<V>>();
		m_map = new RWLockedTreeMap<Range<V>, RWLockedSortedSet<Value>>();
		m_map_builder = new RWLockedTreeMap<Range<V>, RWLockedSortedSet<Value>>();
	}

	/**
	 * Add a Value to the index for the given Range<V> key.
	 * Natural order of the keys is used internally. Value and V both should implement Comparable.
	 * <p/>
	 * put() makes use of two secondary variables: m_map_builder and m_set_builder for efficency sake.  In order to make
	 * the changes done by put actually take effect materialize() must be called.
	 *
	 * @param key the key object
	 * @param val the Range object
	 */
	@Override
	public void put(final Range<V> key, final Value val) {
		//add mapping from key-->value to m_map
		RWLockedSortedSet<Value> set = null;
		m_map_builder.writerLock();
		try {
			set = m_map_builder.get(key);
			if (set == null) {
				set = createSet();
				m_map_builder.put(key, set);
			}
		} finally {
			m_map_builder.writerUnlock();
		}
		set.writerLock();
		try {
			set.add(val);
		} finally {
			set.writerUnlock();
		}

		//either add or modify min/max in m_set.
		V v1 = key.getMin();
		V v2 = key.getMax();
		ArrayList<V> al = new ArrayList<V>(2);
		al.add(v1);
		al.add(v2);
		for (V v : al) {
			RangeIndexDomainMapping<V> m = new RangeIndexDomainMapping<V>(v);
			m_set_builder.writerLock();
			try {
				if (m_set_builder.contains(m)) {
					m_set_builder.tailSet(m).first().addRange(key);
				} else {
					SortedSet<Range<V>> tmpSet = new SortedArraySet<Range<V>>();
					tmpSet.add(key);
					RangeIndexDomainMapping<V> m2 = new RangeIndexDomainMapping<V>(v, tmpSet);
					m_set_builder.add(m2);
				}
			} finally {
				m_set_builder.writerUnlock();
			}
		}
	}

	/**
	 * This method will expand the incoming Range<V> to 1 or more V.  Then for each of these values
	 * it gets the ranges within which it falls and adds them to a set.  Finally, for each element in the set it looks
	 * up the Values from m_map and adds them to a final returnable SortedSet<Value>
	 * <p/>
	 * Note: only the min of the Range<V> key is used for query
	 *
	 * @param key the key object
	 * @return
	 */
	@Override
	public SortedSet<Value> get(Range<V> key) {
		V v = key.getMin();
		SortedSet<Range<V>> ss = getMatchingRanges(v);

		Iterator<Range<V>> i = ss.iterator();
		SortedSet<Value> retSet = new SortedArraySet<Value>();
		m_map.readerLock();
		try {
			while (i.hasNext()) {
				Range<V> r = i.next();
				RWLockedSortedSet<Value> tmpSet = m_map.get(r);
				if (tmpSet != null) {
					retSet.addAll(tmpSet);
				}
			}
		} finally {
			m_map.readerUnlock();
		}
		return retSet;
	}

	/**
	 * For a given V this method will attempt to find a matching RangeIndexDomainMapping in m_set. Four possible
	 * outcomes can result from this search:
	 * 1) V is not part of m_set
	 * 1a) V is smaller than the min of m_set
	 * 1b) V is larger than the max of m_set
	 * 1c) V is between two elements of m_set
	 * 2) V directly matches an element of m_set
	 * With all of case 1a, 1b nothing is returned.
	 * With case 1c the intersection between the previous and next elements are returned
	 * With case 2 the Ranges belonging to the matching set are returned.
	 *
	 * @param key
	 * @return
	 */
	private SortedSet<Range<V>> getMatchingRanges(V key) {
		SortedSet<Range<V>> set = new SortedArraySet<Range<V>>();
		m_set.readerLock();
		try {
			RangeIndexDomainMapping<V> lookupMapping = new RangeIndexDomainMapping<V>(key);
			SortedSet<RangeIndexDomainMapping<V>> tailSet = m_set.tailSet(lookupMapping);
			if (!tailSet.isEmpty()) {
				RangeIndexDomainMapping<V> prev = tailSet.first();
				//if we found an exact match
				if (prev.equals(lookupMapping)) {
					RWLockedSortedSet<Range<V>> tmpRanges = prev.getRanges();
					tmpRanges.readerLock();
					try {
						set.addAll(tmpRanges);
					} finally {
						tmpRanges.readerUnlock();
					}
				} else { //if we did not find an exact match must merge head.last and tail.first
					SortedSet<RangeIndexDomainMapping<V>> headSet = m_set.headSet(lookupMapping);
					if (headSet != null && !headSet.isEmpty()) {
						RangeIndexDomainMapping<V> next = headSet.last();

						RWLockedSortedSet<Range<V>> tailSetRanges = prev.getRanges();
						RWLockedSortedSet<Range<V>> headSetRanges = next.getRanges();

						tailSetRanges.readerLock();
						headSetRanges.readerLock();
						try {
							RangeDomainMappingSetIntersector intersector = new RangeDomainMappingSetIntersector(false);
							intersector.include(tailSetRanges, new RangeWeights());
							intersector.include(headSetRanges, new RangeWeights());
							set = intersector.intersect();
						} finally {
							tailSetRanges.readerUnlock();
							headSetRanges.readerUnlock();
						}
					}
				}
			}

		} finally {
			m_set.readerUnlock();
		}
		return set;
	}

	@Override
	public SortedSet<Value> get(List<Range<V>> ranges) {
		SortedSet<Value> retSet = new SortedArraySet<Value>();
		if (ranges != null) {
			for (Range<V> r : ranges) {
				SortedSet<Value> tmpSet = get(r);
				if (tmpSet != null) {
					retSet.addAll(tmpSet);
				}
			}
		}
		return retSet;
	}

	@Override
	public SortedSet<Value> get(Range<V> low, Range<V> high) {
		throw new UnsupportedOperationException("get(Range low, Range high) is not supported!!");
	}

	@Override
	public int getCount(Range<V> low, Range<V> high) {
		throw new UnsupportedOperationException("getCount(Range low, Range high) is not supported!!");
	}

	@Override
	public void clear() {
		m_map.writerLock();
		m_set.writerLock();
		try {
			m_map.clear();
			m_set.clear();
		} finally {
			m_map.writerUnlock();
			m_set.writerUnlock();
		}
	}

	@Override
	public Set<Range<V>> getKeys() {
		m_map.readerLock();
		try {
			return m_map.keySet();
		} finally {
			m_map.readerUnlock();
		}
	}

	@Override
	public void add(TreeMap<Range<V>, ArrayList<Value>> aMap) {
		for (Range<V> k : aMap.keySet()) {
			ArrayList<Value> list = aMap.get(k);
			for (Value v : list) {
				put(k, v);
			}
		}
	}

	/**
	 * Removes the key-value pair specified in the given map from the internal m_map.
	 *
	 * @param map - contains key-value pair of objects to be deleted
	 */
	@Override
	public void delete(Map<Range<V>, ArrayList<Value>> map) {
		for (Range<V> k : map.keySet()) {
			ArrayList<Value> list = map.get(k);
			if (list != null) {
				RWLockedSortedSet<Value> set = null;
				m_map.readerLock();
				try {
					set = m_map.get(k);
					if (set == null) {
						continue;
					}
				} finally {
					m_map.readerUnlock();
				}
				set.writerLock();
				try {
					set.removeAll(list);
				} finally {
					set.writerUnlock();
				}
				if (set.isEmpty()) {
					remove(k);
				}
			}
		}
	}

	/**
	 * Removes the entry for the specified key from m_map as well as removes that Range<V> from every element of m_set.
	 *
	 * @param key - key for entry to be deleted
	 */
	public void remove(Range<V> key) {
		m_map.writerLock();
		try {
			m_map.remove(key);
		} finally {
			m_map.writerUnlock();
		}

		m_set.readerLock();
		List<RangeIndexDomainMapping<V>> deleteList = new ArrayList<RangeIndexDomainMapping<V>>();
		try {
			Iterator<RangeIndexDomainMapping<V>> i = m_set.iterator();
			while (i.hasNext()) {
				RangeIndexDomainMapping<V> tmpElement = i.next();
				if (tmpElement != null) {
					RWLockedSortedSet<Range<V>> ranges = tmpElement.getRanges();
					ranges.writerLock();
					try {
						if (ranges != null && ranges.contains(key)) {
							ranges.remove(key);
						}
					} finally {
						ranges.writerUnlock();
					}
					ranges.readerLock();
					try {
						if (ranges == null || ranges.isEmpty()) {
							deleteList.add(tmpElement);
						}
					} finally {
						ranges.readerUnlock();
					}
				}
			}
		} finally {
			m_set.readerUnlock();
		}
		if (!deleteList.isEmpty()) {
			m_set.writerLock();
			try {
				m_set.removeAll(deleteList);
			} finally {
				m_set.writerUnlock();
			}
		}
	}

	/**
	 * Gets the count of values associated with a given key
	 * Use this method sparingly, this can be very exoensive
	 *
	 * @param key
	 * @return count of values associated with the key
	 */
	@Override
	public int getCount(final Range<V> range) {
		SortedSet<Value> set = get(range);
		return (set == null ? 0 : set.size());
	}

	/**
	 * Given a List of Key objects keys, returns the count of associated values.
	 * Use this method sparingly, this can be very exoensive
	 *
	 * @param keys a List of key objects
	 */
	@Override
	public int getCount(final ArrayList<Range<V>> keys) {
		int count = 0;
		for (Range<V> k : keys) {
			count += getCount(k);
		}
		return count;
	}

	/**
	 * This method must be called in order for put() to take effect.
	 */
	public void materialize() {
		m_map_builder.writerLock();
		m_map.writerLock();
		try {
			m_map.putAll(m_map_builder);
		} finally {
			try {
				m_map_builder.clear();
			} finally {
				m_map_builder.writerUnlock();
			}
			m_map.writerUnlock();
		}

		SortedSet<Range<V>> tmpSet = new SortedArraySet<Range<V>>();
		m_set_builder.writerLock();
		try {
			Iterator<RangeIndexDomainMapping<V>> iter = m_set_builder.iterator();
			while (iter.hasNext()) {
				RangeIndexDomainMapping<V> mapping = iter.next();

				SetDifference<Range<V>> tmpAdds = new SetDifference<Range<V>>(mapping.getRanges(), tmpSet);
				SetDifference<Range<V>> tmpSeenAlready = new SetDifference<Range<V>>(tmpSet, mapping.getRanges());
				tmpSet.removeAll(tmpSeenAlready);
				tmpSet.addAll(tmpAdds);

				mapping.getRanges().writerLock();
				try {
					mapping.getRanges().addAll(tmpSet);
				} finally {
					mapping.getRanges().writerUnlock();
				}

				m_set.writerLock();
				try {
					if (m_set.contains(mapping)) {
						m_set.tailSet(mapping).first().getRanges().writerLock();
						try {
							m_set.tailSet(mapping).first().getRanges().addAll(mapping.getRanges());
						} finally {
							m_set.tailSet(mapping).first().getRanges().writerUnlock();
						}
					} else {
						m_set.add(mapping);
					}
				} finally {
					m_set.writerUnlock();
				}
			}
		} finally {
			try {
				m_set_builder.clear();
			} finally {
				m_set_builder.writerUnlock();
			}
		}

	}

	/**
	 * Provides implementation of the SortedSet class
	 *
	 * @return a RWLockedSortedSet class instance
	 */
	protected RWLockedSortedSet<Value> createSet() {
		return new RWLockedSortedArraySet<Value>();
	}

}
