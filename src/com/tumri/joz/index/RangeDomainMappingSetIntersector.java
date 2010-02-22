package com.tumri.joz.index;

import com.tumri.joz.Query.SetIntersector;

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
}
