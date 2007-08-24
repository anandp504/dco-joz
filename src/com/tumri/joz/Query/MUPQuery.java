package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.ranks.AttributeWeights;
import com.tumri.joz.ranks.IWeight;

import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * Date: Aug 23, 2007
 * Time: 9:23:38 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class MUPQuery extends SimpleQuery {
  private IProduct.Attribute m_attribute;
  protected MUPQuery(IProduct.Attribute aAttribute) {
    super();
    m_attribute = aAttribute;
  }
  public IProduct.Attribute getAttribute() {
    return m_attribute;
  }
  public IWeight<Handle> getWeight() {
    return AttributeWeights.getWeight(m_attribute);
  }
  public boolean hasIndex() {
    return ProductDB.getInstance().hasIndex(m_attribute);
  }
  public SortedSet<Handle> getAll() {
    return ProductDB.getInstance().getAll();
  }
}
