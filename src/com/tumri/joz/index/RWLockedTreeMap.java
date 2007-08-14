package com.tumri.joz.index;

import org.apache.log4j.Logger;

import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class RWLockedTreeMap<Key, Value> extends TreeMap<Key, Value> implements RWLocked {
  private static final long serialVersionUID = 1L;
  static Logger log = Logger.getLogger(RWLockedTreeMap.class);
  private ReadWriteLock m_rwlock = new ReentrantReadWriteLock();

  public RWLockedTreeMap() {
    super();
  }

  public void readerLock() {
    try {
      m_rwlock.readLock().lock();
    } catch (Exception e) {
      log.error("Exception reader locking ",e);
    }
  }
  public void readerUnlock() {
    try {
      m_rwlock.readLock().unlock();
    } catch (Exception e) {
      log.error("Exception reader unlocking ",e);
    }
  }
  public void writerLock() {
    try {
      m_rwlock.writeLock().lock();
    } catch (Exception e) {
      log.error("Exception writer locking ",e);
    }
  }
  public void writerUnlock() {
    try {
      m_rwlock.writeLock().unlock();
    } catch (Exception e) {
      log.error("Exception writer unlocking ",e);
    }
  }
  
}
