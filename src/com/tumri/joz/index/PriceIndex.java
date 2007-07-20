package com.tumri.joz.index;

import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class PriceIndex extends Index<Double, ProductHandle>  {
  public PriceIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kPrice;
  }

  public Double getKey(IProduct p) {
    return p.getPrice();
  }

  public ProductHandle getValue(IProduct p) {
    return p.getHandle();
  }
}
