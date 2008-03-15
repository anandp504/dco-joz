package com.tumri.joz.filter;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.IProduct;
import com.tumri.content.data.Product;

/**
 * Generic implementation for a String Filter implementation
 * Note that the Strings are converted to a Long value for the key
 * @author: nipun
 */
public class TextFilterImpl extends LongFilter<Handle> {

  private IProduct.Attribute type;

  public TextFilterImpl(IProduct.Attribute attr) {
      super();
      this.type = attr;
  }

  public TextFilterImpl(TextFilterImpl f) {
    super(f);
    type = f.type;
  }

  public boolean accept(Handle h) {
    if (!ProductDB.hasProductInfo()) {
      return super.accept(h);
    } else {
      Product p = ProductDB.getInstance().get(h);
      return ((p != null) && (isMatch((Long)p.getValue(type)) ^ isNegation()));
    }
  }

  public LongFilter<Handle> clone() {
    return new TextFilterImpl(this);
  }
}
