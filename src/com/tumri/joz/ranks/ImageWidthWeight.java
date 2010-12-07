package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Product Attribute Image width weight implementation
 * User: nipun
 */

public class ImageWidthWeight extends AttributeWeights {
	private static ImageWidthWeight g_Weight;

	public static ImageWidthWeight getInstance() {
		if (g_Weight == null) {
			synchronized (ImageWidthWeight.class) {
				if (g_Weight == null) {
					g_Weight = new ImageWidthWeight();
				}
			}
		}
		return g_Weight;
	}

	private ImageWidthWeight() {
	}

	public double getMaxWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kImageWidth);
	}

	public double getWeight(Handle h, double minWeight) {
		return AttributeWeights.getAttributeWeight(IProduct.Attribute.kImageWidth);
	}

	public boolean mustMatch() {
		return true;
	}

	public double getMinWeight() {
		return AttributeWeights.getMaxWeight(IProduct.Attribute.kImageWidth);
	}
}
