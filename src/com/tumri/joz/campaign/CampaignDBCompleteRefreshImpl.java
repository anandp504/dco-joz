package com.tumri.joz.campaign;

import com.tumri.cma.domain.*;
import com.tumri.utils.data.RWLockedTreeMap;
import com.tumri.utils.data.RWLocked;
import com.tumri.utils.Pair;
import com.tumri.joz.products.Handle;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.index.AdpodIndex;
import com.tumri.joz.targeting.TargetingScoreHelper;

import java.util.concurrent.atomic.AtomicReference;
import java.util.*;

import org.apache.log4j.Logger;

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

    private AtomicReference<RWLockedTreeMap<Integer,Campaign>> campaignMap   = new AtomicReference<RWLockedTreeMap<Integer, Campaign>>(new RWLockedTreeMap<Integer, Campaign>());
    private AtomicReference<RWLockedTreeMap<Integer, AdPod>>    adPodMap      = new AtomicReference<RWLockedTreeMap<Integer, AdPod>>(new RWLockedTreeMap<Integer, AdPod>());
    private AtomicReference<RWLockedTreeMap<Integer, OSpec>>    ospecMap      = new AtomicReference<RWLockedTreeMap<Integer, OSpec>>(new RWLockedTreeMap<Integer, OSpec>());
    private AtomicReference<RWLockedTreeMap<String,OSpec>>    ospecNameMap   = new AtomicReference<RWLockedTreeMap<String, OSpec>>(new RWLockedTreeMap<String, OSpec>());

    private AtomicReference<RWLockedTreeMap<Integer, Geocode>>  geocodeMap    = new AtomicReference<RWLockedTreeMap<Integer, Geocode>>(new RWLockedTreeMap<Integer, Geocode>());
    private AtomicReference<RWLockedTreeMap<Integer, Url>>      urlMap        = new AtomicReference<RWLockedTreeMap<Integer, Url>>(new RWLockedTreeMap<Integer, Url>());
    private AtomicReference<RWLockedTreeMap<String, Url>>       urlNameMap    = new AtomicReference<RWLockedTreeMap<String, Url>>(new RWLockedTreeMap<String, Url>());

    @SuppressWarnings({"deprecation"})
    private AtomicReference<RWLockedTreeMap<Integer,Theme>>    themeMap      = new AtomicReference<RWLockedTreeMap<Integer, Theme>>(new RWLockedTreeMap<Integer, Theme>());
    private AtomicReference<RWLockedTreeMap<String, Theme>>    themeNameMap  = new AtomicReference<RWLockedTreeMap<String, Theme>>(new RWLockedTreeMap<String, Theme>());

    private AtomicReference<RWLockedTreeMap<Integer,Location>> locationMap   = new AtomicReference<RWLockedTreeMap<Integer, Location>>(new RWLockedTreeMap<Integer, Location>());

    // Map adpod Id to ospec ID
    private AtomicReference<RWLockedTreeMap<Integer, Integer>> adPodOSpecMap = new AtomicReference<RWLockedTreeMap<Integer, Integer>>(new RWLockedTreeMap<Integer, Integer>());

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

    // All indices required in targeting
    private AtomicAdpodIndex<Integer, Handle> adpodLocationMappingIndex = new AtomicAdpodIndex<Integer, Handle>(new AdpodIndex<Integer, Handle>(AdpodIndex.Attribute.kLocation));
    private AtomicAdpodIndex<String, Handle>  adpodThemeMappingIndex    = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kTheme));
    private AtomicAdpodIndex<String, Handle>  adpodUrlMappingIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kUrl));
    private AtomicAdpodIndex<String, Handle>  adpodRunOfNetworkIndex    = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kRunofNetwork));
    private AtomicAdpodIndex<String, Handle>  adpodGeoNoneIndex         = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kGeoNone));

    private AtomicAdpodIndex<String, Handle>  adpodGeoCountryIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kCountryCode));
    private AtomicAdpodIndex<String, Handle>  adpodGeoRegionIndex       = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kRegionCode));
    private AtomicAdpodIndex<String, Handle>  adpodGeoCityIndex         = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kCityCode));
    private AtomicAdpodIndex<String, Handle>  adpodGeoDmacodeIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kDMACode));
    private AtomicAdpodIndex<String, Handle>  adpodGeoAreacodeIndex     = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kAreaCode));
    private AtomicAdpodIndex<String, Handle>  adpodGeoZipcodeIndex      = new AtomicAdpodIndex<String, Handle>(new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kZipCode));

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
        //@todo remove from the non-geo index
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

    //@todo: look into possible concurrency issue in exposing the values objects to the client
    public List<OSpec> getAllOSpecs() {
        return new ArrayList<OSpec>(ospecMap.get().values());
    }

    public void addOSpec(OSpec oSpec) {
        ospecNameMap.get().safePut(oSpec.getName(), oSpec);
        ospecMap.get().safePut(oSpec.getId(), oSpec);
    }

    public void deleteOSpec(String oSpecName) {
        ospecMap.get().safeRemove(ospecNameMap.get().safeGet(oSpecName).getId());
        ospecNameMap.get().safeRemove(oSpecName);
    }

    public Url getUrl(String urlName) {
        return urlNameMap.get().safeGet(urlName);
    }

    public Theme getTheme(String themeName) {
        return themeNameMap.get().safeGet(themeName);
    }

    public Location getLocation(int locationId) {
        return locationMap.get().safeGet(locationId);
    }

    public Geocode getGeocode(int geocodeId) {
        return geocodeMap.get().safeGet(geocodeId);
    }

    public void addUrl(Url url) {
        urlMap.get().put(url.getId(), url);
        urlNameMap.get().put(url.getName(), url);
    }

    public void deleteUrl(String urlName) {
        urlMap.get().remove(urlNameMap.get().get(urlName).getId());
        urlNameMap.get().remove(urlName);
    }

    public void addTheme(Theme theme) {
        themeMap.get().put(theme.getId(), theme);
        themeNameMap.get().put(theme.getName(), theme);
    }

    public void deleteTheme(String themeName) {
        themeMap.get().remove(themeNameMap.get().get(themeName).getId());
        themeNameMap.get().remove(themeName);
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
        Url url = urlMap.get().get(urlAdPodMapping.getUrlId());
        AdPod adPod = adPodMap.get().get(urlAdPodMapping.getAdPodId());
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

    public void addThemeMapping(ThemeAdPodMapping mapping) {
        Theme theme = themeMap.get().get(mapping.getThemeId());
        AdPod adPod = adPodMap.get().get(mapping.getAdPodId());
        if(theme != null && adPod != null) {
            String themeName = theme.getName();
            adpodThemeMappingIndex.put(themeName, new AdPodHandle(adPod.getId(), TargetingScoreHelper.getInstance().getThemeScore(), mapping.getWeight()));
        }
        else {
            //Theme or Adpod not found, some inconsistency caused this. The theme-adpod mapping for that particular
            // theme or adpod will not be added to index.
            log.error("The Theme or Adpod was not found in the themeMap/adPodMap when looking it up while creating theme-adpod-mapping Indexes");
        }
    }

    public void addLocationMapping(LocationAdPodMapping mapping) {
        Location location = locationMap.get().get(mapping.getLocationId());
        AdPod adPod = adPodMap.get().get(mapping.getAdPodId());
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
        if(geocode == null || adPodId <=0 || (geocodeMap.get().safeGet(adPodId) == null)) {
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

    public void deleteThemeMapping(String themeName, int adPodId) {
        SortedSet<Handle> set = adpodThemeMappingIndex.get(themeName);
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

    public OSpec getDefaultOSpec() {
        OSpec oSpec = ospecNameMap.get().get(getDefaultRealmOSpecName());
        if(oSpec == null) {
            oSpec = super.getDefaultOSpec();
        }
        return oSpec;
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
        RWLockedTreeMap<Integer,AdPod> map;
        if(iterator.hasNext()) {
            map = new RWLockedTreeMap<Integer, AdPod>();
            while(iterator.hasNext()) {
                AdPod adPod = iterator.next();
                map.put(adPod.getId(), adPod);
                adPodCount++;
            }
            adPodMap.compareAndSet(adPodMap.get(), map);
        }
        log.info("AdPod Size: " + adPodCount);

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
        RWLockedTreeMap<Integer,OSpec> map = null;
        ospecNameMap.get().writerLock();
        try {
            if(iterator.hasNext()) {
                map = new RWLockedTreeMap<Integer,OSpec>();
                while(iterator.hasNext()) {
                    OSpec oSpec = iterator.next();
                    map.put(oSpec.getId(), oSpec);
                    ospecNameMap.get().put(oSpec.getName(), oSpec);
                    oSpecCount++;
                }
            }
            log.info("OSpec Size: " + oSpecCount);

        }
        finally {
            ospecNameMap.get().writerUnlock();
        }
        if(map != null) {
            ospecMap.compareAndSet(ospecMap.get(), map);
        }
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
                geocodeMap.get().put(geocode.getId(), geocode);
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
                AdPod adPod = adPodMap.get().get(adPodId);
                if(adPod != null) {
                    handle = new AdPodHandle(adPod.getId(), score);
                    handlesMap.put(adPodId, handle);
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

    @SuppressWarnings({"deprecation"})
    public void loadThemes(Iterator<Theme> iterator) {
        if(iterator == null) {
            return;
        }
        if(iterator.hasNext()) {
            RWLockedTreeMap<Integer,Theme> map = new RWLockedTreeMap<Integer,Theme>();
            RWLockedTreeMap<String,Theme> nameMap = new RWLockedTreeMap<String,Theme>();
            while(iterator.hasNext()) {
                Theme theme = iterator.next();
                map.put(theme.getId(), theme);
                nameMap.put(theme.getName(), theme);
            }
            themeMap.compareAndSet(themeMap.get(), map);
            themeNameMap.compareAndSet(themeNameMap.get(), nameMap);
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
            while(iterator.hasNext()) {
                UrlAdPodMapping urlAdPodMapping = iterator.next();
                url = urlMap.get().get(urlAdPodMapping.getUrlId());
                adPod = adPodMap.get().get(urlAdPodMapping.getAdPodId());
                if(url != null && adPod != null) {
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

    @SuppressWarnings({"deprecation"})
    public void loadThemeAdPodMappings(Iterator<ThemeAdPodMapping> iterator) {
        if(iterator == null) {
            return;
        }
        if(iterator.hasNext()) {
            Map<String, List<Handle>>  themeAdPodMap = new HashMap<String, List<Handle>>();
            AdpodIndex<String, Handle> index         = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kTheme);
            Theme theme;
            AdPod adPod;
            List<Handle> list;

            while(iterator.hasNext()) {
                ThemeAdPodMapping themeAdPodMapping = iterator.next();
                theme = themeMap.get().get(themeAdPodMapping.getThemeId());
                adPod = adPodMap.get().get(themeAdPodMapping.getAdPodId());
                if(theme != null && adPod != null) {
                    list = themeAdPodMap.get(theme.getName());
                    if(list == null) {
                        list = new ArrayList<Handle>();
                    }
                    int oid = adPod.getId(); //themeAdPodMapping.getId();
                    list.add(new AdPodHandle(oid, TargetingScoreHelper.getInstance().getThemeScore(), themeAdPodMapping.getWeight()));
                    themeAdPodMap.put(theme.getName(), list);
                }
                else {
                    //Theme or Adpod not found, some inconsistency caused this. The theme-adpod mapping for that particular
                    // theme or adpod will not be added to index.
                    log.error("The Theme or Adpod was not found in the themeMap/adPodMap when looking it up while creating theme-adpod-mapping Indexes");
                }
            }
            index.put(themeAdPodMap);
            adpodThemeMappingIndex.set(index);
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
                adPod = adPodMap.get().get(locationAdPodMapping.getAdPodId());
                location = locationMap.get().get(locationAdPodMapping.getLocationId());
                if(location != null && adPod != null) {
                    list = locationAdPodMap.get(locationAdPodMapping.getLocationId());
                    if(list == null) {
                        list = new ArrayList<Handle>();
                    }
                    //@todo: add weight field to LocationAdpodMapping class
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

    public AtomicAdpodIndex<String, Handle> getThemeAdPodMappingIndex() {
        return adpodThemeMappingIndex;
    }

    public AtomicAdpodIndex<String, Handle> getRunOfNetworkAdPodIndex() {
        return adpodRunOfNetworkIndex;
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
}
