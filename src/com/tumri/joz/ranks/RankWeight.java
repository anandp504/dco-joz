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
            synchronized(RankWeight.class) {
                if (g_Weight == null) {
                    g_Weight = new RankWeight();
                }
            }
        }
        return g_Weight;
    }

    private RankWeight() {
    }

    public double getWeight(Handle h) {
        //Safe to cast to product handle.
        return ((ProductHandle)h).getRank();
    }

}
