package com.tumri.joz.products;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public interface Handle extends Comparable, Comparator {
  /**
   * Returns the underlying object id to this handle
   * @return
   */
  public int getOid();
  public double getScore();
  public Handle createHandle(double score);
}
