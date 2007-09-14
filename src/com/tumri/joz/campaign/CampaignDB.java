package com.tumri.joz.campaign;

import com.tumri.cma.domain.*;
import com.tumri.utils.data.RWLockedTreeMap;
import com.tumri.utils.Pair;
import com.tumri.joz.products.Handle;
import com.tumri.joz.index.AdpodIndex;

import java.util.*;

/**
 * CampaignDB is an in-memory representation for campaign(CMA) database that holds all the campagin related domain objects for
 * consumption by other components within JoZ.
 *
 * @author bpatel
 */
public class CampaignDB {
    private static CampaignDB campaignDB = new CampaignDB();

    private RWLockedTreeMap<Integer,Campaign> campaignMap   = new RWLockedTreeMap<Integer, Campaign>();
    private RWLockedTreeMap<Integer,AdPod>    adPodMap      = new RWLockedTreeMap<Integer, AdPod>();
    private RWLockedTreeMap<Integer,OSpec>    ospecMap      = new RWLockedTreeMap<Integer, OSpec>();
    private RWLockedTreeMap<String,OSpec>    ospecNameMap   = new RWLockedTreeMap<String, OSpec>();

    private RWLockedTreeMap<Integer,Geocode>  geocodeMap    = new RWLockedTreeMap<Integer, Geocode>();
    private RWLockedTreeMap<Integer,Url>      urlMap        = new RWLockedTreeMap<Integer, Url>();
    @SuppressWarnings({"deprecation"})
    private RWLockedTreeMap<Integer,Theme>    themeMap      = new RWLockedTreeMap<Integer, Theme>();
    private RWLockedTreeMap<Integer,Location> locationMap   = new RWLockedTreeMap<Integer, Location>();

    // Map adpod Id to ospec ID
    private RWLockedTreeMap<Integer, Integer> adPodOSpecMap = new RWLockedTreeMap<Integer, Integer>();

    // Map the handles for all geocode associated adpods to avoid re-creating handles for multiple geocode elements
    // refering to same adpod.
    private RWLockedTreeMap<Integer,Handle> adPodHandlesMap = new RWLockedTreeMap<Integer, Handle>();

    // All indices required in targeting
    private AdpodIndex<Integer, Handle> adpodLocationMappingIndex = new AdpodIndex<Integer, Handle>(AdpodIndex.Attribute.kLocation);
    private AdpodIndex<String, Handle>  adpodThemeMappingIndex    = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kTheme);
    private AdpodIndex<String, Handle>  adpodUrlMappingIndex      = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kUrl);
    private AdpodIndex<String, Handle>  adpodRunOfNetworkIndex    = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kRunofNetwork);
    private AdpodIndex<String, Handle>  adpodGeoNoneIndex         = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kGeoNone);

    private AdpodIndex<String, Handle>  adpodGeoCountryIndex      = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kCountryCode);
    private AdpodIndex<String, Handle>  adpodGeoRegionIndex       = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kRegionCode);
    private AdpodIndex<String, Handle>  adpodGeoCityIndex         = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kCityCode);
    private AdpodIndex<String, Handle>  adpodGeoDmacodeIndex      = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kDMACode);
    private AdpodIndex<String, Handle>  adpodGeoAreacodeIndex     = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kAreaCode);
    private AdpodIndex<String, Handle>  adpodGeoZipcodeIndex      = new AdpodIndex<String, Handle>(AdpodIndex.Attribute.kZipCode);


    private CampaignDB() {
    }

    public static CampaignDB getInstance() {
        return campaignDB;
    }

    public OSpec getOSpecForAdPod(int adPodId) {
        int oSpecId = adPodOSpecMap.get(adPodId);
        return ospecMap.get(oSpecId);

    }

    public OSpec getOspec(String name) {
        return ospecNameMap.get(name);    
    }

    public void loadCampaigns(Iterator<Campaign> iterator) {
        if(iterator == null) {
            return;
        }
        while(iterator.hasNext()) {
            Campaign campaign = iterator.next();
            campaignMap.put(campaign.getId(), campaign);
        }
    }

    public void loadAdPods(Iterator<AdPod> iterator) {
        if(iterator == null) {
            return;
        }
        while(iterator.hasNext()) {
            AdPod adPod = iterator.next();
            adPodMap.put(adPod.getId(), adPod);
        }
        //ToDo: if adpods are changed the associated handles should be updated as well
    }

    public void loadRunOfNetworkAdPods(Iterator<AdPod> iterator) {
        if(iterator == null) {
            return;
        }
        Map<String,List<Handle>> runOfNetworkAdPodMap = new HashMap<String, List<Handle>>();
        List<Handle> list = new ArrayList<Handle>();
        while(iterator.hasNext()) {
            AdPod adPod = iterator.next();
            list.add(new AdPodHandle(adPod, adPod.getId(), AdPodHandle.runOfNetworkScore, AdPodHandle.runOfNetworkWeight));
        }
        runOfNetworkAdPodMap.put(AdpodIndex.RUN_OF_NETWORK, list);
        //ToDo: if adpods are changed the associated handles should be updated as well
        adpodRunOfNetworkIndex.put(runOfNetworkAdPodMap);
    }

    public void loadGeoNoneAdPods(Iterator<AdPod> iterator) {
        if(iterator == null) {
            return;
        }
        Map<String,List<Handle>> geoNoneAdPodMap = new HashMap<String, List<Handle>>();
        List<Handle> list = new ArrayList<Handle>();
        while(iterator.hasNext()) {
            AdPod adPod = iterator.next();
            list.add(new AdPodHandle(adPod, adPod.getId(), AdPodHandle.geoNoneScore, AdPodHandle.geoNoneWeight));
        }
        geoNoneAdPodMap.put(AdpodIndex.GEO_NONE, list);
        //ToDo: if adpods are changed the associated handles should be updated as well
        adpodGeoNoneIndex.put(geoNoneAdPodMap);
    }

    public void loadOSpecs(Iterator<OSpec> iterator) {
        if(iterator == null) {
            return;
        }
        while(iterator.hasNext()) {
            OSpec oSpec = iterator.next();
            ospecMap.put(oSpec.getId(), oSpec);
            ospecNameMap.put(oSpec.getName(), oSpec);
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
            adPodOSpecMap.put(adPodId, oSpecId);
        }

    }

    public void loadGeocodes(Iterator<Geocode> iterator) {
        if(iterator == null) {
            return;
        }
        Map<String, List<Handle>> countriesMap = new HashMap<String, List<Handle>>();
        Map<String, List<Handle>> regionsMap = new HashMap<String, List<Handle>>();
        Map<String, List<Handle>> citiesMap = new HashMap<String, List<Handle>>();
        Map<String, List<Handle>> zipcodesMap = new HashMap<String, List<Handle>>();
        Map<String, List<Handle>> dmacodesMap = new HashMap<String, List<Handle>>();
        Map<String, List<Handle>> areacodesMap = new HashMap<String, List<Handle>>();

        while(iterator.hasNext()) {
            Geocode geocode = iterator.next();
            geocodeMap.put(geocode.getId(), geocode);
            Handle handle = addAdPodHandle(geocode.getAdPodId(), AdPodHandle.defaultScore);
            List<String> countries = geocode.getCountries();
            addToMap(countries, countriesMap, handle);

            List<String> regions   = geocode.getStates();
            addToMap(regions, regionsMap, handle);

            List<String> cities    = geocode.getCities();
            addToMap(cities, citiesMap, handle);

            List<String> zipcodes  = geocode.getZipcodes();
            addToMap(zipcodes, zipcodesMap, handle);

            List<String> dmacodes  = geocode.getDmaCodes();
            addToMap(dmacodes, dmacodesMap, handle);

            List<String> areacodes = geocode.getAreaCodes();
            addToMap(areacodes, areacodesMap, handle);
        }
        getAdpodGeoCountryIndex().put(countriesMap);
        getAdpodGeoRegionIndex().put(regionsMap);
        getAdpodGeoCityIndex().put(citiesMap);
        getAdpodGeoDmacodeIndex().put(dmacodesMap);
        getAdpodGeoAreacodeIndex().put(areacodesMap);
        getAdpodGeoZipcodeIndex().put(zipcodesMap);
        //ToDo: To update the indexes when there are changes in geocodes or adpods
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

    private Handle addAdPodHandle(int adPodId, double score) {
        Handle handle = adPodHandlesMap.get(adPodId);
        if(handle == null) {
            AdPod adPod = adPodMap.get(adPodId);
            if(adPod != null) {
                handle = new AdPodHandle(adPod, adPod.getId(), score);
                adPodHandlesMap.put(adPodId, handle);
            }
            else {
                //ToDo: Throw and log appropriate exception
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
            urlMap.put(url.getId(), url);
        }
    }

    @SuppressWarnings({"deprecation"})
    public void loadThemes(Iterator<Theme> iterator) {
        if(iterator == null) {
            return;
        }
        while(iterator.hasNext()) {
            Theme theme = iterator.next();
            themeMap.put(theme.getId(), theme);
        }
    }

    public void loadLocations(Iterator<Location> iterator) {
        if(iterator == null) {
            return;
        }
        while(iterator.hasNext()) {
            Location location = iterator.next();
            locationMap.put(location.getId(), location);
        }
    }

    public void loadUrlAdPodMappings(Iterator<UrlAdPodMapping> iterator) {
        if(iterator == null) {
            return;
        }
        Map<String,List<Handle>> urlAdPodMap      = new HashMap<String, List<Handle>>();
        while(iterator.hasNext()) {
            UrlAdPodMapping urlAdPodMapping = iterator.next();
            Url url = urlMap.get(urlAdPodMapping.getUrlId());
            List<Handle> list = null;
            if(url != null) {
                list = urlAdPodMap.get(url.getName());
            }
            if(list == null) {
                list = new ArrayList<Handle>();
            }
            int oid = urlAdPodMapping.getId();
            list.add(new AdPodHandle(adPodMap.get(urlAdPodMapping.getAdPodId()), oid, AdPodHandle.urlScore, urlAdPodMapping.getWeight()));
            if(url != null) {
                urlAdPodMap.put(url.getName(), list);
            }
        }
        adpodUrlMappingIndex.put(urlAdPodMap);
    }

    @SuppressWarnings({"deprecation"})
    public void loadThemeAdPodMappings(Iterator<ThemeAdPodMapping> iterator) {
        if(iterator == null) {
            return;
        }
        Map<String, List<Handle>>    themeAdPodMap    = new HashMap<String, List<Handle>>();
        while(iterator.hasNext()) {
            ThemeAdPodMapping themeAdPodMapping = iterator.next();
            Theme theme = themeMap.get(themeAdPodMapping.getThemeId());
            List<Handle> list = null;
            if(theme != null) {
                list = themeAdPodMap.get(theme.getName());
            }
            if(list == null) {
                list = new ArrayList<Handle>();
            }
            int oid = themeAdPodMapping.getId();
            list.add(new AdPodHandle(adPodMap.get(themeAdPodMapping.getAdPodId()), oid, AdPodHandle.themeScore, themeAdPodMapping.getWeight()));
            if(theme != null) {
                themeAdPodMap.put(theme.getName(), list);
            }
        }
        adpodThemeMappingIndex.put(themeAdPodMap);
    }

    public void loadLocationAdPodMappings(Iterator<LocationAdPodMapping> iterator) {
        if(iterator == null) {
            return;
        }
        Map<Integer, List<Handle>> locationAdPodMap = new HashMap<Integer, List<Handle>>();
        while(iterator.hasNext()) {
            LocationAdPodMapping locationAdPodMapping = iterator.next();

            List<Handle> list = locationAdPodMap.get(locationAdPodMapping.getLocationId());
            if(list == null) {
                list = new ArrayList<Handle>();
            }
            //ToDo: add weight field to LocationAdpodMapping class
            int oid = locationAdPodMapping.getId();
            list.add(new AdPodHandle(adPodMap.get(locationAdPodMapping.getAdPodId()), oid, AdPodHandle.locationScore, 1));
            Location location = locationMap.get(locationAdPodMapping.getLocationId());
            if(location != null) {
                locationAdPodMap.put(location.getExternalId(), list);                
            }
        }
        adpodLocationMappingIndex.put(locationAdPodMap);
    }

    public AdpodIndex<Integer, Handle> getLocationAdPodMappingIndex() {
        return adpodLocationMappingIndex;
    }

    public AdpodIndex<String, Handle> getUrlAdPodMappingIndex() {
        return adpodUrlMappingIndex;
    }
    
    public AdpodIndex<String, Handle> getThemeAdPodMappingIndex() {
        return adpodThemeMappingIndex;
    }

    public AdpodIndex<String, Handle> getRunOfNetworkAdPodIndex() {
        return adpodRunOfNetworkIndex;
    }

    public AdpodIndex<String, Handle> getNonGeoAdPodIndex() {
        return adpodGeoNoneIndex;
    }

    public AdpodIndex<String, Handle> getAdpodGeoCountryIndex() {
        return adpodGeoCountryIndex;
    }

    public AdpodIndex<String, Handle> getAdpodGeoRegionIndex() {
        return adpodGeoRegionIndex;
    }
    public AdpodIndex<String, Handle> getAdpodGeoCityIndex() {
        return adpodGeoCityIndex;
    }
    public AdpodIndex<String, Handle> getAdpodGeoDmacodeIndex() {
        return adpodGeoDmacodeIndex;
    }
    public AdpodIndex<String, Handle> getAdpodGeoAreacodeIndex() {
        return adpodGeoAreacodeIndex;
    }
    public AdpodIndex<String, Handle> getAdpodGeoZipcodeIndex() {
        return adpodGeoZipcodeIndex;
    }

}
