package com.tumri.joz.filter;

import com.tumri.content.data.Product;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CPORangeFilter extends Filter<Handle> {
  public CPORangeFilter() {
    super();
  }

  @SuppressWarnings("unchecked")
  public CPORangeFilter(Filter<Handle> f) {
    // ??? This gets an "unchecked conversion" warning.
    super(f);
  }

  public boolean accept(Handle h) {
    if (!ProductDB.hasProductInfo()) {
      return super.accept(h);
    } else {
      Product p = ProductDB.getInstance().get(h);
      return ((p != null) && (inRange(p.getCPO()) ^ isNegation()));
    }
  }

  public Filter<Handle> clone() {
    return new CPORangeFilter(this);
  }
}
