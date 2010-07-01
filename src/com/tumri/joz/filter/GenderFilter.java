package com.tumri.joz.filter;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductHandle;

/**
 * Gender Filter Implementation
 * @author - nipun
 */
public class GenderFilter extends Filter<Handle> {

  public GenderFilter() {
    super();
  }

  public GenderFilter(GenderFilter f) {
    super(f);
  }

  public boolean accept(Handle h) {
      ProductHandle ph = (ProductHandle)h;
      return (isMatch(ph.getGender()) ^ isNegation());
  }

  public Filter<Handle> clone() {
    return new GenderFilter(this);
  }
}