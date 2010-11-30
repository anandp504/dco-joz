package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CPCWeight extends AttributeWeights {
	private static CPCWeight g_Weight;

	public static CPCWeight getInstance() {
		if (g_Weight == null) {
			synchronized (CPCWeight.class) {
				if (g_Weight == null) {
					g_Weight = new CPCWeight();
				}
			}
		}
		return g_Weight;
	}

	private CPCWeight() {
	}

	public double getMaxWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kCPC);
	}

	public double getWeight(Handle h) {
		return AttributeWeights.getAttributeWeight(IProduct.Attribute.kCPC);
	}

}
