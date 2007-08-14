package com.tumri.joz.Query;

import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.products.Handle;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class ProductSetIntersector extends SetIntersector<Handle> {
  /**
   * Given a Value v and a score build the Pair object of PID,SCORE
   * This method should be overridden if default Pair<V,Double> is not acceptable
   * @param h
   * @param score
   * @return a Pair<Pid,Double>
   */
  public Handle getResult(Handle h, Double score) {
    return h.createHandle(score);
  }

  public ProductSetIntersector(Handle reference) {
    super();
    setReference(reference);
  }
}
