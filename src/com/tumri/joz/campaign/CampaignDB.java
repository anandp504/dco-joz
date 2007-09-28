package com.tumri.joz.campaign;

import com.tumri.cma.domain.*;
import com.tumri.utils.Pair;
import com.tumri.joz.products.Handle;
import com.tumri.joz.index.AdpodIndex;
import com.tumri.joz.utils.AppProperties;

import java.util.*;

/**
 * CampaignDB is an in-memory representation for campaign(CMA) database that holds all the campagin related domain objects for
 * consumption by other components within JoZ.
 *
 * @author bpatel
 */
public abstract class CampaignDB {

    private String defaultRealmOSpecName = "T-SPEC-http://default-realm/";

    private static CampaignDB campaignDB = CampaignDBCompleteRefreshImpl.getInstance(); //CampaignDBIncrementalRefreshImpl.getInstance();

    public static CampaignDB getInstance() {
        return campaignDB;
    }

    protected void initialize() {
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

    public String getDefaultRealmOSpecName() {
        return defaultRealmOSpecName;
    }
    
    public abstract OSpec getOSpecForAdPod(int adPodId);

    public abstract OSpec getOspec(String name);
    
    public abstract List<OSpec>  getAllOSpecs();

    public abstract void addOSpec(OSpec oSpec);

    public abstract void deleteOSpec(String oSpecName);

    public abstract void addUrlMapping(String urlName, String tSpecName, float weight);

    public abstract void addThemeMapping(String themeName, String tSpecName, float weight);

    public abstract void addLocationMapping(String locationId, String tSpecName, float weight);

    public abstract void addGeocodeMapping(Geocode geocode, String tSpecName, float weight);

    public abstract void deleteUrlMapping(String urlName, String tSpecName, float weight);

    public abstract void deleteThemeMapping(String themeName, String tSpecName, float weight);

    public abstract void deleteLocationMapping(String locationId, String tSpecName, float weight);

    public abstract void deleteGeocodeMapping(Geocode geocode, String tSpecName, float weight);

    public abstract OSpec getDefaultOSpec();

    public abstract void loadCampaigns(Iterator<Campaign> iterator);

    public abstract void loadAdPods(Iterator<AdPod> iterator);

    public abstract void loadRunOfNetworkAdPods(Iterator<AdPod> iterator);

    public abstract void loadGeoNoneAdPods(Iterator<AdPod> iterator);

    public abstract void loadOSpecs(Iterator<OSpec> iterator);

    public abstract void loadAdPodOSpecMapping(Iterator<Pair<Integer, Integer>> iterator);

    public abstract void loadGeocodes(Iterator<Geocode> iterator);

    public abstract void loadUrls(Iterator<Url> iterator);

    @SuppressWarnings({"deprecation"})
    public abstract void loadThemes(Iterator<Theme> iterator);

    public abstract void loadLocations(Iterator<Location> iterator);

    public abstract void loadUrlAdPodMappings(Iterator<UrlAdPodMapping> iterator);

    @SuppressWarnings({"deprecation"})
    public abstract void loadThemeAdPodMappings(Iterator<ThemeAdPodMapping> iterator);

    public abstract void loadLocationAdPodMappings(Iterator<LocationAdPodMapping> iterator);

    public abstract AdpodIndex<Integer, Handle> getLocationAdPodMappingIndex();

    public abstract AdpodIndex<String, Handle> getUrlAdPodMappingIndex();
    
    public abstract AdpodIndex<String, Handle> getThemeAdPodMappingIndex();

    public abstract AdpodIndex<String, Handle> getRunOfNetworkAdPodIndex();

    public abstract AdpodIndex<String, Handle> getNonGeoAdPodIndex();

    public abstract AdpodIndex<String, Handle> getAdpodGeoCountryIndex();

    public abstract AdpodIndex<String, Handle> getAdpodGeoRegionIndex();

    public abstract AdpodIndex<String, Handle> getAdpodGeoCityIndex();

    public abstract AdpodIndex<String, Handle> getAdpodGeoDmacodeIndex();

    public abstract AdpodIndex<String, Handle> getAdpodGeoAreacodeIndex();

    public abstract AdpodIndex<String, Handle> getAdpodGeoZipcodeIndex();
}
