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

import com.thoughtworks.xstream.XStream;
import com.tumri.content.data.MerchantData;
import com.tumri.content.MerchantDataProvider;
import com.tumri.content.data.impl.AdvertiserMerchantDataMapperImpl;
import com.tumri.joz.JoZException;
import com.tumri.joz.server.domain.JozMerchant;
import com.tumri.joz.server.domain.JozMerchantRequest;
import com.tumri.joz.server.domain.JozMerchantResponse;
import com.tumri.joz.server.domain.JozResponse;
import com.tumri.utils.tcp.server.domain.QueryId;
import com.tumri.utils.tcp.server.domain.QueryInputData;
import com.tumri.utils.tcp.server.domain.QueryResponseData;
import com.tumri.utils.tcp.server.handlers.InvalidRequestException;
import com.tumri.utils.tcp.server.handlers.RequestHandler;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author: nipun
 * Date: Jun 9, 2008
 * Time: 2:37:55 PM
 */

public class JozMerchantRequestHandler implements RequestHandler {

    private static Logger log = Logger.getLogger (JozMerchantRequestHandler.class);

    public QueryResponseData query(QueryInputData input) throws InvalidRequestException {
        JozMerchantResponse response = new JozMerchantResponse();
        doQuery((JozMerchantRequest)input, response);
        return response;
    }

    public void query(QueryInputData input, QueryResponseData response) throws InvalidRequestException {
        try {
            doQuery((JozMerchantRequest)input,(JozMerchantResponse)response);
        } catch (ClassCastException ex) {
            throw new InvalidRequestException("Only type JozMerchantRequest & JozMerchantResponse supported.",ex);
        }
    }

    public QueryId[] getSupportedIds() {
        return  new QueryId[]{ QueryId.MERCHANT };
    }

    public void doQuery(JozMerchantRequest input, JozMerchantResponse response) {
        try {
            processRequest(input, response);
        } catch(JoZException e) {
            response.addDetails(JozMerchantResponse.KEY_ERROR,"Joz exception on processing the request : " + e.getMessage());
            log.error("Jozexception caught",e);
        } catch (Throwable t) {
            response.addDetails(JozMerchantResponse.KEY_ERROR,"Unexpected exception on processing the request : " + t.getMessage());
            log.error("Unexpected Exception caught",t);
        }
    }

    /**
     * Create the Provider listing
     * @param query
     * @return
     * @throws Exception
     */
    private void processRequest(JozMerchantRequest query, JozMerchantResponse response) throws JoZException{
        //GetMerchant does not have any specific query parmaters right now. We might need to get the merchants
        //for a specific provider.
        log.info("Received Merchant data request");
        //Build XML String
        try {
            XStream xstream = new XStream();
            xstream.alias("jozresponse", JozResponse.class);
            xstream.alias("getmerchant",ArrayList.class);
            xstream.alias("merchant",JozMerchant.class);
            xstream.useAttributeFor("id", String.class);
            xstream.useAttributeFor("name", String.class);
            xstream.useAttributeFor("count", String.class);
            xstream.useAttributeFor("logourl",String.class);
            xstream.useAttributeFor("catalogfilename",String.class);
            xstream.useAttributeFor("catalogproductcount",String.class);
            xstream.useAttributeFor("collectstax",String.class);
            xstream.useAttributeFor("contactinfo",String.class);
            xstream.useAttributeFor("hascatalogname",String.class);
            xstream.useAttributeFor("homepageurl",String.class);
            xstream.useAttributeFor("merchant",String.class);
            xstream.useAttributeFor("merchantrating",String.class);
            xstream.useAttributeFor("returnpolicytext",String.class);
            xstream.useAttributeFor("reviewinfo",String.class);
            xstream.useAttributeFor("shippingpromotext",String.class);
            xstream.useAttributeFor("suppliescategory",String.class);

            ArrayList<JozMerchant> merchants = new ArrayList<JozMerchant>();
	        Collection<MerchantDataProvider> cmdp = AdvertiserMerchantDataMapperImpl.getInstance().getAllMerchantDataProviders();
	        List<MerchantData> mdl = new ArrayList<MerchantData>();
	        if(cmdp != null){
		        for(MerchantDataProvider mdp: cmdp){
			        mdl.addAll(mdp.getAll());
		        }
	        }
            for (MerchantData md: mdl) {
                JozMerchant merchant = new JozMerchant();
                merchant.setId(md.getMerchantId()!=null?md.getMerchantId():"");
                merchant.setName(md.getMerchantName()!=null?md.getMerchantName():"");
                merchant.setLogourl(md.getLogoUrl()!=null?md.getLogoUrl():"");
                merchant.setCatalogfilename(md.getCatalogFilename()!=null?md.getCatalogFilename():"");
                merchant.setCatalogproductcount(md.getCatalogProductCount()!=null?md.getCatalogProductCount().toString():"");
                merchant.setCollectstax(md.getCollectsTax()!=null?md.getCollectsTax():"");
                merchant.setContactinfo(md.getContactInfo()!=null?md.getContactInfo():"");
                merchant.setHascatalogname(md.getHasCatalogName()!=null?md.getHasCatalogName():"");
                merchant.setHomepageurl(md.getHomePageUrl()!=null?md.getHomePageUrl():"");
                merchant.setMerchant(md.getMerchant()!=null?md.getMerchant():"");
                merchant.setMerchantrating(md.getMerchantRating()!=null?md.getMerchantRating():"");
                merchant.setReturnpolicytext(md.getReturnPolicyText()!=null?md.getReturnPolicyText():"");
                merchant.setReviewinfo(md.getReviewInfo()!=null?md.getReviewInfo():"");
                merchant.setShippingpromotext(md.getShippingPromotionText()!=null?md.getShippingPromotionText():"");
                merchant.setSuppliescategory(md.getSuppliesCategory()!=null?md.getSuppliesCategory():"");
                merchants.add(merchant);
            }
            JozResponse resp = new JozResponse();
            resp.setMerchants(merchants);
            String xml = xstream.toXML(resp);
            response.addDetails(JozMerchantResponse.KEY_MERCHANTS, xml);

        } catch (Throwable ex) {
            log.error("Error while fetching list of merchants. Request:\"" + toString() + "\".",ex);
            response.addDetails(JozMerchantResponse.KEY_ERROR, "Exception on getting merchants");
        }
    }
}