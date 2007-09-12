package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Proudct Attribute index for Image height
 * User: nipun
 */

public class ImageHeightIndex extends ProductAttributeIndex<Integer, Handle> {
  public ImageHeightIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kImageHeight;
  }

  public Integer getKey(IProduct p) {
    return p.getImageHeight();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }

}
