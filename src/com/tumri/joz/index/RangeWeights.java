package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.ranks.AttributeWeights;
import com.tumri.joz.ranks.IWeight;

/**
 * Created by IntelliJ IDEA.
 * User: scbraun
 * Date: Feb 10, 2010
 * Time: 1:20:51 PM
 */
public class RangeWeights implements IWeight {
	public double getWeight(Object v) {
		return 1;
	}

	public int match(Object v) {
		return 1;
	}

	public boolean mustMatch() {
		return true;
	}

	public double getMaxWeight() {
		return 1;
	}
}
