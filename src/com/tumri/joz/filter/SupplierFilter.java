package com.tumri.joz.filter;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class SupplierFilter extends Filter<Handle> {
  public SupplierFilter() {
    super();
  }

  public SupplierFilter(Filter f) {
    super(f);
  }

  public boolean accept(Handle h) {
    IProduct p = ProductDB.getInstance().get(h);
    return ((p != null) && (isMatch(p.getSupplier()) ^ isNegation()));
  }

  public Filter<Handle> clone() {
    return new SupplierFilter(this);
  }
}