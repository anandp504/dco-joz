package com.tumri.joz.filter;

import com.tumri.joz.Query.MUPQuery;
import com.tumri.joz.products.Handle;

import java.util.ArrayList;
import java.util.SortedSet;

/**
 * Filter class that will handle long index values
 * User: nipun
 */
public abstract class LongFilter<Value> implements IFilter<Value> {
  private boolean m_negation;
  private ArrayList<Long> m_values = new ArrayList<Long>();
  private double m_min;
  private double m_max;
  private MUPQuery m_query;

  /**
   * Given a handle object, check if it meets the filter criterion
   * @return
   */
  public abstract LongFilter<Value> clone();

  protected LongFilter() {
  }

  protected LongFilter(LongFilter<Value> f) {
    m_negation = f.m_negation;
    m_values.addAll(f.m_values);
    m_min = f.m_min;
    m_max = f.m_max;
  }

  public MUPQuery getQuery() {
    return m_query;
  }

  public void setQuery(MUPQuery aQuery) {
    m_query = aQuery;
  }

  public boolean isNegation() {
    return m_negation;
  }

  public void setNegation(boolean aNegation) {
    m_negation = aNegation;
  }

  public final ArrayList<Long> getValues() {
    return m_values;
  }

  public void setValue(Long aValue) {
    m_values.add(aValue);
  }

  public void setValue(ArrayList<Long> aValues) {
    m_values.addAll(aValues);
  }

  public double getMin() {
    return m_min;
  }

  private void setMin(double aMin) {
    m_min = aMin;
  }

  public double getMax() {
    return m_max;
  }

  private void setMax(double aMax) {
    m_max = aMax;
  }

  public void setBounds(double min, double max) {
    setMin(min);
    setMax(max);
  }

  protected boolean isMatch(Long value) {
    return m_values.contains(value);
  }

  protected boolean inRange(double value) {
    return (m_min <= value && value < m_max);
  }


  public boolean accept(Value v) {
    SortedSet<Handle> set = m_query.exec();
    return ((set != null && set.contains(v)) ^ m_negation);
  }
}
