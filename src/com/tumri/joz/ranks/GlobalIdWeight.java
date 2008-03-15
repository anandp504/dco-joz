package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class GlobalIdWeight extends AttributeWeights {
  private static GlobalIdWeight g_Weight;
  public static GlobalIdWeight getInstance() {
    if (g_Weight == null) {
      synchronized(GlobalIdWeight.class) {
        if (g_Weight == null) {
          g_Weight = new GlobalIdWeight();
        }
      }
    }
    return g_Weight;
  }

  private GlobalIdWeight() {
  }

  public double getWeight(Handle h) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kGlobalId);
  }

}
