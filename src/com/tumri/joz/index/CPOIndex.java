package com.tumri.joz.index;

import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CPOIndex extends Index<Double, ProductHandle>  {
  public CPOIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kCPO;
  }

  public Double getKey(IProduct p) {
    return p.getCPO();
  }

  public ProductHandle getValue(IProduct p) {
    return p.getHandle();
  }
}
