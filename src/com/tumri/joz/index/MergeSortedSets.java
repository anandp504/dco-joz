package com.tumri.joz.index;

import org.junit.Test;
import org.junit.Assert;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * This class implements an algorithm to merge sorted sets
 * The merge completes is O(n log k) steps. Where k is number of sets to merge and
 * n is total number of elemets in the merged set
 * When for large values of n, k is still small n : k (1000 : 1), this algorighm will do better
 * set.addAll() will provide n log n behavior
 */
public class MergeSortedSets<V> {
  SortedSet<SetIterator> m_set;
  private Comparator<? super V> m_comparator = null;


  /**
   * This constructor exists for the purpose of JUnit. Call the static method on the class.
   */
  public MergeSortedSets() {
  }

  private MergeSortedSets(ArrayList<SortedSet<V>> aList) {
    m_set = (aList.size() < 8 ? new SortedArraySet<SetIterator>() : new TreeSet<SetIterator>());
    for (int i = 0; i < aList.size(); i++) {
      m_comparator = aList.get(0).comparator();
      Iterator<V> lVs = aList.get(i).iterator();
      if (lVs.hasNext()) {
        add(new SetIterator(lVs.next(),lVs));
      }
    }
  }

  /**
   * A list of SortedSet is merged to create a single sorted set. The individual SortedSet objects may have
   * a non null intersection with each other. The algorithm completes in O(n log k) steps where
   * n is the size of the returned set, and k is size of aList
   * @param aList list of SortedSet objects, individual SortedSets may have non null intersection with each other
   * @return a SortedSet formed by merging all the sets
   */
  public static <V> SortedSet<V> merge(ArrayList<SortedSet<V>> aList) {
    MergeSortedSets<V> merger = new MergeSortedSets<V>(aList);
    return merger.merge();
  }

  private void add(SetIterator si) {
    while(!m_set.add(si) && si.advance() != null); // Empty body
  }

  /**
   * Algorithm works by using the first two elements of the m_set {first , second)
   * Since m_set is sorted using SetIterator.m_next,
   *   all elements of where first.m_next < second.m_next are added
   *   this advances first till first.m_next > second.m_next
   *   at this time first is added back to m_set to resort and algorithm repeats
   * @return SortedSet<V>
   */
  private SortedSet<V> merge() {
    SortedArraySet<V> ret = new SortedArraySet<V>();
    while(true) {
      Iterator<SetIterator> siter = m_set.iterator();
      if (!siter.hasNext())
        break;
      SetIterator first = siter.next();
      ret.add(first.m_next); // add the first element
      if (siter.hasNext()) {
        SetIterator second = siter.next();
        V boundry = second.m_next;
        m_set.remove(first);
        while (first.advance() != null) {
          V lV = first.m_next;
          int cmp = compare(lV, boundry);
          if (cmp < 0) {
            ret.add(lV);
          } else if (cmp == 0) {
            ret.add(lV);
            m_set.remove(second);
            if (first.advance() != null) add(first);
            if (second.advance() != null) add(second);
            break;
          } else if (cmp > 0) {
            add(first);
            break;
          }
        }
      } else {
        m_set.remove(first);
        while (first.advance() != null) {
          ret.add(first.m_next);
        }
        break;
      }
    }
    return ret;
  }

  class SetIterator implements Comparable<SetIterator> {
    private V m_next;
    private Iterator<V> m_iter;

    public SetIterator(V aNext, Iterator<V> aIter) {
      m_next = aNext;
      m_iter = aIter;
    }
    public int compareTo(SetIterator v) {
      if (m_next == null) return -1;
      return compare(m_next,v.m_next);
    }

    private V advance() {
      m_next = (m_iter.hasNext() ? m_iter.next() : null);
      return m_next;
    }
  }
  @SuppressWarnings("unchecked")
  private int compare(V aV, V aV1) {
    return (m_comparator == null ? ((Comparable<V>) aV).compareTo(aV1) :
        m_comparator.compare(aV, aV1));
  }

  @Test public void test() {
    Integer res0[] = new Integer[] {1, 2, 3};
    Integer res1[] = new Integer[] {2, 3, 4};
    Integer res2[] = new Integer[] {3, 4, 5};
    Integer res3[] = new Integer[] {4, 5, 6};
    Integer res4[] = new Integer[] {5, 6, 7};
    Integer res5[] = new Integer[] {6, 7, 8};
    Integer res6[] = new Integer[] {7, 8, 9};
    Integer res7[] = new Integer[] {8, 9, 10};
    Integer res8[] = new Integer[] {9, 10, 11};
    Integer res[][] = new Integer[][] {res0,res1,res2,res3,res4,res5,res6,res7,res8};
    ArrayList<SortedSet<Integer>> set = new ArrayList<SortedSet<Integer>>();
    for (int i = 0; i < 9; i++) {
      set.add(new SortedArraySet<Integer>());
      SortedSet<Integer> s = set.get(i);
      Integer r[] = res[i];
      for (int j = 0; j < r.length; j++) {
        s.add(r[j]);
      }
    }
    ArrayList<SortedSet<Integer>> set1 = new ArrayList<SortedSet<Integer>>();
    for (int i = 0; i < 4; i++) {
      set1.add(new SortedArraySet<Integer>());
      SortedSet<Integer> s = set1.get(i);
      Integer r[] = res[i];
      for (int j = 0; j < r.length; j++) {
        s.add(r[j]);
      }
    }
    {
      SortedSet<Integer> sorted = MergeSortedSets.merge(set);
      Assert.assertTrue(sorted.size() == 11);
      Assert.assertTrue(!sorted.isEmpty());
      Iterator<Integer> iter = sorted.iterator();
      int i=1;
      while (iter.hasNext()) {
        Assert.assertEquals(iter.next(),i++);
      }
    }
    {
      SortedSet<Integer> sorted = MergeSortedSets.merge(set1);
      Assert.assertTrue(sorted.size() == 6);
      Assert.assertTrue(!sorted.isEmpty());
      Iterator<Integer> iter = sorted.iterator();
      int i=1;
      while (iter.hasNext()) {
        Assert.assertEquals(iter.next(),i++);
      }
    }
  }
}
