package com.tumri.joz.filter;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public interface IFilter<Value> {
  /**
   * Given a handle object, check if it meets the filter criterion
   * @param v
   * @return
   */
  public abstract boolean accept(Value v);
}
