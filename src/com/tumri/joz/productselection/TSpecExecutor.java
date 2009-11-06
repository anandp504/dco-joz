/*
 * ListingsQueryHandler.java
 *
 * COPYRIGHT (C)  TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (@tumri.com)
 * @version 1.0     
 *
 */
package com.tumri.joz.productselection;

import com.tumri.cma.domain.ProductInfo;
import com.tumri.cma.domain.TSpec;
import com.tumri.content.data.Product;
import com.tumri.content.data.dictionary.DictionaryManager;
import com.tumri.joz.Query.*;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.TSpecQueryCache;
import com.tumri.joz.campaign.TSpecQueryCacheHelper;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.IndexUtils;
import com.tumri.utils.data.SortedArraySet;
import com.tumri.utils.strings.StringTokenizer;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * Class to execute a given tspec query. The assumption is that if the TSpec contains included products - then all other criteria
 * are ignored. 
 * @author: nipun
 * Date: Jun 26, 2008
 * Time: 3:02:39 PM
 */
public class TSpecExecutor {


	private ProductSelectionRequest request = null;
	private CNFQuery m_tSpecQuery = null;
	private CNFQuery debugTSpecQuery = null;
	private TSpec m_tspec = null;
	private int m_tspecId = 0;
	private static final char MULTI_VALUE_DELIM = AppProperties.getInstance().getMultiValueDelimiter();
	private boolean m_geoFilterEnabled = false;
	private boolean m_ExternalKeywords = false;
	private boolean m_ExternalFilters = false;
	private int m_currPage = 0;
	private int m_pageSize = 0;
	private Features m_feature = null;
	private boolean m_randomize = false;

	private static Logger log = Logger.getLogger (TSpecExecutor.class);

	public TSpecExecutor(ProductSelectionRequest req) {
		this.request = req;
	}

	public TSpecExecutor(ProductSelectionRequest req, Features f) {
		this.request = req;
		this.m_feature = f;
	}

	/**
	 * Process the query using the tspec Id, which is used to get teh query from teh cache
	 * @param tSpecId  - the id
	 * @return - list of product handles
	 */
	public ArrayList<Handle> processQuery(int tSpecId) {
		m_tspecId = tSpecId;
		m_tspec = CampaignDB.getInstance().getTspec(tSpecId);

		if (m_tspec == null) {
			return null;
		}

		ArrayList<Handle> includedProds = getIncludedProducts(m_tspec);
		if (includedProds!= null && includedProds.size()>0) {
			return includedProds;
		}

		//Get the tSpec from the cache - note the tSpec id is used as the key in the TSpecQueryCache
		m_tSpecQuery = (CNFQuery) TSpecQueryCache.getInstance().getCNFQuery(tSpecId).clone();
		setupRequestParms();
		return executeTSpec();
	}

	/**
	 * Process the TSpec object that is passed in.
	 * @param tSpec - The tspec
	 * @return - list of handles
	 */
	public ArrayList<Handle> processQuery(TSpec tSpec) {

		m_tspec = tSpec;
		ArrayList<Handle> includedProds = getIncludedProducts(tSpec);
		if (includedProds!= null && includedProds.size()>0) {
			return includedProds;
		}
		//Get the tSpec from the cache - note the tSpec id is used as the key in the TSpecQueryCache
		m_tSpecQuery = TSpecQueryCacheHelper.getQuery(tSpec);
        //Fix to allow TSpec evaluator from Joz console to backfill correctly
        debugTSpecQuery = (CNFQuery)m_tSpecQuery.clone();
		setupRequestParms();
		return executeTSpec();
	}

	/**
	 * Setup the request parameters for the TSpecExcecutor instance.
	 */
	private void setupRequestParms(){

		if (request.getRequestKeyWords()!=null && !request.getRequestKeyWords().trim().equals("")) {
			m_ExternalKeywords = true;
		}

		m_geoFilterEnabled = m_tspec.isGeoEnabledFlag()||m_tspec.isApplyGeoFilter();


		//Set defaults the current Page and page Size if they have not been specified
		if ((request.getCurrPage() == -1 && request.getPageSize() ==-1)) {
			m_currPage = 0;
			m_pageSize = 0;
		}

		//Set pagination bounds for TSpec Query
		if (request.getCurrPage() > -1 && request.getPageSize() > -1) {
			m_currPage = request.getCurrPage();
			m_pageSize = request.getPageSize();
		}

		if (m_ExternalKeywords || request.isBPaginate() || request.isBMineUrls()
                || m_tspec.isMinePubUrl()) {
			//Do not randomize
			m_tSpecQuery.setStrict(true);
			m_randomize = false;
		} else {
			//Randomize
            if (m_tspec.isEnableBackFill()) {
                m_tSpecQuery.setStrict(false);
            } else {
                m_tSpecQuery.setStrict(true);
            }
			m_randomize = true;
		}

		m_tSpecQuery.setBounds(m_pageSize,m_currPage);

	}

	/**
	 * Perform the keyword search
	 * @param keywords - input keywords
	 */
	private void doKeywordSearch(String keywords) {
		KeywordQuery sKwQuery;
		if ((keywords!=null)&&(!"".equals(keywords.trim()))) {
			m_ExternalKeywords = true;
			sKwQuery = new KeywordQuery(keywords,false);
			m_tSpecQuery.addSimpleQuery(sKwQuery);
		}
		if (m_feature !=null) {
			m_feature.addFeatureDetail(Features.FEATURE_SEARCH_KEYWORDS, keywords);
		}
	}

	private void addRequestCategoryQuery(String requestCategory) {
		ArrayList<Integer> catList = new ArrayList<Integer>();
		Integer catId = DictionaryManager.getId (IProduct.Attribute.kCategory, requestCategory);
		catList.add(catId);
		SimpleQuery catQuery = new AttributeQuery (IProduct.Attribute.kCategory, catList);
		CNFQuery copytSpecQuery = (CNFQuery)m_tSpecQuery.clone();
		copytSpecQuery.addSimpleQuery(catQuery);
		m_tSpecQuery = null;
		m_tSpecQuery = copytSpecQuery;
	}

	private SimpleQuery createGeoEnabledQuery(boolean bGeoEnabled) {
		Integer geoFlagId = DictionaryManager.getId(Product.Attribute.kGeoEnabledFlag, bGeoEnabled?"true":"false");
		return new AttributeQuery(Product.Attribute.kGeoEnabledFlag, geoFlagId);
	}

	private void addGeoFilterQuery(int pageSize, int currPage) {
		//If there are no queries in the selected tspec - do not add geo flag
		boolean bSimpleQueries = false;
		ArrayList<ConjunctQuery> _conjQueryAL = m_tSpecQuery.getQueries();
		for (ConjunctQuery conjQuery:_conjQueryAL) {
			ArrayList<SimpleQuery> simpleQueryAL = conjQuery.getQueries();
			if (simpleQueryAL.size()!=0) {
				bSimpleQueries = true;
				break;
			}
		}
		if (!bSimpleQueries) {
			return;
		}

		if (m_geoFilterEnabled) {
			String zipCode = request.getZipCode();
			if (zipCode!=null && !"".equals(zipCode)) {
				if (m_feature != null) {
					m_feature.addFeatureDetail("Zip", zipCode);
				}
			}
			String cityCode = request.getCityCode();
			if (cityCode!=null && !"".equals(cityCode)) {
				if (m_feature != null) {
					m_feature.addFeatureDetail("City", cityCode);
				}
			}
			String dmaCode = request.getDmaCode();
			if (dmaCode!=null && !"".equals(dmaCode)) {
				if (m_feature != null) {
					m_feature.addFeatureDetail("DMA", dmaCode);
				}

			}
			String areaCode = request.getAreaCode();
			if (areaCode!=null && !"".equals(areaCode)) {
				if (m_feature != null) {
					m_feature.addFeatureDetail("Area", areaCode);
				}
			}
			String stateCode = request.getStateCode();
			if (stateCode!=null && !"".equals(stateCode)) {
				if (m_feature != null) {
					m_feature.addFeatureDetail("State", stateCode);
				}
			}
			String countryCode = request.getCountryCode();
			if (countryCode!=null && !"".equals(countryCode)) {
				if (m_feature != null) {
					m_feature.addFeatureDetail("Country", countryCode);
				}

			}
			CNFQuery geoTSpecQuery = new CNFQuery();
			geoTSpecQuery.setBounds(pageSize, currPage);
			_conjQueryAL = m_tSpecQuery.getQueries();
			for (ConjunctQuery conjQuery:_conjQueryAL) {
				if (zipCode!=null && !"".equals(zipCode)) {
					ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kZip, zipCode);
					if (cloneConjQuery!= null) {
						geoTSpecQuery.addQuery(cloneConjQuery);
					}
					ConjunctQuery radiuscloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kRadius, zipCode);
					if (radiuscloneConjQuery!= null) {
						geoTSpecQuery.addQuery(radiuscloneConjQuery);
					}
				}
				if (cityCode!=null && !"".equals(cityCode)) {
					ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kCity, cityCode);
					if (cloneConjQuery!= null) {
						geoTSpecQuery.addQuery(cloneConjQuery);
					}
				}
				if (dmaCode!=null && !"".equals(dmaCode)) {
					ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kDMA, dmaCode);
					if (cloneConjQuery!= null) {
						geoTSpecQuery.addQuery(cloneConjQuery);
					}
				}
				if (areaCode!=null && !"".equals(areaCode)) {
					ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kArea, areaCode);
					if (cloneConjQuery!= null) {
						geoTSpecQuery.addQuery(cloneConjQuery);
					}

				}
				if (stateCode!=null && !"".equals(stateCode)) {
					ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kState, stateCode);
					if (cloneConjQuery!= null) {
						geoTSpecQuery.addQuery(cloneConjQuery);
					}
				}
				if (countryCode!=null && !"".equals(countryCode)) {
					ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kCountry, countryCode);
					if (cloneConjQuery!= null) {
						geoTSpecQuery.addQuery(cloneConjQuery);
					}
				}
				//Add the backfill query
				{
					ConjunctQuery cloneConjQuery = (ConjunctQuery)conjQuery.clone();
					SimpleQuery geoEnabledQuery = createGeoEnabledQuery(false);
					cloneConjQuery.setBounds(pageSize, currPage);
					cloneConjQuery.setStrict(m_ExternalKeywords);
					cloneConjQuery.addQuery(geoEnabledQuery);
					geoTSpecQuery.addQuery(cloneConjQuery);
				}
			}
			if (m_feature !=null) {
				m_feature.setGeoUsed(true);
			}
			if (!m_ExternalKeywords) {
				//Set a reference so we return random selection of products.
				geoTSpecQuery.setCacheReference(m_tSpecQuery.getCacheReference());
				geoTSpecQuery.setReference(ProductDB.getInstance().genReference ());
			}
			m_tSpecQuery = geoTSpecQuery;
		} else {
            //BUG2556: Do not exclude Geo Enabled products for non geo enabled queries.
            //m_tSpecQuery.addSimpleQuery(createGeoEnabledQuery(false));
		}
	}

	private ConjunctQuery cloneAndAddQuery(ConjunctQuery conjQuery, Product.Attribute kAttr, String val){
		ConjunctQuery cloneConjQuery = (ConjunctQuery)conjQuery.clone();

		if (kAttr == IProduct.Attribute.kRadius) {
			int rad = m_tspec.getRadius();
			if (!m_tspec.isUseRadiusQuery() || m_tspec.getRadius() ==0) {
				return null;
			}
			LatLongQuery latQuery = new LatLongQuery(IProduct.Attribute.kLatitude,val,rad);
			LatLongQuery longQuery = new LatLongQuery(IProduct.Attribute.kLongitude,val,rad);
			cloneConjQuery.addQuery(latQuery);
			cloneConjQuery.addQuery(longQuery);
		} else {
			Integer codeId = DictionaryManager.getId(kAttr, val);
			AttributeQuery aQuery = new AttributeQuery(kAttr, codeId);
			cloneConjQuery.addQuery(aQuery);
		}
		cloneConjQuery.setBounds(m_pageSize, m_currPage);
		cloneConjQuery.setStrict(true);
		return cloneConjQuery;
	}

	/**
	 * Inspects the request and adds multivalue delim fields for Product Selection.
	 * @param request - the request
	 */
	private void addExternalFilterRequestQueries(ProductSelectionRequest request) {
		if (m_tspec.isUseListingFilter1()) {
			String externalFilterField1 = request.getExternalFilterQuery1();
			if (externalFilterField1 != null && !externalFilterField1.equals(""))  {
				addExternalFilterFieldQuery(IProduct.Attribute.kExternalFilterField1, externalFilterField1);
				if (m_feature!=null) {
					m_feature.addFeatureDetail("ExternalFilterField1", externalFilterField1);
				}
			}

		}
		if (m_tspec.isUseListingFilter2()) {
			String externalFilterField2 = request.getExternalFilterQuery2();
			if (externalFilterField2 != null && !externalFilterField2.equals(""))  {
				addExternalFilterFieldQuery(IProduct.Attribute.kExternalFilterField2, externalFilterField2);
				if (m_feature!=null) {
					m_feature.addFeatureDetail("ExternalFilterField2", externalFilterField2);
				}
			}
		}
		if (m_tspec.isUseListingFilter3()) {
			String externalFilterField3 = request.getExternalFilterQuery3();
			if (externalFilterField3 != null && !externalFilterField3.equals(""))  {
				addExternalFilterFieldQuery(IProduct.Attribute.kExternalFilterField3, externalFilterField3);
				if (m_feature!=null) {
					m_feature.addFeatureDetail("ExternalFilterField3", externalFilterField3);
				}
			}
		}
		if (m_tspec.isUseListingFilter4()) {
			String externalFilterField4 = request.getExternalFilterQuery4();
			if (externalFilterField4 != null && !externalFilterField4.equals(""))  {
				addExternalFilterFieldQuery(IProduct.Attribute.kExternalFilterField4, externalFilterField4);
				if (m_feature!=null) {
					m_feature.addFeatureDetail("ExternalFilterField4", externalFilterField4);
				}
			}
		}
		if (m_tspec.isUseListingFilter5()) {
			String externalFilterField5 = request.getExternalFilterQuery5();
			if (externalFilterField5 != null && !externalFilterField5.equals(""))  {
				addExternalFilterFieldQuery(IProduct.Attribute.kExternalFilterField5, externalFilterField5);
				if (m_feature!=null) {
					m_feature.addFeatureDetail("ExternalFilterField5", externalFilterField5);
				}

			}
		}

	}

	/**
	 * Adds a multi value field query to the current tspec being executed
	 * @param kAttr - Product Attribute
	 * @param externalFilterField - The multi value field passed from iCS
	 */
	private void addExternalFilterFieldQuery(IProduct.Attribute kAttr, String externalFilterField) {
		StringTokenizer st = new StringTokenizer(externalFilterField, MULTI_VALUE_DELIM);
		ArrayList<String> multiValueAL = st.getTokens();
		ArrayList<Long> multiValueIdAL = new ArrayList<Long>();
		for (String val : multiValueAL) {
			//Url decode
			if (val == null) {
				continue;
			}
			try {
				val = URLDecoder.decode(val,"utf-8");
				val = val.toLowerCase();
			} catch(UnsupportedEncodingException e){
				log.error("Could not decode the value : " + val);
				continue;
			} catch(IllegalArgumentException ilegalArgEx){
				log.error("Skipping value that cannot be decoded : " + val);
				continue;
			}
			Integer fieldId = DictionaryManager.getId (kAttr, val);
			long key = IndexUtils.createLongIndexKey(kAttr, fieldId);
			multiValueIdAL.add(key);
		}
		if (multiValueIdAL.isEmpty()) {
			return;
		}
		m_ExternalFilters = true;
		LongTextQuery aQuery = new LongTextQuery (IProduct.Attribute.kMultiValueTextField, multiValueIdAL);
		CNFQuery copytSpecQuery = (CNFQuery)m_tSpecQuery.clone();
		ArrayList<ConjunctQuery> cnjQueries = copytSpecQuery.getQueries();
		for (ConjunctQuery conjunctQuery : cnjQueries) {
			conjunctQuery.addQuery(aQuery);
		}
		m_tSpecQuery = copytSpecQuery;
	}

	/**
	 * Performs the URL scavenging and runs the query
	 * @param url - input Ad Data request
	 * @return keywords
	 */
	private String doURLKeywordSearch(String url) {
		if (url==null || url.equals("")){
			return null;
		}
		String queryNames = "";
		String stopWords = "";
		//Get the queryNames and Stopwords
		String tmpqueryNames = m_tspec.getPublicURLQueryNames();
		String tmpstopWords = m_tspec.getPublicUrlStopWords();
		if (tmpqueryNames!=null){
			queryNames = queryNames + " " + tmpqueryNames;
		}
		if (tmpstopWords!=null){
			stopWords = stopWords + " " + tmpstopWords;
		}
		return URLScavenger.mineKeywords(url, stopWords, queryNames);
	}

	/**
	 * Perform the backfill of products only in the case of external keywords or filters
	 * Backfill is merely executing the same tspec again without the additional keywords or filters
	 * @param pageSize - the request page Size
	 * @param currSize - the current page size
	 * @return ArrayList of products that were backfilled
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<Handle> doBackFill(int pageSize, int currSize){
		ArrayList<Handle> backFillProds = new ArrayList<Handle>();

		if (pageSize>0 && currSize<pageSize) {
			//do backfill by dropping the keyword query
            if (m_tspecId !=0 ) {
                m_tSpecQuery = (CNFQuery) TSpecQueryCache.getInstance().getCNFQuery(m_tspecId).clone();
            } else {
                //This is from the joz console.
                m_tSpecQuery = debugTSpecQuery;
            }
			//addProductTypeQuery(request.getOfferType());
			//randomize
			Handle ref = ProductDB.getInstance().genReference();
			m_tSpecQuery.setReference(ref);
			String requestCategory = request.getRequestCategory();
			if ((requestCategory!=null)&&(!"".equals(requestCategory.trim()))) {
				addRequestCategoryQuery(requestCategory);
			}
			if (m_geoFilterEnabled) {
				//For keyword queries with geo enabled or multivalue, do backfill by dropping the keyword query and
				// doing the geo query again
				m_ExternalKeywords =false;
				addGeoFilterQuery(pageSize-currSize, m_currPage);
				SortedSet<Handle> newResults = m_tSpecQuery.exec();
				//Sort by the score
				SortedSet<Handle> geoSortedResult = new SortedArraySet<Handle>(new ProductHandle(1.0, 1L));
				geoSortedResult.addAll(newResults);
				backFillProds.addAll(geoSortedResult);
			} else {
				//Never select any products that have Geo enabled while backfilling for keyword queries
//BUG 2556: Do not exclude geo enabled products
//				SimpleQuery geoEnabledQuery = createGeoEnabledQuery(false);
//				m_tSpecQuery.addSimpleQuery(geoEnabledQuery);
				//We default the pageSize to the difference we need plus 5 since we want to avoid any duplication of results
				int tmpSize = pageSize-currSize+5;
				m_tSpecQuery.setBounds(tmpSize,0);
				m_tSpecQuery.setStrict(false);
				SortedSet<Handle> newResults = m_tSpecQuery.exec();
				backFillProds.addAll(newResults);
			}
		}

		return backFillProds;
	}

	private void setCacheReference(ArrayList<Handle> newResults, CNFQuery cachedQuery) {
		Handle ph = newResults.get(newResults.size()-1);
		if(ph == null) {
			cachedQuery.setCacheReference(ph);
		} else {
			cachedQuery.setCacheReference(new ProductHandle(1.0,(ph.getOid()+1L)));
		}
	}

	/**
	 * Returns the sorted set of included products if the oSpec has included products
	 * @param tSpec - the tspec
	 * @return - the included prods
	 */
	private ArrayList<Handle> getIncludedProducts(TSpec tSpec) {
		ArrayList<Handle> prodsAL = new ArrayList<Handle>();
		List<ProductInfo> prodInfoList = tSpec.getIncludedProducts();
		if (prodInfoList!=null) {
			for (ProductInfo info : prodInfoList) {
				try {
					String productId = info.getName();
					if (productId != null) {
						if (productId.indexOf(".") > -1) {
							productId = productId.substring(productId.indexOf("."), productId.length());
						}
						char[] pidCharArr = productId.toCharArray();
						//Drop any non digit characters
						StringBuffer spid = new StringBuffer();
						for (char ch: pidCharArr) {
							if (Character.isDigit(ch)) {
								spid.append(ch);
							}
						}
						productId = spid.toString();
						Handle prodHandle = ProductDB.getInstance().getHandle(new Long(productId));
						if (prodHandle != null) {
							prodsAL.add(prodHandle);
						}

					}
				} catch(Exception e) {
					log.error("Could not get the product info from the Product DB");
					e.printStackTrace();
				}
			}
		}

		return prodsAL;
	}
	/**
	 * Perform tspec execution
	 * @return ArrayList of product handles
	 */
	private ArrayList<Handle> executeTSpec(){
		// Clone the query always
		m_tSpecQuery = (CNFQuery)m_tSpecQuery.clone();
		SortedSet<Handle> qResult;

		if (m_randomize) {
			Handle ref = ProductDB.getInstance().genReference ();
			m_tSpecQuery.setReference(ref);
		}

		if (request.getRequestKeyWords()!=null || request.isBMineUrls() || m_tspec.isMinePubUrl()) {
			String keywords = request.getRequestKeyWords();
			if (keywords==null) {
				keywords = "";
			}
			if (request.isBMineUrls() || m_tspec.isMinePubUrl()) {
				String urlSearch = doURLKeywordSearch(request.getUrl());
				if(urlSearch != null){
					keywords = keywords + " " + urlSearch;
				}
			}
			doKeywordSearch(keywords);
		}

		String requestCategory = request.getRequestCategory();
		if ((requestCategory!=null)&&(!"".equals(requestCategory))) {
			addRequestCategoryQuery(requestCategory);
		}

		ArrayList<Handle> resultAL = new ArrayList<Handle>();

		//5. Product Type
		//addProductTypeQuery(request.getOfferType());

		addExternalFilterRequestQueries(request);

		addGeoFilterQuery(m_pageSize, m_currPage);

		//6. Exec TSpec query
		qResult = m_tSpecQuery.exec();

		//If Geo Filtered, sort by score
//BUG 2897 - Do not sort by score for geo enabled queries.
//		if (m_geoFilterEnabled && !m_ExternalKeywords) {
//			SortedSet<Handle> geoSortedResult = new SortedArraySet<Handle>(new ProductHandle(1.0, 1L));
//			geoSortedResult.addAll(qResult);
//			qResult = geoSortedResult;
//		}

		resultAL.addAll(qResult);


		ArrayList<Handle> backFillProds = null;
        //Backfill only if needed
        if ((m_ExternalKeywords || m_ExternalFilters) && m_tspec.isEnableBackFill() && qResult!=null){
			backFillProds = doBackFill(request.getPageSize(),qResult.size());
		}

		//Now add any backfill, checking for duplicates
		if (backFillProds!=null && backFillProds.size()>0){
			for(Handle res: backFillProds) {
				if (!resultAL.contains(res)) {
					resultAL.add(res);
				}
			}
		}
		//Set the cached reference for randomization
		if (m_tSpecQuery.getReference() != null && resultAL.size() >0 ) {
			CNFQuery cachedQuery = TSpecQueryCache.getInstance().getCNFQuery(m_tspecId);
			setCacheReference(resultAL, cachedQuery);
		}
		//Cull the result by num products
		int numProds = request.getPageSize();
		if ((numProds > 0) && (resultAL.size() > numProds)){
			while(resultAL.size() > numProds){
				resultAL.remove(resultAL.size()-1);
			}
		}
		return resultAL;
	}

}
