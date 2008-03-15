package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: nipun
 */
public class DmaCodeIndex extends ProductAttributeIndex<Integer, Handle> {
  public DmaCodeIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kDMA;
  }

  public Integer getKey(IProduct p) {
    return p.getDmaCode();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}
