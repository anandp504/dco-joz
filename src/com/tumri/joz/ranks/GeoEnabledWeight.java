package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class GeoEnabledWeight extends AttributeWeights {
	private static GeoEnabledWeight g_Weight;

	public static GeoEnabledWeight getInstance() {
		if (g_Weight == null) {
			synchronized (GeoEnabledWeight.class) {
				if (g_Weight == null) {
					g_Weight = new GeoEnabledWeight();
				}
			}
		}
		return g_Weight;
	}

	private GeoEnabledWeight() {
	}

	public double getMaxWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kGeoEnabledFlag);
	}

	public double getWeight(Handle h, double minWeight) {
		return AttributeWeights.getAttributeWeight(IProduct.Attribute.kGeoEnabledFlag);
	}

	public double getMinWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kGeoEnabledFlag);
	}

	public boolean mustMatch() {
		return true;
	}

}
