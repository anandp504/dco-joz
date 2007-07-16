package com.tumri.joz.index;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public interface IIndex<Key, Value> {

  /**
   * Add a Value val to the index for the given Key key.
   * Natural order of the keys is used internally. Key and Value both should implement Comparable
   * @param key the key object
   * @param val the value object
   */
  public void put(Key key, Value val);

  /**
   * Given a Key key returns a SortedSet of values. The sort order is decided by the
   * natural order of the Value. Key and Value both should implement Comparable
   * @param key the key object
   */
  public SortedSet<Value> get(Key key);

  /**
   * Given a List of Key objects keys, returns the SortedSet of values. The sort order is decided by the
   * natural order of the Value. Key and Value both should implement Comparable
   * @param keys a List of key objects
   */
  public SortedSet<Value> get(List<Key> keys);

  /**
   * Given a Key key returns the count of associated values.
   * @param key the key object
   */
  public int getCount(Key key);
  /**
   * Given a List of Key objects keys, returns the count of associated values.
   * @param keys a List of key objects
   */
  public int getCount(ArrayList<Key> keys);

  /**
   * Given a Range of keys, returns a SortedSet of associated values.
   * @param keys a List of key objects
   */
  public SortedSet<Value> get(Key low, Key high);

  /**
   * Given a Range of keys, returns the count of associated values.
   * @param keys a List of key objects
   */
  public int getCount(Key low, Key high);

  /**
   * Remove all items from this index
   */
  public void clear();
}