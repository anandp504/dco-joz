package com.tumri.joz.campaign;

import com.tumri.cma.domain.*;
import com.tumri.utils.data.RWLockedTreeMap;
import com.tumri.utils.data.RWLocked;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

/**
 * This class manages the transient data request from add-tspec, delete-tspec and incorp-mapping-delta requests from
 * the clients.
 * Once the request arrives from the client, this class internally caches the requests, and adds the appropriate objects
 * into the CamapaignDB maps and indices. The reason for caching the requests is that the CampaignDB data gets reloaded
 * periodically and this transient data will get erased in that process. By holding on to request objects, the manager
 * can re-add all these objects during the CampaignDB reloading process.
 *  
 * It internally maintains an LRU cache for transient campaign data <BR/>
 * There are two ways by which the data gets removed from the cache: <BR/>
 *
 * 1. When delete tspec/delete mapping command is made by portal clients
 *
 * 2. The maximum size of the LRU cache is exceeded.
 *
 * @author bpatel
 */
@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
public class TransientDataManager {
    private OSpecNameLRUCache oSpecNameLRUCache = new OSpecNameLRUCache(1000);
    private static Logger log = Logger.getLogger (TransientDataManager.class);
    //All the request maps below store the tspec-name -> IncorpDeltaMappingRequest mapping.
    private RWLockedTreeMap<String, List<IncorpDeltaMappingRequest<String>>>  urlMapRequest        = new RWLockedTreeMap<String, List<IncorpDeltaMappingRequest<String>>>();
    private RWLockedTreeMap<String, List<IncorpDeltaMappingRequest<String>>>  themeMapRequest      = new RWLockedTreeMap<String, List<IncorpDeltaMappingRequest<String>>>();
    private RWLockedTreeMap<String, List<IncorpDeltaMappingRequest<Integer>>> locationMapRequest   = new RWLockedTreeMap<String, List<IncorpDeltaMappingRequest<Integer>>>();

    private RWLockedTreeMap<String, Integer>       oSpecSiteGeoAdPodMap     = new RWLockedTreeMap<String, Integer>();
    private RWLockedTreeMap<String, Integer>       oSpecSiteNonGeoAdPodMap  = new RWLockedTreeMap<String, Integer>();

    private RWLockedTreeMap<Integer, OSpec> originalOSpecMap = new RWLockedTreeMap<Integer, OSpec>();

    //This low bound is set this high so that the IDs assigned to transient objects doesnt collide with the database generated IDs for other objects
    private final static int lowBound  = 999000000;
    private final static int highBound = 999999999;
    private final static AtomicInteger idSequence = new AtomicInteger(lowBound);
    private static TransientDataManager instance = new TransientDataManager();
    private static CampaignDB campaignDB = CampaignDB.getInstance();


    public static TransientDataManager getInstance() {
        return instance;
    }


    public void reloadInCampaignDB() {
        reloadOSpec();
        reloadUrlMappings();
        reloadThemeMappings();
        reloadLocationMappings();
    }

    private void reloadOSpec() {
        oSpecNameLRUCache.readerLock();
        try {
            Iterator iterator = oSpecNameLRUCache.values().iterator();
            if(iterator != null && iterator.hasNext()) {
                while(iterator.hasNext()) {
                    OSpec oSpec = (OSpec)iterator.next();
                    addOSpecToCampaignDB(oSpec);
                }
            }
        }
        finally {
            oSpecNameLRUCache.readerUnlock();
        }
    }

    @SuppressWarnings({"unchecked"})
    private void reloadUrlMappings() {
        urlMapRequest.readerLock();
        try {
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
        finally {
            urlMapRequest.readerUnlock();
        }
    }

    @SuppressWarnings({"unchecked"})
    private void reloadThemeMappings() {
        themeMapRequest.readerLock();
        try {
            Iterator iterator = themeMapRequest.values().iterator();
            if(iterator != null && iterator.hasNext()) {
                while(iterator.hasNext()) {
                    List<IncorpDeltaMappingRequest<String>> requestList = (List<IncorpDeltaMappingRequest<String>>)iterator.next();
                    if(requestList != null) {
                        for (IncorpDeltaMappingRequest<String> request : requestList) {
                            try {
                                addThemeMapping(request);
                            }
                            catch (TransientDataException e) {
                                log.error("Error occured while reloading thememapping in TransientDataManager", e);
                            }
                        }
                    }
                }
            }
        }
        finally {
            themeMapRequest.readerUnlock();
        }

    }

    @SuppressWarnings({"unchecked"})
    private void reloadLocationMappings() {
        locationMapRequest.readerLock();
        try {
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
        finally {
            locationMapRequest.readerUnlock();
        }

    }

    public void addOSpec(OSpec oSpec) throws TransientDataException {
        //Check if the oSpec already exists
        if(campaignDB.getOspec(oSpec.getName()) != null && !oSpecNameLRUCache.containsKey(oSpec.getName())) {
            //copy the original ospec object to temporary area, for being replaced later on once the delete-tspec is called
            OSpec origOSpec = campaignDB.getOspec(oSpec.getName());
            originalOSpecMap.safePut(origOSpec.getId(), origOSpec);
        }
        int oSpecId;
        if(oSpecNameLRUCache.containsKey(oSpec.getName())) {
            oSpecId = oSpecNameLRUCache.get(oSpec.getName()).getId();
        }
        else {
            //create OSpec ID
            oSpecId = idSequence.incrementAndGet();
        }
        oSpec.setId(oSpecId);


        //Add to LRU cache
        oSpecNameLRUCache.put(oSpec.getName(), oSpec);
        addOSpecToCampaignDB(oSpec);
        //7. Add to local Ospec-Adpod-Map for back reference
        //oSpecAdPodMap.safePut(oSpecId, oSpecId);

        //reset the idSequence if highBound is exceeded
        if(idSequence.get() >= highBound) {
            idSequence.set(lowBound);
            log.info("ID for TransientDataManager got reset to lower bound");
        }
    }

    private void addOSpecToCampaignDB(OSpec oSpec) {
        campaignDB.addOSpec(oSpec);
    }

    public void deleteOSpec(String oSpecName) {
        OSpec oSpec = oSpecNameLRUCache.get(oSpecName);
        if(oSpec != null) {
            deleteDependencies(oSpec);
        }
        else {
            log.error("Trying to delete ospec that doesnt exist in Transient Data Cache. OSpec-Name: " + oSpecName);
        }
    }

    private int createAdPod(int oSpecId) {
        int adPodId = idSequence.incrementAndGet();
        AdPod adPod = new AdPod();
        adPod.setName("Transient-Incorp-AdPod " + adPodId);
        adPod.setId(adPodId);

        campaignDB.addAdPod(adPod);
        campaignDB.addAdpodOSpecMapping(adPodId, oSpecId);
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
        if(campaignDB.getOspec(tSpecName) == null || !oSpecNameLRUCache.containsKey(tSpecName)) {
            throw new TransientDataException("Ospec for this name doesnt Exist");
        }
        IncorpDeltaMappingRequest<String> request = new IncorpDeltaMappingRequest<String>(urlName, tSpecName, weight, geocode);
        synchronized (tSpecName) {
            List<IncorpDeltaMappingRequest<String>> list = urlMapRequest.safeGet(tSpecName);
            if(list == null) {
                list = new ArrayList<IncorpDeltaMappingRequest<String>>();
            }
            list.add(request);
            //Here there is a possibility of two concurrent threads stepping into each other and messing each others list
            //hence made this part of code synchronized by tSpecName. However since this method is synchronized by tSpecName
            //it is making an assumption that the string is interned, which is true for the clients using this method
            urlMapRequest.safePut(tSpecName, list);
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
        int oSpecId = oSpecNameLRUCache.safeGet(request.getTSpecName()).getId();
        if(oSpecId <= 0) {
            throw new TransientDataException("Ospec " + request.getTSpecName()+ "for specified mapping is not present in memeory");
        }
        int adPodId = createAdPod(oSpecId);
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
    }

    public void addThemeMapping(String themeName, String tSpecName, float weight, Geocode geocode) throws TransientDataException {
        if(campaignDB.getOspec(tSpecName) == null || !oSpecNameLRUCache.containsKey(tSpecName)) {
            throw new TransientDataException("Ospec for this name doesnt Exist");
        }
        IncorpDeltaMappingRequest<String> request = new IncorpDeltaMappingRequest<String>(themeName, tSpecName, weight, geocode);
        synchronized (tSpecName) {
            List<IncorpDeltaMappingRequest<String>> list = themeMapRequest.safeGet(tSpecName);
            if(list == null) {
                list = new ArrayList<IncorpDeltaMappingRequest<String>>();
            }
            list.add(request);
            //Here there is a possibility of two concurrent threads stepping into each other and messing each others list
            //hence made this part of code synchronized by tSpecName. However since this method is synchronized by tSpecName
            //it is making an assumption that the string is interned, which is assumed to be true for the clients using this method
            themeMapRequest.safePut(tSpecName, list);
        }

        addThemeMapping(request);
    }

    @SuppressWarnings({"deprecation"})
    private void addThemeMapping(IncorpDeltaMappingRequest<String> request) throws TransientDataException {
        Theme theme = campaignDB.getTheme(request.getId());
        if(theme == null) {
            int themeId = idSequence.incrementAndGet();
            themeId = themeId + lowBound;
            theme = new Theme();
            theme.setId(themeId);
            theme.setName(request.getId());
            campaignDB.addTheme(theme);
        }
        int oSpecId = oSpecNameLRUCache.safeGet(request.getTSpecName()).getId();
        if(oSpecId <= 0) {
            throw new TransientDataException("Ospec " + request.getTSpecName()+ "for specified mapping is not present in memeory");
        }
        int adPodId = createAdPod(oSpecId);
        ThemeAdPodMapping mapping = new ThemeAdPodMapping();
        mapping.setAdPodId(adPodId);
        mapping.setThemeId(theme.getId());
        mapping.setWeight((int)request.getWeight());
        campaignDB.addThemeMapping(mapping);
        String key = generateKey(request.getTSpecName(), request.getId());
        if(request.getGeocode() != null) {
            addGeocodeMapping(request.getGeocode(), adPodId, request.getWeight());
            oSpecSiteGeoAdPodMap.safePut(key, adPodId);
        }
        else {
            addNonGeocodeMapping(adPodId);
            oSpecSiteNonGeoAdPodMap.safePut(key, adPodId);
        }

    }

    public void addLocationMapping(String locationIdStr, String tSpecName, float weight, Geocode geocode) throws TransientDataException {
        if(campaignDB.getOspec(tSpecName) == null || !oSpecNameLRUCache.containsKey(tSpecName)) {
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
        IncorpDeltaMappingRequest<Integer> request = new IncorpDeltaMappingRequest<Integer>(locationId, tSpecName, weight, geocode);
        synchronized (tSpecName) {
            List<IncorpDeltaMappingRequest<Integer>> list = locationMapRequest.safeGet(tSpecName);
            if(list == null) {
                list = new ArrayList<IncorpDeltaMappingRequest<Integer>>();
            }
            list.add(request);
            //Here there is a possibility of two concurrent threads stepping into each other and messing each others list
            //hence made this part of code synchronized by tSpecName. However since this method is synchronized by tSpecName
            //it is making an assumption that the string is interned, which is assumed to be true for the clients using this method
            locationMapRequest.safePut(tSpecName, list);
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
        int oSpecId = oSpecNameLRUCache.safeGet(request.getTSpecName()).getId();
        if(oSpecId <= 0) {
            throw new TransientDataException("Ospec " + request.getTSpecName()+ "for specified mapping is not present in memeory");
        }
        int adPodId = createAdPod(oSpecId);
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
    }

    public void addGeocodeMapping(Geocode geocode, int adPodId, float weight) throws TransientDataException {
        geocode.setId(idSequence.incrementAndGet());
        campaignDB.addGeocode(geocode);
        campaignDB.addGeocodeMapping(geocode, adPodId, weight);
    }

    public void addNonGeocodeMapping(int adPodId) throws TransientDataException {
        campaignDB.addNonGeoAdPod(adPodId);
    }


    public void deleteUrlMapping(String urlName, String tSpecName, float weight, Geocode geocode) {
        List <IncorpDeltaMappingRequest<String>>  urlRequestList      = urlMapRequest.safeGet(tSpecName);
        if(urlRequestList != null && oSpecNameLRUCache.containsKey(tSpecName)) {
            synchronized(urlRequestList) {
                int adPodId = 0;
                for(int i=0; i<urlRequestList.size(); i++) {
                    IncorpDeltaMappingRequest<String> urlRequest = urlRequestList.get(i);
                    if(urlName.equals(urlRequest.getId())) {
                        Url url = campaignDB.getUrl(urlName);
                        if(url != null) {
                            campaignDB.deleteUrlMapping(urlName, adPodId);
                            campaignDB.deleteUrl(url.getName());
                        }
                        urlRequestList.remove(i);
                        if(urlRequest.getGeocode() != null) {
                            adPodId = oSpecSiteGeoAdPodMap.safeGet(generateKey(tSpecName, urlName));
                            deleteGeocodeMapping(urlRequest.getGeocode(), adPodId);
                            oSpecSiteGeoAdPodMap.safeRemove(generateKey(tSpecName, urlName));
                        }
                        else {
                            adPodId = oSpecSiteNonGeoAdPodMap.safeGet(generateKey(tSpecName, urlName));
                            deleteNonGeocodeMapping(adPodId);
                            oSpecSiteNonGeoAdPodMap.safeRemove(generateKey(tSpecName, urlName));
                        }
                        break;
                    }

                }
            }
        }
    }

    @SuppressWarnings({"deprecation"})
    public void deleteThemeMapping(String themeName, String tSpecName, float weight, Geocode geocode) {
        List <IncorpDeltaMappingRequest<String>>  themeRequestList      = themeMapRequest.safeGet(tSpecName);
        if(themeRequestList != null) {
            synchronized(themeRequestList) {
                int adPodId = 0;
                for(int i=0; i<themeRequestList.size(); i++) {
                    IncorpDeltaMappingRequest<String> themeRequest = themeRequestList.get(i);
                    if(themeName.equals(themeRequest.getId())) {
                        Theme theme = campaignDB.getTheme(themeName);
                        if(theme != null) {

                            campaignDB.deleteThemeMapping(theme.getName(), adPodId);
                            campaignDB.deleteTheme(theme.getName());
                        }
                        themeRequestList.remove(i);
                        if(themeRequest.getGeocode() != null) {
                            adPodId = oSpecSiteGeoAdPodMap.safeGet(generateKey(tSpecName, themeName));
                            deleteGeocodeMapping(themeRequest.getGeocode(), adPodId);
                            oSpecSiteGeoAdPodMap.safeRemove(generateKey(tSpecName, themeName));
                        }
                        else {
                            adPodId = oSpecSiteNonGeoAdPodMap.safeGet(generateKey(tSpecName, themeName));
                            deleteNonGeocodeMapping(adPodId);
                            oSpecSiteNonGeoAdPodMap.safeRemove(generateKey(tSpecName, themeName));
                        }
                        break;
                    }
                }
            }
        }
    }

    public void deleteLocationMapping(String locationIdStr, String tSpecName, float weight, Geocode geocode) {
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
                        Location location = campaignDB.getLocation(locationId);
                        if(location != null) {
                            campaignDB.deleteLocationMapping(location.getId(), adPodId);
                            campaignDB.deleteLocation(location.getId());
                        }
                        locationRequestList.remove(i);
                        if(locationRequest.getGeocode() != null) {
                            adPodId = oSpecSiteGeoAdPodMap.safeGet(generateKey(tSpecName, locationIdStr));
                            deleteGeocodeMapping(locationRequest.getGeocode(), adPodId);
                            oSpecSiteGeoAdPodMap.safeRemove(generateKey(tSpecName, locationIdStr));
                        }
                        else {
                            adPodId = oSpecSiteNonGeoAdPodMap.safeGet(generateKey(tSpecName, locationIdStr));
                            deleteNonGeocodeMapping(adPodId);
                            oSpecSiteNonGeoAdPodMap.safeRemove(generateKey(tSpecName, locationIdStr));
                        }
                        break;
                    }
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

    @SuppressWarnings({"deprecation"})
    private void deleteDependencies(OSpec oSpec) {
        List <IncorpDeltaMappingRequest<String>>  urlRequestList      = urlMapRequest.safeGet(oSpec.getName());
        List <IncorpDeltaMappingRequest<String>>  themeRequestList    = themeMapRequest.safeGet(oSpec.getName());
        List <IncorpDeltaMappingRequest<Integer>> locationRequestList = locationMapRequest.safeGet(oSpec.getName());

        oSpecNameLRUCache.writerLock();
        try {
            oSpecNameLRUCache.remove(oSpec.getName());
        }
        finally {
            oSpecNameLRUCache.writerUnlock();
        }
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

        if(themeRequestList != null) {
            for (IncorpDeltaMappingRequest<String> themeRequest : themeRequestList) {
                String themeName = themeRequest.getId();
                Theme theme = campaignDB.getTheme(themeName);
                if (theme != null) {
                    campaignDB.getThemeAdPodMappingIndex().remove(theme.getName());
                    campaignDB.deleteTheme(theme.getName());
                }
                if(themeRequest.getGeocode() != null) {
                    Integer adPodId;
                    adPodId = oSpecSiteGeoAdPodMap.safeGet(generateKey(oSpec.getName(), themeName));
                    if(adPodId != null) {
                        deleteGeocodeMapping(themeRequest.getGeocode(), adPodId);
                        oSpecSiteGeoAdPodMap.safeRemove(generateKey(oSpec.getName(), themeName));
                    }
                }
                else {
                    Integer adPodId;
                    adPodId = oSpecSiteNonGeoAdPodMap.safeGet(generateKey(oSpec.getName(), themeName));
                    if(adPodId != null) {
                        deleteNonGeocodeMapping(adPodId);
                        oSpecSiteNonGeoAdPodMap.safeRemove(generateKey(oSpec.getName(), themeName));
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
            originalOSpecMap.safeRemove(oSpec.getId());
            campaignDB.deleteOSpec(oSpec.getName());
            campaignDB.addOSpec(originalOSpecMap.safeGet(oSpec.getId()));
        }
        else {
            campaignDB.deleteOSpec(oSpec.getName());
        }

    }

    class OSpecNameLRUCache extends LinkedHashMap<String, OSpec> implements RWLocked {
        private ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
        private int cacheSize;
        OSpecNameLRUCache(int cacheSize) {
            super(cacheSize, 0.75f, true);
            this.cacheSize = cacheSize;
        }

        public void readerLock() {
          try {
            m_rwlock.readLock().lock();
          } catch (Exception e) {
            log.error("Exception reader locking ",e);
          }
        }
        public void readerUnlock() {
          try {
            m_rwlock.readLock().unlock();
          } catch (Exception e) {
            log.error("Exception reader unlocking ",e);
          }
        }
        public void writerLock() {
          try {
            m_rwlock.writeLock().lock();
          } catch (Exception e) {
            log.error("Exception writer locking ",e);
          }
        }
        public void writerUnlock() {
          try {
            m_rwlock.writeLock().unlock();
          } catch (Exception e) {
            log.error("Exception writer unlocking ",e);
          }
        }

        protected boolean removeEldestEntry(Map.Entry<String, OSpec> eldest) {
            boolean deleteLastEntry = (size() > cacheSize);
            if(deleteLastEntry) {
                deleteDependencies(eldest.getValue());
            }
            return false;
        }

        public OSpec safeGet(Object key) {
            OSpec oSpec;            
            readerLock();
            try {
                oSpec = super.get(key);
            }
            finally {
                readerUnlock();
            }
            return oSpec;
        }

        public OSpec safePut(String key, OSpec value) {
            writerLock();
            try {
                return super.put(key, value);
            }
            finally {
                writerLock();
            }
        }

        public void safePutAll(Map<String, OSpec> map) {
            writerLock();
            try {
                super.putAll(map);
            }
            finally {
                writerLock();
            }
        }
    }

    class IncorpDeltaMappingRequest<Key> {
        private Key id;
        private String tSpecName;
        private float weight;
        private Geocode geocode;

        public IncorpDeltaMappingRequest(Key id, String tSpecName, float weight, Geocode geocode) {
            this.id        = id;
            this.tSpecName = tSpecName;
            this.weight    = weight;
            this.geocode   = geocode;
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
    }

}
