package com.tumri.joz.Query;

import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.filter.CategoryFilter;
import com.tumri.joz.filter.Filter;
import com.tumri.joz.filter.LongFilter;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.List;

/**
 * Query class implementation to handle category attributes
 * Note: The only difference from the Attribute query is that the values are longs and not ints
 * User: nipun
 */
public class CategoryAttributeQuery extends MUPQuery { 
  private ArrayList<Long> m_values = new ArrayList<Long>();
  private int m_count = kMax;
  protected LongFilter<Handle> m_filter;


  public Type getType() {
    return Type.kAttribute;
  }

  public CategoryAttributeQuery(IProduct.Attribute aAttribute, long aValue) {
    super(aAttribute);
    m_values.add(aValue);
  }

  public CategoryAttributeQuery(IProduct.Attribute aAttribute, ArrayList<Long> values) {
    super(aAttribute);
    m_values.addAll(values);
  }

  public final ArrayList<Long> getValues() {
    return m_values;
  }

  public void addValue(long aValue) {
    m_values.add(aValue);
  }

  public int getCount() {
    if (m_count == kMax) {
      @SuppressWarnings("unchecked")
      ProductAttributeIndex<Long,Handle> index = ProductDB.getInstance().getIndex(getAttribute());
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
      ProductAttributeIndex<Long,Handle> index = ProductDB.getInstance().getIndex(getAttribute());
      List<Long> values = m_values;
      m_results = (index != null) ? index.get(values) : tableScan();
    }
    return m_results;
  }

  public double getCost() {
    return getCount();
  }

  public LongFilter<Handle> getFilter() {
    if (m_filter == null) {
      m_filter = ProductDB.getInstance().getLongFilter(getAttribute());
      m_filter.setValue(m_values);
      m_filter.setNegation(isNegation());
      m_filter.setQuery(this);
    }
    return m_filter;
  }

}
