package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: nipun
 */
public class ZipCodeIndex extends ProductAttributeIndex<Integer, Handle> {
  public ZipCodeIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kZip;
  }

  public Integer getKey(IProduct p) {
    return p.getZip();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}
