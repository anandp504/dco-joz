package com.tumri.joz.campaign;

import com.tumri.cma.CMAConfigurationException;
import com.tumri.cma.CMAFactory;
import com.tumri.cma.RepositoryException;
import com.tumri.cma.domain.*;
import com.tumri.cma.service.CampaignDeltaProvider;
import com.tumri.joz.campaign.wm.VectorDB;
import com.tumri.joz.campaign.wm.VectorHandle;
import com.tumri.joz.campaign.wm.VectorHandleFactory;
import com.tumri.joz.utils.AppProperties;
import com.tumri.utils.Pair;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * CamapignDBDataLoader loads all the campaign related data from the repository using the CampaignDeltaProvider API.
 *
 * @author bpatel
 */
public class CampaignDBDataLoader {
    private static CampaignDBDataLoader dataLoader = new CampaignDBDataLoader();
    private static Logger log = Logger.getLogger (CampaignDBDataLoader.class);
    private CampaignDBDataLoader() {
    }
    
    public static CampaignDBDataLoader getInstance() {
        return dataLoader;
    }

    @SuppressWarnings({"deprecation"})
    public void loadData() throws CampaignDataLoadingException {
        CampaignDB campaignDB = CampaignDB.getInstance();
        CampaignDeltaProvider deltaProvider;
        String region, extVars;
        try {
            region = AppProperties.getInstance().getProperty("com.tumri.campaign.data.region.name");
            if(region != null) {
                region = region.trim();
            }
            extVars = AppProperties.getInstance().getProperty("externalTargetingVariables");
            if(extVars != null) {
                extVars = extVars.trim();
            }

        }
        catch(NullPointerException e) {
            throw new CampaignDataLoadingException("Error loading joz.properties", e);            
        }
        catch(Exception e) {
            throw new CampaignDataLoadingException("cannot load joz.properties", e);
        }


        try {
            CMAFactory factory = CMAFactory.getInstance(AppProperties.getInstance().getProperties());
            deltaProvider = factory.getCampaignDeltaProvider();
            //long lispDataReadStartTime = System.currentTimeMillis();

            // Important: The order in which the get methods are called on the deltaProvider and the load methods that
            // are called on the campaignDB is very important. By calling the methods in the order below, inconsistencies
            // in the data can be minimized.
            // Example: We always first get the mappings and then the url, theme, adpods, etc. so that we know for sure
            // that later when we call the get method for those objects, those objects that are present in mappings, will
            // be present while loading individual objects as well. One exception to this is if the object got deleted
            // between the following two calls
            // 1. get method for url adpod mapping
            // 2. get method for adpods
            // The above situation though not very common as the window between two calls will be in miliseconds,
            // if occured should be appropriately handled by the CampaignDB.

            Iterator<UrlAdPodMapping>        urlsAdPodMappingIterator      = deltaProvider.getUrlAdpodMappings(region);
            Iterator<LocationAdPodMapping>   locationsAdPodMappingIterator = deltaProvider.getLocationAdpodMappings(region);
            Iterator<Pair<Integer, Integer>> adPodOSpecMappings            = deltaProvider.getAllAdPodOSpecMappings(region);
            Iterator<Url>      urlsIterator      = deltaProvider.getUrls(region);
            Iterator<Location> locationsIterator = deltaProvider.getLocations(region);
            Iterator<AdPod>    runOfNetworkAdPodsIterator = deltaProvider.getNonSiteSpecificAdPods(region);
            Iterator<AdPod>    geoNoneAdPodsIterator      = deltaProvider.getNonGeoSpecificAdPods(region);
            Iterator<Geocode>  geocodesIterator  = deltaProvider.getGeocodes(region);
            Iterator<AdPod>    adPodsIterator    = deltaProvider.getAdPods(region);
            Iterator<OSpec>    oSpecsIterator    = deltaProvider.getOspecs(region);
            Iterator<Campaign> campaignsIterator = deltaProvider.getCampaigns(region);
            Iterator<Recipe> recipesIterator = deltaProvider.getRecipes(region);
            Iterator<Experience> expIterator = deltaProvider.getExperiences(region);
            Iterator<AdPod> urlNoneAdPodsIterator = deltaProvider.getNonUrlSpecificAdPods(region);
            Iterator<Pair<Integer, Integer>> adPodCampaignMappings            = deltaProvider.getAllAdPodCampaignMappings(region);
            Iterator<Pair<String, Integer>> locationNameIdMappings            = deltaProvider.getLocationNameIdMappings();
            HashMap<String, ArrayList<AdPodExternalVariableMapping>> extVariablesAdPodMap = deltaProvider.getExternalVariableAdpodMappings(region);
            HashMap<String, ArrayList<AdPod>> nonExtVariablesAdPodMap = deltaProvider.getNonExternalVariableAdPods(region);
            Iterator<AgeAdPodMapping> ageMappings = deltaProvider.getAllAgeAdPodMappings(region);
            Iterator<GenderAdPodMapping> genderMappings = deltaProvider.getAllGenderAdPodMappings(region);
            Iterator<BTAdPodMapping> btMappings = deltaProvider.getAllBTAdPodMappings(region);
            Iterator<MSAdPodMapping> msMappings = deltaProvider.getAllMSAdPodMappings(region);
            Iterator<HHIAdPodMapping> hhiMappings = deltaProvider.getAllHHIAgeAdPodMappings(region);
            Iterator<AdPod> nonAgeAdpods = deltaProvider.getNonAgeAdpods(region);
            Iterator<AdPod> nonGenderAdpods = deltaProvider.getNonGenderAdpods(region);
            Iterator<AdPod> nonBTAdpods = deltaProvider.getNonBTAdpods(region);
            Iterator<AdPod> nonMSAdpods = deltaProvider.getNonMSAdpods(region);
            Iterator<AdPod> nonHHIAdpods = deltaProvider.getNonHHIAdpods(region);
            //long lispDataReadEndTime = System.currentTimeMillis();
            //System.out.println("Data Retrieval from Lisp Provider API: " + (lispDataReadEndTime - lispDataReadStartTime) + " ms");

            //@todo: Clone the objects instead of getting again from database or pull in the ospec query cache into CampaignDB
            Iterator<OSpec>    oSpecsIterator2   = deltaProvider.getOspecs(region);

            //long campaignIndexStartTime = System.currentTimeMillis();

            campaignDB.loadUrls(urlsIterator);
            campaignDB.loadLocations(locationsIterator);
            VectorHandleFactory vhFactory = new VectorHandleFactory();
            campaignDB.loadAdPods(adPodsIterator, vhFactory);
            campaignDB.loadGeocodes(geocodesIterator);
            campaignDB.loadOSpecs(oSpecsIterator);
            campaignDB.loadAdPodOSpecMapping(adPodOSpecMappings);
            campaignDB.loadCampaigns(campaignsIterator);
            campaignDB.loadUrlAdPodMappings(urlsAdPodMappingIterator);
            campaignDB.loadLocationAdPodMappings(locationsAdPodMappingIterator);
            campaignDB.loadRunOfNetworkAdPods(runOfNetworkAdPodsIterator);
            campaignDB.loadGeoNoneAdPods(geoNoneAdPodsIterator);
            campaignDB.loadUrlNoneAdPods(urlNoneAdPodsIterator);
            campaignDB.loadRecipes(recipesIterator);
            campaignDB.loadAdPodCampaignMapping(adPodCampaignMappings);
            campaignDB.loadExternalVariableAdPods(extVariablesAdPodMap);
            campaignDB.loadNonExternalVariableAdPods(nonExtVariablesAdPodMap);
            campaignDB.loadExperiences(expIterator);
            campaignDB.loadAgeAdPodMappings(ageMappings);
            campaignDB.loadAgeNoneAdPods(nonAgeAdpods);
            campaignDB.loadGenderAdPodMappings(genderMappings);
            campaignDB.loadGenderNoneAdPods(nonGenderAdpods);
            campaignDB.loadBTAdPodMappings(btMappings);
            campaignDB.loadBTNoneAdPods(nonBTAdpods);
            campaignDB.loadMSAdPodMappings(msMappings);
            campaignDB.loadMSNoneAdPods(nonMSAdpods);
            campaignDB.loadHHIAdPodMappings(hhiMappings);
            campaignDB.loadHHINoneAdPods(nonHHIAdpods);
            TransientDataManager.getInstance().reloadInCampaignDB();

            TSpecQueryCache.getInstance().load(oSpecsIterator2);

            //Load the Recipe information into the VectorDB as default rules
            SortedSet<VectorHandle> defHandles = vhFactory.getCurrHandles();
            VectorDB.getInstance().addDefNewHandles(defHandles);
            vhFactory.clear();
            //TODO: Also get the personalization rules

            //long campaignIndexEndTime = System.currentTimeMillis();
            //System.out.println("Campaign Indexing Time: " + (campaignIndexEndTime - campaignIndexStartTime) + " ms");

        }
        catch(CMAConfigurationException e) {
            log.error("Invalid Configuration setup for CMA API", e);
            throw new CampaignDataLoadingException("Invalid configuration setup for CMA API", e);
        }
        catch (RepositoryException e) {
            log.error("Error occured while retrieving Campaign data", e);
            throw new CampaignDataLoadingException("Error occured while retrieving Camapign related objects from repository", e);
        }
        catch(Throwable t) {
            //This exception ensures that the calling client doesnt have to handle any runtime exceptions.
            //especially since the calling client for this class will be a poller which needs a graceful exit point.
            log.error("Unexpected Error occured while loading campaign data", t);            
            throw new CampaignDataLoadingException("Unexpected Error occured while loading campaign data", t);
        }
    }

}
