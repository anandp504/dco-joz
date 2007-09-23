package com.tumri.joz.campaign;

import com.tumri.cma.domain.*;
import com.tumri.utils.data.RWLockedTreeMap;
import com.tumri.utils.Pair;
import com.tumri.joz.products.Handle;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.index.AdpodIndex;

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
public class CampaignDBCompleteRefreshImpl extends CampaignDB {
    private static CampaignDBCompleteRefreshImpl instance = new CampaignDBCompleteRefreshImpl();
    private static Logger log = Logger.getLogger (CampaignDBCompleteRefreshImpl.class);

    private AtomicReference<RWLockedTreeMap<Integer,Campaign>> campaignMap   = new AtomicReference<RWLockedTreeMap<Integer, Campaign>>(new RWLockedTreeMap<Integer, Campaign>());
    private AtomicReference<RWLockedTreeMap<Integer, AdPod>>    adPodMap      = new AtomicReference<RWLockedTreeMap<Integer, AdPod>>(new RWLockedTreeMap<Integer, AdPod>());
    private AtomicReference<RWLockedTreeMap<Integer, OSpec>>    ospecMap      = new AtomicReference<RWLockedTreeMap<Integer, OSpec>>(new RWLockedTreeMap<Integer, OSpec>());
    private AtomicReference<RWLockedTreeMap<String,OSpec>>    ospecNameMap   = new AtomicReference<RWLockedTreeMap<String, OSpec>>(new RWLockedTreeMap<String, OSpec>());

    //This map is populated by add-tspec calls from publisher. The map is never cleared on it own, but the delete-tspec
    //calls from portals will delete individual entries in this map.
    //This could potentially lead to memory leak.
    //@todo: Refactor the map to use some kind of LRU cache so the memory leak cannot go out of control.
    private RWLockedTreeMap<String, OSpec>                  tempOSpecNameMap = new RWLockedTreeMap<String, OSpec>();

    private AtomicReference<RWLockedTreeMap<Integer, Geocode>>  geocodeMap    = new AtomicReference<RWLockedTreeMap<Integer, Geocode>>(new RWLockedTreeMap<Integer, Geocode>());
    private AtomicReference<RWLockedTreeMap<Integer, Url>>      urlMap        = new AtomicReference<RWLockedTreeMap<Integer, Url>>(new RWLockedTreeMap<Integer, Url>());
    @SuppressWarnings({"deprecation"})
    private AtomicReference<RWLockedTreeMap<Integer,Theme>>    themeMap      = new AtomicReference<RWLockedTreeMap<Integer, Theme>>(new RWLockedTreeMap<Integer, Theme>());
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

    public OSpec getOSpecForAdPod(int adPodId) {
        int oSpecId = adPodOSpecMap.get().get(adPodId);
        return ospecMap.get().get(oSpecId);
    }

    public OSpec getOspec(String name) {
        OSpec oSpec = ospecNameMap.get().get(name);
        if(oSpec == null) {
            oSpec = tempOSpecNameMap.get(name);
        }
        return oSpec;
    }

    public void addOSpec(OSpec oSpec) {
        tempOSpecNameMap.put(oSpec.getName(), oSpec);
    }

    public void deleteOSpec(String oSpecName) {
        tempOSpecNameMap.remove(oSpecName);
    }

    public OSpec getDefaultOSpec() {
        return ospecNameMap.get().get(getDefaultRealmOSpecName());
    }

    public void loadCampaigns(Iterator<Campaign> iterator) {
        if(iterator == null) {
            return;
        }
        RWLockedTreeMap<Integer,Campaign> map;
        if(iterator.hasNext()) {
            map = new RWLockedTreeMap<Integer, Campaign>();
            while(iterator.hasNext()) {
                Campaign campaign = iterator.next();
                map.put(campaign.getId(), campaign);
            }
            campaignMap.compareAndSet(campaignMap.get(), map);
        }
    }

    public void loadAdPods(Iterator<AdPod> iterator) {
        if(iterator == null) {
            return;
        }
        RWLockedTreeMap<Integer,AdPod> map;
        if(iterator.hasNext()) {
            map = new RWLockedTreeMap<Integer, AdPod>();
            while(iterator.hasNext()) {
                AdPod adPod = iterator.next();
                map.put(adPod.getId(), adPod);
            }
            adPodMap.compareAndSet(adPodMap.get(), map);
        }
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
                list.add(new AdPodHandle(adPod, adPod.getId(), AdPodHandle.runOfNetworkScore, AdPodHandle.runOfNetworkWeight));
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
        Map<String,List<Handle>> geoNoneAdPodMap;
        if(iterator.hasNext()) {
            geoNoneAdPodMap = new HashMap<String, List<Handle>>();
            AdpodIndex<String, Handle> index = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kGeoNone);
            List<Handle> list = new ArrayList<Handle>();
            while(iterator.hasNext()) {
                AdPod adPod = iterator.next();
                list.add(new AdPodHandle(adPod, adPod.getId(), AdPodHandle.geoNoneScore, AdPodHandle.geoNoneWeight));
            }
            geoNoneAdPodMap.put(AdpodIndex.GEO_NONE, list);
            index.put(geoNoneAdPodMap);
            adpodGeoNoneIndex.set(index);
        }
    }

    public void loadOSpecs(Iterator<OSpec> iterator) {
        if(iterator == null) {
            return;
        }
        while(iterator.hasNext()) {
            OSpec oSpec = iterator.next();
            ospecMap.get().put(oSpec.getId(), oSpec);
            ospecNameMap.get().put(oSpec.getName(), oSpec);
        }
    }

    public void loadAdPodOSpecMapping(Iterator<Pair<Integer, Integer>> iterator) {
        if(iterator == null) {
            return;
        }
        while(iterator.hasNext()) {
            Pair<Integer, Integer> pair = iterator.next();
            int adPodId = pair.getFirst();
            int oSpecId = pair.getSecond();
            adPodOSpecMap.get().put(adPodId, oSpecId);
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
                    Handle handle = addAdPodHandle(countryHandlesMap, geocode.getAdPodId(), AdPodHandle.countryScore);
                    addToMap(countries, countriesMap, handle);
                }

                List<String> regions   = geocode.getStates();
                if(regions != null) {
                    Handle handle = addAdPodHandle(regionHandlesMap, geocode.getAdPodId(), AdPodHandle.regionScore);
                    addToMap(regions, regionsMap, handle);
                }

                List<String> cities    = geocode.getCities();
                if(cities != null) {
                    Handle handle = addAdPodHandle(cityHandlesMap, geocode.getAdPodId(), AdPodHandle.cityScore);
                    addToMap(cities, citiesMap, handle);
                }

                List<String> zipcodes  = geocode.getZipcodes();
                if(zipcodes != null) {
                    Handle handle = addAdPodHandle(zipcodeHandlesMap, geocode.getAdPodId(), AdPodHandle.zipcodeScore);
                    addToMap(zipcodes, zipcodesMap, handle);
                }

                List<String> dmacodes  = geocode.getDmaCodes();
                if(dmacodes != null) {
                    Handle handle = addAdPodHandle(dmacodeHandlesMap, geocode.getAdPodId(), AdPodHandle.dmacodeScore);
                    addToMap(dmacodes, dmacodesMap, handle);
                }

                List<String> areacodes = geocode.getAreaCodes();
                if(areacodes != null) {
                    Handle handle = addAdPodHandle(areacodeHandlesMap, geocode.getAdPodId(), AdPodHandle.areacodeScore);
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
                    handle = new AdPodHandle(adPod, adPod.getId(), score);
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
        while(iterator.hasNext()) {
            Url url = iterator.next();
            urlMap.get().put(url.getId(), url);
        }
    }

    @SuppressWarnings({"deprecation"})
    public void loadThemes(Iterator<Theme> iterator) {
        if(iterator == null) {
            return;
        }
        while(iterator.hasNext()) {
            Theme theme = iterator.next();
            themeMap.get().put(theme.getId(), theme);
        }
    }

    public void loadLocations(Iterator<Location> iterator) {
        if(iterator == null) {
            return;
        }
        while(iterator.hasNext()) {
            Location location = iterator.next();
            locationMap.get().put(location.getId(), location);
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
                    list = urlAdPodMap.get(url.getName());
                    if(list == null) {
                        list = new ArrayList<Handle>();
                    }
                    int oid = urlAdPodMapping.getId();
                    list.add(new AdPodHandle(adPod, oid, AdPodHandle.urlScore, urlAdPodMapping.getWeight()));
                    String urlName = UrlNormalizer.getNormalizedUrl(url.getName());
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
            List<Handle> list = null;

            while(iterator.hasNext()) {
                ThemeAdPodMapping themeAdPodMapping = iterator.next();
                theme = themeMap.get().get(themeAdPodMapping.getThemeId());
                adPod = adPodMap.get().get(themeAdPodMapping.getAdPodId());
                if(theme != null && adPod != null) {
                    list = themeAdPodMap.get(theme.getName());
                    if(list == null) {
                        list = new ArrayList<Handle>();
                    }
                    int oid = themeAdPodMapping.getId();
                    list.add(new AdPodHandle(adPod, oid, AdPodHandle.themeScore, themeAdPodMapping.getWeight()));
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
                    int oid = locationAdPodMapping.getId();
                    list.add(new AdPodHandle(adPod, oid, AdPodHandle.locationScore, 1));
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

    public AdpodIndex<Integer, Handle> getLocationAdPodMappingIndex() {
        return adpodLocationMappingIndex.get();
    }

    public AdpodIndex<String, Handle> getUrlAdPodMappingIndex() {
        return adpodUrlMappingIndex.get();
    }

    public AdpodIndex<String, Handle> getThemeAdPodMappingIndex() {
        return adpodThemeMappingIndex.get();
    }

    public AdpodIndex<String, Handle> getRunOfNetworkAdPodIndex() {
        return adpodRunOfNetworkIndex.get();
    }

    public AdpodIndex<String, Handle> getNonGeoAdPodIndex() {
        return adpodGeoNoneIndex.get();
    }

    public AdpodIndex<String, Handle> getAdpodGeoCountryIndex() {
        return adpodGeoCountryIndex.get();
    }

    public AdpodIndex<String, Handle> getAdpodGeoRegionIndex() {
        return adpodGeoRegionIndex.get();
    }
    public AdpodIndex<String, Handle> getAdpodGeoCityIndex() {
        return adpodGeoCityIndex.get();
    }
    public AdpodIndex<String, Handle> getAdpodGeoDmacodeIndex() {
        return adpodGeoDmacodeIndex.get();
    }
    public AdpodIndex<String, Handle> getAdpodGeoAreacodeIndex() {
        return adpodGeoAreacodeIndex.get();
    }

    public AdpodIndex<String, Handle> getAdpodGeoZipcodeIndex() {
        return adpodGeoZipcodeIndex.get();
    }

}
