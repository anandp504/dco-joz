package com.tumri.joz.filter;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.JOZTaxonomy;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CategoryFilter extends Filter<Handle> {
  public CategoryFilter() {
    super();
  }

  protected CategoryFilter(CategoryFilter f) {
    super(f);
  }

  public boolean accept(Handle h) {
    IProduct p = ProductDB.getInstance().get(h);
    JOZTaxonomy tax = JOZTaxonomy.getInstance();
    if (p != null) {
      Integer ancestor = p.getCategory();
      boolean match = false;
      while(!match && ancestor != null) {
        match = isMatch(ancestor);
        ancestor = tax.getParent(ancestor);
      }
      return (match ^ isNegation());
    }
    return false;
  }

  public Filter<Handle> clone() {
    return new CategoryFilter(this);
  }
}