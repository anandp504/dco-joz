package com.tumri.joz.filter;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class BrandFilter extends Filter<Handle> {
  public BrandFilter() {
    super();
  }

  public BrandFilter(BrandFilter f) {
    super(f);
  }

  public boolean accept(Handle h) {
    IProduct p = ProductDB.getInstance().get(h);
    return ((p != null) && (isMatch(p.getBrand()) ^ isNegation()));
  }

  public Filter<Handle> clone() {
    return new BrandFilter(this);
  }
}