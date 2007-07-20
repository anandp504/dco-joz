package com.tumri.joz.Query;

import com.tumri.joz.products.*;
import com.tumri.joz.index.IIndex;
import com.tumri.joz.filter.Filter;

import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class RangeQuery extends SimpleQuery {
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

  public int getCount() {
    if (m_count == kMax) {
      IIndex index = ProductDB.getInstance().getIndex(getAttribute());
      if (index != null) {
        m_count = index.getCount(m_min,m_max);
      }
    }
    return m_count;
  }

  public double getCost() {
    return ((double)getCount()) * 4; // @todo these numbers are random guess work needs to be nlogn
  }

  public SortedSet<ProductHandle> exec() {
    if (m_results == null) {
      IIndex index = ProductDB.getInstance().getIndex(getAttribute());
      m_results = (index != null) ? index.get(m_min,m_max) : tableScan();
    }
    return m_results;
  }

  public Filter<ProductHandle> getFilter() {
    if (m_filter == null) {
      m_filter = ProductDB.getInstance().getFilter(getAttribute());
      m_filter.setNegation(isNegation());
      m_filter.setBounds(m_min,m_max);
    }
    return m_filter;
  }

}