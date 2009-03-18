package com.tumri.joz.filter;

import com.tumri.content.data.Product;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.ProductHandle;

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
      ProductHandle ph = (ProductHandle)h;
      return (isMatch(ph.getProductType()) ^ isNegation());
  }

  public Filter<Handle> clone() {
    return new ProductTypeFilter(this);
  }
}