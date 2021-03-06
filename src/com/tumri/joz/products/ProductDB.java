package com.tumri.joz.products;

import com.tumri.content.ContentProviderFactory;
import com.tumri.content.InvalidConfigException;
import com.tumri.content.ProductProvider;
import com.tumri.content.data.Product;
import com.tumri.joz.filter.*;
import com.tumri.joz.index.*;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.IndexUtils;
import com.tumri.joz.utils.ZipCodeDB;
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

	private RWLockedTreeMap<Integer, Hashtable<IProduct.Attribute, ProductAttributeIndex<?, Handle>>> m_opt_indices = new RWLockedTreeMap<Integer, Hashtable<IProduct.Attribute, ProductAttributeIndex<?, Handle>>>();

	// table of all long filters associated with attributes
	private Hashtable<IProduct.Attribute, LongFilter<Handle>> m_longFilters = new Hashtable<IProduct.Attribute, LongFilter<Handle>>();

	private static Random g_random = new Random(System.currentTimeMillis());

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

//    pdb.addIndex(IProduct.Attribute.kProductType, new ProductTypeIndex());
		pdb.registerFilter(IProduct.Attribute.kProductType, new ProductTypeFilter());
		pdb.registerFilter(IProduct.Attribute.kAge, new AgeFilter());
		pdb.registerFilter(IProduct.Attribute.kGender, new GenderFilter());

//    pdb.addIndex(IProduct.Attribute.kImageWidth, new ImageWidthIndex());
//    pdb.registerFilter(IProduct.Attribute.kImageWidth, new ImageWidthFilter());

//    pdb.addIndex(IProduct.Attribute.kImageHeight, new ImageHeightIndex());
//    pdb.registerFilter(IProduct.Attribute.kImageHeight, new ImageHeightFilter());

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

		pdb.addIndex(IProduct.Attribute.kGeoEnabledFlag, new GeoEnabledIndex());
		pdb.registerFilter(IProduct.Attribute.kGeoEnabledFlag, new GeoEnabledFilter());

		pdb.addIndex(IProduct.Attribute.kGlobalId, new GlobalIdIndex());
		pdb.registerFilter(IProduct.Attribute.kGlobalId, new GlobalIdFilter());

		pdb.addIndex(IProduct.Attribute.kBT, new BTIndex());
		pdb.registerFilter(IProduct.Attribute.kBT, new BTFilter());

		// pdb.addIndex(IProduct.Attribute.kMS, new MSIndex());
		pdb.registerFilter(IProduct.Attribute.kCC, new CCFilter());

		// pdb.addIndex(IProduct.Attribute.kHHI, new HHIIndex());
		pdb.registerFilter(IProduct.Attribute.kHHI, new HHIFilter());
		pdb.addIndex(IProduct.Attribute.kRank, new RankIndex());
		pdb.addIndex(IProduct.Attribute.kDiscount, new DiscountIndex());

		pdb.registerFilter(IProduct.Attribute.kRank, new RankFilter());
		pdb.registerFilter(IProduct.Attribute.kDiscount, new DiscountFilter());

		pdb.addIndex(IProduct.Attribute.kMultiValueTextField, new TextIndexImpl(IProduct.Attribute.kMultiValueTextField));
		pdb.registerLongFilter(IProduct.Attribute.kMultiValueTextField, new TextFilterImpl(IProduct.Attribute.kMultiValueTextField));

		pdb.addIndex(IProduct.Attribute.kLatitude, new LatitudeIndex());
		pdb.registerFilter(IProduct.Attribute.kLatitude, new LatitudeRangeFilter());

		pdb.addIndex(IProduct.Attribute.kLongitude, new LongitudeIndex());
		pdb.registerFilter(IProduct.Attribute.kLongitude, new LongitudeRangeFilter());

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
	public ArrayList<Handle> deleteProduct(ArrayList<IProduct> products) {  //todo: do we need to add removal of product from opt_indexes?
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

	public void addOptIndex(IProduct.Attribute aAttribute, ProductAttributeIndex<?, Handle> index) {
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
		TreeMap<Integer, ArrayList<Handle>> mgeoenabled = new TreeMap<Integer, ArrayList<Handle>>();
		TreeMap<Integer, ArrayList<Handle>> mprovcategory = new TreeMap<Integer, ArrayList<Handle>>();
		TreeMap<Integer, ArrayList<Handle>> mglobalid = new TreeMap<Integer, ArrayList<Handle>>();
		TreeMap<Integer, ArrayList<Handle>> mbt = new TreeMap<Integer, ArrayList<Handle>>();
		TreeMap<Integer, ArrayList<Handle>> mhhi = new TreeMap<Integer, ArrayList<Handle>>();
		TreeMap<Integer, ArrayList<Handle>> mms = new TreeMap<Integer, ArrayList<Handle>>();
		TreeMap<Long, ArrayList<Handle>> mmultitextattr = new TreeMap<Long, ArrayList<Handle>>();
		TreeMap<Integer, ArrayList<Handle>> mlat = new TreeMap<Integer, ArrayList<Handle>>();
		TreeMap<Integer, ArrayList<Handle>> mlong = new TreeMap<Integer, ArrayList<Handle>>();


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
				String zip = p.getZipStr();
				Integer lat;
				try {
					lat = ZipCodeDB.getInstance().getLatLong(Integer.parseInt(zip)).getFirst().intValue();
				} catch (NumberFormatException e) {
					lat = null;
				}
				ArrayList<Handle> list = mlat.get(lat);
				if (list == null) {
					list = new ArrayList<Handle>();
					mlat.put(lat, list);
				}
				list.add(h);
			}
			{
				String zip = p.getZipStr();
				Integer aLong;
				try {
					aLong = ZipCodeDB.getInstance().getLatLong(Integer.parseInt(zip)).getSecond().intValue();
				} catch (NumberFormatException e) {
					aLong = null;
				}
				ArrayList<Handle> list = mlong.get(aLong);
				if (list == null) {
					list = new ArrayList<Handle>();
					mlong.put(aLong, list);
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
				if (k!=null) {
					ArrayList<Handle> list = mcountry.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mcountry.put(k, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getState();
				if (k!=null) {
					ArrayList<Handle> list = mstate.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mstate.put(k, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getCity();
				if (k!=null) {
					ArrayList<Handle> list = mcity.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mcity.put(k, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getZip();
				if (k!=null) {
					ArrayList<Handle> list = mzip.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mzip.put(k, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getDmaCode();
				if (k!=null) {
					ArrayList<Handle> list = mdma.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mdma.put(k, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getAreaCode();
				if (k!=null) {
					ArrayList<Handle> list = marea.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						marea.put(k, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getGeoEnabled();
				if (k!=null) {
					ArrayList<Handle> list = mgeoenabled.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mgeoenabled.put(k, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getProviderCategory();
				if (k!=null) {
					ArrayList<Handle> list = mprovcategory.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mprovcategory.put(k, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getGlobalId();
				if (k!=null) {
					ArrayList<Handle> list = mglobalid.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mglobalid.put(k, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getHHI();
				if (k!=null) {
					ArrayList<Handle> list = mhhi.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mhhi.put(k, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getCC();
				if (k!=null) {
					ArrayList<Handle> list = mms.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mms.put(k, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getBT();
				if (k!=null) {
					ArrayList<Handle> list = mbt.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mbt.put(k, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getExternalFilterField1();
				if (k!= null) {
					long key = IndexUtils.createLongIndexKey(IProduct.Attribute.kExternalFilterField1,k);
					ArrayList<Handle> list = mmultitextattr.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mmultitextattr.put(key, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getExternalFilterField2();
				if (k!= null) {
					long key = IndexUtils.createLongIndexKey(IProduct.Attribute.kExternalFilterField2,k);
					ArrayList<Handle> list = mmultitextattr.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mmultitextattr.put(key, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getExternalFilterField3();
				if (k!= null) {
					long key = IndexUtils.createLongIndexKey(IProduct.Attribute.kExternalFilterField3,k);
					ArrayList<Handle> list = mmultitextattr.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mmultitextattr.put(key, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getExternalFilterField4();
				if (k!= null) {
					long key = IndexUtils.createLongIndexKey(IProduct.Attribute.kExternalFilterField4,k);
					ArrayList<Handle> list = mmultitextattr.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mmultitextattr.put(key, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getExternalFilterField5();
				if (k!= null) {
					long key = IndexUtils.createLongIndexKey(IProduct.Attribute.kExternalFilterField5,k);
					ArrayList<Handle> list = mmultitextattr.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mmultitextattr.put(key, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getUT1();
				if (k!= null) {
					long key = IndexUtils.createLongIndexKey(IProduct.Attribute.kUT1,k);
					ArrayList<Handle> list = mmultitextattr.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mmultitextattr.put(key, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getUT2();
				if (k!= null) {
					long key = IndexUtils.createLongIndexKey(IProduct.Attribute.kUT2,k);
					ArrayList<Handle> list = mmultitextattr.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mmultitextattr.put(key, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getUT3();
				if (k!= null) {
					long key = IndexUtils.createLongIndexKey(IProduct.Attribute.kUT3,k);
					ArrayList<Handle> list = mmultitextattr.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mmultitextattr.put(key, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getUT4();
				if (k!= null) {
					long key = IndexUtils.createLongIndexKey(IProduct.Attribute.kUT4,k);
					ArrayList<Handle> list = mmultitextattr.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mmultitextattr.put(key, list);
					}
					list.add(h);
				}
			}
			{
				Integer k = p.getUT5();
				if (k!= null) {
					long key = IndexUtils.createLongIndexKey(IProduct.Attribute.kUT5,k);
					ArrayList<Handle> list = mmultitextattr.get(k);
					if (list == null) {
						list = new ArrayList<Handle>();
						mmultitextattr.put(key, list);
					}
					list.add(h);
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
			updateIntegerIndex(IProduct.Attribute.kLatitude, mlat);
			updateIntegerIndex(IProduct.Attribute.kLongitude, mlong);

//      updateIntegerIndex(IProduct.Attribute.kProductType, mprovider);
			updateIntegerIndex(IProduct.Attribute.kImageWidth, miwidth);
			updateIntegerIndex(IProduct.Attribute.kImageHeight, miheight);

			updateIntegerIndex(IProduct.Attribute.kCountry, mcountry);
			updateIntegerIndex(IProduct.Attribute.kState, mstate);
			updateIntegerIndex(IProduct.Attribute.kCity, mcity);
			updateIntegerIndex(IProduct.Attribute.kZip, mzip);
			updateIntegerIndex(IProduct.Attribute.kDMA, mdma);
			updateIntegerIndex(IProduct.Attribute.kArea, marea);
			updateIntegerIndex(IProduct.Attribute.kGeoEnabledFlag, mgeoenabled);
			updateIntegerIndex(IProduct.Attribute.kCategory, mprovcategory);
			updateIntegerIndex(IProduct.Attribute.kGlobalId, mglobalid);
			updateIntegerIndex(IProduct.Attribute.kHHI, mglobalid);
			updateIntegerIndex(IProduct.Attribute.kBT, mglobalid);
			updateIntegerIndex(IProduct.Attribute.kCC, mglobalid);
			updateLongIndex(IProduct.Attribute.kMultiValueTextField, mmultitextattr);

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

	@SuppressWarnings("unchecked")
	public void overwriteOptIndex(IProduct.Attribute type, Integer experienceId, TreeMap<Integer, ArrayList<Handle>> mindex){
		m_opt_indices.readerLock();
		Hashtable<Product.Attribute, ProductAttributeIndex<?, Handle>> subIndex = m_opt_indices.get(experienceId);
		m_opt_indices.readerUnlock();
		if(subIndex == null){
			subIndex = new Hashtable<Product.Attribute, ProductAttributeIndex<?, Handle>>();
		}
		ProductAttributeIndex<?, Handle> index = subIndex.get(type);
		if(index == null){
			index = new OptTextIndexImpl(type);
		}

		((ProductAttributeIndex<Integer, Handle>) index).overwrite(mindex);

		subIndex.put(type, index);

		m_opt_indices.writerLock();
		m_opt_indices.put(experienceId, subIndex);
		m_opt_indices.writerUnlock();
	}

	public synchronized void cleanOptIndex(SortedSet<Integer> experiences){
		List<Integer> keysToRemove = new ArrayList<Integer>(experiences.size());
		m_opt_indices.readerLock();
		Set<Integer> keys = m_opt_indices.keySet();
		for(Integer key: keys){
			if(!experiences.contains(key)){
				keysToRemove.add(key);
			}
		}
		m_opt_indices.readerUnlock();
		for(Integer keyToRemove: keysToRemove){
			m_opt_indices.writerLock();
			m_opt_indices.remove(keyToRemove);
			m_opt_indices.writerUnlock();
		}
	}

	public void deleteAllOptIndexesForExperience(Integer experienceId){
		m_opt_indices.writerLock();
		m_opt_indices.remove(experienceId);
		m_opt_indices.writerUnlock();
	}

	public ProductAttributeIndex getOptIndex(IProduct.Attribute type, Integer experienceId){
		m_opt_indices.readerLock();
		Hashtable<Product.Attribute, ProductAttributeIndex<?, Handle>> subIndex = m_opt_indices.get(experienceId);
		m_opt_indices.readerUnlock();
		if(subIndex != null){
			return subIndex.get(type);
		}
		return null;
	}

	/**
	 * Return a handle given a product id.
	 * @param pid
	 * @return
	 */
	public Handle getHandle(Long pid) {
		Handle p = null;
		try {
			m_allProducts.readerLock();
			p = getProdHandle(pid);
		} finally {
			m_allProducts.readerUnlock();
		}
		return p;
	}

	public Handle getNextHandle(Long pid){
		Handle p = null;
		try {
			m_allProducts.readerLock();
			p = new ProductHandle(1.0, pid.longValue());
			SortedSet<Handle> tailSet = m_allProducts.tailSet(p);
			if(tailSet!=null && !tailSet.isEmpty()) {
				p = tailSet.first();
			} else {
				p = null;
			}
		} finally {
			m_allProducts.readerUnlock();
		}
		return p;
	}

	/**
	 * Get Handle without checking a lock, reader should call readerLock()
	 * Check if the prod exists - else return null
	 * @param pid
	 * @return Handle
	 */
	public ProductHandle getProdHandle(Long pid) {
		ProductHandle p = new ProductHandle(1.0, pid.longValue());
		Handle ph;
		ph = m_allProducts.find(p);

		if (ph !=null) {
			p = (ProductHandle) ph;
		} else {
			p = null;
		}
		return p;
	}

	public boolean isEmpty() {
		return m_allProducts.isEmpty();
	}

	/**
	 * Add the new products into the database.
	 */
	public void addNewProducts(SortedSet<Handle> newProducts) {
		try {
			m_allProducts.writerLock();
			m_allProducts.addAll(newProducts);
		} finally {
			m_allProducts.writerUnlock();
		}
	}

	/**
	 * Clears the indices and the maps
	 */
	public void clearProductDB() { //todo: do we need to add cleanup of opt indexes?
		for (ProductAttributeIndex<?, Handle> lIndex : m_indices.values()) {
			lIndex.clear();
		}
		try {
			m_allProducts.writerLock();
			m_allProducts.clear();
		} finally {
			m_allProducts.writerUnlock();
		}
		try {
			m_map.writerLock();
			m_map.clear();
		} finally {
			m_map.writerUnlock();
		}
	}

	public Enumeration<IProduct.Attribute> getIndices() {
		return m_indices.keys();
	}

	public int getSize() {
		return m_allProducts.size();
	}

}
