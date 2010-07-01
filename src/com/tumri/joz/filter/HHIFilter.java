package com.tumri.joz.filter;

import com.tumri.content.data.Product;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.ProductHandle;

/**
 * User: nipun
 */
public class HHIFilter extends Filter<Handle> {
  public HHIFilter() {
    super();
  }

  @SuppressWarnings("unchecked")
  public HHIFilter(Filter<Handle> f) {
    // ??? This gets an "unchecked conversion" warning.
    super(f);
  }

  public boolean accept(Handle h) {
    ProductHandle ph = (ProductHandle)h;
      return (isMatch(ph.getHouseHoldIncome()) ^ isNegation());
  }

  public Filter<Handle> clone() {
    return new HHIFilter(this);
  }
}