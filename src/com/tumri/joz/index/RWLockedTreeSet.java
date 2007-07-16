package com.tumri.joz.index;

import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class RWLockedTreeSet<Type> extends TreeSet<Type> implements RWLockedSortedSet<Type> {
  private ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
  private AtomicInteger m_size = new AtomicInteger(0);
  private AtomicBoolean m_modified = new AtomicBoolean(false);
  
  public RWLockedTreeSet() {
    super();
  }

  /**
   * The method has been overridden to provide an optimized implementation, the count may be wrong
   * in some cases due to synchronization window problem.
   * @return size of the set
   */
  public int size() {
    if (m_modified.get()) {
      m_modified.set(false); // first reset to false, to avoid multiple threads computing same thing
      m_size.set(super.size()); // now recompute the size, Note: some threads may see incorrect value
    }
    return m_size.get();
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
      m_modified.set(true);
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
