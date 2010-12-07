package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * This weight needs to be used for all optimized listing queries
 */
public class OptimizedWeight extends AttributeWeights {
	private static OptimizedWeight g_Weight;

	public static OptimizedWeight getInstance() {
		if (g_Weight == null) {
			synchronized (OptimizedWeight.class) {
				if (g_Weight == null) {
					g_Weight = new OptimizedWeight();
				}
			}
		}
		return g_Weight;
	}

	private OptimizedWeight() {
	}

	public double getMaxWeight() {
		return 1.2;
	}

	public double getWeight(Handle h, double minWeight) {
		return 1.2;
	}

	public double getMinWeight() {
		return 1.2;
	}


	/**
	 * This returns false by default. Therefore allowing second best match in all derived cases
	 *
	 * @return false
	 */
	public boolean mustMatch() {
		return false;
	}
}