package com.tumri.joz.products;

import com.tumri.content.ContentProviderFactory;
import com.tumri.content.InvalidConfigException;
import com.tumri.content.ProductProvider;
import com.tumri.content.data.CategoryAttributeDetails;
import com.tumri.content.data.Product;
import com.tumri.joz.filter.*;
import com.tumri.joz.index.*;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.IndexUtils;
import com.tumri.utils.data.RWLockedSortedArraySet;
import com.tumri.utils.data.RWLockedTreeMap;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class ProductDB {
  static Logger log = Logger.getLogger(ProductDB.class);

  private static ProductDB g_DB;
  // Map m_map maintains map from product id -> Product
  private RWLockedTreeMap<Long, IProduct> m_map = new RWLockedTreeMap<Long, IProduct>();
  // Map m_allproducts maintains set of all product handles
  private RWLockedSortedArraySet<Handle> m_allProducts = new RWLockedSortedArraySet<Handle>();
  // All indices are maintained in the class in a hashtable
  private Hashtable<IProduct.Attribute, ProductAttributeIndex<?, Handle>> m_indices = new Hashtable<IProduct.Attribute, ProductAttributeIndex<?, Handle>>();
  // table of all filters associated with attributes
  private Hashtable<IProduct.Attribute, Filter<Handle>> m_filters = new Hashtable<IProduct.Attribute, Filter<Handle>>();

  // table of all long filters associated with attributes
  private Hashtable<IProduct.Attribute, LongFilter<Handle>> m_longFilters = new Hashtable<IProduct.Attribute, LongFilter<Handle>>();

  private ProductProvider m_productProvider = null;
  private static Random g_random = new Random(System.currentTimeMillis());

  private ArrayList<Handle> m_newProducts = new ArrayList<Handle>();
  private boolean disableJozIndexLoad = false;

  public static ProductDB getInstance() {
    if (g_DB == null) {
      synchronized (ProductDB.class) {
        if (g_DB == null) {
          g_DB = new ProductDB();
        }
      }
    }
    return g_DB;
  }

  static {
    ProductDB pdb = ProductDB.getInstance();

    pdb.addIndex(IProduct.Attribute.kCategory, new CategoryIndex());
    pdb.registerFilter(IProduct.Attribute.kCategory, new CategoryFilter());

    pdb.addIndex(IProduct.Attribute.kProvider, new ProviderIndex());
    pdb.registerFilter(IProduct.Attribute.kProvider, new ProviderFilter());

    pdb.addIndex(IProduct.Attribute.kSupplier, new SupplierIndex());
    pdb.registerFilter(IProduct.Attribute.kSupplier, new SupplierFilter());

    pdb.addIndex(IProduct.Attribute.kBrand, new BrandIndex());
    pdb.registerFilter(IProduct.Attribute.kBrand, new BrandFilter());

    pdb.addIndex(IProduct.Attribute.kCPC, new CPCIndex());
    pdb.registerFilter(IProduct.Attribute.kCPC, new CPCRangeFilter());

    pdb.addIndex(IProduct.Attribute.kCPO, new CPOIndex());
    pdb.registerFilter(IProduct.Attribute.kCPO, new CPORangeFilter());

    pdb.addIndex(IProduct.Attribute.kPrice, new PriceIndex());
    pdb.registerFilter(IProduct.Attribute.kPrice, new PriceRangeFilter());

    pdb.addIndex(IProduct.Attribute.kProductType, new ProductTypeIndex());
    pdb.registerFilter(IProduct.Attribute.kProductType, new ProductTypeFilter());

    pdb.addIndex(IProduct.Attribute.kImageWidth, new ImageWidthIndex());
    pdb.registerFilter(IProduct.Attribute.kImageWidth, new ImageWidthFilter());

    pdb.addIndex(IProduct.Attribute.kImageHeight, new ImageHeightIndex());
    pdb.registerFilter(IProduct.Attribute.kImageHeight, new ImageHeightFilter());

    pdb.addIndex(IProduct.Attribute.kCountry, new CountryIndex());
    pdb.registerFilter(IProduct.Attribute.kCountry, new CountryFilter());

    pdb.addIndex(IProduct.Attribute.kState, new StateIndex());
    pdb.registerFilter(IProduct.Attribute.kState, new StateFilter());

    pdb.addIndex(IProduct.Attribute.kCity, new CityIndex());
    pdb.registerFilter(IProduct.Attribute.kCity, new CityFilter());

    pdb.addIndex(IProduct.Attribute.kZip, new ZipCodeIndex());
    pdb.registerFilter(IProduct.Attribute.kZip, new ZipCodeFilter());

    pdb.addIndex(IProduct.Attribute.kDMA, new DmaCodeIndex());
    pdb.registerFilter(IProduct.Attribute.kDMA, new DmaCodeFilter());

    pdb.addIndex(IProduct.Attribute.kArea, new AreaCodeIndex());
    pdb.registerFilter(IProduct.Attribute.kArea, new AreaCodeFilter());

    pdb.addIndex(IProduct.Attribute.kGlobalId, new GlobalIdIndex());
    pdb.registerFilter(IProduct.Attribute.kGlobalId, new GlobalIdFilter());

    pdb.addIndex(IProduct.Attribute.kCategoryTextField, new TextIndexImpl(IProduct.Attribute.kCategoryTextField));
    pdb.registerLongFilter(IProduct.Attribute.kCategoryTextField, new TextFilterImpl(IProduct.Attribute.kCategoryTextField));

    pdb.addIndex(IProduct.Attribute.kCategoryNumericField, new RangeIndexImpl(IProduct.Attribute.kCategoryNumericField));
    pdb.registerLongFilter(IProduct.Attribute.kCategoryNumericField, new RangeFilterImpl(IProduct.Attribute.kCategoryNumericField));

  }

  private ProductDB() {
    try {
       disableJozIndexLoad = Boolean.parseBoolean(AppProperties.getInstance().getProperty("com.tumri.content.file.disableJozIndex"));
    } catch (Exception e) {
       disableJozIndexLoad = false;
    }
  }

  /**
   * Returns true if the ProductDB instance keeps local copy of Product Information
   *
   * @return true if the DB has product Information
   */
  public static boolean hasProductInfo() {
    return false;
  }

  // Add sequence is follows:
  // Step 1. Dictionaries are updated as part of creating Product
  // Step 2. Add Handle to set of all products
  // Step 3. Add Id->IProduct mapping to m_map
  // Step 4. Update all indices in a sequence
  public ArrayList<Handle> addProduct(ArrayList<IProduct> products) {
    if (disableJozIndexLoad) {
        checkUpdate(products);
    }
    ArrayList<Handle> handles = new ArrayList<Handle>();
    for (IProduct product : products) {
      handles.add(product.getHandle());
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
      for (IProduct p : products) {
        m_map.put(p.getId(), p);
      }
    } finally {
      m_map.writerUnlock();
    }
    // Step 4.
    if (disableJozIndexLoad) {
        buildMap(products);
    }
    return handles;
  }

  private void checkUpdate(ArrayList<IProduct> products) {
    ArrayList<IProduct> ops = new ArrayList<IProduct>();
    try {
      m_map.readerLock();
      for (IProduct p : products) {
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
   * @param products
   * @return
   */
  public ArrayList<Handle> deleteProduct(ArrayList<IProduct> products) {
    ArrayList<Handle> handles = new ArrayList<Handle>();
      if (!disableJozIndexLoad) {
          return handles;
      }

    for (IProduct product : products) {
      handles.add(product.getHandle());
    }
    for (ProductAttributeIndex<?, Handle> lIndex : m_indices.values()) {
      lIndex.delete(products);
    }
    // Step 2.
    m_map.writerLock();
    try {
      for (IProduct p : products) {
        m_map.remove(p.getId());
      }
    } finally {
      m_map.writerUnlock();
    }
    // Step 3.
    m_allProducts.writerLock();
    try {
      m_allProducts.removeAll(handles);
    } finally {
      m_allProducts.writerUnlock();
    }
    return handles;
  }


  public IProduct get(Handle handle) {
    //return (handle instanceof ProductHandle) ? ((ProductHandle)handle).getProduct() : get(handle.getOid());
    return get(handle.getOid());
  }

  public Handle get(IProduct p) {
    return p.getHandle();
  }

  public IProduct get(long id) {
    m_map.readerLock();
    try {
      return m_map.get(id);
    } finally {
      m_map.readerUnlock();
    }
  }

  /**
   * Get IProduct without checking a lock, reader should call readerLock()
   * @param id
   * @return IProduct
   */
  public IProduct getInt(long id) {
    return m_map.get(id);
  }

  public void readerLock() {
    m_map.readerLock();
  }

  public void readerUnlock() {
    m_map.readerUnlock();
  }

  public SortedSet<Handle> getAll() {
    return m_allProducts;
  }

  public void addIndex(IProduct.Attribute aAttribute, ProductAttributeIndex<?, Handle> index) {
    m_indices.put(aAttribute, index);
  }

  public void deleteIndex(IProduct.Attribute aAttribute) {
    m_indices.remove(aAttribute);
  }

  public ProductAttributeIndex getIndex(IProduct.Attribute aAttribute) {
    return m_indices.get(aAttribute);
  }

  public boolean hasIndex(IProduct.Attribute aAttribute) {
    return m_indices.containsKey(aAttribute);
  }

  public void registerFilter(IProduct.Attribute aAttribute, Filter<Handle> filter) {
    m_filters.put(aAttribute, filter);
  }

  public void registerLongFilter(IProduct.Attribute aAttribute, LongFilter<Handle> filter) {
    m_longFilters.put(aAttribute, filter);
  }

  public Filter<Handle> getFilter(IProduct.Attribute aAttribute) {
    Filter<Handle> filter = m_filters.get(aAttribute);
    return ((filter != null) ? filter.clone() : filter);
  }

  public LongFilter<Handle> getLongFilter(IProduct.Attribute aAttribute) {
    LongFilter<Handle> filter = m_longFilters.get(aAttribute);
    return ((filter != null) ? filter.clone() : filter);
  }

  public Handle genReference() {
    m_allProducts.readerLock();
    try {
      return m_allProducts.random(g_random);
    } finally {
      m_allProducts.readerUnlock();
    }
  }

  private ProductProvider getProductProvider() {
    try {
      if (m_productProvider == null) {
        ProductProvider pp = ContentProviderFactory.getInstance().getContentProvider().getContent().getProducts();
        m_productProvider = pp;
      }
    } catch (InvalidConfigException e) {
      log.error("Could not get Product Content Provider", e);
    }
    return m_productProvider;
  }

  @SuppressWarnings("unchecked")
  private void buildMap(ArrayList<IProduct> products) {
    TreeMap<Integer, ArrayList<Handle>> mprovider = new TreeMap<Integer, ArrayList<Handle>>();
    TreeMap<Integer, ArrayList<Handle>> msupplier = new TreeMap<Integer, ArrayList<Handle>>();
    TreeMap<Integer, ArrayList<Handle>> mcategory = new TreeMap<Integer, ArrayList<Handle>>();
    TreeMap<Integer, ArrayList<Handle>> mbrand = new TreeMap<Integer, ArrayList<Handle>>();
    TreeMap<Double, ArrayList<Handle>> mprice = new TreeMap<Double, ArrayList<Handle>>();
    TreeMap<Double, ArrayList<Handle>> mcpc = new TreeMap<Double, ArrayList<Handle>>();
    TreeMap<Double, ArrayList<Handle>> mcpo = new TreeMap<Double, ArrayList<Handle>>();
    TreeMap<Integer, ArrayList<Handle>> mptype = new TreeMap<Integer, ArrayList<Handle>>();
    TreeMap<Integer, ArrayList<Handle>> miheight = new TreeMap<Integer, ArrayList<Handle>>();
    TreeMap<Integer, ArrayList<Handle>> miwidth = new TreeMap<Integer, ArrayList<Handle>>();
    TreeMap<Integer, ArrayList<Handle>> mcountry = new TreeMap<Integer, ArrayList<Handle>>();
    TreeMap<Integer, ArrayList<Handle>> mstate= new TreeMap<Integer, ArrayList<Handle>>();
    TreeMap<Integer, ArrayList<Handle>> mcity = new TreeMap<Integer, ArrayList<Handle>>();
    TreeMap<Integer, ArrayList<Handle>> mzip = new TreeMap<Integer, ArrayList<Handle>>();
    TreeMap<Integer, ArrayList<Handle>> mdma = new TreeMap<Integer, ArrayList<Handle>>();
    TreeMap<Integer, ArrayList<Handle>> marea = new TreeMap<Integer, ArrayList<Handle>>();
    TreeMap<Integer, ArrayList<Handle>> mprovcategory = new TreeMap<Integer, ArrayList<Handle>>();
    TreeMap<Integer, ArrayList<Handle>> mglobalid = new TreeMap<Integer, ArrayList<Handle>>();
    TreeMap<Long, ArrayList<Handle>> mcategorytextattr = new TreeMap<Long, ArrayList<Handle>>();
    TreeMap<Long, ArrayList<Handle>> mcategorynumattr = new TreeMap<Long, ArrayList<Handle>>();

    for (IProduct prod : products) {
      Handle h = prod.getHandle();
      Product p = ((ProductWrapper) prod).getProduct();
      {
        Integer k = p.getProvider();
        ArrayList<Handle> list = mprovider.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          mprovider.put(k, list);
        }
        list.add(h);
      }
      {
        Integer k = p.getSupplier();
        ArrayList<Handle> list = msupplier.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          msupplier.put(k, list);
        }
        list.add(h);
      }
      {
        Integer k = p.getCategory();
        ArrayList<Handle> list = mcategory.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          mcategory.put(k, list);
        }
        list.add(h);
      }
      {
        Integer k = p.getBrand();
        ArrayList<Handle> list = mbrand.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          mbrand.put(k, list);
        }
        list.add(h);
      }
      {
        Double k = p.getPrice();
        ArrayList<Handle> list = mprice.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          mprice.put(k, list);
        }
        list.add(h);
      }
      {
        Double k = p.getCPC();
        ArrayList<Handle> list = mcpc.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          mcpc.put(k, list);
        }
        list.add(h);
      }
      {
        Double k = p.getCPO();
        ArrayList<Handle> list = mcpo.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          mcpo.put(k, list);
        }
        list.add(h);
      }
      {
        Integer k = p.getProductType();
        ArrayList<Handle> list = mptype.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          mptype.put(k, list);
        }
        list.add(h);
      }
      {
        Integer k = p.getImageHeight();
        ArrayList<Handle> list = miheight.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          miheight.put(k, list);
        }
        list.add(h);
      }
      {
        Integer k = p.getImageWidth();
        ArrayList<Handle> list = miwidth.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          miwidth.put(k, list);
        }
        list.add(h);
      }
      {
        Integer k = p.getCountry();
        ArrayList<Handle> list = mcountry.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          mcountry.put(k, list);
        }
        list.add(h);
      }
      {
        Integer k = p.getState();
        ArrayList<Handle> list = mstate.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          mstate.put(k, list);
        }
        list.add(h);
      }
      {
        Integer k = p.getCity();
        ArrayList<Handle> list = mcity.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          mcity.put(k, list);
        }
        list.add(h);
      }
      {
        Integer k = p.getZip();
        ArrayList<Handle> list = mzip.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          mzip.put(k, list);
        }
        list.add(h);
      }
      {
        Integer k = p.getDmaCode();
        ArrayList<Handle> list = mdma.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          mdma.put(k, list);
        }
        list.add(h);
      }
      {
        Integer k = p.getAreaCode();
        ArrayList<Handle> list = marea.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          marea.put(k, list);
        }
        list.add(h);
      }
      {
        Integer k = p.getProviderCategory();
        ArrayList<Handle> list = mprovcategory.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          mprovcategory.put(k, list);
        }
        list.add(h);
      }
      {
        Integer k = p.getGlobalId();
        ArrayList<Handle> list = mglobalid.get(k);
        if (list == null) {
          list = new ArrayList<Handle>();
          mglobalid.put(k, list);
        }
        list.add(h);
      }
      {
        Integer k = p.getCategoryField1();
        Integer c = p.getCategory();
        long key = IndexUtils.createIndexKeyForCategory(c,IProduct.Attribute.kCategoryField1,k);
        CategoryAttributeDetails details = IndexUtils.getDetailsForCategoryField(c, IProduct.Attribute.kCategoryField1);
        CategoryAttributeDetails.DataType type = details.getFieldtype();
        if (type != null) {
            if (type == CategoryAttributeDetails.DataType.kText) {
                ArrayList<Handle> list = mcategorytextattr.get(k);
                if (list == null) {
                  list = new ArrayList<Handle>();
                  mcategorytextattr.put(key, list);
                }
                list.add(h);
            }  else {
                ArrayList<Handle> list = mcategorynumattr.get(k);
                if (list == null) {
                  list = new ArrayList<Handle>();
                  mcategorynumattr.put(key, list);
                }
                list.add(h);
            }
        }
      }
      {
        Integer k = p.getCategoryField2();
        Integer c = p.getCategory();
        long key = IndexUtils.createIndexKeyForCategory(c,IProduct.Attribute.kCategoryField2,k);
        CategoryAttributeDetails details = IndexUtils.getDetailsForCategoryField(c, IProduct.Attribute.kCategoryField2);
        CategoryAttributeDetails.DataType type = details.getFieldtype();

        if (type != null) {
            if (type == CategoryAttributeDetails.DataType.kText) {
                ArrayList<Handle> list = mcategorytextattr.get(k);
                if (list == null) {
                  list = new ArrayList<Handle>();
                  mcategorytextattr.put(key, list);
                }
                list.add(h);
            }  else {
                ArrayList<Handle> list = mcategorynumattr.get(k);
                if (list == null) {
                  list = new ArrayList<Handle>();
                  mcategorynumattr.put(key, list);
                }
                list.add(h);
            }
        }
      }
      {
        Integer k = p.getCategoryField3();
        Integer c = p.getCategory();
        long key = IndexUtils.createIndexKeyForCategory(c,IProduct.Attribute.kCategoryField3,k);
        CategoryAttributeDetails details = IndexUtils.getDetailsForCategoryField(c, IProduct.Attribute.kCategoryField3);
        CategoryAttributeDetails.DataType type = details.getFieldtype();
        if (type != null) {
            if (type == CategoryAttributeDetails.DataType.kText) {
                ArrayList<Handle> list = mcategorytextattr.get(k);
                if (list == null) {
                  list = new ArrayList<Handle>();
                  mcategorytextattr.put(key, list);
                }
                list.add(h);
            }  else {
                ArrayList<Handle> list = mcategorynumattr.get(k);
                if (list == null) {
                  list = new ArrayList<Handle>();
                  mcategorynumattr.put(key, list);
                }
                list.add(h);
            }
        }
      }
      {
        Integer k = p.getCategoryField4();
        Integer c = p.getCategory();
        long key = IndexUtils.createIndexKeyForCategory(c,IProduct.Attribute.kCategoryField4,k);
        CategoryAttributeDetails details = IndexUtils.getDetailsForCategoryField(c, IProduct.Attribute.kCategoryField4);
        CategoryAttributeDetails.DataType type = details.getFieldtype();
        if (type != null) {
            if (type == CategoryAttributeDetails.DataType.kText) {
                ArrayList<Handle> list = mcategorytextattr.get(k);
                if (list == null) {
                  list = new ArrayList<Handle>();
                  mcategorytextattr.put(key, list);
                }
                list.add(h);
            }  else {
                ArrayList<Handle> list = mcategorynumattr.get(k);
                if (list == null) {
                  list = new ArrayList<Handle>();
                  mcategorynumattr.put(key, list);
                }
                list.add(h);
            }
        }
      }
      {
        Integer k = p.getCategoryField5();
        Integer c = p.getCategory();
        long key = IndexUtils.createIndexKeyForCategory(c,IProduct.Attribute.kCategoryField5,k);
        CategoryAttributeDetails details = IndexUtils.getDetailsForCategoryField(c, IProduct.Attribute.kCategoryField5);
        CategoryAttributeDetails.DataType type = details.getFieldtype();
        if (type != null) {
            if (type == CategoryAttributeDetails.DataType.kText) {
                ArrayList<Handle> list = mcategorytextattr.get(k);
                if (list == null) {
                  list = new ArrayList<Handle>();
                  mcategorytextattr.put(key, list);
                }
                list.add(h);
            }  else {
                ArrayList<Handle> list = mcategorynumattr.get(k);
                if (list == null) {
                  list = new ArrayList<Handle>();
                  mcategorynumattr.put(key, list);
                }
                list.add(h);
            }
        }
      }
    }
    {
      updateIntegerIndex(IProduct.Attribute.kProvider, mprovider);
      updateIntegerIndex(IProduct.Attribute.kSupplier, msupplier);
      updateIntegerIndex(IProduct.Attribute.kCategory, mcategory);
      updateIntegerIndex(IProduct.Attribute.kBrand, mbrand);

      updateDoubleIndex(IProduct.Attribute.kPrice, mprice);
      updateDoubleIndex(IProduct.Attribute.kCPC, mcpc);
      updateDoubleIndex(IProduct.Attribute.kCPO, mcpo);

      updateIntegerIndex(IProduct.Attribute.kProductType, mprovider);
      updateIntegerIndex(IProduct.Attribute.kImageWidth, miwidth);
      updateIntegerIndex(IProduct.Attribute.kImageHeight, miheight);

      updateIntegerIndex(IProduct.Attribute.kCountry, mcountry);
      updateIntegerIndex(IProduct.Attribute.kState, mstate);
      updateIntegerIndex(IProduct.Attribute.kCity, mcity);
      updateIntegerIndex(IProduct.Attribute.kZip, mzip);
      updateIntegerIndex(IProduct.Attribute.kDMA, mdma);
      updateIntegerIndex(IProduct.Attribute.kArea, marea);
      updateIntegerIndex(IProduct.Attribute.kProviderCategory, mprovcategory);
      updateIntegerIndex(IProduct.Attribute.kGlobalId, mglobalid);

      updateLongIndex(IProduct.Attribute.kCategoryTextField, mcategorytextattr);
      updateLongIndex(IProduct.Attribute.kCategoryNumericField, mcategorynumattr);

    }
  }

  @SuppressWarnings("unchecked")
  public void updateIntegerIndex(IProduct.Attribute type, TreeMap<Integer, ArrayList<Handle>> mindex) {
     ((ProductAttributeIndex<Integer, Handle>) m_indices.get(type)).add(mindex); 
  }

  @SuppressWarnings("unchecked")
  public void updateDoubleIndex(IProduct.Attribute type, TreeMap<Double, ArrayList<Handle>> mindex) {
     ((ProductAttributeIndex<Double, Handle>) m_indices.get(type)).add(mindex);
  }

  @SuppressWarnings("unchecked")
  public void updateLongIndex(IProduct.Attribute type, TreeMap<Long, ArrayList<Handle>> mindex) {
     ((ProductAttributeIndex<Long, Handle>) m_indices.get(type)).add(mindex);
  }

  @SuppressWarnings("unchecked")
  public void deleteIntegerIndex(IProduct.Attribute type, TreeMap<Integer, ArrayList<Handle>> mindex) {
     ((ProductAttributeIndex<Integer, Handle>) m_indices.get(type)).delete(mindex);
  }

  @SuppressWarnings("unchecked")
  public void deleteDoubleIndex(IProduct.Attribute type, TreeMap<Double, ArrayList<Handle>> mindex) {
     ((ProductAttributeIndex<Double, Handle>) m_indices.get(type)).delete(mindex);
  }

  @SuppressWarnings("unchecked")
  public void deleteLongIndex(IProduct.Attribute type, TreeMap<Long, ArrayList<Handle>> mindex) {
     ((ProductAttributeIndex<Long, Handle>) m_indices.get(type)).delete(mindex);
  }


    /**
     * Return a handle given a product id.
      * @param pid
     * @return
     */
  public Handle getHandle(Long pid) {
   //Check if the prod exists - else create and return handle.
   ProductHandle p = new ProductHandle(0.0, pid.longValue());
   Handle ph = m_allProducts.find(p);
   if (ph !=null) {
        p = (ProductHandle) ph;
   } else {
       //Create a new product
       m_newProducts.add(p);
   }
    return p;
  }

    
  public boolean isEmpty() {
    return m_allProducts.isEmpty();  
  }

    /**
     * Add the new products into the database.
      */
  public void addNewProducts() {
    if (!m_newProducts.isEmpty()) {
      log.debug("Adding : " + m_newProducts.size() + " new products to the Product DB");
      m_allProducts.addAll(m_newProducts);
      m_newProducts.clear();
    }
  }

    /**
     * Clears the indices and the maps
      */
  public void clearProductDB() {
      for (ProductAttributeIndex<?, Handle> lIndex : m_indices.values()) {
        lIndex.clear();
      }
       m_allProducts.clear();
       m_map.clear();
  }
}
