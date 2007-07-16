package com.tumri.joz.Query;

import com.tumri.joz.Query.SimpleQuery;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.Handle;
import com.tumri.joz.filter.Filter;

import java.util.SortedSet;
import java.util.TreeSet;

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

  public KeywordQuery(IProduct.Attribute aAttribute, String aKeywords) {
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
    return kMax; // @todo improve this with Lucenes help
  }

  public double getCost() {
    return ((double)getCount()) * 8; // @todo numbers are random guess work
  }

  public boolean hasIndex() {
    return true;
  }

  public SortedSet<Handle> exec() {
    if (m_results == null) {
      m_results = new TreeSet<Handle>();  // @todo results to be hooked to lucene
    }
    return m_results;
  }

  public Filter<Handle> getFilter() {
    return null;
  }
}