package com.tumri.joz.filter;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductHandle;

/**
 * Rank Filter Implementation
 * @author - nipun
 */
public class RankFilter extends Filter<Handle> {

  public RankFilter() {
    super();
  }

  public RankFilter(RankFilter f) {
    super(f);
  }

  public boolean accept(Handle h) {
      ProductHandle ph = (ProductHandle)h;
      //Accept if rank is available
      return (ph.getRank()>0);
  }

  public Filter<Handle> clone() {
    return new RankFilter(this);
  }
}