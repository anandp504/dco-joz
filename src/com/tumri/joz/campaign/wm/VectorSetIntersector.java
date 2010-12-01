package com.tumri.joz.campaign.wm;

import com.tumri.joz.Query.ProductNSATopKQuery;
import com.tumri.joz.products.Handle;
import com.tumri.joz.Query.SetIntersector;
import com.tumri.utils.topk.NSATopKQuery;

import java.util.List;
import java.util.SortedSet;

/**
 * Set intersector for adpod results.
 *
 * @author nipun
 */
public class VectorSetIntersector {
//
//	public Handle getResult(Handle h, Double score) {
//		return h.createHandle(score);
//	}
//
//	public VectorSetIntersector(boolean strict) {
//		super(strict);
//	}
//
//	public VectorSetIntersector(VectorSetIntersector set) {
//		super(set);
//	}
//
//	public SetIntersector<Handle> clone() throws CloneNotSupportedException {
//		return new VectorSetIntersector(this);
//	}
//
//	@Override
//	protected NSATopKQuery getTopKResults(List<SortedSet<Handle>> sortedSets, int numReqs, boolean strict) {
//		ProductNSATopKQuery retQuery = new ProductNSATopKQuery(sortedSets, numReqs, strict);
//		retQuery.setM_excludes(m_excludes);
//		retQuery.setM_excludesWeight(m_excludesWeight);
//		retQuery.setM_filters(m_filters);
//		retQuery.setM_filtersWeight(m_filtersWeight);
//		return retQuery;
//	}
}