package com.tumri.joz.utils;

import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.server.domain.JozAdResponse;
import com.tumri.utils.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: omprakash
 * Date: 11/06/13
 * Time: 8:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class RequestResponseCache {
    private Map<String, Pair<JozAdRequest, JozAdResponse>> cacheForAdvertiser;
    private Map<String, Pair<JozAdRequest, JozAdResponse>> cacheForCampaign;
    private static RequestResponseCache requestResponseCache;

    private RequestResponseCache() {
        cacheForAdvertiser = new ConcurrentHashMap<String, Pair<JozAdRequest, JozAdResponse>>();
        cacheForCampaign = new ConcurrentHashMap<String, Pair<JozAdRequest, JozAdResponse>>();
    }

    static {
        requestResponseCache = new RequestResponseCache();
    }

    public static RequestResponseCache getRequestResponseCacheInstance() {
        return requestResponseCache;
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
