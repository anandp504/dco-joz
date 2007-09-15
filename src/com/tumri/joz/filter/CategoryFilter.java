package com.tumri.joz.filter;

import com.tumri.content.data.Taxonomy;
import com.tumri.content.data.Category;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.joz.products.ProductDB;

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
    Taxonomy t = tax.getTaxonomy();
    if (p != null) {
      Category ancestor=t.getCategory(p.getCategory());
      boolean match = false;
      while(!match && ancestor != null) {
        match = isMatch(ancestor.getGlassId());
        ancestor = ancestor.getParent();
      }
      return (match ^ isNegation());
    }
    return false;
  }

  public Filter<Handle> clone() {
    return new CategoryFilter(this);
  }
}