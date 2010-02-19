package com.tumri.joz.index;

import com.tumri.utils.data.RWLockedSortedArraySet;
import com.tumri.utils.data.RWLockedSortedSet;
import com.tumri.utils.data.RWLockedTreeMap;
import com.tumri.utils.data.SortedArraySet;
import com.tumri.utils.index.*;

import java.util.*;

/**
 * The purpose of this class is to index Range<V>-->Value mappings.  Where a query to the index for any given Range<V>
 * will return a SortedSet<Values> which contains all Value(s) which fall within the parameter Range<V>. The specific
 * implementation of IRangeValue on which the Ranges are created will determine how this index functions.
 * <p/>
 * User: scbraun
 * Date: Feb 9, 2010
 * Time: 11:47:06 AM
 */
public abstract class AbstractRangeIndex<attr, V, Value> extends AbstractIndex<Value, attr, Range<V>, Value> {
	/**
	 * m_set houses all the min/max values for each of the ranges added, each of which are mapped to a set of Ranges under which they fall
	 */
	protected RWLockedSortedSet<RangeDomainMapping<IRangeValue<V>, Range<V>>> m_set; //to allow for fast Range selection

	protected RWLockedTreeMap<Range<V>, RWLockedSortedSet<Value>> m_map;  //to keep Range-->Handle mapping

	/**
	 * Constructs an empty index
	 */
	public AbstractRangeIndex() {
		m_set = new RWLockedSortedArraySet<RangeDomainMapping<IRangeValue<V>, Range<V>>>();
		m_map = new RWLockedTreeMap<Range<V>, RWLockedSortedSet<Value>>();
	}

	/**
	 * Add a Value to the index for the given Range<V> key.
	 * Natural order of the keys is used internally. Value and V both should implement Comparable
	 *
	 * @param key the key object
	 * @param val the Range object
	 */
	@Override
	public void put(final Range<V> key, final Value val) {
		//add mapping from key-->value to m_map
		RWLockedSortedSet<Value> set = null;
		m_map.writerLock();
		try {
			set = m_map.get(key);
			if (set == null) {
				set = createSet();
				m_map.put(key, set);
			}
		} finally {
			m_map.writerUnlock();
		}
		set.writerLock();
		try {
			set.add(val);
		} finally {
			set.writerUnlock();
		}

		//add up to two new elements to m_set and mutate up to m_set.size() elements if necessary.
		m_set.readerLock();
		try {
			//iterate over entire set to see if we need to add this range.
			Iterator<RangeDomainMapping<IRangeValue<V>, Range<V>>> i = m_set.iterator();
			while (i.hasNext()) {
				RangeDomainMapping<IRangeValue<V>, Range<V>> tmp = i.next();
				if (key.contains(tmp.getKey())) {
					RWLockedSortedSet<Range<V>> tmpSet = tmp.getRanges();
					tmpSet.writerLock();
					try {
						if (!tmpSet.contains(key)) {
							tmpSet.add(key);
						}
					} finally {
						tmpSet.writerUnlock();
					}
				}
			}
		} finally {
			m_set.readerUnlock();
		}

		//either add or modify min/max in m_set.
		IRangeValue<V> v1 = key.getMin();
		IRangeValue<V> v2 = key.getMax();
		ArrayList<IRangeValue<V>> al = new ArrayList<IRangeValue<V>>(2);
		al.add(v1);
		al.add(v2);
		for (IRangeValue<V> v : al) {
			RangeDomainMapping<IRangeValue<V>, Range<V>> m = new RangeDomainMapping<IRangeValue<V>, Range<V>>(v);
			m_set.writerLock();
			try {
				if (m_set.contains(m)) {
					m_set.tailSet(m).first().addRange(key);
				} else {
					SortedSet<Range<V>> tmpSet = getMatchingRanges(v);
					if (tmpSet == null) { //handles boundary condition where nothing is found from getRanges()
						tmpSet = new SortedArraySet<Range<V>>();
					}
					tmpSet.add(key);
					RangeDomainMapping<IRangeValue<V>, Range<V>> m2 = new RangeDomainMapping<IRangeValue<V>, Range<V>>(v, tmpSet);
					m_set.add(m2);
				}
			} finally {
				m_set.writerUnlock();
			}
		}
	}

	/**
	 * This method will expand the incoming Range<V> to 1 or more IRangeValue<V>.  Then for each of these values
	 * it gets the ranges within which it falls and adds them to a set.  Finally, for each element in the set it looks
	 * up the Values from m_map and adds them to a final returnable SortedSet<Value>
	 *
	 * @param key the key object
	 * @return
	 */
	@Override
	public SortedSet<Value> get(Range<V> key) {
		List<IRangeValue<V>> values = getPossibleIRangeValues(key);
		SortedSet<Range<V>> ss = null;
		for (IRangeValue<V> v : values) {
			if (ss == null) {
				ss = getMatchingRanges(v);
			} else {
				ss.addAll(getMatchingRanges(v));
			}
		}
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
	 * For a given IRangeValue this method will attempt to find a matching RangeDomainMapping in m_set. Four possible
	 * outcomes can result from this search:
	 * 1) IRangeValue is not part of m_set
	 * 1a) IRangeValue is smaller than the min of m_set
	 * 1b) IRangeValue is larger than the max of m_set
	 * 1c) IRangeValue is between two elements of m_set
	 * 2) IRangeValue directly matches an element of m_set
	 * With all of case 1a, 1b nothing is returned.
	 * With case 1c the intersection between the previous and next elements are returned
	 * With case 2 the Ranges belonging to the matching set are returned.
	 *
	 * @param key
	 * @return
	 */
	private SortedSet<Range<V>> getMatchingRanges(IRangeValue<V> key) {
		SortedSet<Range<V>> set = new SortedArraySet<Range<V>>();
		m_set.readerLock();
		try {
			RangeDomainMapping<IRangeValue<V>, Range<V>> lookupMapping = new RangeDomainMapping<IRangeValue<V>, Range<V>>(key);
			SortedSet<RangeDomainMapping<IRangeValue<V>, Range<V>>> tailSet = m_set.tailSet(lookupMapping);
			if (!tailSet.isEmpty()) {
				RangeDomainMapping<IRangeValue<V>, Range<V>> prev = tailSet.first();
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
					SortedSet<RangeDomainMapping<IRangeValue<V>, Range<V>>> headSet = m_set.headSet(lookupMapping);
					if (headSet != null && !headSet.isEmpty()) {
						RangeDomainMapping<IRangeValue<V>, Range<V>> next = headSet.last();

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
		List<RangeDomainMapping<IRangeValue<V>, Range<V>>> deleteList = new ArrayList<RangeDomainMapping<IRangeValue<V>, Range<V>>>();
		try {
			Iterator<RangeDomainMapping<IRangeValue<V>, Range<V>>> i = m_set.iterator();
			while (i.hasNext()) {
				RangeDomainMapping<IRangeValue<V>, Range<V>> tmpElement = i.next();
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
	 * This expands a Range<V> into all possible IRangeValue<V> inclusive of min,max.
	 *
	 * @param r
	 * @return
	 */
	private List<IRangeValue<V>> getPossibleIRangeValues(Range<V> r) {
		List<IRangeValue<V>> retList = new ArrayList<IRangeValue<V>>();
		IRangeValue<V> min = r.getMin();
		IRangeValue<V> max = r.getMax();

		while (min.lessThanEqualTo(max)) {
			retList.add(min);
			min = min.getNext();
		}
		return retList;
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
