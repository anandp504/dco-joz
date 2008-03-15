package com.tumri.joz.filter;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.content.data.Product;

/**
 * User: nipun
 */
public class AreaCodeFilter extends Filter<Handle> {
  public AreaCodeFilter() {
    super();
  }

  @SuppressWarnings("unchecked")
  public AreaCodeFilter(Filter<Handle> f) {
    // ??? This gets an "unchecked conversion" warning.
    super(f);
  }

  public boolean accept(Handle h) {
    if (!ProductDB.hasProductInfo()) {
      return super.accept(h);
    } else {
      Product p = ProductDB.getInstance().get(h);
      return ((p != null) && (isMatch(p.getAreaCode()) ^ isNegation()));
    }
  }

  public Filter<Handle> clone() {
    return new AreaCodeFilter(this);
  }
}
