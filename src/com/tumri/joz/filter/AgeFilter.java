package com.tumri.joz.filter;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductHandle;

/**
 * Age Filter Implementation
 * @author - nipun
 */
public class AgeFilter extends Filter<Handle> {

  public AgeFilter() {
    super();
  }

  public AgeFilter(AgeFilter f) {
    super(f);
  }

  public boolean accept(Handle h) {
      ProductHandle ph = (ProductHandle)h;
      return (isMatch(ph.getAge()) ^ isNegation());
  }

  public Filter<Handle> clone() {
    return new AgeFilter(this);
  }
}