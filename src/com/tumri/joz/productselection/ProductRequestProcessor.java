package com.tumri.joz.productselection;

import com.tumri.cma.domain.OSpec;
import com.tumri.cma.domain.TSpec;
import com.tumri.content.data.Product;
import com.tumri.joz.Query.*;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.OSpecHelper;
import com.tumri.joz.campaign.OSpecQueryCache;
import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.AdDataRequest.AdOfferType;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.targeting.TargetingRequestProcessor;
import com.tumri.utils.data.SortedArraySet;
import com.tumri.utils.sexp.SexpUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;

/**
 * Processes the get-ad-data request, and creates the result set
 * @author nipun
 *
 */
public class ProductRequestProcessor {

	private static Logger log = Logger.getLogger (ProductRequestProcessor.class);

	private CNFQuery m_tSpecQuery = null;
	private OSpec m_currOSpec = null;
    private boolean m_revertToDefaultRealm;
    private boolean m_geoFilterEnabled;
	private HashMap<String, String> m_jozFeaturesMap = new HashMap<String, String>();
	
	
	/**
	 * Default constructor
	 *
	 */
	public ProductRequestProcessor() {
        m_revertToDefaultRealm = false;
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
     * 		<li>6. Add leadgen products if needed </li>
     * 		<li>7. Add outer disjuncted products if needed </li>
	 * 		<li>8. Do the Product Selection </li>
	 * 		<li>9. Return the right number of results </li>
	 * 	 </ul>
	 * @param request
	 * @return
	 */
	public ProductSelectionResults processRequest(AdDataRequest request) {
		ProductSelectionResults pResults = new ProductSelectionResults();
		try {
			long startTime = System.currentTimeMillis();
			SortedSet<Handle> rResult;
			ArrayList<Handle> resultAL;

			//1. if row-size and which-row are non-nil and integers then deterministically return a row/page of results:
            Integer m_currentPage = request.get_which_row();
            Integer m_pageSize = request.get_row_size();

            //2.Num products will override the current page, and page size
            Integer m_NumProducts = request.get_num_products();

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
			if (mMineUrls == null) {
				if (m_currOSpec.isMinePubUrl()) {
					mMineUrls = SexpUtils.MaybeBoolean.TRUE;
				}
			}

			//Clone the query always
			m_tSpecQuery = (CNFQuery)m_tSpecQuery.clone();

			//4. Determine Random vs. Deterministic behaviour: Randomize results only when there is no keyword search, and there is no pagination
			Handle ref = null;
			if (((mMineUrls == SexpUtils.MaybeBoolean.FALSE) && (request.get_keywords() ==null) && (request.get_script_keywords() ==null))
					|| ((m_currentPage ==null) && (m_pageSize ==null))) {
				ref = ProductDB.getInstance().genReference ();
			}
			m_tSpecQuery.setReference(ref);

			//Default the current Page and page Size if they have not been specified
			if (m_NumProducts !=null && (m_currentPage ==null && m_pageSize ==null)) {
				m_currentPage = 0;
				m_pageSize = m_NumProducts;
			}

            //Set pagination bounds for TSpec Query
            if ((m_pageSize !=null) && (m_currentPage !=null)) {
                m_tSpecQuery.setBounds(m_pageSize, m_currentPage);
            } else {
                //Default
                m_pageSize = 0;
                m_currentPage = 0;
                m_tSpecQuery.setBounds(0,0);
            }

            //5. Determine backfill of products
			SexpUtils.MaybeBoolean mAllowTooFewProducts = request.get_allow_too_few_products();
			boolean revertToDefaultRealm = (request.get_revert_to_default_realm() != null) && request.get_revert_to_default_realm();

			if ((mAllowTooFewProducts == SexpUtils.MaybeBoolean.TRUE)||(!revertToDefaultRealm)) {
				m_tSpecQuery.setStrict(true);
				m_revertToDefaultRealm = false;
			} else {
				m_tSpecQuery.setStrict(false);
				m_revertToDefaultRealm = true;
			}

            //Determine if Geo FIlter is enabled
            m_geoFilterEnabled = false;
            List<TSpec> tSpecList = m_currOSpec.getTspecs();
            for (TSpec spec : tSpecList) {
                if (spec.isGeoEnabledFlag()) {
                    m_geoFilterEnabled = true;
                    break;
                }
            }

            resultAL = new ArrayList<Handle>();

            ArrayList<Handle> includedProds = new ArrayList<Handle>();

            //6. Add leadgens for ad-Offer-Type product-leadgen
            AdOfferType offerType = request.get_ad_offer_type();
            if (AdOfferType.PRODUCT_LEADGEN.equals(offerType)) {
                Integer adHeight = request.get_ad_height();
                Integer adWeight = request.get_ad_width();
                ArrayList<Handle> leadGenAL = getLeadGenProducts(adHeight, adWeight);
                if (leadGenAL!=null && leadGenAL.size() > 0){
                    includedProds.addAll(leadGenAL);
                }
            }
            
            //7. Do Outer Disjunction
			ArrayList<Handle> disjunctedProds = OSpecHelper.getIncludedProducts(m_currOSpec);

			if (disjunctedProds!=null && disjunctedProds.size() > 0){
				includedProds.addAll(disjunctedProds);
			}

            if (m_pageSize > 0 && m_currentPage > 0 && (includedProds.size() > m_pageSize *(m_currentPage +1))) {
                rResult = paginateResults(m_pageSize, m_currentPage, includedProds);
                resultAL.addAll(rResult);
            } else {
                //8. Product selection
                if (includedProds.size() > 0 && m_pageSize > 0) {
                    int pageSize = m_pageSize *(m_currentPage +1);
                    m_tSpecQuery.setBounds(pageSize,0);
                }

                rResult = doProductSelection(request);

                if (includedProds.size() > 0) {
                    includedProds.addAll(rResult);
                    rResult = paginateResults(m_pageSize, m_currentPage, includedProds);
                }
                
                ArrayList<Handle> backFillProds = null;
                if (m_NumProducts !=null && rResult!=null){
                    backFillProds = doBackFill(request, m_pageSize,rResult.size());
                }
                //First add the results
                resultAL.addAll(rResult);

                //Now add any backfill, checking for duplicates
                if (backFillProds!=null && backFillProds.size()>0){
                    for(Handle res: backFillProds) {
                        if (!resultAL.contains(res)) {
                            resultAL.add(res);
                        }
                    }
                }
            }
			
			//9. Cull the result by num products
			if ((resultAL!=null) && (m_NumProducts !=null) && (resultAL.size() > m_NumProducts)){
				while(resultAL.size() > m_NumProducts){
					resultAL.remove(resultAL.size()-1);
				}
			}

			pResults.setResults(resultAL);
			pResults.setTargetedOSpec(m_currOSpec);
			pResults.setFeaturesMap(m_jozFeaturesMap);
			log.info("Product Selection processing time : " + (System.currentTimeMillis() - startTime) + " millis.");
		} catch (Throwable t) {
			log.error("Product Selection layer: unxepected error. The products selection has failed", t);
		}
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
	 * 		<li> 4. Include request category into TSpec query</li>
	 * 		<li> 5. Include Product Type request into TSpec query</li>
	 * 		<li> 6. Execute TSpec query</li>
	 * 		<li> 7. Return the result in the sorted order of score</li>
	 * </ul>
	 * @param request
	 * @param tSpecQuery
	 * @return
	 */
	private SortedSet<Handle> doProductSelection(AdDataRequest request) {
		SortedSet<Handle> qResult;

		//1. Request Keywords
		doWidgetKeywordSearch(request);

		//2. Script keywords, if present determine scope
		doScriptKeywordSearch(request);

		//3. URL keywords
		doURLKeywordSearch(request);

		//4. Request Category
		String requestCategory = request.get_category();
		if ((requestCategory!=null)&&(!"".equals(requestCategory))) {
			addRequestCategoryQuery(requestCategory);
		}

		//5. Product Type
		addProductTypeQuery(request);

        addGeoFilterQuery(request);

        //6. Exec TSpec query
		qResult = m_tSpecQuery.exec();

        //Set the cached reference for randomization
        if (m_tSpecQuery.getReference() != null && qResult.size() >0 ) {
            CNFQuery cachedQuery = OSpecQueryCache.getInstance().getCNFQuery(m_currOSpec.getName());
            cachedQuery.setCacheReference(qResult.last());
        }
        return qResult;
	}

	/**
	 * Perform the backfill of products when required
	 * For ScriptKeywords and Mined PubUrl keywords the backfill is done from within the tspec first.
	 * For other cases the backfill is done from the default realm tspec.
     * @param request - the Ad Data Request
     * @param pageSize - the request page Size
     * @param currSize - the current page size
	 * @return ArrayList of products that were backfilled
	 */
	private ArrayList doBackFill(AdDataRequest request,int pageSize, int currSize){
		//Do backfill from the tspec results when there are scriptkeywords or urlmining involved,.
		SexpUtils.MaybeBoolean mMineUrls = request.get_mine_pub_url_p();
		boolean bKeywordBackfill = false;
		
		if (mMineUrls == null) {
			if (m_currOSpec.isMinePubUrl()) {
				mMineUrls = SexpUtils.MaybeBoolean.TRUE;
			}
		}
		
		if ((mMineUrls == SexpUtils.MaybeBoolean.TRUE) || (request.get_script_keywords() !=null)) {
			bKeywordBackfill = true;
		}

		//Check if backfill is needed bcos of the keyword query
		ArrayList<Handle> backFillProds = new ArrayList<Handle>();

        //TODO: Check if the backfill is needed bcos of Geo Filter Query
        if (m_revertToDefaultRealm && m_geoFilterEnabled && pageSize>0 && currSize<pageSize)  {
           SortedSet<Handle> geoBackFillProds = doGeoBackFill(pageSize, currSize);
           backFillProds.addAll(geoBackFillProds);
           currSize = currSize + backFillProds.size();
        }

		if (m_revertToDefaultRealm && bKeywordBackfill && pageSize>0 && currSize<pageSize){
			m_tSpecQuery = (CNFQuery) OSpecQueryCache.getInstance().getCNFQuery(m_currOSpec.getName()).clone();
			//We default the pageSize to the difference we need plus 5 since we want to avoid any duplication of results
			int tmpSize = pageSize-currSize+5;
			m_tSpecQuery.setBounds(tmpSize,0);
			m_tSpecQuery.setStrict(false);
			Handle ref = ProductDB.getInstance().genReference();
			m_tSpecQuery.setReference(ref);
			SortedSet<Handle> newResults = m_tSpecQuery.exec();
			backFillProds.addAll(newResults);
			currSize = currSize + backFillProds.size();
		}


        //Check if the backfill needs to be done - after the queries have been executed
		if (m_revertToDefaultRealm && pageSize>0 && currSize<pageSize){
			//Get the default realm tSpec query
			OSpec defaultRealmOSpec = CampaignDB.getInstance().getDefaultOSpec();
			if (defaultRealmOSpec!=null) {
				CNFQuery defaultRealmTSpec = OSpecQueryCache.getInstance().getCNFQuery(defaultRealmOSpec.getName());
				if (defaultRealmTSpec!=null){
					int tmpSize = pageSize-currSize;
					defaultRealmTSpec.setBounds(tmpSize,0);
					Handle ref = ProductDB.getInstance().genReference ();
					defaultRealmTSpec.setReference(ref);
                    SortedSet<Handle> newResults = defaultRealmTSpec.exec();
                    if (ref !=null && newResults.size() >0 ) {
                        defaultRealmTSpec.setCacheReference(newResults.last());
                    }
                    backFillProds.addAll(newResults);
				}
			}
		}
		return backFillProds;
	}

    /**
     * Backfill by dropping the Geo queries in the order of precedence : Country, State, City, Zip, DMA, Area, GeoEnabled flag
     */
    private SortedSet<Handle> doGeoBackFill(int pageSize, int currSize) {
        SortedSet<Handle> backFillSet = new SortedArraySet<Handle>();

        SortedSet<Handle> newResults = m_tSpecQuery.exec();
        backFillSet.addAll(newResults);
        currSize = currSize + backFillSet.size();

        if (currSize<pageSize) {
            //Drop the Geo Country query
            newResults = doDropGeoAttrQueryAndBackFill(m_tSpecQuery, IProduct.Attribute.kCountry, pageSize, currSize);
            if (newResults!=null) {
                backFillSet.addAll(newResults);
            }
            currSize = currSize + backFillSet.size();
        }

        if (currSize<pageSize) {
            //Drop the Geo State query
            newResults = doDropGeoAttrQueryAndBackFill(m_tSpecQuery, IProduct.Attribute.kState, pageSize, currSize);
            if (newResults!=null) {
                backFillSet.addAll(newResults);
            }
            currSize = currSize + backFillSet.size();
        }

        if (currSize<pageSize) {
            //Drop the Geo City query
            newResults = doDropGeoAttrQueryAndBackFill(m_tSpecQuery, IProduct.Attribute.kCity, pageSize, currSize);
            if (newResults!=null) {
                backFillSet.addAll(newResults);
            }
            currSize = currSize + backFillSet.size();
        }

        if (currSize<pageSize) {
            //Drop the Geo Zip query
            newResults = doDropGeoAttrQueryAndBackFill(m_tSpecQuery, IProduct.Attribute.kZip, pageSize, currSize);
            if (newResults!=null) {
                backFillSet.addAll(newResults);
            }
            currSize = currSize + backFillSet.size();
        }

        if (currSize<pageSize) {
            //Drop the Geo DMA query
            newResults = doDropGeoAttrQueryAndBackFill(m_tSpecQuery, IProduct.Attribute.kDMA, pageSize, currSize);
            if (newResults!=null) {
                backFillSet.addAll(newResults);
            }
            currSize = currSize + backFillSet.size();
        }

        if (currSize<pageSize) {
            //Drop the Geo Area query
            newResults = doDropGeoAttrQueryAndBackFill(m_tSpecQuery, IProduct.Attribute.kArea, pageSize, currSize);
            if (newResults!=null) {
                backFillSet.addAll(newResults);
            }
            currSize = currSize + backFillSet.size();
        }

        if (currSize<pageSize) {
            //Drop the Geo Filter query
            newResults = doDropGeoAttrQueryAndBackFill(m_tSpecQuery, IProduct.Attribute.kGeoEnabledFlag, pageSize, currSize);
            if (newResults!=null) {
                backFillSet.addAll(newResults);
            }
            currSize = currSize + backFillSet.size();
        }

        return backFillSet;
    }

    private SortedSet<Handle> doDropGeoAttrQueryAndBackFill(CNFQuery tmp_tSpecQuery, IProduct.Attribute geoAttr,
                                                            int pageSize, int currSize) {
        SortedSet<Handle> newResults = null;

        //Drop the Geo Filter query
        boolean bDropped = false;
        ArrayList<ConjunctQuery> conjQueries = tmp_tSpecQuery.getQueries();
        for(ConjunctQuery cq: conjQueries) {
            ArrayList<SimpleQuery> simpQueries = cq.getQueries();
            for(SimpleQuery sq: simpQueries) {
                if (sq.getType() == SimpleQuery.Type.kAttribute) {
                    if (((MUPQuery)sq).getAttribute() == geoAttr) {
                        simpQueries.remove(sq);
                        bDropped = true;
                        break;
                    }
                }
            }
        }
        if (bDropped) {
            int tmpSize = pageSize-currSize;
            tmp_tSpecQuery.setBounds(tmpSize,0);
            tmp_tSpecQuery.setStrict(false);
            Handle ref = ProductDB.getInstance().genReference();
            tmp_tSpecQuery.setReference(ref);
            tmp_tSpecQuery.setReference(ref);
            newResults = tmp_tSpecQuery.exec();
        }
        return newResults;
    }
    
    /**
	 * Category may be passed in from the request. If this is the case we always intersect with the TSpec results
	 * Assumption is that the Category is a "included" condition and not excluded condition
	 * @param requestCategory - The input request category
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
	 * Add query for the Product Type
	 * @param request - the input ad data request
	 */
	private void addProductTypeQuery(AdDataRequest request) {
		AdOfferType offerType = request.get_ad_offer_type();
		if (offerType == null) {
			return;
		}
		//If there are no queries in the selected tspec - do not add ad offer type
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
		}
	}

    private void addGeoFilterQuery(AdDataRequest request) {
        if (m_geoFilterEnabled) {
            String countryCode = request.getCountry();
            if (countryCode!=null && !"".equals(countryCode)) {
                Integer countryId = DictionaryManager.getInstance().getId(Product.Attribute.kCountry, countryCode);
                AttributeQuery countryCodeQuery = new AttributeQuery(Product.Attribute.kCountry, countryId);
                m_tSpecQuery.addSimpleQuery(countryCodeQuery);
            }
            String cityCode = request.getCity();
            if (cityCode!=null && !"".equals(cityCode)) {
                Integer cityId = DictionaryManager.getInstance().getId(Product.Attribute.kCity, cityCode);
                AttributeQuery cityQuery = new AttributeQuery(Product.Attribute.kCity, cityId);
                m_tSpecQuery.addSimpleQuery(cityQuery);
            }
            String stateCode = request.getRegion();
            if (stateCode!=null && !"".equals(stateCode)) {
                Integer stateId = DictionaryManager.getInstance().getId(Product.Attribute.kState, stateCode);
                AttributeQuery stateQuery = new AttributeQuery(Product.Attribute.kState, stateId);
                m_tSpecQuery.addSimpleQuery(stateQuery);
            }
            String dmaCode = request.getDmacode();
            if (dmaCode!=null && !"".equals(dmaCode)) {
                Integer dmaCodeId = DictionaryManager.getInstance().getId(Product.Attribute.kDMA, dmaCode);
                AttributeQuery dmaQuery = new AttributeQuery(Product.Attribute.kDMA, dmaCodeId);
                m_tSpecQuery.addSimpleQuery(dmaQuery);
            }
            String areaCode = request.getAreacode();
            if (areaCode!=null && !"".equals(areaCode)) {
                Integer areaCodeId = DictionaryManager.getInstance().getId(Product.Attribute.kArea, areaCode);
                AttributeQuery areaCodeQuery = new AttributeQuery(Product.Attribute.kArea, areaCodeId);
                m_tSpecQuery.addSimpleQuery(areaCodeQuery);
            }

        }
    }

    /**
	 * Perform the widget search
	 * @param request - input ad data request
	 */
	private void doWidgetKeywordSearch(AdDataRequest request) {
		String requestKeyWords = request.get_keywords();
		if ((requestKeyWords!=null)&&(!"".equals(requestKeyWords))) {
			//Note: This is a difference from SoZ. The widget search is going to be constrained by the TSpec
			doKeywordSearch(requestKeyWords, false);
			m_tSpecQuery.setStrict(true);
			m_revertToDefaultRealm = false; // Do not revert to default realm for a Widget Search
			m_jozFeaturesMap.put(Features.FEATURE_WIDGET_SEARCH, requestKeyWords);
		}
	}


	/**
	 * Performs the URL scavenging and runs the query
	 * @param request - input Ad Data request
	 */
	private void doURLKeywordSearch(AdDataRequest request) {
		SexpUtils.MaybeBoolean mMineUrls = request.get_mine_pub_url_p();
		if (mMineUrls == null) {
			if (m_currOSpec.isMinePubUrl()) {
				mMineUrls = SexpUtils.MaybeBoolean.TRUE;
			}
		}
		if (mMineUrls == SexpUtils.MaybeBoolean.TRUE) {
            String queryNames = "";
            String stopWords = "";
			//Get the queryNames and Stopwords
			List<TSpec> tSpecList = m_currOSpec.getTspecs();
			for (TSpec spec : tSpecList) {
				String tmpqueryNames = spec.getPublicURLQueryNames();
				String tmpstopWords = spec.getPublicUrlStopWords();
				if (tmpqueryNames!=null){
                    queryNames = queryNames + " " + tmpqueryNames;
				}
				if (tmpstopWords!=null){
                    stopWords = stopWords + " " + tmpstopWords;
				}
			}
			String urlKeywords = URLScavenger.mineKeywords(request, stopWords, queryNames);
			doKeywordSearch(urlKeywords, !m_currOSpec.isPublishUrlKeywordsWithinOSpec());
			m_tSpecQuery.setStrict(true);
			if ((urlKeywords!=null)&&(!"".equals(urlKeywords))) {
				m_jozFeaturesMap.put(Features.FEATURE_MINE_URL_SEARCH, urlKeywords);
			}
		}
	}

	/**
	 * Perform the Script keywords search query
	 * @param request - input Ad Data request
	 */
	private void doScriptKeywordSearch(AdDataRequest request) {
		String scriptKeywords = request.get_script_keywords();
		if ((scriptKeywords!=null)&&(!"".equals(scriptKeywords))) {
			doKeywordSearch(scriptKeywords, !m_currOSpec.isScriptKeywordsWithinOSpec());
			m_jozFeaturesMap.put(Features.FEATURE_SCRIPT_SEARCH, scriptKeywords);
			m_tSpecQuery.setStrict(true);
		}
	}

	/**
	 * Perform the keyword search
	 * @param keywords - input keywords
	 * @param bCreateNew - flag to create new CNFQuery, or modify existing one
	 */
	private void doKeywordSearch(String keywords, boolean bCreateNew) {
		KeywordQuery sKwQuery;
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
	 * Returns an arraylist of the LeadGen products that need to be appended to the result set for a hybrid ad pod request
     * Note that we select all available leadgen products for the given tspec.
     * @param adHeight - input ad Height
     * @param adWidth - input ad Width
	 * @return LeadGen product list
	 */
	private ArrayList<Handle> getLeadGenProducts(Integer adHeight, Integer adWidth) {
		ArrayList<Handle> leadGenProds = new ArrayList<Handle>();
        //If there are no queries in the selected tspec - do not get lead gen prods
        boolean bSimpleQueries = false;
		ArrayList<ConjunctQuery> _conjQueryAL = m_tSpecQuery.getQueries();
		for (ConjunctQuery conjQuery : _conjQueryAL) {
			ArrayList<SimpleQuery> simpleQueryAL = conjQuery.getQueries();
			if (simpleQueryAL.size()!=0) {
				bSimpleQueries = true;
				break;
			}
		}
		if (!bSimpleQueries) {
			return leadGenProds;
		}

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
        clonedTSpecQuery.setBounds(0, 0);
		SortedSet<Handle> qResult = clonedTSpecQuery.exec();
		leadGenProds.addAll(qResult);
		return leadGenProds;
	}

    /**
     * Helper method to paginate thru the results
     * @param pageSize - page size for the request
     * @param currPage - input request curr page
     * @param resultsAL - current set of results
     * @return  - paginated results
     */
    private SortedArraySet<Handle> paginateResults(int pageSize, int currPage, ArrayList<Handle> resultsAL) {
        //Paginate
        ArrayList<Handle> pageResults;
        if (pageSize > 0) {
            pageResults = new ArrayList<Handle>(pageSize);
            int start = (currPage * pageSize) + 1;
            int end = start + pageSize;
            int i = 0;
            for (Handle handle : resultsAL) {
                i++;
                if (i < start) {
                    // do nothing.
                } else if ((i >= start) && (i < end)) {
                    pageResults.add(handle);
                } else {
                    break;
                }
            }
        } else {
            pageResults = new ArrayList<Handle>(pageSize);
            for (Handle handle : resultsAL) {
                pageResults.add(handle);
            }
        }

        return new SortedArraySet<Handle>(pageResults, true);

    }
}
