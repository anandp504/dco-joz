package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CategoryNumericFieldWeight extends AttributeWeights {
  private static CategoryNumericFieldWeight g_Weight;
  public static CategoryNumericFieldWeight getInstance() {
    if (g_Weight == null) {
      synchronized(CategoryNumericFieldWeight.class) {
        if (g_Weight == null) {
          g_Weight = new CategoryNumericFieldWeight();
        }
      }
    }
    return g_Weight;
  }

  private CategoryNumericFieldWeight() {
  }

  public double getWeight(Handle h) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kCategoryNumericField);
  }

}