package com.tumri.joz.campaign;

import com.tumri.cma.domain.*;
import com.tumri.cma.misc.SexpOSpecHelper;
import com.tumri.utils.Pair;
import com.tumri.utils.sexp.BadSexpException;
import com.tumri.joz.products.Handle;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.utils.AppProperties;

import java.util.*;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * CampaignDB is an in-memory representation for campaign(CMA) database that holds all the campagin related domain objects for
 * consumption by other components within JoZ.
 *
 * @author bpatel
 */
@SuppressWarnings({"deprecation"})
public abstract class CampaignDB {

    private static Logger log = Logger.getLogger (CampaignDB.class);

    private static CampaignDB campaignDB = CampaignDBCompleteRefreshImpl.getInstance(); //CampaignDBIncrementalRefreshImpl.getInstance();

    public static CampaignDB getInstance() {
        return campaignDB;
    }

    protected void initialize() {
    }

    public abstract OSpec getOSpecForAdPod(int adPodId);

    public abstract OSpec getOspec(String name);

    public abstract OSpec getOspec(int oSpecId);

    public abstract Url getUrl(String urlName);

    public abstract Url getUrl(int urlId);

    public abstract Theme getTheme(String themeName);

    public abstract Theme getTheme(int themeId);

    public abstract Location getLocation(int locationId);

    public abstract Geocode getGeocode(int geocodeId);

    public abstract List<OSpec>  getAllOSpecs();

    public abstract void addUrl(Url url);

    public abstract void deleteUrl(String urlName);

    public abstract void addTheme(Theme theme);

    public abstract void deleteTheme(String themeName);

    public abstract void addLocation(Location location);

    public abstract void deleteLocation(int locationId);

    public abstract void addAdPod(AdPod adPod);

    public abstract void deleteAdPod(int adPodId);

    public abstract void addGeocode(Geocode geocode);

    public abstract void deleteGeocode(int geocodeId);

    public abstract void addNonGeoAdPod(int adPodId);

    public abstract void deleteNonGeoAdPod(int adPodId);

    public abstract void addAdpodOSpecMapping(int adPodId, int oSpecId);

    public abstract void addOSpec(OSpec oSpec);

    public abstract void deleteOSpec(String oSpecName);

    public abstract void addUrlMapping(UrlAdPodMapping mapping);

    public abstract void addThemeMapping(ThemeAdPodMapping mapping);

    public abstract void addLocationMapping(LocationAdPodMapping mapping);

    public abstract void addGeocodeMapping(Geocode geocode, int adPodId, float weight);

    public abstract void deleteUrlMapping(String urlName, int adPodId);

    public abstract void deleteThemeMapping(String themeName, int adPodId);

    public abstract void deleteLocationMapping(int locationId, int adPodId);

    public abstract void deleteGeocodeMapping(Geocode geocode, int adPodId);

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

    public abstract AtomicAdpodIndex<Integer, Handle> getLocationAdPodMappingIndex();

    public abstract AtomicAdpodIndex<String, Handle> getUrlAdPodMappingIndex();
    
    public abstract AtomicAdpodIndex<String, Handle> getThemeAdPodMappingIndex();

    public abstract AtomicAdpodIndex<String, Handle> getRunOfNetworkAdPodIndex();

    public abstract AtomicAdpodIndex<String, Handle> getNonGeoAdPodIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodGeoCountryIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodGeoRegionIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodGeoCityIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodGeoDmacodeIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodGeoAreacodeIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodGeoZipcodeIndex();

    public abstract AdPod getAdPod(AdPodHandle handle);

    public abstract AdPod getAdPod(int adPodId);

    public abstract Campaign getCampaign(AdPodHandle handle);

    public abstract Campaign getCampaign(int campaignId);

    public abstract void addCampaign(Campaign campaign);

    public abstract void delCampaign(int campaignId);

    public abstract void addRecipe(Recipe recipe);

    public abstract void delRecipe(int recipeId);

    public abstract Recipe getRecipe(int recipeId);

    public abstract void addTSpec(TSpec tspec);

    public abstract void delTSpec(int tspecId);

    public abstract TSpec getTspec(int tspecId);
    
    public abstract void addAdpodCampaignMapping(int adPodId, int campaignId);

    public abstract void addNonUrlAdPod(int adPodId);

    public abstract void deleteNonUrlAdPod(int adPodId);

    public abstract void loadRecipes(Iterator<Recipe> iterator);

    public abstract void loadUrlNoneAdPods(Iterator<AdPod> iterator);

    public abstract void loadAdPodCampaignMapping(Iterator<Pair<Integer, Integer>> iterator);

    public abstract AtomicAdpodIndex<String, Handle> getNonUrlAdPodIndex();

    public abstract void loadLocationNameIdMapping(Iterator<Pair<String, Integer>> iterator);

    public abstract Integer getLocationIdForName(String locationName);

    public abstract void addLocationNameIdMap(String locName, Integer id);

    public abstract void deleteLocationNameIdMapping(String themeName);

    public abstract boolean isEmpty();

    public abstract ArrayList<Campaign> getCampaigns();

    public abstract ArrayList<OSpec> getOSpecs();

    public abstract ArrayList<AdPod> getAdPods();

    public abstract ArrayList<TSpec> getTSpecs();

    public abstract ArrayList<Recipe> getRecipes();
    
    public abstract void loadExternalVariableAdPods(HashMap<String, ArrayList<AdPodExternalVariableMapping>> iterator);
    
    public abstract void loadNonExternalVariableAdPods(HashMap<String, ArrayList<AdPod>> iterator);
    
    public abstract AtomicAdpodIndex<String, Handle> getExternalVariableAdPodMappingIndex(String variableName);
    
    public abstract AtomicAdpodIndex<String, Handle> getNonExternalVariableAdPodMappingIndex(String variableName);

}
