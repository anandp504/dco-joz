package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class ProviderIndex extends Index<Integer, Handle> {
  public ProviderIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kProvider;
  }

  public Integer getKey(IProduct p) {
    return p.getProvider();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}
