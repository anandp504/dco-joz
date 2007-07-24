package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class NeutralWeight extends AttributeWeights {
  private static NeutralWeight g_Weight;
  public static NeutralWeight getInstance() {
    if (g_Weight == null) {
      synchronized(NeutralWeight.class) {
        if (g_Weight == null) {
          g_Weight = new NeutralWeight();
        }
      }
    }
    return g_Weight;
  }

  private NeutralWeight() {
  }

  public double getWeight(Handle h) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kNone);
  }

  public double getWeight(Integer pid) {
    return AttributeWeights.getAttributeWeight(IProduct.Attribute.kNone);
  }

  public int match(Handle v) {
    return 0;
  }

  public int match(Integer pid) {
    return 0;
  }

}
