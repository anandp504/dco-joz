package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class AreaCodeWeight extends AttributeWeights {
  private static AreaCodeWeight g_Weight;
  public static AreaCodeWeight getInstance() {
    if (g_Weight == null) {
      synchronized(AreaCodeWeight.class) {
        if (g_Weight == null) {
          g_Weight = new AreaCodeWeight();
        }
      }
    }
    return g_Weight;
  }

  private AreaCodeWeight() {
  }

  public double getWeight(Handle h) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kArea);
  }

}