package com.tumri.joz.products;

import com.tumri.joz.index.*;
import com.tumri.joz.filter.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
// @todo add handle changes to take care of randomization
public class ProductDB { 
  private static ProductDB g_DB;
  // Map m_map maintains map from product id -> Product
  private RWLockedTreeMap<Integer, IProduct> m_map = new RWLockedTreeMap<Integer, IProduct>();
  // Map m_allproducts maintains set of all product handles
  private RWLockedTreeSet<Handle> m_allProducts = new RWLockedTreeSet<Handle>();
  // All indices are maintained in the class in a hashtable
  private Hashtable<IProduct.Attribute,Index<?,Handle>> m_indices = new Hashtable<IProduct.Attribute, Index<?,Handle>>();
  // table of all filters associated with attributes
  private Hashtable<IProduct.Attribute, Filter<Handle>> m_filters = new Hashtable<IProduct.Attribute, Filter<Handle>>();

  public static ProductDB getInstance() {
    if (g_DB == null) {
      synchronized(ProductDB.class) {
        if (g_DB == null) {
          g_DB = new ProductDB();
        }
      }
    }
    return g_DB;
  }

  static {
    ProductDB pdb = ProductDB.getInstance();

    pdb.addIndex(IProduct.Attribute.kCategory,new CategoryIndex());
    pdb.registerFilter(IProduct.Attribute.kCategory,new CategoryFilter());

    pdb.addIndex(IProduct.Attribute.kProvider,new ProviderIndex());
    pdb.registerFilter(IProduct.Attribute.kProvider,new ProviderFilter());

    pdb.addIndex(IProduct.Attribute.kSupplier,new SupplierIndex());
    pdb.registerFilter(IProduct.Attribute.kSupplier,new SupplierFilter());

    pdb.addIndex(IProduct.Attribute.kBrand,new BrandIndex());
    pdb.registerFilter(IProduct.Attribute.kBrand,new BrandFilter());

    pdb.addIndex(IProduct.Attribute.kCPC,new CPCIndex());
    pdb.registerFilter(IProduct.Attribute.kCPC,new CPCRangeFilter());

    pdb.addIndex(IProduct.Attribute.kCPO,new CPOIndex());
    pdb.registerFilter(IProduct.Attribute.kCPO,new CPORangeFilter());

    pdb.addIndex(IProduct.Attribute.kPrice,new PriceIndex());
    pdb.registerFilter(IProduct.Attribute.kPrice,new PriceRangeFilter());
  }

  private ProductDB() {
  }

  // Add sequence is follows:
  // Step 1. Dictionaries are updated as part of creating Product
  // Step 2. Add Handle to set of all products
  // Step 3. Add Id->IProduct mapping to m_map
  // Step 4. Update all indices in a sequence
  public Handle addProduct(IProduct p) {
    checkUpdate(p);

    Handle h = p.getHandle();
    // Step 2.
    m_allProducts.writerLock();
    try {
      m_allProducts.add(h);
    } finally {
      m_allProducts.writerUnlock();
    }
    // Step 3.
    m_map.writerLock();
    try {
      m_map.put(p.getId(),p);
    } finally {
      m_map.writerUnlock();
    }
    // Step 4.
    Iterator<Index<?,Handle>> iter = m_indices.values().iterator();
    while (iter.hasNext()) {
      Index<?,Handle> lIndex = iter.next();
      lIndex.addProduct(p);
    }
    return h;
  }

  public ArrayList<Handle> addProduct(ArrayList<IProduct> products) {
    checkUpdate(products);
    ArrayList<Handle> handles = new ArrayList<Handle>();
    for (int i = 0; i < products.size(); i++) {
      handles.add(products.get(i).getHandle());
    }
    // Step 2.
    m_allProducts.writerLock();
    try {
      m_allProducts.addAll(handles);
    } finally {
      m_allProducts.writerUnlock();
    }
    // Step 3.
    m_map.writerLock();
    try {
      for (int i = 0; i < products.size(); i++) {
        IProduct p = products.get(i);
        m_map.put(p.getId(),p);
      }
    } finally {
      m_map.writerUnlock();
    }
    // Step 4.
    Iterator<Index<?,Handle>> iter = m_indices.values().iterator();
    while (iter.hasNext()) {
      Index<?,Handle> lIndex = iter.next();
      lIndex.addProduct(products);
    }
    return handles;
  }

  private void checkUpdate(IProduct p) {
    IProduct op = null;
    try {
      m_map.readerLock();
      op = m_map.get(p.getId());
    } finally {
      m_map.readerUnlock();
    }
    if (op != null) {
      deleteProduct(op);
    }
  }
  private void checkUpdate(ArrayList<IProduct> products) {
    ArrayList<IProduct> ops = new ArrayList<IProduct>();
    try {
      m_map.readerLock();
      for (int i = 0; i < products.size(); i++) {
        IProduct p = products.get(i);
        IProduct op = m_map.get(p.getId());
        if (op != null)
          ops.add(op);
      }
    } finally {
      m_map.readerUnlock();
    }
    if (ops.size() > 0) {
      deleteProduct(ops);
    }
  }

  /**
   * Delete sequence is as follows:
   * step 1. Remove from all indices in a sequence
   * step 2. Remove from m_map
   * Step 3. remove from allProducts set
   * Step 4. remove from kId dictionary
   * @param p
   * @return
   */
  public Handle deleteProduct(IProduct p) {
    Handle h = p.getHandle();
    // Step 1.
    Iterator<Index<?,Handle>> iter = m_indices.values().iterator();
    while (iter.hasNext()) {
      Index<?,Handle> lIndex = iter.next();
      lIndex.deleteProduct(p);
    }
    // Step 2.
    m_map.writerLock();
    try {
      m_map.remove(p.getId());
    } finally {
      m_map.writerUnlock();
    }
    // Step 3.
    m_allProducts.writerLock();
    try {
      m_allProducts.remove(h);
    } finally {
      m_allProducts.writerUnlock();
    }
    // step 4
    DictionaryManager.getInstance().remove(IProduct.Attribute.kId,h.getOid());
    return h;
  }

  public ArrayList<Handle> deleteProduct(ArrayList<IProduct> products) {
    ArrayList<Handle> handles = new ArrayList<Handle>();
    for (int i = 0; i < products.size(); i++) {
      handles.add(products.get(i).getHandle());
    }
    Iterator<Index<?,Handle>> iter = m_indices.values().iterator();
    while (iter.hasNext()) {
      Index<?,Handle> lIndex = iter.next();
      lIndex.deleteProduct(products);
    }
    // Step 2.
    m_map.writerLock();
    try {
      for (int i = 0; i < products.size(); i++) {
        IProduct p = products.get(i);
        m_map.remove(p.getId());
      }
    } finally {
      m_map.writerUnlock();
    }
    // Step 3.
    m_allProducts.writerLock();
    try {
      for (int i = 0; i < handles.size(); i++) {
        Handle h = handles.get(i);
        m_allProducts.remove(h);
      }
    } finally {
      m_allProducts.writerUnlock();
    }
    // step 4
    for (int i = 0; i < handles.size(); i++) {
      Handle h = handles.get(i);
      DictionaryManager.getInstance().remove(IProduct.Attribute.kId,h.getOid());
    }
    return handles;
  }


  public IProduct get(Handle handle) {
    return get(handle.getOid());
  }

  public Handle get(IProduct p) {
    return p.getHandle();
  }

  public IProduct get(int id) {
    m_map.readerLock();
    try {
      return m_map.get(id);
    } finally {
      m_map.readerUnlock();
    }
  }

  public SortedSet<Handle> getAll() {
    return m_allProducts;
  }

  public void addIndex(IProduct.Attribute aAttribute, Index<?,Handle> index) {
    m_indices.put(aAttribute,index);
    reindex(aAttribute);
  }

  public void deleteIndex(IProduct.Attribute aAttribute) {
    m_indices.remove(aAttribute);
  }

  public void reindex(IProduct.Attribute aAttribute) {
    Index index = m_indices.get(aAttribute);
    if (index != null) {
      index.clear();
      try {
        m_map.readerLock();
        Iterator<IProduct> iter = m_map.values().iterator();
        while (iter.hasNext()) {
          IProduct lIProduct = iter.next();
          index.addProduct(lIProduct);
        }
      } finally {
        m_map.readerUnlock();
      }
    }
  }

  public Index getIndex(IProduct.Attribute aAttribute) {
    return m_indices.get(aAttribute);
  }

  public boolean hasIndex(IProduct.Attribute aAttribute) {
    return m_indices.containsKey(aAttribute);
  }

  public void registerFilter(IProduct.Attribute aAttribute, Filter<Handle> filter) {
    m_filters.put(aAttribute,filter);
  }

  public Filter<Handle> getFilter(IProduct.Attribute aAttribute) {
    Filter<Handle> filter = m_filters.get(aAttribute);
    return ((filter != null) ? filter.clone() : filter);
  }

  /**
   * Changes the handle for a given product. This causes the indices to be rearranged
   * @param h
   * @return
   */
  public Handle changeHandle(Handle h) {
    // @todo implement this
    IProduct p = get(h);
    if ( p == null)
      return null;
    Iterator<Index<?,Handle>> iter = m_indices.values().iterator();
    while (iter.hasNext()) {
      Index<?,Handle> lIndex = iter.next();
      lIndex.deleteProduct(p);
    }
    iter = m_indices.values().iterator();
    while (iter.hasNext()) {
      Index lIndex = iter.next();
      lIndex.addProduct(p);
    }
    return h;
  }

}
