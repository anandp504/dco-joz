package com.tumri.joz.products;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public interface Handle<V> extends Comparable<V> {
  /**
   * Returns the underlying object id to this handle
   * @return
   */
  public int getOid();
}
