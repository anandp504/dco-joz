package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;
import com.tumri.utils.topk.NSATopKQuery;

import java.util.List;
import java.util.SortedSet;

/**
 * Set intersector for adpod results.
 *
 * @author bpatel
 */
public class AdPodSetIntersector extends SetIntersector<Handle> {

	public Handle getResult(Handle h, Double score) {
		return h.createHandle(score);
	}

	public AdPodSetIntersector(boolean strict) {
		super(strict);
	}

	public AdPodSetIntersector(AdPodSetIntersector set) {
		super(set);
	}

	public SetIntersector<Handle> clone() throws CloneNotSupportedException {
		return new AdPodSetIntersector(this);
	}

	@Override
	protected NSATopKQuery getTopKResults(List<SortedSet<Handle>> sortedSets, int numReqs, boolean strict) {
		ProductNSATopKQuery retQuery = new ProductNSATopKQuery(sortedSets, numReqs, strict);
		retQuery.setM_excludes(m_excludes);
		retQuery.setM_excludesWeight(m_excludesWeight);
		retQuery.setM_filters(m_filters);
		retQuery.setM_filtersWeight(m_filtersWeight);
		return retQuery;
	}
}
