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

import com.tumri.cma.domain.ExperienceTSpecInfo;
import com.tumri.joz.targeting.TargetingResults;
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
            TargetingResults trs = TargetingRequestProcessor.getInstance().processRequest(request, features);
            PerformanceStats.getInstance().registerStartEvent(PROCESS_STATS_ID);
            //long startTime = System.currentTimeMillis();

            if (trs != null) {
                pResults = new ProductSelectionResults();
                if (trs.getRecipe()!=null) {
                    Recipe targetedRecipe = trs.getRecipe();
                    pResults.setTargetedTSpecName(targetedRecipe.getName());
                    pResults.setTargetedRecipe(targetedRecipe);
                    List<RecipeTSpecInfo> infoListRecipe = targetedRecipe.getTSpecInfo();
                    if (infoListRecipe != null) {
                        Collections.sort(infoListRecipe);
                        ProductSelectionRequest pr = prepareRequest(request);
                        pr.setListingClause(trs.getListingClause());
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
                            ArrayList<Handle> prodResults = doProductSelection(tspecId, pr, features);
                            if (prodResults==null) {
                                prodResults = new ArrayList<Handle>();
                            }

                            pResults.addResults(tspecId, slotId, prodResults);
                        }
                    } else {
                        log.info("No TSpecs found in the targeting recipe. Skipping product selection");
                    }
                } else {
                    //Experience based
                    List<ExperienceTSpecInfo> infoListExp = trs.getInfoListExperience();
                    if (infoListExp!=null) {
                        Collections.sort(infoListExp);
                        ProductSelectionRequest pr = prepareRequest(request);
                        pr.setListingClause(trs.getListingClause());
                        if ("false".equals(pr.getTopK())) {
                            pr.setUseTopK(false); //Conditionally turn off TopK if flag is passed in.
                        } else {
                            pr.setUseTopK(true); //By default always turn on TopK for TC campaigns
                        }
                        //The order of tspecs are important in the case of included prods
                        for (ExperienceTSpecInfo queryInfoRecipe : infoListExp) {
                            int tspecId = queryInfoRecipe.getTspecId();
                            int numProds = queryInfoRecipe.getNumProducts();
                            String slotId = queryInfoRecipe.getSlotId();
                            if (numProds > 0) {
                                pr.setPageSize(numProds);
                                pr.setCurrPage(0);
                            } else {
                                continue;
                            }
                            ArrayList<Handle> prodResults = doProductSelection(tspecId, pr, features);
                            if (prodResults==null) {
                                prodResults = new ArrayList<Handle>();
                            }

                            pResults.addResults(tspecId, slotId, prodResults);
                        }
                    } else {
                        log.info("No TSpecs found in the targeted experience. Skipping product selection");
                    }
                    pResults.setAttributePositions(trs.getAttributePositions());
                    pResults.setAttributeValues(trs.getAttributeValues());
                    pResults.setCamDimensionNames(trs.getCamDimensionNames());
                    pResults.setCamDimensionTypes(trs.getCamDimensionTypes());
                    pResults.setTargetedExperience(trs.getExperience());
                    pResults.setFixedDimMap(trs.getFixedDimMap());
                }
            } else {
                log.error("No Recipe/Creative found during targeting. Skipping product selection");
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

        pr.setUrl(request.get_url());
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

        pr.setBMineUrls(false);

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

//        AdDataRequest.AdOfferType offerType = request.get_ad_offer_type();
//        if (offerType != null) {
//            pr.setOfferType(offerType);
//        } else {
//            pr.setOfferType(AdDataRequest.AdOfferType.PRODUCT_ONLY);
//        }
//
//        if (offerType != AdDataRequest.AdOfferType.PRODUCT_ONLY) {
//            if (request.get_ad_height()!=null) {
//                pr.setAdHeight(request.get_ad_height());
//            }
//            if (request.get_ad_width()!=null) {
//                pr.setAdWidth(request.get_ad_width());
//            }
//        }

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

        String externalFilterField1 = request.getExternalFilterField1();
        if (externalFilterField1!= null) {
            pr.setExternalFilterQuery1(externalFilterField1);
        }

        String externalFilterField2 = request.getExternalFilterField2();
        if (externalFilterField2!= null) {
            pr.setExternalFilterQuery2(externalFilterField2);
        }

        String externalFilterField3 = request.getExternalFilterField3();
        if (externalFilterField3!= null) {
            pr.setExternalFilterQuery3(externalFilterField3);
        }

        String externalFilterField4 = request.getExternalFilterField4();
        if (externalFilterField4!= null) {
            pr.setExternalFilterQuery4(externalFilterField4);
        }

        String externalFilterField5 = request.getExternalFilterField5();
        if (externalFilterField5!= null) {
            pr.setExternalFilterQuery5(externalFilterField5);
        }

        String age = request.getAge();
        if (age!=null) {
            pr.setAge(age);
        }

        String gender = request.getGender();
        if (gender!=null) {
            pr.setGender(gender);
        }

        String bt = request.getBt();
        if (bt!=null) {
            pr.setBt(bt);
        }

        String ms = request.getMs();
        if (ms!=null) {
            pr.setMs(ms);
        }

        String hhi = request.getHhi();
        if (hhi!=null) {
            pr.setHhi(hhi);
        }
	    
		pr.setUt1(request.getUt1());
		pr.setUt2(request.getUt2());
		pr.setUt3(request.getUt3());
		pr.setUt4(request.getUt4());
		pr.setUt5(request.getUt5());

        pr.setTopK(request.getUseTopK());
        if ("true".equals(request.getUseTopK())) {
            pr.setUseTopK(true);
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
