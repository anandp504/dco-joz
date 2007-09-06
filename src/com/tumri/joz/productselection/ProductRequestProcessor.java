package com.tumri.joz.productselection;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tumri.cma.domain.OSpec;
import com.tumri.cma.domain.ProductInfo;
import com.tumri.cma.domain.TSpec;
import com.tumri.joz.Query.AttributeQuery;
import com.tumri.joz.Query.CNFQuery;
import com.tumri.joz.Query.KeywordQuery;
import com.tumri.joz.Query.ProductTypeQuery;
import com.tumri.joz.Query.SimpleQuery;
import com.tumri.joz.campaign.CampaignDataCache;
import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.JozData;
import com.tumri.joz.jozMain.AdDataRequest.AdOfferType;
import com.tumri.joz.jozMain.Enums.MaybeBoolean;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.targeting.TSpecTargetingHelper;
import com.tumri.utils.data.SortedArraySet;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpSymbol;

/**
 * Processes the get-ad-data request, and creates the result set
 * @author nipun
 *
 */
public class ProductRequestProcessor {

	private static Logger log = Logger.getLogger (ProductRequestProcessor.class);

	private CNFQuery m_tSpecQuery = null;
	private OSpec m_currOSpec = null;
	private Integer m_NumProducts = null;
	private Integer m_currentPage = null;
	private Integer m_pageSize = null;
	private boolean m_clonedQuery = false;
	private boolean m_productLeadgenRequest = false;

	/**
	 * Default constructor
	 *
	 */
	public ProductRequestProcessor() {
	}


	@SuppressWarnings("unchecked")
	/**
	 * Select the Campaign --> AdPod --> OSpec based on the request parm, and process the query.
	 * The flow of events is as follows:
	 * 	 <ul>
	 * 		<li>1. Determine Max number of products, current page and page size for this request </li>
	 * 		<li>2. Select Tspec for the request </li>
	 * 		<li>3. Determine Random vs. Deterministic behaviour </li>
	 * 		<li>4. Determine backfill of products </li>
	 * 		<li>5. Determine whether or not to do URL Scavenging </li>
	 * 		<li>6. Do the Product Selection </li>
	 * 		<li>7. Add outer disjuncted products if needed </li>
	 * 		<li>8. Return the right number of results </li>
	 * 	 </ul>
	 * @param request
	 * @return
	 */
	public ProductSelectionResults processRequest(AdDataRequest request) {
		ProductSelectionResults pResults = new ProductSelectionResults();
		SortedSet<Handle> rResult = null;

		//1. Max Number of products to be served up
		m_NumProducts = request.get_num_products ();
		if (m_NumProducts!=null) {
			int numProducts = m_NumProducts.intValue();

			// FIXME: This code was just throw away code while
			// trying to get something working.  Something better
			// is warranted.
			if (numProducts < 12)
				numProducts = 12;
			if (numProducts > 100)
				numProducts = 100;
			m_NumProducts = new Integer(numProducts);
		}

		//1. if row-size and which-row are non-nil and integers then deterministically return a row/page of results:
		m_currentPage = request.get_which_row();
		m_pageSize = request.get_row_size();

		//2. Select the TSpec for the request
		String tSpecName = request.get_t_spec();
		if ((tSpecName!=null) && (!"".equals(tSpecName))) {
			m_tSpecQuery = CampaignDataCache.getInstance().getCNFQuery(tSpecName);
			m_currOSpec = CampaignDataCache.getInstance().getOSpec(tSpecName);
		} else {
			String oSpecName = TSpecTargetingHelper.doTargeting(request);
			if (oSpecName != null) {
				m_tSpecQuery = CampaignDataCache.getInstance().getCNFQuery(oSpecName);
			}
			m_currOSpec = CampaignDataCache.getInstance().getOSpec(oSpecName);
		}

		MaybeBoolean mMineUrls = request.get_mine_pub_url_p();

		//3. Determine Random vs. Deterministic behaviour: Randomize results only when there is no keyword search, and there is no pagination
		Handle ref = null;
		if (((mMineUrls == MaybeBoolean.FALSE) && (request.get_keywords() ==null) && (request.get_script_keywords() ==null) && !hasKeywords(m_currOSpec)) 
				|| ((m_currentPage==null) && (m_pageSize==null))) {
			ref = ProductDB.getInstance().genReference ();
		}
		m_tSpecQuery.setReference(ref);

		//4. Determine backfill of products
		MaybeBoolean mAllowTooFewProducts = request.get_allow_too_few_products();
		boolean revertToDefaultRealm = (request.get_revert_to_default_realm()!=null)?request.get_revert_to_default_realm().booleanValue():false;

		if ((mAllowTooFewProducts == MaybeBoolean.TRUE)||(!revertToDefaultRealm)) {
			m_tSpecQuery.getQueries().get(0).setStrict(true);
		} else {
			m_tSpecQuery.getQueries().get(0).setStrict(false);
		}

		if (m_tSpecQuery != null) {
			//6. Product selection
			rResult = doProductSelection(request);

			//7. Do Outer Disjunction
			ArrayList<Handle> disjunctedProds = getIncludedProducts(m_currOSpec);

			//8. Cull the result to get the right page
			if ((rResult!=null) && (m_NumProducts!=null)){
				ArrayList<Handle> results = new ArrayList<Handle>();
				//Cull the results further by num products
				int i = 0;
				for (Handle handle : rResult) {
					if (i<m_NumProducts.intValue()){
						results.add(handle);
					} else {
						break;
					}
					i++;
				}
				//sort by the score
				rResult = new SortedArraySet(results);
			} 


			if (disjunctedProds!=null){
				disjunctedProds.addAll(rResult);
				rResult = new SortedArraySet<Handle>(disjunctedProds, false);
			}

			//Do Hybrid AdPod
			if (m_productLeadgenRequest) {
				ArrayList<Handle> leadGenProds = getLeadGenProducts();
				if (leadGenProds!=null){
					//Append to the top of the results
					leadGenProds.addAll(rResult);
					rResult =  new SortedArraySet<Handle>(leadGenProds, false);
				}
			}


		} else {
			//This shouldnt happen since we always will get back the TSpec out of targeting
			throw new RuntimeException("Could not locate the TSpec to use for the given request");
		}
		pResults.setResults(rResult);
		pResults.setTargetedOSpec(m_currOSpec);
		return pResults;
	}

	@SuppressWarnings("unchecked")
	/**
	 * Process the request and perform the product selection. 
	 * Here are the steps of the Product selection:
	 * <ul>
	 * 		<li> 1. Request Keywords search, if yes then do Keyword query against MUP and return </li>
	 * 	    <li> 2. Script keywords, if present determine scope. If against MUP - return results sorted by Lucene score </li> 
	 * 		<li> 3. URL keywords, if present determine scope. If against MUP - return results sorted by Lucene score</li>
	 * 		<li> 4. Set pagination bound for TSpec squery</li>
	 * 		<li> 5. Include request category into TSpec query</li>
	 * 		<li> 6. Include Product Type request into TSpec query</li>
	 * 		<li> 7. Excecute TSpec query</li>
	 * 		<li> 8. If keywordResult present - then intersect with the TSpec results</li>
	 * 		<li> 9. Return the result in the sorted order of score</li>
	 * </ul>
	 * @param request
	 * @param tSpecQuery
	 * @return
	 */
	private SortedSet<Handle> doProductSelection(AdDataRequest request) {
		SortedSet<Handle> qResult = null;

		//1. Request Keywords
		qResult = doWidgetKeywordSearch(request);
		if (qResult!=null) {
			//This is against the MUP always
			return qResult;
		}

		//2. Script keywords, if present determine scope
		SortedSet<Handle> skeywordQueryResult = doScriptKeywordSearch(request);
		if (skeywordQueryResult!=null) {
			//FIXME: Use the script keyword flag from OSpec
			//if (!m_currOSpec.isScriptKeywordsWithinOSpec()) {
			return skeywordQueryResult;
			//}
		}

		//3. URL keywords
		SortedSet<Handle> ukeywordQueryResult = doURLKeywordSearch(request);
		if (ukeywordQueryResult!=null) {
			if (!m_currOSpec.isPublishUrlKeywordsWithinOSpec()) {
				return ukeywordQueryResult;
			} else {
				//Merge with the Script keywords results
				if (skeywordQueryResult!=null) {
					skeywordQueryResult.addAll(ukeywordQueryResult);
				} else {
					skeywordQueryResult = ukeywordQueryResult;
				}
			}
		}

		//4. Set pagination bounds for TSpec Query
		if ((m_pageSize!=null) && (m_currentPage !=null)) {
			doCloneTSpecQuery();
			m_tSpecQuery.setBounds(m_pageSize.intValue(),m_currentPage.intValue() );
		}

		//5. Request Category
		String requestCategory = request.get_category();
		if ((requestCategory!=null)&&(!"".equals(requestCategory))) {
			addRequestCategoryQuery(requestCategory);
		}

		//6. Product Type
		addProductTypeQuery(request);

		//7. Exec TSpec query
		qResult = m_tSpecQuery.exec();

		//8. If there was a keyword query result, then return the top "n" matches with the tSpec query 
		if (skeywordQueryResult!=null) {
			int numResults = 100; //Setting the limit to 100
			if (m_NumProducts!=null) {
				numResults = m_NumProducts.intValue();
			}
			ArrayList<Handle> finalResult = new ArrayList<Handle>(numResults);
			int count = 0;
			for (Handle handle : skeywordQueryResult) {
				if (count>numResults) {
					break;
				}
				//Check if this is present in the tspec query result
				if (qResult.contains(handle)) {
					finalResult.add(handle);
				}
				count++;
			}
			qResult = new SortedArraySet<Handle>(finalResult,false);
		} else {
			//9. Sort by the score
			SortedSet<Handle> sortedResult = new SortedArraySet<Handle>(ProductDB.getInstance().genReference());
			sortedResult.addAll(qResult);
			qResult = sortedResult;
		}

		return qResult;
	}


	/**
	 * Category may be passed in from the request. If this is the case we always intersect with the TSpec results
	 * Assumption is that the Category is a "included" condition and not excluded condition
	 * @param ospec
	 * @param requestCategory
	 * @return
	 */
	private void addRequestCategoryQuery(String requestCategory) {
		ArrayList<Integer> catList = new ArrayList<Integer>(); 
		DictionaryManager dm = DictionaryManager.getInstance ();
		Integer catId = dm.getId (IProduct.Attribute.kCategory, requestCategory);
		catList.add(catId);
		SimpleQuery catQuery = new AttributeQuery (IProduct.Attribute.kCategory, catList);
		CNFQuery copytSpecQuery = (CNFQuery)m_tSpecQuery.clone();
		copytSpecQuery.getQueries().get(0).addQuery(catQuery);
		m_tSpecQuery = null;
		m_tSpecQuery = copytSpecQuery;
	}

	/**
	 * Add intersect for the Product Type
	 * @param request
	 */
	private void addProductTypeQuery(AdDataRequest request) {
		AdOfferType offerType = request.get_ad_offer_type();
		if (offerType == null) {
			offerType = AdOfferType.PRODUCT_ONLY;
		}
		DictionaryManager dm = DictionaryManager.getInstance ();
		Integer leadGenTypeId = dm.getId (IProduct.Attribute.kProductType, "LEADGEN");
		ProductTypeQuery ptQuery = new ProductTypeQuery(leadGenTypeId);
		doCloneTSpecQuery();
		if (offerType==AdOfferType.LEADGEN_ONLY) {
			m_tSpecQuery.getQueries().get(0).addQuery(ptQuery);
		} else if (offerType==AdOfferType.PRODUCT_ONLY){
			ptQuery.setNegation(true);
			m_tSpecQuery.getQueries().get(0).addQuery(ptQuery);
		} else if (offerType==AdOfferType.PRODUCT_LEADGEN) {
			m_productLeadgenRequest = true;
			ptQuery.setNegation(true);
			m_tSpecQuery.getQueries().get(0).addQuery(ptQuery);
		}
	}

	/**
	 * Clone the query if not already cloned
	 *
	 */
	private void doCloneTSpecQuery() {
		if (!m_clonedQuery) {
			CNFQuery copytSpecQuery = (CNFQuery)m_tSpecQuery.clone();
			m_tSpecQuery = null;
			m_tSpecQuery = copytSpecQuery;
			m_clonedQuery = true;
		}
	}

	/**
	 * Perform the widget search
	 * @param request
	 * @return
	 */
	private SortedSet<Handle> doWidgetKeywordSearch(AdDataRequest request) {
		String requestKeyWords = request.get_keywords();
		return doKeywordSearch(request, requestKeyWords);
	}


	/**
	 * Performs the URL scavenging and runs the query
	 *
	 */
	private SortedSet<Handle> doURLKeywordSearch(AdDataRequest request) {
		SortedSet<Handle> results = null;
		MaybeBoolean mMineUrls = request.get_mine_pub_url_p();
		if (mMineUrls == MaybeBoolean.TRUE) {
			String urlKeywords = URLScavenger.mineKeywords(request, null);
			results = doKeywordSearch(request, urlKeywords);
		}
		return results;
	}

	/**
	 * Perform the Script keywords search query
	 * @param request
	 * @return
	 */
	private SortedSet<Handle> doScriptKeywordSearch(AdDataRequest request) {
		String scriptKeywords = request.get_script_keywords();
		return doKeywordSearch(request, scriptKeywords);
	}

	/**
	 * Perform the keyword search
	 * @param request
	 * @param keywords
	 * @return
	 */
	private SortedSet<Handle> doKeywordSearch(AdDataRequest request, String keywords) {
		KeywordQuery sKwQuery = null;
		SortedSet<Handle> kResults = null;
		if ((keywords!=null)&&(!"".equals(keywords))) {
			sKwQuery = new KeywordQuery(keywords);
			if ((m_pageSize!=null) && (m_currentPage !=null)) {
				sKwQuery.setBounds(m_pageSize.intValue(),m_currentPage.intValue());
			}
			sKwQuery.setLuceneSortOrder(true);			
			kResults = sKwQuery.exec();
		}
		return kResults;
	}

	/**
	 * returns true if the oSpec has included keywords
	 * @param ospec
	 * @return
	 */
	private boolean hasKeywords(OSpec ospec) {
		List<TSpec> tspeclist = ospec.getTspecs();
		for (TSpec tspec : tspeclist) {
			if (tspec.getIncludedKeywords() != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns an arraylist of the LeadGen products that need to be appended to the result set for a hybrid ad pod request
	 * @return
	 */
	private ArrayList<Handle> getLeadGenProducts() {
		ArrayList<Handle> leadGenProds = new ArrayList<Handle>();
		DictionaryManager dm = DictionaryManager.getInstance ();
		Integer leadGenTypeId = dm.getId (IProduct.Attribute.kProductType, "LEADGEN");
		ProductTypeQuery ptQuery = new ProductTypeQuery(leadGenTypeId);
		doCloneTSpecQuery();
		m_tSpecQuery.getQueries().get(0).addQuery(ptQuery);
		SortedSet<Handle> qResult = m_tSpecQuery.exec();
		if (qResult != null)
			leadGenProds.add(qResult.first());
		return leadGenProds;
	}

	/**
	 * Returns the included products if the oSpec has included products
	 * @param ospec
	 * @return
	 */
	private ArrayList<Handle> getIncludedProducts(OSpec ospec) {
		List<TSpec> tspeclist = ospec.getTspecs();
		ArrayList<Handle> prodList = null;
		for (TSpec tspec : tspeclist) {
			List<ProductInfo> prodInfoList = tspec.getIncludedProducts();
			if (prodInfoList!=null) {
				for (ProductInfo info : prodInfoList) {
					try {
						String productId = info.getName();
						if (productId != null) {
							productId = productId.substring(productId.indexOf("."), productId.length());
							Handle prodHandle = ProductDB.getInstance().get(new Integer(productId).intValue()).getHandle();
							if (prodHandle!=null){
								prodList.add(prodHandle);
							}
						}
					} catch(Exception e) {
						log.error("Could not get the product info from the Product DB");
					}
				}
			}
		}
		return prodList;
	}

	@BeforeClass
	public static void initiatize() {
		JozData.init ();
	}

	@Test
	public void testVanillaGetAdData() {
		try {
			String queryStr = "(get-ad-data :theme \"http://www.photography.com/\")";
			SortedSet<Handle> result = testProcessRequest(queryStr);
			Assert.assertTrue(result!=null);
		} catch(Exception e){
			log.error("Exception caught during test run");
			e.printStackTrace();
		}
	}

	@Test
	public void testNumProducts() {
		try {
			String queryStr = "(get-ad-data :theme \"http://www.photography.com/\" :num-products 30)";
			SortedSet<Handle> result = testProcessRequest(queryStr);
			Assert.assertTrue(result!=null);
		} catch(Exception e){
			log.error("Exception caught during test run");
			e.printStackTrace();
		}
	}

	@Test
	public void testPagination() {
		try {
			String queryStr = "(get-ad-data :theme \"http://www.photography.com/\" :which-row 2 :row-size 12)";
			SortedSet<Handle> result = testProcessRequest(queryStr);
			Assert.assertTrue(result!=null);
		} catch(Exception e){
			log.error("Exception caught during test run");
			e.printStackTrace();
		}
	}

	@Test
	public void testAllowTooFewProducts() {
		try {
			String queryStr = "(get-ad-data :theme \"http://www.photography.com/\" :allow-too-few-products t)";
			SortedSet<Handle> result = testProcessRequest(queryStr);
			Assert.assertTrue(result!=null);
		} catch(Exception e){
			log.error("Exception caught during test run");
			e.printStackTrace();
		}
	}

	@Test
	public void testRevertToDefaultRealm() {
		try {
			String queryStr = "(get-ad-data :theme \"http://www.photography.com/\" :revert-to-default-realm nil)";
			SortedSet<Handle> result = testProcessRequest(queryStr);
			Assert.assertTrue(result!=null);
		} catch(Exception e){
			log.error("Exception caught during test run");
			e.printStackTrace();
		}
	}

	@Test
	public void testLeadgenOnly() {
		try {
			String queryStr =  "(get-ad-data :theme \"http://www.photography.com/\" :ad-offer-type :leadgen-only :revert-to-default-realm nil)";
			SortedSet<Handle> result = testProcessRequest(queryStr);
			Assert.assertTrue(result!=null);
		} catch(Exception e){
			log.error("Exception caught during test run");
			e.printStackTrace();
		}
	}

	@Test
	public void testProductOnly() {
		try {
			String queryStr =  "(get-ad-data :theme \"http://www.photography.com/\" :ad-offer-type :product-only :revert-to-default-realm nil)";
			SortedSet<Handle> result = testProcessRequest(queryStr);
			Assert.assertTrue(result!=null);
		} catch(Exception e){
			log.error("Exception caught during test run");
			e.printStackTrace();
		}
	}

	@Test
	public void testKeywordSearch() {
		try {
			String queryStr = "(get-ad-data :theme \"http://www.photography.com/\" :keywords \"nikon\" :revert-to-default-realm nil)";
			SortedSet<Handle> result = testProcessRequest(queryStr);
			Assert.assertTrue(result!=null);
		} catch(Exception e){
			log.error("Exception caught during test run");
			e.printStackTrace();
		}
	}

	@Test
	public void testScriptKeywordSearch() {
		try {
			String queryStr = "(get-ad-data :theme \"http://www.photography.com/\" :script-keywords \"nikon\" :revert-to-default-realm nil)";
			SortedSet<Handle> result = testProcessRequest(queryStr);
			Assert.assertTrue(result!=null);
		} catch(Exception e){
			log.error("Exception caught during test run");
			e.printStackTrace();
		}
	}

	@Test
	public void testIncludedCategories() {
		try {
			String queryStr = "(get-ad-data :theme \"http://www.photography.com/\" :category \"GLASSVIEW.TUMRI_14337\" :revert-to-default-realm nil)";
			SortedSet<Handle> result = testProcessRequest(queryStr);
			Assert.assertTrue(result!=null);
		} catch(Exception e){
			log.error("Exception caught during test run");
			e.printStackTrace();
		}
	}

	private SortedSet<Handle> testProcessRequest(String getAdDataCommandStr) throws Exception {
		ProductRequestProcessor prodRequest = new ProductRequestProcessor();
		Reader r = new StringReader (getAdDataCommandStr);
		SortedSet<Handle> results = null;
		SexpReader lr = new SexpReader (r);
		try {
			Sexp e = lr.read ();
			SexpList l = e.toSexpList ();
			Sexp cmd_expr = l.getFirst ();
			if (! cmd_expr.isSexpSymbol ())
				log.error("command name not a symbol: " + cmd_expr.toString ());

			SexpSymbol sym = cmd_expr.toSexpSymbol ();
			String cmd_name = sym.toString ();

			// Return the right Cmd* class to handle this request.

			if (cmd_name.equals ("get-ad-data")) {
				AdDataRequest rqst = new AdDataRequest (e);
				ProductSelectionResults presults = prodRequest.processRequest(rqst);
				//Inspect the results
				results = presults.getResults();
				log.info("Number of results returned are : " + results.size());
				Assert.assertTrue(presults!=null);

				ProductDB pdb = ProductDB.getInstance ();
				results = presults.getResults();
				for (Handle res : results)
				{
					int id = res.getOid ();
					IProduct ip = pdb.get (id);
					String name = ip.getProductName ();
					String desc = ip.getDescription ();
					log.info(id + "     " + name + "    " + desc);
				}

			} else {
				log.error("The request could not be parsed correctly");
				Assert.assertTrue(false);
			}
		} catch(Exception e) {
			throw e;
		}

		return results;

	}
}
