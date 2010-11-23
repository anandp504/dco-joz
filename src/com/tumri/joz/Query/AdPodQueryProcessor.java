package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;

import java.util.ArrayList;
import java.util.SortedSet;

/**
 * Query processor for adpods
 *
 * @author bpatel
 * @author nipun
 */
public class AdPodQueryProcessor extends QueryProcessor {

    public SetIntersector<Handle> buildTableScanner(ArrayList<SimpleQuery> aQueries, Handle reference,boolean isStrict) {
        throw new UnsupportedOperationException("This method is not yet implemented");
    }

    public SetIntersector<Handle> buildIntersector(ArrayList<SimpleQuery> queries, Handle reference,boolean isStrict,boolean isTopK) {
        AdPodSetIntersector intersector = new AdPodSetIntersector(isStrict);
        intersector.useTopK(isTopK);
        for(SimpleQuery query: queries) {
            if (query.hasIndex()) {
                SortedSet<Handle> results = query.exec();
                    intersector.include(results, (TargetingQuery)query);
            } else {
                intersector.addFilter(query.getFilter(), query.getWeight());
            }
        }
        return intersector;
    }
}
