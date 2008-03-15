package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: nipun
 */
public class GlobalIdIndex extends ProductAttributeIndex<Integer, Handle> {
  public GlobalIdIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kGlobalId;
  }

  public Integer getKey(IProduct p) {
    return p.getGlobalId();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}
