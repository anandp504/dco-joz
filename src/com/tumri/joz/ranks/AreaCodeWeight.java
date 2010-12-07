package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class AreaCodeWeight extends AttributeWeights {
	private static AreaCodeWeight g_Weight;

	public static AreaCodeWeight getInstance() {
		if (g_Weight == null) {
			synchronized (AreaCodeWeight.class) {
				if (g_Weight == null) {
					g_Weight = new AreaCodeWeight();
				}
			}
		}
		return g_Weight;
	}

	private AreaCodeWeight() {
	}

	public double getWeight(Handle h, double minWeight) {
		return AttributeWeights.getAttributeWeight(IProduct.Attribute.kArea);
	}

	public double getMinWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kArea);
	}

	public double getMaxWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kArea);
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
