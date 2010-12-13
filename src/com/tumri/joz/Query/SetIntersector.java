package com.tumri.joz.Query;

import com.tumri.joz.filter.IFilter;
import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.ranks.IWeight;
import com.tumri.joz.ranks.IWeightGradedSetWrapper;
import com.tumri.utils.data.*;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */

/**
 * Interset a set of sorted sets
 * The function tries to achieve the following
 * S1 ^ S2 ^ ~S3 ^ S4 ....
 * Where ~S3 indicates exclusion set S3
 */
public abstract class SetIntersector<Value> implements SortedSet<Value> {
	static Logger log = Logger.getLogger(SetIntersector.class);
	public static int MAXRET = 24;

	private boolean m_strict = false;
	private ArrayList<SortedSet<Value>> m_includes;
	private ArrayList<IWeight<Value>> m_includesWeight;
	protected ArrayList<IFilter<Value>> m_filters;
	protected ArrayList<IWeight<Value>> m_filtersWeight;
	protected ArrayList<SortedSet<Value>> m_excludes;
	protected ArrayList<IWeight<Value>> m_excludesWeight;
	private static Random g_random = new Random(System.currentTimeMillis());

	private SortedSet<Value> m_rankedSet;
	private IWeight<Value> m_rankedSetWeight;

	private int m_maxSetSize = MAXRET;
	private Value m_reference; // Reference point is used as a starting point of set intersection
	// Computable fields, used as short hands in the code
	private int m_incSize;
	private int m_filterSize;
	private int m_excSize;
	private int m_iZeroSize = 0; // Incremented whenever include() sees zero size set
	private int m_eZeroSize = 0; // Incremented whenever exclude() sees zero size set
	private int m_listSize = 0;

	private double alpha = 1.0;

	private boolean useTopK;

	/**
	 * Given a Value v and a score build the Result object.
	 *
	 * @param v
	 * @param score
	 * @return a Pair<Value,Double>
	 */
	public abstract Value getResult(Value v, Double score);

	public abstract SetIntersector<Value> clone() throws CloneNotSupportedException;

	protected abstract SortedSet<Value> getTopKResults(List<SortedSet<Value>> sets, int numReqs, boolean strict, double alpha);

	public SetIntersector(boolean strict) {
		this();
		m_strict = strict;
	}

	/**
	 * Gets the reference point of staring the intersection, default is first()
	 *
	 * @return reference value
	 */
	public Value getReference() {
		return m_reference;
	}

	/**
	 * Sets the reference point of staring the intersection, default is first()
	 *
	 * @param aReference value
	 */
	@SuppressWarnings("unchecked")
	public void setReference(Value aReference) {
		m_reference = aReference;
		if (m_reference != null && m_rankedSet == null) {
			//Make sure that the starting reference is set randomly within the first set
			SortedSet<Value> firstSet = m_includes.get(0);
			//todo: lock firstSet
			if (!firstSet.isEmpty()) {
				Value last = firstSet.last();
				SortedSet<Value> tSet = firstSet.tailSet(m_reference);
				if (tSet.isEmpty()) { //if true this means that we fell into the buckect of handles after the last handle of the first set
					m_reference = distance(last, m_reference) == 1L ? firstSet.first() : (Value) ((IRandom) firstSet).random(g_random);
				} else if (tSet.first() == firstSet.first()) { // if true means that we fell into the bucket of handles before the first handle of the first set
					m_reference = (Value) ((IRandom) firstSet).random(g_random);
				} //else we m_reference is within the bucket of handles of the first set

			}
			for (int i = 0; i < m_includes.size(); i++) {
				SortedSet<Value> lValues = m_includes.get(i);
				boolean doLock = (lValues instanceof RWLocked);
				try {
					if (doLock)
						((RWLocked) lValues).readerLock();
					m_includes.set(i, new SortedSplitSet<Value>(lValues, m_reference));
				} finally {
					if (doLock) {
						((RWLocked) lValues).readerUnlock();
					}
				}
			}
		}
	}

	/**
	 * Overriden in ProductSetIntersector
	 *
	 * @param v1
	 * @param v2
	 * @return
	 */
	protected long distance(Value v1, Value v2) {
		return -1L;
	}

	private SetIntersector() {
		m_includes = new ArrayList<SortedSet<Value>>();
		m_includesWeight = new ArrayList<IWeight<Value>>();
		m_filters = new ArrayList<IFilter<Value>>();
		m_filtersWeight = new ArrayList<IWeight<Value>>();
		m_excludes = new ArrayList<SortedSet<Value>>();
		m_excludesWeight = new ArrayList<IWeight<Value>>();
	}

	protected SetIntersector(SetIntersector<Value> other) {
		m_includes = other.m_includes;
		m_includesWeight = other.m_includesWeight;
		m_filters = other.m_filters;
		m_filtersWeight = other.m_filtersWeight;
		m_excludes = other.m_excludes;
		m_excludesWeight = other.m_excludesWeight;
		m_strict = other.m_strict;
		m_maxSetSize = other.m_maxSetSize;
		m_reference = other.m_reference;
		m_incSize = other.m_incSize;
		m_filterSize = other.m_filterSize;
		m_excSize = other.m_excSize;
		m_iZeroSize = other.m_iZeroSize;
		m_eZeroSize = other.m_eZeroSize;
		m_listSize = other.m_listSize;
		m_rankedSet = other.m_rankedSet;
		m_rankedSetWeight = other.m_rankedSetWeight;
		useTopK = other.useTopK;
		alpha = other.alpha;
	}

	public boolean isStrict() {
		return m_strict;
	}

	/**
	 * Set to be included in the conjunction
	 *
	 * @param set    SortedSet of values to be include in the intersection
	 * @param weight of a Value is computed using weight object
	 */
	public void include(SortedSet<Value> set, IWeight<Value> weight) {
		if ((set != null && set.size() > 0) || ((set == null || set.size() == 0) && weight.mustMatch())) {
			if (set == null) {
				set = new SortedArraySet<Value>();
			}
			boolean added = false;
			for (int i = 0; i < m_includes.size(); i++) {
				SortedSet<Value> lValues = m_includes.get(i);
				IWeight<Value> lWeight = m_includesWeight.get(i);
				if ((set.size() < lValues.size() && (lWeight.mustMatch() == weight.mustMatch() || isStrict())) ||
						(!lWeight.mustMatch() && weight.mustMatch())) { // insert in a sorted order with smallest set first
					m_includes.add(i, set);
					m_includesWeight.add(i, weight);
					added = true;
					break;
				}
			}
			if (!added) {
				m_includes.add(set);
				m_includesWeight.add(weight);
			}
		} else {
			m_iZeroSize++;
		}
		m_incSize = m_includes.size();
		setListSize();
	}

	public SortedSet<Value> getRankedSet() {
		return m_rankedSet;
	}

	/**
	 * Single Rank ordered set can be included in the intersector. If the set is included then the intersector proceeds differently
	 *
	 * @param aRankedSet
	 */
	public void includeRankedSet(SortedSet<Value> aRankedSet, IWeight<Value> weight) {
		m_rankedSet = aRankedSet;
		m_rankedSetWeight = weight;
		setListSize();
	}

	public boolean hasIncludes() {
		return m_includes.size() > 0;
	}

	public boolean hasExcludes() {
		return m_excludes.size() > 0;
	}

	public boolean hasFilters() {
		return m_filters.size() > 0;
	}


	/**
	 * Complement of set to be added to conjunction
	 *
	 * @param set    SortedSet of values to be include in the intersection
	 * @param weight of a Value is computed using weight object
	 */
	public void exclude(SortedSet<Value> set, IWeight<Value> weight) {
		if (set != null && set.size() > 0) {
			m_excludes.add(set);
			m_excludesWeight.add(weight);
		} else {
			m_eZeroSize++;
		}
		m_excSize = m_excludes.size();
		setListSize();
	}

	/**
	 * Add a filter to the conjunction, filter acts as an alternative to an index
	 *
	 * @param aFilter filter used for accepting matches
	 * @param weight  of a Value is computed using weight object
	 */
	public void addFilter(IFilter<Value> aFilter, IWeight<Value> weight) {
		m_filters.add(aFilter);
		m_filtersWeight.add(weight);
		m_filterSize = m_filters.size();
		setListSize();
	}

	public void setMax(int size) {
		m_maxSetSize = size;
		if (size <= 0) {
			if (m_includes.size() > 0)
				m_maxSetSize = m_includes.get(0).size();
			else if (m_rankedSet != null)
				m_maxSetSize = m_rankedSet.size();
			else
				m_maxSetSize = MAXRET;
		}
	}

	public int getMax() {
		return m_maxSetSize;
	}

	/**
	 * Create the returnSet with same length as includes sets + exclude sets
	 * Each of the entry is set to ArrayList for now
	 * Also m_zeroSize is used to avoid false accurate matches with zero sized weights
	 */
	public SortedSet<Value> intersect() {
		ArrayList<ArrayList<Value>> lists = createLists();
		intersect(lists, null, m_maxSetSize, true);
		return buildReturnSet(lists, m_maxSetSize);
	}

	protected ArrayList<ArrayList<Value>> createLists() {
		ArrayList<ArrayList<Value>> lists = new ArrayList<ArrayList<Value>>();
		int cnt = m_listSize;
		if (cnt == 0 || useTopK) {
			lists.add(new ArrayList<Value>());
		} else {
			for (int i = 0; i < cnt; i++) {
				lists.add(new ArrayList<Value>());
			}
		}

		return lists;
	}

	/**
	 * Adds a set element to the correct set in returnsets.
	 *
	 * @param matches number of required matches met so far
	 * @param element the element to be added
	 * @param score   the score of element match
	 * @return returns true if max number of exact results have been found
	 */
	private boolean addResult(ArrayList<ArrayList<Value>> lists, int matches, Value element,
	                          double score, int max) {
		int index = m_listSize - 1 - matches;
		// Two cases to consider:
		// 1. Strict is set, index == 0 AND less than max results filled
		// 2. Strict is not set AND less than max results filled
		if ((!isStrict() || index == 0) && resultsSize(lists, index) < max)
			lists.get(index).add(getResult(element, score));
		// We are done in two cases:
		// 1. the list at m_iZeroSize has exceeded max
		// 2. Strict was set and at least one of the included set was zero size (m_iZeroSize > 0)
		return (lists.get(m_iZeroSize).size() >= max || (isStrict() && m_iZeroSize > 0));
	}

	private int resultsSize(ArrayList<ArrayList<Value>> lists, int index) {
		int count = 0;
		for (int i = 0; i < index && i < lists.size(); i++) {
			count += lists.get(i).size();
		}
		return count;
	}

	private void print() {
		for (int i = 0; i < m_includes.size(); i++) {
			Iterator<Value> lValues = m_includes.get(i).iterator();
			while (lValues.hasNext()) {
				Value lValue = lValues.next();
				System.out.println(lValue);
			}
		}
	}

	protected Value rankedIntersect(ArrayList<ArrayList<Value>> lists, Value cPointer, int count) {
		lock();
		try {
			boolean last = false;
			for (Value v : m_rankedSet) {
				if (last) return v;
				if (cPointer != null && !cPointer.equals(v))
					continue;
				cPointer = null;
				int match = containsInt(v); // NB: match can be zero
				if (match >= 0 && addResult(lists, match, v, m_rankedSetWeight.getWeight(v, 0.0), count)) {
					last = true;
				}
			}
		} finally {
			unlock();
		}
		return null;
	}

	/**
	 * Starts walking all the sorted sets, till one of them reaches the end point
	 * cPointer points to a set member
	 * variable score is incremented each time a positive match happens, based on the total score the
	 * item pointed to by cPointer is added to one of the entries in returnSet
	 *
	 * @param lists,    the lists array is filled with results, lists should be created using createLists()
	 * @param cPointer, start point of intersection, if null is provided then end points are used
	 * @param count,    number of strict matches to be found
	 * @param first,    which end point of the set to use, starting or ending
	 */
	protected Value intersect(ArrayList<ArrayList<Value>> lists, Value cPointer, int count, boolean first) {
		if (useTopK) {
			return getTopK(lists);
		}
		if (m_rankedSet != null) {
			return rankedIntersect(lists, cPointer, count);
		}
		if (m_incSize == 0) {
			return null;
		} else if (m_incSize == 1 && first) {
			return intersectSimple(lists, cPointer, count);
		}
		try {
			lock();
			cPointer = locateElement(m_includes.get(0), cPointer, first);
			int itemVisitCount = 1, itemLookupCount = 0; // Performace indicator for how many items were visited
			int loopcount = 0;
			while (cPointer != null) {
				int matches = 0; // score for the cPointer matches
				double totalWeight = 1.0;
				loopcount++;
				totalWeight *= m_includesWeight.get(0).getWeight(cPointer, 0.0);
				// Inner loops runs as many times as the size of the sets
				Value nextPointer = null;
				for (int i = 1; i < m_incSize && totalWeight > 0.0; i++) {
					itemLookupCount++;
					nextPointer = locateElement(m_includes.get(i), cPointer, first);
					if (nextPointer == null || !nextPointer.equals(cPointer)) {
						for (int j = i; j < m_incSize && totalWeight > 0.0; j++) {
							if (m_includesWeight.get(j).mustMatch()) totalWeight = 0.0;
						}
						break;
					}
					IWeight<Value> w = m_includesWeight.get(i);
					matches++; // += w.match(cPointer);
					totalWeight *= w.getWeight(nextPointer, 0.0);
				}
				for (int i = 0; i < m_filterSize && totalWeight > 0.0; i++) {
					IWeight<Value> w = m_filtersWeight.get(i);
					if (!m_filters.get(i).accept(cPointer)) {
						if (w.mustMatch()) totalWeight = 0.0;
						continue;
					}
					matches++; // += w.match(cPointer);
					totalWeight *= w.getWeight(cPointer, 0.0);
				}
				for (int i = 0; i < m_excSize && totalWeight > 0.0; i++) {
					itemLookupCount++;
					IWeight<Value> w = m_excludesWeight.get(i);
					if (m_excludes.get(i).contains(cPointer)) {
						if (w.mustMatch()) totalWeight = 0.0;
						continue;
					}
					matches++; //= w.match(cPointer);
					totalWeight *= w.getWeight(cPointer, 0.0);
				}
				Value v = cPointer;
				cPointer = ((nextPointer == null || cPointer.equals(nextPointer)) ?
						adjuscentElement(m_includes.get(0), cPointer, first) :
						locateElement(m_includes.get(0), nextPointer, first));
				if (totalWeight > 0.0 && addResult(lists, matches, v, totalWeight, count)) {
					break;
				}
				itemVisitCount++;
			}
			//System.out.print("visit: "+itemVisitCount + " lookup: "+itemLookupCount+" loopcount: "+loopcount);
		} finally {
			unlock();
		}
		return cPointer;
	}

	public void useTopK(boolean useTopK) {
		this.useTopK = useTopK;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	@SuppressWarnings("unchecked")
	private Value getTopK(ArrayList<ArrayList<Value>> lists) {
		List<SortedSet<Value>> sets = new ArrayList<SortedSet<Value>>();
		for (int i = 0; i < m_includes.size(); i++) {
			SortedSet<Value> set = m_includes.get(i);
			IWeight<Value> wt = m_includesWeight.get(i);
			GradedSetWrapper<Value> iWeightGS = new IWeightGradedSetWrapper(set, wt);
			sets.add(iWeightGS);
		}

		if (m_rankedSet != null) {
			SortedSet<Value> m_rankedSet2 = new SortedArraySet<Value>();
			m_rankedSet2.addAll(m_rankedSet);
			GradedSetWrapper<Value> keywordGradedSet = new KeywordGradedSetWrapper(m_rankedSet2);
			sets.add(keywordGradedSet);
		}

		SortedSet<Value> topKResults = getTopKResults(sets, m_maxSetSize, m_strict, alpha);
		Iterator<Value> iter = topKResults.iterator();
		ArrayList<Value> firstList = lists.get(0);
		while (iter.hasNext()) {
			firstList.add(iter.next());
		}
		return null;
	}

	protected Value intersectSimple(ArrayList<ArrayList<Value>> lists, Value cPointer, int count) {
		try {
			lock();
			int itemVisitCount = 1, itemLookupCount = 0; // Performace indicator for how many items were visited
			int loopcount = 0;
			SortedSet<Value> single = m_includes.get(0);
			single = (cPointer == null ? single : single.tailSet(cPointer));
			boolean done = false;
			for (Value value : single) {
				cPointer = value; // in case we are done then the next pointer is set correctly
				if (done)
					break;
				int matches = 0; // score for the cPointer matches
				double totalWeight = m_includesWeight.get(0).getWeight(cPointer, 0.0);
				loopcount++;
				for (int i = 0; i < m_filterSize && totalWeight > 0.0; i++) {
					IWeight<Value> w = m_filtersWeight.get(i);
					if (!m_filters.get(i).accept(cPointer)) {
						if (w.mustMatch()) totalWeight = 0.0;
						continue;
					}
					matches++; // += w.match(cPointer);
					totalWeight *= w.getWeight(cPointer, 0.0);
				}
				for (int i = 0; i < m_excSize && totalWeight > 0.0; i++) {
					itemLookupCount++;
					IWeight<Value> w = m_excludesWeight.get(i);
					if (m_excludes.get(i).contains(cPointer)) {
						if (w.mustMatch()) totalWeight = 0.0;
						continue;
					}
					matches++; //= w.match(cPointer);
					totalWeight *= w.getWeight(cPointer, 0.0);
				}
				if (totalWeight > 0.0 && addResult(lists, matches, cPointer, totalWeight, count)) {
					done = true;
				}
				cPointer = null; // will be set right before exit from loop, if this is last element then it should be null
				itemVisitCount++;
			}
			//System.out.print("visit: "+itemVisitCount + " lookup: "+itemLookupCount+" loopcount: "+loopcount);
		} finally {
			unlock();
		}
		return cPointer;
	}

	//todo: add method lock(SortedSet<Value> set)--do same checks

	private void lock() {
		for (SortedSet<Value> lValues : m_includes) {
			if (lValues instanceof RWLocked) {
				((RWLocked) lValues).readerLock();
			}
		}
		for (SortedSet<Value> lValues : m_excludes) {
			if (lValues instanceof RWLocked) {
				((RWLocked) lValues).readerLock();
			}
		}
	}

	private void unlock() {
		for (SortedSet<Value> set : m_includes) {
			if (set instanceof RWLocked) {
				((RWLocked) set).readerUnlock();
			}
		}
		for (SortedSet<Value> set : m_excludes) {
			if (set instanceof RWLocked) {
				((RWLocked) set).readerUnlock();
			}
		}
	}

	/**
	 * Builds a MultiSortedSet object
	 *
	 * @return sorted set
	 */
	private SortedSet<Value> buildReturnSet(ArrayList<ArrayList<Value>> lists, int max) {
		SortedSet<Value> ret;
		if (lists.size() > 0) {
			ArrayList<Value> list = lists.get(0);
			for (int i = 1; list.size() < max && i < lists.size(); i++) {
				list.addAll(lists.get(i));
			}
			ret = new SortedArraySet<Value>(list);
			if (m_reference != null) {
				ret = new SortedSplitSet<Value>(ret, getResult(m_reference, 1.0));
			}
		} else {
			ret = new SortedArraySet<Value>();
		}
		return ret;
	}

	protected ArrayList<SortedSet<Value>> getIncludes() {
		return m_includes;
	}

	private Value locateElement(SortedSet<Value> set, Value current, boolean first) {
		return (first ? firstElement(set, current) : lastElement(set, current));
	}

	private Value adjuscentElement(SortedSet<Value> set, Value current, boolean next) {
		return (next ? nextElement(set, current) : prevElement(set, current));
	}

	/**
	 * Find the first Element e such that either e == current or e follows current in the set
	 *
	 * @param set     the set to use
	 * @param current element, if null then the first() of the set is returned
	 * @return element e that is either e==current or e follows current in the set,
	 *         if end of the set is encountered then null is returned
	 */
	private Value firstElement(SortedSet<Value> set, Value current) {
		if (current == null)
			return (set.isEmpty() ? null : set.first());
		SortedSet<Value> tail = set.tailSet(current);
		return (!tail.isEmpty() ? tail.first() : null);
	}

	/**
	 * Find the first Element e such that either e == current or e preceeds current in the set
	 *
	 * @param set     the set to use
	 * @param current element, if null then the last() of the set is returned
	 * @return element e that is either e==current or e preceeds current in the set,
	 *         if end of the set is encountered then null is returned
	 */
	private Value lastElement(SortedSet<Value> set, Value current) {
		if (current == null)
			return (set.isEmpty() ? null : set.last());
		if (set.contains(current))
			return current;
		SortedSet<Value> head = set.headSet(current);
		return (!head.isEmpty() ? head.last() : null);
	}

	/**
	 * Returns the Value that would follow the Value current in the set
	 *
	 * @param set     the set
	 * @param current the Value
	 * @return the element that would follow the Value current, returns null if current is null
	 */
	private Value nextElement(SortedSet<Value> set, Value current) {
		if (current != null) {
			Iterator<Value> tail = set.tailSet(current).iterator();
			if (tail.hasNext()) {
				Value v = tail.next();
				if (!v.equals(current))
					return v;
				if (tail.hasNext())
					return tail.next();
			}
		}
		return null;
	}

	/**
	 * Returns the Value that would preceed the Value current in the set
	 *
	 * @param set     the set
	 * @param current the Value
	 * @return the element that would preceed the Value current, returns null if current is null
	 */
	private Value prevElement(SortedSet<Value> set, Value current) {
		if (current != null) {
			SortedSet<Value> head = set.headSet(current);
			return (head.isEmpty() ? null : head.last());
		}
		return null;
	}

	public SortedSet<Value> subSet(Value aValue, Value aValue1) {
		SetIntersector<Value> d = duplicate();
		ArrayList<SortedSet<Value>> includes = new ArrayList<SortedSet<Value>>();
		lock();
		try {
			for (SortedSet<Value> include : m_includes) {
				includes.add(include.subSet(aValue, aValue1));
			}
		} finally {
			unlock();
		}
		d.m_includes = includes;
		return d;
	}

	public SortedSet<Value> headSet(Value aValue) {
		SetIntersector<Value> d = duplicate();
		ArrayList<SortedSet<Value>> includes = new ArrayList<SortedSet<Value>>();
		lock();
		try {
			for (SortedSet<Value> include : m_includes) {
				includes.add(include.headSet(aValue));
			}
		} finally {
			unlock();
		}
		d.m_includes = includes;
		return d;
	}

	public SortedSet<Value> tailSet(Value aValue) {
		SetIntersector<Value> d = duplicate();
		ArrayList<SortedSet<Value>> includes = new ArrayList<SortedSet<Value>>();
		lock();
		try {
			for (SortedSet<Value> include : m_includes) {
				includes.add(include.tailSet(aValue));
			}
		} finally {
			unlock();
		}
		d.m_includes = includes;
		return d;
	}

	private SetIntersector<Value> duplicate() {
		SetIntersector<Value> d = null;
		try {
			d = this.clone();
		} catch (CloneNotSupportedException e) {
		}
		return d;
	}

	public Value first() {
		ArrayList<ArrayList<Value>> lists = createLists();
		intersect(lists, null, 1, true);
		if (lists.get(0).size() > 0)
			return lists.get(0).get(0);
		throw new NoSuchElementException();
	}

	public Value last() {
		ArrayList<ArrayList<Value>> lists = createLists();
		intersect(lists, null, 1, false);
		if (lists.get(0).size() > 0)
			return lists.get(0).get(0);
		throw new NoSuchElementException();
	}

	@SuppressWarnings({"unchecked"})
	public Comparator<? super Value> comparator() {
		if (m_rankedSet != null && !m_rankedSet.isEmpty()) {
			return new ProductHandle(1.0, 1L);
		} else {
			return m_includes.isEmpty() ? null : m_includes.get(0).comparator();
		}
	}


	public int size() {
		ArrayList<ArrayList<Value>> lists = createLists();
		int max = Math.min(m_maxSetSize, m_includes.get(0).size());
		intersect(lists, null, max, true);
		log.warn("Size operator called on setIntersector");
		return lists.get(0).size();
	}

	public boolean isEmpty() {
		ArrayList<ArrayList<Value>> lists = createLists();
		intersect(lists, null, 1, false);
		return (lists.get(0).size() == 0);
	}

	@SuppressWarnings("unchecked")
	public boolean contains(Object o) {
		lock();
		try {
			return (containsInt(o) == m_listSize);
		} finally {
			unlock();
		}
	}

	private void setListSize() {
		m_listSize = m_incSize + m_excSize + m_filterSize + m_iZeroSize + m_eZeroSize + ((m_rankedSet != null) ? 1 : 0);
	}

	/**
	 * Returns the number of queries that matched the set
	 *
	 * @param o
	 * @return integer value of number of matching sets/queries, returns -1 if exclusion requested
	 */
	@SuppressWarnings("unchecked")
	private int containsInt(Object o) {
		int match = 0;
		int i = 0;
		for (SortedSet<Value> lInclude : m_includes) {
			if (lInclude.contains(o))
				match++;
			else if (m_includesWeight.get(i).mustMatch())
				return -1;
			i++;
		}
		i = 0;
		for (SortedSet<Value> lExclude : m_excludes) {
			if (!lExclude.contains(o))
				match++;
			else if (m_excludesWeight.get(i).mustMatch())
				return -1;
			i++;
		}
		i = 0;
		for (IFilter<Value> lFilter : m_filters) {
			if (lFilter.accept((Value) o))
				match++;
			else if (m_filtersWeight.get(i).mustMatch())
				return -1;
			i++;
		}
		return match;
	}

	public Iterator<Value> iterator() {
		return new SetIntersectorIterator();
	}

	public Object[] toArray() {
		ArrayList<ArrayList<Value>> lists = createLists();
		int max = m_includes.get(0).size();
		intersect(lists, null, max, true);
		log.warn("toArray operator called on setIntersector");
		return lists.get(0).toArray();
	}

	public <T> T[] toArray(T[] aTs) {
		ArrayList<ArrayList<Value>> lists = createLists();
		int max = m_includes.get(0).size();
		intersect(lists, null, max, true);
		log.warn("toArray operator called on setIntersector");
		return lists.get(0).toArray(aTs);
	}

	public boolean add(Value aValue) {
		throw new UnsupportedOperationException();
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean containsAll(Collection<?> aObjects) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection<? extends Value> aValues) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection<?> aObjects) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection<?> aObjects) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	class SetIntersectorIterator implements Iterator<Value> {
		private Value m_ref = null;
		private ArrayList<ArrayList<Value>> m_lists;
		private Iterator<Value> m_iter;
		private boolean m_fallback = true; // If true then less accurate results are included
		private int count = 0; //Keep track of the number of items returned by this iterator.

		private SetIntersectorIterator() {
			m_lists = createLists();
			fill();
		}

		public boolean hasNext() {
			//Abort if we have already returned upto max size of the bounded set
			if (count >= m_maxSetSize) {
				return false;
			}
			if (m_iter != null && !m_iter.hasNext()) {
				m_iter = null; // We need to refill from the intersector
				if (!done()) { // refill only if not done yet
					fill();      // fill will create the m_iter object
				}
			}
			return (m_iter != null && m_iter.hasNext());
		}

		public Value next() {
			if (m_iter != null && count < m_maxSetSize) {
				count++;
				return m_iter.next();
			}
			throw new NoSuchElementException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		private void fill() {
			ArrayList<Value> firstList = m_lists.get(0);
			firstList.clear();
			m_ref = intersect(m_lists, m_ref, m_maxSetSize, true);
			// If at firsttime while filling if we get CHUNK number of products then we don't fallback ever
			if (m_fallback && m_lists.get(0).size() >= m_maxSetSize) {
				m_fallback = false;
			}
			// If fallback is true that means we didn't get enough results, so use other lists
			if (m_fallback) {
				for (int i = 1; i < m_lists.size(); i++) {
					ArrayList<Value> lv = m_lists.get(i);
					for (Value v : lv) {
						firstList.add(v);
					}
				}
			}
			m_iter = firstList.iterator();
		}

		private boolean done() {
			return (m_ref == null);
		}
	}
}
