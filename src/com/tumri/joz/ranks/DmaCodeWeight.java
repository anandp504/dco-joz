package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class DmaCodeWeight extends AttributeWeights {
	private static DmaCodeWeight g_Weight;

	public static DmaCodeWeight getInstance() {
		if (g_Weight == null) {
			synchronized (DmaCodeWeight.class) {
				if (g_Weight == null) {
					g_Weight = new DmaCodeWeight();
				}
			}
		}
		return g_Weight;
	}

	private DmaCodeWeight() {
	}

	public double getMaxWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kDMA);
	}

	public double getWeight(Handle h, double minWeight) {
		return AttributeWeights.getAttributeWeight(IProduct.Attribute.kDMA);
	}

	public double getMinWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kDMA);
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
