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
import com.tumri.cma.domain.AdPod;
import com.tumri.cma.domain.Campaign;
import com.tumri.cma.domain.Location;
import com.tumri.cma.domain.Recipe;
import com.tumri.joz.JoZException;
import com.tumri.joz.campaign.TransientDataManager;
import com.tumri.joz.server.domain.JozAdPod;
import com.tumri.joz.server.domain.JozAdvertiser;
import com.tumri.joz.server.domain.JozCampaign;
import com.tumri.joz.server.domain.JozCampaignRequest;
import com.tumri.joz.server.domain.JozCampaignResponse;
import com.tumri.joz.server.domain.JozICSCampaignRequest;
import com.tumri.joz.server.domain.JozICSCampaignResponse;
import com.tumri.joz.server.domain.JozLocation;
import com.tumri.joz.server.domain.JozMerchant;
import com.tumri.joz.server.domain.JozMerchantRequest;
import com.tumri.joz.server.domain.JozMerchantResponse;
import com.tumri.joz.server.domain.JozProvider;
import com.tumri.joz.server.domain.JozRecipe;
import com.tumri.joz.server.domain.JozResponse;
import com.tumri.utils.tcp.server.domain.QueryId;
import com.tumri.utils.tcp.server.domain.QueryInputData;
import com.tumri.utils.tcp.server.domain.QueryResponseData;
import com.tumri.utils.tcp.server.handlers.InvalidRequestException;
import com.tumri.utils.tcp.server.handlers.RequestHandler;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class JozICSCampaignRequestHandler implements RequestHandler {

    private static Logger log = Logger.getLogger (JozICSCampaignRequestHandler.class);

    public QueryResponseData query(QueryInputData input) throws InvalidRequestException {
        JozICSCampaignResponse response = new JozICSCampaignResponse();
        doQuery((JozICSCampaignRequest)input, response);
        return response;
    }

    public void query(QueryInputData input, QueryResponseData response) throws InvalidRequestException {
        try {
            doQuery((JozICSCampaignRequest)input,(JozICSCampaignResponse)response);
        } catch (ClassCastException ex) {
            throw new InvalidRequestException("Only type JozICSCampaignRequest & JozICSCampaignResponse supported.",ex);
        }
    }

    public QueryId[] getSupportedIds() {
        return  new QueryId[]{ QueryId.CAMPAIGN_DATA_ICS };
    }

    public void doQuery(JozICSCampaignRequest input, JozICSCampaignResponse response) {
        try {
            processRequest(input, response);
        } catch(JoZException e) {
            response.addDetails(JozICSCampaignResponse.KEY_ERROR,"Joz exception on processing the request : " + e.getMessage());
            log.error("Jozexception caught",e);
        } catch (Throwable t) {
            response.addDetails(JozICSCampaignResponse.KEY_ERROR,"Unexpected exception on processing the request : " + t.getMessage());
            log.error("Unexpected Exception caught",t);
        } 
    }

    /**
     * Create the Provider listing
     * @param query
     * @return
     * @throws Exception
     */
    private void processRequest(JozICSCampaignRequest query, JozICSCampaignResponse response) throws JoZException{
    	String commandType = "";
    	try {
            log.info("Received JozCampaign data request");
            try {
            	XStream xstream = new XStream();
                JozResponse resp = new JozResponse();
                // get the Campaign data object from the XML using XStream apis
            	commandType = query.getValue(JozICSCampaignRequest.KEY_COMMAND);
            	            	
        		log.info("CommandType :"+commandType);
        		if (commandType==null) {
                    response.addDetails(JozCampaignResponse.KEY_ERROR, "No command type specified");
                    return;
                }
            	if(commandType.equalsIgnoreCase(JozICSCampaignRequest.COMMAND_GET_ALL_ADVERTISERS)){
            		// get the advertiser data for all campaigns
            		ArrayList<JozAdvertiser> advertisers = getAllAdvertisers();
            		resp.setAdvertisers(advertisers);
            	}else if(commandType.equalsIgnoreCase(JozICSCampaignRequest.COMMAND_CAMPAIGN_FOR_ADVERTISER)){
            		// get the campaign data for the advertiser
            		String advertiserIdStr = query.getValue(JozCampaignRequest.KEY_ADVERTISER_ID);
            		int advertiserId=Integer.parseInt(advertiserIdStr);
            		log.info("getCampaignData for advertiser with id "+advertiserId);
            		ArrayList<JozCampaign> campaigns = getAdvertiserCampaignData(advertiserId);
            		resp.setCampaigns(campaigns);
            	}else if(commandType.equalsIgnoreCase(JozICSCampaignRequest.COMMAND_CAMPAIGN_FOR_ALL_ADVERTISERS)){
            		// get the campaign data for the all advertisers
            		int advertiserId=-1;
            		log.info("getCampaignData for advertiser with id "+advertiserId);
            		ArrayList<JozCampaign> campaigns = getAdvertiserCampaignData(advertiserId);
            		resp.setCampaigns(campaigns);
            	}
                resp.setStatus(JozResponse.JOZ_OPERATION_SUCCESS);
                xstream.alias("jozresponse", JozResponse.class);
                String xml = xstream.toXML(resp);
                response.addDetails(JozICSCampaignResponse.KEY_CAMPAIGN, xml);

            } catch (Exception ex) {
                log.error("Error on doing "+commandType.toString(),ex);
                response.addDetails(JozICSCampaignResponse.KEY_ERROR, "Error in command "+commandType);
            }
        } catch (Throwable ex) {
        	log.error("Error on doing "+commandType.toString(),ex);
            response.addDetails(JozICSCampaignResponse.KEY_ERROR, "Error in command "+commandType);
        }
    }
    
    private ArrayList<JozAdvertiser> getAllAdvertisers(){
    	// return all advertisers
    	ArrayList<JozAdvertiser> advertisers = new ArrayList<JozAdvertiser>();   	
    	TransientDataManager transientDataMgr = TransientDataManager.getInstance();    	
    	List<Campaign> campaigns = transientDataMgr.getCampaigns();
    	if(campaigns != null){
    		for(Campaign campaign:campaigns){
    			int clientId = campaign.getClientId();
    			String clientName = campaign.getClientName();
    			JozAdvertiser advertiser = new JozAdvertiser();
    			advertiser.setId(""+clientId);
    	    	advertiser.setName(clientName);
    	    	advertisers.add(advertiser);
    		}
    	}
    	return advertisers;
    }
    private JozCampaign getJozCampaign(Campaign campaign){
    	JozCampaign jozCampaign = new JozCampaign();
		jozCampaign.setId(""+campaign.getId());
		jozCampaign.setName(campaign.getName());
		jozCampaign.setClientId(""+campaign.getClientId());
		jozCampaign.setClientName(campaign.getClientName());
    	
    	ArrayList<JozAdPod> jozAdpods = new ArrayList<JozAdPod>();
    	List<AdPod> adpods = campaign.getAdpods();
    	if(adpods != null){
	    	for(AdPod adpod:adpods){
	    		JozAdPod jozAdpod = new JozAdPod();
	    		jozAdpod.setId(""+adpod.getId());
	    		jozAdpod.setName(adpod.getName());
	    		jozAdpod.setAdType(adpod.getAdType());
	    			    		    		
	    		// locations
	    		ArrayList<JozLocation> jozLocations = new ArrayList<JozLocation>();
	    		List<Location> locations = adpod.getLocations();
	    		if(locations != null){
	    			for(Location location:locations){
	    				JozLocation jozLocation = new JozLocation();
		    			jozLocation.setClientId(""+location.getClientId());
		    			jozLocation.setClientName(location.getClientName());
		    			jozLocation.setExternalId(""+location.getExternalId());
		    			jozLocation.setName(location.getName());
		    			jozLocations.add(jozLocation);
	    			}	    		    			
	    		}
	    		// recipes
	    		ArrayList<JozRecipe> jozRecipes = new ArrayList<JozRecipe>();
	    		List<Recipe> recipes = adpod.getRecipes();
	    		if(recipes != null){
	    			for(Recipe recipe:recipes){
	    				JozRecipe jozRecipe = new JozRecipe();
		    			jozRecipe.setId(""+recipe.getId());
		    			jozRecipe.setName(recipe.getName());
		    			jozRecipes.add(jozRecipe);
	    			}	    		    			
	    		}
	    		jozAdpod.setLocations(jozLocations);
	    		jozAdpod.setRecipes(jozRecipes);
	    		jozAdpods.add(jozAdpod);	    		    		
	    	}
	    	jozCampaign.setAdpods(jozAdpods);
    	}
    	return jozCampaign;
    }
    private ArrayList<JozCampaign> getAdvertiserCampaignData(int advertiserId){
    	TransientDataManager transientDataMgr = TransientDataManager.getInstance();    	
    	ArrayList<JozCampaign> jozCampaigns = new ArrayList<JozCampaign>();    	
    	List<Campaign> campaigns = transientDataMgr.getCampaigns();
    	if(campaigns != null){
    		if(advertiserId != -1){
        	    // get campaigns for specific advertiser
	    		for(Campaign campaign:campaigns){
	    			int clientId = campaign.getClientId();
	    			if(advertiserId == clientId){
	    				JozCampaign jozCampaign = getJozCampaign(campaign);
	    				jozCampaigns.add(jozCampaign);
	    			}    			
	    		}
    		}else{ 		
	    		// get all campaigns
	    		for(Campaign campaign:campaigns){
					JozCampaign jozCampaign = getJozCampaign(campaign);
					jozCampaigns.add(jozCampaign);
	    		} 
    		}  			
    	}
    	return jozCampaigns;
    }
}