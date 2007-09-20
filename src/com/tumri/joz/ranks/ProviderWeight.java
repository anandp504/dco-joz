package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class ProviderWeight extends AttributeWeights {
  private static ProviderWeight g_Weight;
  public static ProviderWeight getInstance() {
    if (g_Weight == null) {
      synchronized(ProviderWeight.class) {
        if (g_Weight == null) {
          g_Weight = new ProviderWeight();
        }
      }
    }
    return g_Weight;
  }
  
  private ProviderWeight() {
  }

  public double getWeight(Handle h) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kProvider);
  }


  public boolean mustMatch() {
    return true;
  }
}
