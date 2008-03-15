package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class StateWeight extends AttributeWeights {
  private static StateWeight g_Weight;
  public static StateWeight getInstance() {
    if (g_Weight == null) {
      synchronized(StateWeight.class) {
        if (g_Weight == null) {
          g_Weight = new StateWeight();
        }
      }
    }
    return g_Weight;
  }

  private StateWeight() {
  }

  public double getWeight(Handle h) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kState);
  }

}
