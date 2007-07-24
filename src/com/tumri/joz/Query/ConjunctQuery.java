package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.ranks.AttributeWeights;
import com.tumri.joz.utils.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class ConjunctQuery implements Query {
  private ArrayList<SimpleQuery> m_queries = new ArrayList<SimpleQuery>();
  private SortedSet<Result> m_results;

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
  public SortedSet<Result> exec() {
    if (m_results != null) return m_results;
    Collections.sort(m_queries);
    ProductSetIntersector intersector = new ProductSetIntersector();
    for (int i = 0; i < m_queries.size(); i++) { // Step 1
      SimpleQuery lSimpleQuery = m_queries.get(i);
      if ((lSimpleQuery.getType() == SimpleQuery.Type.kAttribute ||
           lSimpleQuery.getType() == SimpleQuery.Type.kKeyword) &&
          lSimpleQuery.hasIndex() &&
          !lSimpleQuery.isNegation()) {
        intersector.include(lSimpleQuery.exec(), lSimpleQuery.getWeight()); // include indexed attribute/keyword queries first
      }
    }
    for (int i = 0; i < m_queries.size(); i++) {
      SimpleQuery lSimpleQuery = m_queries.get(i);
      if (lSimpleQuery.getType() == SimpleQuery.Type.kRange &&
          lSimpleQuery.hasIndex() &&
          !lSimpleQuery.isNegation()) {
        if (!intersector.hasIncludes() || i == 0) {
          intersector.include(lSimpleQuery.exec(), lSimpleQuery.getWeight()); // Step 2. include indexed range query if efficient
        } else {
          intersector.addFilter(lSimpleQuery.getFilter(), lSimpleQuery.getWeight()); // Step. 3 else range queries are filters
        }
      }
    }
    for (int i = 0; i < m_queries.size(); i++) {
      SimpleQuery lSimpleQuery = m_queries.get(i);
      if (!lSimpleQuery.hasIndex() && !lSimpleQuery.isNegation()) {
        if (!intersector.hasIncludes())
          intersector.include(lSimpleQuery.exec(), lSimpleQuery.getWeight()); // Step 4. non indexed table scan query
        else
          intersector.addFilter(lSimpleQuery.getFilter(), lSimpleQuery.getWeight()); // Step. 5 else range queries are filters
      }
    }
    if (!intersector.hasIncludes()) { // Step 6. this means we have closed world negation query
      intersector.include(ProductDB.getInstance().getAll(),AttributeWeights.getWeight(IProduct.Attribute.kNone)); // add our universe for negation
    }
    boolean excludeCategory = categoryExclusion(intersector);
    for (int i = 0; i < m_queries.size(); i++) {
      SimpleQuery lSimpleQuery = m_queries.get(i);
      if (lSimpleQuery.isNegation() && (!excludeCategory || lSimpleQuery.getAttribute() != IProduct.Attribute.kCategory)) {
        intersector.addFilter(lSimpleQuery.getFilter(), lSimpleQuery.getWeight()); // Step 7. Add filter for all negations
      }
    }
    long start = System.currentTimeMillis();
    m_results = intersector.intersect();
    System.out.println("Time is " + (System.currentTimeMillis() - start));
    return m_results;
  }

  public SortedSet<Result> getResults() {
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
      SortedSet<Handle> plusset = lCatPlus.exec();
      if (plusset instanceof MultiSortedSet) {
        SortedSet<Handle> minusset = lCatMinus.exec();
        List<SortedSet<Handle>> mlist = null;
        if (minusset instanceof MultiSortedSet) {
          mlist = ((MultiSortedSet<Handle>)minusset).getList();
        } else {
          mlist = new ArrayList<SortedSet<Handle>>();
          mlist.add(minusset);
        }
        List<SortedSet<Handle>> res= new ArrayList<SortedSet<Handle>>();
        res.addAll(((MultiSortedSet<Handle>)plusset).getList());
        res.removeAll(mlist);
        ArrayList<SortedSet<Handle>> alist = intersector.getIncludes();
        for(int i=0;i< alist.size() ; i++) {
          if (alist.get(i) == plusset) {
            alist.set(i,new MultiSortedSet<Handle>(res));
            break;
          }
        }
        return true;
      }
    }
    return false;
  }
}