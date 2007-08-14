package com.tumri.joz.index;

import org.junit.Test;
import org.junit.Assert;

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

  protected SortedArraySet(SortedArraySet<V> set) {
    m_list = set.m_list;
    m_comparator = set.m_comparator;
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
    int pos1 = search(aV1);
    if (pos1 < 0) pos1 = (-1 - pos1);
    return new SortedArraySubset<V>(this,pos,pos1);
  }

  public SortedSet<V> headSet(V aV) {
    int pos = search(aV);
    if (pos < 0) pos = (-1 - pos);
    return new SortedArraySubset<V>(this,start(),pos);
  }

  public SortedSet<V> tailSet(V aV) {
    int pos = search(aV);
    if (pos < 0) pos = (-1 - pos);
    return new SortedArraySubset<V>(this,pos,end());
  }

  public V first() {
    return m_list.get(start());
  }

  public V last() {
    return m_list.get(end()-1);
  }

  public int size() {
    return (end() - start());
  }

  public boolean isEmpty() {
    return (start() == end());
  }

  public boolean contains(Object o) {
    @SuppressWarnings("unchecked")
    int s = search((V)o);
    return (s >= start() && s < end());
  }

  public Iterator<V> iterator() {
    return new SortedArraySetIterator();
  }

  public Object[] toArray() {
    Object ar[] = new Object[end()-start()];
    int j=0;
    for(int i = start(); i < end(); i++) {
      ar[j++] = m_list.get(i);
    }
    return ar;
  }

  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] aTs) {
    int j=0;
    for (int i = start(); i < end(); i++) {
      // ??? This gets an "unchecked cast" exception.
      aTs[j++] = (T)m_list.get(i);
    }
    return aTs;
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
    @SuppressWarnings("unchecked")
    V item = (V)o;
    int insertionPoint = search(item);
    if (insertionPoint >= 0) {
      m_list.remove(insertionPoint);
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

  @SuppressWarnings("unchecked")
  public boolean retainAll(Collection<?> aObjects) {
    Iterator iter = aObjects.iterator();
    Set<V> list = new TreeSet<V>();
    while (iter.hasNext()) {
      Object o = iter.next();
      // ??? This gets an "unchecked cast" warning.
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
    Iterator<?> iter = aObjects.iterator();
    ArrayList<Integer> list = new ArrayList<Integer>();
    while (iter.hasNext()) {
      Object o = iter.next();
      @SuppressWarnings("unchecked")
      int index = search((V)o);
      if (index >= 0)
        list.add(index);
    }
    if (list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        m_list.set(list.get(i),null);
      }
      ArrayList<V> nlist = new ArrayList<V>();
      for (int i = 0; i < m_list.size(); i++) {
        V lV = m_list.get(i);
        if (lV != null) {
          nlist.add(lV);
        }
      }
      m_list = nlist;
      return true;
    }
    return false;
  }

  public void clear() {
    m_list.clear();
  }

  @SuppressWarnings("unchecked")
  private void sort() {
    if (m_comparator == null)
      // ??? This gets an "unchecked cast" warning.
      Collections.sort((ArrayList<Comparable>)m_list);
    else
      Collections.sort(m_list,m_comparator);

    // Remove duplicates
    ArrayList<V> list = new ArrayList<V>();
    V prev = null;
    for (int i = 0; i < m_list.size(); i++) {
      V lV = m_list.get(i);
      if (prev == null || compare(prev,lV) != 0)
        list.add(lV);
      prev = lV;
    }
    m_list = list;
  }

  @SuppressWarnings("unchecked")
  private int compare(V aV, V aV1) {
    // ??? This gets an "unchecked cast" warning.
    return (m_comparator == null ? ((Comparable<V>) aV).compareTo(aV1) :
        m_comparator.compare(aV, aV1));
  }

  @SuppressWarnings("unchecked")
  private int search(V v) {
    if (m_comparator == null)
      // ??? This gets an "unchecked cast" warning.
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
      return m_list.get(current++);
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }


  @Test public void test() {
    Integer res1[] = new Integer[] {1, 5, 9, 23, 24};
    Integer res2[] = new Integer[] {1, 4, 8, 23, 24};
    Integer res3[] = new Integer[] {1, 4, 5, 8, 9, 23, 24};
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
      int i=0;
      SortedArraySet<Integer> set = new SortedArraySet<Integer>(set1);
      Iterator<Integer> iter = set.iterator();
      while (iter.hasNext()) {
        Integer lInteger = iter.next();
        Assert.assertEquals(lInteger,res1[i++]);
      }
    }
    {
      int i=0;
      Iterator<Integer> iter = set2.iterator();
      while (iter.hasNext()) {
        Integer lInteger = iter.next();
        Assert.assertEquals(lInteger,res2[i++]);
      }
    }
    {
      int i=0;
      set2.addAll(set1);
      Iterator<Integer> iter = set2.iterator();
      while (iter.hasNext()) {
        Integer lInteger = iter.next();
        Assert.assertEquals(lInteger,res3[i++]);
      }
    }
    {
      int offset = 4;
      for (int i = 0; i < res3.length; i++) {
        Assert.assertTrue(set2.remove(res3[(i+offset)%res3.length]));
      }
      Assert.assertTrue(set2.size() == 0);
    }
    {
      SortedArraySet<Integer> set = new SortedArraySet<Integer>(set1);
      int size = set.size();
      ArrayList<Integer> list = new ArrayList<Integer>();
      list.add(100); list.add(5); list.add(9);
      set.removeAll(list);
      Assert.assertFalse(set.isEmpty());
      Assert.assertTrue(set.size() == (size-2));
      int last = -1;
      Iterator<Integer> iter = set.iterator();
      while (iter.hasNext()) {
        int next = iter.next();
        Assert.assertTrue(last < next);
        last = next;
      }
      set.removeAll(set1);
      Assert.assertTrue(set.isEmpty());
      Assert.assertTrue(set.size() == 0);
    }
  }
}
class SortedArraySubset<V> extends SortedArraySet<V> {
  private int m_start;
  private int m_end;

  public SortedArraySubset(SortedArraySet<V> set,int aStart, int aEnd) {
    super(set);
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
