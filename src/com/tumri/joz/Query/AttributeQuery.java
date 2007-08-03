package com.tumri.joz.Query;

import com.tumri.joz.filter.Filter;
import com.tumri.joz.index.Index;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;

import java.util.ArrayList;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class AttributeQuery extends SimpleQuery { 
  private ArrayList<Integer> m_values = new ArrayList<Integer>();
  private int m_count = kMax;

  public Type getType() {
    return Type.kAttribute;
  }

  public AttributeQuery(IProduct.Attribute aAttribute, int aValue) {
    super(aAttribute);
    m_values.add(aValue);
  }

  public AttributeQuery(IProduct.Attribute aAttribute, ArrayList<Integer> values) {
    super(aAttribute);
    m_values.addAll(values);
  }

  public final ArrayList<Integer> getValues() {
    return m_values;
  }

  public void addValue(int aValue) {
    m_values.add(aValue);
  }

  @SuppressWarnings("unchecked")
  public int getCount() {
    if (m_count == kMax) {
      // ??? This gets an "unchecked conversion" warning.
      Index<Integer,Handle> index = ProductDB.getInstance().getIndex(getAttribute());
      if (index != null) {
        m_count = index.getCount(m_values);
      }
    }
    return m_count;
  }

  @SuppressWarnings("unchecked")
  public SortedSet<Handle> exec() {
    if (m_results == null) {
      // ??? This gets an "unchecked conversion" warning.
      Index<Integer,Handle> index = ProductDB.getInstance().getIndex(getAttribute());
      m_results = (index != null) ? index.get(m_values) : tableScan();
    }
    return m_results;
  }

  public double getCost() {
    return getCount();
  }

  public Filter<Handle> getFilter() {
    if (m_filter == null) {
      m_filter = ProductDB.getInstance().getFilter(getAttribute());
      m_filter.setValue(m_values);
      m_filter.setNegation(isNegation());
    }
    return m_filter;
  }

}