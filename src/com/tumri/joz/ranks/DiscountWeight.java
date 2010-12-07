package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductHandle;

/**
 * Discount percentage iweight implementaion
 * User: nipun
 */
public class DiscountWeight extends AttributeWeights {
	private static DiscountWeight g_Weight;

	public static DiscountWeight getInstance() {
		if (g_Weight == null) {
			synchronized (DiscountWeight.class) {
				if (g_Weight == null) {
					g_Weight = new DiscountWeight();
				}
			}
		}
		return g_Weight;
	}

	private DiscountWeight() {
	}

	public double getMaxWeight() {
		return 2.0;
	}

	public double getWeight(Handle h, double minWeight) {
		if (minWeight <= 1.0) {
			return ((ProductHandle) h).getDiscount();
		}
		double tmp = minWeight - ((int) minWeight);
		if (tmp == 0.0) {
			tmp = 1.0;
		}
		tmp = tmp / 101.0;
		return 1 + tmp * ((ProductHandle) h).getDiscount();
	}

	public double getMinWeight() {
		return 0.0;
	}

}
