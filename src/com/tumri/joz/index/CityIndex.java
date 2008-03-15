package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: nipun
 */
public class CityIndex extends ProductAttributeIndex<Integer, Handle> {
  public CityIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kCity;
  }

  public Integer getKey(IProduct p) {
    return p.getCity();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}
