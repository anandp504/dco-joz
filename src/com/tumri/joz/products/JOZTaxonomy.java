package com.tumri.joz.products;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import com.tumri.content.data.Category;
import com.tumri.content.data.Taxonomy;
import com.tumri.joz.index.DictionaryManager;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class JOZTaxonomy {
  private static JOZTaxonomy g_taxonomy;

  public static JOZTaxonomy getInstance() {
    if (g_taxonomy == null) {
      synchronized(JOZTaxonomy.class) {
        if (g_taxonomy == null) {
          g_taxonomy = new JOZTaxonomy();
        }
      }
    }
    return g_taxonomy;
  }

  AtomicReference<com.tumri.content.data.Taxonomy> tax = new AtomicReference<com.tumri.content.data.Taxonomy>(); 

  private JOZTaxonomy() {
  }

  public void setTaxonomy(com.tumri.content.data.Taxonomy t) {
      tax.set(t);
  }
  
  public com.tumri.content.data.Taxonomy getTaxonomy() {
      return tax.get();
  }



}
