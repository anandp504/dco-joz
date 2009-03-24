package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.ranks.AttributeWeights;
import com.tumri.utils.data.MultiSortedSet;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * Date: Aug 24, 2007
 * Time: 1:30:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProductQueryProcessor extends QueryProcessor {

  public ProductQueryProcessor() {
    super();
  }

  public SetIntersector<Handle> buildTableScanner(ArrayList<SimpleQuery> aQueries, Handle reference, boolean isStrict) {
    ProductSetIntersector aIntersector = new ProductSetIntersector(isStrict);
    if (aQueries.size() != 0) {
      for (SimpleQuery aQuery : aQueries) {
        MUPQuery mq = (MUPQuery) aQuery;
        if (mq.getAttribute() == IProduct.Attribute.kKeywords) {
          if (((KeywordQuery)mq).isInternal()) {
            aIntersector.includeRankedSet(mq.exec(), AttributeWeights.getWeight(IProduct.Attribute.kKeywords));
          } else {
            aIntersector.includeRankedSet(((KeywordQuery)mq).rawResults(), AttributeWeights.getWeight(IProduct.Attribute.kKeywords));
          }
        }
        else
        aIntersector.addFilter(mq.getFilter(), mq.getWeight());
      }
      if (!aIntersector.hasIncludes() && aIntersector.getRankedSet() == null)
        aIntersector.include(ProductDB.getInstance().getAll(), AttributeWeights.getWeight(IProduct.Attribute.kNone)); // add our universe for negation
    }
    aIntersector.setReference(reference);
    return aIntersector;
  }
  /**
   * Order of walking through the query set
   * 0. Handle internal and external keyword queries
   * 1. Look for Attribute          + indexed + positive (include: indexed)
   * 2.          Range              + indexed + positive (include: indexed if efficient)
   * 3.          Range              + indexed + positive (filter: if atleast one included)
   * 4.          all                + non indexed + positive (include: if no indexed yet)
   * 5.          all                + non indexed + positive (filter)
   * 6.          all                              + negative (add as filters)
   * 7.          World                                    (include: for closed world negation if no includes yet)
   *
   */
  public SetIntersector<Handle> buildIntersector(ArrayList<SimpleQuery> aQueries, Handle reference, boolean isStrict) {
    ProductSetIntersector aIntersector = new ProductSetIntersector(isStrict);
    if (aQueries.size() == 0){
      //aIntersector.include(ProductDB.getInstance().getAll(), AttributeWeights.getWeight(IProduct.Attribute.kNone)); // add our universe for negation
    } else {
      handleKeywordQueries(aQueries,aIntersector); // step 0, take care of all keyword queries
      for (SimpleQuery lSimpleQuery : aQueries) { // Step 1
        if ((lSimpleQuery.getType() == SimpleQuery.Type.kAttribute ||
            lSimpleQuery.getType() == SimpleQuery.Type.kProductType) &&
            lSimpleQuery.hasIndex() &&
            !lSimpleQuery.isNegation()) {
          aIntersector.include(lSimpleQuery.exec(), lSimpleQuery.getWeight()); // include indexed attribute/keyword queries first
        }
      }
      for (int i = 0; i < aQueries.size(); i++) {
        SimpleQuery lSimpleQuery = aQueries.get(i);
        if (lSimpleQuery.getType() == SimpleQuery.Type.kRange &&
            lSimpleQuery.hasIndex() &&
            !lSimpleQuery.isNegation()) {
          if (!aIntersector.hasIncludes() || i == 0 || shouldInclude(aQueries,lSimpleQuery)) {
            aIntersector.include(lSimpleQuery.exec(), lSimpleQuery.getWeight()); // Step 2. include indexed range query if efficient
          } else {
            aIntersector.addFilter(lSimpleQuery.getFilter(), lSimpleQuery.getWeight()); // Step. 3 else range queries are filters
          }
        }
      }
      for (SimpleQuery lSimpleQuery : aQueries) {
        if (!lSimpleQuery.hasIndex() && !lSimpleQuery.isNegation()) {
          if (!aIntersector.hasIncludes())
            aIntersector.include(lSimpleQuery.exec(), lSimpleQuery.getWeight()); // Step 4. non indexed table scan query
          else
            aIntersector.addFilter(lSimpleQuery.getFilter(), lSimpleQuery.getWeight()); // Step. 5 else range queries are filters
        }
      }
      boolean excludeCategory = categoryExclusion(aIntersector, aQueries);
      for (SimpleQuery aQuery : aQueries) {
        MUPQuery mupQuery = (MUPQuery) aQuery;
        if (mupQuery.isNegation() && (!excludeCategory || mupQuery.getAttribute() != IProduct.Attribute.kCategory)) {
          aIntersector.addFilter(mupQuery.getFilter(), mupQuery.getWeight()); // Step 6. Add filter for all negations
        }
      }
      if (!aIntersector.hasIncludes() && aIntersector.getRankedSet() == null &&
          (aIntersector.hasFilters() || aIntersector.hasExcludes())) { // Step 7. this means we have closed world negation query
        aIntersector.include(ProductDB.getInstance().getAll(), AttributeWeights.getWeight(IProduct.Attribute.kNone)); // add our universe for negation
      }
    }
    aIntersector.setReference(reference);
    return aIntersector;
  }

  /**
   * Handles keyword queries as follows:
   * Looks for one or two queries in the list.
   * If one query is present then uses the ranked include with rawResults of query
   * If two queries are present then internal query is included in Handle order while the external query is included ranked with rawResults
   * @param queries
   * @param aIntersector
   */
  private void handleKeywordQueries(List<SimpleQuery> queries, ProductSetIntersector aIntersector) {
    KeywordQuery kq0=null, kq1=null;
    for (SimpleQuery q : queries) {
      if (q.getType() == SimpleQuery.Type.kKeyword) {
        if (kq0 == null)
          kq0 = (KeywordQuery)q;
        else if (kq1 == null)
          kq1 = (KeywordQuery)q;
      }
    }
    if (kq0 !=null) {
       if (kq0.isInternal()) {
            aIntersector.include(kq0.exec(),kq0.getWeight());
       } else {
            aIntersector.includeRankedSet(kq0.rawResults(),kq0.getWeight());
       }
    }

    if (kq1 !=null) {
       if (kq1.isInternal()) {
           aIntersector.include(kq1.exec(),kq1.getWeight());
       } else {
           aIntersector.includeRankedSet(kq1.rawResults(),kq1.getWeight());
       }
    } 
  }

  /**
   * Computes the set differences of exclude category if include category yields a MultiSortedSet.
   * The excluded category set is simply removed from include category set using subset level differences
   * @return returns true if difference was already computed
   */
  private boolean categoryExclusion(ProductSetIntersector intersector, ArrayList<SimpleQuery> aQueries) {
    SimpleQuery lCatPlus = null;
    SimpleQuery lCatMinus = null;
    for (SimpleQuery aQuery : aQueries) {
      MUPQuery lSimpleQuery = (MUPQuery) aQuery;
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
        List<SortedSet<Handle>> minuslist = null;
        if (minusset instanceof MultiSortedSet) {
          minuslist = ((MultiSortedSet<Handle>)minusset).getList();
        } else {
          minuslist = new ArrayList<SortedSet<Handle>>();
          minuslist.add(minusset);
        }
        List<SortedSet<Handle>> res= new ArrayList<SortedSet<Handle>>();
        res.addAll(((MultiSortedSet<Handle>)plusset).getList());
        boolean removedAll = true; // Have all sets been removed, initially true
        boolean removedSome = false; // Have some sets been removed, initially false
        for (SortedSet<Handle> set : minuslist) {
          boolean removed = res.remove(set);
          removedSome = removedSome || removed;
          removedAll = removedAll && removed;
        }
        if (removedSome) {
          ArrayList<SortedSet<Handle>> alist = intersector.getIncludes();
          for(int i=0;i< alist.size() ; i++) {
            if (alist.get(i) == plusset) {
              alist.set(i,new MultiSortedSet<Handle>(res));
              break;
            }
          }
        }
        return removedAll; // Unless all were removed the computation is not complete
      }
    }
    return false;
  }

  private boolean shouldInclude(List<SimpleQuery> queries, SimpleQuery sq) {
    double count = sq.getCost();
    int lowercost = 0;
    int positiveQueries = 0;
    for(SimpleQuery q : queries) {
      if (q.isNegation())
        continue;
      positiveQueries++;
      if (count <= q.getCost())
        lowercost++;
    }
    return (lowercost * 2) > positiveQueries;
  }
}
