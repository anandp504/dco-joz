package com.tumri.joz.index;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * Date: Jul 31, 2007
 * Time: 10:11:42 AM
 * Adds ReadWrite Locking to the SortedArraySet class
 */
public class RWLockedSortedArraySet<V> extends SortedArraySet<V> implements RWLockedSortedSet<V> {
  private ReadWriteLock m_rwlock = new ReentrantReadWriteLock();

  public RWLockedSortedArraySet() {
    super();
  }

  public RWLockedSortedArraySet(Comparator<? super V> aComparator) {
    super(aComparator);
  }

  public RWLockedSortedArraySet(ArrayList<V> aList) {
    super(aList);
  }

  public RWLockedSortedArraySet(ArrayList<V> aList, boolean presorted) {
    super(aList, presorted);
  }

  public RWLockedSortedArraySet(Collection<V> aList) {
    super(aList);
  }

  public void readerLock() {
    try {
      m_rwlock.readLock().lock();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  public void readerUnlock() {
    try {
      m_rwlock.readLock().unlock();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  public void writerLock() {
    try {
      m_rwlock.writeLock().lock();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  public void writerUnlock() {
    try {
      m_rwlock.writeLock().unlock();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
