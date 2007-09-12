package com.tumri.joz.filter;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;

/**
 * Product Attribute filter for the Image Height property.
 * User: nipun
 */

public class ImageWidthFilter extends Filter<Handle> {
  public ImageWidthFilter() {
    super();
  }

  public ImageWidthFilter(ImageWidthFilter f) {
    super(f);
  }

  public boolean accept(Handle h) {
    IProduct p = ProductDB.getInstance().get(h);
    return ((p != null) && (isMatch(p.getImageWidth()) ^ isNegation()));
  }

  public Filter<Handle> clone() {
    return new ImageWidthFilter(this);
  }
}