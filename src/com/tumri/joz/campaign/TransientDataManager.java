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
 * 1. When tspec-delete command is made by portal clients
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
    private RWLockedTreeMap<String, IncorpDeltaMappingRequest<Geocode>>       geocodeMapRequest    = new RWLockedTreeMap<String, IncorpDeltaMappingRequest<Geocode>>();
    private RWLockedTreeMap<String, String>                                   nonGeocodeMapRequest = new RWLockedTreeMap<String, String>();

    private RWLockedTreeMap<Integer, Integer> oSpecAdPodMap = new RWLockedTreeMap<Integer, Integer>();

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
        reloadNonGeoAdPods();
        reloadGeocodeMappings();
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

    private void reloadNonGeoAdPods() {
        nonGeocodeMapRequest.readerLock();
        try {
            Iterator iterator = nonGeocodeMapRequest.values().iterator();
            if(iterator != null && iterator.hasNext()) {
                while(iterator.hasNext()) {
                    String tSpecName = (String)iterator.next();
                    if(campaignDB.getOspec(tSpecName) == null || !oSpecNameLRUCache.containsKey(tSpecName)) {
                        log.error("TSpec under non-geo transient data map is no longer valid");
                    }
                    int adPodId = oSpecAdPodMap.safeGet(oSpecNameLRUCache.safeGet(tSpecName).getId());
                    campaignDB.addNonGeoAdPod(adPodId);
                }
            }
        }
        finally {
            nonGeocodeMapRequest.readerUnlock();
        }

    }

    @SuppressWarnings({"unchecked"})
    private void reloadGeocodeMappings() {
        geocodeMapRequest.readerLock();
        try {
            Iterator iterator = geocodeMapRequest.values().iterator();
            if(iterator != null && iterator.hasNext()) {
                while(iterator.hasNext()) {
                    List<IncorpDeltaMappingRequest<Geocode>> requestList = (List<IncorpDeltaMappingRequest<Geocode>>)iterator.next();
                    if(requestList != null) {
                        for (IncorpDeltaMappingRequest<Geocode> request : requestList) {
                            try {
                                addGeocodeMapping(request);
                            }
                            catch (TransientDataException e) {
                                log.error("Error occured while reloading geocode-mapping in TransientDataManager", e);
                            }
                        }
                    }
                }
            }
        }
        finally {
            geocodeMapRequest.readerUnlock();
        }
    }


    public void addOSpec(OSpec oSpec) throws TransientDataException {
        //Check if the oSpec already exists
//        if(campaignDB.getOspec(oSpec.getName()) != null) {
//            throw new TransientDataException("Ospec for this name already Exists");
//        }
        int oSpecId = 0;
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
        oSpecAdPodMap.safePut(oSpecId, oSpecId);
//        }

        //reset the idSequence if highBound is exceeded
        if(idSequence.get() >= highBound) {
            idSequence.set(lowBound);
            log.info("ID for TransientDataManager got reset to lower bound");
        }
    }

    private void addOSpecToCampaignDB(OSpec oSpec) {

        //2. Create new Adpod
        int adPodId = oSpec.getId(); //For now keeping the ospec and adpod id to be the same, since there is a 1-to-1 mapping between the two.
        AdPod adPod = new AdPod();
        adPod.setName(oSpec.getName());
        adPod.setId(adPodId);
        adPod.setOspec(oSpec);

        //4. Add OSpec to CampaignDB maps
        campaignDB.addOSpec(oSpec);

        //5. Add AdPod to CampaignDB maps
        campaignDB.addAdPod(adPod);

        //6. Add to Adpod-Ospec-Map within CampaignDB
        campaignDB.addAdpodOSpecMapping(adPodId, oSpec.getId());

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
     * @throws TransientDataException - Gets thrown for invalid condition
     */
    public void addUrlMapping(String urlName, String tSpecName, float weight) throws TransientDataException {
        if(campaignDB.getOspec(tSpecName) == null || !oSpecNameLRUCache.containsKey(tSpecName)) {
            throw new TransientDataException("Ospec for this name doesnt Exist");
        }
        IncorpDeltaMappingRequest<String> request = new IncorpDeltaMappingRequest<String>(urlName, tSpecName, weight);
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
        int adPodId = oSpecAdPodMap.safeGet(oSpecNameLRUCache.safeGet(request.getTSpecName()).getId());
        UrlAdPodMapping mapping = new UrlAdPodMapping();
        mapping.setAdPodId(adPodId);
        mapping.setUrlId(url.getId());
        mapping.setWeight((int)request.getWeight());
        campaignDB.addUrlMapping(mapping);
    }

    public void addThemeMapping(String themeName, String tSpecName, float weight) throws TransientDataException {
        if(campaignDB.getOspec(tSpecName) == null || !oSpecNameLRUCache.containsKey(tSpecName)) {
            throw new TransientDataException("Ospec for this name doesnt Exist");
        }
        IncorpDeltaMappingRequest<String> request = new IncorpDeltaMappingRequest<String>(themeName, tSpecName, weight);
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
        int adPodId = oSpecAdPodMap.safeGet(oSpecNameLRUCache.safeGet(request.getTSpecName()).getId());
        ThemeAdPodMapping mapping = new ThemeAdPodMapping();
        mapping.setAdPodId(adPodId);
        mapping.setThemeId(theme.getId());
        mapping.setWeight((int)request.getWeight());
        campaignDB.addThemeMapping(mapping);
    }

    public void addLocationMapping(String locationIdStr, String tSpecName, float weight) throws TransientDataException {
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
        IncorpDeltaMappingRequest<Integer> request = new IncorpDeltaMappingRequest<Integer>(locationId, tSpecName, weight);
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
        int adPodId = oSpecAdPodMap.safeGet(oSpecNameLRUCache.safeGet(request.getTSpecName()).getId());
        LocationAdPodMapping mapping = new LocationAdPodMapping();
        mapping.setAdPodId(adPodId);
        mapping.setLocationId(location.getId());
        campaignDB.addLocationMapping(mapping);
    }

    public void addGeocodeMapping(Geocode geocode, String tSpecName, float weight) throws TransientDataException {
        if(campaignDB.getOspec(tSpecName) == null || !oSpecNameLRUCache.containsKey(tSpecName)) {
            throw new TransientDataException("Ospec for this name doesnt Exist");
        }
        geocode.setId(idSequence.incrementAndGet());
        IncorpDeltaMappingRequest<Geocode> request = new IncorpDeltaMappingRequest<Geocode>(geocode, tSpecName, weight);
        synchronized (tSpecName) {
            //Here there is a possibility of two concurrent threads stepping into each other and messing each others list
            //hence made this part of code synchronized by tSpecName. However since this method is synchronized by tSpecName
            //it is making an assumption that the string is interned, which is true for the clients using this method
            geocodeMapRequest.safePut(tSpecName, request);
        }

        addGeocodeMapping(request);
    }

    private void addGeocodeMapping(IncorpDeltaMappingRequest<Geocode> request) throws TransientDataException {
        campaignDB.addGeocode(request.getId());
        int adPodId = oSpecAdPodMap.safeGet(oSpecNameLRUCache.safeGet(request.getTSpecName()).getId());
        campaignDB.addGeocodeMapping(request.getId(), adPodId, request.getWeight());
    }

    public void addNonGeocodeMapping(String tSpecName) throws TransientDataException {
        if(campaignDB.getOspec(tSpecName) == null || !oSpecNameLRUCache.containsKey(tSpecName)) {
            throw new TransientDataException("Ospec for this name doesnt Exist");
        }
        int adPodId = oSpecAdPodMap.safeGet(oSpecNameLRUCache.safeGet(tSpecName).getId());
        campaignDB.addNonGeoAdPod(adPodId);
        nonGeocodeMapRequest.safePut(tSpecName, tSpecName);
    }


    public void deleteUrlMapping(String urlName, String tSpecName, float weight) {
        List <IncorpDeltaMappingRequest<String>>  urlRequestList      = urlMapRequest.safeGet(tSpecName);
        if(urlRequestList != null && oSpecNameLRUCache.containsKey(tSpecName)) {
            synchronized(urlRequestList) {
                int adPodId = oSpecAdPodMap.safeGet(oSpecNameLRUCache.safeGet(tSpecName).getId());
                for(int i=0; i<urlRequestList.size(); i++) {
                    IncorpDeltaMappingRequest<String> urlRequest = urlRequestList.get(i);
                    if(urlName.equals(urlRequest.getId())) {
                        Url url = campaignDB.getUrl(urlName);
                        if(url != null) {
                            campaignDB.deleteUrlMapping(urlName, adPodId);
                            campaignDB.deleteUrl(url.getName());
                        }
                        urlRequestList.remove(i);
                        break;
                    }
                }
            }
        }
    }

    @SuppressWarnings({"deprecation"})
    public void deleteThemeMapping(String themeName, String tSpecName, float weight) {
        List <IncorpDeltaMappingRequest<String>>  themeRequestList      = themeMapRequest.safeGet(tSpecName);
        if(themeRequestList != null) {
            synchronized(themeRequestList) {
                int adPodId = oSpecAdPodMap.safeGet(oSpecNameLRUCache.safeGet(tSpecName).getId());
                for(int i=0; i<themeRequestList.size(); i++) {
                    IncorpDeltaMappingRequest<String> themeRequest = themeRequestList.get(i);
                    if(themeName.equals(themeRequest.getId())) {
                        Theme theme = campaignDB.getTheme(themeName);
                        if(theme != null) {

                            campaignDB.deleteThemeMapping(theme.getName(), adPodId);
                            campaignDB.deleteTheme(theme.getName());
                        }
                        themeRequestList.remove(i);
                        break;
                    }
                }
            }
        }
    }

    public void deleteLocationMapping(String locationIdStr, String tSpecName, float weight) {
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
                int adPodId = oSpecAdPodMap.safeGet(oSpecNameLRUCache.safeGet(tSpecName).getId());
                for(int i=0; i<locationRequestList.size(); i++) {
                    IncorpDeltaMappingRequest<Integer> locationRequest = locationRequestList.get(i);
                    if(locationId == locationRequest.getId()) {
                        Location location = campaignDB.getLocation(locationId);
                        if(location != null) {
                            campaignDB.deleteLocationMapping(location.getId(), adPodId);
                            campaignDB.deleteLocation(location.getId());
                        }
                        locationRequestList.remove(i);
                        break;
                    }
                }
            }
        }
    }

    public void deleteGeocodeMapping(String tSpecName) {
        if(geocodeMapRequest.containsKey(tSpecName) && oSpecNameLRUCache.containsKey(tSpecName)) {
            IncorpDeltaMappingRequest<Geocode> geocodeRequest  = geocodeMapRequest.safeGet(tSpecName);
            int adPodId = oSpecAdPodMap.safeGet(oSpecNameLRUCache.safeGet(tSpecName).getId());
            campaignDB.deleteGeocodeMapping(geocodeRequest.getId(), adPodId);
            geocodeMapRequest.safeRemove(tSpecName);
        }
    }

    public void deleteNonGeocodeMapping(String tSpecName) {
        if(nonGeocodeMapRequest.containsKey(tSpecName) && oSpecNameLRUCache.containsKey(tSpecName)) {
            int adPodId = oSpecAdPodMap.safeGet(oSpecNameLRUCache.safeGet(tSpecName).getId());
            campaignDB.deleteNonGeoAdPod(adPodId);
            nonGeocodeMapRequest.safeRemove(tSpecName);
        }
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
        oSpecAdPodMap.remove(oSpec.getId());
        urlMapRequest.safeRemove(oSpec.getName());
        themeMapRequest.safeRemove(oSpec.getName());
        locationMapRequest.safeRemove(oSpec.getName());
        geocodeMapRequest.safeRemove(oSpec.getName());
        nonGeocodeMapRequest.safeRemove(oSpec.getName());

        deleteNonGeocodeMapping(oSpec.getName());
        deleteGeocodeMapping(oSpec.getName());
        if(urlRequestList != null) {
            for (IncorpDeltaMappingRequest<String> urlRequest : urlRequestList) {
                String urlName = urlRequest.getId();
                Url url = campaignDB.getUrl(urlName);
                if (url != null) {
                    campaignDB.getUrlAdPodMappingIndex().remove(UrlNormalizer.getNormalizedUrl(url.getName()));
                    campaignDB.deleteUrl(url.getName());
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
            }
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

        public IncorpDeltaMappingRequest(Key id, String tSpecName, float weight) {
            this.id = id;
            this.tSpecName = tSpecName;
            this.weight = weight;
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
    }

}
