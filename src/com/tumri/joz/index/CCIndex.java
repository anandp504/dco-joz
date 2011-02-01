package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: nipun
 */
public class CCIndex extends ProductAttributeIndex<Integer, Handle> {
  public CCIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kCC;
  }

  public Integer getKey(IProduct p) {
    return p.getCC();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}