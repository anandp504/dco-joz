package com.tumri.joz.index;

import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CPCIndex extends Index<Double, ProductHandle>  {
  public CPCIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kCPC;
  }

  public Double getKey(IProduct p) {
    return p.getCPC();
  }

  public ProductHandle getValue(IProduct p) {
    return p.getHandle();
  }
}
