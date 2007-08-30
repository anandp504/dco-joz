package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Product Type Index implementation
 * @author nipun
 */

public class ProductTypeIndex extends ProductAttributeIndex<Integer, Handle>  {
  public ProductTypeIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kProductType;
  }

  public Integer getKey(IProduct p) {
	    return p.getProductType();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}
