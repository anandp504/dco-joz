package com.tumri.joz.Query;

import com.tumri.joz.filter.Filter;
import com.tumri.joz.index.MultiSortedSet;
import com.tumri.joz.index.RWLocked;
import com.tumri.joz.index.SortedArraySet;
import com.tumri.joz.ranks.IWeight;
import com.tumri.joz.utils.Result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;

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
abstract public class SetIntersector<Value extends Comparable> {
  public static int MAXRET = 1000;
  private ArrayList<SortedSet<Value>> m_includes;
  private ArrayList<IWeight<Value>> m_includesWeight;
  private ArrayList<Filter<Value>> m_filters;
  private ArrayList<IWeight<Value>> m_filtersWeight;
  private ArrayList<SortedSet<Value>> m_excludes;
  private ArrayList<IWeight<Value>> m_excludesWeight;
  private int m_maxSetSize = MAXRET;
  // Temp class variables
  private ArrayList<ArrayList<Result>> m_returnList;
  private SortedSet<Result> m_returnSet;
  private int m_incSize;
  private int m_filterSize;
  private int m_excSize;

  /**
   * Given a Value v and a score build the Result object.
   * @param v
   * @param score
   * @return a Pair<Value,Double>
   */
  public abstract Result getResult(Value v, Double score);

  public SetIntersector() {
    m_includes = new ArrayList<SortedSet<Value>>();
    m_includesWeight = new ArrayList<IWeight<Value>>();
    m_filters = new ArrayList<Filter<Value>>();
    m_filtersWeight = new ArrayList<IWeight<Value>>();
    m_excludes = new ArrayList<SortedSet<Value>>();
    m_excludesWeight = new ArrayList<IWeight<Value>>();
  }

  /**
   * Set to be included in the conjunction
   *
   * @param set
   */
  public void include(SortedSet<Value> set, IWeight<Value> weight) {
    if (set != null && set.size() > 0) {
      m_includes.add(set);
      m_includesWeight.add(weight);
    }
  }

  public boolean hasIncludes() {
    return m_includes.size() > 0;
  }

  /**
   * Complement of set to be added to conjunction
   *
   * @param set
   */
  public void exclude(SortedSet<Value> set, IWeight<Value> weight) {
    if (set.size() > 0) {
      m_excludes.add(set);
      m_excludesWeight.add(weight);

    }
  }

  /**
   * Add a filter to the conjunction, filter acts as an alternative to an index
   *
   * @param aFilter
   */
  public void addFilter(Filter<Value> aFilter, IWeight<Value> weight) {
    m_filters.add(aFilter);
    m_filtersWeight.add(weight);
  }

  public void setMax(int size) {
    m_maxSetSize = size;
  }

  /**
   * Create the returnSet with same length as includes sets + exclude sets
   * Each of the entry is set to TreeSet for now
   */
  private void setup() {
    m_returnList = new ArrayList<ArrayList<Result>>();
    m_incSize = m_includes.size();
    m_filterSize = m_filters.size();
    m_excSize = m_excludes.size();
    for (int i = 0; i < m_incSize + m_excSize + m_filterSize; i++) {
      m_returnList.add(new ArrayList<Result>());
    }
  }

  /**
   * Adds a set element to the correct set in returnsets.
   * @param matches number of required matches met so far
   * @param element the element to be added
   * @param score the score of element match
   * @return
   */
  private boolean addResult(int matches, Value element, double score) {
    int index = m_incSize + m_excSize + m_filterSize - 1 - matches;
    if (resultsSize(index) < m_maxSetSize)
      m_returnList.get(index).add(getResult(element,score));
    return (m_returnList.get(0).size() >= m_maxSetSize);
  }

  private int resultsSize(int index) {
    int count = 0;
    for (int i = 0; i < index && i < m_returnList.size(); i++) {
      count += m_returnList.get(i).size();
    }
    return count;
  }

  private Value nextElement(SortedSet<Value> set, Value current) {
    if (set instanceof MultiSortedSet) {
      Value v = ((MultiSortedSet<Value>)set).nextElement(current);
      if (v != null) return v;
    }
    Iterator<Value> iter = set.iterator();
    if (iter.hasNext()) {
      Value next = iter.next();
      if (!next.equals(current)) return next;
      if (iter.hasNext()) return iter.next();
    }
    return null;
  }

  /**
   * Starts walking all the sorted sets, till one of them reaches the end point
   * cPointer points to a set member
   * variable score is incremented each time a positive match happens, based on the total score the
   * item pointed to by cPointer is added to one of the entries in returnSet
   */
  public SortedSet<Result> intersect() {
    if (m_returnSet != null) {
      return m_returnSet;
    }
    setup();
    if (m_incSize == 0) {
      return m_returnSet;
    }
    try {
      lock();
      int matches = 0; // score for the cPointer matches
      int setIndex = 0;
      double totalWeight = 1.0;
      SortedSet<Value> currentSet = m_includes.get(setIndex++); // current set where cPointer is fetched
      Value cPointer = currentSet.first(); // cPointer will point to an item in the set under consideration
      int itemVisitCount = 1, itemLookupCount = 0; // Performace indicator for how many items were visited
      int loopcount=0;
      while (cPointer != null) {
        loopcount++;
        currentSet = currentSet.tailSet(cPointer);
        m_includes.set((setIndex-1)%m_incSize,currentSet);
        totalWeight *= m_includesWeight.get((setIndex-1)%m_incSize).getWeight(cPointer);
        // Inner loops runs as many times as the size of the sets
        for (int i = 0; i < m_incSize - 1; i++) {
          itemLookupCount++;
          setIndex = (setIndex + i) % m_incSize;
          currentSet = m_includes.get(setIndex).tailSet(cPointer);
          m_includes.set(setIndex,currentSet);
          if (currentSet.isEmpty() || !currentSet.first().equals(cPointer)) {
            break;
          }
          IWeight w = m_includesWeight.get(i);
          matches += w.match(cPointer);
          totalWeight *= w.getWeight(cPointer);
        }
        for (int i = 0; i < m_filterSize; i++) {
          if (!m_filters.get(i).accept(cPointer)) break; // @todo fail on first filter may not be the best move
          IWeight w = m_filtersWeight.get(i);
          matches += w.match(cPointer);
          totalWeight *= w.getWeight(cPointer);
        }
        for (int i = 0; i < m_excSize; i++) {
          itemLookupCount++;
          if (m_excludes.get(i).contains(cPointer)) break; // @todo fail on first exclude may not be the best move
          IWeight w = m_excludesWeight.get(i);
          matches += w.match(cPointer);
          totalWeight *= w.getWeight(cPointer);
        }
        if (addResult(matches, cPointer, totalWeight)) break;
        matches = 0; totalWeight = 1.0;
        cPointer = nextElement(currentSet, cPointer);
        itemVisitCount++;
        setIndex++;
      }
      //System.out.print("visit: "+itemVisitCount + " lookup: "+itemLookupCount+" loopcount: "+loopcount);
    } finally {
      unlock();
    }
    buildReturnSet();
    return m_returnSet;
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
   */
  private void buildReturnSet() {
    if (m_returnSet != null) return;
    if (m_returnList.size() > 0) {
      ArrayList<Result> list = m_returnList.get(0);
      for (int i = 1;list.size() < m_maxSetSize && i < m_returnList.size(); i++) {
        list.addAll(m_returnList.get(i));
      }
      m_returnSet = new SortedArraySet<Result>(list);
    } else {
      m_returnSet = new SortedArraySet<Result>();
    }
    m_returnList.clear();
  }

  protected ArrayList<SortedSet<Value>> getIncludes() {
    return m_includes;
  }
}