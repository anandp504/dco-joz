package com.tumri.joz.index;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */

/**
 * Creates a Dictionary of values and allocates a unique integer to the Object value
 */
public interface IDictionary<Type> {
  /**
   * Given an Object return a unique integer value representing the Object Value
   * The returned value is guranteed to be unique int represenatation of the Object Value
   * The object must support the functions hashCode(), compare(Object o2), equals(Object obj)
   * @param obj
   * @return integer
   */
  public Integer getId(Type t);

  /**
   * Returns the value object for a given index
   * @param index
   * @return
   */
  public Type getValue(int index);

  /**
   * Remove an object from the directory
   * @param obj
   */
  public void remove(Type obj);

  /**
   * Remove an object given by the index from the directory
   * @param index
   */
  public void remove(int index);
}
