package com.tumri.joz.index;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * Date: Jul 29, 2007
 * Time: 8:48:38 PM
 * The Set is essentially a SortedSet, with a following twist
 * Consider an ordered set of values { V0, V1, V2,... Vs, Vs+1, Vs+2, Vs+3,... Vs+n }
 * where each consecutive element is greater than the previous under some comparison operation C(Vi,Vj)
 * Now consider the following derived comparison operator
 * Cs(Vi,Vj) : where s is a subscript of some arbitray element of the set
 *    => C(Vi,Vj) : i < s && j < s
 *    => C(Vi,Vj) : i >= s && j >= s
 *    => -1 : i >= s && j < s
 *    => 1 : i < s && j >= s
 * It follows that:
 * SortedSplitSet.first() -> Vs
 * SortedSplitSet.last() -> Vs-1
 * The Resulting order is as follows:
 * S = { Vs, Vs+1, Vs+2, ... Vs+n, V1, V2, V3, V4, ... Vs-1 }
 */
public class SortedSplitSet<V> implements SortedSet<V> {
  private SortedSet<V> m_head;
  private SortedSet<V> m_tail;
  private V m_splitter;
  private Comparator<? super V> m_comparator = null;

  public SortedSplitSet(SortedSet<V> set, V s) {
    m_splitter = s;
    m_head = set.tailSet(m_splitter);
    m_tail = set.headSet(m_splitter);
    m_comparator = set.comparator();
  }


  private SortedSplitSet(SortedSet<V> aHead, SortedSet<V> aTail, V aSplitter, Comparator<? super V> aComparator) {
    m_head = aHead;
    m_tail = aTail;
    m_splitter = aSplitter;
    m_comparator = aComparator;
  }

  public Comparator<? super V> comparator() {
    return m_comparator;
  }

  public SortedSet<V> subSet(V aV, V aV1) {
    int cmp = compare(aV,m_splitter);
    int cmp1 = compare(aV1,m_splitter);
    if (cmp < 0 && cmp1 < 0) {
      return m_tail.subSet(aV,aV1);
    } else if (cmp >= 0 && cmp1 >= 0) {
      return m_head.subSet(aV,aV1);
    } else if (cmp < 0 && cmp1 >= 0) {
      return new SortedSplitSet<V>(m_head.tailSet(aV1),m_tail.headSet(aV),m_splitter,m_comparator);
    } else {
      return new SortedSplitSet<V>(m_head.tailSet(aV),m_tail.headSet(aV1),m_splitter,m_comparator);
    }
  }

  public SortedSet<V> headSet(V aV) {
    int cmp = compare(aV,m_splitter);
    if (cmp < 0) {
      return new SortedSplitSet<V>(m_head,m_tail.headSet(aV),m_splitter,m_comparator);
    } else {
      return m_head.headSet(aV);
    }
  }

  public SortedSet<V> tailSet(V aV) {
    int cmp = compare(aV,m_splitter);
    if (cmp >= 0) {
      return new SortedSplitSet<V>(m_head.tailSet(aV),m_tail,m_splitter,m_comparator);
    } else {
      return m_tail.tailSet(aV);
    }
  }

  public V first() {
    return m_head.first();
  }

  public V last() {
    return m_tail.last();
  }

  public int size() {
    return m_head.size() + m_tail.size();
  }

  public boolean isEmpty() {
    return (m_head.isEmpty() && m_tail.isEmpty());
  }

  public boolean contains(Object o) {
    int cmp = compare((V)o,m_splitter);
    return (cmp >= 0 ? m_head.contains(o) : m_tail.contains(o));
  }

  public Iterator<V> iterator() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public Object[] toArray() {
    ArrayList<V> list = new ArrayList<V>();
    list.addAll(m_head);
    list.addAll(m_tail);
    return list.toArray();
  }

  public <T> T[] toArray(T[] aTs) {
    ArrayList<V> list = new ArrayList<V>();
    list.addAll(m_head);
    list.addAll(m_tail);
    return list.toArray(aTs);
  }

  public boolean add(V aV) {
    throw new UnsupportedOperationException();
  }

  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  public boolean containsAll(Collection<?> aObjects) {
    throw new UnsupportedOperationException();
  }

  public boolean addAll(Collection<? extends V> aVs) {
    throw new UnsupportedOperationException();
  }

  public boolean retainAll(Collection<?> aObjects) {
    throw new UnsupportedOperationException();
  }

  public boolean removeAll(Collection<?> aObjects) {
    throw new UnsupportedOperationException();
  }

  public void clear() {
    m_head = null;
    m_tail = null;
    m_splitter = null;
    m_comparator = null;
  }

  private int compare(V aV, V aV1) {
    return (m_comparator == null ? ((Comparable<V>) aV).compareTo(aV1) :
        m_comparator.compare(aV, aV1));
  }

  class SortedSplitSetIterator implements Iterator<V> {
    Iterator<V> m_current;
    boolean m_flip = true;

    public SortedSplitSetIterator() {
      m_current = m_head.iterator();
    }

    public boolean hasNext() {
      return (m_current.hasNext() || (flip() && m_current.hasNext()));
    }

    public V next() {
      return (hasNext() ? m_current.next(): null);
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    private boolean flip() {
      boolean ret= m_flip;
      if (m_flip) {
        m_current = m_tail.iterator();
        m_flip = false;
      }
      return ret;
    }
  }
}