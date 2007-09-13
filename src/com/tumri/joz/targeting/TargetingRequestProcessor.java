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

import java.util.SortedSet;

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
        //oSpec = get the ospec for given tspec name from campaignDB

        oSpec = doSiteTargeting(request);

        return oSpec;
    }

    private OSpec doSiteTargeting(AdDataRequest request) {
        OSpec ospec = null;
        int locationId       = 0;
        
        String locationIdStr = request.get_store_id();
        String themeName     = request.get_theme();
        String urlName       = request.get_url();

        SortedSet<Handle> results = null;
        SortedSet<Handle> siteResults = null;
        SortedSet<Handle> geoResults = null;

        if(locationIdStr != null && locationIdStr != "") {
            locationId = new Integer(locationIdStr).intValue();

        }
        try {
            SiteTargetingQuery siteQuery = new SiteTargetingQuery(locationId, urlName, themeName);
            //siteResults = siteQuery.exec();
            GeoTargetingQuery geoQuery = new GeoTargetingQuery(request.getCountries(), request.getRegions(), request.getCities(), request.getDmacodes(), request.getZipcodes(), request.getAreacodes());
            //geoResults = geoQuery.exec();
            AdPodQueryProcessor adPodQueryProcessor = new AdPodQueryProcessor();
            ConjunctQuery cjQuery = new ConjunctQuery(adPodQueryProcessor);
            cjQuery.addQuery(siteQuery);
            cjQuery.addQuery(geoQuery);
            results = cjQuery.exec();
        }
        catch(Exception e) {
            //ToDo: take appropriate action for invalid value
            e.printStackTrace();
        }


        ospec = pickOneOSpec(results);
        return ospec;
    }

    private OSpec pickOneOSpec(SortedSet<Handle> results) {
        AdPodHandle handle = (AdPodHandle)results.first();
        AdPod adPod = handle.getAdpod();
        return CampaignDB.getInstance().getOSpecForAdPod(adPod.getId());
    }

}
