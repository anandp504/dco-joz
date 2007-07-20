package com.tumri.joz.Query;

import com.tumri.joz.filter.Filter;
import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;

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

  private IProduct.Attribute m_attribute;
  private boolean m_negation; // used to express !=, not in range queries
  protected SortedSet<ProductHandle> m_results;
  protected Filter<ProductHandle> m_filter;

  public abstract Type getType();
  public abstract int getCount();
  public abstract double getCost();
  public abstract SortedSet<ProductHandle> exec();
  public abstract Filter<ProductHandle> getFilter();

  protected SimpleQuery(IProduct.Attribute aAttribute) {
    m_attribute = aAttribute;
  }

  public IProduct.Attribute getAttribute() {
    return m_attribute;
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

    AttributeQuery that = (AttributeQuery) o;

    return ((getCost() < that.getCost()) ? -1 :
        (getCost() == that.getCost() ? 0 : 1));
  }

  public boolean hasIndex() {
    return ProductDB.getInstance().hasIndex(m_attribute);
  }

  protected SortedSet<ProductHandle> tableScan() {
    Filter<ProductHandle> lFilter = getFilter();
    SortedSet<ProductHandle> set = new TreeSet<ProductHandle>();
    if (lFilter != null) {
      Iterator<ProductHandle> iter = ProductDB.getInstance().getAll().iterator();
      while (iter.hasNext()) {
        ProductHandle lHandle = iter.next();
        if (lFilter.accept(lHandle))
          set.add(lHandle);
      }
    }
    return set;
  }

}