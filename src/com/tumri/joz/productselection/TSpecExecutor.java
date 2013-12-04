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

import com.tumri.cma.domain.*;
import com.tumri.joz.ranks.GenericIWeight;
import com.tumri.joz.ranks.IWeight;
import com.tumri.joz.rules.ListingClause;
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
import com.tumri.joz.ranks.OptimizedWeight;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.IndexUtils;
import com.tumri.utils.data.SortedArraySet;
import com.tumri.utils.strings.StringTokenizer;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Class to execute a given tspec query. The assumption is that if the TSpec contains included products - then all other criteria
 * are ignored.
 *
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
	private String m_scriptKeywords = null;
	private static final Map<Product.Attribute, String> attMap = getAttributeMap();
	private Random r = new Random();
	private boolean doListingOpt = true;

	private static Logger log = Logger.getLogger(TSpecExecutor.class);

	public TSpecExecutor(ProductSelectionRequest req) {
		this.request = req;
		if(req.isDoProdOpt()){
			Experience e = CampaignDB.getInstance().getExperience(req.getExperienceId());

			if(e!=null && req.getUserbucket() > e.getListingOptPercent()){
				doListingOpt = false;
			}
		} else {
			doListingOpt = false;
		}
	}

	public TSpecExecutor(ProductSelectionRequest req, Features f) {
		this.request = req;
		this.m_feature = f;

		if(req.isDoProdOpt()){
			Experience e = CampaignDB.getInstance().getExperience(req.getExperienceId());

			if(e!=null && req.getUserbucket() > e.getListingOptPercent()){
				doListingOpt = false;
			}
		} else {
			doListingOpt = false;
		}
	}

	/**
	 * Process the query using the tspec Id, which is used to get teh query from teh cache
	 *
	 * @param tSpecId - the id
	 * @return - list of product handles
	 */
	public ArrayList<Handle> processQuery(int tSpecId) {
		m_tspecId = tSpecId;
		m_tspec = CampaignDB.getInstance().getTspec(tSpecId);

		if (m_tspec == null) {
			return null;
		}

		//Set the Features campaign client name here if needed.
		if (m_feature.getCampaignClientName() == null) {
			List<ProviderInfo> info = m_tspec.getIncludedProviders();
			if (info != null && info.size() > 0) {
				String providerName = info.get(0).getName();
				m_feature.setCampaignClientName(providerName);
			}
		}
		ArrayList<Handle> includedProds = getIncludedProducts(m_tspec);
		if (includedProds != null && includedProds.size() > 0) {
			return includedProds;
		}

		//Get the tSpec from the cache - note the tSpec id is used as the key in the TSpecQueryCache
		m_tSpecQuery = (CNFQuery) TSpecQueryCache.getInstance().getCNFQuery(tSpecId).clone();
		m_tSpecQuery.setUseTopK(request.isUseTopK(), m_tspec.getProdSelFuzzFactor());
		if (m_feature != null && request.isUseTopK()) {
			m_feature.addFeatureDetail(Features.FEATURE_TOPK, "true");
		}
		setupRequestParms();
		return executeTSpec();
	}

	/**
	 * Process the TSpec object that is passed in.
	 *
	 * @param tSpec - The tspec
	 * @return - list of handles
	 */
	public ArrayList<Handle> processQuery(TSpec tSpec) {

		m_tspec = tSpec;
		ArrayList<Handle> includedProds = getIncludedProducts(tSpec);
		if (includedProds != null && includedProds.size() > 0) {
			return includedProds;
		}
		//Get the tSpec from the cache - note the tSpec id is used as the key in the TSpecQueryCache
		m_tSpecQuery = TSpecQueryCacheHelper.getQuery(tSpec);
		m_tSpecQuery.setUseTopK(request.isUseTopK(), m_tspec.getProdSelFuzzFactor());
		//Fix to allow TSpec evaluator from Joz console to backfill correctly
		debugTSpecQuery = (CNFQuery) m_tSpecQuery.clone();
		debugTSpecQuery.setUseTopK(request.isUseTopK(), m_tspec.getProdSelFuzzFactor());
		setupRequestParms();
		return executeTSpec();
	}

	/**
	 * Replace comma with string for external filters. The reason for this is that comma is treated as a "Phrase" join term
	 * in lucene. Since multivalue filter fields have comma as a delimiter this could pose issues
	 *
	 * @param str
	 * @return
	 */
	private String cleanseKeywords(String str) {
		if (str != null) {
			str = str.replaceAll(",", " ");
		}
		return str;
	}

	/**
	 * Setup the request parameters for the TSpecExcecutor instance.
	 */
	private void setupRequestParms() {

		KeywordAttributeLookup.KWAttribute src = KeywordAttributeLookup.lookup(m_tspec.getKeywordSource());
		switch (src) {
			case S1:
				m_scriptKeywords = request.getRequestKeyWords();
				break;
			case F1:
				m_scriptKeywords = cleanseKeywords(request.getExternalFilterQuery1());
				break;
			case F2:
				m_scriptKeywords = cleanseKeywords(request.getExternalFilterQuery2());
				break;
			case F3:
				m_scriptKeywords = cleanseKeywords(request.getExternalFilterQuery3());
				break;
			case F4:
				m_scriptKeywords = cleanseKeywords(request.getExternalFilterQuery4());
				break;
			case F5:
				m_scriptKeywords = cleanseKeywords(request.getExternalFilterQuery5());
				break;
			case IGNORE:
				m_scriptKeywords = null;
				break;
			default:
				m_scriptKeywords = request.getRequestKeyWords();
				break;
		}

		if (m_scriptKeywords != null && !m_scriptKeywords.trim().equals("")) {
			m_ExternalKeywords = true;
		}

		m_geoFilterEnabled = m_tspec.isApplyGeoFilter();


		//Set defaults the current Page and page Size if they have not been specified
		if ((request.getCurrPage() == -1 && request.getPageSize() == -1)) {
			m_currPage = 0;
			m_pageSize = 0;
		}

		//Set pagination bounds for TSpec Query
		if (request.getCurrPage() > -1 && request.getPageSize() > -1) {
			m_currPage = request.getCurrPage();
			m_pageSize = request.getPageSize();
		}

		if (m_ExternalKeywords || request.isBPaginate()
				|| m_tspec.isMinePubUrl()) {
			//Do not randomize
			m_randomize = false;
		} else {
			m_tSpecQuery.setStrict(false);
			//Randomize
//			if (m_tspec.isEnableBackFill()) {
//				m_tSpecQuery.setStrict(false);
//			} else {
//				m_tSpecQuery.setStrict(true);
//			}
			m_randomize = true;
		}

		m_tSpecQuery.setBounds(m_pageSize, m_currPage);

	}

	/**
	 * Perform the keyword search
	 *
	 * @param keywords - input keywords
	 */
	private void doKeywordSearch(String keywords) {
		KeywordQuery sKwQuery;
		if ((keywords != null) && (!"".equals(keywords.trim()))) {
			String advertiser = null;
			m_ExternalKeywords = true;
			if (m_tspec.getIncludedProviders() != null && !m_tspec.getIncludedProviders().isEmpty()) {
				advertiser = m_tspec.getIncludedProviders().get(0).getName();
			}
			sKwQuery = new KeywordQuery(advertiser, keywords, false);
			m_tSpecQuery.addSimpleQuery(sKwQuery);
		}
		if (m_feature != null) {
			m_feature.addFeatureDetail(Features.FEATURE_SEARCH_KEYWORDS, keywords);
		}
	}

	private void addRequestCategoryQuery(String requestCategory) {
		ArrayList<Integer> catList = new ArrayList<Integer>();
		Integer catId = DictionaryManager.lookupId(IProduct.Attribute.kCategory, requestCategory);
		catList.add(catId);
		SimpleQuery catQuery = new AttributeQuery(IProduct.Attribute.kCategory, catList);
		CNFQuery copytSpecQuery = (CNFQuery) m_tSpecQuery.clone();
		copytSpecQuery.addSimpleQuery(catQuery);
		m_tSpecQuery = null;
		m_tSpecQuery = copytSpecQuery;
	}

	private SimpleQuery createGeoEnabledQuery(boolean bGeoEnabled) {
		Integer geoFlagId = DictionaryManager.lookupId(Product.Attribute.kGeoEnabledFlag, bGeoEnabled ? "true" : "false");
		return new AttributeQuery(Product.Attribute.kGeoEnabledFlag, geoFlagId);
	}

	private void addListingClauseQueries() {
		ListingClause clause = request.getListingClause();
		if (clause != null && m_tspec.isAllowListingOptimization()) {
			addLCQuery(clause, IProduct.Attribute.kCategory);
			addLCQuery(clause, IProduct.Attribute.kBrand);
			addLCQuery(clause, IProduct.Attribute.kSupplier);
			addLCQuery(clause, IProduct.Attribute.kGlobalId);
			addLCQuery(clause, IProduct.Attribute.kKeywords);
			addLCQuery(clause, IProduct.Attribute.kId);
		}
	}

	/**
	 * get the list of simple queries that correspond to the entries in the listing clause
	 *
	 * @return
	 */
	public void addLCQuery(ListingClause lc, IProduct.Attribute attr) {
		Set<String> values = lc.getListingClause(attr.name());
		if (attr == IProduct.Attribute.kId) {
			if (values != null && !values.isEmpty()) {
				ArrayList<Long> pidList = new ArrayList<Long>();
				for (String productId : values) {
					if (productId.indexOf(".") > -1) {
						productId = productId.substring(productId.indexOf("."), productId.length());
					}
					char[] pidCharArr = productId.toCharArray();
					//Drop any non digit characters
					StringBuffer spid = new StringBuffer();
					for (char ch : pidCharArr) {
						if (Character.isDigit(ch)) {
							spid.append(ch);
						}
					}

					productId = spid.toString();
					pidList.add(new Long(productId));
				}
				if (!pidList.isEmpty()) {
					m_feature.addFeatureDetail(attr.toString(), pidList.toString());
					SimpleQuery catQuery = new IncludedProductQuery(pidList);
					catQuery.setWeight(OptimizedWeight.getInstance());
					CNFQuery copytSpecQuery = (CNFQuery) m_tSpecQuery.clone();
					copytSpecQuery.addSimpleQuery(catQuery);
					m_tSpecQuery = copytSpecQuery;
				}
			}

		} else {
			if (values != null && !values.isEmpty()) {
				ArrayList<Integer> catList = new ArrayList<Integer>();
				ArrayList<String> validValues = new ArrayList<String>();
				for (String cat : values) {
					Integer catId = DictionaryManager.lookupId(attr, cat);
					if (catId != null) {
						validValues.add(cat);
						catList.add(catId);
					}
				}
				if (!validValues.isEmpty()) {
					m_feature.addFeatureDetail(attr.toString(), validValues.toString());
				}
				SimpleQuery catQuery = new AttributeQuery(attr, catList);
				catQuery.setWeight(OptimizedWeight.getInstance());
				CNFQuery copytSpecQuery = (CNFQuery) m_tSpecQuery.clone();
				copytSpecQuery.addSimpleQuery(catQuery);
				m_tSpecQuery = copytSpecQuery;
			}
		}
	}

	private void addGeoFilterQuery(int pageSize, int currPage) {
		//If there are no queries in the selected tspec - do not add geo flag
		boolean bSimpleQueries = false;
		ArrayList<ConjunctQuery> _conjQueryAL = m_tSpecQuery.getQueries();
		for (ConjunctQuery conjQuery : _conjQueryAL) {
			ArrayList<SimpleQuery> simpleQueryAL = conjQuery.getQueries();
			if (simpleQueryAL.size() != 0) {
				bSimpleQueries = true;
				break;
			}
		}
		if (!bSimpleQueries) {
			return;
		}

		if (m_geoFilterEnabled) {
			String zipCode = request.getZipCode();
			if (zipCode != null && !"".equals(zipCode)) {
				if (m_feature != null) {
					m_feature.addFeatureDetail("Zip", zipCode);
				}
			}
			String cityCode = request.getCityCode();
			if (cityCode != null && !"".equals(cityCode)) {
				if (m_feature != null) {
					m_feature.addFeatureDetail("City", cityCode);
				}
			}
			String dmaCode = request.getDmaCode();
			if (dmaCode != null && !"".equals(dmaCode)) {
				if (m_feature != null) {
					m_feature.addFeatureDetail("DMA", dmaCode);
				}

			}
			String areaCode = request.getAreaCode();
			if (areaCode != null && !"".equals(areaCode)) {
				if (m_feature != null) {
					m_feature.addFeatureDetail("Area", areaCode);
				}
			}
			String stateCode = request.getStateCode();
			if (stateCode != null && !"".equals(stateCode)) {
				if (m_feature != null) {
					m_feature.addFeatureDetail("State", stateCode);
				}
			}
			String countryCode = request.getCountryCode();
			if (countryCode != null && !"".equals(countryCode)) {
				if (m_feature != null) {
					m_feature.addFeatureDetail("Country", countryCode);
				}

			}
			CNFQuery geoTSpecQuery = new CNFQuery();
			geoTSpecQuery.setUseTopK(request.isUseTopK(), m_tspec.getProdSelFuzzFactor());
			geoTSpecQuery.setBounds(pageSize, currPage);
			_conjQueryAL = m_tSpecQuery.getQueries();
			for (ConjunctQuery conjQuery : _conjQueryAL) {
				if (zipCode != null && !"".equals(zipCode)) {
					ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kZip, zipCode);
					if (cloneConjQuery != null) {
						geoTSpecQuery.addQuery(cloneConjQuery);
					}
					ConjunctQuery radiuscloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kRadius, zipCode);
					if (radiuscloneConjQuery != null) {
						geoTSpecQuery.addQuery(radiuscloneConjQuery);
					}
				}
				if (cityCode != null && !"".equals(cityCode)) {
					ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kCity, cityCode);
					if (cloneConjQuery != null) {
						geoTSpecQuery.addQuery(cloneConjQuery);
					}
				}
				if (dmaCode != null && !"".equals(dmaCode)) {
					ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kDMA, dmaCode);
					if (cloneConjQuery != null) {
						geoTSpecQuery.addQuery(cloneConjQuery);
					}
				}
				if (areaCode != null && !"".equals(areaCode)) {
					ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kArea, areaCode);
					if (cloneConjQuery != null) {
						geoTSpecQuery.addQuery(cloneConjQuery);
					}

				}
				if (stateCode != null && !"".equals(stateCode)) {
					ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kState, stateCode);
					if (cloneConjQuery != null) {
						geoTSpecQuery.addQuery(cloneConjQuery);
					}
				}
				if (countryCode != null && !"".equals(countryCode)) {
					ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kCountry, countryCode);
					if (cloneConjQuery != null) {
						geoTSpecQuery.addQuery(cloneConjQuery);
					}
				}
				//Add the backfill query
				{
					ConjunctQuery cloneConjQuery = (ConjunctQuery) conjQuery.clone();
					SimpleQuery geoEnabledQuery = createGeoEnabledQuery(false);
					cloneConjQuery.setBounds(pageSize, currPage);
					cloneConjQuery.addQuery(geoEnabledQuery);
					geoTSpecQuery.addQuery(cloneConjQuery);
				}
			}
			if (m_feature != null) {
				m_feature.setGeoUsed(true);
			}
			if (!m_ExternalKeywords) {
				//Set a reference so we return random selection of products.
				geoTSpecQuery.setCacheReference(m_tSpecQuery.getCacheReference());
				geoTSpecQuery.setReference(ProductDB.getInstance().genReference());
			}
			m_tSpecQuery = geoTSpecQuery;
		} else {
			//BUG2556: Do not exclude Geo Enabled products for non geo enabled queries.
			//m_tSpecQuery.addSimpleQuery(createGeoEnabledQuery(false));
		}
	}

	private ConjunctQuery cloneAndAddQuery(ConjunctQuery conjQuery, Product.Attribute kAttr, String val) {
		ConjunctQuery cloneConjQuery = (ConjunctQuery) conjQuery.clone();

		if (kAttr == IProduct.Attribute.kRadius) {
			int rad = m_tspec.getRadius();
			if (!m_tspec.isUseRadiusQuery() || m_tspec.getRadius() == 0) {
				return null;
			}
			LatLongQuery latQuery = new LatLongQuery(IProduct.Attribute.kLatitude, val, rad);
			LatLongQuery longQuery = new LatLongQuery(IProduct.Attribute.kLongitude, val, rad);
			cloneConjQuery.addQuery(latQuery);
			cloneConjQuery.addQuery(longQuery);
		} else {
			Integer codeId = DictionaryManager.lookupId(kAttr, val);
			AttributeQuery aQuery = new AttributeQuery(kAttr, codeId);
			cloneConjQuery.addQuery(aQuery);
		}
		cloneConjQuery.setBounds(m_pageSize, m_currPage);
		return cloneConjQuery;
	}

	/**
	 * Inspects the request and adds multivalue delim fields for Product Selection.
	 *
	 * @param request - the request
	 */
	private void addExternalFilterRequestQueries(ProductSelectionRequest request) {
		CNFQuery copytSpecQuery = null;
		if (m_tspec.isUseListingFilter1()) {
			String externalFilterField1 = request.getExternalFilterQuery1();
			if (externalFilterField1 != null && !externalFilterField1.equals("")) {
				addExternalFilterFieldQuery(IProduct.Attribute.kExternalFilterField1, externalFilterField1, copytSpecQuery);
				if (m_feature != null) {
					m_feature.addFeatureDetail("ExternalFilterField1", externalFilterField1);
				}
				if(doListingOpt){
					addOptimizedExternalFilterFieldQuery(request.getExperienceId(), IProduct.Attribute.kExperienceIdF1, externalFilterField1, copytSpecQuery);
				}
			}
		}
		if (m_tspec.isUseListingFilter2()) {
			String externalFilterField2 = request.getExternalFilterQuery2();
			if (externalFilterField2 != null && !externalFilterField2.equals("")) {
				addExternalFilterFieldQuery(IProduct.Attribute.kExternalFilterField2, externalFilterField2, copytSpecQuery);
				if (m_feature != null) {
					m_feature.addFeatureDetail("ExternalFilterField2", externalFilterField2);
				}
				if(doListingOpt){
					addOptimizedExternalFilterFieldQuery(request.getExperienceId(), IProduct.Attribute.kExperienceIdF2, externalFilterField2, copytSpecQuery);
				}
			}
		}
		if (m_tspec.isUseListingFilter3()) {
			String externalFilterField3 = request.getExternalFilterQuery3();
			if (externalFilterField3 != null && !externalFilterField3.equals("")) {
				addExternalFilterFieldQuery(IProduct.Attribute.kExternalFilterField3, externalFilterField3, copytSpecQuery);
				if (m_feature != null) {
					m_feature.addFeatureDetail("ExternalFilterField3", externalFilterField3);
				}
				if(doListingOpt){
					addOptimizedExternalFilterFieldQuery(request.getExperienceId(), IProduct.Attribute.kExperienceIdF3, externalFilterField3, copytSpecQuery);
				}
			}
		}
		if (m_tspec.isUseListingFilter4()) {
			String externalFilterField4 = request.getExternalFilterQuery4();
			if (externalFilterField4 != null && !externalFilterField4.equals("")) {
				addExternalFilterFieldQuery(IProduct.Attribute.kExternalFilterField4, externalFilterField4, copytSpecQuery);
				if (m_feature != null) {
					m_feature.addFeatureDetail("ExternalFilterField4", externalFilterField4);
				}
				if(doListingOpt){
					addOptimizedExternalFilterFieldQuery(request.getExperienceId(), IProduct.Attribute.kExperienceIdF4, externalFilterField4, copytSpecQuery);
				}
			}
		}
		if (m_tspec.isUseListingFilter5()) {
			String externalFilterField5 = request.getExternalFilterQuery5();
			if (externalFilterField5 != null && !externalFilterField5.equals("")) {
				addExternalFilterFieldQuery(IProduct.Attribute.kExternalFilterField5, externalFilterField5, copytSpecQuery);
				if (m_feature != null) {
					m_feature.addFeatureDetail("ExternalFilterField5", externalFilterField5);
				}
				if(doListingOpt){
					addOptimizedExternalFilterFieldQuery(request.getExperienceId(), IProduct.Attribute.kExperienceIdF5, externalFilterField5, copytSpecQuery);
				}

			}
		}
		if (m_tspec.isUseAgeFilter()) {
			String ageFilter = request.getAge();
			if (ageFilter != null && !ageFilter.equals("")) {
				addAdditionalQuery(IProduct.Attribute.kAge, ageFilter, copytSpecQuery);
				if (m_feature != null) {
					m_feature.addFeatureDetail("age", ageFilter);
				}
			}
		}
		if (m_tspec.isUseGenderFilter()) {
			String genderFilter = request.getGender();
			if (genderFilter != null && !genderFilter.equals("")) {
				addAdditionalQuery(IProduct.Attribute.kGender, genderFilter, copytSpecQuery);
				if (m_feature != null) {
					m_feature.addFeatureDetail("gender", genderFilter);
				}
			}
		}
		if (m_tspec.isUseBTFilter()) {
			String btFilter = request.getBt();
			if (btFilter != null && !btFilter.equals("")) {
				addAdditionalQuery(IProduct.Attribute.kBT, btFilter, copytSpecQuery);
				if (m_feature != null) {
					m_feature.addFeatureDetail("bt", btFilter);
				}
			}
		}
		if (m_tspec.isUseMSFilter()) {
			String msFilter = request.getCc();
			if (msFilter != null && !msFilter.equals("")) {
				addAdditionalQuery(IProduct.Attribute.kCC, msFilter, copytSpecQuery);
				if (m_feature != null) {
					m_feature.addFeatureDetail("ms", msFilter);
				}
			}
		}
		if (m_tspec.isUseHHIFilter()) {
			String hhiFilter = request.getHhi();
			if (hhiFilter != null && !hhiFilter.equals("")) {
				addAdditionalQuery(IProduct.Attribute.kHHI, hhiFilter, copytSpecQuery);
				if (m_feature != null) {
					m_feature.addFeatureDetail("hhi", hhiFilter);
				}
			}
		}
		if (m_tspec.isUseUT1()) {
			String ut1Filter = request.getUt1();
			if (ut1Filter != null && !ut1Filter.equals("")) {
				addExternalFilterFieldQuery(IProduct.Attribute.kUT1, ut1Filter, copytSpecQuery);
				if (m_feature != null) {
					m_feature.addFeatureDetail("ut1", ut1Filter);
				}
				if(doListingOpt){
					addOptimizedExternalFilterFieldQuery(request.getExperienceId(), IProduct.Attribute.kExperienceIdUT1, ut1Filter, copytSpecQuery);
				}
			}
		}
		if (m_tspec.isUseUT2()) {
			String ut2Filter = request.getUt2();
			if (ut2Filter != null && !ut2Filter.equals("")) {
				addExternalFilterFieldQuery(IProduct.Attribute.kUT2, ut2Filter, copytSpecQuery);
				if (m_feature != null) {
					m_feature.addFeatureDetail("ut2", ut2Filter);
				}
				if(doListingOpt){
					addOptimizedExternalFilterFieldQuery(request.getExperienceId(), IProduct.Attribute.kExperienceIdUT2, ut2Filter, copytSpecQuery);
				}
			}
		}
		if (m_tspec.isUseUT3()) {
			String ut3Filter = request.getUt3();
			if (ut3Filter != null && !ut3Filter.equals("")) {
				addExternalFilterFieldQuery(IProduct.Attribute.kUT3, ut3Filter, copytSpecQuery);
				if (m_feature != null) {
					m_feature.addFeatureDetail("ut3", ut3Filter);
				}
				if(doListingOpt){
					addOptimizedExternalFilterFieldQuery(request.getExperienceId(), IProduct.Attribute.kExperienceIdUT3, ut3Filter, copytSpecQuery);
				}
			}
		}
		if (m_tspec.isUseUT4()) {
			String ut4Filter = request.getUt4();
			if (ut4Filter != null && !ut4Filter.equals("")) {
				addExternalFilterFieldQuery(IProduct.Attribute.kUT4, ut4Filter, copytSpecQuery);
				if (m_feature != null) {
					m_feature.addFeatureDetail("ut4", ut4Filter);
				}
				if(doListingOpt){
					addOptimizedExternalFilterFieldQuery(request.getExperienceId(), IProduct.Attribute.kExperienceIdUT4, ut4Filter, copytSpecQuery);
				}
			}
		}
		if (m_tspec.isUseUT5()) {
			String ut5Filter = request.getUt5();
			if (ut5Filter != null && !ut5Filter.equals("")) {
				addExternalFilterFieldQuery(IProduct.Attribute.kUT5, ut5Filter, copytSpecQuery);
				if (m_feature != null) {
					m_feature.addFeatureDetail("ut5", ut5Filter);
				}
				if(doListingOpt){
					addOptimizedExternalFilterFieldQuery(request.getExperienceId(), IProduct.Attribute.kExperienceIdUT5, ut5Filter, copytSpecQuery);
				}
			}
		}

	}

	/**
	 * Adds a multi value field query to the current tspec being executed
	 *
	 * @param kAttr               - Product Attribute
	 * @param externalFilterField - The multi value field passed from iCS
	 */
	private void addExternalFilterFieldQuery(IProduct.Attribute kAttr, String externalFilterField, CNFQuery copytSpecQuery) {
		StringTokenizer st = new StringTokenizer(externalFilterField, MULTI_VALUE_DELIM);
		ArrayList<String> multiValueAL = st.getTokens();
		ArrayList<Long> multiValueIdAL = new ArrayList<Long>();
		for (String val : multiValueAL) {
			//Url decode
			if (val == null) {
				continue;
			}
			try {
				val = URLDecoder.decode(val, "utf-8");
				val = val.toLowerCase();
			} catch (UnsupportedEncodingException e) {
				log.error("Could not decode the value : " + val);
				continue;
			} catch (IllegalArgumentException ilegalArgEx) {
				log.error("Skipping value that cannot be decoded : " + val);
				continue;
			}
			Integer fieldId = DictionaryManager.lookupId(kAttr, val); //todo: perhaps use same dictionary for F1/ut1/tspecf1/tspecut1
			Long key = null;
			if (fieldId != null) { //KEY!!  this is done to prevent f1 from impacting f2 from f3...ut5
				key = IndexUtils.createLongIndexKey(kAttr, fieldId);
			}
			multiValueIdAL.add(key);
		}
		if (multiValueIdAL.isEmpty()) {
			return;
		}
		m_ExternalFilters = true;
		LongTextQuery aQuery = new LongTextQuery(IProduct.Attribute.kMultiValueTextField, multiValueIdAL);
		IWeight<Handle> wt = findWeight(kAttr);
		if (wt != null) {
			aQuery.setWeight(wt);
		}
		if (copytSpecQuery == null) {
			copytSpecQuery = (CNFQuery) m_tSpecQuery.clone();
		}
		ArrayList<ConjunctQuery> cnjQueries = copytSpecQuery.getQueries();
		for (ConjunctQuery conjunctQuery : cnjQueries) {
			conjunctQuery.addQuery(aQuery);
		}
		m_tSpecQuery = copytSpecQuery;
	}

	/**
	 * Adds a multi value field query to the current tspec being executed
	 *
	 * @param kAttr               - Product Attribute
	 * @param externalFilterField - The multi value field passed from iCS
	 */
	private void addOptimizedExternalFilterFieldQuery(int experienceId, IProduct.Attribute kAttr, String externalFilterField, CNFQuery copytSpecQuery) {
		StringTokenizer st = new StringTokenizer(externalFilterField, MULTI_VALUE_DELIM);
		ArrayList<String> multiValueAL = st.getTokens();
		ArrayList<Integer> multiValueIdAL = new ArrayList<Integer>();
		for (String val : multiValueAL) {
			//Url decode
			if (val == null) {
				continue;
			}
			try {
				val = URLDecoder.decode(val, "utf-8");
				val = val.toLowerCase();
			} catch (UnsupportedEncodingException e) {
				log.error("Could not decode the value : " + val);
				continue;
			} catch (IllegalArgumentException ilegalArgEx) {
				log.error("Skipping value that cannot be decoded : " + val);
				continue;
			}
			Integer fieldId = DictionaryManager.lookupId(kAttr, val); //todo: perhaps use same dictionary for F1/ut1/tspecf1/tspecut1
			multiValueIdAL.add(fieldId);
		}
		if (multiValueIdAL.isEmpty()) {
			return;
		}

		OptTextQuery aQuery = new OptTextQuery(kAttr, experienceId,multiValueIdAL);

		IWeight<Handle> wt = findOptWeight(kAttr);
		if (wt != null) {
			aQuery.setWeight(wt);
		}
		if (copytSpecQuery == null) {
			copytSpecQuery = (CNFQuery) m_tSpecQuery.clone();
		}
		ArrayList<ConjunctQuery> cnjQueries = copytSpecQuery.getQueries();
		for (ConjunctQuery conjunctQuery : cnjQueries) {
			conjunctQuery.addQuery(aQuery);
		}
		m_tSpecQuery = copytSpecQuery;
	}

	private IWeight<Handle> findOptWeight(IProduct.Attribute kAttr){
		IWeight<Handle> wt = null;
		double finalScore = 1.00001;
		if(kAttr == IProduct.Attribute.kExperienceId){    //should be lowest in priority order
			finalScore = 1.000001;
		} else if(kAttr == IProduct.Attribute.kExperienceIdF1){
			Map<String, Double> specifiedWeights = m_tspec.getProdSelWeights();
			Double score = null;
			if (specifiedWeights != null && (score = specifiedWeights.get(attMap.get(IProduct.Attribute.kExternalFilterField1))) != null) {
				double newScore = score - (score.intValue());
				newScore = newScore/10000;
				newScore += 1;
				score = newScore;
			} else {
				score = 1.00002;
			}
			finalScore = score;
		} else if(kAttr == IProduct.Attribute.kExperienceIdF2){
			Map<String, Double> specifiedWeights = m_tspec.getProdSelWeights();
			Double score = null;
			if (specifiedWeights != null && (score = specifiedWeights.get(attMap.get(IProduct.Attribute.kExternalFilterField2))) != null) {
				double newScore = score - (score.intValue());
				newScore = newScore/10000;
				newScore += 1;
				score = newScore;
			} else {
				score = 1.00002;
			}
			finalScore = score;
		} else if(kAttr == IProduct.Attribute.kExperienceIdF3){
			Map<String, Double> specifiedWeights = m_tspec.getProdSelWeights();
			Double score = null;
			if (specifiedWeights != null && (score = specifiedWeights.get(attMap.get(IProduct.Attribute.kExternalFilterField3))) != null) {
				double newScore = score - (score.intValue());
				newScore = newScore/10000;
				newScore += 1;
				score = newScore;
			} else {
				score = 1.00002;
			}
			finalScore = score;
		} else if(kAttr == IProduct.Attribute.kExperienceIdF4){
			Map<String, Double> specifiedWeights = m_tspec.getProdSelWeights();
			Double score = null;
			if (specifiedWeights != null && (score = specifiedWeights.get(attMap.get(IProduct.Attribute.kExternalFilterField4))) != null) {
				double newScore = score - (score.intValue());
				newScore = newScore/10000;
				newScore += 1;
				score = newScore;
			} else {
				score = 1.00002;
			}
			finalScore = score;
		} else if(kAttr == IProduct.Attribute.kExperienceIdF5){
			Map<String, Double> specifiedWeights = m_tspec.getProdSelWeights();
			Double score = null;
			if (specifiedWeights != null && (score = specifiedWeights.get(attMap.get(IProduct.Attribute.kExternalFilterField5))) != null) {
				double newScore = score - (score.intValue());
				newScore = newScore/10000;
				newScore += 1;
				score = newScore;
			} else {
				score = 1.00002;
			}
			finalScore = score;
		} else if(kAttr == IProduct.Attribute.kExperienceIdUT1){
			Map<String, Double> specifiedWeights = m_tspec.getProdSelWeights();
			Double score = null;
			if (specifiedWeights != null && (score = specifiedWeights.get(attMap.get(IProduct.Attribute.kUT1))) != null) {
				double newScore = score - (score.intValue());
				newScore = newScore/10000;
				newScore += 1;
				score = newScore;
			} else {
				score = 1.00002;
			}
			finalScore = score;
		} else if(kAttr == IProduct.Attribute.kExperienceIdUT2){
			Map<String, Double> specifiedWeights = m_tspec.getProdSelWeights();
			Double score = null;
			if (specifiedWeights != null && (score = specifiedWeights.get(attMap.get(IProduct.Attribute.kUT2))) != null) {
				double newScore = score - (score.intValue());
				newScore = newScore/10000;
				newScore += 1;
				score = newScore;
			} else {
				score = 1.00002;
			}
			finalScore = score;
		} else if(kAttr == IProduct.Attribute.kExperienceIdUT3){
			Map<String, Double> specifiedWeights = m_tspec.getProdSelWeights();
			Double score = null;
			if (specifiedWeights != null && (score = specifiedWeights.get(attMap.get(IProduct.Attribute.kUT3))) != null) {
				double newScore = score - (score.intValue());
				newScore = newScore/10000;
				newScore += 1;
				score = newScore;
			} else {
				score = 1.00002;
			}
			finalScore = score;
		} else if(kAttr == IProduct.Attribute.kExperienceIdUT4){
			Map<String, Double> specifiedWeights = m_tspec.getProdSelWeights();
			Double score = null;
			if (specifiedWeights != null && (score = specifiedWeights.get(attMap.get(IProduct.Attribute.kUT4))) != null) {
				double newScore = score - (score.intValue());
				newScore = newScore/10000;
				newScore += 1;
				score = newScore;
			} else {
				score = 1.00002;
			}
			finalScore = score;
		} else if(kAttr == IProduct.Attribute.kExperienceIdUT5){
			Map<String, Double> specifiedWeights = m_tspec.getProdSelWeights();
			Double score = null;
			if (specifiedWeights != null && (score = specifiedWeights.get(attMap.get(IProduct.Attribute.kUT5))) != null) {
				double newScore = score - (score.intValue());
				newScore = newScore/10000;
				newScore += 1;
				score = newScore;
			} else {
				score = 1.00002;
			}
			finalScore = score;
		}
		return new GenericIWeight<Handle>(finalScore, false);
	}

	IWeight<Handle> findWeight(IProduct.Attribute kAttr) {
		IWeight<Handle> wt = null;

		Map<String, Double> specifiedWeights = m_tspec.getProdSelWeights();
		String tAtt = attMap.get(kAttr);
		Double score = null;
		boolean isStrict = !m_tspec.isEnableBackFill();
		if (specifiedWeights != null && (score = specifiedWeights.get(tAtt)) != null) {
			wt = new GenericIWeight<Handle>(score, isStrict);
		}

		return wt;
	}

	/**
	 * Adds a multi value field query to the current tspec being executed
	 *
	 * @param kAttr - Product Attribute
	 * @param value - The multi value field passed from iCS
	 */
	private void addAdditionalQuery(IProduct.Attribute kAttr, String value, CNFQuery copytSpecQuery) {
		StringTokenizer st = new StringTokenizer(value, MULTI_VALUE_DELIM);
		ArrayList<String> multiValueAL = st.getTokens();
		ArrayList<Integer> multiValueIdAL = new ArrayList<Integer>();
		for (String val : multiValueAL) {
			//Url decode
			if (val == null) {
				continue;
			}
			try {
				val = URLDecoder.decode(val, "utf-8");
				val = val.toLowerCase();
			} catch (UnsupportedEncodingException e) {
				log.error("Could not decode the value : " + val);
				continue;
			} catch (IllegalArgumentException ilegalArgEx) {
				log.error("Skipping value that cannot be decoded : " + val);
				continue;
			}
			Integer fieldId = DictionaryManager.lookupId(kAttr, val);
			multiValueIdAL.add(fieldId);
		}
		if (multiValueIdAL.isEmpty()) {
			return;
		}
		m_ExternalFilters = true;
		SimpleQuery aQuery = new AttributeQuery(kAttr, multiValueIdAL);
		if (copytSpecQuery == null) {
			copytSpecQuery = (CNFQuery) m_tSpecQuery.clone();
		}
		ArrayList<ConjunctQuery> cnjQueries = copytSpecQuery.getQueries();
		for (ConjunctQuery conjunctQuery : cnjQueries) {
			conjunctQuery.addQuery(aQuery);
		}
		m_tSpecQuery = copytSpecQuery;
	}

	/**
	 * Performs the URL scavenging and runs the query
	 *
	 * @param url - input Ad Data request
	 * @return keywords
	 */
	private String doURLKeywordSearch(String url) {
		if (url == null || url.equals("")) {
			return null;
		}
		String queryNames = "";
		String stopWords = "";
		//Get the queryNames and Stopwords
		String tmpqueryNames = m_tspec.getPublicURLQueryNames();
		String tmpstopWords = m_tspec.getPublicUrlStopWords();
		if (tmpqueryNames != null) {
			queryNames = queryNames + " " + tmpqueryNames;
		}
		if (tmpstopWords != null) {
			stopWords = stopWords + " " + tmpstopWords;
		}
		return URLScavenger.mineKeywords(url, stopWords, queryNames);
	}

	/**
	 * Perform the backfill of products only in the case of external keywords or filters
	 * Backfill is merely executing the same tspec again without the additional keywords or filters
	 *
	 * @param pageSize - the request page Size
	 * @param currSize - the current page size
	 * @return ArrayList of products that were backfilled
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<Handle> doBackFill(int pageSize, int currSize) {
		ArrayList<Handle> backFillProds = new ArrayList<Handle>();

		if (pageSize > 0 && currSize < pageSize) {
			//do backfill by dropping the keyword query
			if (m_tspecId != 0) {
				m_tSpecQuery = (CNFQuery) TSpecQueryCache.getInstance().getCNFQuery(m_tspecId).clone();
			} else {
				//This is from the joz console.
				m_tSpecQuery = debugTSpecQuery;
			}
			m_tSpecQuery.setUseTopK(request.isUseTopK(), m_tspec.getProdSelFuzzFactor());
			//addProductTypeQuery(request.getOfferType());
			//randomize
			Handle ref = ProductDB.getInstance().genReference();
			m_tSpecQuery.setReference(ref);
			String requestCategory = request.getRequestCategory();
			if ((requestCategory != null) && (!"".equals(requestCategory.trim()))) {
				addRequestCategoryQuery(requestCategory);
			}
			if (m_geoFilterEnabled) {
				//For keyword queries with geo enabled or multivalue, do backfill by dropping the keyword query and
				// doing the geo query again
				m_ExternalKeywords = false;
				addGeoFilterQuery(pageSize + 1, m_currPage);
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
				int tmpSize = pageSize + 1;
				m_tSpecQuery.setBounds(tmpSize, 0);
				m_tSpecQuery.setStrict(false);
				SortedSet<Handle> newResults = m_tSpecQuery.exec();
				backFillProds.addAll(newResults);
			}
		}

		return backFillProds;
	}

	private void setCacheReference(Handle handle, CNFQuery cachedQuery) {
		if (handle == null) {
			cachedQuery.setCacheReference(handle);
		} else {
			cachedQuery.setCacheReference(new ProductHandle(1.0, (handle.getOid() + 1L)));
		}
	}

	/**
	 * Returns the sorted set of included products if the oSpec has included products
	 *
	 * @param tSpec - the tspec
	 * @return - the included prods
	 */
	private ArrayList<Handle> getIncludedProducts(TSpec tSpec) {
		ArrayList<Handle> prodsAL = new ArrayList<Handle>();
		List<ProductInfo> prodInfoList = tSpec.getIncludedProducts();
		if (prodInfoList != null) {
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
						for (char ch : pidCharArr) {
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
				} catch (Exception e) {
					log.error("Could not get the product info from the Product DB");
					e.printStackTrace();
				}
			}
		}

		return prodsAL;
	}

	/**
	 * Check if the tspec has these filters turned on, and add necessary filters to the execution
	 */
	private void handleRankAndDiscountFilters() {
		boolean bClone = false;
		CNFQuery copytSpecQuery = null;
		if (m_tspec.isSortByDiscount()) {
			bClone = true;
			copytSpecQuery = (CNFQuery) m_tSpecQuery.clone();
			SimpleQuery aQuery = new AttributeQuery(IProduct.Attribute.kDiscount, 0);
			copytSpecQuery.addSimpleQuery(aQuery);
			if (m_feature != null) {
				m_feature.addFeatureDetail(Features.FEATURE_DISCOUNTSORT, "true");
			}
		}
		if (m_tspec.isSortByRank()) {
			if (!bClone) {
				copytSpecQuery = (CNFQuery) m_tSpecQuery.clone();
				bClone = true;
			}
			SimpleQuery aQuery = new AttributeQuery(IProduct.Attribute.kRank, 0);
			copytSpecQuery.addSimpleQuery(aQuery);
			if (m_feature != null) {
				m_feature.addFeatureDetail(Features.FEATURE_RANKSORT, "true");
			}
		}
		if (bClone) {
			m_tSpecQuery = copytSpecQuery;
		}

	}

	/**
	 * Perform tspec execution
	 *
	 * @return ArrayList of product handles
	 */
	private ArrayList<Handle> executeTSpec() {
		// Clone the query always
		ArrayList<Handle> resultAL = new ArrayList<Handle>();
		int numProds = request.getPageSize();
		if (numProds > 0) {
			m_tSpecQuery = (CNFQuery) m_tSpecQuery.clone();
			SortedSet<Handle> qResult;

			if (m_randomize) {
				Handle ref = ProductDB.getInstance().genReference();
				m_tSpecQuery.setReference(ref);
			}

			if (m_scriptKeywords != null || request.isBMineUrls() || m_tspec.isMinePubUrl()) {
				String keywords = m_scriptKeywords;
				if (keywords == null) {
					keywords = "";
				}
				if (request.isBMineUrls() || m_tspec.isMinePubUrl()) {
					String urlSearch = doURLKeywordSearch(request.getUrl());
					if (urlSearch != null) {
						keywords = keywords + " " + urlSearch;
					}
				}
				doKeywordSearch(keywords);
			}

			String requestCategory = request.getRequestCategory();
			if ((requestCategory != null) && (!"".equals(requestCategory))) {
				addRequestCategoryQuery(requestCategory);
			}

			//Add Listing Clause only if there are no backfills and there are no external keywords passed in.
			if (!m_ExternalKeywords && m_tspec.isEnableBackFill()) {
				addListingClauseQueries();
			}


			//5. Product Type
			//addProductTypeQuery(request.getOfferType());
			if(doListingOpt){
				addOptimizedExternalFilterFieldQuery(request.getExperienceId(), IProduct.Attribute.kExperienceId, request.getExperienceId()+"", null);
			}

			addExternalFilterRequestQueries(request);

			addGeoFilterQuery(m_pageSize, m_currPage);

			handleRankAndDiscountFilters();

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


			//todo: get rid of backfill
			ArrayList<Handle> backFillProds = null;
			//Backfill only if needed
			if ((m_ExternalKeywords || m_ExternalFilters) && m_tspec.isEnableBackFill() && qResult != null) {
				backFillProds = doBackFill(request.getPageSize(), qResult.size());
			}

			//Now add any backfill, checking for duplicates
			if (backFillProds != null && backFillProds.size() > 0) {
				for (Handle res : backFillProds) {
					if (!resultAL.contains(res)) {
						resultAL.add(res);
					}
				}
			}

			//Cull the result by num products
			for(int i = (resultAL.size() - 1); i >= numProds; i--){
				resultAL.remove(i);
			}

			if(resultAL.size() > 0){
				// need to 'group' by score...only randomize within groups of equal scores,
				// keeping overall order of scores in order
				ArrayList<Integer> newScoreEndIndexes = new ArrayList<Integer>();
				Double prevScore = resultAL.get(0).getScore();
				for(int i = 0; i < resultAL.size(); i++){
					Double currentScore = resultAL.get(i).getScore();
					double diff = currentScore - prevScore;
					if(diff > 0.000000000001 || diff < -0.000000000001){
						newScoreEndIndexes.add(i);
					}
					prevScore = currentScore;
				}
				newScoreEndIndexes.add(resultAL.size());

				// this code results in a total number of iterations = resultAL.size() as well as no new array construction
				// as well as no array shifting
				Handle cacheReferenceHandle = null;
				long highestPHID = -1L;
				for(int j = 0; j < newScoreEndIndexes.size(); j++){
					int prevEndIndex = j==0 ? 0 : newScoreEndIndexes.get(j-1);
					int diff = newScoreEndIndexes.get(j) - prevEndIndex;
					for(int i = diff, k=0; i > 0; i--, k++){
						int randomIndex = r.nextInt(i) + prevEndIndex; //generate random index within group of same scores
						Handle swapHandle = resultAL.get(randomIndex);  //save handle residing at random index
						resultAL.set(randomIndex, resultAL.get(k+prevEndIndex)); //move element from the first 'available' spot in the group to the random index
						resultAL.set(k+prevEndIndex, swapHandle); //put swapHandle which was at random index in now 'free' first available spot within the group
						if(swapHandle.getOid() > highestPHID){
							highestPHID = swapHandle.getOid();
							cacheReferenceHandle = swapHandle; //todo: think about when to set cache reference..
						}
					}
				}

				//Set the cached reference for 'randomization'
				if (cacheReferenceHandle != null) {
					CNFQuery cachedQuery = TSpecQueryCache.getInstance().getCNFQuery(m_tspecId);
					if(cachedQuery != null){
						setCacheReference(cacheReferenceHandle, cachedQuery);
					}
				}
			}
		}
		return resultAL;
	}

	private static Map<Product.Attribute, String> getAttributeMap() {
		Map<Product.Attribute, String> retMap = new HashMap<Product.Attribute, String>();
		retMap.put(Product.Attribute.kExternalFilterField1, TSpec.ProdAttribute.f1.name());
		retMap.put(Product.Attribute.kExternalFilterField2, TSpec.ProdAttribute.f2.name());
		retMap.put(Product.Attribute.kExternalFilterField3, TSpec.ProdAttribute.f3.name());
		retMap.put(Product.Attribute.kExternalFilterField4, TSpec.ProdAttribute.f4.name());
		retMap.put(Product.Attribute.kExternalFilterField5, TSpec.ProdAttribute.f5.name());
		retMap.put(Product.Attribute.kUT1, TSpec.ProdAttribute.ut1.name());
		retMap.put(Product.Attribute.kUT2, TSpec.ProdAttribute.ut2.name());
		retMap.put(Product.Attribute.kUT3, TSpec.ProdAttribute.ut3.name());
		retMap.put(Product.Attribute.kUT4, TSpec.ProdAttribute.ut4.name());
		retMap.put(Product.Attribute.kUT5, TSpec.ProdAttribute.ut5.name());
		return retMap;
	}

}
