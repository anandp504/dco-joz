package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Weight implementation for
 * User: nipun
 */
public class ZipCodeWeight extends AttributeWeights {
	private static ZipCodeWeight g_Weight;

	public static ZipCodeWeight getInstance() {
		if (g_Weight == null) {
			synchronized (ZipCodeWeight.class) {
				if (g_Weight == null) {
					g_Weight = new ZipCodeWeight();
				}
			}
		}
		return g_Weight;
	}

	private ZipCodeWeight() {
	}

	public double getMaxWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kZip);
	}

	public double getWeight(Handle h) {
		return AttributeWeights.getAttributeWeight(IProduct.Attribute.kZip);
	}

	/**
	 * This returns false by default. Therefore allowing second best match in all derived cases
	 * Overridden in other classes such as ProviderWight to return true. Therefore if provider match is
	 * no found then the result is rejected.
	 *
	 * @return false
	 */
	public boolean mustMatch() {
		return true;
	}
}
