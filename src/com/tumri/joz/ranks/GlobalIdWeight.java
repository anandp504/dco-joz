package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class GlobalIdWeight extends AttributeWeights {
	private static GlobalIdWeight g_Weight;

	public static GlobalIdWeight getInstance() {
		if (g_Weight == null) {
			synchronized (GlobalIdWeight.class) {
				if (g_Weight == null) {
					g_Weight = new GlobalIdWeight();
				}
			}
		}
		return g_Weight;
	}

	private GlobalIdWeight() {
	}

	public double getMaxWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kGlobalId);
	}

	public double getWeight(Handle h) {
		return AttributeWeights.getAttributeWeight(IProduct.Attribute.kGlobalId);
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
