package com.tumri.joz.campaign;

import com.tumri.cma.domain.*;
import com.tumri.utils.data.RWLockedTreeMap;
import com.tumri.utils.Pair;
import com.tumri.joz.products.Handle;
import com.tumri.joz.index.AdpodIndex;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.utils.AppProperties;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * CampaignDB is an in-memory representation for campaign(CMA) database that holds all the campagin related domain objects for
 * consumption by other components within JoZ.
 *
 * @author bpatel
 */
public class CampaignDB {
    private static CampaignDB campaignDB = new CampaignDB();

    private AtomicReference<RWLockedTreeMap<Integer,Campaign>> campaignMap   = new AtomicReference<RWLockedTreeMap<Integer, Campaign>>(new RWLockedTreeMap<Integer, Campaign>());
    private AtomicReference<RWLockedTreeMap<Integer,AdPod>>    adPodMap      = new AtomicReference<RWLockedTreeMap<Integer, AdPod>>(new RWLockedTreeMap<Integer, AdPod>());
    private AtomicReference<RWLockedTreeMap<Integer,OSpec>>    ospecMap      = new AtomicReference<RWLockedTreeMap<Integer, OSpec>>(new RWLockedTreeMap<Integer, OSpec>());
    private AtomicReference<RWLockedTreeMap<String,OSpec>>    ospecNameMap   = new AtomicReference<RWLockedTreeMap<String, OSpec>>(new RWLockedTreeMap<String, OSpec>());
    private RWLockedTreeMap<String, OSpec>                  tempOSpecNameMap = new RWLockedTreeMap<String, OSpec>();

    private AtomicReference<RWLockedTreeMap<Integer,Geocode>>  geocodeMap    = new AtomicReference<RWLockedTreeMap<Integer, Geocode>>(new RWLockedTreeMap<Integer, Geocode>());
    private AtomicReference<RWLockedTreeMap<Integer,Url>>      urlMap        = new AtomicReference<RWLockedTreeMap<Integer, Url>>(new RWLockedTreeMap<Integer, Url>());
    @SuppressWarnings({"deprecation"})
    private AtomicReference<RWLockedTreeMap<Integer,Theme>>    themeMap      = new AtomicReference<RWLockedTreeMap<Integer, Theme>>(new RWLockedTreeMap<Integer, Theme>());
    private AtomicReference<RWLockedTreeMap<Integer,Location>> locationMap   = new AtomicReference<RWLockedTreeMap<Integer, Location>>(new RWLockedTreeMap<Integer, Location>());

    // Map adpod Id to ospec ID
    private AtomicReference<RWLockedTreeMap<Integer, Integer>> adPodOSpecMap = new AtomicReference<RWLockedTreeMap<Integer, Integer>>(new RWLockedTreeMap<Integer, Integer>());

    // Map the handles for all geocode associated adpods to avoid re-creating handles for multiple geocode elements
    // refering to same adpod.
    private AtomicReference<RWLockedTreeMap<Integer,Handle>> adPodConuntryHandlesMap = new AtomicReference<RWLockedTreeMap<Integer, Handle>>(new RWLockedTreeMap<Integer, Handle>());
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

    private String defaultRealmOSpecName = "T-SPEC-http://default-realm/";

    private CampaignDB() {
        initialize();
    }

    public static CampaignDB getInstance() {
        return campaignDB;
    }

    private void initialize() {
        String defaultOSpecName = null;
        try {
            defaultOSpecName = AppProperties.getInstance().getProperty("com.tumri.targeting.default.realm.ospec.name");
        }
        catch(NullPointerException e) {
            //ignore the error and pick the default realm specified specified in java class instead
        }
        catch(Exception e) {
            //ignore the error and pick the default realm specified specified in java class instead
        }

        if(defaultOSpecName != null && !("".equals(defaultOSpecName))) {
            defaultRealmOSpecName = defaultOSpecName;
        }

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

    public AdPod getDefaultAdPod() {
        return adPodMap.get().get(adPodMap.get().firstKey());
    }

    public OSpec getDefaultOSpec() {
        return ospecNameMap.get().get(defaultRealmOSpecName);
    }

    public void loadCampaigns(Iterator<Campaign> iterator) {
        if(iterator == null) {
            return;
        }
        RWLockedTreeMap<Integer,Campaign> map = new RWLockedTreeMap<Integer, Campaign>();
        while(iterator.hasNext()) {
            Campaign campaign = iterator.next();
            map.put(campaign.getId(), campaign);
        }
        campaignMap.compareAndSet(campaignMap.get(), map);
    }

    public void loadAdPods(Iterator<AdPod> iterator) {
        if(iterator == null) {
            return;
        }
        RWLockedTreeMap<Integer,AdPod> map = new RWLockedTreeMap<Integer, AdPod>();
        while(iterator.hasNext()) {
            AdPod adPod = iterator.next();
            map.put(adPod.getId(), adPod);
        }
        adPodMap.compareAndSet(adPodMap.get(), map);
    }

    public void loadRunOfNetworkAdPods(Iterator<AdPod> iterator) {
        if(iterator == null) {
            return;
        }
        Map<String,List<Handle>> runOfNetworkAdPodMap = new HashMap<String, List<Handle>>();
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

    public void loadGeoNoneAdPods(Iterator<AdPod> iterator) {
        if(iterator == null) {
            return;
        }
        Map<String,List<Handle>> geoNoneAdPodMap = new HashMap<String, List<Handle>>();
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

        adPodConuntryHandlesMap.compareAndSet(adPodConuntryHandlesMap.get(), countryHandlesMap);
        adPodRegionHandlesMap.compareAndSet(adPodRegionHandlesMap.get(), regionHandlesMap);
        adPodCityHandlesMap.compareAndSet(adPodCityHandlesMap.get(), cityHandlesMap);
        adPodZipcodeHandlesMap.compareAndSet(adPodZipcodeHandlesMap.get(), zipcodeHandlesMap);
        adPodDmacodeHandlesMap.compareAndSet(adPodDmacodeHandlesMap.get(), dmacodeHandlesMap);
        adPodAreacodeHandlesMap.compareAndSet(adPodAreacodeHandlesMap.get(), areacodeHandlesMap);


        countryIndex.put(countriesMap);
        regionIndex.put(regionsMap);
        cityIndex.put(citiesMap);
        dmacodeIndex.put(dmacodesMap);
        areacodeIndex.put(areacodesMap);
        zipcodeIndex.put(zipcodesMap);

        getAdpodGeoCountryIndex().set(countryIndex);
        getAdpodGeoRegionIndex().set(regionIndex);
        getAdpodGeoCityIndex().set(cityIndex);
        getAdpodGeoDmacodeIndex().set(dmacodeIndex);
        getAdpodGeoAreacodeIndex().set(areacodeIndex);
        getAdpodGeoZipcodeIndex().set(zipcodeIndex);
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
            AdPod adPod = adPodMap.get().get(adPodId);
            if(adPod != null) {
                handle = new AdPodHandle(adPod, adPod.getId(), score);
                handlesMap.put(adPodId, handle);
            }
            else {
                //@todo: Throw or log appropriate warning
                //log Adpod not found, some inconsistency need to be investigated further
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
        Map<String,List<Handle>>   urlAdPodMap = new HashMap<String, List<Handle>>();
        AdpodIndex<String, Handle> index       = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kUrl);
        while(iterator.hasNext()) {
            UrlAdPodMapping urlAdPodMapping = iterator.next();
            Url url = urlMap.get().get(urlAdPodMapping.getUrlId());
            List<Handle> list = null;
            if(url != null) {
                list = urlAdPodMap.get(url.getName());
            }
            if(list == null) {
                list = new ArrayList<Handle>();
            }
            int oid = urlAdPodMapping.getId();
            list.add(new AdPodHandle(adPodMap.get().get(urlAdPodMapping.getAdPodId()), oid, AdPodHandle.urlScore, urlAdPodMapping.getWeight()));
            if(url != null) {
                String urlName = UrlNormalizer.getNormalizedUrl(url.getName());
                if(urlName != null) {
                    urlAdPodMap.put(urlName, list);
                }
                else {
                    //log warning message, indicating some possible bug in normalizing process
                    urlAdPodMap.put(url.getName(), list);
                }
            }
        }
        index.put(urlAdPodMap);
        adpodUrlMappingIndex.set(index);
    }

    @SuppressWarnings({"deprecation"})
    public void loadThemeAdPodMappings(Iterator<ThemeAdPodMapping> iterator) {
        if(iterator == null) {
            return;
        }
        Map<String, List<Handle>>  themeAdPodMap = new HashMap<String, List<Handle>>();
        AdpodIndex<String, Handle> index         = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kTheme);
        while(iterator.hasNext()) {
            ThemeAdPodMapping themeAdPodMapping = iterator.next();
            Theme theme = themeMap.get().get(themeAdPodMapping.getThemeId());
            List<Handle> list = null;
            if(theme != null) {
                list = themeAdPodMap.get(theme.getName());
            }
            if(list == null) {
                list = new ArrayList<Handle>();
            }
            int oid = themeAdPodMapping.getId();
            list.add(new AdPodHandle(adPodMap.get().get(themeAdPodMapping.getAdPodId()), oid, AdPodHandle.themeScore, themeAdPodMapping.getWeight()));
            if(theme != null) {
                themeAdPodMap.put(theme.getName(), list);
            }
        }
        index.put(themeAdPodMap);
        adpodThemeMappingIndex.set(index);
    }

    public void loadLocationAdPodMappings(Iterator<LocationAdPodMapping> iterator) {
        if(iterator == null) {
            return;
        }
        Map<Integer, List<Handle>>  locationAdPodMap = new HashMap<Integer, List<Handle>>();
        AdpodIndex<Integer, Handle> index            = new AdpodIndex<Integer, Handle>(AdpodIndex.Attribute.kLocation);
        while(iterator.hasNext()) {
            LocationAdPodMapping locationAdPodMapping = iterator.next();

            List<Handle> list = locationAdPodMap.get(locationAdPodMapping.getLocationId());
            if(list == null) {
                list = new ArrayList<Handle>();
            }
            //@todo: add weight field to LocationAdpodMapping class
            int oid = locationAdPodMapping.getId();
            list.add(new AdPodHandle(adPodMap.get().get(locationAdPodMapping.getAdPodId()), oid, AdPodHandle.locationScore, 1));
            Location location = locationMap.get().get(locationAdPodMapping.getLocationId());
            if(location != null) {
                locationAdPodMap.put(location.getExternalId(), list);                
            }
        }
        index.put(locationAdPodMap);
        adpodLocationMappingIndex.set(index);
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
