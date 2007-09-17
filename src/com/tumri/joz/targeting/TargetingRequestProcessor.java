package com.tumri.joz.targeting;

import org.apache.log4j.Logger;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.Query.SiteTargetingQuery;
import com.tumri.joz.Query.GeoTargetingQuery;
import com.tumri.joz.Query.AdPodQueryProcessor;
import com.tumri.joz.Query.ConjunctQuery;
import com.tumri.joz.products.Handle;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.AdPodHandle;
import com.tumri.joz.campaign.OSpecNotFoundException;
import com.tumri.cma.domain.OSpec;
import com.tumri.cma.domain.AdPod;
import com.tumri.utils.data.SortedArraySet;

import java.util.*;

/**
 * TargetingRequestProcessor handles the incoming adrequest and does the site and geo targeting against the request data.
 *
 * @author bpatel
 */
public class TargetingRequestProcessor {

    private static TargetingRequestProcessor processor = new TargetingRequestProcessor();

    private TargetingRequestProcessor() {

    }

    public static TargetingRequestProcessor getInstance() {
        return processor;
    }
    private static Logger log = Logger.getLogger (TargetingRequestProcessor.class);

    public OSpec processRequest(AdDataRequest request) {
        OSpec oSpec = null;
        String tSpecName = request.get_t_spec();
        if(tSpecName != null && !"".equals(tSpecName)) {
            try {
            oSpec = getOSpecForGivenName(tSpecName);
            }
            catch(OSpecNotFoundException e) {
                //@todo: Confirm with management whether to provide default ospec or propogate the error to top layer
                AdPod adPod = CampaignDB.getInstance().getDefaultAdPod();
                oSpec = CampaignDB.getInstance().getOSpecForAdPod(adPod.getId());
            }
        }
        else {
            oSpec = doSiteTargeting(request);
        }
        return oSpec;
    }

    private OSpec getOSpecForGivenName(String oSpecName) throws OSpecNotFoundException {
        OSpec oSpec = CampaignDB.getInstance().getOspec(oSpecName);
        if(oSpec == null) {
            log.error("OSpec not found for the specified name");
            throw new OSpecNotFoundException("OSpec not found for the specified name: " + oSpecName);
        }
        return oSpec;
    }
    
    private OSpec doSiteTargeting(AdDataRequest request) {
        OSpec ospec;
        int locationId       = 0;
        
        String locationIdStr = request.get_store_id();
        String themeName     = request.get_theme();
        String urlName       = request.get_url();

        SortedSet<Handle> results = null;

        if(locationIdStr != null && locationIdStr != "") {
            locationId = Integer.parseInt(locationIdStr);

        }
        try {
            SiteTargetingQuery siteQuery = new SiteTargetingQuery(locationId, urlName, themeName);
            GeoTargetingQuery geoQuery = new GeoTargetingQuery(request.getCountry(), request.getRegion(), request.getCity(), request.getDmacode(), request.get_zip_code(), request.getAreacode());
            AdPodQueryProcessor adPodQueryProcessor = new AdPodQueryProcessor();
            ConjunctQuery cjQuery = new ConjunctQuery(adPodQueryProcessor);
            cjQuery.addQuery(siteQuery);
            cjQuery.addQuery(geoQuery);
            results = cjQuery.exec();
        }
        catch(Exception e) {
            //@todo: log the error and take appropriate action for invalid value
            //@todo: Confirm with management whether to provide some default adpods for unexpected errors
            log.error("Unexpected error. Not able to retrieve any adpods for given request", e);
            e.printStackTrace();
        }

        ospec = pickOneOSpec(results);
        return ospec;
    }

    private OSpec pickOneOSpec(SortedSet<Handle> results) {
        AdPod adPod;

        AdPodHandle handle;
        List<AdPodHandle> list = getHighestScoreAdPodHandles(results);
        if(list.size() == 0) {
            //@todo: Discuss the requirements with business side for displaying default adpods
            adPod = CampaignDB.getInstance().getDefaultAdPod();

        }
        else if(list.size() == 1) {
            handle = list.get(0);
            adPod = handle.getAdpod();
        }
        else {
            handle = selectAdPodHandle(list);
            adPod = handle.getAdpod();
        }

        return CampaignDB.getInstance().getOSpecForAdPod(adPod.getId());
    }

    private AdPodHandle selectAdPodHandle(List<AdPodHandle> list) {
        AdPodHandle handle = null;
        int totalWeight = 0;
        int weightRatio;
        int[] weightArray = new int[list.size()];

        for(int i=0; i<list.size(); i++) {
            totalWeight += list.get(i).getWeight();
            weightArray[i] = list.get(i).getWeight();
        }
        weightRatio = new Random().nextInt(totalWeight);
        Arrays.sort(weightArray);
        int additionFactor = 0;
        for(AdPodHandle aHandle : list) {
            int weight = aHandle.getWeight();
            weight = weight + additionFactor;
            if(weight > weightRatio) {
                handle = aHandle;
                break;
            }
            additionFactor = weight;
        }
        return handle;
    }

    private List<AdPodHandle> getHighestScoreAdPodHandles(SortedSet<Handle> results) {
        List<AdPodHandle> handles = new ArrayList<AdPodHandle>();
        SortedArraySet<Handle> set = new SortedArraySet<Handle>(results, results.first());
        Iterator<Handle> iterator = set.iterator();
        double score = 0.0;
        if(iterator != null) {
            int i = 0;
            double currentScore;
            while(iterator.hasNext()) {
                Handle handle = iterator.next();
                currentScore = handle.getScore();
                if(i == 0) {
                    score = currentScore;
                }
                else if(i > 0) {
                    if(score != currentScore) {
                        break;
                    }
                }
                i++;
                handles.add((AdPodHandle)handle);
            }
        }
        return handles;
    }
}
