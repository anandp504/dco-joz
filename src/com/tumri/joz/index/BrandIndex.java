package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class BrandIndex extends Index<Integer, Handle> {
  public BrandIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kBrand;
  }

  public Integer getKey(IProduct p) {
    return p.getBrand();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }

}
