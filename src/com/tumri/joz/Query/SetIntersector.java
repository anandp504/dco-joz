package com.tumri.joz.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import com.tumri.joz.filter.IFilter;
import com.tumri.joz.ranks.IWeight;
import com.tumri.utils.data.RWLocked;
import com.tumri.utils.data.SortedArraySet;
import com.tumri.utils.data.SortedSplitSet;

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
 *
 */
public abstract class SetIntersector<Value> implements SortedSet<Value> {
  public static int MAXRET = 1000;

  private boolean m_strict = false;
  private ArrayList<SortedSet<Value>> m_includes;
  private ArrayList<IWeight<Value>> m_includesWeight;
  private ArrayList<IFilter<Value>> m_filters;
  private ArrayList<IWeight<Value>> m_filtersWeight;
  private ArrayList<SortedSet<Value>> m_excludes;
  private ArrayList<IWeight<Value>> m_excludesWeight;

  private int m_maxSetSize = MAXRET;
  private Value m_reference; // Reference point is used as a starting point of set intersection
  // Computable fields, used as short hands in the code
  private int m_incSize;
  private int m_filterSize;
  private int m_excSize;
  private int m_zeroSize = 0; // Incremented whenever include() or exclude() sees zero size set

  /**
   * Given a Value v and a score build the Result object.
   * @param v
   * @param score
   * @return a Pair<Value,Double>
   */
  public abstract Value getResult(Value v, Double score);
  public abstract SetIntersector<Value> clone() throws CloneNotSupportedException;

  /**
   * Gets the reference point of staring the intersection, default is first()
   * @return reference value
   */
  public Value getReference() {
    return m_reference;
  }

  /**
   * Sets the reference point of staring the intersection, default is first()
   * @param aReference value
   */
  public void setReference(Value aReference) {
    m_reference = aReference;
    markReference();
  }

  public SetIntersector() {
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
    m_zeroSize = other.m_zeroSize;
  }

  public boolean isStrict() {
    return m_strict;
  }

  public void setStrict(boolean aStrict) {
    m_strict = aStrict;
  }

  /**
   * Set to be included in the conjunction
   *
   * @param set SortedSet of values to be include in the intersection
   * @param weight of a Value is computed using weight object
   */
  public void include(SortedSet<Value> set, IWeight<Value> weight) {
    if (set != null && set.size() > 0) {
      boolean added = false;
      for (int i = 0; i < m_includes.size(); i++) {
        SortedSet<Value> lValues = m_includes.get(i);
        if (set.size() < lValues.size()) { // insert in a sorted order with smallest set first
          m_includes.add(i,set);
          m_includesWeight.add(weight);
          added = true;
          break;
        }
      }
      if (!added) {
        m_includes.add(set);
        m_includesWeight.add(weight);
      }
    } else {
        m_zeroSize++;
    }
    m_incSize = m_includes.size();
  }

  public boolean hasIncludes() {
    return m_includes.size() > 0;
  }

  
  /**
   * Complement of set to be added to conjunction
   *
   * @param set SortedSet of values to be include in the intersection
   * @param weight of a Value is computed using weight object
   */
  public void exclude(SortedSet<Value> set, IWeight<Value> weight) {
    if (set != null && set.size() > 0) {
      m_excludes.add(set);
      m_excludesWeight.add(weight);
    } else {
      m_zeroSize++;
    }
    m_excSize = m_excludes.size();
  }

  /**
   * Add a filter to the conjunction, filter acts as an alternative to an index
   *
   * @param aFilter filter used for accepting matches
   * @param weight of a Value is computed using weight object
   */
  public void addFilter(IFilter<Value> aFilter, IWeight<Value> weight) {
    m_filters.add(aFilter);
    m_filtersWeight.add(weight);
    m_filterSize = m_filters.size();
  }

  public void setMax(int size) {
    m_maxSetSize = size;
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
    return buildReturnSet(lists,m_maxSetSize);
  }

  protected ArrayList<ArrayList<Value>> createLists() {
    ArrayList<ArrayList<Value>> lists = new ArrayList<ArrayList<Value>>();
    for (int i = 0; i < m_incSize + m_excSize + m_filterSize + m_zeroSize; i++) {
      lists.add(new ArrayList<Value>());
    }
    return lists;
  }

  /**
   * Adds a set element to the correct set in returnsets.
   * @param matches number of required matches met so far
   * @param element the element to be added
   * @param score the score of element match
   * @return
   */
  private boolean addResult(ArrayList<ArrayList<Value>> lists, int matches, Value element,
                            double score, int max) {
    int index = m_incSize + m_excSize + m_filterSize + m_zeroSize - 1 - matches;
    if ((!isStrict() || index == 0) && resultsSize(lists,index) < max)
      lists.get(index).add(getResult(element,score));
    return (lists.get(0).size() >= max);
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
  /**
   * Starts walking all the sorted sets, till one of them reaches the end point
   * cPointer points to a set member
   * variable score is incremented each time a positive match happens, based on the total score the
   * item pointed to by cPointer is added to one of the entries in returnSet
   *
   * @param lists, the lists array is filled with results, lists should be created using createLists()
   * @param cPointer, start point of intersection, if null is provided then end points are used
   * @param count, number of strict matches to be found
   * @param first, which end point of the set to use, starting or ending
   */
  protected Value intersect(ArrayList<ArrayList<Value>> lists, Value cPointer, int count, boolean first) {
    if (m_incSize == 0) {
      return null;
    }
    try {
      lock();
      int matches = 0; // score for the cPointer matches
      int setIndex = 0;
      double totalWeight = 1.0;
      cPointer = locateElement(m_includes.get(0),cPointer,first);
      int itemVisitCount = 1, itemLookupCount = 0; // Performace indicator for how many items were visited
      int loopcount=0;
      while (cPointer != null) {
        loopcount++;
        totalWeight *= m_includesWeight.get(0).getWeight(cPointer);
        // Inner loops runs as many times as the size of the sets
        Value nextPointer = null;
        for (int i = 1; i < m_incSize; i++) {
          itemLookupCount++;
          setIndex = i;
          nextPointer = locateElement(m_includes.get(i),cPointer,first);
          if (nextPointer == null || !nextPointer.equals(cPointer)) {
            break;
          }
          IWeight<Value> w = m_includesWeight.get(i);
          matches += w.match(cPointer);
          totalWeight *= w.getWeight(cPointer);
        }
        for (int i = 0; i < m_filterSize; i++) {
          if (!m_filters.get(i).accept(cPointer))
            continue;
          IWeight<Value> w = m_filtersWeight.get(i);
          matches += w.match(cPointer);
          totalWeight *= w.getWeight(cPointer);
        }
        for (int i = 0; i < m_excSize; i++) {
          itemLookupCount++;
          if (m_excludes.get(i).contains(cPointer))
            continue;
          IWeight<Value> w = m_excludesWeight.get(i);
          matches += w.match(cPointer);
          totalWeight *= w.getWeight(cPointer);
        }
        Value v = cPointer;
        cPointer = ((nextPointer == null || cPointer.equals(nextPointer))?
            adjuscentElement(m_includes.get(0), cPointer,first) :
            locateElement(m_includes.get(0),nextPointer,first));
        if (addResult(lists,matches, v, totalWeight, count)) {
          break;
        }
        matches = 0; totalWeight = 1.0;
        itemVisitCount++;
        setIndex++;
      }
      //System.out.print("visit: "+itemVisitCount + " lookup: "+itemLookupCount+" loopcount: "+loopcount);
    } finally {
      unlock();
    }
    return cPointer;
  }

  /**
   * Set the reference point for intersection
   */
  private void markReference() {
    if (m_reference != null) {
      for (int i = 0; i < m_includes.size(); i++) {
        SortedSet<Value> lValues = m_includes.get(i);
        m_includes.set(i,new SortedSplitSet<Value>(lValues,m_reference));
      }
    }
  }

  private void lock() {
    for (int i = 0; i < m_includes.size(); i++) {
      SortedSet<Value> lValues = m_includes.get(i);
      if (lValues instanceof RWLocked) {
        ((RWLocked) lValues).readerLock();
      }
    }
    for (int i = 0; i < m_excludes.size(); i++) {
      SortedSet<Value> lValues = m_excludes.get(i);
      if (lValues instanceof RWLocked) {
        ((RWLocked) lValues).readerLock();
      }
    }
  }

  private void unlock() {
    for (int i = 0; i < m_includes.size(); i++) {
      SortedSet<Value> lValues = m_includes.get(i);
      if (lValues instanceof RWLocked) {
        ((RWLocked) lValues).readerUnlock();
      }
    }
    for (int i = 0; i < m_excludes.size(); i++) {
      SortedSet<Value> lValues = m_excludes.get(i);
      if (lValues instanceof RWLocked) {
        ((RWLocked) lValues).readerUnlock();
      }
    }
  }

  /**
   * Builds a MultiSortedSet object
   * @return sorted set
   */
  private SortedSet<Value> buildReturnSet(ArrayList<ArrayList<Value>> lists, int max) {
    SortedSet<Value> ret;
    if (lists.size() > 0) {
      ArrayList<Value> list = lists.get(0);
      for (int i = 1;list.size() < max && i < lists.size(); i++) {
        list.addAll(lists.get(i));
      }
      ret = new SortedArraySet<Value>(list);
      if (m_reference != null) {
        ret = new SortedSplitSet<Value>(ret,getResult(m_reference,1.0));
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
    return (first ? firstElement(set,current) : lastElement(set,current));
  }
  private Value adjuscentElement(SortedSet<Value> set, Value current, boolean next) {
    return (next ? nextElement(set,current) : prevElement(set,current));
  }

  /**
   * Find the first Element e such that either e == current or e follows current in the set
   * @param set the set to use
   * @param current element, if null then the first() of the set is returned
   * @return element e that is either e==current or e follows current in the set,
   * if end of the set is encountered then null is returned
   */
  private Value firstElement(SortedSet<Value> set, Value current) {
    if (current == null)
      return (set.isEmpty() ? null : set.first());
    SortedSet<Value> tail = set.tailSet(current);
    return (!tail.isEmpty() ? tail.first(): null);
  }

  /**
   * Find the first Element e such that either e == current or e preceeds current in the set
   * @param set the set to use
   * @param current element, if null then the last() of the set is returned
   * @return element e that is either e==current or e preceeds current in the set,
   * if end of the set is encountered then null is returned
   */
  private Value lastElement(SortedSet<Value> set, Value current) {
    if (current == null)
      return (set.isEmpty() ? null : set.last());
    if (set.contains(current))
      return current;
    SortedSet<Value> head = set.headSet(current);
    return (!head.isEmpty() ? head.last(): null);
  }

  /**
   * Returns the Value that would follow the Value current in the set
   * @param set the set
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
   * @param set the set
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
    for (int i = 0; i < m_includes.size(); i++) {
      includes.add(m_includes.get(i).subSet(aValue,aValue1));
    }
    d.m_includes = includes;
    return d;
  }

  public SortedSet<Value> headSet(Value aValue) {
    SetIntersector<Value> d = duplicate();
    ArrayList<SortedSet<Value>> includes = new ArrayList<SortedSet<Value>>();
    for (int i = 0; i < m_includes.size(); i++) {
      includes.add(m_includes.get(i).headSet(aValue));
    }
    d.m_includes = includes;
    return d;
  }

  public SortedSet<Value> tailSet(Value aValue) {
    SetIntersector<Value> d = duplicate();
    ArrayList<SortedSet<Value>> includes = new ArrayList<SortedSet<Value>>();
    for (int i = 0; i < m_includes.size(); i++) {
      includes.add(m_includes.get(i).tailSet(aValue));
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
    intersect(lists,null,1, true);
    if (lists.get(0).size() > 0)
      return lists.get(0).get(0);
    throw new NoSuchElementException();
  }

  public Value last() {
    ArrayList<ArrayList<Value>> lists = createLists();
    intersect(lists,null,1, false);
    if (lists.get(0).size() > 0)
      return lists.get(0).get(0);
    throw new NoSuchElementException();
  }

  public Comparator<? super Value> comparator() {
    return null;
  }


  public int size() {
    ArrayList<ArrayList<Value>> lists = createLists();
    int max = m_includes.get(0).size();
    intersect(lists,null,max, true);
    return lists.get(0).size();
  }

  public boolean isEmpty() {
    ArrayList<ArrayList<Value>> lists = createLists();
    intersect(lists,null,1, false);
    return (lists.get(0).size() == 0);
  }

  public boolean contains(Object o) {
    throw new UnsupportedOperationException();
  }

  public Iterator<Value> iterator() {
    return new SetIntersectorIterator();
  }

  public Object[] toArray() {
    ArrayList<ArrayList<Value>> lists = createLists();
    int max = m_includes.get(0).size();
    intersect(lists,null,max, true);
    return lists.get(0).toArray();
  }

  public <T> T[] toArray(T[] aTs) {
    ArrayList<ArrayList<Value>> lists = createLists();
    int max = m_includes.get(0).size();
    intersect(lists,null,max, true);
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
    private final int CHUNK = 24;
    Value m_ref = null;
    ArrayList<ArrayList<Value>> m_lists;
    Iterator<Value> m_iter;
    boolean m_fallback = true; // If true then less accurate results are included

    SetIntersectorIterator() {
      m_lists = createLists();
      fill();
    }

    public boolean hasNext() {
      if (m_iter != null && !m_iter.hasNext()) {
        m_iter = null; // We need to refill from the intersector
        if (!done()) { // refill only if not done yet
          fill();      // fill will create the m_iter object
        }
      }
      return (m_iter != null && m_iter.hasNext());
    }

    public Value next() {
      if (m_iter != null) {
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
      m_ref = intersect(m_lists,m_ref,CHUNK,true);
      // If at firsttime while filling if we get CHUNK number of products then we don't fallback ever
      if (m_fallback && m_lists.get(0).size() >= CHUNK) {
        m_fallback = false;
      }
      // If fallback is true that means we didn't get enough results, so use other lists
      if (m_fallback) {
        for (int i = 1; i < m_lists.size(); i++) {
          ArrayList<Value> lv = m_lists.get(i);
          for (int j = 0; j < lv.size(); j++) {
            firstList.add(lv.get(j));
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