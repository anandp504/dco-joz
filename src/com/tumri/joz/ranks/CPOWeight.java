package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CPOWeight extends AttributeWeights {
	private static CPOWeight g_Weight;

	public static CPOWeight getInstance() {
		if (g_Weight == null) {
			synchronized (CPOWeight.class) {
				if (g_Weight == null) {
					g_Weight = new CPOWeight();
				}
			}
		}
		return g_Weight;
	}

	private CPOWeight() {
	}

	public double getMaxWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kCPO);
	}

	public double getWeight(Handle h) {
		return AttributeWeights.getAttributeWeight(IProduct.Attribute.kCPO);
	}

}
