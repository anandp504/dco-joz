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

    private volatile boolean isCapture = true;


    public boolean isCapture() {
        return isCapture;
    }

    public void setCapture(boolean capture) {
        isCapture = capture;
    }

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

    public Pair<JozAdRequest, JozAdResponse> getRequestResponsePairForAdvertiser(String advertiser) {
        if(cacheForAdvertiser.containsKey(advertiser) && cacheForAdvertiser.get(advertiser)!=null){
        return this.cacheForAdvertiser.get(advertiser);
        }else{
            return null;
        }
    }

    public Pair<JozAdRequest, JozAdResponse> getRequestResponsePairForCampaign(String campaign) {
        if(cacheForCampaign.containsKey(campaign) && cacheForCampaign.get(campaign)!=null){
           return this.cacheForCampaign.get(campaign);
        } else {
            return null;
        }

    }
    public boolean isRequestResponseCacheEmpty(){

        return (reqRespPair.get()==null);
    }
    public void cleanReqRespCaches(){
        if(cacheForAdvertiser!=null){
          Set<String> advertiserSet = cacheForAdvertiser.keySet();
          for(String key:advertiserSet){
              cacheForAdvertiser.remove(key);
          }
        }

        if(cacheForCampaign!=null){
            Set<String> campaignSet = cacheForCampaign.keySet();
            for(String key:campaignSet){
               cacheForCampaign.remove(key);
            }
        }
    }

}
