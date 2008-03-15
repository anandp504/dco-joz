package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Generic implementation of a index that supports range lookups for Double values
 * @author: nipun
 * Date: Mar 6, 2008
 * Time: 9:17:47 AM
 */
public class RangeIndexImpl extends ProductAttributeIndex<Long, Handle> {

  private IProduct.Attribute type;

  public RangeIndexImpl(IProduct.Attribute attr) {
      this.type = attr;
  }

  public IProduct.Attribute getType() {
    return type;
  }

  public Long getKey(IProduct p) {
     Long retVal = (Long)p.getValue(type);
     return retVal;
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }

}
