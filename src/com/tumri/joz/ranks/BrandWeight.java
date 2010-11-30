package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class BrandWeight extends AttributeWeights {
	private static BrandWeight g_Weight;

	public static BrandWeight getInstance() {
		if (g_Weight == null) {
			synchronized (BrandWeight.class) {
				if (g_Weight == null) {
					g_Weight = new BrandWeight();
				}
			}
		}
		return g_Weight;
	}

	private BrandWeight() {
	}

	public double getWeight(Handle h) {
		return AttributeWeights.getAttributeWeight(IProduct.Attribute.kBrand);
	}

	public double getMaxWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kBrand);
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
