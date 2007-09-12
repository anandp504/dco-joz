package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Proudct Attribute index for Image width
 * User: nipun
 */

public class ImageWidthIndex extends ProductAttributeIndex<Integer, Handle> {
  public ImageWidthIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kImageWidth;
  }

  public Integer getKey(IProduct p) {
    return p.getImageWidth();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }

}
