package com.tumri.joz.filter;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.content.data.Product;

/**
 * User: nipun
 */
public class CountryFilter extends Filter<Handle> {
  public CountryFilter() {
    super();
  }

  @SuppressWarnings("unchecked")
  public CountryFilter(Filter<Handle> f) {
    // ??? This gets an "unchecked conversion" warning.
    super(f);
  }

  public boolean accept(Handle h) {
    if (!ProductDB.hasProductInfo()) {
      return super.accept(h);
    } else {
      Product p = ProductDB.getInstance().get(h);
      return ((p != null) && (isMatch(p.getCountry()) ^ isNegation()));
    }
  }

  public Filter<Handle> clone() {
    return new CountryFilter(this);
  }
}
