package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: nipun
 */
public class DiscountIndex extends ProductAttributeIndex<Integer, Handle> {
  public DiscountIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kDiscount;
  }

  public Integer getKey(IProduct p) {
    return p.getDiscount();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}