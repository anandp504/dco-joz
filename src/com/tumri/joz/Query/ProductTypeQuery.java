package com.tumri.joz.Query;

import com.tumri.joz.filter.Filter;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.content.data.dictionary.DictionaryManager;

import java.util.SortedSet;

/**
 * Implementation of the ProductTypeQuery
 * @author nipun
 */

public class ProductTypeQuery extends MUPQuery { 
  private int m_count = kMax;
  private Integer m_productType;
  private Integer m_productTypeProd = null;
  private Integer m_productTypeLeadgen = null;
  private Integer m_productTypeBoth = null;
  
  public Type getType() {
    return Type.kProductType;
  }

  public ProductTypeQuery(Integer aValue) {
	super(IProduct.Attribute.kProductType);
	DictionaryManager dm = DictionaryManager.getInstance ();
	//TODO: Check this
	m_productTypeProd = dm.getId (IProduct.Attribute.kProductType, "Product");
	m_productTypeLeadgen = dm.getId (IProduct.Attribute.kProductType, "LEADGEN");
	m_productTypeBoth = dm.getId (IProduct.Attribute.kProductType, "BOTH");
	m_productType = aValue;
  }

  public Integer getProductType() {
    return m_productType;
  }

  public int getCount() {
    if (m_count == kMax) {
      @SuppressWarnings("unchecked")
      ProductAttributeIndex<Integer,Handle> index = ProductDB.getInstance().getIndex(getAttribute());
      if (index != null) {
        m_count = index.getCount(m_productType);
      }
    }
    return m_count;
  }
  
  @SuppressWarnings("unchecked")
  public SortedSet<Handle> exec() {
    if (m_results == null) {
      ProductAttributeIndex<Integer,Handle> index = ProductDB.getInstance().getIndex(getAttribute());
      if (m_productType.equals(m_productTypeLeadgen)) {
          m_results = (index != null) ? index.get(m_productTypeLeadgen) : tableScan();
      } else if (m_productType.equals(m_productTypeProd)) {
    	  m_results = (index != null) ? index.get(m_productTypeProd) : tableScan();
      } else if (m_productType.equals(m_productTypeBoth)) {
    	  //TODO: Verify if null or empty set needs to be passed for no results
          // This is not used yet, or else it is broken, sandeep 12/25/07
          m_results = null;
      }
    }
    return m_results;
  }

  public double getCost() {
    return getCount();
  }

  public Filter<Handle> getFilter() {
    if (m_filter == null) {
      m_filter = ProductDB.getInstance().getFilter(getAttribute());
      m_filter.setValue(m_productType);
      m_filter.setNegation(isNegation());
      m_filter.setQuery(this);
    }
    return m_filter;
  }

}
