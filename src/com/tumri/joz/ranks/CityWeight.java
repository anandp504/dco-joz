package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CityWeight extends AttributeWeights {
  private static CityWeight g_Weight;
  public static CityWeight getInstance() {
    if (g_Weight == null) {
      synchronized(CityWeight.class) {
        if (g_Weight == null) {
          g_Weight = new CityWeight();
        }
      }
    }
    return g_Weight;
  }

  private CityWeight() {
  }

  public double getWeight(Handle h) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kCity);
  }

      /**
     * This returns false by default. Therefore allowing second best match in all derived cases
     * Overridden in other classes such as ProviderWight to return true. Therefore if provider match is
     * no found then the result is rejected.
     *
     * @return false
     */
    public boolean mustMatch() {
        return true;
    }
}
