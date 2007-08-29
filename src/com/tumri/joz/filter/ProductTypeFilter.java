package com.tumri.joz.filter;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;

/**
 * ProductType Filter Implementation
 * @author - nipun
 */
public class ProductTypeFilter extends Filter<Handle> {
	
  public ProductTypeFilter() {
    super();
  }

  public ProductTypeFilter(ProductTypeFilter f) {
    super(f);
  }

  public boolean accept(Handle h) {
    IProduct p = ProductDB.getInstance().get(h);
    return ((p != null) && (isMatch(p.getProductType()) ^ isNegation()));
  }

  public Filter<Handle> clone() {
    return new ProductTypeFilter(this);
  }
}