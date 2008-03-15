package com.tumri.joz.filter;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.content.data.Product;

import java.util.ArrayList;

/**
 * Generic implementaion of the RangeFilter
 * Note that the range value supported right now is for Double.
 * @author: nipun
 */
public class RangeFilterImpl extends LongFilter<Handle> {

  private IProduct.Attribute type;

  public RangeFilterImpl(IProduct.Attribute attr) {
    super();
    this.type = attr;
  }

  public RangeFilterImpl(RangeFilterImpl f) {
    super(f);
    type = f.type;
  }

  public boolean accept(Handle h) {
    if (!ProductDB.hasProductInfo()) {
      return super.accept(h);
    } else {
      Product p = ProductDB.getInstance().get(h);
      return ((p != null) && (inRange((Long)p.getValue(type)) ^ isNegation()));
    }
  }

  public LongFilter<Handle> clone() {
    return new RangeFilterImpl(this);
  }


    
}
