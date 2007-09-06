package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class SupplierWeight extends AttributeWeights {
  private static SupplierWeight g_Weight;
  public static SupplierWeight getInstance() {
    if (g_Weight == null) {
      synchronized(SupplierWeight.class) {
        if (g_Weight == null) {
          g_Weight = new SupplierWeight();
        }
      }
    }
    return g_Weight;
  }

  private SupplierWeight() {
  }

  public double getWeight(Handle h) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kSupplier);
  }

}
