package com.tumri.joz.Query;

import com.tumri.joz.utils.Result;
import com.tumri.joz.products.Handle;

import java.util.ArrayList;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CNFQuery implements Query {
  private ArrayList<ConjunctQuery> m_queries = new ArrayList<ConjunctQuery>();
  private Handle m_reference;
  private int m_pagesize = 12;
  private int m_maxPages = 4;

  private SortedSet<Result> m_results;

  public CNFQuery() {
  }

  public ArrayList<ConjunctQuery> getQueries() {
    return m_queries;
  }

  public void addQuery(ConjunctQuery q) {
    m_queries.add(q);
  }


  public Handle getReference() {
    return m_reference;
  }

  public void setReference(Handle aReference) {
    m_reference = aReference;
  }
  // Clear the internal results of last computation
  public void clear() {
    for (int i = 0; i < m_queries.size(); i++) {
      m_queries.get(i).clear();
    }
    m_results = null;
  }

  public SortedSet<Result> exec() {
    if (m_results == null) {
      SetUnionizer<Result> unionizer = new SetUnionizer<Result>();
      for (int i = 0; i < m_queries.size(); i++) {
        ConjunctQuery lConjunctQuery = m_queries.get(i);
        lConjunctQuery.setReference(m_reference);
        unionizer.include(lConjunctQuery.exec());
      }
      m_results = unionizer.union();
    }
    return m_results;
  }

  public void setBounds(int pagesize, int maxPages) {
    m_pagesize = pagesize;
    m_maxPages = maxPages;
  }

}