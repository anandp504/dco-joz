package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: nipun
 */
public class HHIIndex extends ProductAttributeIndex<Integer, Handle> {
  public HHIIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kHHI;
  }

  public Integer getKey(IProduct p) {
    return p.getHHI();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}