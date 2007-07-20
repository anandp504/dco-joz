package com.tumri.joz.Query;

import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.AttributeWeights;
import com.tumri.joz.index.MultiSortedSet;
import com.tumri.joz.utils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.SortedSet;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class ConjunctQuery implements Query {
  private ArrayList<SimpleQuery> m_queries = new ArrayList<SimpleQuery>();
  private SortedSet<Pair<ProductHandle,Double>> m_results;

  public ConjunctQuery() {
  }

  public ArrayList<SimpleQuery> getQueries() {
    return m_queries;
  }

  public void addQuery(SimpleQuery q) {
    m_queries.add(q);
  }

  /**
   * Order of walking through the query set
   * 1. Look for Attribute/kdeyword + indexed + positive (include: indexed)
   * 2.          Range              + indexed + positive (include: indexed if efficient)
   * 3.          Range              + indexed + positive (filter: if atleast one included)
   * 4.          all                + non indexed + positive (include: if no indexed yet)
   * 5.          all                + non indexed + positive (filter)
   * 6.          World                                    (include: for closed world negation if no includes yet)
   * 7.          all                              + negative (add as filters)
   *
   */
  public SortedSet<Pair<ProductHandle,Double>> exec() {
    if (m_results != null) return m_results;
    Collections.sort(m_queries);
    SetIntersector<ProductHandle> intersector = new SetIntersector<ProductHandle>();
    for (int i = 0; i < m_queries.size(); i++) { // Step 1
      SimpleQuery lSimpleQuery = m_queries.get(i);
      if ((lSimpleQuery.getType() == SimpleQuery.Type.kAttribute ||
           lSimpleQuery.getType() == SimpleQuery.Type.kKeyword) &&
          lSimpleQuery.hasIndex() &&
          !lSimpleQuery.isNegation()) {
        intersector.include(lSimpleQuery.exec(), AttributeWeights.getWeight(lSimpleQuery.getAttribute())); // include indexed attribute/keyword queries first
      }
    }
    for (int i = 0; i < m_queries.size(); i++) {
      SimpleQuery lSimpleQuery = m_queries.get(i);
      if (lSimpleQuery.getType() == SimpleQuery.Type.kRange &&
          lSimpleQuery.hasIndex() &&
          !lSimpleQuery.isNegation()) {
        if (!intersector.hasIncludes() || i == 0) {
          intersector.include(lSimpleQuery.exec(), AttributeWeights.getWeight(lSimpleQuery.getAttribute())); // Step 2. include indexed range query if efficient
        } else {
          intersector.addFilter(lSimpleQuery.getFilter(), AttributeWeights.getWeight(lSimpleQuery.getAttribute())); // Step. 3 else range queries are filters
        }
      }
    }
    for (int i = 0; i < m_queries.size(); i++) {
      SimpleQuery lSimpleQuery = m_queries.get(i);
      if (!lSimpleQuery.hasIndex() && !lSimpleQuery.isNegation()) {
        if (!intersector.hasIncludes())
          intersector.include(lSimpleQuery.exec(), AttributeWeights.getWeight(lSimpleQuery.getAttribute())); // Step 4. non indexed table scan query
        else
          intersector.addFilter(lSimpleQuery.getFilter(), AttributeWeights.getWeight(lSimpleQuery.getAttribute())); // Step. 5 else range queries are filters
      }
    }
    if (!intersector.hasIncludes()) { // Step 6. this means we have closed world negation query
      intersector.include(ProductDB.getInstance().getAll(),1.0); // add our universe for negation
    }
    boolean excludeCategory = categoryExclusion(intersector);
    for (int i = 0; i < m_queries.size(); i++) {
      SimpleQuery lSimpleQuery = m_queries.get(i);
      if (lSimpleQuery.isNegation() && (!excludeCategory || lSimpleQuery.getAttribute() != IProduct.Attribute.kCategory)) {
        intersector.addFilter(lSimpleQuery.getFilter(), AttributeWeights.getWeight(lSimpleQuery.getAttribute())); // Step 7. Add filter for all negations
      }
    }
    long start = System.currentTimeMillis();
    m_results = intersector.intersect();
    System.out.println("Time is " + (System.currentTimeMillis() - start));
    return m_results;
  }

  public SortedSet<Pair<ProductHandle,Double>> getResults() {
    return m_results;
  }

  /**
   * Computes the set differences of exclude category if include category yields a MultiSortedSet.
   * The excluded category set is simply removed from include category set using subset level differences
   * @return returns true if difference was already computed
   */
  private boolean categoryExclusion(SetIntersector intersector) {
    SimpleQuery lCatPlus = null;
    SimpleQuery lCatMinus = null;
    for (int i = 0; i < m_queries.size(); i++) {
      SimpleQuery lSimpleQuery = m_queries.get(i);
      if (lSimpleQuery.getAttribute() == IProduct.Attribute.kCategory) {
        if (lSimpleQuery.isNegation())
          lCatMinus = lSimpleQuery;
        else
          lCatPlus = lSimpleQuery;
      }
    }
    if (lCatPlus != null && lCatMinus != null) {
      SortedSet<ProductHandle> plusset = lCatPlus.exec();
      if (plusset instanceof MultiSortedSet) {
        SortedSet<ProductHandle> minusset = lCatMinus.exec();
        List<SortedSet<ProductHandle>> mlist = null;
        if (minusset instanceof MultiSortedSet) {
          mlist = ((MultiSortedSet<ProductHandle>)minusset).getList();
        } else {
          mlist = new ArrayList<SortedSet<ProductHandle>>();
          mlist.add(minusset);
        }
        List<SortedSet<ProductHandle>> res= new ArrayList<SortedSet<ProductHandle>>();
        res.addAll(((MultiSortedSet<ProductHandle>)plusset).getList());
        res.removeAll(mlist);
        ArrayList<SortedSet<ProductHandle>> alist = intersector.getIncludes();
        for(int i=0;i< alist.size() ; i++) {
          if (alist.get(i) == plusset) {
            alist.set(i,new MultiSortedSet<ProductHandle>(res));
            break;
          }
        }
        return true;
      }
    }
    return false;
  }

}