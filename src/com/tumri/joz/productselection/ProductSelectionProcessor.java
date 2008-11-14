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

import org.apache.log4j.Logger;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.targeting.TargetingRequestProcessor;
import com.tumri.joz.products.Handle;
import com.tumri.cma.domain.Recipe;
import com.tumri.cma.domain.RecipeTSpecInfo;
import com.tumri.utils.sexp.SexpUtils;
import com.tumri.utils.stats.PerformanceStats;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Entry point into the Product Selection in Joz
 * @author: nipun
 * Date: Jun 26, 2008
 * Time: 3:44:58 PM
 */
public class ProductSelectionProcessor {

    private static Logger log = Logger.getLogger (ProductSelectionProcessor.class);
	public static final String PROCESS_STATS_ID = "PS";

    @SuppressWarnings("unchecked")
    public ProductSelectionResults processRequest(AdDataRequest request, Features features) {
        ProductSelectionResults pResults = null;
        try {
            Recipe targetedRecipe = TargetingRequestProcessor.getInstance().processRequest(request, features);
	        PerformanceStats.getInstance().registerStartEvent(PROCESS_STATS_ID);
            //long startTime = System.currentTimeMillis();
            
            if (targetedRecipe != null) {
                pResults = new ProductSelectionResults();
                pResults.setTargetedTSpecName(targetedRecipe.getName());
                pResults.setTargetedRecipe(targetedRecipe);
                List<RecipeTSpecInfo> infoListRecipe = targetedRecipe.getTSpecInfo();
                if (infoListRecipe != null) {
                    Collections.sort(infoListRecipe);
                    ProductSelectionRequest pr = prepareRequest(request);
                    //The order of tspecs are important in the case of included prods
                    for (RecipeTSpecInfo queryInfoRecipe : infoListRecipe) {
                        int tspecId = queryInfoRecipe.getTspecId();
                        int numProds = queryInfoRecipe.getNumProducts();
                        String slotId = queryInfoRecipe.getSlotId();
                        if (numProds > 0) {
                            pr.setPageSize(numProds);
                            pr.setCurrPage(0);
                        } else {
                            continue;
                        }
                        ArrayList<Handle> results = new ArrayList<Handle>();
                        if (request.get_ad_offer_type()== AdDataRequest.AdOfferType.PRODUCT_LEADGEN) {
                            //First execute the tspec for leadgen
                            pr.setOfferType(AdDataRequest.AdOfferType.LEADGEN_ONLY);
                            ArrayList<Handle> leadGenresults = doProductSelection(tspecId, pr, features);
                            if (leadGenresults!=null) {
                                results.addAll(leadGenresults);
                            }

                            if (leadGenresults==null || (numProds > leadGenresults.size())) {
                                //Next execute the same tspec for products
                                pr.setOfferType(AdDataRequest.AdOfferType.PRODUCT_ONLY);
                                pr.setPageSize(numProds - leadGenresults.size());
                                ArrayList<Handle> prodResults = doProductSelection(tspecId, pr, features);
                                if (prodResults!=null) {
                                    results.addAll(prodResults);
                                }
                            }
                        } else {
                            ArrayList<Handle> prodResults = doProductSelection(tspecId, pr, features);
                            if (prodResults!=null) {
                                results.addAll(prodResults);
                            }
                        }

                        pResults.addResults(tspecId, slotId, results);
                    }
                } else {
                    log.info("No TSpecs found in the targeting recipe. Skipping product selection");
                }
            } else {
                log.error("No Recipe found during targeting. Skipping product selection");
            }
	        PerformanceStats.getInstance().registerFinishEvent(PROCESS_STATS_ID);
            //log.debug("Product Selection processing time : " + (System.currentTimeMillis() - startTime) + " millis.");
        } catch (Throwable t) {
            log.error("Product Selection layer: unexpected error. The products selection has failed", t);
        }
        return pResults;
    }


    private ProductSelectionRequest prepareRequest(AdDataRequest request) {
        ProductSelectionRequest pr = new ProductSelectionRequest();

        Integer m_NumProducts = request.get_num_products();
        Integer rowSize = request.get_row_size();
        Integer whichRow = request.get_which_row();
        if (m_NumProducts != null) {
            pr.setPageSize(m_NumProducts);
            pr.setCurrPage(0);
            pr.setBPaginate(false);
        } else if (whichRow!=null && rowSize != null) {
            pr.setPageSize(rowSize);
            pr.setCurrPage(whichRow);
            pr.setBPaginate(true);
        } else {
            pr.setPageSize(0);
            pr.setCurrPage(0);
            pr.setBPaginate(false);
        }
        
        SexpUtils.MaybeBoolean mMineUrls = request.get_mine_pub_url_p();
        if (mMineUrls == null || mMineUrls == SexpUtils.MaybeBoolean.FALSE) {
            pr.setBMineUrls(false);
        } else if (mMineUrls == SexpUtils.MaybeBoolean.TRUE) {
            pr.setBMineUrls(true);
        }

        String keywords = "";
        String scriptKeywords = request.get_script_keywords();
		if ((scriptKeywords!=null)&&(!"".equals(scriptKeywords.trim()))) {
            keywords= scriptKeywords.trim();
        }
        
        String requestKeyWords = request.get_keywords();
        if ((requestKeyWords!=null)&&(!"".equals(requestKeyWords.trim()))) {
            keywords = keywords + " " + requestKeyWords.trim();
	        keywords = keywords.trim();
        }

        if (keywords!=null && !"".equals(keywords)) {
            pr.setRequestKeyWords(keywords);
        } else {
            pr.setRequestKeyWords(null);
        }

        if (((mMineUrls == SexpUtils.MaybeBoolean.FALSE) && (keywords==null))
					|| ((whichRow ==null) && (rowSize ==null))) {
				pr.setBRandomize(true);
	    } 

        AdDataRequest.AdOfferType offerType = request.get_ad_offer_type();
        if (offerType != null) {
            pr.setOfferType(offerType);
        } else {
            pr.setOfferType(AdDataRequest.AdOfferType.PRODUCT_ONLY);
        }

        if (offerType != AdDataRequest.AdOfferType.PRODUCT_ONLY) {
            if (request.get_ad_height()!=null) {
                pr.setAdHeight(request.get_ad_height());
            }
            if (request.get_ad_width()!=null) {
                pr.setAdWidth(request.get_ad_width());
            }
        }

        String requestCategory = request.get_category();
        if (requestCategory!=null) {
            pr.setRequestCategory(requestCategory);
        }

        String zipCode = request.get_zip_code();
        if (zipCode!=null) {
            pr.setZipCode(zipCode);
        }

        String countryCode = request.getCountry();
        if (countryCode!=null) {
            pr.setCountryCode(countryCode);
        }

        String cityCode = request.getCity();
        if (cityCode!= null) {
            pr.setCityCode(cityCode);
        }

        String stateCode = request.getRegion();
        if(stateCode!= null) {
            pr.setStateCode(stateCode);
        }

        String dmaCode = request.getDmacode();
        if (dmaCode!= null) {
            pr.setDmaCode(dmaCode);
        }

        String areaCode = request.getAreacode();
        if (areaCode!= null) {
            pr.setAreaCode(areaCode);
        }

        String multiValueField1 = request.getMultiValueField1();
        if (multiValueField1!= null) {
            pr.setMultiValueQuery1(multiValueField1);
        }

        String multiValueField2 = request.getMultiValueField2();
        if (multiValueField2!= null) {
            pr.setMultiValueQuery1(multiValueField2);
        }

        String multiValueField3 = request.getMultiValueField3();
        if (multiValueField3!= null) {
            pr.setMultiValueQuery1(multiValueField3);
        }

        String multiValueField4 = request.getMultiValueField4();
        if (multiValueField4!= null) {
            pr.setMultiValueQuery4(multiValueField4);
        }

        String multiValueField5 = request.getMultiValueField5();
        if (multiValueField5!= null) {
            pr.setMultiValueQuery1(multiValueField5);
        }

        return pr;
    }

    /**
     * Execute the current tspec and add to the results map
     * @param tspecId - the tspec
     * @param pr - request
     * @param f - features
     * @return ArrayList<Handle> - results
     */
    private ArrayList<Handle> doProductSelection(int tspecId, ProductSelectionRequest pr, Features f) {
        TSpecExecutor qp = new TSpecExecutor(pr, f);
        return qp.processQuery(tspecId);
    }

}
