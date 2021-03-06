package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class KeywordsWeight extends AttributeWeights {
	private static KeywordsWeight g_Weight;

	public static KeywordsWeight getInstance() {
		if (g_Weight == null) {
			synchronized (KeywordsWeight.class) {
				if (g_Weight == null) {
					g_Weight = new KeywordsWeight();
				}
			}
		}
		return g_Weight;
	}

	private KeywordsWeight() {
	}

	public double getMaxWeight() {
		return 10.0;
	}

	public double getWeight(Handle h, double minWeight) {
		return h.getScore();
	}

	public double getMinWeight() {
		return 0.0;
	}

	public boolean mustMatch() {
		return true;
	}

}
