package com.tumri.joz.index;

import com.tumri.joz.products.IProduct;

import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class DictionaryManager {
  private static DictionaryManager g_Instance = null;
  Hashtable<IProduct.Attribute, IDictionary> m_table = new Hashtable<IProduct.Attribute, IDictionary>();
  static {
    DictionaryManager dm = getInstance();
    dm.addType(IProduct.Attribute.kId);
    dm.addType(IProduct.Attribute.kCategory);
    dm.addType(IProduct.Attribute.kProvider);
    dm.addType(IProduct.Attribute.kRank);
    dm.addType(IProduct.Attribute.kSupplier);
    dm.addType(IProduct.Attribute.kBrand);
    dm.addType(IProduct.Attribute.kCatalog);
    dm.addType(IProduct.Attribute.kCurrency);
    dm.addType(IProduct.Attribute.kDiscountPriceCurrency);
    dm.addType(IProduct.Attribute.kBlackWhiteListStatus);
    dm.addType(IProduct.Attribute.kProductType);
  }

  public static final DictionaryManager getInstance() {
    if (g_Instance == null) {
      synchronized(DictionaryManager.class) {
        if (g_Instance == null) {
          g_Instance = new DictionaryManager();
        }
      }
    }
    return g_Instance;
  }

  protected final IDictionary getDictionary(IProduct.Attribute aAttribute) {
    if (m_table.containsKey(aAttribute)) {
      return m_table.get(aAttribute);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public final Integer getId(IProduct.Attribute aAttribute,Object obj) {
    IDictionary dict = getDictionary(aAttribute);
    if (dict != null) {
      // ??? This gets an "unchecked call" warning.
      return dict.getId(obj);
    }
    return -1;
  }

  public final Object getValue(IProduct.Attribute aAttribute, int index) {
    if (m_table.containsKey(aAttribute)) {
      IDictionary dict = m_table.get(aAttribute);
      dict.getValue(index);
    }
    return null;
  }

  public int maxId(IProduct.Attribute aAttribute) {
    if (m_table.containsKey(aAttribute)) {
      return m_table.get(aAttribute).maxId();
    }
    return 0;
  }


  public void addType(IProduct.Attribute aAttribute) {
    if (!m_table.containsKey(aAttribute))
      m_table.put(aAttribute,new Dictionary());
  }

  public void remove(IProduct.Attribute aAttribute, int index) {
    IDictionary dict = getDictionary(aAttribute);
    if (dict != null) {
      dict.remove(index);
    }
  }
}