package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: nipun
 */
public class BTIndex extends ProductAttributeIndex<Integer, Handle> {
  public BTIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kBT;
  }

  public Integer getKey(IProduct p) {
    return p.getBT();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}