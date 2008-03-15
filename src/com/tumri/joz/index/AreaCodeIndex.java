package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: nipun
 */
public class AreaCodeIndex extends ProductAttributeIndex<Integer, Handle> {
  public AreaCodeIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kArea;
  }

  public Integer getKey(IProduct p) {
    return p.getAreaCode();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}
