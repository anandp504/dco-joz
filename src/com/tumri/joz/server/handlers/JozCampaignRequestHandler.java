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
import com.tumri.cma.domain.Campaign;
import com.tumri.cma.export.ExportUtils;
import com.tumri.joz.JoZException;
import com.tumri.joz.campaign.TransientDataManager;
import com.tumri.joz.server.domain.JozCampaignRequest;
import com.tumri.joz.server.domain.JozCampaignResponse;
import com.tumri.joz.server.domain.JozMerchantResponse;
import com.tumri.joz.server.domain.JozResponse;
import com.tumri.utils.tcp.server.domain.QueryId;
import com.tumri.utils.tcp.server.domain.QueryInputData;
import com.tumri.utils.tcp.server.domain.QueryResponseData;
import com.tumri.utils.tcp.server.handlers.InvalidRequestException;
import com.tumri.utils.tcp.server.handlers.RequestHandler;
import org.apache.log4j.Logger;


public class JozCampaignRequestHandler implements RequestHandler {

    private static Logger log = Logger.getLogger (JozCampaignRequestHandler.class);

    public QueryResponseData query(QueryInputData input) throws InvalidRequestException {
        JozCampaignResponse response = new JozCampaignResponse();
        query(input, response);
        return response;
    }

    public void query(QueryInputData input, QueryResponseData response) throws InvalidRequestException {
        try {
            query((JozCampaignRequest)input,(JozCampaignResponse)response);
        } catch (ClassCastException ex) {
            throw new InvalidRequestException("Only type JozMerchantRequest & JozMerchantResponse supported.",ex);
        }
    }

    public QueryId[] getSupportedIds() {
        return  new QueryId[]{ QueryId.ADD_CAMPAIGN };
    }

    public void query(JozCampaignRequest input, JozCampaignResponse response) {
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
    private void processRequest(JozCampaignRequest query,JozCampaignResponse response) throws JoZException{
    	String commandType = "";
    	try {
            log.debug("Received JozCampaign data request");
            try {
	            XStream xstream = ExportUtils.getXStreamForExport();
                // get the Campaign data object from the XML using XStream apis
            	commandType = query.getValue(JozCampaignRequest.KEY_COMMAND);
            	            	
        		log.debug("CommandType :"+commandType);
        		if (commandType==null) {
                    response.addDetails(JozCampaignResponse.KEY_ERROR, "No command type specified");
                    return;
                }
            	if(commandType.equalsIgnoreCase(JozCampaignRequest.COMMAND_ADD)){
            		String xmlCampaign = query.getValue(JozCampaignRequest.KEY_CAMPAIGN);
            		log.debug("Request handler \n"+xmlCampaign);
                	Campaign campaign = (Campaign)xstream.fromXML(xmlCampaign);
                	addCampaignToTransientManager(campaign);
            	}else if(commandType.equalsIgnoreCase(JozCampaignRequest.COMMAND_DELETE)){
                    String campIdStr = query.getValue(JozCampaignRequest.KEY_CAMPAIGN_ID);
                    if (campIdStr!=null) {
                        try {
                            int campId=Integer.parseInt(campIdStr);
                            deleteCampaignFromTransientManager(campId);
                        } catch(NumberFormatException e) {
                            throw new JoZException("Invalid campaign id specified");
                        }
                    } else {
                        String xmlCampaign = query.getValue(JozCampaignRequest.KEY_CAMPAIGN);
                        log.debug("Request handler \n"+xmlCampaign);
                        Campaign campaign = (Campaign)xstream.fromXML(xmlCampaign);
                        deleteCampaignFromTransientManager(campaign);
                    }
            	}
                JozResponse resp = new JozResponse();
                resp.setStatus(JozResponse.JOZ_OPERATION_SUCCESS);
                String xml = xstream.toXML(resp);
                response.addDetails(JozCampaignResponse.KEY_CAMPAIGN, xml);

            } catch (Exception ex) {
                log.error("Error on doing "+commandType.toString(),ex);
                response.addDetails(JozCampaignResponse.KEY_ERROR, "Error in command "+commandType);
            }
        } catch (Throwable ex) {
        	log.error("Error on doing "+commandType.toString(),ex);
            response.addDetails(JozCampaignResponse.KEY_ERROR, "Error in command "+commandType);
        }
    }
    
    private void addCampaignToTransientManager(Campaign campaign) throws JoZException{
    	TransientDataManager transientDataMgr = TransientDataManager.getInstance();
    	transientDataMgr.addCampaign(campaign);
    }
    
    private void deleteCampaignFromTransientManager(Campaign campaign) throws JoZException{
        if (campaign==null){
            throw new JoZException("Campaign object is empty");
        }
        deleteCampaignFromTransientManager(campaign.getId());
    }

    private void deleteCampaignFromTransientManager(int campaignId) throws JoZException{
    	TransientDataManager transientDataMgr = TransientDataManager.getInstance();
    	transientDataMgr.deleteCampaign(campaignId);
    }
}