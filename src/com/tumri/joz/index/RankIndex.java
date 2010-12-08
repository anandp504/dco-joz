package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: nipun
 */
public class RankIndex extends ProductAttributeIndex<Integer, Handle> {
  public RankIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kRank;
  }

  public Integer getKey(IProduct p) {
    return p.getRank();
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }
}