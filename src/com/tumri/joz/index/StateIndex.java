package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: nipun
 */
public class StateIndex extends ProductAttributeIndex<Integer, Handle> {
  public StateIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kState;
  }

  public Integer getKey(IProduct p) {
    return p.getState();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}
