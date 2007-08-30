package com.tumri.joz.index;

import java.util.Hashtable;

import com.tumri.joz.products.IProduct;
import com.tumri.utils.dictionary.Dictionary;
import com.tumri.utils.dictionary.IDictionary;

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
    dm.addType(IProduct.Attribute.kId, new ProductDictionary());
    dm.addType(IProduct.Attribute.kCategory, new Dictionary());
    dm.addType(IProduct.Attribute.kProvider, new Dictionary());
    dm.addType(IProduct.Attribute.kRank, new Dictionary());
    dm.addType(IProduct.Attribute.kSupplier, new Dictionary());
    dm.addType(IProduct.Attribute.kBrand, new Dictionary());
    dm.addType(IProduct.Attribute.kCatalog, new Dictionary());
    dm.addType(IProduct.Attribute.kCurrency, new Dictionary());
    dm.addType(IProduct.Attribute.kDiscountPriceCurrency, new Dictionary());
    dm.addType(IProduct.Attribute.kBlackWhiteListStatus, new Dictionary());
    dm.addType(IProduct.Attribute.kProductType, new Dictionary());
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
      return dict.getValue(index);
    }
    return null;
  }

  public int maxId(IProduct.Attribute aAttribute) {
    if (m_table.containsKey(aAttribute)) {
      return m_table.get(aAttribute).maxId();
    }
    return 0;
  }
  public int minId(IProduct.Attribute aAttribute) {
    if (m_table.containsKey(aAttribute)) {
      return m_table.get(aAttribute).minId();
    }
    return 0;
  }


  public void addType(IProduct.Attribute aAttribute, IDictionary dict) {
    if (!m_table.containsKey(aAttribute))
      m_table.put(aAttribute,dict);
  }

  public void remove(IProduct.Attribute aAttribute, int index) {
    IDictionary dict = getDictionary(aAttribute);
    if (dict != null) {
      dict.remove(index);
    }
  }
}