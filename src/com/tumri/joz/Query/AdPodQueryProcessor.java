package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;

import java.util.ArrayList;

/**
 * Qurey processor for adpods
 *
 * @author bpatel
 */
public class AdPodQueryProcessor extends QueryProcessor {
    public SetIntersector<Handle> buildTableScanner(ArrayList<SimpleQuery> aQueries, Handle reference) {
        throw new UnsupportedOperationException("This method is not yet implemented");
    }

    public SetIntersector<Handle> buildIntersector(ArrayList<SimpleQuery> queries, Handle reference) {
        AdPodSetIntersector intersector = new AdPodSetIntersector();
        for(SimpleQuery query: queries) {
            intersector.include(query.exec(), (TargetingQuery)query);
        }
        return intersector;
    }
}
