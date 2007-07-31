package com.tumri.joz.index;

import org.junit.Test;
import org.junit.Assert;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 * The class is not itself a True Set
 * The iterator does make sure the Set abstraction behaves correctly
 * All other functions preserve the Set abstraction
 * Size function is an approximation
 */
public class MultiSortedSet<V> implements RWLockedSortedSet<V>  {
  private List<SortedSet<V>> m_list = new ArrayList<SortedSet<V>>();
  private EndItems<V> m_endItems = null;
  private Comparator<? super V> m_comparator = null;

 
  public MultiSortedSet() {
  }

  public MultiSortedSet(Comparator<? super V> aComparator) {
    m_comparator = aComparator;
  }

  public MultiSortedSet(List<SortedSet<V>> aList) {
    m_list = aList;
  }

  public Comparator<? super V> comparator() {
    return m_comparator;
  }

  public void add(SortedSet<V> set) {
    if (set != null && !set.isEmpty())
      m_list.add(set);
  }

  public void add(SortedSet<V> set, boolean force) {
    if (set != null && (force || !set.isEmpty()))
      m_list.add(set);
  }

  public SortedSet<V> subSet(V aV, V aV1) {
    MultiSortedSet<V> set = new MultiSortedSet<V>();
    for (int i = 0; i < m_list.size(); i++) {
      SortedSet<V> lVs = m_list.get(i);
      set.add(lVs.subSet(aV, aV1));
    }
    return set;
  }

  public SortedSet<V> headSet(V aV) {
    MultiSortedSet<V> set = new MultiSortedSet<V>();
    for (int i = 0; i < m_list.size(); i++) {
      SortedSet<V> lVs = m_list.get(i);
      set.add(lVs.headSet(aV));
    }
    return set;
  }

  public SortedSet<V> tailSet(V aV) {
    MultiSortedSet<V> set = new MultiSortedSet<V>();
    EndItems<V> endItems = (m_endItems == null ? new EndItems<V>() : m_endItems);
    EndItems<V> nEndItems = new EndItems<V>();
    for (int i = 0; i < m_list.size(); i++) {
      V lastItem = endItems.get(i,false);
      SortedSet<V> lVs = m_list.get(i);
      if (lastItem == null) {
        SortedSet<V> tailSet = lVs.tailSet(aV);
        if (!tailSet.isEmpty()) {
          lastItem = tailSet.last();
          set.add(tailSet,true);
          nEndItems.add(null,lastItem);
        }
      } else if (compare(aV,lastItem) <= 0) {
        SortedSet<V> tailSet = lVs.tailSet(aV);
        set.add(tailSet, true);
        nEndItems.add(null, lastItem);
      }
    }
    if (nEndItems.size() > 0) {
      set.setLastItems(nEndItems);
    }
    return set;
  }

  public V first() {
    V ret = null;
    for (int i = 0; i < m_list.size(); i++) {
      if (m_endItems == null && m_list.get(i).isEmpty()) continue;
      V lV = m_list.get(i).first();
      if (m_endItems != null) m_endItems.setFirst(i,lV);
      if (lV != null) {
        if (ret == null || compare(ret, lV) > 0) {
          ret = lV;
        }
      }
    }
    return ret;
  }

  public V last() {
    V ret = null;
    for (int i = 0; i < m_list.size(); i++) {
      if (m_list.get(i).isEmpty()) continue;
      V lV = m_list.get(i).last();
      if (lV != null) {
        if (ret == null || compare(ret, lV) < 0) {
          ret = lV;
        }
      }
    }
    return ret;
  }

  /**
   * Expected to return size of the set, NOTE: this doesn't work correctly
   * @return approximate size of the set, actual size may lower than the value returned
   */
  public int size() {
    int count = 0;
    for (int i = 0; i < m_list.size(); i++) {
      count += m_list.get(i).size();
    }
    return count;
  }

  public boolean isEmpty() {
    if (m_endItems != null)
      return false;
    for (int i = 0; i < m_list.size(); i++) {
      if (!m_list.get(i).isEmpty()) return false;
    }
    return true;
  }

  public boolean contains(Object o) {
    for (int i = 0; i < m_list.size(); i++) {
      if (m_list.get(i).contains(o)) return true;
    }
    return false;
  }

  public Iterator<V> iterator() {
    return new MultiSortedSetIterator();
  }

  public Object[] toArray() {
    if (m_list.size() > 1) {
      SortedSet<V> set = new TreeSet<V>();
      for (int i = 0; i < m_list.size(); i++) {
        SortedSet<V> lVs = m_list.get(i);
        Iterator<V> iter = lVs.iterator();
        while (iter.hasNext()) {
          set.add(iter.next());
        }
      }
      return set.toArray();
    } else if (m_list.size() == 1) {
      return m_list.get(0).toArray();
    }
    return new Object[0];
  }

  public <T> T[] toArray(T[] aVs) {
    if (m_list.size() > 1) {
      SortedSet<V> set = new TreeSet<V>();
      for (int i = 0; i < m_list.size(); i++) {
        SortedSet<V> lVs = m_list.get(i);
        Iterator<V> iter = lVs.iterator();
        while (iter.hasNext()) {
          set.add(iter.next());
        }
      }
      return set.toArray(aVs);
    } else if (m_list.size() == 1) {
      return m_list.get(0).toArray(aVs);
    }
    return aVs;
  }

  public boolean add(V aV) {
    if (contains(aV)) return false;
    if (m_list.size() == 0) {
      m_list.add(new TreeSet<V>(m_comparator));
    }
    m_list.get(0).add(aV);
    return true;
  }

  public boolean remove(Object o) {
    boolean ret = false;
    for (int i = 0; i < m_list.size(); i++) {
      SortedSet<V> lVs = m_list.get(i);
      if (lVs.remove(o)) {
        ret = ret || true;
      }
    }
    return ret;
  }

  public boolean containsAll(Collection<?> aObjects) {
    Iterator<?> iter = aObjects.iterator();
    while (iter.hasNext()) {
      Object lV = iter.next();
      if (!contains(lV)) return false;
    }
    return true;
  }

  public boolean addAll(Collection<? extends V> aVs) {
    Iterator<? extends V> iter = aVs.iterator();
    boolean ret = false;
    while (iter.hasNext()) {
      V lV = iter.next();
      ret = ret || add(lV);
    }
    return ret;
  }

  public boolean retainAll(Collection<?> aObjects) {
    TreeSet<V> set = new TreeSet<V>();
    Iterator<?> iter = aObjects.iterator();
    while (iter.hasNext()) {
      V v = (V)iter.next();
      if (contains(v)) set.add(v);
    }
    clear();
    add(set);
    return true;
  }

  public boolean removeAll(Collection<?> aObjects) {
    Iterator<?> iter = aObjects.iterator();
    boolean ret = false;
    while (iter.hasNext()) {
      Object lV = iter.next();
      ret = ret || remove(lV);
    }
    return ret;
  }

  public void clear() {
    for (int i = 0; i < m_list.size(); i++) {
      SortedSet<V> lVs = m_list.get(i);
      lVs.clear();
    }
  }

  private int compare(V aV, V aV1) {
    return (m_comparator == null ? ((Comparable<V>) aV).compareTo(aV1) :
        m_comparator.compare(aV, aV1));
  }

  public void readerLock() {
    for (int i = 0; i < m_list.size(); i++) {
      SortedSet<V> lVs = m_list.get(i);
      if (lVs instanceof RWLocked) {
        ((RWLocked)lVs).readerLock();
      }
    }
  }

  public void readerUnlock() {
    for (int i = 0; i < m_list.size(); i++) {
      SortedSet<V> lVs = m_list.get(i);
      if (lVs instanceof RWLocked) {
        ((RWLocked)lVs).readerUnlock();
      }
    }
  }

  public void writerLock() {
    for (int i = 0; i < m_list.size(); i++) {
      SortedSet<V> lVs = m_list.get(i);
      if (lVs instanceof RWLocked) {
        ((RWLocked)lVs).writerLock();
      }
    }
  }

  public void writerUnlock() {
    for (int i = 0; i < m_list.size(); i++) {
      SortedSet<V> lVs = m_list.get(i);
      if (lVs instanceof RWLocked) {
        ((RWLocked)lVs).writerUnlock();
      }
    }
  }

  class MultiSortedSetIterator implements Iterator<V> {
    private ArrayList<Iterator<V>> m_iters = new ArrayList<Iterator<V>>();
    private ArrayList<V> m_nexts = new ArrayList<V>();
    private V m_prev = null; // value returned by the immediately previous next() method call

    public MultiSortedSetIterator() {
      for (int i = 0; i < m_list.size(); i++) {
        m_iters.add(m_list.get(i).iterator());
        m_nexts.add(null);
        setNext(i);
      }
    }

    private void setNext(int index) {
      Iterator<V> iter = m_iters.get(index);
      m_nexts.set(index, (iter.hasNext() ? iter.next() : null));
    }

    public boolean hasNext() {
      for (int i = 0; i < m_nexts.size(); i++) {
        if (m_nexts.get(i) != null) return true;
      }
      return false;
    }

    public V next() {
      V ret = null;
      int index = -1;
      for (int i = 0; i < m_nexts.size(); i++) {
        V tmp = m_nexts.get(i);
        if (tmp != null) {
          int cmp = -1;
          if (ret == null || (cmp = compare(ret, tmp)) > 0) {
            ret = tmp;
            index = i;
          } else if (cmp == 0) {
            setNext(i); // Same element can not be returned twice
          }
        }
      }
      if (index >= 0) {
        setNext(index);
      }
      m_prev = ret;
      return ret;
    }

    public void remove() {
      // Not implemented
      Exception e = new Exception("Not implemented");
      e.printStackTrace();
      // throw e;
    }
  }

  @Test public void test() {
    TreeSet<Integer> set1 = new TreeSet<Integer>();
    set1.add(1);
    set1.add(5);
    set1.add(9);
    set1.add(23);
    set1.add(24);
    TreeSet<Integer> set2 = new TreeSet<Integer>();
    set2.add(1);
    set2.add(4);
    set2.add(8);
    set2.add(23);
    set2.add(24);
    {
      MultiSortedSet<Integer> set = new MultiSortedSet<Integer>();
      set.add(set1);
      Iterator<Integer> iter = set.iterator();
      Iterator<Integer> iter1 = set1.iterator();
      while (iter.hasNext()) {
        Integer lInteger = iter.next();
        Assert.assertEquals(iter1.next(),lInteger);
      }
    }
    {
      MultiSortedSet<Integer> set = new MultiSortedSet<Integer>();
      set.add(set1);
      set.add(set2);
      Iterator<Integer> iter = set.iterator();
      int i=0;
      Integer set3[] = new Integer[] {1, 4 , 5, 8, 9, 23, 24};
      while (iter.hasNext()) {
        Integer lInteger = iter.next();
        Assert.assertEquals(lInteger.intValue(),set3[i++]);
      }
    }
  }

  /**
   * This should be treated like an SPI
   * @return list of SortedSets contained within the set
   */
  public final List<SortedSet<V>> getList() {
    return m_list;
  }

  /**
   * Given a vPointer find immediate next Element after it.
   * @param vPointer
   * @return
   */
  public V nextElement(V vPointer) {
    if (m_endItems != null) {
      List<V> list = new ArrayList<V>();
      list.addAll(m_endItems.get(true));
      List<Integer> indices = endItemIndex(vPointer,true);
      for (int i = 0; i < indices.size(); i++) {
        Integer index = indices.get(i);
        Iterator<V> iter = m_list.get(index).iterator();
        if (iter.hasNext() && compare(iter.next(), vPointer) == 0 && iter.hasNext()) {
          list.set(index, iter.next());
        } else {
          list.set(index,null);
        }
      }
      V ret = null;
      for (int i = 0; i < list.size(); i++) {
        V lV = list.get(i);
        if (ret == null || (lV != null && compare(ret, lV) > 0)) {
          ret = lV;
        }
      }
      return ret;
    }
    return null;
  }

  private List<Integer> endItemIndex(V lV, boolean first) {
    List<Integer> ret = new ArrayList<Integer>();
    if (m_endItems != null) {
      List<V> list = m_endItems.get(first);
      for (int i = 0; i < list.size(); i++) {
        V lV1 = list.get(i);
        if (lV1 != null &&  compare(lV,lV1) == 0) {
          ret.add(i);
        }
      }
    }
    return ret;
  }


  private void setLastItems(EndItems<V> aEndItems) {
    m_endItems = aEndItems;
  }

  @Test public void test1() {
    Integer x[] = new Integer[] {1, 2, 4, 5, 6, 7, 8, 9, 12 };
    Integer x1[] = new Integer[] {1, 2, 5, 6, 8, 9};
    Integer x2[] = new Integer[] {1, 4, 5, 7, 8, 12 };
    Integer x3[] = new Integer[] {2, 4, 6, 7, 9, 12 };

    ArrayList<Integer> y1 = new ArrayList<Integer>();
    for (int i = 0; i < x1.length; i++) { y1.add(x1[i]); }

    ArrayList<Integer> y2 = new ArrayList<Integer>();
    for (int i = 0; i < x2.length; i++) { y2.add(x2[i]); }

    ArrayList<Integer> y3 = new ArrayList<Integer>();
    for (int i = 0; i < x3.length; i++) { y3.add(x3[i]); }

    MultiSortedSet<Integer> set = new MultiSortedSet<Integer>();
    set.add(new SortedArraySet<Integer>(y1));
    set.add(new SortedArraySet<Integer>(y2));
    set.add(new SortedArraySet<Integer>(y3));
    Iterator<Integer> iter = set.iterator();
    for (int i = 0; i < x.length; i++) {
      Integer cur = x[i];
      Assert.assertTrue(iter.hasNext());
      Assert.assertEquals(iter.next(),cur);

      SortedSet<Integer> headset = set.headSet(cur);
      SortedSet<Integer> tailset = set.tailSet(cur);
      SortedSet<Integer> subset  = set.subSet(x[0],cur);

      Assert.assertTrue(headset.size() >= i);
      if (!headset.isEmpty()) {
        Assert.assertEquals(headset.first(),x[0]);
        Assert.assertEquals(headset.last(),x[(i+x.length-1)%x.length]);
      }

      Assert.assertTrue(tailset.size() >= (x.length-i));
      if (!tailset.isEmpty()) {
        Assert.assertEquals(tailset.first(),x[i]);
        Assert.assertEquals(tailset.last(),x[x.length-1]);
      }

      Assert.assertTrue(subset.size() >= i);
      if (!subset.isEmpty()) {
        Assert.assertEquals(subset.first(),x[0]);
        Assert.assertEquals(subset.last(),x[(i+x.length-1)%x.length]);
      }
    }
  }
}
class EndItems<V> {
  private List<V> m_last = new ArrayList<V>();
  private List<V> m_first = new ArrayList<V>();

  EndItems() {
  }

  void add(V first, V last) {
      m_first.add(first);
      m_last.add(last);
  }

  V get(int index,boolean first) {
    if (first) {
      return (index < m_first.size() ? m_first.get(index) : null);
    } else {
      return (index < m_last.size() ? m_last.get(index) : null);
    }
  }

  int size() {
    return (m_first.size());
  }

  List<V> get(boolean first) {
    return (first ? m_first : m_last);
  }

  void setFirst(int index, V value) {
    if (index < m_first.size()) m_first.set(index,value);
  }

}