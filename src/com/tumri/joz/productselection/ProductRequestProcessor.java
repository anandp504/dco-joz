package com.tumri.joz.productselection;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.tumri.cma.domain.OSpec;
import com.tumri.cma.domain.ProductInfo;
import com.tumri.cma.domain.TSpec;
import com.tumri.content.data.Product;
import com.tumri.joz.Query.AttributeQuery;
import com.tumri.joz.Query.CNFQuery;
import com.tumri.joz.Query.ConjunctQuery;
import com.tumri.joz.Query.KeywordQuery;
import com.tumri.joz.Query.ProductQueryProcessor;
import com.tumri.joz.Query.ProductTypeQuery;
import com.tumri.joz.Query.SimpleQuery;
import com.tumri.joz.campaign.OSpecQueryCache;
import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.AdDataRequest.AdOfferType;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.targeting.TargetingRequestProcessor;
import com.tumri.utils.sexp.SexpUtils;

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
	 * 		<li>1. Check for pagination bounds </li>
	 * 		<li>2. Determine Max number of products, current page and page size for this request </li>
	 * 		<li>3. Select Tspec for the request </li>
	 * 		<li>4. Determine Random vs. Deterministic behaviour </li>
	 * 		<li>5. Determine backfill of products </li>
	 * 		<li>6. Do the Product Selection </li>
	 * 		<li>7. Add leadgen products if needed </li>
	 * 		<li>8. Add outer disjuncted products if needed </li>
	 * 		<li>9. Return the right number of results </li>
	 * 	 </ul>
	 * @param request
	 * @return
	 */
	public ProductSelectionResults processRequest(AdDataRequest request) {
		ProductSelectionResults pResults = new ProductSelectionResults();
		SortedSet<Handle> rResult = null;
		ArrayList<Handle> resultAL = null;

		//1. if row-size and which-row are non-nil and integers then deterministically return a row/page of results:
		m_currentPage = request.get_which_row();
		m_pageSize = request.get_row_size();

		//2.Num products will override the current page, and page size
		m_NumProducts = request.get_num_products ();

		//3. Pass request to Targeting Processor
		m_currOSpec = TargetingRequestProcessor.getInstance().processRequest(request);
		if(m_currOSpec != null) {
			m_tSpecQuery = OSpecQueryCache.getInstance().getCNFQuery(m_currOSpec.getName());
		} else {
			//Return 0 results.
			resultAL = new ArrayList<Handle>();
			pResults.setResults(resultAL);
			pResults.setTargetedOSpec(null);
			return pResults;
		}

		SexpUtils.MaybeBoolean mMineUrls = request.get_mine_pub_url_p();
		//Clone the query always
		m_tSpecQuery = (CNFQuery)m_tSpecQuery.clone();

		//4. Determine Random vs. Deterministic behaviour: Randomize results only when there is no keyword search, and there is no pagination
		Handle ref = null;
		if (((mMineUrls == SexpUtils.MaybeBoolean.FALSE) && (request.get_keywords() ==null) && (request.get_script_keywords() ==null) && !hasKeywords(m_currOSpec))
				|| ((m_currentPage==null) && (m_pageSize==null))) {
			ref = ProductDB.getInstance().genReference ();
		}
		m_tSpecQuery.setReference(ref);

		//Default the current Page and page Size 
		if (m_NumProducts!=null) {
			int numProducts = m_NumProducts.intValue();
			m_currentPage = new Integer(0);
			m_pageSize = numProducts;
		}

		//5. Determine backfill of products
		SexpUtils.MaybeBoolean mAllowTooFewProducts = request.get_allow_too_few_products();
		boolean revertToDefaultRealm = (request.get_revert_to_default_realm()!=null)?request.get_revert_to_default_realm().booleanValue():false;

		if ((mAllowTooFewProducts == SexpUtils.MaybeBoolean.TRUE)||(!revertToDefaultRealm)) {
			m_tSpecQuery.setStrict(true);
		} else {
			m_tSpecQuery.setStrict(false);
		}

		//6. Product selection
		rResult = doProductSelection(request);
		resultAL = new ArrayList<Handle>();

		//7.Add leadgens if needed
		if (m_productLeadgenRequest) {
			Integer numLeadGenProds = request.get_min_num_leadgens();
			Integer adHeight = request.get_ad_height();
			Integer adWeight = request.get_ad_width();
			ArrayList<Handle> leadGenAL = getLeadGenProducts(numLeadGenProds, adHeight, adWeight);
			//Append to the top of the results
			resultAL.addAll(leadGenAL);
		}

		//8. Do Outer Disjunction
		ArrayList<Handle> disjunctedProds = getIncludedProducts(m_currOSpec);

		if (disjunctedProds!=null){
			resultAL.addAll(disjunctedProds);
		} 

		resultAL.addAll(rResult);

		//9. Cull the result by num products
		if ((resultAL!=null) && (m_NumProducts!=null) && (resultAL.size() > m_NumProducts)){
			while(resultAL.size() > m_NumProducts){
				resultAL.remove(resultAL.size()-1);
			}
		}

		pResults.setResults(resultAL);
		pResults.setTargetedOSpec(m_currOSpec);
		return pResults;
	}

	@SuppressWarnings("unchecked")
	/**
	 * Process the request and perform the product selection.
	 * Here are the steps of the Product selection:
	 * <ul>
	 * 		<li> 1. Request Keywords search, if yes then construct Keyword query, ignoring the TSpec query  </li>
	 * 	    <li> 2. Script keywords, if present determine scope. If against MUP, then create new query - or specialize TSpec otherwise </li>
	 * 		<li> 3. URL keywords, if present determine scope. If against MUP - then create new query - or specialize TSpec otherwise </li>
	 * 		<li> 4. Set pagination bound for TSpec squery</li>
	 * 		<li> 5. Include request category into TSpec query</li>
	 * 		<li> 6. Include Product Type request into TSpec query</li>
	 * 		<li> 7. Excecute TSpec query</li>
	 * 		<li> 8. Return the result in the sorted order of score</li>
	 * </ul>
	 * @param request
	 * @param tSpecQuery
	 * @return
	 */
	private SortedSet<Handle> doProductSelection(AdDataRequest request) {
		SortedSet<Handle> qResult = null;

		//1. Request Keywords
		doWidgetKeywordSearch(request);

		//2. Script keywords, if present determine scope
		doScriptKeywordSearch(request);

		//3. URL keywords
		doURLKeywordSearch(request);

		//4. Set pagination bounds for TSpec Query
		if ((m_pageSize!=null) && (m_currentPage !=null)) {
			m_tSpecQuery.setBounds(m_pageSize.intValue(),m_currentPage.intValue() );
		} else {
			//Default
			m_tSpecQuery.setBounds(0,0);
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

		return qResult;
	}


	/**
	 * Category may be passed in from the request. If this is the case we always intersect with the TSpec results
	 * Assumption is that the Category is a "included" condition and not excluded condition
	 * @param requestCategory
	 */
	private void addRequestCategoryQuery(String requestCategory) {
		ArrayList<Integer> catList = new ArrayList<Integer>();
		DictionaryManager dm = DictionaryManager.getInstance ();
		Integer catId = dm.getId (IProduct.Attribute.kCategory, requestCategory);
		catList.add(catId);
		SimpleQuery catQuery = new AttributeQuery (IProduct.Attribute.kCategory, catList);
		CNFQuery copytSpecQuery = (CNFQuery)m_tSpecQuery.clone();
		copytSpecQuery.addSimpleQuery(catQuery);
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
			offerType = AdOfferType.PRODUCT_LEADGEN;
		}
		DictionaryManager dm = DictionaryManager.getInstance ();
		Integer leadGenTypeId = dm.getId (IProduct.Attribute.kProductType, "LEADGEN");
		ProductTypeQuery ptQuery = new ProductTypeQuery(leadGenTypeId);
		if (offerType==AdOfferType.LEADGEN_ONLY) {
			Integer adHeight = request.get_ad_height();
			Integer adWidth = request.get_ad_width();
			if (adHeight!=null) {
				AttributeQuery adHeightQuery = new AttributeQuery(Product.Attribute.kImageHeight, adHeight);
				m_tSpecQuery.addSimpleQuery(adHeightQuery);
			}
			if (adWidth!=null) {
				AttributeQuery adWidthQuery = new AttributeQuery(Product.Attribute.kImageWidth, adWidth);
				m_tSpecQuery.addSimpleQuery(adWidthQuery);
			}
			m_tSpecQuery.addSimpleQuery(ptQuery);
		} else if (offerType==AdOfferType.PRODUCT_ONLY || offerType==AdOfferType.PRODUCT_LEADGEN){
			ptQuery.setNegation(true);
			m_tSpecQuery.addSimpleQuery(ptQuery);
			//TODO: Remove this once we add a strict query type for Product Type and Product attributes
			m_tSpecQuery.setStrict(true);
			if (offerType == AdOfferType.PRODUCT_LEADGEN) {
				m_productLeadgenRequest = true;
			}
		} 
	}

	/**
	 * Perform the widget search
	 * @param request
	 * @return
	 */
	private void doWidgetKeywordSearch(AdDataRequest request) {
		String requestKeyWords = request.get_keywords();
		//Note: This is a difference from SoZ. The widget search is going to be constrained by the TSpec
		doKeywordSearch(requestKeyWords, false);
	}


	/**
	 * Performs the URL scavenging and runs the query
	 *
	 */
	private void doURLKeywordSearch(AdDataRequest request) {
		SexpUtils.MaybeBoolean mMineUrls = request.get_mine_pub_url_p();
		if (mMineUrls == null) {
			if (m_currOSpec.isMinePubUrl()) {
				mMineUrls = SexpUtils.MaybeBoolean.TRUE;
			}
		}
		if (mMineUrls == SexpUtils.MaybeBoolean.TRUE) {
			ArrayList<String> stopWordsAL = null;
			ArrayList<String> queryNamesAL = null;
			//Get the queryNames and Stopwords
			List<TSpec> tSpecList = m_currOSpec.getTspecs();
			for (TSpec spec : tSpecList) {
				String queryNames = spec.getPublicURLQueryNames();
				String stopWords = spec.getPublicUrlStopWords();
				if (queryNames!=null){
					StringTokenizer tokenizer = new StringTokenizer(queryNames, " ");
					if (queryNamesAL==null) {
						queryNamesAL = new ArrayList<String>();
					}
					while (tokenizer.hasMoreTokens()){
						queryNamesAL.add(tokenizer.nextToken());
					}
				}
				if (stopWords!=null){
					if (stopWordsAL==null) {
						stopWordsAL = new ArrayList<String>();
					}
					StringTokenizer tokenizer = new StringTokenizer(stopWords, " ");
					while (tokenizer.hasMoreTokens()){
						stopWordsAL.add(tokenizer.nextToken());
					}
				}
			}
			String urlKeywords = URLScavenger.mineKeywords(request, stopWordsAL, queryNamesAL);
			doKeywordSearch(urlKeywords, !m_currOSpec.isPublishUrlKeywordsWithinOSpec());
		}
	}

	/**
	 * Perform the Script keywords search query
	 * @param request
	 * @return
	 */
	private void doScriptKeywordSearch(AdDataRequest request) {
		String scriptKeywords = request.get_script_keywords();
		doKeywordSearch(scriptKeywords, !m_currOSpec.isScriptKeywordsWithinOSpec());
	}

	/**
	 * Perform the keyword search
	 * @param keywords
	 * @param bCreateNew
	 * @return
	 */
	private void doKeywordSearch(String keywords, boolean bCreateNew) {
		KeywordQuery sKwQuery = null;
		if ((keywords!=null)&&(!"".equals(keywords))) {
			sKwQuery = new KeywordQuery(keywords,false);
			if (bCreateNew) {
				//Ensure that we dont clone again if the TSpec is a new one
				m_tSpecQuery = new CNFQuery();
				m_tSpecQuery.addQuery(new ConjunctQuery(new ProductQueryProcessor()));
			}
			m_tSpecQuery.addSimpleQuery(sKwQuery);
		}
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
	private ArrayList<Handle> getLeadGenProducts(Integer minNumLeadGenProds, Integer adHeight, Integer adWidth) {
		ArrayList<Handle> leadGenProds = new ArrayList<Handle>();
		DictionaryManager dm = DictionaryManager.getInstance ();
		Integer leadGenTypeId = dm.getId (IProduct.Attribute.kProductType, "LEADGEN");
		ProductTypeQuery ptQuery = new ProductTypeQuery(leadGenTypeId);
		CNFQuery clonedTSpecQuery = (CNFQuery)OSpecQueryCache.getInstance().getCNFQuery(m_currOSpec.getName()).clone();
		clonedTSpecQuery.addSimpleQuery(ptQuery);
		clonedTSpecQuery.setStrict(true);
		clonedTSpecQuery.setReference(null);
		if (adHeight!=null) {
			AttributeQuery adHeightQuery = new AttributeQuery(Product.Attribute.kImageHeight, adHeight);
			clonedTSpecQuery.addSimpleQuery(adHeightQuery);
		}
		if (adWidth!=null) {
			AttributeQuery adWidthQuery = new AttributeQuery(Product.Attribute.kImageWidth, adWidth);
			clonedTSpecQuery.addSimpleQuery(adWidthQuery);
		}
		int numLeadGens = 1;
		if (minNumLeadGenProds!=null){
			numLeadGens = minNumLeadGenProds.intValue();
		}
		clonedTSpecQuery.setBounds(numLeadGens, 0);
		SortedSet<Handle> qResult = clonedTSpecQuery.exec();
		leadGenProds.addAll(qResult);
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
							productId = productId.substring(productId.indexOf(".")+3, productId.length());
							Handle prodHandle = ProductDB.getInstance().get(new Integer(productId).intValue()).getHandle();
							if (prodHandle!=null){
								if (prodList==null) {
									prodList = new ArrayList<Handle>();
								}
								prodList.add(prodHandle);
							}
						}
					} catch(Exception e) {
						log.error("Could not get the product info from the Product DB");
						e.printStackTrace();
					}
				}
			}
		}
		return prodList;
	}
}
