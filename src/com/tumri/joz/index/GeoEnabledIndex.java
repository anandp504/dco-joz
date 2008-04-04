package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Geo Filter Index implementation
 * @author nipun
 */

public class GeoEnabledIndex extends ProductAttributeIndex<Integer, Handle>  {
  public GeoEnabledIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kGeoEnabledFlag;
  }

  public Integer getKey(IProduct p) {
	 return p.getGeoEnabled();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}
