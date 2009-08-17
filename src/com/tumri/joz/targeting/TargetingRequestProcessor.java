package com.tumri.joz.targeting;

import com.tumri.cma.domain.*;
import com.tumri.joz.Query.*;
import com.tumri.joz.campaign.AdPodHandle;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.wm.RecipeSelector;
import com.tumri.joz.campaign.wm.WMIndex;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.products.Handle;
import com.tumri.utils.data.SortedArraySet;
import com.tumri.utils.stats.PerformanceStats;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * TargetingRequestProcessor handles the incoming adrequest and does the site and geo targeting against the request data.
 *
 * @author bpatel
 * @author nipun
 */
public class TargetingRequestProcessor {

    private static TargetingRequestProcessor processor = null;
	public static final String PROCESS_STATS_ID = "TG";

    private TargetingRequestProcessor() {
    }

    public static TargetingRequestProcessor getInstance() {
        if (processor == null) {
          synchronized (TargetingRequestProcessor.class) {
            if (processor == null) {
              processor = new TargetingRequestProcessor();
            }
          }
        }
        return processor;
    }

    private static Logger log = Logger.getLogger (TargetingRequestProcessor.class);

    public Recipe processRequest(AdDataRequest request, Features features) {
        //long startTime = System.nanoTime();
	    PerformanceStats.getInstance().registerStartEvent(PROCESS_STATS_ID);
        Recipe theRecipe = null;
        OSpec oSpec;
        if(request == null) {
            return null;
        }

        try {
            String tSpecName = request.get_t_spec();
            int recipeId = request.getRecipeId();
            if (recipeId > 0) {
                //If Recipe Id is provided - then target to specific set of TSpecs
                theRecipe = CampaignDB.getInstance().getRecipe(recipeId);
                if (theRecipe ==null) {
                    log.error("Targeted Recipe ID : " + recipeId + " not present in Campaign DB");
                }
                if (features!=null && theRecipe !=null) {
                    features.setRecipeId(theRecipe.getId());
                }
            } else if(tSpecName != null && !"".equals(tSpecName)) {
                //If tspec name is provided - then  target to the OSpec name
                //Create a new recipe, add all the tspecs from the ospec into it.
                oSpec = CampaignDB.getInstance().getOspec(tSpecName);
                if (oSpec != null) {
                    theRecipe = new Recipe();
                    theRecipe.setId(0);
                    theRecipe.setName(oSpec.getName());
                    List<TSpec> tspecs = oSpec.getTspecs();
                    List<RecipeTSpecInfo> queries = new ArrayList<RecipeTSpecInfo>();
                    for (TSpec ts : tspecs) {
                        RecipeTSpecInfo queryInfoRecipe = new RecipeTSpecInfo();
                        queryInfoRecipe.setTspecId(ts.getId());
                        queries.add(queryInfoRecipe);
                    }
                    theRecipe.setTSpecInfo(queries);
                    if (features!= null) {
                        features.setRecipeId(theRecipe.getId());
                        features.setRecipeName(theRecipe.getName());
                    }
                } else {
                    log.error("Targeted TSpec : " + tSpecName + " not present in Campaign DB");
                }
            }
            else {
                //Default to Targeting
                SiteTargetingResults str = doSiteTargeting(request, features);
                theRecipe = str.getCurrRecipe();
                if(theRecipe != null && features!= null) {
                    features.setAdPodId(str.getAdPodId());
                    features.setAdpodName(str.getAdPodName());
                    features.setCampaignId(str.getCampaignId());
                    features.setCampaignName(str.getCampaignName());
	                features.setCampaignClientId(str.getCampaignClientId());
	                features.setCampaignClientName(str.getCampaignClientName());
                    features.setRecipeId(theRecipe.getId());
                    features.setRecipeName(theRecipe.getName());
                    String theme = request.get_theme();
                    String adtype = request.getAdType();
                    Integer targetedLocationId = null;
                    if (theme != null&&!theme.equals("")&&adtype!=null&&!"".equals(adtype)) {
                        features.setTargetedLocationName(theme);
                        targetedLocationId = CampaignDB.getInstance().getLocationIdForName(theme+adtype);
                        if (targetedLocationId!=null){
                            features.setTargetedLocationId(Integer.toString(targetedLocationId));
                        }
                    }
                    String locationIdStr = request.get_store_id();
                    if (locationIdStr!= null&&!locationIdStr.equals("")) {
                        try {
                            targetedLocationId = Integer.parseInt(locationIdStr);
                            features.setTargetedLocationId(locationIdStr);
                        } catch(NumberFormatException e) {
                            log.warn("Invalid value specified for the location : " + locationIdStr,e);
                        }
                    }
                    if (targetedLocationId!=null) {
                        Location loc = CampaignDB.getInstance().getLocation(targetedLocationId);
                        if (loc!=null) {
                            features.setLocationClientId(loc.getClientId());
                            features.setLocationClientName(loc.getClientName());
	                        features.setTargetedLocationName(loc.getName());
                        }
                    }
                }
            }
        }
        catch(NumberFormatException e){
        	log.warn("Invalid value specified for the location : ",e);
        }
        catch(Throwable t) {
            //It is critical to catch any unexpected error so that the JoZ server doesnt exit
            log.error("Targeting layer: unxepected error. Owner need to look into the issue", t);
        }

        //Do not fall back to any default tspec.
	    PerformanceStats.getInstance().registerFinishEvent(PROCESS_STATS_ID);
//        long endTime =  System.nanoTime();
//        long totalTargetingTime = endTime - startTime;

        if (theRecipe == null) {
            log.error("Could not target Recipe for the given request. " + request.toString(true));
        } else {
            log.debug(request.toString(true));
        }
        return theRecipe;
    }

    /**
     * Select an Recipe for the given request
     * @param request  - the request
     * @return  results
     */
    private SiteTargetingResults doSiteTargeting(AdDataRequest request, Features feature) {
        Recipe theRecipe = null;
        int locationId       = 0;
        
        String locationIdStr = request.get_store_id();
        String themeName     = request.get_theme();
        String urlName       = request.get_url();
        String adType = request.getAdType();
        HashMap<String, String> extVarsMap = request.getExtTargetFields();

        SortedSet<Handle> results = null;

        if(locationIdStr != null && !"".equals(locationIdStr)) {
            locationId = Integer.parseInt(locationIdStr);

        }
        try {
            SiteTargetingQuery siteQuery = new SiteTargetingQuery(locationId, themeName, adType);
            UrlTargetingQuery urlQuery = new UrlTargetingQuery(urlName);
            GeoTargetingQuery geoQuery = new GeoTargetingQuery(request.getCountry(), request.getRegion(), request.getCity(), request.getDmacode(), request.get_zip_code(), request.getAreacode());
            TimeTargetingQuery timeQuery = new TimeTargetingQuery();
            AdTypeTargetingQuery adTypeQuery = new AdTypeTargetingQuery(adType);

            AdPodQueryProcessor adPodQueryProcessor = new AdPodQueryProcessor();
            ConjunctQuery cjQuery = new ConjunctQuery(adPodQueryProcessor);
            cjQuery.setStrict(true);
            cjQuery.addQuery(siteQuery);           
            cjQuery.addQuery(geoQuery);
            cjQuery.addQuery(urlQuery);          
            cjQuery.addQuery(timeQuery);
            cjQuery.addQuery(adTypeQuery);
            Set<String> extVars = extVarsMap.keySet();
            for(String extVar : extVars) {
                ExternalVariableTargetingQuery externalVariableQuery = new ExternalVariableTargetingQuery(extVar, extVarsMap.get(extVar));
                cjQuery.addQuery(externalVariableQuery);
            }
            results = cjQuery.exec();

        }
        catch(Exception e) {
            log.error("Unexpected error. Not able to retrieve any adpods for given request", e);
        }

        //1. Get the Highest scored adpod
        AdPodHandle handle = pickOneAdPod(results);
        SiteTargetingResults str = new SiteTargetingResults();

        //2. Get the recipe for the Adpod
        if (handle != null) {
            Campaign theCampaign = CampaignDB.getInstance().getCampaign(handle);
            if (theCampaign!= null) {
                str.setCampaignId(theCampaign.getId());
                str.setCampaignName(theCampaign.getName());
                str.setCampaignClientId(theCampaign.getClientId());
                str.setCampaignClientName(theCampaign.getClientName());
            }
            AdPod theAdPod = CampaignDB.getInstance().getAdPod(handle);
            if (theAdPod!=null) {
                str.setAdPodId(theAdPod.getId());
                str.setAdPodName(theAdPod.getName());
                log.debug("Targeted adpod : " + theAdPod.getName() + " . id= " + theAdPod.getId());
                theRecipe = selectRecipe(request, theAdPod,feature );
                if (theRecipe == null) {
                    log.error("Could not find the recipe for the selected adpod. Not able to select recipe");
                } 
            }
        }
        if (theRecipe!=null) {
            log.debug("Targeted recipe : " + theRecipe.getName() + " . id= " + theRecipe.getId());
            str.setCurrRecipe(theRecipe);
        }
        return str;
    }

    private Recipe selectRecipe(AdDataRequest request, AdPod theAdPod, Features feature) {
        Recipe theRecipe;
        RecipeSelector proc = RecipeSelector.getInstance();
        Map<WMIndex.Attribute, String> contextMap = new HashMap<WMIndex.Attribute, String>();
        if (request.getLineId()!=null) {
            contextMap.put(WMIndex.Attribute.kLineId, request.getLineId());
        }
        if (request.getRegion()!=null) {
            contextMap.put(WMIndex.Attribute.kState, request.getRegion());
        }
        theRecipe = proc.getRecipe(theAdPod.getId(), theAdPod.getRecipes(), contextMap, feature);
        return theRecipe;
    }

    private AdPodHandle pickOneAdPod(SortedSet<Handle> results) {
        AdPodHandle handle;
        List<AdPodHandle> list = getHighestScoreAdPodHandles(results);
        if(list == null || list.size() == 0) {
            //No adpod will get selected by targeting layer so return null
            return null;
        }
        else if(list.size() == 1) {
            handle = list.get(0);
        }
        else {
            handle = selectAdPodHandle(list);
        }

        return handle;
    }

    private AdPodHandle selectAdPodHandle(List<AdPodHandle> list) {
        AdPodHandle handle = null;
        int totalWeight = 0;
        int weightRatio;
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

    @SuppressWarnings("unchecked")
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

    private class SiteTargetingResults {
        Recipe currRecipe = null;
        Integer adPodId = null;
        Integer campaignId = null;
        Integer campaignClientId = null;
        String campaignClientName = null;
        String campaignName = null;
        String adPodName = null;

        public Integer getAdPodId() {
            return adPodId;
        }

        public void setAdPodId(Integer adPodId) {
            this.adPodId = adPodId;
        }

        public Integer getCampaignId() {
            return campaignId;
        }

        public void setCampaignId(Integer campaignId) {
            this.campaignId = campaignId;
        }

        public Recipe getCurrRecipe() {
            return currRecipe;
        }

        public void setCurrRecipe(Recipe currRecipe) {
            this.currRecipe = currRecipe;
        }

        public String getAdPodName() {
            return adPodName;
        }

        public void setAdPodName(String adPodName) {
            this.adPodName = adPodName;
        }

        public String getCampaignName() {
            return campaignName;
        }

        public void setCampaignName(String campaignName) {
            this.campaignName = campaignName;
        }


        public Integer getCampaignClientId() {
            return campaignClientId;
        }

        public void setCampaignClientId(Integer campaignClientId) {
            this.campaignClientId = campaignClientId;
        }

        public String getCampaignClientName() {
            return campaignClientName;
        }

        public void setCampaignClientName(String campaignClientName) {
            this.campaignClientName = campaignClientName;
        }
    }
}
