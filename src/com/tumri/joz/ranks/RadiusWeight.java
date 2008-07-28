package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Weight implementation for
 * User: nipun
 */
public class RadiusWeight extends AttributeWeights {
  private static RadiusWeight g_Weight;
  public static RadiusWeight getInstance() {
    if (g_Weight == null) {
      synchronized(RadiusWeight.class) {
        if (g_Weight == null) {
          g_Weight = new RadiusWeight();
        }
      }
    }
    return g_Weight;
  }

  private RadiusWeight() {
  }

  public double getWeight(Handle h) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kRadius);
  }

}