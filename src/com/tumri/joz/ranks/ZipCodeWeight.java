package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Weight implementation for 
 * User: nipun
 */
public class ZipCodeWeight extends AttributeWeights {
  private static ZipCodeWeight g_Weight;
  public static ZipCodeWeight getInstance() {
    if (g_Weight == null) {
      synchronized(ZipCodeWeight.class) {
        if (g_Weight == null) {
          g_Weight = new ZipCodeWeight();
        }
      }
    }
    return g_Weight;
  }

  private ZipCodeWeight() {
  }

  public double getWeight(Handle h) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kZip);
  }

}