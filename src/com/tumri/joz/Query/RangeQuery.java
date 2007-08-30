package com.tumri.joz.Query;

import java.util.SortedSet;

import com.tumri.joz.filter.Filter;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.utils.index.IIndex;
import com.tumri.utils.index.Index;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class RangeQuery extends MUPQuery {
  private double m_min;
  private double m_max;
  private int m_count = kMax;

  public Type getType() {
    return Type.kRange;
  }

  public RangeQuery(IProduct.Attribute aAttribute, double aAMin, double aAMax) {
    super(aAttribute);
    m_min = aAMin;
    m_max = aAMax;
  }

  public double getMin() {
    return m_min;
  }

  public double getMax() {
    return m_max;
  }

  public void setMin(double aMin) {
    m_min = aMin;
  }

  public void setMax(double aMax) {
    m_max = aMax;
  }

  @SuppressWarnings("unchecked")
  public int getCount() {
    if (m_count == kMax) {
      IIndex index = ProductDB.getInstance().getIndex(getAttribute());
      if (index != null) {
        // ??? This gets an "unchecked call" warning.
        m_count = index.getCount(m_min, m_max);
      }
    }
    return m_count;
  }

  public double getCost() {
    return ((double) getCount()) * 4; // @todo these numbers are random guess work needs to be nlogn
  }

  @SuppressWarnings("unchecked")
  public SortedSet<Handle> exec() {
    if (m_results == null) {
      // ??? This gets an "unchecked conversion" warning.
      Index<Double, Handle> index = ProductDB.getInstance().getIndex(getAttribute());
      m_results = (index != null) ? index.get(m_min, m_max) : tableScan();
    }
    return m_results;
  }

  public Filter<Handle> getFilter() {
    if (m_filter == null) {
      m_filter = ProductDB.getInstance().getFilter(getAttribute());
      m_filter.setNegation(isNegation());
      m_filter.setBounds(m_min, m_max);
    }
    return m_filter;
  }

}
