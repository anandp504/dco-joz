package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * User: nipun
 */
public class MultiValueTextFieldWeight extends AttributeWeights {
  private static MultiValueTextFieldWeight g_Weight;
  public static MultiValueTextFieldWeight getInstance() {
    if (g_Weight == null) {
      synchronized(MultiValueTextFieldWeight.class) {
        if (g_Weight == null) {
          g_Weight = new MultiValueTextFieldWeight();
        }
      }
    }
    return g_Weight;
  }

  private MultiValueTextFieldWeight() {
  }

  public double getWeight(Handle h) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kMultiValueTextField);
  }

}