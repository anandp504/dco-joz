package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * ProductType Weight class
 *
 * @author: nipun
 */
public class ProductTypeWeight extends AttributeWeights {
	private static ProductTypeWeight g_Weight;

	public static ProductTypeWeight getInstance() {
		if (g_Weight == null) {
			synchronized (ProductTypeWeight.class) {
				if (g_Weight == null) {
					g_Weight = new ProductTypeWeight();
				}
			}
		}
		return g_Weight;
	}

	private ProductTypeWeight() {
	}

	public double getMaxWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kProductType);
	}

	public double getWeight(Handle h) {
		return AttributeWeights.getAttributeWeight(IProduct.Attribute.kProductType);
	}

	public boolean mustMatch() {
		return true;
	}
}
