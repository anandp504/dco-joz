package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: nipun
 */
public class MSIndex extends ProductAttributeIndex<Integer, Handle> {
  public MSIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kMS;
  }

  public Integer getKey(IProduct p) {
    return p.getMS();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}