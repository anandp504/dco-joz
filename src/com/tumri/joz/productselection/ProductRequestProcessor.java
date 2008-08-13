package com.tumri.joz.productselection;

import com.tumri.cma.domain.OSpec;
import com.tumri.cma.domain.TSpec;
import com.tumri.content.data.Product;
import com.tumri.content.data.dictionary.DictionaryManager;
import com.tumri.joz.Query.*;
import com.tumri.joz.campaign.OSpecHelper;
import com.tumri.joz.campaign.OSpecQueryCache;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.AdDataRequest.AdOfferType;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.targeting.TargetingRequestProcessor;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.IndexUtils;
import com.tumri.utils.data.SortedArraySet;
import com.tumri.utils.sexp.SexpUtils;
import com.tumri.utils.strings.StringTokenizer;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

/**
 * Processes the get-ad-data request, and creates the result set
 * @author nipun
 *
 */
public class ProductRequestProcessor {

	private static Logger log = Logger.getLogger (ProductRequestProcessor.class);

	private CNFQuery m_tSpecQuery = null;
	private OSpec m_currOSpec = null;
    private boolean m_geoFilterEnabled;
    private boolean m_ExternalKeywords;
    private boolean m_MultiValueQuery;
	private HashMap<String, String> m_jozFeaturesMap = new HashMap<String, String>();
    private Integer m_currentPage= null;
    private Integer m_pageSize = null;
    private static final char MULTI_VALUE_DELIM = AppProperties.getInstance().getMultiValueDelimiter();


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
            m_currentPage = request.get_which_row();
            m_pageSize = request.get_row_size();

            //2.Num products will override the current page, and page size
            Integer numProducts = request.get_num_products();

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

            if ((mMineUrls == SexpUtils.MaybeBoolean.TRUE) || (request.get_script_keywords() !=null)) {
                m_ExternalKeywords = true;
            }

            //Default the current Page and page Size if they have not been specified
			if (numProducts !=null && (m_currentPage ==null && m_pageSize ==null)) {
				m_currentPage = 0;
				m_pageSize = numProducts;
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
			} else {
				m_tSpecQuery.setStrict(false);
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

                //If Geo Filtered, sort by score
                if (m_geoFilterEnabled && !m_ExternalKeywords) {
                    SortedSet<Handle> geoSortedResult = new SortedArraySet<Handle>(new ProductHandle(1.0, 1L));
                    geoSortedResult.addAll(rResult);
                    rResult = geoSortedResult;
                }

                if (includedProds.size() > 0) {
                    includedProds.addAll(rResult);
                    rResult = paginateResults(m_pageSize, m_currentPage, includedProds);
                }

                ArrayList<Handle> backFillProds = null;
                if (numProducts !=null && rResult!=null){
                    backFillProds = doBackFill(request, m_pageSize,rResult);
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
			if ((resultAL!=null) && (numProducts !=null) && (resultAL.size() > numProducts)){
				while(resultAL.size() > numProducts){
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
	 * 		<li> 6. Multi Value queries from request</li>
	 * 		<li> 7. Geo Filtering queries</li>
	 * 		<li> 8. Execute TSpec query</li>
	 * 		<li> 9. Return the result in the sorted order of score</li>
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

        //6. Multi Value Queries
        addMultiValueRequestQueries(request);

        //7. Geo Filtering
        addGeoFilterQuery(request,m_pageSize, m_currentPage);

        //8. Exec TSpec query
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
	 * For ScriptKeywords and Mined PubUrl keywords the backfill is done from within the tspec.
     * For Multivalue queries with geo, backfill is done by dropping the multivalue query and doing the backfill from tspec
     * @param request - the Ad Data Request
     * @param pageSize - the request page Size
     * @param currResults - the current result set
	 * @return ArrayList of products that were backfilled
	 */
	private ArrayList doBackFill(AdDataRequest request,int pageSize,SortedSet<Handle> currResults){
        int currSize = 0;
        if (currResults != null) {
            currSize = currResults.size();
        }

		//Check if backfill is needed bcos of the keyword query
		ArrayList<Handle> backFillProds = new ArrayList<Handle>();

        //For keyword queries with geo enabled or multivalue, do backfill by dropping the keyword query and doing the geo query again
        if (m_geoFilterEnabled  && (m_ExternalKeywords|| m_MultiValueQuery) && pageSize>0 && currSize<pageSize) {
            m_tSpecQuery = (CNFQuery) OSpecQueryCache.getInstance().getCNFQuery(m_currOSpec.getName()).clone();
            addGeoFilterQuery(request,pageSize-currSize, m_currentPage);
            //randomize
            Handle ref = ProductDB.getInstance().genReference();
			m_tSpecQuery.setReference(ref);
            SortedSet<Handle> newResults = m_tSpecQuery.exec();
            if (m_tSpecQuery.getReference() != null && newResults.size() >0 ) {
                CNFQuery cachedQuery = OSpecQueryCache.getInstance().getCNFQuery(m_currOSpec.getName());
                cachedQuery.setCacheReference(newResults.last());
            }
            //Sort by the score
            SortedSet<Handle> geoSortedResult = new SortedArraySet<Handle>(new ProductHandle(1.0, 1L));
            geoSortedResult.addAll(newResults);
            backFillProds.addAll(geoSortedResult);
            currSize = currSize + backFillProds.size();
        }

        if (!m_geoFilterEnabled && m_ExternalKeywords && pageSize>0 && currSize<pageSize){
			m_tSpecQuery = (CNFQuery) OSpecQueryCache.getInstance().getCNFQuery(m_currOSpec.getName()).clone();
            //Never select any products that have Geo enabled while backfilling for keyword queries
            addGeoEnabledQuery(true);
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

		return backFillProds;
	}

    private void addGeoEnabledQuery(boolean bNegation) {
        SimpleQuery geoEnabledQuery = createGeoEnabledQuery();
        geoEnabledQuery.setNegation(bNegation);
        m_tSpecQuery.addSimpleQuery(geoEnabledQuery);
    }

    private SimpleQuery createGeoEnabledQuery() {
        Integer geoFlagId = DictionaryManager.getInstance().getId(Product.Attribute.kGeoEnabledFlag, "true");
        AttributeQuery geoEnabledQuery = new AttributeQuery(Product.Attribute.kGeoEnabledFlag, geoFlagId);
        return geoEnabledQuery;
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

    private void addGeoFilterQuery(AdDataRequest request, int pageSize, int currPage) {
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

        SimpleQuery geoEnabledQuery = createGeoEnabledQuery();
        if (m_geoFilterEnabled) {
            int resultCount = 0;
            String zipCode = request.get_zip_code();
            if (zipCode!=null && !"".equals(zipCode)) {
                resultCount = resultCount+2;  //Add an additional one for the radius set
            }
            String cityCode = request.getCity();
            if (cityCode!=null && !"".equals(cityCode)) {
                resultCount++;
            }
            String dmaCode = request.getDmacode();
            if (dmaCode!=null && !"".equals(dmaCode)) {
                resultCount++;
            }
            String areaCode = request.getAreacode();
            if (areaCode!=null && !"".equals(areaCode)) {
                resultCount++;
            }
            String stateCode = request.getRegion();
            if (stateCode!=null && !"".equals(stateCode)) {
                resultCount++;
            }
            String countryCode = request.getCountry();
            if (countryCode!=null && !"".equals(countryCode)) {
                resultCount++;
            }
            resultCount++; //For the backfill
            CNFQuery geoTSpecQuery = new CNFQuery();
            geoTSpecQuery.setBounds(resultCount*pageSize, currPage);
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
                    addToFeatures(Features.FEATURE_GEO_PARMS, "Zip="+zipCode);
                }
                if (cityCode!=null && !"".equals(cityCode)) {
                    ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kCity, cityCode);
                    if (cloneConjQuery!= null) {
                        geoTSpecQuery.addQuery(cloneConjQuery);
                    }
                    addToFeatures(Features.FEATURE_GEO_PARMS, "City="+cityCode);
                }
                if (dmaCode!=null && !"".equals(dmaCode)) {
                    ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kDMA, dmaCode);
                    if (cloneConjQuery!= null) {
                        geoTSpecQuery.addQuery(cloneConjQuery);
                    }
                    addToFeatures(Features.FEATURE_GEO_PARMS, "Dma="+dmaCode);

                }
                if (areaCode!=null && !"".equals(areaCode)) {
                    ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kArea, areaCode);
                    if (cloneConjQuery!= null) {
                        geoTSpecQuery.addQuery(cloneConjQuery);
                    }
                    addToFeatures(Features.FEATURE_GEO_PARMS, "Area="+areaCode);

                }
                if (stateCode!=null && !"".equals(stateCode)) {
                    ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kState, stateCode);
                    if (cloneConjQuery!= null) {
                        geoTSpecQuery.addQuery(cloneConjQuery);
                    }
                    addToFeatures(Features.FEATURE_GEO_PARMS, "State="+stateCode);
                }
                if (countryCode!=null && !"".equals(countryCode)) {
                    ConjunctQuery cloneConjQuery = cloneAndAddQuery(conjQuery, Product.Attribute.kCountry, countryCode);
                    if (cloneConjQuery!= null) {
                        geoTSpecQuery.addQuery(cloneConjQuery);
                    }
                    addToFeatures(Features.FEATURE_GEO_PARMS, "Country="+countryCode);
                }
                //Add the backfill query
                {
                    ConjunctQuery cloneConjQuery = (ConjunctQuery)conjQuery.clone();
                    geoEnabledQuery.setNegation(true);
                    cloneConjQuery.setBounds(pageSize, currPage);
                    cloneConjQuery.setStrict(false);
                    cloneConjQuery.addQuery(geoEnabledQuery);
                    geoTSpecQuery.addQuery(cloneConjQuery);
                }
            }
            if (resultCount>0) {
                if (!m_ExternalKeywords) {
                    //Set a reference so we return random selection of products.
                    geoTSpecQuery.setCacheReference(m_tSpecQuery.getCacheReference());
                    geoTSpecQuery.setReference(ProductDB.getInstance().genReference ());
                }
                m_tSpecQuery = geoTSpecQuery;
            }
        } else {
            geoEnabledQuery.setNegation(true);
            m_tSpecQuery.addSimpleQuery(geoEnabledQuery);
        }
    }

    private ConjunctQuery cloneAndAddQuery(ConjunctQuery conjQuery, Product.Attribute kAttr, String val){
        AttributeQuery aQuery;
        if (kAttr == IProduct.Attribute.kRadius) {
           Integer codeId = DictionaryManager.getInstance().getId(IProduct.Attribute.kZip, val);
           aQuery = new RadiusQuery(kAttr, codeId);
           //Check if tspec has radius specified
           List<TSpec> tspecList = m_currOSpec.getTspecs();
            for (TSpec tspec : tspecList) {
                if (!tspec.isUseRadiusQuery()) {
                    return null;
                }
                int rad = tspec.getRadius();
                if (rad > 0) {
                    ((RadiusQuery)aQuery).setRadius(rad);
                }
            }
        } else {
           Integer codeId = DictionaryManager.getInstance().getId(kAttr, val);
           aQuery = new AttributeQuery(kAttr, codeId);
        }
        if (aQuery == null) {
            return null;
        }
        ConjunctQuery cloneConjQuery = (ConjunctQuery)conjQuery.clone();
        cloneConjQuery.addQuery(aQuery);
        cloneConjQuery.setBounds(m_pageSize, m_currentPage);
        cloneConjQuery.setStrict(true);
        return cloneConjQuery;
    }

    /**
     * Inspects the request and adds multivalue delim fields for Product Selection.
     * @param request
     */
    private void addMultiValueRequestQueries(AdDataRequest request) {
       boolean bAllowExternalQuery = true;
        List<TSpec> tspecs = m_currOSpec.getTspecs();
        for (TSpec tspec:tspecs) {
            if (!tspec.isAllowExternalQuery()) {
                bAllowExternalQuery = false;
                break;
            }
        }
        if (!bAllowExternalQuery) {
            return;
        }
        String multiValueField1 = request.getMultiValueField1();
        if (multiValueField1 != null && !multiValueField1.equals(""))  {
            addMultiValueFieldQuery(IProduct.Attribute.kMultiValueField1, multiValueField1);
            addToFeatures(Features.FEATURE_MULTI_VALUE_QUERY, "MultiValueField1="+multiValueField1);
        }

        String multiValueField2 = request.getMultiValueField2();
        if (multiValueField2 != null && !multiValueField2.equals(""))  {
            addMultiValueFieldQuery(IProduct.Attribute.kMultiValueField2, multiValueField2);
            addToFeatures(Features.FEATURE_MULTI_VALUE_QUERY, "MultiValueField2="+multiValueField2);
       }

        String multiValueField3 = request.getMultiValueField3();
        if (multiValueField3 != null && !multiValueField3.equals(""))  {
            addMultiValueFieldQuery(IProduct.Attribute.kMultiValueField3, multiValueField3);
            addToFeatures(Features.FEATURE_MULTI_VALUE_QUERY, "MultiValueField3="+multiValueField3);
        }

        String multiValueField4 = request.getMultiValueField4();
        if (multiValueField4 != null && !multiValueField4.equals(""))  {
            addMultiValueFieldQuery(IProduct.Attribute.kMultiValueField4, multiValueField4);
            addToFeatures(Features.FEATURE_MULTI_VALUE_QUERY, "MultiValueField4="+multiValueField4);
        }

        String multiValueField5 = request.getMultiValueField5();
        if (multiValueField5 != null && !multiValueField5.equals(""))  {
            addMultiValueFieldQuery(IProduct.Attribute.kMultiValueField5, multiValueField5);
            addToFeatures(Features.FEATURE_MULTI_VALUE_QUERY, "MultiValueField5="+multiValueField5);
        }

    }

    /**
     * Adds a multi value field query to the current tspec being executed
     * @param kAttr - Product Attribute
     * @param multiValueField - The multi value field passed from iCS
     */
    private void addMultiValueFieldQuery(IProduct.Attribute kAttr, String multiValueField) {
        StringTokenizer st = new StringTokenizer(multiValueField, MULTI_VALUE_DELIM);
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
            }
            Integer fieldId = DictionaryManager.getId (kAttr, val);
            long key = IndexUtils.createLongIndexKey(kAttr, fieldId);
            multiValueIdAL.add(key);
        }
        LongTextQuery aQuery = new LongTextQuery (IProduct.Attribute.kMultiValueTextField, multiValueIdAL);
        CNFQuery copytSpecQuery = (CNFQuery)m_tSpecQuery.clone();
        ArrayList<ConjunctQuery> cnjQueries = copytSpecQuery.getQueries();
        for (ConjunctQuery conjunctQuery : cnjQueries) {
            conjunctQuery.addQuery(aQuery);
        }
        m_MultiValueQuery = true;
        m_tSpecQuery = copytSpecQuery;
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
            addToFeatures(Features.FEATURE_WIDGET_SEARCH, requestKeyWords);

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
                addToFeatures(Features.FEATURE_MINE_URL_SEARCH, urlKeywords);
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
            addToFeatures(Features.FEATURE_SCRIPT_SEARCH, scriptKeywords);
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

    /**
     * Add or append to the features map values
     * @param featuresKey
     * @param value
     */
    private void addToFeatures(String featuresKey, String value) {
        String currFeatures = m_jozFeaturesMap.get(featuresKey);
        if (currFeatures != null) {
            currFeatures = currFeatures + " " + value;
        } else {
            currFeatures = value;
        }
        m_jozFeaturesMap.put(featuresKey, currFeatures);
    }
}
