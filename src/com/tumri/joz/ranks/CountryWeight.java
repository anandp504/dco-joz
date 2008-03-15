package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CountryWeight extends AttributeWeights {
  private static CountryWeight g_Weight;
  public static CountryWeight getInstance() {
    if (g_Weight == null) {
      synchronized(CountryWeight.class) {
        if (g_Weight == null) {
          g_Weight = new CountryWeight();
        }
      }
    }
    return g_Weight;
  }

  private CountryWeight() {
  }

  public double getWeight(Handle h) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kCountry);
  }

}
