package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Weight implementation for
 * User: nipun
 */
public class RadiusWeight extends AttributeWeights {
	private static RadiusWeight g_Weight;

	public static RadiusWeight getInstance() {
		if (g_Weight == null) {
			synchronized (RadiusWeight.class) {
				if (g_Weight == null) {
					g_Weight = new RadiusWeight();
				}
			}
		}
		return g_Weight;
	}

	private RadiusWeight() {
	}

	public double getMaxWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kRadius);
	}

	public double getWeight(Handle h, double minWeight) {
		return AttributeWeights.getAttributeWeight(IProduct.Attribute.kRadius);
	}

	public double getMinWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kRadius);
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