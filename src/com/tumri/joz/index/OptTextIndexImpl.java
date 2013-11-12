package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * User: scbraun
 * Date: 10/2/13
 */
public class OptTextIndexImpl  extends ProductAttributeIndex<Integer, Handle> {

  private IProduct.Attribute type;

  public OptTextIndexImpl(IProduct.Attribute attr) {
      this.type = attr;
  }

  public IProduct.Attribute getType() {
    return type;
  }

  public Integer getKey(IProduct p) { //todo: not sure this is used any more..
     Integer retVal = (Integer)p.getValue(type);
     return retVal;
  }

  public Handle getValue(IProduct p) {
    return p.getHandle();
  }



}
