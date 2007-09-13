package com.tumri.joz.campaign;

import com.tumri.cma.CMAConfigurationException;
import com.tumri.cma.CMAFactory;
import com.tumri.cma.RepositoryException;
import com.tumri.cma.domain.*;
import com.tumri.cma.service.CampaignDeltaProvider;

import com.tumri.utils.Pair;

import java.util.Iterator;

/**
 * CamapignDBDataLoader loads all the campaign related data from the repository using the CampaignDeltaProvider API.
 *
 * @author bpatel
 */
public class CampaignDBDataLoader {
    private static CampaignDBDataLoader dataLoader = new CampaignDBDataLoader();

    private CampaignDBDataLoader() {
    }
    
    public static CampaignDBDataLoader getInstance() {
        return dataLoader;
    }

    @SuppressWarnings({"deprecation"})
    public void loadData() throws CampaignDataLoadingException {
        CampaignDB campaignDB = CampaignDB.getInstance();
        CampaignDeltaProvider deltaProvider;
        //ToDo: The region is hard-coded for now, but this needs to be moved to properties file, from where value should
        //be retrieved
        String region = "USA";

        try {
            CMAFactory factory = CMAFactory.getInstance();
            deltaProvider = factory.getCampaignDeltaProvider();

            Iterator<UrlAdPodMapping>        urlsAdPodMappingIterator      = deltaProvider.getUrlAdpodMappings(region);
            Iterator<ThemeAdPodMapping>      themesAdPodMappingIterator    = deltaProvider.getThemeAdpodMappings(region);
            Iterator<LocationAdPodMapping>   locationsAdPodMappingIterator = deltaProvider.getLocationAdpodMappings(region);
            Iterator<Pair<Integer, Integer>> adPodOSpecMappings            = deltaProvider.getAllAdPodOSpecMappings(region);

            Iterator<Url>      urlsIterator      = deltaProvider.getUrls(region);
            Iterator<Theme>    themesIterator    = deltaProvider.getThemes(region);
            Iterator<Location> locationsIterator = deltaProvider.getLocations(region);
            Iterator<AdPod>    adPodsIterator    = deltaProvider.getAdPods(region);
            Iterator<Geocode>  geocodesIterator  = deltaProvider.getGeocodes(region);
            Iterator<OSpec>    oSpecsIterator    = deltaProvider.getOspecs(region);
            Iterator<Campaign> campaignsIterator = deltaProvider.getCampaigns(region);
            Iterator<AdPod>    runOfNetworkAdPodsIterator = deltaProvider.getNonSiteSpecificAdPods(region);
            Iterator<AdPod>    geoNoneAdPodsIterator      = deltaProvider.getNonGeoSpecificAdPods(region);

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

        }
        catch(CMAConfigurationException e) {
            e.printStackTrace();
            throw new CampaignDataLoadingException("Invalid configuration setup for CMA API", e);
        }
        catch (RepositoryException e) {
            e.printStackTrace();
            throw new CampaignDataLoadingException("Error occured while retrieving Camapign related objects from repository", e);
        }
        catch(Throwable t) {
            //This exception ensures that the calling client doesnt have to handle any runtime exceptions.
            //especially since the calling client for this class will be a poller which needs a graceful exit point.
            t.printStackTrace();
            throw new CampaignDataLoadingException("Unexpected Error occured while loading campaign data", t);
        }
    }

}
