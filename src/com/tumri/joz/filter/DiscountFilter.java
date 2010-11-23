package com.tumri.joz.filter;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductHandle;

/**
 * Rank Filter Implementation
 * @author - nipun
 */
public class DiscountFilter extends Filter<Handle> {

  public DiscountFilter() {
    super();
  }

  public DiscountFilter(DiscountFilter f) {
    super(f);
  }

  public boolean accept(Handle h) {
      ProductHandle ph = (ProductHandle)h;
      //Accept if the discount is available.
      return (ph.getDiscount()>0);
  }

  public Filter<Handle> clone() {
    return new DiscountFilter(this);
  }
}