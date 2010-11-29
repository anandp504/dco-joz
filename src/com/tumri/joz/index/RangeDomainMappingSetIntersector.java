package com.tumri.joz.index;

import com.tumri.joz.Query.ProductNSATopKQuery;
import com.tumri.joz.Query.SetIntersector;
import com.tumri.utils.topk.NSATopKQuery;

import java.util.List;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: scbraun
 * Date: Feb 10, 2010
 * Time: 11:01:03 AM
 */
public class RangeDomainMappingSetIntersector<Value> extends SetIntersector<Range<Value>> {

	public RangeDomainMappingSetIntersector(boolean strict) {
		super(strict);
	}

	protected RangeDomainMappingSetIntersector(SetIntersector<Range<Value>> other) {
		super(other);
	}

	@Override
	public Range<Value> getResult(Range<Value> v, Double score) {
		return v;
	}

	@Override
	public SetIntersector<Range<Value>> clone() throws CloneNotSupportedException {
		return new RangeDomainMappingSetIntersector<Value>(this);
	}

	@Override
	protected NSATopKQuery getTopKResults(List<SortedSet<Range<Value>>> sortedSets, int numReqs, boolean strict) {
		RangeNSATopKQuery retQuery = new RangeNSATopKQuery(sortedSets, numReqs, strict);
		retQuery.setM_excludes(m_excludes);
		retQuery.setM_excludesWeight(m_excludesWeight);
		retQuery.setM_filters(m_filters);
		retQuery.setM_filtersWeight(m_filtersWeight);
		return retQuery;
	}
}
