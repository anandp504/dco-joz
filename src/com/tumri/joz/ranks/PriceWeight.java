package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class PriceWeight extends AttributeWeights {
	private static PriceWeight g_Weight;

	public static PriceWeight getInstance() {
		if (g_Weight == null) {
			synchronized (PriceWeight.class) {
				if (g_Weight == null) {
					g_Weight = new PriceWeight();
				}
			}
		}
		return g_Weight;
	}

	private PriceWeight() {
	}

	public double getMaxWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kPrice);
	}

	public double getWeight(Handle h, double minWeight) {
		return AttributeWeights.getAttributeWeight(IProduct.Attribute.kPrice);
	}

	public double getMinWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kPrice);
	}

}
