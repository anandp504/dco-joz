/**
 * 
 */
package com.tumri.joz.productselection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.tumri.joz.campaign.CampaignDBDataLoader;
import com.tumri.joz.campaign.CampaignDataLoadingException;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.products.Handle;
import com.tumri.joz.server.domain.JozAdRequest;

/**
 * @author omprakash
 * @date Jun 25, 2014
 * @time 1:15:06 PM
 */
public class TestProdSelection {

	@BeforeClass
	public static void init() throws Exception{
		CampaignDBDataLoader loader = CampaignDBDataLoader.getInstance();
        try {
            loader.loadData();
            System.out.println("campaign loaded !");
        } catch (CampaignDataLoadingException e) {
            throw new RuntimeException(e);
        }
	}

	@Test
	public void test1() {
		
		{
			// experience based
			JozAdRequest jozReq = new JozAdRequest();
			jozReq.setValue(JozAdRequest.KEY_AD_TYPE, "skyscraper");
			jozReq.setValue(JozAdRequest.KEY_LOCATION_ID, "115395");
			jozReq.setValue(JozAdRequest.KEY_USER_BUCKET, "70");
			jozReq.setValue(JozAdRequest.KEY_ENV, "flash,edge");

			AdDataRequest adReq = new AdDataRequest(jozReq);
			Features features = new Features();
			ProductSelectionProcessor prp = new ProductSelectionProcessor();
	        ProductSelectionResults prs = prp.processRequest(adReq, features);
	        Assert.assertNotNull(prs.getTargetedExperience());
	        Assert.assertNull(prs.getTargetedRecipe());
	        HashMap<Integer, ArrayList<Handle>> tspecResultsMap = prs.getTspecResultsMap();
	        Set<Integer> keys = tspecResultsMap.keySet();
	        for(Integer key: keys){
	        	ArrayList<Handle> handles = new ArrayList<Handle>();
	        	handles = tspecResultsMap.get(key);
	        	System.out.println("num_products:" +handles.size());
	        }
		}
		{
			JozAdRequest jozReq = new JozAdRequest();
			jozReq.setValue(JozAdRequest.KEY_AD_TYPE, "skyscraper");
			jozReq.setValue(JozAdRequest.KEY_LOCATION_ID, "115395");
			jozReq.setValue(JozAdRequest.KEY_EXPERIENCE_ID, "10946");
			jozReq.setValue(JozAdRequest.KEY_USER_BUCKET, "90");
			AdDataRequest adReq = new AdDataRequest(jozReq);
			Features features = new Features();
			ProductSelectionProcessor prp = new ProductSelectionProcessor();
	        ProductSelectionResults prs = prp.processRequest(adReq, features);
	        Assert.assertNotNull(prs.getTargetedExperience());
	        Assert.assertNull(prs.getTargetedRecipe());
		}
		{
			JozAdRequest jozReq = new JozAdRequest();
			jozReq.setValue(JozAdRequest.KEY_AD_TYPE, "banner300x50");
			jozReq.setValue(JozAdRequest.KEY_LOCATION_ID, "116038");
			jozReq.setValue(JozAdRequest.KEY_USER_BUCKET, "40");
			jozReq.setValue(JozAdRequest.KEY_ENV, "mraid");
			AdDataRequest adReq = new AdDataRequest(jozReq);
			Features features = new Features();
			ProductSelectionProcessor prp = new ProductSelectionProcessor();
	        ProductSelectionResults prs = prp.processRequest(adReq, features);
	        Assert.assertNotNull(prs.getTargetedExperience());
	        Assert.assertNull(prs.getTargetedRecipe());
		}
		{
			JozAdRequest jozReq = new JozAdRequest();
			jozReq.setValue(JozAdRequest.KEY_AD_TYPE, "banner300x50");
			jozReq.setValue(JozAdRequest.KEY_LOCATION_ID, "11603");        // wrong Id
			jozReq.setValue(JozAdRequest.KEY_USER_BUCKET, "80");
			jozReq.setValue(JozAdRequest.KEY_ENV, "mraid");
			AdDataRequest adReq = new AdDataRequest(jozReq);
			Features features = new Features();
			ProductSelectionProcessor prp = new ProductSelectionProcessor();
	        ProductSelectionResults prs = prp.processRequest(adReq, features);
	        Assert.assertNull(prs);
		}
		{
			JozAdRequest jozReq = new JozAdRequest();
			jozReq.setValue(JozAdRequest.KEY_AD_TYPE, "skyscraper");	// wrong type
			jozReq.setValue(JozAdRequest.KEY_LOCATION_ID, "116038");        
			jozReq.setValue(JozAdRequest.KEY_USER_BUCKET, "80");
			jozReq.setValue(JozAdRequest.KEY_ENV, "mraid");
			AdDataRequest adReq = new AdDataRequest(jozReq);
			Features features = new Features();
			ProductSelectionProcessor prp = new ProductSelectionProcessor();
	        ProductSelectionResults prs = prp.processRequest(adReq, features);
	        Assert.assertNull(prs);
		}
		{
			// Recipe based
			JozAdRequest jozReq = new JozAdRequest();
			jozReq.setValue(JozAdRequest.KEY_AD_TYPE, "onebyone");	
			jozReq.setValue(JozAdRequest.KEY_LOCATION_ID, "109227");
			jozReq.setValue(JozAdRequest.KEY_USER_BUCKET, "20");
			AdDataRequest adReq = new AdDataRequest(jozReq);
			Features features = new Features();
			ProductSelectionProcessor prp = new ProductSelectionProcessor();
	        ProductSelectionResults prs = prp.processRequest(adReq, features);
	        Assert.assertNotNull(prs.getTargetedRecipe());
	        Assert.assertNull(prs.getTargetedExperience());
		}
	}
}
