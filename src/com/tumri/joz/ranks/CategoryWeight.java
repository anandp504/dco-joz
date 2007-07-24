package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CategoryWeight extends AttributeWeights {
  private static CategoryWeight g_Weight;
  
  public static CategoryWeight getInstance() {
    if (g_Weight == null) {
      synchronized(CategoryWeight.class) {
        if (g_Weight == null) {
          g_Weight = new CategoryWeight();
        }
      }
    }
    return g_Weight;
  }

  private CategoryWeight() {
  }

  public double getWeight(Handle h) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kCategory);
  }

  public double getWeight(Integer pid) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kCategory);
  }
}
