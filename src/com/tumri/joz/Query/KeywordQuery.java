package com.tumri.joz.Query;

import com.tumri.joz.filter.Filter;
import com.tumri.joz.index.SortedArraySet;
import com.tumri.joz.keywordServer.ProductIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

import java.util.ArrayList;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class KeywordQuery extends SimpleQuery {
  private String m_keywords;
  public Type getType() {
    return Type.kKeyword;
  }

  public KeywordQuery(String aKeywords) {
    super(IProduct.Attribute.kKeywords);
    m_keywords = aKeywords;
  }

  public void setKeywords(String aKeywords) {
    m_keywords = aKeywords;
  }

  public String getKeywords() {
    return m_keywords;
  }

  public int getCount() {
    return exec().size();
  }

  public double getCost() {
    return getCount();
  }

  public boolean hasIndex() {
    return true;
  }

  @SuppressWarnings("unchecked")
  public SortedSet<Handle> exec() {
    if (m_results == null) {
      ArrayList<Handle> res = ProductIndex.getInstance().search(m_keywords,0.0,2000);
      m_results = new SortedArraySet(res);
    }
    return m_results;
  }

  public Filter<Handle> getFilter() {
    return null;
  }
}