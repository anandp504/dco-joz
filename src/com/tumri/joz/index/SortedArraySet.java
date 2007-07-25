package com.tumri.joz.index;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 *
 * Implements SortedSet interface based on ArrayList implementation.
 * Provides fast implementations O(1) of first(),last(), size(), isEmpty(), iterator()
 * Implementation of Add,remove, and Constructor with collections are not efficient
 * Following are the algorithmic characteristics: of ArraySet vs RB TreeSet
 * size(): O(1) / O(N)
 * isEmpty(): O(1) / O(N)
 * first(): O(1) / O(log n)
 * last(): O(1) / O (log n)
 * contains(V): O(log n) / O (log n)
 * add(V): O(n/2) / O (log n)
 * remove(V): O(n/2) / O(log n)
 * SortedArraySet(Collection<V> c): O(n + n log n) / n log n
 * subset(V1,V2): 2 O(log n) / 2 O(log n)
 * tailset(V): O(log n) / O(log n)
 * headset(V): O(log n) / O (log n)
 * Iterator.hasNext(): O(1) / O(log n)
 * Iterator.next(): O(1) / O(log n)
 * Some of the optional APIs are not supported
 */
public class SortedArraySet<V> implements SortedSet<V> {
  ArrayList<V> m_list;
  private Comparator<? super V> m_comparator = null;

  public SortedArraySet() {
    m_list = new ArrayList<V>();
  }

  public SortedArraySet(Comparator<? super V> aComparator) {
    m_list = new ArrayList<V>();
    m_comparator = aComparator;
  }

  /**
   * The list aList is adopted, caller should ensure no duplicate items
   * @param aList
   */
  public SortedArraySet(ArrayList<V> aList) {
    m_list = aList;
    sort();
  }

  /**
   * The list aList is adopted, caller should ensure no duplicates
   * @param aList
   * @param presorted
   */
  public SortedArraySet(ArrayList<V> aList, boolean presorted) {
    m_list = aList;
    if (!presorted) sort();
  }

  /**
   * Caller should ensure no duplicates
   * @param aList
   */
  public SortedArraySet(Collection<V> aList) {
    m_list = new ArrayList<V>();
    m_list.addAll(aList);
    sort();
  }

  public Comparator<? super V> comparator() {
    return m_comparator;
  }

  public SortedSet<V> subSet(V aV, V aV1) {
    int pos = search(aV);
    if (pos < 0) pos = (-1 - pos);
    int pos1 = search(aV);
    if (pos1 < 0) pos1 = (-1 - pos1);
    return new SortedArraySubset<V>(pos,pos1);
  }

  public SortedSet<V> headSet(V aV) {
    int pos = search(aV);
    if (pos < 0) pos = (-1 - pos);
    return new SortedArraySubset<V>(0,pos);
  }

  public SortedSet<V> tailSet(V aV) {
    int pos = search(aV);
    if (pos < 0) pos = (-1 - pos);
    return new SortedArraySubset<V>(pos,end());
  }

  public V first() {
    return m_list.get(start());
  }

  public V last() {
    return m_list.get(end());
  }

  public int size() {
    return (end() - start());
  }

  public boolean isEmpty() {
    return (start() == end());
  }

  public boolean contains(Object o) {
    return (search((V)o) >= 0);
  }

  public Iterator<V> iterator() {
    return new SortedArraySetIterator();
  }

  public Object[] toArray() {
    return m_list.toArray();
  }

  public <T> T[] toArray(T[] aTs) {
    return m_list.toArray(aTs);
  }

  public boolean add(V aV) {
    int insertionPoint = search(aV);
    if (insertionPoint < 0) {
      m_list.add(-1-insertionPoint,aV);
      return true;
    }
    return false;
  }

  public boolean remove(Object o) {
    V item = (V)o;
    int insertionPoint = search(item);
    if (insertionPoint >= 0) {
      int i = insertionPoint + 1;
      for (; i < m_list.size(); i++) {
        V lV = m_list.get(i);
        if (compare(item, lV) != 0)
          break;
      }
      for (int j = i-1; j >= insertionPoint; j--) {
        m_list.remove(j); // can be optimized to avoid too many remove calls
      }
      return true;
    }
    return false;
  }

  public boolean containsAll(Collection<?> aObjects) {
    Iterator iter = aObjects.iterator();
    while (iter.hasNext()) {
      Object o = iter.next();
      if (!contains(o)) return false;
    }
    return true;
  }

  public boolean addAll(Collection<? extends V> aVs) {
    ArrayList<V> list = new ArrayList<V>();
    Iterator<? extends V> iter = aVs.iterator();
    while (iter.hasNext()) {
      V lV = iter.next();
      if (!contains(lV)) list.add(lV);
    }
    if (list.size() > 0) {
      m_list.addAll(list);
      sort();
      return true;
    }
    return false;
  }

  public boolean retainAll(Collection<?> aObjects) {
    Iterator iter = aObjects.iterator();
    Set<V> list = new TreeSet<V>();
    while (iter.hasNext()) {
      Object o = iter.next();
      if(contains(o)) list.add((V)o);
    }
    if (size() != list.size()) {
      m_list.clear();
      m_list.addAll(list);
      sort();
      return true;
    }
    return false;
  }

  public boolean removeAll(Collection<?> aObjects) {
    throw new UnsupportedOperationException();
  }

  public void clear() {
    m_list.clear();
  }

  private void sort() {
    if (m_comparator == null)
      Collections.sort((ArrayList<Comparable>)m_list);
    else
      Collections.sort(m_list,m_comparator);
  }

  private int compare(V aV, V aV1) {
    return (m_comparator == null ? ((Comparable<V>) aV).compareTo(aV1) :
        m_comparator.compare(aV, aV1));
  }

  private int search(V v) {
    if (m_comparator == null)
      return Collections.binarySearch((ArrayList<Comparable<? super Comparable>>)m_list,(Comparable)v);
    else
      return Collections.binarySearch(m_list,v,m_comparator);
  }

  protected int start() {
    return 0;
  }
  protected int end() {
    return m_list.size();
  }

  class SortedArraySetIterator implements Iterator<V> {
    int current;

    public SortedArraySetIterator() {
      current = start();
    }

    public boolean hasNext() {
      return current < end();
    }

    public V next() {
      V ret = m_list.get(current++);
      while(hasNext() && compare(m_list.get(current),ret) == 0) {
        current++; // avoid returning same element twice, set semantics
      }
      return ret;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  class SortedArraySubset<V> extends SortedArraySet<V> {
    private int m_start;
    private int m_end;

    public SortedArraySubset(int aStart, int aEnd) {
      m_start = aStart;
      m_end = aEnd;
    }
    protected int start() {
      return m_start;
    }
    protected int end() {
      return m_end;
    }
  }

  public static void main(String argv[]) {
    ArrayList<Integer> set1 = new ArrayList<Integer>();
    set1.add(9);
    set1.add(1);
    set1.add(24);
    set1.add(1);
    set1.add(5);
    set1.add(5);
    set1.add(9);
    set1.add(9);
    set1.add(1);
    set1.add(23);
    set1.add(24);
    set1.add(24);
    SortedArraySet<Integer> set2 = new SortedArraySet<Integer>();
    set2.add(24);
    set2.add(24);
    set2.add(1);
    set2.add(1);
    set2.add(23);
    set2.add(23);
    set2.add(4);
    set2.add(4);
    set2.add(8);
    set2.add(8);
    {
      SortedArraySet<Integer> set = new SortedArraySet<Integer>(set1);
      Iterator<Integer> iter = set.iterator();
      while (iter.hasNext()) {
        Integer lInteger = iter.next();
        System.out.println(lInteger);
      }
    }
    {
      Iterator<Integer> iter = set2.iterator();
      while (iter.hasNext()) {
        Integer lInteger = iter.next();
        System.out.println(lInteger);
      }
    }
    {
      set2.addAll(set1);
      Iterator<Integer> iter = set2.iterator();
      while (iter.hasNext()) {
        Integer lInteger = iter.next();
        System.out.println(lInteger);
      }
    }
  }

}