package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class ProductSetIntersector extends SetIntersector<Handle> {
  /**
   * Given a Handle h and a score build a new handle
   * This method should be overridden if default Pair<V,Double> is not acceptable
   * @param h
   * @param score
   * @return a Pair<Pid,Double>
   */
  public Handle getResult(Handle h, Double score) {
    return h.createHandle(score);
  }

  public ProductSetIntersector(boolean strict) {
    super(strict);
  }

  private ProductSetIntersector(ProductSetIntersector set) {
    super(set);
  }

  public SetIntersector<Handle> clone() throws CloneNotSupportedException {
    return new ProductSetIntersector(this);
  }
}
