package com.tumri.joz.targeting;

import com.tumri.cma.domain.*;
import com.tumri.cma.rules.CreativeInstance;
import com.tumri.cma.rules.CreativeSet;
import com.tumri.cma.util.ExperienceUtils;
import com.tumri.joz.Query.*;
import com.tumri.joz.campaign.AdPodHandle;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.wm.VectorSelectionException;
import com.tumri.joz.campaign.wm.VectorTargetingProcessor;
import com.tumri.joz.campaign.wm.VectorTargetingResult;
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

    public TargetingResults processRequest(AdDataRequest request, Features features) {
        PerformanceStats.getInstance().registerStartEvent(PROCESS_STATS_ID);
        TargetingResults trs = null;
        OSpec oSpec;
        if(request == null) {
            return null;
        }

        try {
            String tSpecName = request.get_t_spec();
            long recipeId = request.getRecipeId();
            int expId = request.getExpId();
            if (expId > 0 || recipeId > Integer.MAX_VALUE) {
                int varId = -1;
                if (recipeId > Integer.MAX_VALUE) {
                    int[] details = ExperienceUtils.getRecipeIdDetails(recipeId);
                    expId = details[1];
                    varId = details[0];
                }
                Experience exp = CampaignDB.getInstance().getExperience(expId);
                if (exp ==null) {
                    log.error("Targeted Experience Id : " + expId + " not present in Campaign DB");
                } else {
                    trs = new TargetingResults();
                    trs.setExperience(exp);
                    trs.setInfoListExperience(exp.getOfferLists());
                    CAM theCAM = exp.getCam();
                    //Check if variation id is provided.
                    if (varId==-1) {
                        varId = request.getVariationId();
                    }
                    CreativeInstance ci = null;
                    if (varId>-1) {
                        //Select the creative instance for the given variation id
                        try {
                            int[] selectedDimIds = ExperienceUtils.getAttributeIds(varId);
                            //Construct a creative set
                            CreativeSet cs = new CreativeSet(theCAM);
                            for (int i=0;i<theCAM.getNumberOfDimensions();i++){
                                cs.add(i,selectedDimIds[i]);
                            }
                            ci = cs.getCreativeInstance();
                        } catch (Exception e) {
                            log.error("Invalid variation id for the given cam", e);
                            ci = null;
                        }

                    }
                    if (ci==null) {
                        //Select the creative instance from the CAM.
                        VectorTargetingProcessor proc = VectorTargetingProcessor.getInstance();
                        theCAM = handleZeroCamDimension(theCAM);
                        VectorTargetingResult vtr = proc.processRequest(-1, expId, theCAM, request, features);
                        ci = vtr.getCi();
                        trs.setListingClause(vtr.getLc());
                    }
                    handleFixedDimensions(theCAM, trs);

                    String[] attribValues = ci.getAttributes();
                    int[] dimIdx = ci.getAttributeIds();

                    trs.setAttributePositions(dimIdx);
                    trs.setAttributeValues(attribValues);
                    CAMDimension[] dims = theCAM.getCamDimensions();
                    String[] attrNames = new String[dims.length];
                    CAMDimensionType[] dimTypes = new CAMDimensionType[dims.length];
                    int i = 0;
                    for (CAMDimension dim: dims) {
                        attrNames[i] = dim.getName();
                        dimTypes[i] = dim.getType();
                        i++;
                    }
                    trs.setCamDimensionNames(attrNames);
                    trs.setCamDimensionTypes(dimTypes);
                    features.setExpName(exp.getName());
                }
            } else if (recipeId > 0) {
                //If Recipe Id is provided - then target to specific set of TSpecs

                Recipe theRecipe = CampaignDB.getInstance().getRecipe(recipeId);

                if (theRecipe ==null) {
                    log.error("Targeted Recipe ID : " + recipeId + " not present in Campaign DB");
                }  else {
                    trs =  new TargetingResults();
                    trs.setRecipe(theRecipe);
                }
                if (features!=null && trs !=null) {
                    //Get the campaign client name from the tspec - we need this for the listings lookup
                    List<RecipeTSpecInfo> infoListRecipe = trs.getInfoListRecipe();
                    if (infoListRecipe != null&& !infoListRecipe.isEmpty()) {
                        RecipeTSpecInfo info = infoListRecipe.get(0);
                        int tspecId = info.getTspecId();
                        TSpec theTSpec = CampaignDB.getInstance().getTspec(tspecId);
                        if (theTSpec!=null) {
                            String advName = theTSpec.getIncludedProviders().get(0).getName();
                            features.setCampaignClientName(advName);
                        }
                    }
                    features.setRecipeId(theRecipe.getId());
                }
            } else if(tSpecName != null && !"".equals(tSpecName)) {
                //If tspec name is provided - then  target to the OSpec name
                //Create a new recipe, add all the tspecs from the ospec into it.
                oSpec = CampaignDB.getInstance().getOspec(tSpecName);
                Recipe theRecipe = null;
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
                    trs = new TargetingResults();
                    trs.setRecipe(theRecipe);
                } else {
                    log.error("Targeted TSpec : " + tSpecName + " not present in Campaign DB");
                }
            }
            else {
                //Default to Targeting
                SiteTargetingResults str = doSiteTargeting(request, features);
                trs = str.getCurrCreative();
                if(trs != null && features!= null) {
                    features.setAdPodId(str.getAdPodId());
                    features.setAdpodName(str.getAdPodName());
                    features.setCampaignId(str.getCampaignId());
                    features.setCampaignName(str.getCampaignName());
                    features.setCampaignClientId(str.getCampaignClientId());
                    features.setCampaignClientName(str.getCampaignClientName());
                    if (trs.getRecipe()!=null) {
                        features.setRecipeId(trs.getRecipe().getId());
                        features.setRecipeName(trs.getRecipe().getName());
                    }
                    Integer targetedLocationId = null;
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
            log.error("Targeting layer: unexpected error. Owner need to look into the issue", t);
        }

        PerformanceStats.getInstance().registerFinishEvent(PROCESS_STATS_ID);

        if (trs == null) {
            log.error("Could not target Creative Instance for the given request. " + request.toString());
        } else {
            log.debug(request.toString());
        }
        return trs;
    }

    /**
     * Select an Recipe for the given request
     * @param request  - the request
     * @return  results
     */
    private SiteTargetingResults doSiteTargeting(AdDataRequest request, Features feature) {
        TargetingResults targetingResults = null;
        int locationId       = 0;

        String locationIdStr = request.get_store_id();
        String urlName       = request.get_url();
        String adType = request.getAdType();
        HashMap<String, String> extVarsMap = request.getExtTargetFields();

        SortedSet<Handle> results = null;

        if(locationIdStr != null && !"".equals(locationIdStr)) {
            locationId = Integer.parseInt(locationIdStr);

        }
        try {
            SiteTargetingQuery siteQuery = new SiteTargetingQuery(locationId, adType);
            UrlTargetingQuery urlQuery = new UrlTargetingQuery(urlName);
            GeoTargetingQuery geoQuery = new GeoTargetingQuery(request.getCountry(), request.getRegion(), request.getCity(), request.getDmacode(), request.get_zip_code(), request.getAreacode());
            TimeTargetingQuery timeQuery = new TimeTargetingQuery();
            AgeTargetingQuery ageQuery = new AgeTargetingQuery(request.getAge());
            BTTargetingQuery btQuery = new BTTargetingQuery(request.getBt());
            CCTargetingQuery msQuery = new CCTargetingQuery(request.getCc());
            HHITargetingQuery hhiQuery = new HHITargetingQuery(request.getHhi());
            GenderTargetingQuery genQuery = new GenderTargetingQuery(request.getGender());
            AdTypeTargetingQuery adTypeQuery = new AdTypeTargetingQuery(adType);

            AdPodQueryProcessor adPodQueryProcessor = new AdPodQueryProcessor();
            ConjunctQuery cjQuery = new ConjunctQuery(adPodQueryProcessor);
            cjQuery.setStrict(true);
            cjQuery.addQuery(siteQuery);
            cjQuery.addQuery(geoQuery);
            cjQuery.addQuery(urlQuery);
            cjQuery.addQuery(timeQuery);
            cjQuery.addQuery(adTypeQuery);
            cjQuery.addQuery(ageQuery);
            cjQuery.addQuery(btQuery);
            cjQuery.addQuery(msQuery);
            cjQuery.addQuery(hhiQuery);
            cjQuery.addQuery(genQuery);
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
                try {
                    targetingResults = selectCreative(request, theAdPod,feature );
                } catch (VectorSelectionException e) {
                    log.error("Creative selection has failed");
                }
                if (targetingResults == null) {
                    log.error("Could not find the creative for the selected adpod. Not able to select creative");
                }
            }
        }
        if (targetingResults!=null) {
            if (targetingResults.getRecipe()!=null) {
                log.debug("Targeted recipe : " + targetingResults.getRecipe() + " . id= " + targetingResults.getRecipe().getId());
            } else {
                log.debug("Targeted Creative instance : " + targetingResults.getAttributeValues());
            }
            str.setCurrCreative(targetingResults);
        }
        return str;
    }

    private TargetingResults selectCreative(AdDataRequest request, AdPod theAdPod, Features feature) throws VectorSelectionException {
        VectorTargetingProcessor proc = VectorTargetingProcessor.getInstance();

        List<Recipe> recipes = theAdPod.getRecipes();
        CAM theCAM = null;
        Experience exp = null;
        CreativeInstance ci = null;
        int adpodId = theAdPod.getId();
        int expId = theAdPod.getExperienceId();

        if (recipes!=null || expId <=0) {
            theCAM = CampaignDB.getInstance().getDefaultCAM(theAdPod.getId());
            expId = -1;
        } else {
            adpodId = -1;
            exp = CampaignDB.getInstance().getExperience(expId);
            if (exp!=null) {
                theCAM = exp.getCam();
            }
        }

        if (theCAM==null) {
            throw new RuntimeException("Could not get CAM for the given request");
        }

        theCAM = handleZeroCamDimension(theCAM);
        VectorTargetingResult vtr = proc.processRequest(adpodId, expId, theCAM, request, feature);
        ci = vtr.getCi();

        String[] attribValues = ci.getAttributes();
        int[] dimIdx = ci.getAttributeIds();

        TargetingResults trs = new TargetingResults();
        if (dimIdx.length ==1 && theCAM.getCAMDimension(CAMDimensionType.RECIPEID)!=null) {
            //This is recipe targeting
            int recipeId = Integer.parseInt(attribValues[0]);
            Recipe theRecipe = CampaignDB.getInstance().getRecipe(recipeId);
            trs.setRecipe(theRecipe);
            trs.setInfoListRecipe(theRecipe.getTspecInfoList());
        } else {
            //Experience based targeting
            int variationId = ExperienceUtils.getVariationId(dimIdx);
            feature.setRecipeId(variationId); // Set the variation id into the recipe id field
            trs.setAttributePositions(dimIdx);
            trs.setAttributeValues(attribValues);
            CAMDimension[] dims = theCAM.getCamDimensions();
            String[] attrNames = new String[dims.length];
            CAMDimensionType[] dimTypes = new CAMDimensionType[dims.length];
            int i = 0;
            for (CAMDimension dim: dims) {
                attrNames[i] = dim.getName();
                dimTypes[i] = dim.getType();
                i++;
            }
            trs.setCamDimensionNames(attrNames);
            trs.setCamDimensionTypes(dimTypes);
            if (exp!=null) {
                trs.setInfoListExperience(exp.getOfferLists());
            }
            if (exp!=null) {
                feature.setExpId(expId);
                feature.setExpName(exp.getName());
                trs.setExperience(exp);
            }
            handleFixedDimensions(theCAM, trs);


        }
        trs.setListingClause(vtr.getLc());
        return trs;
    }

    private CAM handleZeroCamDimension(CAM theCAM) throws VectorSelectionException {
        ArrayList<CAMDimension> fixedDimensions = theCAM.getFixedDimensions();
        if (theCAM.getCamDimensions()==null || theCAM.getCamDimensions().length==0) {
            if (fixedDimensions!=null&&!fixedDimensions.isEmpty())  {
                //Make the CAM non empty
                int max = (fixedDimensions.size()>5)?5:fixedDimensions.size();
                CAMDimension[] camDims = new CAMDimension[max];
                for (int i=0;i<max;i++) {
                   CAMDimension dim = fixedDimensions.get(i);
                    switch(i) {
                        case 0:
                            dim.setType("D1");
                            break;
                        case 1:
                            dim.setType("D2");
                            break;
                        case 2:
                            dim.setType("D3");
                            break;
                        case 3:
                            dim.setType("D4");
                            break;
                        case 4:
                            dim.setType("D5");
                            break;
                        default:
                    }
                    camDims[i] = dim;
                }
                theCAM = new CAM(camDims);
                theCAM.setFixedDimensions(fixedDimensions);
            } else {
                //No dimensions - not supported.
                throw new VectorSelectionException("CAM does not contain Fixed/Dynamic dimensions");
            }
        }
        return theCAM;
    }

    private void handleFixedDimensions(CAM theCAM, TargetingResults trs) {
        HashMap<String, String> fixDimMap = new HashMap<String, String>();
        List<CAMDimension> fixDimList = theCAM.getFixedDimensions();
        if (fixDimList!=null) {
            for (CAMDimension dim: fixDimList) {
                String[] values = dim.getValues();
                if (values!=null && values.length>0) {
                    fixDimMap.put(dim.getName(), values[0]);
                }
            }
        }
        if (!fixDimMap.isEmpty()) {
           trs.setFixedDimMap(fixDimMap);
        }
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
            //Avoid warning on toArray operator on set intersector
            SortedArraySet<Handle> set = new SortedArraySet<Handle>(new AdPodHandle(0, 0));
            for (Handle h:results) {
                set.add(h);
            }
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
        TargetingResults currCreative = null;
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

        public TargetingResults getCurrCreative() {
            return currCreative;
        }

        public void setCurrCreative(TargetingResults currCreative) {
            this.currCreative = currCreative;
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

        public void addCreativeElem(String attribName, String attribValue) {

        }
    }
}
