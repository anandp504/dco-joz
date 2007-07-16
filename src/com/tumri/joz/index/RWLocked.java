package com.tumri.joz.index;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public interface RWLocked {
  public void readerLock();
  public void readerUnlock();
  public void writerLock();
  public void writerUnlock();
}
