/*
 * ListingsQueryHandler.java
 *
 * COPYRIGHT (C) ${year} TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (${user}@tumri.com)
 * @version 1.0     ${date}
 *
 */
package com.tumri.joz.server.handlers;

import com.tumri.cma.domain.Recipe;
import com.tumri.cma.domain.UIProperty;
import com.tumri.joz.JoZException;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.jozMain.ListingProviderFactory;
import com.tumri.joz.jozMain.MerchantDB;
import com.tumri.joz.monitor.AdRequestMonitor;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.joz.productselection.ProductSelectionProcessor;
import com.tumri.joz.productselection.ProductSelectionResults;
import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.server.domain.JozAdResponse;
import com.tumri.lls.client.main.ListingProvider;
import com.tumri.lls.client.response.ListingResponse;
import com.tumri.utils.stats.PerformanceStats;
import com.tumri.utils.tcp.server.domain.QueryId;
import com.tumri.utils.tcp.server.domain.QueryInputData;
import com.tumri.utils.tcp.server.domain.QueryResponseData;
import com.tumri.utils.tcp.server.handlers.InvalidRequestException;
import com.tumri.utils.tcp.server.handlers.RequestHandler;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author: nipun
 * Date: Jun 9, 2008
 * Time: 2:37:55 PM
 */

public class JozAdRequestHandler implements RequestHandler {

    private static Logger log = Logger.getLogger (JozAdRequestHandler.class);
	public static final String STATS_ID = "AD";
    public JozAdResponse query(QueryInputData input) throws InvalidRequestException {
        return doQuery((JozAdRequest)input);
    }

    public void query(QueryInputData input, QueryResponseData response) throws InvalidRequestException {
        throw new UnsupportedOperationException("Not supported");
    }

    public QueryId[] getSupportedIds() {
        return  new QueryId[]{ QueryId.AD_REQUEST };
    }

    private JozAdResponse doQuery(JozAdRequest input) {
        JozAdResponse result = new JozAdResponse();
        try {
            result = processRequest(input, result);
        } catch(JoZException e) {
            result = new JozAdResponse();
            result.addDetails(JozAdResponse.KEY_ERROR,"Joz exception on processing the request : " + e.getMessage());
            log.error("Jozexception caught",e);
        } catch (Throwable t) {
            result = new JozAdResponse();
            result.addDetails(JozAdResponse.KEY_ERROR,"Unexpected exception on processing the request : " + t.getMessage());
            log.error("Unexpected Exception caught",t);
        }
        return result;
    }

    /**
     * Create the JSON string listing
     * @param query
     * @return
     * @throws Exception
     */
    private JozAdResponse processRequest(JozAdRequest query, JozAdResponse response) throws JoZException{
        Features features = new Features();
        boolean private_label_p = false;
        AdDataRequest rqst = new AdDataRequest(query);

        //Reverse order of precedence.
        String reqParams=rqst.get_store_id();
        if (null == reqParams || "".equals(reqParams)) {
            reqParams=rqst.get_theme();
        }
        if (null == reqParams || "".equals(reqParams)) {
            reqParams=(rqst.getRecipeId()!=null?rqst.getRecipeId().toString():null);
        }
        if (null == reqParams || "".equals(reqParams)) {
            reqParams=rqst.get_t_spec();
        }
        if (null == reqParams || "".equals(reqParams)) {
            reqParams = "UNKNOWN";
        }
        // This does the real work of selecting a set of products.
        long start_time = System.nanoTime();
        long elapsed_time = 0L;
	    PerformanceStats.getInstance().registerStartEvent(STATS_ID);
        ProductSelectionProcessor prp = new ProductSelectionProcessor();
        ProductSelectionResults prs = prp.processRequest(rqst, features);

        if (prs!=null) {
            HashMap<Integer, ArrayList<Handle>> resultsMap = prs.getTspecResultsMap();
            HashMap<Integer, String> resultsSlotMap = prs.getTspecSlotIdMap();
            ArrayList<Handle> product_handles = new ArrayList<Handle>();
            ArrayList<String> slotIdAL = new ArrayList<String>();
            if (resultsMap!= null && !resultsMap.isEmpty()) {
                Iterator<Integer> tspecIdList = resultsMap.keySet().iterator();
                while (tspecIdList.hasNext()) {
                    Integer id = tspecIdList.next();
                    product_handles.addAll(resultsMap.get(id));
                    for (int i=0;i<product_handles.size();i++) {
                        slotIdAL.add(resultsSlotMap.get(id));
                    }
                }
            }
            String targetedOSpec = prs.getTargetedTSpecName();
            elapsed_time = System.nanoTime() - start_time;

            write_result(rqst, targetedOSpec,
                    private_label_p, features, elapsed_time, product_handles, response, slotIdAL);

            PerformanceStats.getInstance().registerFinishEvent(STATS_ID, reqParams);

            //Get the recipe data
            String recipeData = null;
            Recipe r = prs.getTargetedRecipe();
            if (r!= null) {
                recipeData = getRecipeData(r);
                response.addDetails(JozAdResponse.KEY_RECIPE_ID ,new Integer(r.getId()).toString());
            }

            response.addDetails(JozAdResponse.KEY_RECIPE,recipeData);
            response.addDetails(JozAdResponse.KEY_CAMPAIGN_ID,(features.getCampaignId()>0?new Integer(features.getCampaignId()).toString():""));
            response.addDetails(JozAdResponse.KEY_CAMPAIGN_NAME,(features.getCampaignName()));
            response.addDetails(JozAdResponse.KEY_CAMPAIGN_CLIENT_ID,(features.getCampaignClientId()>0?new Integer(features.getCampaignClientId()).toString():""));
            response.addDetails(JozAdResponse.KEY_CAMPAIGN_CLIENT_NAME,(features.getCampaignClientName()));
            response.addDetails(JozAdResponse.KEY_ADPOD_ID,(features.getAdPodId()>0?new Integer(features.getAdPodId()).toString():""));
            response.addDetails(JozAdResponse.KEY_ADPOD_NAME,(features.getAdpodName()));
            response.addDetails(JozAdResponse.KEY_LOCATION_ID,(features.getTargetedLocationId()));
            response.addDetails(JozAdResponse.KEY_LOCATION_NAME,features.getTargetedLocationName());
            response.addDetails(JozAdResponse.KEY_LOCATION_CLIENT_ID,(features.getLocationClientId()>0?new Integer(features.getLocationClientId()).toString():""));
            response.addDetails(JozAdResponse.KEY_LOCATION_CLIENT_NAME,features.getTargetedLocationName());
            response.addDetails(JozAdResponse.KEY_RECIPE_NAME,features.getRecipeName());
            response.addDetails(JozAdResponse.KEY_GEO_USED,(features.isGeoUsed()?"Y":"N"));
        } else {
	        PerformanceStats.getInstance().registerFailedEvent(STATS_ID,reqParams);
            response.addDetails("ERROR","Could not target Recipe for the request");
        }
	    AdRequestMonitor.getInstance().setReqResp(query, response);
        return response;
    }

    private String getRecipeData(Recipe r) {
        StringBuilder sbuild = new StringBuilder();
        List<UIProperty> props = r.getProperties();
        if (props!= null) {
            int count = props.size();
            int i = 0;
            String design = r.getDesign();
            if (design!=null){
                sbuild.append("design===" + design + "&&&");
            }
            for (UIProperty prop: props) {
                String name = prop.getName();
                String value = prop.getValue();
                if (name != null && !name.equals("") && value != null && !value.equals("")) {
                    sbuild.append(name + "===" + value);
                    if (i+1 != count) {
                        sbuild.append("&&&");
                    }
                }
            }
        }
        return sbuild.toString();

    }

    // Write the chosen product list back to the client.
    // The format is:
    //
    // (
    // ("VERSION" "1.0")
    // ("PRODUCTS" product-list)
    // ("PROD-IDS" product-id-list)
    // ("CATEGORIES" category-list)
    // ("CAT-NAMES" category-name-list)
    // ("REALM" realm)
    // ("STRATEGY" t-spec-name)
    // ("IS-PRIVATE-LABEL-P" is-private-label-p)
    // ("SOZFEATURES" feature-list-or-nil)
    // )
    //
    // FIXME: version number needs to be spec'd to be first, remainder should
    // allow for optional parameters, (consider precedent of IS-PRIVATE-LABEL-P
    // and what happens over time as more are added).
    //
    // NOTE: In SoZ this is the "js-friendly" format, js for JSON
    // http://www.json.org.

    private void write_result(AdDataRequest rqst, String ospec,
                              boolean private_label_p, Features features, long elapsed_time,
                              ArrayList<Handle> product_handles, JozAdResponse resp,ArrayList<String> slotIdAL) throws JoZException {
        Integer maxDescLength = rqst.get_max_prod_desc_len();
        if (product_handles==null) {
            throw new JoZException("No products returned by the product selection");
        }

        long[] pids = new long[product_handles.size()];

        for (int i=0;i<product_handles.size();i++){
            pids[i] = product_handles.get(i).getOid();
        }

        String[] slotIdArr = null;

        if (slotIdAL != null) {
            slotIdArr = slotIdAL.toArray(new String[0]);
        }

        ListingProvider _prov = ListingProviderFactory.getProviderInstance(JOZTaxonomy.getInstance().getTaxonomy(),
                MerchantDB.getInstance().getMerchantData());
        ListingResponse response = _prov.getListing(pids, (maxDescLength != null) ? maxDescLength.intValue() : 0, slotIdArr);
        if (response==null) {
            throw new JoZException("Invalid response from Listing Provider");
        }
        resp.addDetails(JozAdResponse.KEY_PRODUCTS ,response.getListingDetails());
        resp.addDetails(JozAdResponse.KEY_PRODIDS ,response.getProductIdList());
        resp.addDetails(JozAdResponse.KEY_CATEGORIES ,response.getCatDetails());
        resp.addDetails(JozAdResponse.KEY_CATNAMES,response.getCatIdList());

        resp.addDetails(JozAdResponse.KEY_REALM,rqst.getTargetedRealm());
        resp.addDetails(JozAdResponse.KEY_STRATEGY ,ospec);

        if (private_label_p) {
            resp.addDetails(JozAdResponse.KEY_ISPRIVATELABEL,"t");
        }
        resp.addDetails(JozAdResponse.KEY_SOZFEATURES,features.toString(elapsed_time));
    }


}