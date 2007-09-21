package com.tumri.joz.campaign;

import com.tumri.cma.CMAConfigurationException;
import com.tumri.cma.CMAFactory;
import com.tumri.cma.RepositoryException;
import com.tumri.cma.util.DeepCopy;
import com.tumri.cma.domain.*;
import com.tumri.cma.service.CampaignDeltaProvider;

import com.tumri.utils.Pair;
import com.tumri.joz.utils.AppProperties;

import java.util.Iterator;

import org.apache.log4j.Logger;

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
        String region;
        try {
            region = AppProperties.getInstance().getProperty("com.tumri.campaign.data.region.name");
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

            Iterator<UrlAdPodMapping>        urlsAdPodMappingIterator      = deltaProvider.getUrlAdpodMappings(region);
            Iterator<ThemeAdPodMapping>      themesAdPodMappingIterator    = deltaProvider.getThemeAdpodMappings(region);
            Iterator<LocationAdPodMapping>   locationsAdPodMappingIterator = deltaProvider.getLocationAdpodMappings(region);
            Iterator<Pair<Integer, Integer>> adPodOSpecMappings            = deltaProvider.getAllAdPodOSpecMappings(region);

            Iterator<Url>      urlsIterator      = deltaProvider.getUrls(region);
            Iterator<Theme>    themesIterator    = deltaProvider.getThemes(region);
            Iterator<Location> locationsIterator = deltaProvider.getLocations(region);
            Iterator<AdPod>    runOfNetworkAdPodsIterator = deltaProvider.getNonSiteSpecificAdPods(region);
            Iterator<AdPod>    geoNoneAdPodsIterator      = deltaProvider.getNonGeoSpecificAdPods(region);
            Iterator<Geocode>  geocodesIterator  = deltaProvider.getGeocodes(region);
            Iterator<AdPod>    adPodsIterator    = deltaProvider.getAdPods(region);
            Iterator<OSpec>    oSpecsIterator    = deltaProvider.getOspecs(region);
            Iterator<Campaign> campaignsIterator = deltaProvider.getCampaigns(region);

            Iterator<OSpec>    oSpecsIterator2   = (Iterator<OSpec>) DeepCopy.copy(oSpecsIterator);

            campaignDB.loadUrls(urlsIterator);
            campaignDB.loadThemes(themesIterator);
            campaignDB.loadLocations(locationsIterator);
            campaignDB.loadAdPods(adPodsIterator);
            campaignDB.loadGeocodes(geocodesIterator);
            campaignDB.loadOSpecs(oSpecsIterator);
            campaignDB.loadAdPodOSpecMapping(adPodOSpecMappings);
            campaignDB.loadCampaigns(campaignsIterator);
            campaignDB.loadUrlAdPodMappings(urlsAdPodMappingIterator);
            campaignDB.loadThemeAdPodMappings(themesAdPodMappingIterator);
            campaignDB.loadLocationAdPodMappings(locationsAdPodMappingIterator);
            campaignDB.loadRunOfNetworkAdPods(runOfNetworkAdPodsIterator);
            campaignDB.loadGeoNoneAdPods(geoNoneAdPodsIterator);

            OSpecQueryCache.getInstance().load(oSpecsIterator2);

        }
        catch(CMAConfigurationException e) {
            //e.printStackTrace();
            log.error("Invalid Configuration setup for CMA API", e);
            throw new CampaignDataLoadingException("Invalid configuration setup for CMA API", e);
        }
        catch (RepositoryException e) {
            //e.printStackTrace();
            log.error("Error occured while retrieving Campaign data", e);
            throw new CampaignDataLoadingException("Error occured while retrieving Camapign related objects from repository", e);
        }
        catch(Throwable t) {
            //This exception ensures that the calling client doesnt have to handle any runtime exceptions.
            //especially since the calling client for this class will be a poller which needs a graceful exit point.
            //t.printStackTrace();
            log.error("Unexpected Error occured while loading campaign data", t);            
            throw new CampaignDataLoadingException("Unexpected Error occured while loading campaign data", t);
        }
    }

}
