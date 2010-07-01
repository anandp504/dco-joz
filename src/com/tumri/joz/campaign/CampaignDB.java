package com.tumri.joz.campaign;

import com.tumri.cma.domain.*;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.products.Handle;
import com.tumri.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * CampaignDB is an in-memory representation for campaign(CMA) database that holds all the campagin related domain objects for
 * consumption by other components within JoZ.
 *
 * @author bpatel
 */
@SuppressWarnings({"deprecation"})
public abstract class CampaignDB {

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

    public abstract Location getLocation(int locationId);

    public abstract Geocode getGeocode(int geocodeId);

    public abstract List<OSpec>  getAllOSpecs();

    public abstract void addUrl(Url url);

    public abstract void deleteUrl(String urlName);

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

    public abstract void addLocationMapping(LocationAdPodMapping mapping);

    public abstract void addGeocodeMapping(Geocode geocode, int adPodId, float weight);

    public abstract void deleteUrlMapping(String urlName, int adPodId);

    public abstract void deleteLocationMapping(int locationId, int adPodId);

    public abstract void deleteGeocodeMapping(Geocode geocode, int adPodId);

    public abstract void loadCampaigns(Iterator<Campaign> iterator);

    public abstract void loadAdPods(Iterator<AdPod> iterator);

    public abstract void loadExperiences(Iterator<Experience> iterator);

    public abstract void loadRunOfNetworkAdPods(Iterator<AdPod> iterator);

    public abstract void loadGeoNoneAdPods(Iterator<AdPod> iterator);

    public abstract void loadOSpecs(Iterator<OSpec> iterator);

    public abstract void loadAdPodOSpecMapping(Iterator<Pair<Integer, Integer>> iterator);

    public abstract void loadGeocodes(Iterator<Geocode> iterator);

    public abstract void loadUrls(Iterator<Url> iterator);

    public abstract void loadLocations(Iterator<Location> iterator);

    public abstract void loadUrlAdPodMappings(Iterator<UrlAdPodMapping> iterator);

    public abstract void loadLocationAdPodMappings(Iterator<LocationAdPodMapping> iterator);

    public abstract AtomicAdpodIndex<Integer, Handle> getLocationAdPodMappingIndex();

    public abstract AtomicAdpodIndex<String, Handle> getUrlAdPodMappingIndex();
    
    public abstract AtomicAdpodIndex<String, Handle> getNonGeoAdPodIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodGeoCountryIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodGeoRegionIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodGeoCityIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodGeoDmacodeIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodGeoAreacodeIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodGeoZipcodeIndex();

    public abstract AdPod getAdPod(AdPodHandle handle);

    public abstract AdPod getAdPod(int adPodId);
    
    public abstract Experience getExperience(int expId);

    public abstract Campaign getCampaign(AdPodHandle handle);

    public abstract Campaign getCampaign(int campaignId);

    public abstract void addCampaign(Campaign campaign);

    public abstract void delCampaign(int campaignId);

    public abstract void addExperience(Experience exp);

    public abstract void delExperience(int expId);

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

    public abstract CAM getDefaultCAM(int adpodId);

    public abstract AtomicAdpodIndex<String, Handle> getAdpodAgeIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodAgeNoneIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodGenderIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodGenderNoneIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodBTIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodBTNoneIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodMSIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodMSNoneIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodHHIIndex();

    public abstract AtomicAdpodIndex<String, Handle> getAdpodHHINoneIndex();

    public abstract void loadAgeAdPodMappings(Iterator<AgeAdPodMapping> iterator);

    public abstract void loadGenderAdPodMappings(Iterator<GenderAdPodMapping> iterator);

    public abstract void loadBTAdPodMappings(Iterator<BTAdPodMapping> iterator);

    public abstract void loadMSAdPodMappings(Iterator<MSAdPodMapping> iterator);
    
    public abstract void loadHHIAdPodMappings(Iterator<HHIAdPodMapping> iterator);

    public abstract void loadAgeNoneAdPods(Iterator<AdPod> iterator);

    public abstract void loadHHINoneAdPods(Iterator<AdPod> iterator);

    public abstract void loadMSNoneAdPods(Iterator<AdPod> iterator);

    public abstract void loadBTNoneAdPods(Iterator<AdPod> iterator);

    public abstract void loadGenderNoneAdPods(Iterator<AdPod> iterator);



}
