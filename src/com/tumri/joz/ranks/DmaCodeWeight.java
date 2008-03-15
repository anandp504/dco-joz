package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class DmaCodeWeight extends AttributeWeights {
  private static DmaCodeWeight g_Weight;
  public static DmaCodeWeight getInstance() {
    if (g_Weight == null) {
      synchronized(DmaCodeWeight.class) {
        if (g_Weight == null) {
          g_Weight = new DmaCodeWeight();
        }
      }
    }
    return g_Weight;
  }

  private DmaCodeWeight() {
  }

  public double getWeight(Handle h) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kDMA);
  }

}
