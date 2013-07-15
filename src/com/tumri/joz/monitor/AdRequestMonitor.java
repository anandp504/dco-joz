package com.tumri.joz.monitor;

import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.server.domain.JozAdResponse;
import com.tumri.utils.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AdRequestMonitor to monitor the last request and response processed by Jz
 * User: scbraun
 * Date: Sep 17, 2008
 * Time: 12:18:33 PM
 */
public class AdRequestMonitor {
	private AtomicReference reqRespPair=new AtomicReference();
    private Map<String, Pair<JozAdRequest, JozAdResponse>> cacheForAdvertiser;
    private Map<String, Pair<JozAdRequest, JozAdResponse>> cacheForCampaign;
	private static AdRequestMonitor instance=null;

    private AdRequestMonitor() {
        cacheForAdvertiser = new ConcurrentHashMap<String, Pair<JozAdRequest, JozAdResponse>>();
        cacheForCampaign = new ConcurrentHashMap<String, Pair<JozAdRequest, JozAdResponse>>();
    }
	public static AdRequestMonitor getInstance() {
		if (null == instance) {
			synchronized(AdRequestMonitor.class) {
				if (null == instance) {
					instance=new AdRequestMonitor();
				}
			}
		}
		return instance;
	}

    @SuppressWarnings("unchecked")
    public void setReqResp(JozAdRequest req, JozAdResponse resp){
		 reqRespPair.set(new Pair<JozAdRequest, JozAdResponse>(req, resp));
	}

    @SuppressWarnings("unchecked")
    public Pair<JozAdRequest, JozAdResponse> getReqResp(){
		return (Pair)reqRespPair.get();
	}

    public Pair<JozAdRequest, JozAdResponse> getReqResCacheForAdvertiser(String advertiser) {

        return cacheForAdvertiser.get(advertiser);
    }

    public Pair<JozAdRequest, JozAdResponse> getReqResCacheForCampaign(String campaign) {

        return cacheForCampaign.get(campaign);
    }

    public void addReqResForAdvertiser(JozAdRequest req, JozAdResponse res, String advertiser) {

        cacheForAdvertiser.put(advertiser, new Pair<JozAdRequest, JozAdResponse>(req, res));

    }

    public void addReqResForCampaign(JozAdRequest req, JozAdResponse res, String campaign) {

        cacheForCampaign.put(campaign, new Pair<JozAdRequest, JozAdResponse>(req, res));

    }

    public List<String> getAllAdvertiser() {
        List<String> advertisers = new ArrayList<String>(cacheForAdvertiser.keySet());
        return advertisers;
    }

    public List<String> getAllCampaign() {
        List<String> campaigns = new ArrayList<String>(cacheForCampaign.keySet());
        return campaigns;
    }

    public Map<String, Pair<JozAdRequest, JozAdResponse>> getAdvertiserCacheMap() {
        return cacheForAdvertiser;
    }

    public Map<String, Pair<JozAdRequest, JozAdResponse>> getCampaignCacheMap() {
        return cacheForCampaign;
    }

    public Pair<JozAdRequest, JozAdResponse> getRequestResponsePairForAdvertiser(String advertiser) {
        return this.cacheForAdvertiser.get(advertiser);
    }

    public Pair<JozAdRequest, JozAdResponse> getRequestResponsePairForCampaign(String campaign) {
        return this.cacheForCampaign.get(campaign);
    }

}
