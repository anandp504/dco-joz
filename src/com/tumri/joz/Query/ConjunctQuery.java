package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class ConjunctQuery implements Query, Cloneable {
  private QueryProcessor m_queryProcessor;
  private ArrayList<SimpleQuery> m_queries = new ArrayList<SimpleQuery>();
  private boolean m_strict = false; // If true Strict match only no rel. ranking
  private boolean m_scan = false; // Forces a table scan approach
  private boolean m_useTopK = false; // Forces a topK style set intersection
  private Handle  m_reference;
  private int m_pagesize;
  private int m_currentPage;


  /**
   * Construct a Conjunct Query passing a QueryProcessor
   * @param qp instance of QueryProcessor object
   */
  public ConjunctQuery(QueryProcessor qp) {
    m_queryProcessor = qp;
  }

  public ArrayList<SimpleQuery> getQueries() {
    return m_queries;
  }

  public void addQuery(SimpleQuery q) {
    m_queries.add(q);
  }


  /**
   * @return current value of strict matching state
   */
  public boolean isStrict() {
    return m_strict;
  }

  /**
   * If set to true then strictly matching items are returned, no partial matches are considered
   * @return
   */
  public void setStrict(boolean aStrict) {
    m_strict = aStrict;
  }


  /**
   * @return state of table scan mode as a boolean
   */
  public boolean isScan() {
    return m_scan;
  }

  /**
   * Set the table scan mode for the query Processing
   * @param aScan boolean value
   */
  public void setScan(boolean aScan) {
    m_scan = aScan;
  }


  /**
   * @return state of topK mode as a boolean
   */
  public boolean isUseTopK() {
    return m_useTopK;
  }

  /**
   * Set the table scan mode for the query Processing
   * @param aTopK boolean value
   */
  public void setUseTopK(boolean aTopK) {
    m_useTopK = aTopK;
  }


  /**
   * Get the last reference handle used for computation
   * @return a Handle object if any, else returns null
   */
  public Handle getReference() {
    return m_reference;
  }

  /**
   * Set a reference point as a start of computation for the query processing
   * @param aReference a handle object as a start of computation point.
   *   If Null then computation starts from beginning.
   */
  public void setReference(Handle aReference) {
    m_reference = aReference;
  }
  /**
   * Clear the internal results of last computation, after a clean() operation is called the next
   * evaluation of the Query using exec() will cause computation to happen
   */
  public void clear() {
    for (SimpleQuery query : m_queries) {
      query.clear();
    }
    m_reference = null;
  }

  @SuppressWarnings("unchecked")
  public SortedSet<Handle> exec() {
    // ??? This gets an "unchecked method invocation" warning.
    Collections.sort(m_queries);
    SetIntersector<Handle> intersector =
    (isScan() ?  m_queryProcessor.buildTableScanner(m_queries, m_reference, isStrict()) : m_queryProcessor.buildIntersector(m_queries, m_reference, isStrict(), isUseTopK()));
    intersector.setMax(getMax());
    return intersector;
  }

  public Object clone() {
	  ConjunctQuery copyQuery = null;
      try {
    	  copyQuery = (ConjunctQuery) super.clone();
      }
      catch (CloneNotSupportedException e) {
          // this should never happen
          throw new InternalError(e.toString());
      }
      if (m_queries !=null) {
    	ArrayList<SimpleQuery> copyQueries = new ArrayList<SimpleQuery>(m_queries.size()+2); // some space for additional queries
        for (SimpleQuery _query : m_queries) {
          copyQueries.add(_query);
        }
        copyQuery.m_queries = copyQueries;
      }
      copyQuery.setUseTopK(m_useTopK);
      return copyQuery;
  }

  public void setBounds(int pagesize, int currentPage) {
    m_pagesize = pagesize;
    m_currentPage = currentPage;
  }

  private int getMax() {
    return ((m_pagesize > 0) ? (m_currentPage + 1) * m_pagesize:0);
  }

  /**
   * Not bo be used by external clients, use ConjunctQuery(QueryProcessor qp) instead
   */
  private ConjunctQuery() {
  }

}