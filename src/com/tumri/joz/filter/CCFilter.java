package com.tumri.joz.filter;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductHandle;

/**
 * User: nipun
 */
public class CCFilter extends Filter<Handle> {
  public CCFilter() {
    super();
  }

  @SuppressWarnings("unchecked")
  public CCFilter(Filter<Handle> f) {
    // ??? This gets an "unchecked conversion" warning.
    super(f);
  }

  public boolean accept(Handle h) {
    ProductHandle ph = (ProductHandle)h;
      return (isMatch(ph.getChildCount()) ^ isNegation());
  }

  public Filter<Handle> clone() {
    return new CCFilter(this);
  }
}