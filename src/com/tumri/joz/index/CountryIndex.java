package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: nipun
 */
public class CountryIndex extends ProductAttributeIndex<Integer, Handle> {
  public CountryIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kCountry;
  }

  public Integer getKey(IProduct p) {
    return p.getCountry();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}
