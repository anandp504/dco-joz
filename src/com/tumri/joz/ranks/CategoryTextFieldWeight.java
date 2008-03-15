package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CategoryTextFieldWeight extends AttributeWeights {
  private static CategoryTextFieldWeight g_Weight;
  public static CategoryTextFieldWeight getInstance() {
    if (g_Weight == null) {
      synchronized(CategoryTextFieldWeight.class) {
        if (g_Weight == null) {
          g_Weight = new CategoryTextFieldWeight();
        }
      }
    }
    return g_Weight;
  }

  private CategoryTextFieldWeight() {
  }

  public double getWeight(Handle h) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kCategoryTextField);
  }

}
