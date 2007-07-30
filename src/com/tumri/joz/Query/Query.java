package com.tumri.joz.Query;

import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public interface Query {
  // Returns results from execution of the query
  public SortedSet exec();
  // Clear the internal results of last computation
  public void clear();
}