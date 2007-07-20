package com.tumri.joz.index;

import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class SupplierIndex extends Index<Integer, ProductHandle> {
  public SupplierIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kSupplier;
  }

  public Integer getKey(IProduct p) {
    return p.getSupplier();
  }

  public ProductHandle getValue(IProduct p) {
    return p.getHandle();
  }
}
