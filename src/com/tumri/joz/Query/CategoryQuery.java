package com.tumri.joz.Query;

import com.tumri.joz.filter.CategoryFilter;
import com.tumri.joz.filter.Filter;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.List;

/**
 * This class is used specifically for the Category query without computing descendants
 * User: nipun
 */
public class CategoryQuery extends AttributeQuery {

  public CategoryQuery(int aValue) {
    super(IProduct.Attribute.kCategory, aValue);
    m_values.add(aValue);
  }

  @SuppressWarnings("unchecked")
  public SortedSet<Handle> exec() {
    if (m_results == null) {
      ProductAttributeIndex<Integer,Handle> index = ProductDB.getInstance().getIndex(getAttribute());
      List<Integer> values = m_values;
      m_results = (index != null) ? index.get(values) : tableScan();
    }
    return m_results;
  }

}
