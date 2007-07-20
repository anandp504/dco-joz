package com.tumri.joz.index;

import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class BrandIndex extends Index<Integer, ProductHandle> {
  public BrandIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kBrand;
  }

  public Integer getKey(IProduct p) {
    return p.getBrand();
  }

  public ProductHandle getValue(IProduct p) {
    return p.getHandle();
  }

}
