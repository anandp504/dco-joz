package com.tumri.joz.filter;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CPCRangeFilter extends Filter<Handle> {
  public CPCRangeFilter() {
    super();
  }

  public CPCRangeFilter(Filter<Handle> f) {
    super(f);
  }

  public boolean accept(Handle h) {
    IProduct p = ProductDB.getInstance().get(h);
    return ((p != null) && (inRange(p.getCPC()) ^ isNegation()));
  }

  public Filter<Handle> clone() {
    return new CPCRangeFilter(this);
  }
}
