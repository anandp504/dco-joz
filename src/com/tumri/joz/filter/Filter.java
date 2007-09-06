package com.tumri.joz.filter;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */ 
public abstract class Filter<Value> implements IFilter<Value> {
  private boolean m_negation;
  private ArrayList<Integer> m_values = new ArrayList<Integer>();
  private double m_min;
  private double m_max;

  /**
   * Given a handle object, check if it meets the filter criterion
   * @param v
   * @return
   */
  public abstract Filter<Value> clone();

  protected Filter() {
  }

  protected Filter(Filter<Value> f) {
    m_negation = f.m_negation;
    m_values.addAll(f.m_values);
    m_min = f.m_min;
    m_max = f.m_max;
  }

  public boolean isNegation() {
    return m_negation;
  }

  public void setNegation(boolean aNegation) {
    m_negation = aNegation;
  }

  public final ArrayList<Integer> getValues() {
    return m_values;
  }

  public void setValue(Integer aValue) {
    m_values.add(aValue);
  }

  public void setValue(ArrayList<Integer> aValues) {
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

  protected boolean isMatch(Integer value) {
    return m_values.contains(value);
  }

  protected boolean inRange(double value) {
    return (m_min <= value && value < m_max);
  }

}