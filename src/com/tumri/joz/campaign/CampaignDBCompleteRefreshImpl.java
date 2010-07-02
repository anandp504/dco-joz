package com.tumri.joz.campaign;

import com.tumri.cma.domain.*;
import com.tumri.cma.rules.CreativeSet;
import com.tumri.joz.campaign.wm.*;
import com.tumri.joz.campaign.wm.loader.WMDBLoader;
import com.tumri.joz.index.AdpodIndex;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.targeting.TargetingScoreHelper;
import com.tumri.utils.Pair;
import com.tumri.utils.data.RWLocked;
import com.tumri.utils.data.RWLockedTreeMap;
import com.tumri.utils.data.SortedBag;
import com.tumri.utils.data.SortedListBag;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implements the CampaignDB interface and provides an In-Memory database for campaign related objects.
 * This class does the complete refresh of cached data, so every time a new campaign content
 * arrives, the class clears the existing cache of campaign data and adds the new data to its cache.
 * This class is therefore suitable only for scenarios where all the data will be sent to CampaignDB, for delta support,
 * other implementation of CampaignDB should be used that supports delta content.
 *
 * @author bpatel
 */
@SuppressWarnings({"UnnecessaryLocalVariable", "deprecation"})
public class CampaignDBCompleteRefreshImpl extends CampaignDB {
    private static CampaignDBCompleteRefreshImpl instance = new CampaignDBCompleteRefreshImpl();
    private static Logger log = Logger.getLogger (CampaignDBCompleteRefreshImpl.class);
    private static Logger fatallog = Logger.getLogger("fatal");

    private AtomicReference<RWLockedTreeMap<Integer,Campaign>> campaignMap   = new AtomicReference<RWLockedTreeMap<Integer, Campaign>>(new RWLockedTreeMap<Integer, Campaign>());
    private AtomicReference<RWLockedTreeMap<Integer, AdPod>>    adPodMap      = new AtomicReference<RWLockedTreeMap<Integer, AdPod>>(new RWLockedTreeMap<Integer, AdPod>());
    private AtomicReference<RWLockedTreeMap<Integer, Experience>> expMap      = new AtomicReference<RWLockedTreeMap<Integer, Experience>>(new RWLockedTreeMap<Integer, Experience>());
    private AtomicReference<RWLockedTreeMap<Integer, CAM>> adPodCamMap      = new AtomicReference<RWLockedTreeMap<Integer, CAM>>(new RWLockedTreeMap<Integer, CAM>());

    //Need to deprecate the date OSpec
    private AtomicReference<RWLockedTreeMap<Integer, OSpec>>    ospecMap      = new AtomicReference<RWLockedTreeMap<Integer, OSpec>>(new RWLockedTreeMap<Integer, OSpec>());
    private AtomicReference<RWLockedTreeMap<String,OSpec>>    ospecNameMap   = new AtomicReference<RWLockedTreeMap<String, OSpec>>(new RWLockedTreeMap<String, OSpec>());

    private AtomicReference<RWLockedTreeMap<Integer, Geocode>>  geocodeMap    = new AtomicReference<RWLockedTreeMap<Integer, Geocode>>(new RWLockedTreeMap<Integer, Geocode>());
    private AtomicReference<RWLockedTreeMap<Integer, Url>>      urlMap        = new AtomicReference<RWLockedTreeMap<Integer, Url>>(new RWLockedTreeMap<Integer, Url>());
    private AtomicReference<RWLockedTreeMap<String, Url>>       urlNameMap    = new AtomicReference<RWLockedTreeMap<String, Url>>(new RWLockedTreeMap<String, Url>());

    //Use recipe map to maintain the recipe id to recipe lookup
    private AtomicReference<RWLockedTreeMap<Long, Recipe>>   recipeMap    = new AtomicReference<RWLockedTreeMap<Long, Recipe>>(new RWLockedTreeMap<Long, Recipe>());


    private AtomicReference<RWLockedTreeMap<Integer,Location>> locationMap   = new AtomicReference<RWLockedTreeMap<Integer, Location>>(new RWLockedTreeMap<Integer, Location>());

    //Map adpod Id to ospec ID
    private AtomicReference<RWLockedTreeMap<Integer, Integer>> adPodOSpecMap = new AtomicReference<RWLockedTreeMap<Integer, Integer>>(new RWLockedTreeMap<Integer, Integer>());

    //Map adpod Id to campaign ID
    private AtomicReference<RWLockedTreeMap<Integer, Integer>> adPodCampaignMap = new AtomicReference<RWLockedTreeMap<Integer, Integer>>(new RWLockedTreeMap<Integer, Integer>());

    //Maintain TSpec/Listing query map
    private AtomicReference<RWLockedTreeMap<Integer, TSpec>>    tspecMap      = new AtomicReference<RWLockedTreeMap<Integer, TSpec>>(new RWLockedTreeMap<Integer, TSpec>());

    // Map the handles for all geocode associated adpods to avoid re-creating handles for multiple geocode elements
    // refering to same adpod.
    private AtomicReference<RWLockedTreeMap<Integer, Handle>> adPodConuntryHandlesMap = new AtomicReference<RWLockedTreeMap<Integer, Handle>>(new RWLockedTreeMap<Integer, Handle>());
    private AtomicReference<RWLockedTreeMap<Integer,Handle>> adPodRegionHandlesMap   = new AtomicReference<RWLockedTreeMap<Integer, Handle>>(new RWLockedTreeMap<Integer, Handle>());
    private AtomicReference<RWLockedTreeMap<Integer,Handle>> adPodCityHandlesMap     = new AtomicReference<RWLockedTreeMap<Integer, Handle>>(new RWLockedTreeMap<Integer, Handle>());
    private AtomicReference<RWLockedTreeMap<Integer,Handle>> adPodZipcodeHandlesMap  = new AtomicReference<RWLockedTreeMap<Integer, Handle>>(new RWLockedTreeMap<Integer, Handle>());
    private AtomicReference<RWLockedTreeMap<Integer,Handle>> adPodDmacodeHandlesMap  = new AtomicReference<RWLockedTreeMap<Integer, Handle>>(new RWLockedTreeMap<Integer, Handle>());
    private AtomicReference<RWLockedTreeMap<Integer,Handle>> adPodAreacodeHandlesMap = new AtomicReference<RWLockedTreeMap<Integer, Handle>>(new RWLockedTreeMap<Integer, Handle>());

    //This map is only used by dynamic data request - incorp-mapping-delta.
    private AtomicReference<RWLockedTreeMap<Integer,Handle>> adPodGeoNoneHandlesMap = new AtomicReference<RWLockedTreeMap<Integer, Handle>>(new RWLockedTreeMap<Integer, Handle>());
    private AtomicReference<RWLockedTreeMap<Integer,Handle>> adPodUrlNoneHandlesMap = new AtomicReference<RWLockedTreeMap<Integer, Handle>>(new RWLockedTreeMap<Integer, Handle>());
    private AtomicReference<RWLockedTreeMap<Integer,Handle>>  adPodExternalVariableNoneHandlesMap = new AtomicReference<RWLockedTreeMap<Integer, Handle>>(new RWLockedTreeMap<Integer, Handle>());

    // All indices required in targeting
    private AtomicAdpodIndex<Integer, Handle> adpodLocationMappingIndex = new AtomicAdpodIndex<Integer, Handle>(new AdpodIndex<Integer, Handle>(AdpodIndex.Attribute.kLocation));
    private AtomicAdpodIndex<String, Handle>  adpodUrlMappingIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kUrl));
    private AtomicAdpodIndex<String, Handle>  adpodRunOfNetworkIndex    = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kRunofNetwork));
    private AtomicAdpodIndex<String, Handle>  adpodGeoNoneIndex         = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kGeoNone));
    private AtomicAdpodIndex<String, Handle>  adpodUrlNoneIndex         = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kUrlNone));

    private AtomicAdpodIndex<String, Handle>  adpodGeoCountryIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kCountryCode));
    private AtomicAdpodIndex<String, Handle>  adpodGeoRegionIndex       = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kRegionCode));
    private AtomicAdpodIndex<String, Handle>  adpodGeoCityIndex         = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kCityCode));
    private AtomicAdpodIndex<String, Handle>  adpodGeoDmacodeIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kDMACode));
    private AtomicAdpodIndex<String, Handle>  adpodGeoAreacodeIndex     = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kAreaCode));
    private AtomicAdpodIndex<String, Handle>  adpodGeoZipcodeIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kZipCode));

    private AtomicAdpodIndex<String, Handle>  adpodAgeIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kAge));
    private AtomicAdpodIndex<String, Handle>  adpodAgeNoneIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kAgeNone));
    private AtomicAdpodIndex<String, Handle>  adpodGenderIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kGender));
    private AtomicAdpodIndex<String, Handle>  adpodGenderNoneIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kGenderNone));
    private AtomicAdpodIndex<String, Handle>  adpodHHIIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kHHI));
    private AtomicAdpodIndex<String, Handle>  adpodHHINoneIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kHHINone));
    private AtomicAdpodIndex<String, Handle>  adpodBTIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kBT));
    private AtomicAdpodIndex<String, Handle>  adpodBTNoneIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kBTNone));
    private AtomicAdpodIndex<String, Handle>  adpodMSIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kMS));
    private AtomicAdpodIndex<String, Handle>  adpodMSNoneIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kMSNone));

    private AtomicReference<RWLockedTreeMap<String,AtomicAdpodIndex<String, Handle>>> adPodExtVarMappingIndexMap =
            new AtomicReference<RWLockedTreeMap<String,AtomicAdpodIndex<String, Handle>>>(new RWLockedTreeMap<String, AtomicAdpodIndex<String, Handle>>());
    private AtomicReference<RWLockedTreeMap<String,AtomicAdpodIndex<String, Handle>>> adPodNonExtVarMappingIndexMap =
            new AtomicReference<RWLockedTreeMap<String,AtomicAdpodIndex<String, Handle>>>(new RWLockedTreeMap<String, AtomicAdpodIndex<String, Handle>>());

    private CampaignDBCompleteRefreshImpl() {
        initialize();
    }

    public static CampaignDBCompleteRefreshImpl getInstance() {
        return instance;
    }

    public void addAdPod(AdPod adPod) {
        adPodMap.get().safePut(adPod.getId(), adPod);
    }

    public void deleteAdPod(int adPodId) {
        adPodMap.get().safeRemove(adPodId);
        adPodCampaignMap.get().safeRemove(adPodId);
    }

    public void addNonGeoAdPod(int adPodId) {
        if(adPodGeoNoneHandlesMap.get().safeGet(adPodId) == null) {
            AdPodHandle handle = new AdPodHandle(adPodId, TargetingScoreHelper.getInstance().getGeoNoneScore(), TargetingScoreHelper.getInstance().getGeoNoneWeight());
            adpodGeoNoneIndex.put(AdpodIndex.GEO_NONE, handle);
            adPodGeoNoneHandlesMap.get().safePut(adPodId, handle);
        }
    }

    public void deleteNonGeoAdPod(int adPodId) {
        SortedSet<Handle> set = adpodGeoNoneIndex.get(AdpodIndex.GEO_NONE);
        set.remove(adPodGeoNoneHandlesMap.get().safeGet(adPodId));
        adPodGeoNoneHandlesMap.get().safeRemove(adPodId);
    }

    public void addAdpodOSpecMapping(int adPodId, int oSpecId) {
        adPodOSpecMap.get().safePut(adPodId, oSpecId);
    }

    public OSpec getOSpecForAdPod(int adPodId) {
        int oSpecId = adPodOSpecMap.get().safeGet(adPodId);
        return ospecMap.get().safeGet(oSpecId);
    }

    public OSpec getOspec(String name) {
        OSpec oSpec = ospecNameMap.get().safeGet(name);
        return oSpec;
    }

    public OSpec getOspec(int ospecId) {
        OSpec oSpec = ospecMap.get().safeGet(ospecId);
        return oSpec;
    }


    //@todo: look into possible concurrency issue in exposing the values objects to the client
    public List<OSpec> getAllOSpecs() {
        return new ArrayList<OSpec>(ospecMap.get().values());
    }

    public void addOSpec(OSpec oSpec) {
        if (oSpec == null) {
            return;
        }
        ospecNameMap.get().safePut(oSpec.getName(), oSpec);
        ospecMap.get().safePut(oSpec.getId(), oSpec);
        List<TSpec> tspecList = oSpec.getTspecs();
        for (TSpec tspec: tspecList) {
            addTSpec(tspec);
        }
    }

    public void deleteOSpec(String oSpecName) {
        OSpec oSpec = getOspec(oSpecName);
        if (oSpec != null) {
            List<TSpec> tspecList = oSpec.getTspecs();
            for(TSpec tspec: tspecList) {
                CampaignDB.getInstance().delTSpec(tspec.getId());
            }
        }

        int id = ospecNameMap.get().safeGet(oSpecName).getId();
        ospecMap.get().safeRemove(id);
        ospecNameMap.get().safeRemove(oSpecName);
    }

    public Url getUrl(String urlName) {
        return urlNameMap.get().safeGet(urlName);
    }

    public Url getUrl(int urlId) {
        return urlMap.get().safeGet(urlId);
    }

    public Location getLocation(int locationId) {
        return locationMap.get().safeGet(locationId);
    }

    public Geocode getGeocode(int geocodeId) {
        return geocodeMap.get().safeGet(geocodeId);
    }

    public void addUrl(Url url) {
        urlMap.get().put(url.getId(), url);
        urlNameMap.get().safePut(url.getName(), url);
    }

    public void deleteUrl(String urlName) {
        int id = urlNameMap.get().safeGet(urlName).getId();
        urlMap.get().safeRemove(id);
        urlNameMap.get().safeRemove(urlName);
    }

    public void addLocation(Location location) {
        locationMap.get().safePut(location.getId(), location);
    }

    public void deleteLocation(int locationId) {
        locationMap.get().safeRemove(locationId);
    }

    public void addGeocode(Geocode geocode) {
        geocodeMap.get().safePut(geocode.getId(), geocode);
    }

    public void deleteGeocode(int geocodeId) {
        geocodeMap.get().safeRemove(geocodeId);
    }

    public void addUrlMapping(UrlAdPodMapping urlAdPodMapping) {
        Url url = urlMap.get().safeGet(urlAdPodMapping.getUrlId());
        AdPod adPod = adPodMap.get().safeGet(urlAdPodMapping.getAdPodId());
        if(url != null && adPod != null) {
            String urlName = UrlNormalizer.getNormalizedUrl(url.getName());
            adpodUrlMappingIndex.put(urlName, new AdPodHandle(adPod.getId(), TargetingScoreHelper.getInstance().getUrlScore(), urlAdPodMapping.getWeight()));
        }
        else {
            //Url or Adpod not found, some inconsistency caused this. The url-adpod mapping for that particular
            // url or adpod will not be added to index.
            log.error("The Url or Adpod was not found in the urlMap/adPodMap when looking it up while creating url-adpod-mapping Indexes");
        }
    }

    public void addLocationMapping(LocationAdPodMapping mapping) {
        Location location = locationMap.get().safeGet(mapping.getLocationId());
        AdPod adPod = adPodMap.get().safeGet(mapping.getAdPodId());
        if(location != null && adPod != null) {
            int locationId = location.getId();
            adpodLocationMappingIndex.put(locationId, new AdPodHandle(adPod.getId(), TargetingScoreHelper.getInstance().getLocationScore(), 1));
        }
        else {
            //Location or Adpod not found, some inconsistency caused this. The location-adpod mapping for that particular
            // location or adpod will not be added to index.
            log.error("The Location or Adpod was not found in the locationMap/adPodMap when looking it up while creating location-adpod-mapping Indexes");
        }
    }

    public void addGeocodeMapping(Geocode geocode, int adPodId, float weight) {
        if(geocode == null || adPodId <=0) {
            log.error("Invalid geocode/adpodId passed for addind the geocode mapping");
        }

        else if(geocodeMap.get().safeGet(adPodId) != null) {
            log.error("Trying to add geocode with ID that already exists in geocodeMap inside CampaignDB");
        }
        else {
            geocodeMap.get().safePut(geocode.getId(), geocode);

            addGeocodeMapping(geocode.getCountries(), adPodId, TargetingScoreHelper.getInstance().getCountryScore(), adpodGeoCountryIndex, adPodConuntryHandlesMap);
            addGeocodeMapping(geocode.getStates(), adPodId, TargetingScoreHelper.getInstance().getRegionScore(), adpodGeoRegionIndex, adPodRegionHandlesMap);
            addGeocodeMapping(geocode.getCities(), adPodId, TargetingScoreHelper.getInstance().getCityScore(), adpodGeoCityIndex, adPodCityHandlesMap);
            addGeocodeMapping(geocode.getAreaCodes(), adPodId, TargetingScoreHelper.getInstance().getAreacodeScore(), adpodGeoAreacodeIndex, adPodAreacodeHandlesMap);
            addGeocodeMapping(geocode.getZipcodes(), adPodId, TargetingScoreHelper.getInstance().getZipcodeScore(), adpodGeoZipcodeIndex, adPodZipcodeHandlesMap);
            addGeocodeMapping(geocode.getDmaCodes(), adPodId, TargetingScoreHelper.getInstance().getDmacodeScore(), adpodGeoDmacodeIndex, adPodDmacodeHandlesMap);
        }
    }

    private void addGeocodeMapping(List<String> list, int adPodId, double score, AtomicAdpodIndex<String, Handle> index, AtomicReference<RWLockedTreeMap<Integer, Handle>> hamdlesMap) {
        if(list != null && list.size() > 0) {
            Handle handle = hamdlesMap.get().safeGet(adPodId);
            if(handle == null) {
                handle = new AdPodHandle(adPodId, score);
                hamdlesMap.get().safePut(adPodId, handle);
            }
            for (String name : list) {
                index.put(name, handle);
            }
        }
    }

    public void deleteGeocodeMapping(Geocode geocode, int adPodId) {
        if(geocode == null || adPodId <=0) {
            log.error("Invalid geocode/adpodId passed for deleting the geocode mapping");
        }

        else {
            geocodeMap.get().safeRemove(geocode.getId());

            deleteGeocodeMapping(geocode.getCountries(), adPodId, adpodGeoCountryIndex);
            deleteGeocodeMapping(geocode.getStates(), adPodId, adpodGeoRegionIndex);
            deleteGeocodeMapping(geocode.getCities(), adPodId, adpodGeoCityIndex);
            deleteGeocodeMapping(geocode.getAreaCodes(), adPodId, adpodGeoAreacodeIndex);
            deleteGeocodeMapping(geocode.getZipcodes(), adPodId, adpodGeoZipcodeIndex);
            deleteGeocodeMapping(geocode.getDmaCodes(), adPodId, adpodGeoDmacodeIndex);
        }

    }

    private void deleteGeocodeMapping(List<String> list, int adPodId, AtomicAdpodIndex<String, Handle> index) {
        if(list != null && list.size() > 0) {
            for (String name : list) {
                SortedSet<Handle> set = index.get(name);
                Iterator iterator = set.iterator();
                Handle handleToDelete = null;
                if (iterator != null && iterator.hasNext()) {
                    while (iterator.hasNext()) {
                        Handle handle = (Handle) iterator.next();
                        if (handle.getOid() == adPodId) {
                            handleToDelete = handle;
                            break;
                        }
                    }
                    if (handleToDelete != null) {
                        if (set instanceof RWLocked) {
                            ((RWLocked) set).writerLock();
                            try {
                                set.remove(handleToDelete);
                            }
                            finally {
                                ((RWLocked) set).writerUnlock();
                            }
                        } else {
                            set.remove(handleToDelete);
                        }
                    }
                }
            }
        }
    }

    public void deleteUrlMapping(String urlName, int adPodId) {
        SortedSet<Handle> set = adpodUrlMappingIndex.get(UrlNormalizer.getNormalizedUrl(urlName));
        Iterator iterator = set.iterator();
        Handle handleToDelete = null;
        if(iterator != null && iterator.hasNext()) {
            while(iterator.hasNext()) {
                Handle handle = (Handle)iterator.next();
                if(handle.getOid() == adPodId) {
                    handleToDelete = handle;
                    break;
                }
            }
            if(handleToDelete != null) {
                if(set instanceof RWLocked) {
                    ((RWLocked)set).writerLock();
                    try {
                        set.remove(handleToDelete);
                    }
                    finally {
                        ((RWLocked)set).writerUnlock();
                    }
                }
                else {
                    set.remove(handleToDelete);
                }
            }
        }

    }

    public void deleteLocationMapping(int locationId, int adPodId) {
        SortedSet<Handle> set = adpodLocationMappingIndex.get(locationId);
        Iterator iterator = set.iterator();
        Handle handleToDelete = null;
        if(iterator != null && iterator.hasNext()) {
            while(iterator.hasNext()) {
                Handle handle = (Handle)iterator.next();
                if(handle.getOid() == adPodId) {
                    handleToDelete = handle;
                    break;
                }
            }
            if(handleToDelete != null) {
                if(set instanceof RWLocked) {
                    ((RWLocked)set).writerLock();
                    try {
                        set.remove(handleToDelete);
                    }
                    finally {
                        ((RWLocked)set).writerUnlock();
                    }
                }
                else {
                    set.remove(handleToDelete);
                }
            }
        }
    }

    public void loadCampaigns(Iterator<Campaign> iterator) {
        if(iterator == null) {
            return;
        }
        int campaignCount = 0;
        RWLockedTreeMap<Integer,Campaign> map;
        if(iterator.hasNext()) {
            map = new RWLockedTreeMap<Integer, Campaign>();
            while(iterator.hasNext()) {
                Campaign campaign = iterator.next();
                map.put(campaign.getId(), campaign);
                campaignCount++;
            }
            campaignMap.compareAndSet(campaignMap.get(), map);
        }
        log.info("Campaign Size: " + campaignCount);
    }

    public void loadAdPods(Iterator<AdPod> iterator) {
        if(iterator == null) {
            return;
        }
        int adPodCount = 0;
        int camCount = 0;
        RWLockedTreeMap<Integer,AdPod> map;
        RWLockedTreeMap<Integer,CAM> camMap;

        if(iterator.hasNext()) {
            map = new RWLockedTreeMap<Integer, AdPod>();
            camMap = new RWLockedTreeMap<Integer, CAM>();
            while(iterator.hasNext()) {
                AdPod adPod = iterator.next();
                map.put(adPod.getId(), adPod);
                CAM theCAM = loadVectorDataForAdpod(adPod);
                if (theCAM!=null) {
                    camMap.put(adPod.getId(), theCAM);
                    camCount++;
                }
                adPodCount++;
            }
            adPodMap.compareAndSet(adPodMap.get(), map);
            adPodCamMap.compareAndSet(adPodCamMap.get(), camMap);
        }
        log.info("AdPod Size: " + adPodCount);
        log.info("Default CAM Size: " + camCount);

    }

    /**
     * Load the vector data into the Weight Matrix
     * @param theAdPod
     */
    private CAM loadVectorDataForAdpod(AdPod theAdPod){
        List<Recipe> recipes = theAdPod.getRecipes();
        CAM theCAM = null;
        SortedBag<Pair<CreativeSet, Double>> defRules = new SortedListBag<Pair<CreativeSet, Double>>();

        if (recipes!=null) {
            List<Recipe> activeRecipes = new ArrayList<Recipe>();
            for (Recipe r : recipes) {
                if (r.getWeight()>0.0) {
                    activeRecipes.add(r);
                }
            }
            if (activeRecipes.size() > 64) {
                log.warn("More than 64 active recipes in an adpod!!" + theAdPod.getId() + ". Recipe count = " + recipes.size());
                return null;
            }

            CAMDimensionType[] types = {CAMDimensionType.RECIPEID};
            String[] names = {CAMDimensionType.RECIPEID.name()};
            theCAM = new CAM(types, names);
            CAMDimension recipeDim = theCAM.getCAMDimension(CAMDimensionType.RECIPEID);
            //Create the Dimensions
            for (int i=0;i<activeRecipes.size();i++) {
                recipeDim.setValue(i , Integer.toString(activeRecipes.get(i).getId()));

            }

            //Create the rules
            for (int i=0;i<activeRecipes.size();i++) {
                CreativeSet oRule = new CreativeSet(theCAM);
                Recipe r =  activeRecipes.get(i);
                String rid = Integer.toString(r.getId());
                oRule.add(CAMDimensionType.RECIPEID, rid);
                Pair<CreativeSet, Double> rulePair = new Pair<CreativeSet, Double>();
                rulePair.setFirst(oRule);
                rulePair.setSecond(r.getWeight());
                defRules.add(rulePair);
            }

        }
        if (!defRules.isEmpty()) {
            int adPodId = theAdPod.getId();
            int vectorId = 1; // DEFAULT
            Map<VectorAttribute,  List<Integer>> idMap = new HashMap<VectorAttribute, List<Integer>>();
            List<Integer> list = new ArrayList<Integer>();
            list.add(adPodId);
            idMap.put(VectorAttribute.kDefault, list);
            VectorHandle h = VectorHandleFactory.getInstance().getHandle(adPodId, vectorId, VectorHandle.DEFAULT, idMap, true);
            if (h != null) {
                WMDBLoader.updateDb(adPodId, defRules, null, idMap, h);
            }
        }
        return theCAM;
    }

    public void loadExperiences(Iterator<Experience> iterator) {
        if(iterator == null) {
            return;
        }
        int expCount = 0;
        RWLockedTreeMap<Integer,Experience> map;
        if(iterator.hasNext()) {
            map = new RWLockedTreeMap<Integer, Experience>();
            while(iterator.hasNext()) {
                Experience experience = iterator.next();
                map.put(experience.getId(), experience);
                expCount++;
            }
            expMap.compareAndSet(expMap.get(), map);
        }
        log.info("Experience Size: " + expCount);

    }

    public void loadRunOfNetworkAdPods(Iterator<AdPod> iterator) {
        if(iterator == null) {
            return;
        }
        Map<String,List<Handle>> runOfNetworkAdPodMap;
        if(iterator.hasNext()) {
            runOfNetworkAdPodMap = new HashMap<String, List<Handle>>();
            AdpodIndex<String, Handle> index = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kRunofNetwork);
            List<Handle> list = new ArrayList<Handle>();
            while(iterator.hasNext()) {
                AdPod adPod = iterator.next();
                list.add(new AdPodHandle(adPod.getId(), TargetingScoreHelper.getInstance().getRunOfNetworkScore(), TargetingScoreHelper.getInstance().getRunOfNetworkWeight()));

            }
            runOfNetworkAdPodMap.put(AdpodIndex.RUN_OF_NETWORK, list);
            index.put(runOfNetworkAdPodMap);
            adpodRunOfNetworkIndex.set(index);
        }
    }

    public void loadGeoNoneAdPods(Iterator<AdPod> iterator) {
        if(iterator == null) {
            return;
        }
        int nonGeoAdpodCount = 0;
        Map<String,List<Handle>> geoNoneAdPodMap;
        if(iterator.hasNext()) {
            geoNoneAdPodMap = new HashMap<String, List<Handle>>();
            AdpodIndex<String, Handle> index = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kGeoNone);
            List<Handle> list = new ArrayList<Handle>();
            while(iterator.hasNext()) {
                AdPod adPod = iterator.next();
                list.add(new AdPodHandle(adPod.getId(), TargetingScoreHelper.getInstance().getGeoNoneScore(), TargetingScoreHelper.getInstance().getGeoNoneWeight()));
                nonGeoAdpodCount++;
            }
            log.info("Non-Geo Adpod Size: " + nonGeoAdpodCount);

            geoNoneAdPodMap.put(AdpodIndex.GEO_NONE, list);
            index.put(geoNoneAdPodMap);
            adpodGeoNoneIndex.set(index);

            //Reset the map that holds on to adpod handles for dynamic request - incorpmappingdelta
            adPodGeoNoneHandlesMap.compareAndSet(adPodGeoNoneHandlesMap.get(), new RWLockedTreeMap<Integer, Handle>());
        }
    }

    public void loadOSpecs(Iterator<OSpec> iterator) {
        if(iterator == null) {
            return;
        }
        int oSpecCount = 0;
        RWLockedTreeMap<Integer,OSpec> map     = null;
        RWLockedTreeMap<Integer,TSpec> tmap     = null;
        RWLockedTreeMap<String,OSpec>  nameMap = null;

        if(iterator.hasNext()) {
            map = new RWLockedTreeMap<Integer,OSpec>();
            tmap = new RWLockedTreeMap<Integer,TSpec>();
            nameMap = new RWLockedTreeMap<String,OSpec>();

            while(iterator.hasNext()) {
                OSpec oSpec = iterator.next();
                map.put(oSpec.getId(), oSpec);
                List<TSpec> tspecs = oSpec.getTspecs();
                if (tspecs!=null) {
                    for (TSpec t: tspecs) {
                        tmap.put(t.getId(), t);
                    }
                }

                nameMap.put(oSpec.getName(), oSpec);
                oSpecCount++;
            }
            ospecNameMap.compareAndSet(ospecNameMap.get(), nameMap);
            ospecMap.compareAndSet(ospecMap.get(), map);
            tspecMap.compareAndSet(tspecMap.get(), tmap);
        }
        log.info("OSpec Size: " + oSpecCount);
    }

    public void loadAdPodOSpecMapping(Iterator<Pair<Integer, Integer>> iterator) {
        if(iterator == null) {
            return;
        }
        if(iterator.hasNext()) {
            RWLockedTreeMap<Integer,Integer> map = new RWLockedTreeMap<Integer,Integer>();
            while(iterator.hasNext()) {
                Pair<Integer, Integer> pair = iterator.next();
                int adPodId = pair.getFirst();
                int oSpecId = pair.getSecond();
                map.put(adPodId, oSpecId);
            }
            adPodOSpecMap.compareAndSet(adPodOSpecMap.get(), map);
        }
    }

    public void loadGeocodes(Iterator<Geocode> iterator) {
        if(iterator == null) {
            return;
        }

        if(iterator.hasNext()) {
            Map<String, List<Handle>> countriesMap = new HashMap<String, List<Handle>>();
            Map<String, List<Handle>> regionsMap   = new HashMap<String, List<Handle>>();
            Map<String, List<Handle>> citiesMap    = new HashMap<String, List<Handle>>();
            Map<String, List<Handle>> zipcodesMap  = new HashMap<String, List<Handle>>();
            Map<String, List<Handle>> dmacodesMap  = new HashMap<String, List<Handle>>();
            Map<String, List<Handle>> areacodesMap = new HashMap<String, List<Handle>>();

            AdpodIndex<String, Handle> countryIndex  = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kCountryCode);
            AdpodIndex<String, Handle> regionIndex   = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kRegionCode);
            AdpodIndex<String, Handle> cityIndex     = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kCityCode);
            AdpodIndex<String, Handle> zipcodeIndex  = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kZipCode);
            AdpodIndex<String, Handle> dmacodeIndex  = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kDMACode);
            AdpodIndex<String, Handle> areacodeIndex = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kAreaCode);

            RWLockedTreeMap<Integer,Handle> countryHandlesMap  = new RWLockedTreeMap<Integer,Handle>();
            RWLockedTreeMap<Integer,Handle> regionHandlesMap   = new RWLockedTreeMap<Integer,Handle>();
            RWLockedTreeMap<Integer,Handle> cityHandlesMap     = new RWLockedTreeMap<Integer,Handle>();
            RWLockedTreeMap<Integer,Handle> zipcodeHandlesMap  = new RWLockedTreeMap<Integer,Handle>();
            RWLockedTreeMap<Integer,Handle> dmacodeHandlesMap  = new RWLockedTreeMap<Integer,Handle>();
            RWLockedTreeMap<Integer,Handle> areacodeHandlesMap = new RWLockedTreeMap<Integer,Handle>();

            while(iterator.hasNext()) {
                Geocode geocode = iterator.next();
                geocodeMap.get().safePut(geocode.getId(), geocode);
                List<String> countries = geocode.getCountries();
                if(countries != null) {
                    Handle handle = addAdPodHandle(countryHandlesMap, geocode.getAdPodId(), TargetingScoreHelper.getInstance().getCountryScore());
                    addToMap(countries, countriesMap, handle);
                }

                List<String> regions   = geocode.getStates();
                if(regions != null) {
                    Handle handle = addAdPodHandle(regionHandlesMap, geocode.getAdPodId(), TargetingScoreHelper.getInstance().getRegionScore());
                    addToMap(regions, regionsMap, handle);
                }

                List<String> cities    = geocode.getCities();
                if(cities != null) {
                    Handle handle = addAdPodHandle(cityHandlesMap, geocode.getAdPodId(), TargetingScoreHelper.getInstance().getCityScore());
                    addToMap(cities, citiesMap, handle);
                }

                List<String> zipcodes  = geocode.getZipcodes();
                if(zipcodes != null) {
                    Handle handle = addAdPodHandle(zipcodeHandlesMap, geocode.getAdPodId(), TargetingScoreHelper.getInstance().getZipcodeScore());
                    addToMap(zipcodes, zipcodesMap, handle);
                }

                List<String> dmacodes  = geocode.getDmaCodes();
                if(dmacodes != null) {
                    Handle handle = addAdPodHandle(dmacodeHandlesMap, geocode.getAdPodId(), TargetingScoreHelper.getInstance().getDmacodeScore());
                    addToMap(dmacodes, dmacodesMap, handle);
                }

                List<String> areacodes = geocode.getAreaCodes();
                if(areacodes != null) {
                    Handle handle = addAdPodHandle(areacodeHandlesMap, geocode.getAdPodId(), TargetingScoreHelper.getInstance().getAreacodeScore());
                    addToMap(areacodes, areacodesMap, handle);
                }
            }

            if(countryHandlesMap.size() > 0) {
                adPodConuntryHandlesMap.compareAndSet(adPodConuntryHandlesMap.get(), countryHandlesMap);
                countryIndex.put(countriesMap);
                adpodGeoCountryIndex.set(countryIndex);
            }

            if(regionHandlesMap.size() > 0) {
                adPodRegionHandlesMap.compareAndSet(adPodRegionHandlesMap.get(), regionHandlesMap);
                regionIndex.put(regionsMap);
                adpodGeoRegionIndex.set(regionIndex);
            }

            if(cityHandlesMap.size() > 0) {
                adPodCityHandlesMap.compareAndSet(adPodCityHandlesMap.get(), cityHandlesMap);
                cityIndex.put(citiesMap);
                adpodGeoCityIndex.set(cityIndex);
            }
            if(zipcodeHandlesMap.size() > 0) {
                adPodZipcodeHandlesMap.compareAndSet(adPodZipcodeHandlesMap.get(), zipcodeHandlesMap);
                zipcodeIndex.put(zipcodesMap);
                adpodGeoZipcodeIndex.set(zipcodeIndex);
            }
            if(dmacodeHandlesMap.size() > 0) {
                adPodDmacodeHandlesMap.compareAndSet(adPodDmacodeHandlesMap.get(), dmacodeHandlesMap);
                dmacodeIndex.put(dmacodesMap);
                adpodGeoDmacodeIndex.set(dmacodeIndex);
            }
            if(areacodeHandlesMap.size() > 0) {
                adPodAreacodeHandlesMap.compareAndSet(adPodAreacodeHandlesMap.get(), areacodeHandlesMap);
                areacodeIndex.put(areacodesMap);
                adpodGeoAreacodeIndex.set(areacodeIndex);
            }
        }
    }

    private void addToMap(List<String> list, Map<String, List<Handle>> map, Handle handle) {
        if(list != null && list.size() > 0) {
            for (String str : list) {
                str = str.toLowerCase();
                List<Handle> handleList = map.get(str);
                if (handleList == null) {
                    handleList = new ArrayList<Handle>();
                }
                handleList.add(handle);
                map.put(str, handleList);
            }
        }
    }

    private Handle addAdPodHandle(RWLockedTreeMap<Integer,Handle> handlesMap, int adPodId, double score) {
        Handle handle = handlesMap.get(adPodId);
        if(handle == null) {
            if(adPodMap.get() != null) {
                AdPod adPod = adPodMap.get().safeGet(adPodId);
                if(adPod != null) {
                    handle = new AdPodHandle(adPod.getId(), score);
                    handlesMap.safePut(adPodId, handle);
                }
                else {
                    //Adpod not found, some inconsistency caused this. The geocode mapping for that particular adpod will
                    //not be added to index.
                    log.error("The Adpod was not found in the adPodMap while looking it up while creating Geocode Indexes");
                }
            }
            else {
                //AdPod Map is null, some error condition caused this. The geocode mappings will not be added to index.
                log.error("AdPod Map internal map object is null. Some error conditions could have caused this. See previous log messages for details");
            }
        }
        return handle;
    }

    public void loadUrls(Iterator<Url> iterator) {
        if(iterator == null) {
            return;
        }
        if(iterator.hasNext()) {
            RWLockedTreeMap<Integer,Url> map    = new RWLockedTreeMap<Integer,Url>();
            RWLockedTreeMap<String,Url> nameMap = new RWLockedTreeMap<String,Url>();
            while(iterator.hasNext()) {
                Url url = iterator.next();
                map.put(url.getId(), url);
                nameMap.put(url.getName(), url);
            }
            urlMap.compareAndSet(urlMap.get(), map);
            urlNameMap.compareAndSet(urlNameMap.get(), nameMap);
        }

    }


    public void loadLocations(Iterator<Location> iterator) {
        if(iterator == null) {
            return;
        }
        if(iterator.hasNext()) {
            RWLockedTreeMap<Integer,Location> map = new RWLockedTreeMap<Integer,Location>();

            while(iterator.hasNext()) {
                Location location = iterator.next();
                map.put(location.getId(), location);
            }
            locationMap.compareAndSet(locationMap.get(), map);
        }
    }

    public void loadUrlAdPodMappings(Iterator<UrlAdPodMapping> iterator) {
        if(iterator == null) {
            return;
        }
        if(iterator.hasNext()) {
            Map<String,List<Handle>>   urlAdPodMap = new HashMap<String, List<Handle>>();
            AdpodIndex<String, Handle> index       = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kUrl);
            Url url;
            AdPod adPod;
            List<Handle> list;
            boolean defaultOSpecFound = false;
            while(iterator.hasNext()) {
                UrlAdPodMapping urlAdPodMapping = iterator.next();
                url = urlMap.get().safeGet(urlAdPodMapping.getUrlId());
                adPod = adPodMap.get().safeGet(urlAdPodMapping.getAdPodId());
                if(url != null && adPod != null) {
                    //This is a special case where we check for the mapping for default realm url. If the mapping is found we
                    //set the default ospec as per adpod id in the mapping.
                    //The assumption here is that the default-realm url will always be present with a valid mapping to a tspec


                    String urlName = UrlNormalizer.getNormalizedUrl(url.getName());
                    list = urlAdPodMap.get(urlName);
                    if(list == null) {
                        list = new ArrayList<Handle>();
                    }
                    int oid = adPod.getId();
                    list.add(new AdPodHandle(oid, TargetingScoreHelper.getInstance().getUrlScore(), urlAdPodMapping.getWeight()));
                    if(urlName != null) {
                        urlAdPodMap.put(urlName, list);
                    }
                    else {
                        log.error("url normalizing process did not succeed. Further Diagnosis required");
                        urlAdPodMap.put(url.getName(), list);
                    }
                }
                else {
                    //Url or Adpod not found, some inconsistency caused this. The url-adpod mapping for that particular
                    // url or adpod will not be added to index.
                    log.error("The Url or Adpod was not found in the urlMap/adPodMap when looking it up while creating url-adpod-mapping Indexes");
                }
            }
            index.put(urlAdPodMap);
            adpodUrlMappingIndex.set(index);
        }
    }

    public void loadLocationAdPodMappings(Iterator<LocationAdPodMapping> iterator) {
        if(iterator == null) {
            return;
        }
        if(iterator.hasNext()) {
            Map<Integer, List<Handle>>  locationAdPodMap = new HashMap<Integer, List<Handle>>();
            AdpodIndex<Integer, Handle> index            = new AdpodIndex<Integer, Handle>(AdpodIndex.Attribute.kLocation);
            Location location;
            AdPod adPod;
            List<Handle> list;
            while(iterator.hasNext()) {
                LocationAdPodMapping locationAdPodMapping = iterator.next();
                adPod = adPodMap.get().safeGet(locationAdPodMapping.getAdPodId());
                location = locationMap.get().safeGet(locationAdPodMapping.getLocationId());
                if(location != null && adPod != null) {
                    list = locationAdPodMap.get(locationAdPodMapping.getLocationId());
                    if(list == null) {
                        list = new ArrayList<Handle>();
                    }
                    int oid = adPod.getId(); //locationAdPodMapping.getId();
                    list.add(new AdPodHandle(oid, TargetingScoreHelper.getInstance().getLocationScore(), 1));
                    locationAdPodMap.put(location.getExternalId(), list);
                }
                else {
                    //Location or Adpod not found, some inconsistency caused this. The url-adpod mapping for that particular
                    // theme or adpod will not be added to index.
                    log.error("The Location or Adpod was not found in the locationMap/adPodMap when looking it up while creating location-adpod-mapping Indexes");
                }
            }
            index.put(locationAdPodMap);
            adpodLocationMappingIndex.set(index);
        }
    }

    public AtomicAdpodIndex<Integer, Handle> getLocationAdPodMappingIndex() {
        return adpodLocationMappingIndex;
    }

    public AtomicAdpodIndex<String, Handle> getUrlAdPodMappingIndex() {
        return adpodUrlMappingIndex;
    }

    public AtomicAdpodIndex<String, Handle> getNonGeoAdPodIndex() {
        return adpodGeoNoneIndex;
    }

    public AtomicAdpodIndex<String, Handle> getAdpodGeoCountryIndex() {
        return adpodGeoCountryIndex;
    }

    public AtomicAdpodIndex<String, Handle> getAdpodGeoRegionIndex() {
        return adpodGeoRegionIndex;
    }
    public AtomicAdpodIndex<String, Handle> getAdpodGeoCityIndex() {
        return adpodGeoCityIndex;
    }
    public AtomicAdpodIndex<String, Handle> getAdpodGeoDmacodeIndex() {
        return adpodGeoDmacodeIndex;
    }
    public AtomicAdpodIndex<String, Handle> getAdpodGeoAreacodeIndex() {
        return adpodGeoAreacodeIndex;
    }

    public AtomicAdpodIndex<String, Handle> getAdpodGeoZipcodeIndex() {
        return adpodGeoZipcodeIndex;
    }

    public AdPod getAdPod(AdPodHandle handle) {
        return adPodMap.get().get((int)handle.getOid());
    }

    public AdPod getAdPod(int adPodId) {
        return adPodMap.get().get(adPodId);
    }

    public Experience getExperience(int expId) {
        return expMap.get().get(expId);
    }

    public Campaign getCampaign(AdPodHandle handle) {
        if (handle==null || handle.getOid()<=0L){
            return null;
        }
        int campaignId = adPodCampaignMap.get().get((int)handle.getOid());
        return campaignMap.get().get(campaignId);
    }

    public void addCampaign(Campaign campaign) {
        campaignMap.get().safePut(campaign.getId(), campaign);
    }

    public void delCampaign(int campaignId) {
        campaignMap.get().safeRemove(campaignId);
    }

    public void addExperience(Experience exp) {
        expMap.get().safePut(exp.getId(), exp);
    }

    public void delExperience(int expId) {
        expMap.get().safeRemove(expId);
    }

    public void addRecipe(Recipe recipe) {
        recipeMap.get().safePut(new Long(recipe.getId()), recipe);
    }

    public void delRecipe(long recipeId) {
        recipeMap.get().safeRemove(recipeId);
    }

    public Recipe getRecipe(long recipeId) {
        return recipeMap.get().safeGet(recipeId);
    }

    public void addTSpec(TSpec tspec) {
        tspecMap.get().safePut(tspec.getId(), tspec);
    }

    public void delTSpec(int tspecId) {
        tspecMap.get().safeRemove(tspecId);
    }

    public TSpec getTspec(int tspecId) {
        return tspecMap.get().get(tspecId);
    }

    public void addAdpodCampaignMapping(int adPodId, int campaignId) {
        adPodCampaignMap.get().safePut(adPodId, campaignId);
    }

    public void addNonUrlAdPod(int adPodId) {
        if(adPodUrlNoneHandlesMap.get().safeGet(adPodId) == null) {
            AdPodHandle handle = new AdPodHandle(adPodId, TargetingScoreHelper.getInstance().getUrlNoneScore(), TargetingScoreHelper.getInstance().getUrlNoneWeight());
            adpodUrlNoneIndex.put(AdpodIndex.URL_NONE, handle);
            adPodUrlNoneHandlesMap.get().safePut(adPodId, handle);
        }
    }

    public void deleteNonUrlAdPod(int adPodId) {
        SortedSet<Handle> set = adpodUrlNoneIndex.get(AdpodIndex.URL_NONE);
        if (set!=null) {
            Handle h = adPodUrlNoneHandlesMap.get().safeGet(adPodId);
            if (h!=null) {
                set.remove(h);
            }
        }
        adPodUrlNoneHandlesMap.get().safeRemove(adPodId);
    }

    public void loadRecipes(Iterator<Recipe> iterator) {
        if(iterator == null) {
            return;
        }
        if(iterator.hasNext()) {
            RWLockedTreeMap<Long,Recipe> map = new RWLockedTreeMap<Long,Recipe>();

            while(iterator.hasNext()) {
                Recipe recipe = iterator.next();
                map.put(new Long(recipe.getId()), recipe);
            }
            recipeMap.compareAndSet(recipeMap.get(), map);
        }
    }

    public void loadUrlNoneAdPods(Iterator<AdPod> iterator) {
        if(iterator == null) {
            return;
        }
        int nonUrlAdpodCount = 0;
        Map<String,List<Handle>> urlNoneAdPodMap;
        if(iterator.hasNext()) {
            urlNoneAdPodMap = new HashMap<String, List<Handle>>();
            AdpodIndex<String, Handle> index = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kUrlNone);
            List<Handle> list = new ArrayList<Handle>();
            while(iterator.hasNext()) {
                AdPod adPod = iterator.next();
                list.add(new AdPodHandle(adPod.getId(), TargetingScoreHelper.getInstance().getUrlNoneScore(), TargetingScoreHelper.getInstance().getUrlNoneWeight()));
                nonUrlAdpodCount++;
            }
            log.info("Non-Url Adpod Size: " + nonUrlAdpodCount);

            urlNoneAdPodMap.put(AdpodIndex.URL_NONE, list);
            index.put(urlNoneAdPodMap);
            adpodUrlNoneIndex.set(index);

            //Reset the map that holds on to adpod handles for dynamic request - incorpmappingdelta
            adPodUrlNoneHandlesMap.compareAndSet(adPodUrlNoneHandlesMap.get(), new RWLockedTreeMap<Integer, Handle>());
        }
    }

    public void loadAdPodCampaignMapping(Iterator<Pair<Integer, Integer>> iterator) {
        if(iterator == null) {
            return;
        }
        if(iterator.hasNext()) {
            RWLockedTreeMap<Integer,Integer> map = new RWLockedTreeMap<Integer,Integer>();
            while(iterator.hasNext()) {
                Pair<Integer, Integer> pair = iterator.next();
                int adPodId = pair.getFirst();
                int campaignId = pair.getSecond();
                map.put(adPodId, campaignId);
            }
            adPodCampaignMap.compareAndSet(adPodCampaignMap.get(), map);
        }
    }

    public AtomicAdpodIndex<String, Handle> getNonUrlAdPodIndex() {
        return adpodUrlNoneIndex;
    }

    public Campaign getCampaign(int campaignId) {
        return campaignMap.get().safeGet(campaignId);
    }


    public boolean isEmpty(){
        return (campaignMap.get().isEmpty());
    }

    public ArrayList<Campaign> getCampaigns() {
        ArrayList<Campaign> campAL = new ArrayList<Campaign>();
        RWLockedTreeMap<Integer, Campaign> campTreeMap = campaignMap.get();
        Iterator<Integer> campIdSetIter = campTreeMap.keySet().iterator();
        while(campIdSetIter.hasNext()) {
            Integer campId = campIdSetIter.next();
            campAL.add(campTreeMap.get(campId));
        }
        return campAL;
    }

    public ArrayList<AdPod> getAdPods() {
        ArrayList<AdPod> adPodAL = new ArrayList<AdPod>();
        RWLockedTreeMap<Integer, AdPod> adPodTreeMap = adPodMap.get();
        Iterator<Integer> adPodKeySetIter = adPodTreeMap.keySet().iterator();
        while(adPodKeySetIter.hasNext()) {
            Integer adPodId = adPodKeySetIter.next();
            adPodAL.add(adPodTreeMap.get(adPodId));
        }
        return adPodAL;
    }

    public ArrayList<OSpec> getOSpecs() {
        ArrayList<OSpec> oSpecArrayList = new ArrayList<OSpec>();
        RWLockedTreeMap<Integer, OSpec> oSpecTreeMap = ospecMap.get();
        Iterator<Integer> oSpecKeysIter = oSpecTreeMap.keySet().iterator();
        while(oSpecKeysIter.hasNext()) {
            Integer oSpecId = oSpecKeysIter.next();
            oSpecArrayList.add(oSpecTreeMap.get(oSpecId));
        }
        return oSpecArrayList;
    }

    public ArrayList<TSpec> getTSpecs() {
        ArrayList<TSpec> tSpecArrayList = new ArrayList<TSpec>();
        RWLockedTreeMap<Integer, TSpec> oSpecTreeMap = tspecMap.get();
        Iterator<Integer> tSpecKeysIter = oSpecTreeMap.keySet().iterator();
        while(tSpecKeysIter.hasNext()) {
            Integer oSpecId = tSpecKeysIter.next();
            tSpecArrayList.add(oSpecTreeMap.get(oSpecId));
        }
        return tSpecArrayList;
    }

    public ArrayList<Recipe> getRecipes() {
        ArrayList<Recipe> recipeArrayList = new ArrayList<Recipe>();
        RWLockedTreeMap<Long, Recipe> recipeTreeMap = recipeMap.get();
        Iterator<Long> recipeKeyIter = recipeTreeMap.keySet().iterator();
        while(recipeKeyIter.hasNext()) {
            Long recipeId = recipeKeyIter.next();
            recipeArrayList.add(recipeTreeMap.get(recipeId));
        }
        return recipeArrayList;
    }

    @Override
    public void loadExternalVariableAdPods(HashMap<String, ArrayList<AdPodExternalVariableMapping>> inputMappings) {
        if(inputMappings == null) {
            return;
        }

        Set<String> extVariableNames = inputMappings.keySet();
        for (String extVarName : extVariableNames) {
            List<AdPodExternalVariableMapping> mappings = inputMappings.get(extVarName);
            Map<String,List<Handle>> extVariableAdPodMap = new HashMap<String, List<Handle>>();
            AdpodIndex<String, Handle> index = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kExtTarget);
            for (AdPodExternalVariableMapping m : mappings) {
                AdPod adPod;
                List<Handle> list;
                String externalTarget = m.getValue();
                adPod = adPodMap.get().safeGet(m.getAdPodId());
                if(externalTarget != null && adPod != null) {
                    list = extVariableAdPodMap.get(externalTarget);
                    if(list == null) {
                        list = new ArrayList<Handle>();
                    }
                    int oid = adPod.getId();
                    list.add(new AdPodHandle(oid, TargetingScoreHelper.getInstance().getTargetingVariableScore()));
                    extVariableAdPodMap.put(externalTarget, list);
                }
                else {
                    //Adpod not found, some inconsistency caused this. The externaltarget-adpod mapping for that particular
                    // adpod will not be added to index.
                    log.error("The Adpod was not found in the externaltarget/adPodMap when looking it up while creating externaltarget-adpod-mapping Indexes");
                }
            }
            index.put(extVariableAdPodMap);
            RWLockedTreeMap<String, AtomicAdpodIndex<String, Handle>> idxMap = adPodExtVarMappingIndexMap.get();

            AtomicAdpodIndex<String, Handle> idx = idxMap.get(extVarName);
            if (idx == null) {
                idx = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kExtTarget));
            }
            idx.set(index);
            idxMap.put(extVarName, idx);

        }
    }

    @Override
    public void loadNonExternalVariableAdPods(HashMap<String, ArrayList<AdPod>> inputMappings) {
        if(inputMappings == null) {
            return;
        }

        Set<String> extVariableNames = inputMappings.keySet();
        for (String extVarName : extVariableNames) {
            List<AdPod> mappings = inputMappings.get(extVarName);
            Map<String,List<Handle>> extVariableAdPodMap = new HashMap<String, List<Handle>>();
            AdpodIndex<String, Handle> index = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kExtTargetNone);
            List<Handle> list = new ArrayList<Handle>();

            for (AdPod m : mappings) {
                list.add(new AdPodHandle(m.getId(), TargetingScoreHelper.getInstance().getTargetingVariableNoneScore(), TargetingScoreHelper.getInstance().getTargetingVariableNoneWeight()));
            }
            extVariableAdPodMap.put(AdpodIndex.EXTERNAL_VARIABLE_NONE, list);
            index.put(extVariableAdPodMap);
            RWLockedTreeMap<String, AtomicAdpodIndex<String, Handle>> idxMap = adPodNonExtVarMappingIndexMap.get();

            AtomicAdpodIndex<String, Handle> idx = idxMap.get(extVarName);
            if (idx == null) {
                idx = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kExtTargetNone));
            }
            idx.set(index);
            idxMap.put(extVarName, idx);
        }

    }

    @Override
    public AtomicAdpodIndex<String, Handle> getExternalVariableAdPodMappingIndex(String variableName) {
        return adPodExtVarMappingIndexMap.get().get(variableName);
    }

    @Override
    public AtomicAdpodIndex<String, Handle> getNonExternalVariableAdPodMappingIndex(String variableName) {
        return adPodNonExtVarMappingIndexMap.get().get(variableName);
    }

    @Override
    public CAM getDefaultCAM(int adpodId) {
        return adPodCamMap.get().get(adpodId);
    }

    @Override
    public AtomicAdpodIndex<String, Handle> getAdpodAgeIndex() {
        return adpodAgeIndex;
    }

    @Override
    public AtomicAdpodIndex<String, Handle> getAdpodAgeNoneIndex() {
        return adpodAgeNoneIndex;
    }

    @Override
    public AtomicAdpodIndex<String, Handle> getAdpodGenderIndex() {
        return adpodGenderIndex;
    }

    @Override
    public AtomicAdpodIndex<String, Handle> getAdpodGenderNoneIndex() {
        return adpodGenderNoneIndex;
    }

    @Override
    public AtomicAdpodIndex<String, Handle> getAdpodBTIndex() {
        return adpodBTIndex;
    }

    @Override
    public AtomicAdpodIndex<String, Handle> getAdpodBTNoneIndex() {
        return adpodBTNoneIndex;
    }

    @Override
    public AtomicAdpodIndex<String, Handle> getAdpodMSIndex() {
        return adpodMSIndex;
    }

    @Override
    public AtomicAdpodIndex<String, Handle> getAdpodMSNoneIndex() {
        return adpodMSNoneIndex;
    }

    @Override
    public AtomicAdpodIndex<String, Handle> getAdpodHHIIndex() {
        return adpodHHIIndex;
    }

    @Override
    public AtomicAdpodIndex<String, Handle> getAdpodHHINoneIndex() {
        return adpodHHINoneIndex;
    }

    @Override
    public void loadAgeAdPodMappings(Iterator<AgeAdPodMapping> iterator) {
        AdpodIndex<String, Handle> index = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kAge);
        while(iterator.hasNext()) {
            AgeAdPodMapping m = iterator.next();
            AdPod adPod;
            adPod = adPodMap.get().safeGet(m.getAdPodId());
            if(adPod != null) {
                int oid = adPod.getId();
                index.put(m.getAge(), new AdPodHandle(oid, TargetingScoreHelper.getInstance().getAgescore()));
            }
            else {
                log.error("The Adpod was not found in the age adpod mapping");
            }
        }
        adpodAgeIndex.set(index);
    }

    @Override
    public void loadGenderAdPodMappings(Iterator<GenderAdPodMapping> iterator) {
        AdpodIndex<String, Handle> index = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kGender);
        while(iterator.hasNext()) {
            GenderAdPodMapping m = iterator.next();
            AdPod adPod;
            adPod = adPodMap.get().safeGet(m.getAdPodId());
            if(adPod != null) {
                int oid = adPod.getId();
                index.put(m.getGender(), new AdPodHandle(oid, TargetingScoreHelper.getInstance().getGenderscore()));
            }
            else {
                log.error("The Adpod was not found in the gender adpod mapping");
            }
        }
        adpodGenderIndex.set(index);
    }

    @Override
    public void loadBTAdPodMappings(Iterator<BTAdPodMapping> iterator) {
        AdpodIndex<String, Handle> index = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kBT);
        while(iterator.hasNext()) {
            BTAdPodMapping m = iterator.next();
            AdPod adPod;
            adPod = adPodMap.get().safeGet(m.getAdPodId());
            if(adPod != null) {
                int oid = adPod.getId();
                index.put(m.getBt(), new AdPodHandle(oid, TargetingScoreHelper.getInstance().getBtscore()));
            }
            else {
                log.error("The Adpod was not found in the BT adpod mapping");
            }
        }
        adpodBTIndex.set(index);
    }

    @Override
    public void loadMSAdPodMappings(Iterator<MSAdPodMapping> iterator) {
        AdpodIndex<String, Handle> index = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kMS);
        while(iterator.hasNext()) {
            MSAdPodMapping m = iterator.next();
            AdPod adPod;
            adPod = adPodMap.get().safeGet(m.getAdPodId());
            if(adPod != null) {
                int oid = adPod.getId();
                index.put(m.getMs(), new AdPodHandle(oid, TargetingScoreHelper.getInstance().getMsscore()));
            }
            else {
                log.error("The Adpod was not found in the MS adpod mapping");
            }
        }
        adpodMSIndex.set(index);
    }

    @Override
    public void loadHHIAdPodMappings(Iterator<HHIAdPodMapping> iterator) {
        AdpodIndex<String, Handle> index = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kHHI);
        while(iterator.hasNext()) {
            HHIAdPodMapping m = iterator.next();
            AdPod adPod;
            adPod = adPodMap.get().safeGet(m.getAdPodId());
            if(adPod != null) {
                int oid = adPod.getId();
                index.put(m.getHhi(), new AdPodHandle(oid, TargetingScoreHelper.getInstance().getHhiscore()));
            }
            else {
                log.error("The Adpod was not found in the HHI adpod mapping");
            }
        }
        adpodHHIIndex.set(index);
    }

    @Override
    public void loadAgeNoneAdPods(Iterator<AdPod> iterator) {
        AdpodIndex<String, Handle> index = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kAgeNone);
        while(iterator.hasNext()) {
            AdPod adPod = iterator.next();
            if(adPod != null) {
                int oid = adPod.getId();
                index.put(AdpodIndex.AGE_NONE, new AdPodHandle(oid, TargetingScoreHelper.getInstance().getAgenonescore()));
            }
            else {
                log.error("The Adpod was not found in the age none adpod mapping");
            }
        }
        adpodAgeNoneIndex.set(index);
    }

    @Override
    public void loadHHINoneAdPods(Iterator<AdPod> iterator) {
        AdpodIndex<String, Handle> index = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kHHINone);
        while(iterator.hasNext()) {
            AdPod adPod = iterator.next();
            if(adPod != null) {
                int oid = adPod.getId();
                index.put(AdpodIndex.HHI_NONE, new AdPodHandle(oid, TargetingScoreHelper.getInstance().getHhinonescore()));
            }
            else {
                log.error("The Adpod was not found in the hhi none adpod mapping");
            }
        }
        adpodHHINoneIndex.set(index);
    }

    @Override
    public void loadMSNoneAdPods(Iterator<AdPod> iterator) {
        AdpodIndex<String, Handle> index = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kMSNone);
        while(iterator.hasNext()) {
            AdPod adPod = iterator.next();
            if(adPod != null) {
                int oid = adPod.getId();
                index.put(AdpodIndex.MS_NONE, new AdPodHandle(oid, TargetingScoreHelper.getInstance().getMsnonescore()));
            }
            else {
                log.error("The Adpod was not found in the ms none adpod mapping");
            }
        }
        adpodMSNoneIndex.set(index);
    }

    @Override
    public void loadBTNoneAdPods(Iterator<AdPod> iterator) {
        AdpodIndex<String, Handle> index = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kBTNone);
        while(iterator.hasNext()) {
            AdPod adPod = iterator.next();
            if(adPod != null) {
                int oid = adPod.getId();
                index.put(AdpodIndex.BT_NONE, new AdPodHandle(oid, TargetingScoreHelper.getInstance().getBtnonescore()));
            }
            else {
                log.error("The Adpod was not found in the bt none adpod mapping");
            }
        }
        adpodBTNoneIndex.set(index);
    }

    @Override
    public void loadGenderNoneAdPods(Iterator<AdPod> iterator) {
        AdpodIndex<String, Handle> index = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kGenderNone);
        while(iterator.hasNext()) {
            AdPod adPod = iterator.next();
            if(adPod != null) {
                int oid = adPod.getId();
                index.put(AdpodIndex.GENDER_NONE, new AdPodHandle(oid, TargetingScoreHelper.getInstance().getGendernonescore()));
            }
            else {
                log.error("The Adpod was not found in the gender none adpod mapping");
            }
        }
        adpodGenderNoneIndex.set(index);
    }
}
