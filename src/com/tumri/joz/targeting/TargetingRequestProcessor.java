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
        long startTime = System.nanoTime();
        OSpec oSpec = null;
        if(request == null) {
            return null;
        }

        try {
            String tSpecName = request.get_t_spec();
            if(tSpecName != null && !"".equals(tSpecName)) {
                oSpec = CampaignDB.getInstance().getOspec(tSpecName);
            }
            else {
                oSpec = doSiteTargeting(request);
                if(oSpec != null) {
                    setTargetedRealm(request);
                }
            }
        }
        catch(Throwable t) {
            //It is critical to catch any unexpected error so that the JoZ server doesnt exit
            log.error("Targeting layer: unxepected error. Owner need to look into the issue", t);
            //Continue to provide default O-Spec after logging the error
        }
        //If OSpec match is not found for the given request, pick default realm ospec if revertToDefaultRealm is set to true or null
        try {
        if(oSpec == null) {
            boolean revertToDefaultRealm = (request.get_revert_to_default_realm() != null) && request.get_revert_to_default_realm().booleanValue();
            if(revertToDefaultRealm) {
                oSpec = CampaignDB.getInstance().getDefaultOSpec();
                //@todo: This should be fixed the default-realm should come from properties file and not hard-coded here
                //also the targetedRealm should be set in response object and not request.
                request.setTargetedRealm("http://default-realm/");
            }
        }
        }
        catch(Throwable t) {
            //It is critical to catch any unexpected error so that the JoZ server doesnt exit
            //This error could occur if the campaign data loading failed for some reason
            log.error("Targeting layer: unxepected error. The default o-spec not retrieved", t);
        }

        long endTime =  System.nanoTime();
        long totalTargetingTime = endTime - startTime;


        log.info(request.toString(true));
        log.info("Targeting Processing time: " + (totalTargetingTime/1000) + " usecs");
        log.info("Passing OSpec To Product Selection Processor: " + ((oSpec == null)? null: oSpec.getName()));
        return oSpec;
    }

    //@todo: We should use a response object across all the requestprocessor. Currently we are adding
    //targetedRealm to the request object itself, which should soon be refactored to use response objects across
    //the whole flow of get-ad-data request
    private void setTargetedRealm(AdDataRequest request) {
        String locationIdStr = request.get_store_id();
        String themeName     = request.get_theme();
        String urlName       = request.get_url();
        String targetedRealm = null;
        if(locationIdStr != null && !"".equals(locationIdStr)) {
            targetedRealm = locationIdStr;
        }
        else if(urlName != null && !"".equals(urlName)) {
            targetedRealm = urlName;
        }
        else if(themeName != null && !"".equals(themeName)) {
            targetedRealm = themeName;
        }
        request.setTargetedRealm(targetedRealm);
    }

    private OSpec doSiteTargeting(AdDataRequest request) {
        OSpec ospec;
        int locationId       = 0;
        
        String locationIdStr = request.get_store_id();
        String themeName     = request.get_theme();
        String urlName       = request.get_url();

        SortedSet<Handle> results = null;

        if(locationIdStr != null && !"".equals(locationIdStr)) {
            locationId = Integer.parseInt(locationIdStr);

        }
        try {
            SiteTargetingQuery siteQuery = new SiteTargetingQuery(locationId, urlName, themeName);
            GeoTargetingQuery geoQuery = new GeoTargetingQuery(request.getCountry(), request.getRegion(), request.getCity(), request.getDmacode(), request.get_zip_code(), request.getAreacode());
            AdPodQueryProcessor adPodQueryProcessor = new AdPodQueryProcessor();
            ConjunctQuery cjQuery = new ConjunctQuery(adPodQueryProcessor);
            cjQuery.setStrict(true);
            cjQuery.addQuery(siteQuery);
            cjQuery.addQuery(geoQuery);
            results = cjQuery.exec();
        }
        catch(Exception e) {
            log.error("Unexpected error. Not able to retrieve any adpods for given request", e);
        }

        ospec = pickOneOSpec(results);
        return ospec;
    }

    private OSpec pickOneOSpec(SortedSet<Handle> results) {
        OSpec oSpec;
        AdPodHandle handle;
        List<AdPodHandle> list = getHighestScoreAdPodHandles(results);
        if(list == null || list.size() == 0) {
            //No ospec will get selected by targeting layer so return null
            return null;
        }
        else if(list.size() == 1) {
            handle = list.get(0);
            oSpec = CampaignDB.getInstance().getOSpecForAdPod(handle.getOid());
        }
        else {
            handle = selectAdPodHandle(list);
            oSpec = CampaignDB.getInstance().getOSpecForAdPod(handle.getOid());
        }

        return oSpec;
    }

    private AdPodHandle selectAdPodHandle(List<AdPodHandle> list) {
        AdPodHandle handle = null;
        int totalWeight = 0;
        int weightRatio = 0;
        int[] weightArray = new int[list.size()];
        for(int i=0; i<list.size(); i++) {
            weightArray[i] = Math.abs(list.get(i).getWeight());
            totalWeight += list.get(i).getWeight();
        }

        if(totalWeight == 0) {
            //Invalid weight assigned to the mappings. Overriding with equal weight for all the adpods
            for(int i=0; i<list.size(); i++) {
                totalWeight += 1;
            }
        }
        try {
            weightRatio = new Random().nextInt(totalWeight);
        }
        catch(IllegalArgumentException e) {
            weightRatio = 0;
            log.warn("Calculated totalWeight was not positive. totalWeight:" + totalWeight);
        }
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
        if(results != null) {
            SortedArraySet<Handle> set = new SortedArraySet<Handle>(results, new AdPodHandle(0, 0));
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
        }
        return handles;
    }
}
