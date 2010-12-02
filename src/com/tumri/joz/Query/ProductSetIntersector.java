package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;
import com.tumri.utils.topk.NSATopKQuery;

import java.util.List;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 */
public class ProductSetIntersector extends SetIntersector<Handle> {
	/**
	 * Given a Handle h and a score build a new handle
	 * This method should be overridden if default Pair<V,Double> is not acceptable
	 *
	 * @param h
	 * @param score
	 * @return a Pair<Pid,Double>
	 */
	public Handle getResult(Handle h, Double score) {
		return h.createHandle(score);
	}

	public ProductSetIntersector(boolean strict) {
		super(strict);
	}

	private ProductSetIntersector(ProductSetIntersector set) {
		super(set);
	}

	public SetIntersector<Handle> clone() throws CloneNotSupportedException {
		return new ProductSetIntersector(this);
	}

	@Override
	protected SortedSet<Handle> getTopKResults(List<SortedSet<Handle>> sortedSets, int numReqs, boolean strict) {
		ProductNSATopKQuery retQuery = new ProductNSATopKQuery(sortedSets, numReqs, strict);
		retQuery.setM_excludes(m_excludes);
		retQuery.setM_excludesWeight(m_excludesWeight);
		retQuery.setM_filters(m_filters);
		retQuery.setM_filtersWeight(m_filtersWeight);
		return retQuery;
	}

	/**
	 * Needed to override method in SetIntersector because it does not have access to "Handles" only "Values"
	 *
	 * @param v1
	 * @param v2
	 * @return
	 */
	protected long distance(Handle v1, Handle v2) {
		return Math.abs(v1.getOid() - v2.getOid());
	}
}
