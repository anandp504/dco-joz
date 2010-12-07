package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * User: nipun
 */
public class MultiValueTextFieldWeight extends AttributeWeights {
	private static MultiValueTextFieldWeight g_Weight;

	public static MultiValueTextFieldWeight getInstance() {
		if (g_Weight == null) {
			synchronized (MultiValueTextFieldWeight.class) {
				if (g_Weight == null) {
					g_Weight = new MultiValueTextFieldWeight();
				}
			}
		}
		return g_Weight;
	}

	private MultiValueTextFieldWeight() {
	}

	public double getMaxWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kMultiValueTextField);
	}

	public double getWeight(Handle h, double minWeight) {
		return AttributeWeights.getAttributeWeight(IProduct.Attribute.kMultiValueTextField);
	}

	public double getMinWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kMultiValueTextField);
	}

}