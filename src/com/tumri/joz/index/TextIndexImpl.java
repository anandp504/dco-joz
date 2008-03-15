package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Implentation for a integer based index lookup without support for Range
 * @author: nipun
 * Date: Mar 6, 2008
 * Time: 9:17:47 AM
 */
public class TextIndexImpl extends ProductAttributeIndex<Long, Handle> {

  private IProduct.Attribute type;

  public TextIndexImpl(IProduct.Attribute attr) {
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
