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
public class ConjunctQuery implements Query {
  private QueryProcessor m_queryProcessor;
  private ArrayList<SimpleQuery> m_queries = new ArrayList<SimpleQuery>();
  private SortedSet<Handle> m_results;
  private boolean m_strict = false; // If true Strict match only no rel. ranking
  private boolean m_scan = false; // Forces a table scan approach
  private Handle  m_reference;

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
    for (int i = 0; i < m_queries.size(); i++) {
      m_queries.get(i).clear();
    }
    m_results = null;
    m_reference = null;
  }

  @SuppressWarnings("unchecked")
  public SortedSet<Handle> exec() {
    if (m_results != null) return m_results;
    // ??? This gets an "unchecked method invocation" warning.
    Collections.sort(m_queries);
    SetIntersector<Handle> intersector =
    (isScan() ?  m_queryProcessor.buildTableScanner(m_queries) : m_queryProcessor.buildIntersector(m_queries));
    intersector.setReference(m_reference);
    intersector.setStrict(isStrict());
    long start = System.nanoTime();
    m_results = intersector.intersect();
    System.out.println("Time is " + (System.nanoTime() - start));
    return m_results;
  }


  /**
   * Not bo be used by external clients, use ConjunctQuery(QueryProcessor qp) instead
   */
  private ConjunctQuery() {
  }

}