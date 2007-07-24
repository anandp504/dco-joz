package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CPCIndex extends Index<Double, Handle>  {
  public CPCIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kCPC;
  }

  public Double getKey(IProduct p) {
    return p.getCPC();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}
