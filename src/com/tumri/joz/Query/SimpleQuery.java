package com.tumri.joz.Query;

import com.tumri.joz.filter.Filter;
import com.tumri.joz.products.Handle;
import com.tumri.joz.ranks.IWeight;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public abstract class SimpleQuery implements Query, Comparable {
  protected final int kMax = 0x7FFFFFFF;

  public enum Type {
    kAttribute, kRange, kKeyword
  }

  private boolean m_negation; // used to express !=, not in range queries
  protected SortedSet<Handle> m_results;
  protected Filter<Handle> m_filter;

  public abstract Type getType();
  public abstract int getCount();
  public abstract double getCost();
  public abstract SortedSet<Handle> exec();
  public abstract Filter<Handle> getFilter();
  public abstract IWeight<Handle> getWeight();

  /**
   * @return true of the query has index built
   */
  public abstract boolean hasIndex();

  /**
   * @return list of all possible Handles
   */
  public abstract SortedSet<Handle> getAll();

  protected SimpleQuery() {
  }

  public boolean isNegation() {
    return m_negation;
  }

  public void setNegation(boolean aNegation) {
    m_negation = aNegation;
  }

  public int compareTo(Object o) {
    if (this == o) return 0;
    if (o == null || getClass() != o.getClass()) return -1;

    SimpleQuery that = (SimpleQuery) o;

    return ((getCost() < that.getCost()) ? -1 :
        (getCost() == that.getCost() ? 0 : 1));
  }

  protected SortedSet<Handle> tableScan() {
    Filter<Handle> lFilter = getFilter();
    SortedSet<Handle> set = new TreeSet<Handle>();
    if (lFilter != null) {
      Iterator<Handle> iter = getAll().iterator();
      while (iter.hasNext()) {
        Handle lHandle = iter.next();
        if (lFilter.accept(lHandle))
          set.add(lHandle);
      }
    }
    return set;
  }

  // Clear the internal results of last computation
  public void clear() {
    m_results = null;
  }
}