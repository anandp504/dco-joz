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
      synchronized(DiscountWeight.class) {
        if (g_Weight == null) {
          g_Weight = new DiscountWeight();
        }
      }
    }
    return g_Weight;
  }

  private DiscountWeight() {
  }

  public double getWeight(Handle h) {
    return ((ProductHandle)h).getDiscount();
  }

}
