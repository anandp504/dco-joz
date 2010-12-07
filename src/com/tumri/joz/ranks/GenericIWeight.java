package com.tumri.joz.ranks;

import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: scbraun
 * Date: Dec 3, 2010
 * Time: 9:46:54 AM
 */
public class GenericIWeight<Value> implements IWeight<Value> {
	double weight = 1.0;
	boolean isMustMatch;

	public GenericIWeight(double wt, boolean mm) {
		weight = wt;
		isMustMatch = mm;
	}

	public double getWeight(Value v, double minWeight) {
		return weight;
	}

	public int match(Value v) {
		return 1;
	}

	public boolean mustMatch() {
		return isMustMatch;
	}

	public double getMaxWeight() {
		return weight;
	}

	public double getMinWeight() {
		return weight;
	}
}
