package com.tumri.joz.index;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class RWLockedTreeMap<Key, Value> extends TreeMap<Key, Value> implements RWLocked {
  private ReadWriteLock m_rwlock = new ReentrantReadWriteLock();

  public RWLockedTreeMap() {
    super();
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
