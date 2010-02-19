package com.tumri.joz.index;

import com.tumri.joz.Query.SetIntersector;

/**
 * Created by IntelliJ IDEA.
 * User: scbraun
 * Date: Feb 10, 2010
 * Time: 11:01:03 AM
 */
public class RangeDomainMappingSetIntersector extends SetIntersector {

	public RangeDomainMappingSetIntersector(boolean strict) {
		super(strict);
	}

	protected RangeDomainMappingSetIntersector(SetIntersector other) {
		super(other);
	}

	@Override
	public Object getResult(Object o, Double score) {
		return o;
	}

	@Override
	public SetIntersector clone() throws CloneNotSupportedException {
		return new RangeDomainMappingSetIntersector(this);
	}
}
