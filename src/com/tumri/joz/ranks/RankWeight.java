package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductHandle;

/**
 * Product Rank IWeight implementation
 * User: nipun
 */
public class RankWeight extends AttributeWeights {
	private static RankWeight g_Weight;

	public static RankWeight getInstance() {
		if (g_Weight == null) {
			synchronized (RankWeight.class) {
				if (g_Weight == null) {
					g_Weight = new RankWeight();
				}
			}
		}
		return g_Weight;
	}

	private RankWeight() {
	}

	public double getMaxWeight() {
		return 2.0;
	}

	public double getWeight(Handle h, double minWeight) {

		double tmp = 0.004 - 0.001;
		if (tmp == 0.0) {
			tmp = 1.0;
		}
		tmp = tmp / 101.0;
		return 1 + tmp * ((ProductHandle) h).getRank();
	}

	public double getMinWeight() {
		return 0.0;
	}

}
