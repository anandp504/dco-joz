package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Product Attribute Image height weight implementation
 * User: nipun
 */

public class ImageHeightWeight extends AttributeWeights {
	private static ImageHeightWeight g_Weight;

	public static ImageHeightWeight getInstance() {
		if (g_Weight == null) {
			synchronized (ImageHeightWeight.class) {
				if (g_Weight == null) {
					g_Weight = new ImageHeightWeight();
				}
			}
		}
		return g_Weight;
	}

	private ImageHeightWeight() {
	}

	public double getMaxWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kImageHeight);
	}


	public double getWeight(Handle h, double minWeight) {
		return AttributeWeights.getAttributeWeight(IProduct.Attribute.kImageHeight);
	}

	public double getMinWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kImageHeight);
	}

	public boolean mustMatch() {
		return true;
	}
}
