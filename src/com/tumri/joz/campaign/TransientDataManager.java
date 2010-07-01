package com.tumri.joz.campaign;

import com.tumri.cma.domain.*;
import com.tumri.joz.JoZException;
import com.tumri.utils.data.RWLockedTreeMap;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class manages the transient data request from add-tspec, delete-tspec and incorp-mapping-delta requests from
 * the clients.
 * Once the request arrives from the client, this class internally caches the requests, and adds the deleteUrlMappingappropriate objects
 * into the CamapaignDB maps and indices. The reason for caching the requests is that the CampaignDB data gets reloaded
 * periodically and this transient data will get erased in that process. By holding on to request objects, the manager
 * can re-add all these objects during the CampaignDB reloading process. <BR/>
 *
 * This class internally maintains an LRU cache for transient campaign data <BR/>
 * There are two ways by which the data gets removed from the internal cache: <BR/>
 *
 * 1. When delete tspec/delete mapping command is made by portal clients
 * 2. The maximum size of the LRU cache is exceeded.
 *
 * TransientDataManager also caches any campaign adds that are done. TCM will use only the campaign data add/delete, there
 * wont be any mappings supported in the transient data add from TCM - since the only valid use case is for iCS to preview
 * a specific recipe.
 * 
 * @author bpatel
 * @author nipun
 */
@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
public class TransientDataManager {
    private OSpecNameLRUCache oSpecNameLRUCache = new OSpecNameLRUCache(1000);
    private CampaignLRUCache campaignLRUCache = new CampaignLRUCache(1000);
//    private AdPodLRUCache adPodLRUCache = new AdPodLRUCache(1000);
//    private RecipeLRUCache recipeLRUCache = new RecipeLRUCache(1000);
//    private TSpecLRUCache tSpecLRUCache = new TSpecLRUCache(1000);
    
    private static Logger log = Logger.getLogger (TransientDataManager.class);
    //All the request maps below store the tspec-name -> IncorpDeltaMappingRequest mapping.
    private RWLockedTreeMap<String, List<IncorpDeltaMappingRequest<String>>>  urlMapRequest        = new RWLockedTreeMap<String, List<IncorpDeltaMappingRequest<String>>>();
    private RWLockedTreeMap<String, List<IncorpDeltaMappingRequest<String>>>  themeMapRequest      = new RWLockedTreeMap<String, List<IncorpDeltaMappingRequest<String>>>();
    private RWLockedTreeMap<String, List<IncorpDeltaMappingRequest<Integer>>> locationMapRequest   = new RWLockedTreeMap<String, List<IncorpDeltaMappingRequest<Integer>>>();

    private RWLockedTreeMap<String, Integer>       oSpecSiteGeoAdPodMap     = new RWLockedTreeMap<String, Integer>();
    private RWLockedTreeMap<String, Integer>       oSpecSiteNonGeoAdPodMap  = new RWLockedTreeMap<String, Integer>();
    private RWLockedTreeMap<String, Integer>       oSpecSiteNonUrlAdPodMap  = new RWLockedTreeMap<String, Integer>();

    private RWLockedTreeMap<Integer, OSpec> originalOSpecMap = new RWLockedTreeMap<Integer, OSpec>();
    private RWLockedTreeMap<Integer, Campaign> originalCampaignMap = new RWLockedTreeMap<Integer, Campaign>();
//    private RWLockedTreeMap<Integer, Recipe> originalRecipeMap = new RWLockedTreeMap<Integer, Recipe>();
//    private RWLockedTreeMap<Integer, AdPod> originalAdPodMap = new RWLockedTreeMap<Integer, AdPod>();
//    private RWLockedTreeMap<Integer, TSpec> originalTSpecMap = new RWLockedTreeMap<Integer, TSpec>();

    //The low bound is set to a high value so that the IDs assigned to transient objects dont collide with the database generated IDs for other objects
    private final static int lowBound  = 999000000;
    private final static int highBound = 999999999;
    private final static AtomicInteger idSequence = new AtomicInteger(lowBound);
    private static TransientDataManager instance = new TransientDataManager();
    private static CampaignDB campaignDB = CampaignDB.getInstance();

    public static TransientDataManager getInstance() {
        return instance;
    }

    public void reloadInCampaignDB() {
        //Important: Since multiple read locks are acquired in this method, care must be taken in future if there is
        //a need to acquire multiple write locks anywhere else within the class. Acquiring write locks in any other
        //order then below can lead to deadlocks
        urlMapRequest.readerLock();
        themeMapRequest.readerLock();
        locationMapRequest.readerLock();
        try {
            reloadCampaign();
            reloadOSpec();
            reloadUrlMappings();
            reloadLocationMappings();
        }
        finally {
            locationMapRequest.readerUnlock();
            themeMapRequest.readerUnlock();
            urlMapRequest.readerUnlock();
        }
    }

    private void reloadOSpec() {
        Iterator iterator = oSpecNameLRUCache.values().iterator();
        if(iterator != null && iterator.hasNext()) {
            while(iterator.hasNext()) {
                OSpec oSpec = (OSpec)iterator.next();
                addOSpecToCampaignDB(oSpec);
            }
        }
    }

    private void reloadCampaign() {
        Iterator iterator = campaignLRUCache.values().iterator();
        if(iterator != null && iterator.hasNext()) {
            while(iterator.hasNext()) {
                Campaign camp = (Campaign)iterator.next();
                addCampaignToCampaignDB(camp);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private void reloadUrlMappings() {
        Iterator iterator = urlMapRequest.values().iterator();
        if(iterator != null && iterator.hasNext()) {
            while(iterator.hasNext()) {
                List<IncorpDeltaMappingRequest<String>> requestList = (List<IncorpDeltaMappingRequest<String>>)iterator.next();
                if(requestList != null) {
                    for (IncorpDeltaMappingRequest<String> request : requestList) {
                        try {
                            addUrlMapping(request);
                        }
                        catch (TransientDataException e) {
                            log.error("Error occured while reloading urlmapping in TransientDataManager", e);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private void reloadLocationMappings() {
        Iterator iterator = locationMapRequest.values().iterator();
        if(iterator != null && iterator.hasNext()) {
            while(iterator.hasNext()) {
                List<IncorpDeltaMappingRequest<Integer>> requestList = (List<IncorpDeltaMappingRequest<Integer>>)iterator.next();
                if(requestList != null) {
                    for (IncorpDeltaMappingRequest<Integer> request : requestList) {
                        try {
                            addLocationMapping(request);
                        }
                        catch (TransientDataException e) {
                            log.error("Error occured while reloading thememapping in TransientDataManager", e);
                        }
                    }
                }
            }
        }
    }

    public void addOSpec(OSpec oSpec) throws TransientDataException {
        int oSpecId = 0;
        //Check if the oSpec already exists
        if(campaignDB.getOspec(oSpec.getName()) != null && !safeContainsOSpecKey(oSpec.getName())) {
            //Copy the original ospec object from CampaignDB into temporary area, so it can be replaced later on
            //when the delete-tspec from portals is called
            OSpec origOSpec = campaignDB.getOspec(oSpec.getName());
            originalOSpecMap.safePut(origOSpec.getId(), origOSpec);
            oSpecId = origOSpec.getId();
        }
        if(oSpecId == 0) {
            if(safeContainsOSpecKey(oSpec.getName())) {
                oSpecId = safeGetOSpec(oSpec.getName()).getId();
            }
            else {
                //create OSpec ID
                oSpecId = idSequence.incrementAndGet();
            }
            oSpec.setId(oSpecId);
            //Create a new Campaign Object
            Campaign camp = createCampaignAdPod(oSpec);
            addCampaignToCampaignDB(camp);
        }
        oSpec.setId(oSpecId);

        //Add to LRU cache
        safePutOSpec(oSpec.getName(), oSpec);
        addOSpecToCampaignDB(oSpec);

        //reset the idSequence if highBound is exceeded
        if(idSequence.get() >= highBound) {
            idSequence.set(lowBound);
            log.info("ID for TransientDataManager got reset to lower bound");
        }
    }

    private Campaign createCampaignAdPod(OSpec oSpec){
        //Create the campaign and adpod objects here
        Campaign theCampaign = new Campaign();
        theCampaign.setOwnerId("advuser1");
        theCampaign.setRegion("USA");
        theCampaign.setSource("ADVERTISER");

        String tSpecName = oSpec.getName();
        AdPod theAdPod = new AdPod();
        theCampaign.setId(idSequence.incrementAndGet());
        theAdPod.setId(idSequence.incrementAndGet());
        theAdPod.setName(tSpecName);
        theAdPod.setSource(theCampaign.getSource());
        theAdPod.setRegion(theCampaign.getRegion());
        theAdPod.setDisplayName(tSpecName);
        theAdPod.setOwnerId(theCampaign.getOwnerId());
        theAdPod.setOspec(oSpec);
        Recipe aRecipe = new Recipe();
        aRecipe.setName(oSpec.getName());
        aRecipe.setId(idSequence.incrementAndGet());
        List<TSpec> tspecs = oSpec.getTspecs();
        for (TSpec tspec: tspecs) {
            RecipeTSpecInfo tSpecInfo = new RecipeTSpecInfo();
            tSpecInfo.setTspecId(tspec.getId());
            aRecipe.addTSpecInfo(tSpecInfo);
        }
        theAdPod.addRecipe(aRecipe);
        return theCampaign;
    }


    private void addOSpecToCampaignDB(OSpec oSpec) {
        campaignDB.addOSpec(oSpec);
    }

    private void addCampaignToCampaignDB(Campaign camp) {
        campaignDB.addCampaign(camp);
        List<AdPod> adPodList = camp.getAdpods();
        if (adPodList != null) {
            for (AdPod adpod: adPodList) {
                addAdPodToCampaignDB(camp.getId(), adpod);
            }
        }
        List<Experience> expList = camp.getExperiences();
        if (expList!=null) {
            for (Experience exp: expList) {
                campaignDB.addExperience(exp);
            }
        }
    }

    private void addAdPodToCampaignDB(int campId, AdPod adpod) {
        campaignDB.addAdPod(adpod);
        campaignDB.addAdpodCampaignMapping(adpod.getId(), campId);

        //OSpecs
        OSpec campOspec = adpod.getOspec();
        addOSpecToCampaignDB(campOspec);
        //Recipes
        List<Recipe> recipeList = adpod.getRecipes();
        if (recipeList!= null && !recipeList.isEmpty()) {
            for(Recipe r : recipeList) {
                addRecipeToCampaignDB(r);
            }
        }
    }

    private void addRecipeToCampaignDB(Recipe recipe) {
        campaignDB.addRecipe(recipe);
    }

    public void deleteOSpec(String oSpecName) {
        OSpec oSpec = safeGetOSpec(oSpecName);
        if(oSpec != null) {
            deleteDependencies(oSpec);
        }
        else {
            log.error("Trying to delete ospec that doesnt exist in Transient Data Cache. OSpec-Name: " + oSpecName);
        }
    }

    private int createAdPod(OSpec ospec) {
        //Create the campaign and adpod objects here
        Campaign theCampaign = new Campaign();
        AdPod theAdPod = new AdPod();
        theCampaign.setId(idSequence.incrementAndGet());
        int adPodId = idSequence.incrementAndGet();
        theAdPod.setId(adPodId);
        theAdPod.setName("Transient-Incorp-AdPod " + adPodId);
        theAdPod.setSource(theCampaign.getSource());
        theAdPod.setRegion(theCampaign.getRegion());
        theAdPod.setOwnerId(theCampaign.getOwnerId());
        Recipe aRecipe = new Recipe();
        aRecipe.setId(idSequence.incrementAndGet());
        aRecipe.setName(ospec.getName());
        theAdPod.addRecipe(aRecipe);
        campaignDB.addCampaign(theCampaign);
        campaignDB.addAdPod(theAdPod);
        campaignDB.addAdpodCampaignMapping(adPodId, theCampaign.getId());
        campaignDB.addAdpodOSpecMapping(adPodId, ospec.getId());
        return adPodId;
    }

    private String generateKey(String tSpecName, String siteId) {
        return tSpecName + "-" + siteId;        
    }

    /**
     * Adds the url-adpod-mapping
     * Step 1: Check if OSpec exists, if not throw exception
     * Step 2: Check if url exist, if not create one.
     * Step 3: Lookup adpod id and url id for given request
     * Step 4: Add to Url-Adpod-Index within CampaignDB
     *  
     * @param urlName - URL Name
     * @param tSpecName - t-spec name
     * @param weight - weight for mapping
     * @param geocode - geocode
     * @throws TransientDataException - Gets thrown for invalid condition
     */
    public void addUrlMapping(String urlName, String tSpecName, float weight, Geocode geocode) throws TransientDataException {
        if(campaignDB.getOspec(tSpecName) == null || !safeContainsOSpecKey(tSpecName)) {
            throw new TransientDataException("Ospec for this name doesnt Exist");
        }
        IncorpDeltaMappingRequest<String> request = new IncorpDeltaMappingRequest<String>(urlName, tSpecName, weight, geocode, null);
        IncorpDeltaMappingRequest<String> existingRequest = getExistingUrlMapping(request);
        if(existingRequest != null) {
            deleteUrlMapping(existingRequest.getId(), existingRequest.getTSpecName(), 1.0f, existingRequest.getGeocode());
        }
        urlMapRequest.writerLock();
        try {
            List<IncorpDeltaMappingRequest<String>> list = urlMapRequest.get(tSpecName);
            if(list == null) {
                list = new ArrayList<IncorpDeltaMappingRequest<String>>();
            }
            list.add(request);
            urlMapRequest.put(tSpecName, list);
        }
        finally {
            urlMapRequest.writerUnlock();
        }
        addUrlMapping(request);
    }

    private void addUrlMapping(IncorpDeltaMappingRequest<String> request) throws TransientDataException {
        Url url = campaignDB.getUrl(request.getId());
        if(url == null) {
            int urlId = idSequence.incrementAndGet();
            urlId = urlId + lowBound;
            url = new Url();
            url.setId(urlId);
            url.setName(request.getId());
            campaignDB.addUrl(url);
        }
        OSpec ospec = safeGetOSpec(request.getTSpecName());
        int oSpecId = 0;
        if (ospec!= null) {
            oSpecId = ospec.getId();
        }
        if(oSpecId <= 0) {
            throw new TransientDataException("Ospec " + request.getTSpecName()+ "for specified mapping is not present in memeory");
        }
        int adPodId = createAdPod(ospec);
        UrlAdPodMapping mapping = new UrlAdPodMapping();
        mapping.setAdPodId(adPodId);
        mapping.setUrlId(url.getId());
        mapping.setWeight((int)request.getWeight());
        campaignDB.addUrlMapping(mapping);
        String key = generateKey(request.getTSpecName(), request.getId());
        if(request.getGeocode() != null) {
            addGeocodeMapping(request.getGeocode(), adPodId, request.getWeight());
            oSpecSiteGeoAdPodMap.safePut(key, adPodId);
        }
        else {
            addNonGeocodeMapping(adPodId);
            oSpecSiteNonGeoAdPodMap.safePut(key, adPodId);
        }
        //If the adpod is there in non url mapping - remove it
        deleteNonUrlMapping(adPodId);
    }

    private IncorpDeltaMappingRequest<String> getExistingUrlMapping(IncorpDeltaMappingRequest<String> request) {
        if(request == null) {
            return null;
        }
        List <IncorpDeltaMappingRequest<String>>  urlRequestList      = urlMapRequest.safeGet(request.getTSpecName());
        IncorpDeltaMappingRequest<String> result = null;
        if(urlRequestList != null) {
            for (IncorpDeltaMappingRequest<String> urlRequest : urlRequestList) {
                String urlName = urlRequest.getId();
                String tSpecName = urlRequest.getTSpecName();
                Geocode geocode = urlRequest.getGeocode();
                if (urlName.equals(request.getId()) && tSpecName.equals(request.getTSpecName())) {
                    if (request.getGeocode() != null) {
                        if (geocode != null) {
                            result = urlRequest;
                            break;
                        }
                    } else {
                        if (geocode == null) {
                            result = urlRequest;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    private IncorpDeltaMappingRequest<Integer> getExistingLocationMapping(IncorpDeltaMappingRequest<Integer> request) {
        if(request == null) {
            return null;
        }
        List <IncorpDeltaMappingRequest<Integer>>  locationRequestList      = locationMapRequest.safeGet(request.getTSpecName());
        IncorpDeltaMappingRequest<Integer> result = null;
        if(locationRequestList != null) {
            for (IncorpDeltaMappingRequest<Integer> locationRequest : locationRequestList) {
                Integer locationId = locationRequest.getId();
                String tSpecName = locationRequest.getTSpecName();
                Geocode geocode = locationRequest.getGeocode();
                String url = locationRequest.getUrl();
                if (locationId != null && locationId.equals(request.getId()) && tSpecName.equals(request.getTSpecName())) {
                    if (request.getGeocode() != null) {
                        if (geocode != null) {
                            if (geocode != null) {
                                if (url != null && request.getUrl()!= null) {
                                    if (url.equals(request.getUrl()))  {
                                        result = locationRequest;
                                        break;
                                    }
                                } else {
                                    result = locationRequest;
                                    break;
                                }
                            }
                        }
                    } else {
                        if (geocode == null) {
                            if (url != null && request.getUrl()!= null) {
                                if (url.equals(request.getUrl()))  {
                                    result = locationRequest;
                                    break;
                                }
                            } else {
                                result = locationRequest;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public void addLocationMapping(String locationIdStr, String tSpecName, float weight, Geocode geocode, String url) throws TransientDataException {
        if(campaignDB.getOspec(tSpecName) == null || !safeContainsOSpecKey(tSpecName)) {
            throw new TransientDataException("Ospec for this name doesnt Exist");
        }
        int locationId;
        try {
            locationId = Integer.parseInt(locationIdStr);
        }
        catch(NumberFormatException e) {
            log.error("Invalid location ID passed in incorp-mapping-delta request");
            throw new TransientDataException("Invalid location ID passed in incorp-mapping-delta request");
        }
        IncorpDeltaMappingRequest<Integer> request = new IncorpDeltaMappingRequest<Integer>(locationId, tSpecName, weight, geocode, url);
        IncorpDeltaMappingRequest<Integer> existingRequest = getExistingLocationMapping(request);
        if(existingRequest != null) {
            deleteLocationMapping(existingRequest.getId() +"", existingRequest.getTSpecName(), 1.0f, existingRequest.getGeocode(), url);
        }
        locationMapRequest.writerLock();
        try {
            List<IncorpDeltaMappingRequest<Integer>> list = locationMapRequest.get(tSpecName);
            if(list == null) {
                list = new ArrayList<IncorpDeltaMappingRequest<Integer>>();
            }
            list.add(request);
            locationMapRequest.put(tSpecName, list);
        }
        finally {
            locationMapRequest.writerUnlock();
        }
        addLocationMapping(request);
    }

    private void addLocationMapping(IncorpDeltaMappingRequest<Integer> request) throws TransientDataException {
        Location location = campaignDB.getLocation(request.getId());
        if(location == null) {
            location = new Location();
            location.setId(request.getId());
            campaignDB.addLocation(location);
        }
        int oSpecId = safeGetOSpec(request.getTSpecName()).getId();
        if(oSpecId <= 0) {
            throw new TransientDataException("Ospec " + request.getTSpecName()+ "for specified mapping is not present in memeory");
        }
        int adPodId = createAdPod(safeGetOSpec(request.getTSpecName()));
        LocationAdPodMapping mapping = new LocationAdPodMapping();
        mapping.setAdPodId(adPodId);
        mapping.setLocationId(location.getId());
        campaignDB.addLocationMapping(mapping);
        String key = generateKey(request.getTSpecName(), request.getId() + "");
        if(request.getGeocode() != null) {
            addGeocodeMapping(request.getGeocode(), adPodId, request.getWeight());
            oSpecSiteGeoAdPodMap.safePut(key, adPodId);
        }
        else {
            addNonGeocodeMapping(adPodId);
            oSpecSiteNonGeoAdPodMap.safePut(key, adPodId);
        }
        if (request.getUrl()==null) {
            oSpecSiteNonUrlAdPodMap.safePut(key, adPodId);
            addNonUrlcodeMapping(adPodId);
        }
    }

    public void addGeocodeMapping(Geocode geocode, int adPodId, float weight) throws TransientDataException {
        geocode.setId(idSequence.incrementAndGet());
        campaignDB.addGeocode(geocode);
        campaignDB.addGeocodeMapping(geocode, adPodId, weight);
    }

    public void addNonGeocodeMapping(int adPodId) throws TransientDataException {
        campaignDB.addNonGeoAdPod(adPodId);
    }

    public void addNonUrlcodeMapping(int adPodId) throws TransientDataException {
        campaignDB.addNonUrlAdPod(adPodId);
    }

    public void deleteUrlMapping(String urlName, String tSpecName, float weight, Geocode geocode) {
        List <IncorpDeltaMappingRequest<String>>  urlRequestList      = urlMapRequest.safeGet(tSpecName);
        if(urlRequestList != null && safeContainsOSpecKey(tSpecName)) {
            synchronized(urlRequestList) {
                int adPodId = 0;
                for(int i=0; i<urlRequestList.size(); i++) {
                    IncorpDeltaMappingRequest<String> urlRequest = urlRequestList.get(i);
                    if(urlName.equals(urlRequest.getId())) {
                        if(urlRequest.getGeocode() != null && geocode != null) {
                            adPodId = oSpecSiteGeoAdPodMap.safeGet(generateKey(tSpecName, urlName));
                            deleteGeocodeMapping(urlRequest.getGeocode(), adPodId);
                            oSpecSiteGeoAdPodMap.safeRemove(generateKey(tSpecName, urlName));
                            urlRequestList.remove(i);
                            break;
                        }
                        else if(urlRequest.getGeocode() == null && geocode == null) {
                            adPodId = oSpecSiteNonGeoAdPodMap.safeGet(generateKey(tSpecName, urlName));
                            deleteNonGeocodeMapping(adPodId);
                            oSpecSiteNonGeoAdPodMap.safeRemove(generateKey(tSpecName, urlName));
                            urlRequestList.remove(i);
                            break;
                        }
                    }
                }
                if(urlName != null && adPodId > 0) {
                    campaignDB.deleteUrlMapping(urlName, adPodId);
                    //Note: Url is not deleted from campaignDB, instead the unused url will get removed during the campaign data refresh
                    //In future, if we do decide to delete url then we will have to keep track of all the related mappings and selectively
                    //delete url if there are no more mappings to it and it was created as a part of incorp-delta request
                    //campaignDB.deleteUrl(url.getName());
                }
            }
        }
    }

    public void deleteLocationMapping(String locationIdStr, String tSpecName, float weight, Geocode geocode, String url) {
        List <IncorpDeltaMappingRequest<Integer>>  locationRequestList      = locationMapRequest.safeGet(tSpecName);
        if(locationRequestList != null) {
            int locationId;
            try {
                locationId = Integer.parseInt(locationIdStr);
            }
            catch(NumberFormatException e) {
                log.error("Invalid location ID passed in incorp-mapping-delta request");
                return;
            }
            synchronized(locationRequestList) {
                int adPodId = 0;
                for(int i=0; i<locationRequestList.size(); i++) {
                    IncorpDeltaMappingRequest<Integer> locationRequest = locationRequestList.get(i);
                    if(locationId == locationRequest.getId()) {
                        String key = generateKey(tSpecName, locationIdStr);
                        if(locationRequest.getGeocode() != null && geocode != null) {
                            adPodId = oSpecSiteGeoAdPodMap.safeGet(key);
                            if (locationRequest.getUrl() != null && url != null) {
                                oSpecSiteNonUrlAdPodMap.safeRemove(key);
                                deleteNonUrlMapping(adPodId);
                            }
                            deleteGeocodeMapping(locationRequest.getGeocode(), adPodId);
                            oSpecSiteGeoAdPodMap.safeRemove(key);
                            locationRequestList.remove(i);
                            break;
                        }
                        else if(locationRequest.getGeocode() == null && geocode == null) {
                            adPodId = oSpecSiteNonGeoAdPodMap.safeGet(key);
                            if (locationRequest.getUrl() != null && url != null) {
                                oSpecSiteNonUrlAdPodMap.safeRemove(key);
                                deleteNonUrlMapping(adPodId);
                            }
                            deleteNonGeocodeMapping(adPodId);
                            oSpecSiteNonGeoAdPodMap.safeRemove(key);
                            locationRequestList.remove(i);
                            break;
                        }
                    }
                }
                if(locationId > 0 && adPodId > 0) {
                    campaignDB.deleteLocationMapping(locationId, adPodId);
                    //Note: Location is not deleted from campaignDB, instead the unused location will get removed during the campaign data refresh
                    //In future, if we do decide to delete location then we will have to keep track of all the related mappings and selectively
                    //delete location if there are no more mappings to it and it was created as a part of incorp-delta request                    
                    //campaignDB.deleteLocation(location.getId());
                }
            }
        }
    }

    public void deleteGeocodeMapping(Geocode geocode, int adPodId) {
        campaignDB.deleteGeocodeMapping(geocode, adPodId);
    }

    public void deleteNonGeocodeMapping(int adPodId) {
        campaignDB.deleteNonGeoAdPod(adPodId);
    }

    public void deleteNonUrlMapping(int adPodId) {
        campaignDB.deleteNonUrlAdPod(adPodId);
    }

    /**
     * Adds a campaign with a valid Id into the db.
     * @param campaign
     */
    public void addCampaign(Campaign campaign) throws JoZException {
        int campaignId = campaign.getId();
        if (campaignId == 0) {
            throw new JoZException("Campaign with invalid Id being added - aborting");
        }

        //Check if the oSpec already exists
        if(campaignDB.getCampaign(campaignId) != null && !safeContainsCampaignKey(campaignId)) {
            //Copy the original campaign object from CampaignDB into temporary area, so it can be replaced later on
            //when the delete-campaign from portals is called
            Campaign origCamp = campaignDB.getCampaign(campaignId);
            originalCampaignMap.safePut(campaignId, origCamp);
        }
        //Add to LRU cache
        safePutCampaign(campaign.getId(), campaign);
        addCampaignToCampaignDB(campaign);
    }

    public void deleteCampaign(int campaignId) throws JoZException {
        Campaign camp = safeGetCampaign(campaignId);
        if(camp != null) {
            deleteDependencies(camp);
            campaignDB.delCampaign(campaignId);
            safeRemoveCampaign(campaignId);
        }
        else {
            log.error("Trying to delete camp that doesnt exist in Transient Data Cache. Campaign ID: " + campaignId);
            throw new JoZException("Trying to delete camp that doesnt exist in Transient Data Cache. Campaign ID: " + campaignId);
        }
    }

//    /**
//     * Add the adpod into the campaign db - provided the parent (campiagn id ) exists
//     * @param campaignId
//     * @param adPod
//     */
//    public void addAdPod(int campaignId, AdPod adPod) {
//        if (campaignId == 0 || campaignDB.getCampaign(campaignId) == null) {
//            log.error("Adpod add failed - the parent Campaign does not exist - aborting");
//            return;
//        }
//
//        //Check if the AdPod already exists
//        int adPodId = adPod.getId();
//        if (adPodId == 0) {
//            log.error("Adpod add failed - the id is invalid - aborting");
//            return;
//        }
//
//        if(campaignDB.getAdPod(adPodId) != null && !safeContainsAdPodKey(adPodId)) {
//            //Copy the original adpod object from CampaignDB into temporary area, so it can be replaced later on
//            //when the delete-adpod from portals is called
//            AdPod origAdPod = campaignDB.getAdPod(adPodId);
//            originalAdPodMap.safePut(adPodId, origAdPod);
//        }
//        //Add to LRU cache
//        safePutAdPod(adPodId, adPod);
//        addAdPodToCampaignDB(campaignId, adPod);
//
//    }
//
//    public void deleteAdPod(int apodId) {
//        AdPod adPod = safeGetAdPod(apodId);
//        if(adPod != null) {
//            deleteDependencies(adPod);
//        }
//        else {
//            log.error("Trying to delete AdPod that doesnt exist in Transient Data Cache. AdPod ID: " + apodId);
//        }
//    }
//
//    /**
//     * Add a recipe to the campaign db if the adpod is exists
//     * @param adpodId
//     * @param recipe
//     */
//    public void addRecipe(int adpodId, Recipe recipe) {
//        if (adpodId == 0 || campaignDB.getAdPod(adpodId) == null) {
//            log.error("Recipe add failed - the parent AdPod does not exist - aborting");
//            return;
//        }
//
//        //Check if the Recipe already exists
//        int recipeId = recipe.getId();
//        if (recipeId == 0) {
//            log.error("Recipe add failed - the id is invalid - aborting");
//            return;
//        }
//
//        if(campaignDB.getRecipe(recipeId) != null && !safeContainsRecipeKey(recipeId)) {
//            //Copy the original recipe object from CampaignDB into temporary area, so it can be replaced later on
//            //when the delete-recipe from portals is called
//            Recipe origRecipe = campaignDB.getRecipe(recipeId);
//            originalRecipeMap.safePut(recipeId, origRecipe);
//        }
//        //Add to LRU cache
//        safePutRecipe(recipeId, recipe);
//        addRecipeToCampaignDB(recipe);
//
//    }
//
//    public void delRecipe(int adpodid, int recipeId) {
//        Recipe recipe = safeGetRecipe(recipeId);
//        if(recipe != null) {
//            deleteDependencies(adpodid, recipe);
//        }
//        else {
//            log.error("Trying to delete Recipe that doesnt exist in Transient Data Cache. Recipe ID: " + recipeId);
//        }
//    }
//
//    /**
//     * Add the tspec into the db if the ospec exists.
//     * @param ospecId
//     * @param tspec
//     */
//    public void addTSpec(int ospecId , TSpec tspec) {
//        if (ospecId == 0 || campaignDB.getOspec(ospecId) == null) {
//            log.error("TSpec add failed - the parent OSpec does not exist - aborting");
//            return;
//        }
//
//        //Check if the TSpec already exists
//        int tspecId = tspec.getId();
//        if (tspecId == 0) {
//            log.error("TSpec add failed - the id is invalid - aborting");
//            return;
//        }
//
//        if(campaignDB.getTspec(tspecId) != null && !safeContainsTSpecKey(tspecId)) {
//            //Copy the original tspec object from CampaignDB into temporary area, so it can be replaced later on
//            //when the delete-tspec from portals is called
//            TSpec origTSpec = campaignDB.getTspec(tspecId);
//            originalTSpecMap.safePut(tspecId, origTSpec);
//        }
//        //Add to LRU cache
//        safePutTSpec(tspecId, tspec);
//        addTSpecToCampaignDB(tspec);
//    }
//
//    public void delTSpec(int tspecId) {
//        TSpec tSpec = safeGetTSpec(tspecId);
//        if(tSpec != null) {
//            deleteDependencies(tSpec);
//        }
//        else {
//            log.error("Trying to delete Tspec that doesnt exist in Transient Data Cache. Tspec ID: " + tspecId);
//        }
//    }
//
//    public void addUrlMapping(UrlCampaignMapping urlCampMapping) throws TransientDataException {
//        int campId = urlCampMapping.getCampaignId();
//        Campaign camp = campaignDB.getCampaign(campId);
//        if (camp != null) {
//            List<AdPod> adPodList = camp.getAdGroups().get(0).getAdPods();
//            if (adPodList != null && !adPodList.isEmpty()) {
//                AtomicAdpodIndex index = campaignDB.getUrlAdPodMappingIndex();
//                Url urlObj = campaignDB.getUrl(urlCampMapping.getUrlId());
//                if (urlObj != null) {
//                    SortedSet<Handle> results = index.get(urlObj.getName());
//                    ArrayList<Integer> adPodIds = new ArrayList<Integer>();
//                    for(Handle adPod : results) {
//                       adPodIds.add((int)adPod.getOid());
//                    }
//                    for(AdPod adPod : adPodList) {
//                        if (!adPodList.contains(adPod.getId())) {
//                            UrlAdPodMapping urlAdpodMapping = new UrlAdPodMapping();
//                            urlAdpodMapping.setAdPodId(adPod.getId());
//                            urlAdpodMapping.setUrlId(urlCampMapping.getId());
//                            campaignDB.addUrlMapping(urlAdpodMapping);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    public void addUrlMapping(UrlAdPodMapping urlMapping) throws TransientDataException {
//        campaignDB.addUrlMapping(urlMapping);
//    }
//
//    public void deleteUrlMapping(int adPodid, String urlName) throws TransientDataException{
//        campaignDB.deleteUrlMapping(urlName, adPodid);
//    }
//
//    public void addThemeMapping(ThemeAdPodMapping themeMapping) throws TransientDataException {
//        campaignDB.addThemeMapping(themeMapping);
//    }
//
//    public void deleteThemeMapping(int adPodid, String themeName) throws TransientDataException{
//        campaignDB.deleteThemeMapping(themeName, adPodid);
//    }
//
//    public void addLocationMapping(LocationAdPodMapping locMapping) throws TransientDataException {
//        campaignDB.addLocationMapping(locMapping);
//    }
//
//    public void deleteThemeMapping(int adPodid, int locationId) throws TransientDataException {
//        campaignDB.deleteLocationMapping(locationId, adPodid);
//    }
//
//    public void addGeoMapping(GeoAdPodMapping geoMapping) throws TransientDataException {
//        //Create the Geocode object
//        String type = geoMapping.getType();
//        Geocode geoObj = new Geocode();
//        if (type.equals(GeoAdPodMapping.TYPE_COUNTRY)) {
//            geoObj.setCountries(geoMapping.getGeoValue());
//        } else if (type.equals(GeoAdPodMapping.TYPE_STATE)){
//            geoObj.setStates(geoMapping.getGeoValue());
//        } else if (type.equals(GeoAdPodMapping.TYPE_CITY)){
//            geoObj.setCities(geoMapping.getGeoValue());
//        } else if (type.equals(GeoAdPodMapping.TYPE_ZIP)){
//            geoObj.setZipcodes(geoMapping.getGeoValue());
//        } else if (type.equals(GeoAdPodMapping.TYPE_AREA)){
//            geoObj.setAreaCodes(geoMapping.getGeoValue());
//        } else if (type.equals(GeoAdPodMapping.TYPE_DMA)){
//            geoObj.setDmaCodes(geoMapping.getGeoValue());
//        }
//        campaignDB.addGeocodeMapping(geoObj, geoMapping.getAdPodId(), 0.0f);
//    }
//
//    public void addGeoMapping(Geocode geoObj, int adPodId) throws TransientDataException {
//        campaignDB.addGeocodeMapping(geoObj, adPodId, 0.0f);
//    }
//
//    public void deleteGeocodeMapping(int adPodid, Geocode geoObj) throws TransientDataException {
//        campaignDB.deleteGeocodeMapping(geoObj, adPodid);
//    }

    /**
     * Delete the campaign and all its dependencies
     * @param camp
     */
    private void deleteDependencies(Campaign camp) {
        if (camp != null) {
            List<Experience> expList = camp.getExperiences();
            if (expList!=null) {
                for (Experience exp: expList) {
                    campaignDB.delExperience(exp.getId());
                }
            }
            List<AdPod> adPodList = camp.getAdpods();
            if (adPodList != null && !adPodList.isEmpty()) {
                for (AdPod adpod: adPodList) {
                    OSpec oSpec = adpod.getOspec();
                    if (oSpec != null) {
                        deleteDependencies(oSpec);
                    }
                    List<Recipe> recipes = adpod.getRecipes();
                    if (recipes!= null) {
                        for (Recipe r: recipes) {
                            campaignDB.delRecipe(r.getId());
                        }
                    }
                    campaignDB.deleteAdPod(adpod.getId());
                }
            }
        }
    }
    
    private void deleteDependencies(AdPod adpod) {
        OSpec oSpec = adpod.getOspec();
        if (oSpec != null) {
            deleteDependencies(oSpec);
        }
        List<Recipe> recipes = adpod.getRecipes();
        if (recipes!= null) {
            for (Recipe r: recipes) {
                campaignDB.delRecipe(r.getId());
            }
        }
        campaignDB.deleteAdPod(adpod.getId());
    }

    private void deleteDependencies(int adpodid, Recipe recipe) {
        AdPod theAdpod = campaignDB.getAdPod(adpodid);
        if (theAdpod != null) {
            List<Recipe> recipes = theAdpod.getRecipes();
            for(Recipe r: recipes) {
                if (r.getId() == recipe.getId()) {
                    recipes.remove(r);
                    campaignDB.addAdPod(theAdpod);
                    break;
                }
            }
        }
        campaignDB.delRecipe(recipe.getId());
    }

    private void deleteDependencies(TSpec tspec) {
        campaignDB.delTSpec(tspec.getId());
    }

    @SuppressWarnings({"deprecation"})
    private void deleteDependencies(OSpec oSpec) {
        List <IncorpDeltaMappingRequest<String>>  urlRequestList      = urlMapRequest.safeGet(oSpec.getName());
        List <IncorpDeltaMappingRequest<String>>  themeRequestList    = themeMapRequest.safeGet(oSpec.getName());
        List <IncorpDeltaMappingRequest<Integer>> locationRequestList = locationMapRequest.safeGet(oSpec.getName());

        safeRemoveOSpec(oSpec.getName());
        urlMapRequest.safeRemove(oSpec.getName());
        themeMapRequest.safeRemove(oSpec.getName());
        locationMapRequest.safeRemove(oSpec.getName());

        if(urlRequestList != null) {
            for (IncorpDeltaMappingRequest<String> urlRequest : urlRequestList) {
                String urlName = urlRequest.getId();
                Url url = campaignDB.getUrl(urlName);
                if (url != null) {
                    campaignDB.getUrlAdPodMappingIndex().remove(UrlNormalizer.getNormalizedUrl(url.getName()));
                    campaignDB.deleteUrl(url.getName());
                }
                if(urlRequest.getGeocode() != null) {
                    Integer adPodId;
                    adPodId = oSpecSiteGeoAdPodMap.safeGet(generateKey(oSpec.getName(), urlName));
                    if(adPodId != null) {
                        deleteGeocodeMapping(urlRequest.getGeocode(), adPodId);
                        oSpecSiteGeoAdPodMap.safeRemove(generateKey(oSpec.getName(), urlName));
                    }
                }
                else {
                    Integer adPodId;
                    adPodId = oSpecSiteNonGeoAdPodMap.safeGet(generateKey(oSpec.getName(), urlName));
                    if(adPodId != null) {
                        deleteNonGeocodeMapping(adPodId);
                        oSpecSiteNonGeoAdPodMap.safeRemove(generateKey(oSpec.getName(), urlName));
                    }
                }
            }
        }

        if(locationRequestList != null) {
            for (IncorpDeltaMappingRequest<Integer> locationRequest : locationRequestList) {
                int locationId = locationRequest.getId();
                Location location = campaignDB.getLocation(locationId);
                if (location != null) {
                    campaignDB.getLocationAdPodMappingIndex().remove(location.getId());
                    campaignDB.deleteLocation(location.getId());
                }
                if(locationRequest.getGeocode() != null) {
                    Integer adPodId;
                    adPodId = oSpecSiteGeoAdPodMap.safeGet(generateKey(oSpec.getName(), locationId + ""));
                    if(adPodId != null) {
                        deleteGeocodeMapping(locationRequest.getGeocode(), adPodId);
                        oSpecSiteGeoAdPodMap.safeRemove(generateKey(oSpec.getName(), locationId + ""));
                    }
                }
                else {
                    Integer adPodId;
                    adPodId = oSpecSiteNonGeoAdPodMap.safeGet(generateKey(oSpec.getName(), locationId + ""));
                    if(adPodId != null) {
                        deleteNonGeocodeMapping(adPodId);
                        oSpecSiteNonGeoAdPodMap.safeRemove(generateKey(oSpec.getName(), locationId + ""));
                    }
                }
            }
        }      
        if(originalOSpecMap.containsKey(oSpec.getId())) {
            campaignDB.deleteOSpec(oSpec.getName());
            campaignDB.addOSpec(originalOSpecMap.safeGet(oSpec.getId()));
            originalOSpecMap.safeRemove(oSpec.getId());
        }
        else {
            campaignDB.deleteOSpec(oSpec.getName());
        }
    }

    public boolean safeContainsOSpecKey(Object key) {
        synchronized (oSpecNameLRUCache) {
            return (oSpecNameLRUCache.containsKey(key));
        }
    }

    public OSpec safeGetOSpec(Object key) {
        synchronized (oSpecNameLRUCache) {
            return oSpecNameLRUCache.get(key);
        }
    }

    public OSpec safePutOSpec(String key, OSpec value) {
        synchronized (oSpecNameLRUCache) {
            return oSpecNameLRUCache.put(key, value);
        }
    }

    public void safePutAllOSpec(Map<String, OSpec> map) {
        synchronized (oSpecNameLRUCache) {
            oSpecNameLRUCache.putAll(map);
        }
    }

    public OSpec safeRemoveOSpec(String key) {
        synchronized (oSpecNameLRUCache) {
            return oSpecNameLRUCache.remove(key);
        }
    }

    public boolean safeContainsCampaignKey(Object key) {
        synchronized (campaignLRUCache) {
            return (campaignLRUCache.containsKey(key));
        }
    }

    public Campaign safeGetCampaign(Object key) {
        synchronized (campaignLRUCache) {
            return campaignLRUCache.get(key);
        }
    }

    public Campaign safePutCampaign(Integer key, Campaign value) {
        synchronized (campaignLRUCache) {
            return campaignLRUCache.put(key, value);
        }
    }

    public void safePutAllCampaign(Map<Integer, Campaign> map) {
        synchronized (campaignLRUCache) {
            campaignLRUCache.putAll(map);
        }
    }

    public Campaign safeRemoveCampaign(Integer key) {
        synchronized (campaignLRUCache) {
            return campaignLRUCache.remove(key);
        }
    }
    public List<Campaign> getCampaigns(){
    	return campaignDB.getCampaigns();
    }

//    public boolean safeContainsAdPodKey(Object key) {
//        synchronized (adPodLRUCache) {
//            return (adPodLRUCache.containsKey(key));
//        }
//    }
//
//    public AdPod safeGetAdPod(Object key) {
//        synchronized (adPodLRUCache) {
//            return adPodLRUCache.get(key);
//        }
//    }
//
//    public AdPod safePutAdPod(Integer key, AdPod value) {
//        synchronized (adPodLRUCache) {
//            return adPodLRUCache.put(key, value);
//        }
//    }
//
//    public void safePutAllAdPod(Map<Integer, AdPod> map) {
//        synchronized (adPodLRUCache) {
//            adPodLRUCache.putAll(map);
//        }
//    }
//
//    public AdPod safeRemoveAdPod(Integer key) {
//        synchronized (adPodLRUCache) {
//            return adPodLRUCache.remove(key);
//        }
//    }
//
//    public boolean safeContainsRecipeKey(Object key) {
//        synchronized (recipeLRUCache) {
//            return (recipeLRUCache.containsKey(key));
//        }
//    }
//
//    public Recipe safeGetRecipe(Object key) {
//        synchronized (recipeLRUCache) {
//            return recipeLRUCache.get(key);
//        }
//    }
//
//    public Recipe safePutRecipe(Integer key, Recipe value) {
//        synchronized (recipeLRUCache) {
//            return recipeLRUCache.put(key, value);
//        }
//    }
//
//    public void safePutAllRecipe(Map<Integer, Recipe> map) {
//        synchronized (recipeLRUCache) {
//            recipeLRUCache.putAll(map);
//        }
//    }
//
//    public Recipe safeRemoveRecipe(Integer key) {
//        synchronized (recipeLRUCache) {
//            return recipeLRUCache.remove(key);
//        }
//    }
//
//    public boolean safeContainsTSpecKey(Object key) {
//        synchronized (tSpecLRUCache) {
//            return (tSpecLRUCache.containsKey(key));
//        }
//    }
//
//    public TSpec safeGetTSpec(Object key) {
//        synchronized (tSpecLRUCache) {
//            return tSpecLRUCache.get(key);
//        }
//    }
//
//    public TSpec safePutTSpec(Integer key, TSpec value) {
//        synchronized (tSpecLRUCache) {
//            return tSpecLRUCache.put(key, value);
//        }
//    }
//
//    public void safePutAllTSpec(Map<Integer, TSpec> map) {
//        synchronized (tSpecLRUCache) {
//            tSpecLRUCache.putAll(map);
//        }
//    }
//
//    public TSpec safeRemoveTSpec(Integer key) {
//        synchronized (tSpecLRUCache) {
//            return tSpecLRUCache.remove(key);
//        }
//    }

    class OSpecNameLRUCache extends LinkedHashMap<String, OSpec>{
        private int cacheSize;
        OSpecNameLRUCache(int cacheSize) {
            super(cacheSize, 0.75f, true);
            this.cacheSize = cacheSize;
        }

        protected boolean removeEldestEntry(Map.Entry<String, OSpec> eldest) {
            boolean deleteLastEntry = (size() > cacheSize);
            if(deleteLastEntry) {
                deleteDependencies(eldest.getValue());
            }
            return false;
        }

    }

    class CampaignLRUCache extends LinkedHashMap<Integer, Campaign>{
        private int cacheSize;
        CampaignLRUCache(int cacheSize) {
            super(cacheSize, 0.75f, true);
            this.cacheSize = cacheSize;
        }

        protected boolean removeEldestEntry(Map.Entry<Integer, Campaign> eldest) {
            boolean deleteLastEntry = (size() > cacheSize);
            if(deleteLastEntry) {
                deleteDependencies(eldest.getValue());
            }
            return false;
        }

    }

//    class RecipeLRUCache extends LinkedHashMap<Integer, Recipe>{
//        private int cacheSize;
//        RecipeLRUCache(int cacheSize) {
//            super(cacheSize, 0.75f, true);
//            this.cacheSize = cacheSize;
//        }
//
//        protected boolean removeEldestEntry(Map.Entry<Integer, Recipe> eldest) {
//            return (size() > cacheSize);
//        }
//
//    }
//
//    class TSpecLRUCache extends LinkedHashMap<Integer, TSpec>{
//        private int cacheSize;
//        TSpecLRUCache(int cacheSize) {
//            super(cacheSize, 0.75f, true);
//            this.cacheSize = cacheSize;
//        }
//
//        protected boolean removeEldestEntry(Map.Entry<Integer, TSpec> eldest) {
//            return (size() > cacheSize);
//        }
//
//    }
//
//    class AdPodLRUCache extends LinkedHashMap<Integer, AdPod>{
//        private int cacheSize;
//        AdPodLRUCache(int cacheSize) {
//            super(cacheSize, 0.75f, true);
//            this.cacheSize = cacheSize;
//        }
//
//        protected boolean removeEldestEntry(Map.Entry<Integer, AdPod> eldest) {
//            boolean deleteLastEntry = (size() > cacheSize);
//            if(deleteLastEntry) {
//                deleteDependencies(eldest.getValue());
//            }
//            return false;
//        }
//
//    }

    class IncorpDeltaMappingRequest<Key> {
        private Key id;
        private String tSpecName;
        private float weight;
        private Geocode geocode;
        private String url;

        public IncorpDeltaMappingRequest(Key id, String tSpecName, float weight, Geocode geocode, String url) {
            this.id        = id;
            this.tSpecName = tSpecName;
            this.weight    = weight;
            this.geocode   = geocode;
            this.url = url;
        }

        public Key getId() {
            return id;
        }

        public String getTSpecName() {
            return tSpecName;
        }

        public float getWeight() {
            return weight;
        }

        public Geocode getGeocode() {
            return geocode;
        }

        public String getUrl() {
            return url;
        }
    }
}
