package com.tumri.joz.ranks;

import com.tumri.content.data.Product;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * User: scbraun
 * Date: 10/2/13
 */
public class OptTextFieldWeight extends AttributeWeights {
	private static OptTextFieldWeight g_Weight;

	public static OptTextFieldWeight getInstance() {
		if (g_Weight == null) {
			synchronized (OptTextFieldWeight.class) {
				if (g_Weight == null) {
					g_Weight = new OptTextFieldWeight();
				}
			}
		}
		return g_Weight;
	}

	private OptTextFieldWeight() {
	}

	public double getMaxWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kExperienceId);
	}

	public double getWeight(Handle h, double minWeight) {
		return AttributeWeights.getAttributeWeight(IProduct.Attribute.kExperienceId);
	}

	public double getMinWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kExperienceId);
	}

}
