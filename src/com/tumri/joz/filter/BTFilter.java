package com.tumri.joz.filter;

import com.tumri.content.data.Product;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;

/**
 * User: nipun
 */
public class BTFilter extends Filter<Handle> {
  public BTFilter() {
    super();
  }

  @SuppressWarnings("unchecked")
  public BTFilter(Filter<Handle> f) {
    // ??? This gets an "unchecked conversion" warning.
    super(f);
  }

  public boolean accept(Handle h) {
    if (!ProductDB.hasProductInfo()) {
      return super.accept(h);
    } else {
      Product p = ProductDB.getInstance().get(h);
      return ((p != null) && (isMatch(p.getBT()) ^ isNegation()));
    }
  }

  public Filter<Handle> clone() {
    return new BTFilter(this);
  }
}