package com.tumri.joz.ranks;

import com.tumri.utils.data.GradedSetWrapper;

import java.util.SortedSet;

/**
 * User: scbraun
 * Date: Nov 18, 2010
 */
public class IWeightGradedSetWrapper<Value> extends GradedSetWrapper<Value> {
	IWeight<Value> weight = null;

	public IWeightGradedSetWrapper(SortedSet<Value> set, IWeight<Value> iWeight) {
		super(set);
		weight = iWeight;
	}

	public double getGrade(Value p) {
		return weight.getWeight(p);
	}

	public double getMaxGrade() {
		return weight.getMaxWeight();
	}

	public boolean isMustMatch() {
		return weight.mustMatch();
	}
}
