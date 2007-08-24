package com.tumri.joz.campaign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.tumri.cma.CMAException;
import com.tumri.cma.domain.AdPod;
import com.tumri.cma.domain.BrandInfo;
import com.tumri.cma.domain.Campaign;
import com.tumri.cma.domain.CategoryInfo;
import com.tumri.cma.domain.MerchantInfo;
import com.tumri.cma.domain.OSpec;
import com.tumri.cma.domain.ProductInfo;
import com.tumri.cma.domain.ProviderInfo;
import com.tumri.cma.domain.TSpec;
import com.tumri.cma.persistence.lisp.CampaignLispDataProviderImpl;
import com.tumri.joz.Query.AttributeQuery;
import com.tumri.joz.Query.CNFQuery;
import com.tumri.joz.Query.ConjunctQuery;
import com.tumri.joz.Query.KeywordQuery;
import com.tumri.joz.Query.ProductQueryProcessor;
import com.tumri.joz.Query.RangeQuery;
import com.tumri.joz.Query.SimpleQuery;
import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.utils.AppProperties;

/**
 * Class to maintain the cache of the campaign and oSpec data
 * @author nipun
 *
 */
public class CampaignDataCache {
	
	  private static Logger log = Logger.getLogger (CampaignDataCache.class);

	  private static AtomicReference<CampaignDataCache> g_campaignProvider = null;
	  private HashMap<String, OSpec> m_oSpecHashtable = null;
	  //TODO: Convert the query cache into LRU
	  private WeakHashMap<String, CNFQuery> m_oSpecQueryCache = null;
	  
	  private static String _lispSourceFilePath = "..";
	  
	  public CampaignDataCache() {
		 AppProperties props = AppProperties.getInstance();
		 String srcPath = props.getProperty("TSPEC_MAPPINGS_SOURCE_DIR_PATH");
		 if (srcPath != null) {
			 _lispSourceFilePath = srcPath;
		 }
		 m_oSpecHashtable = new HashMap<String, OSpec>();
		 m_oSpecQueryCache = new WeakHashMap<String, CNFQuery>();
	  }
	  
	  public static CampaignDataCache getInstance() {
		  if (g_campaignProvider == null) {
			  synchronized (CampaignDataCache.class) {
				  if (g_campaignProvider == null) {
					  g_campaignProvider = new AtomicReference<CampaignDataCache>();
					  CampaignDataCache cdCache = new CampaignDataCache();
					  cdCache.load();
				  }
			  }
		  }
	    return g_campaignProvider.get();
	  }
	  
	  /**
	   * Initialize the campaign Hashtable, and update the reference.
	   * This method is called whenever we need to load or refresh the cache.
	   *
	   */
	  public void load() {
		  try {
			  long startTime = System.currentTimeMillis();
			  log.info("Going to load the campaign data");
			  //TODO: Change this to the CMA factory instantiation (Once Bhupen makes the change to CMAFactory)
			  CampaignLispDataProviderImpl lispDeltaProvider = CampaignLispDataProviderImpl.getInstance(_lispSourceFilePath);
			  Iterator<Campaign> campaignIter = lispDeltaProvider.getNewDeltas();
			  CampaignDataCache newCache = new CampaignDataCache();
			  HashMap<String, OSpec> tmpSpecHashtable = new HashMap<String, OSpec>();
			  WeakHashMap<String, CNFQuery> tmpSpecQueryCache = new WeakHashMap<String, CNFQuery>();

			  if (campaignIter != null) {
				  while (campaignIter.hasNext()) {
					  Campaign theCampaign = campaignIter.next();
					  for (int i=0;i<theCampaign.getAdPods().size();i++){
						  //TODO: Build the indices for the Campaign lookup
						  AdPod theAdPod = theCampaign.getAdPods().get(i);
						  OSpec theOSpec = theAdPod.getOspec();
						  String oSpecName = theOSpec.getName();
						  tmpSpecHashtable.put(oSpecName, theOSpec);  
						  //Materialize the queries
						  //Note: The new string() is done here so that there is no strong reference to the key (not added to the string pool), 
						  // to enable it to be garbage collected when needed
						  tmpSpecQueryCache.put(new String(oSpecName), getQuery(theOSpec));
					  }
				  }
				  newCache.set_oSpecHashtable(tmpSpecHashtable);
				  newCache.set_oSpecQueryCache(tmpSpecQueryCache);
				  g_campaignProvider.set(newCache);
			  }
			  log.info("Campaign data loaded into cache. Time taken (millis) : " + (System.currentTimeMillis() - startTime));
		  } catch (CMAException e) {
			  e.printStackTrace();
		  }
	  }
	  
	  
	  /**
	   * return the Query for a given oSpec, from the cache - or build the query and then return
	   * @param oSpecName
	   * @return
	   */
	  public CNFQuery getCNFQuery(String oSpecName) {
		  CNFQuery query = m_oSpecQueryCache.get(oSpecName);
		  if (query == null) {
			  //Get the query from the g_OSpecHashtable
			  OSpec oSpec = m_oSpecHashtable.get(oSpecName);
			  query = getQuery(oSpec);
			  //Put into the cache
			  m_oSpecQueryCache.put(new String(oSpecName), query);
		  }
		  
		  return query;
	  }
	  
	  /**
	   * Returns an ospec object from the cache
	   * @param oSpecName
	   * @return
	   */
	  public OSpec getOSpec(String oSpecName) {
		  return m_oSpecHashtable.get(oSpecName);
	  }
	  
	  /**
	   * Walk thru the OSpec details and create the Query
	   * @param oSpec
	   * @return
	   */
	  private CNFQuery getQuery(OSpec oSpec) {
		  CNFQuery _query = new CNFQuery ();
		  ConjunctQuery _cjquery = new ConjunctQuery (new ProductQueryProcessor());
		  List<TSpec> tSpecList = oSpec.getTspecs();
		  Iterator<TSpec> tSpecIter = tSpecList.iterator();
		  while (tSpecIter.hasNext()){
			  TSpec theTSpec = tSpecIter.next();
			  //Keyword
			  String keywordExp = theTSpec.getLoadTimeKeywordExpression();
			  if (keywordExp != null){
				  KeywordQuery kwQuery = new KeywordQuery(keywordExp);
				  _cjquery.addQuery(kwQuery);
			  }
			  
			  //Excluded Brand
			  List<BrandInfo> bixList = theTSpec.getExcludedBrands();
			  if (bixList != null) {
				SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kBrand, bixList, true);
				_cjquery.addQuery(sq);
			  }
			  
			  //Included brand
			  List<BrandInfo> binList = theTSpec.getIncludedBrands();
			  if (binList != null) {
				SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kBrand, binList, false);
				_cjquery.addQuery(sq);
			  }			  
			  
			  //Include cats
			  List<CategoryInfo> cinList = theTSpec.getIncludedCategories();
			  if (cinList != null) {
				SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kCategory, cinList, false);
				_cjquery.addQuery(sq);
			  }

			  //Excluded cats
			  List<CategoryInfo> cexList = theTSpec.getExcludedCategories();
			  if (cexList != null) {
				SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kCategory, cexList, true);
				_cjquery.addQuery(sq);
			  }

			  //Included merchants
			  List<MerchantInfo> inMerchants = theTSpec.getIncludedMerchants();
			  if (inMerchants != null) {
					SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kSupplier, inMerchants, false);
					_cjquery.addQuery(sq);				  
			  }
			  
			  //Excluded merchants
			  List<MerchantInfo> exMerchants = theTSpec.getExcludedMerchants();
			  if (exMerchants != null) {
					SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kSupplier, exMerchants, true);
					_cjquery.addQuery(sq);				  
			  }

			  //Included Products
			  List<ProductInfo> inProducts = theTSpec.getIncludedProducts();
			  if (inProducts != null) {
					SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kProductName, inProducts, false);
					_cjquery.addQuery(sq);				  
			  }	
			  
			  //Excluded products
			  List<ProductInfo> exProducts = theTSpec.getExcludedProducts();
			  if (exProducts != null) {
					SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kProductName, exProducts, true);
					_cjquery.addQuery(sq);				  
			  }	
			  
			  //Included Providers
			  List<ProviderInfo> inProviders = theTSpec.getIncludedProviders();
			  if (inProviders != null) {
					SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kProvider, inProviders, false);
					_cjquery.addQuery(sq);				  
			  }
			  
			  //Excluded Providers
			  List<ProviderInfo> exProviders = theTSpec.getExcludedProviders();
			  if (exProviders != null) {
					SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kProvider, exProviders, true);
					_cjquery.addQuery(sq);				  
			  }	
			  
			  //CPC Range
			  int highCPC = theTSpec.getHighCPC();
			  int lowCPC = theTSpec.getLowCPC();
			  if ((highCPC > 0) || ( lowCPC > 0)) {
				  SimpleQuery sq = new RangeQuery (IProduct.Attribute.kCPC,lowCPC, highCPC);
				  _cjquery.addQuery(sq);
			  }
			  
			  //Price Range
			  int highPrice = theTSpec.getHighCPC();
			  int lowPrice = theTSpec.getLowCPC();
			  if ((highPrice > 0) || ( lowPrice > 0)) {
				  SimpleQuery sq = new RangeQuery (IProduct.Attribute.kPrice,lowPrice, highPrice);
				  _cjquery.addQuery(sq);
			  }
			  
		  }
		  
		  _query.addQuery (_cjquery);
		  return _query;
	  }
	  
	  /**
	   * Helper method that Builds a Simplequery based on the values passed in
	   * @param type
	   * @param values
	   * @return
	   */
	  private SimpleQuery buildAttributeQuery(IProduct.Attribute type, List values, boolean bNegation) {
			ArrayList<Integer> valueIdList = new ArrayList<Integer>(values.size()); 
			ArrayList<String> valueStrList = new ArrayList<String>(values.size()); 
			if (values != null) {
				for (int i=0;i<values.size();i++){
					if (type.equals(IProduct.Attribute.kCategory)) {
						CategoryInfo cInfo = (CategoryInfo)values.get(i);
						valueStrList.add(cInfo.getName());
					} else if (type.equals(IProduct.Attribute.kBrand)) {
						BrandInfo bInfo = (BrandInfo)values.get(i);
						valueStrList.add(bInfo.getName());
					} else if (type.equals(IProduct.Attribute.kProvider)) {
						ProviderInfo pInfo = (ProviderInfo)values.get(i);
						valueStrList.add(pInfo.getName());
					} else if (type.equals(IProduct.Attribute.kSupplier)) {
						MerchantInfo mInfo = (MerchantInfo)values.get(i);
						valueStrList.add(mInfo.getName());
					} else if (type.equals(IProduct.Attribute.kProductName)) {
						ProductInfo pInfo = (ProductInfo)values.get(i);
						valueStrList.add(pInfo.getName());
					}
				}
				for (int i=0;i<valueStrList.size();i++){
					String valueStr = valueStrList.get(i);
					DictionaryManager dm = DictionaryManager.getInstance ();
					Integer brandId = dm.getId (type, valueStr);
					valueIdList.add(brandId);
				}				
			}
			SimpleQuery sq = new AttributeQuery (type, valueIdList);
			sq.setNegation(bNegation);
			return sq;
	  }
	  
	  @Test
	  public void testCampaignDataCache() {
		  _lispSourceFilePath = "/Users/nipun/Documents/Tumri/JoZ";
		  CampaignDataCache cdCache = CampaignDataCache.getInstance();
		  CNFQuery iWonQuery = cdCache.getCNFQuery("T-SPEC-http://games.iwon.com/");
		  if (iWonQuery!= null) {
			  log.info("Got back the query object");
			  iWonQuery.exec();
		  } else {
			  assert(false);
		  }
	  }

	private void set_oSpecHashtable(HashMap<String, OSpec> specHashtable) {
		synchronized (CampaignDataCache.class) {
			m_oSpecHashtable = specHashtable;			
		}
	}

	private void set_oSpecQueryCache(WeakHashMap<String, CNFQuery> specQueryCache) {
		 synchronized (CampaignDataCache.class) {
			 m_oSpecQueryCache = specQueryCache;
		 }
	}
}
