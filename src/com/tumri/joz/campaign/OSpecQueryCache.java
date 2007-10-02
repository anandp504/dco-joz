package com.tumri.joz.campaign;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.tumri.cma.domain.OSpec;
import com.tumri.joz.Query.CNFQuery;
import com.tumri.joz.utils.AppProperties;

/**
 * Class to maintain the LRU cache of the OSpec queries. The queries are looked up using the OSpec name.
 * The size of the cache currently is set to 2000, but this can be tuned depending upon the number of Ospecs that are most often used.
 * @author nipun
 *
 */
public class OSpecQueryCache {
	private LinkedHashMap<String, CNFQuery> m_oSpecQueryCache = null;
	private static AtomicReference<OSpecQueryCache> g_queryCache = null;
    private static final int DEFAULT_MAX_CACHE_ENTRIES = 2000;
    private static final String CONFIG_OSPEC_CACHE_SIZE = "com.tumri.campaign.querycache.size";
    
    
	private OSpecQueryCache() {
		int cacheSize = DEFAULT_MAX_CACHE_ENTRIES;
		try {
			cacheSize = Integer.parseInt(AppProperties.getInstance().getProperty(CONFIG_OSPEC_CACHE_SIZE));
		} catch(Exception e) {
			cacheSize = DEFAULT_MAX_CACHE_ENTRIES;
		}
		m_oSpecQueryCache = new OSpecLinkedHashMap<String, CNFQuery>(cacheSize);
	}

	public static OSpecQueryCache getInstance() {
		if (g_queryCache == null) {
			synchronized (OSpecQueryCache.class) {
				if (g_queryCache == null) {
					g_queryCache = new AtomicReference<OSpecQueryCache>();
					OSpecQueryCache qCache = new OSpecQueryCache();
					g_queryCache.set(qCache);
				}
			}
		}
		return g_queryCache.get();
	}

	/**
	 * Load the ospec query cache
	 * @param ospecIter
	 */
	public void load(Iterator<OSpec> ospecIter) {
		if (ospecIter!=null) {
			clear();

			while(ospecIter.hasNext()) {
				OSpec theOSpec = ospecIter.next();
				CNFQuery _query = OSpecQueryCacheHelper.getQuery(theOSpec);
				addToOSpecQueryCache(theOSpec.getName(), _query);
			}
		}
	}

	/**
	 * return the Query for a given oSpec, from the cache - or build the query and then return
	 * @param oSpecName
	 * @return
	 */
	public CNFQuery getCNFQuery(String oSpecName) {
		CNFQuery query = null;
		synchronized (OSpecQueryCache.class) {
			query = lookupOSpecQuery(oSpecName);
			if (query == null) {
				OSpec oSpec = lookupOSpec(oSpecName);
				if (oSpec!=null) {
					query = OSpecQueryCacheHelper.getQuery(oSpec);
					addToOSpecQueryCache(oSpecName, query);
				}
			}
		}

		return query;
	}

	/**
	 * Remove specfic query from the cache
	 */
	public void removeQuery(String oSpecName){
		synchronized (OSpecQueryCache.class) {
			m_oSpecQueryCache.remove(oSpecName);
		}	
	}

	/**
	 * Clear the cache
	 *
	 */
	public void clear() {
		synchronized (OSpecQueryCache.class) {
			m_oSpecQueryCache.clear();
		}	
	}
	
	private void addToOSpecQueryCache(String oSpecName, CNFQuery _query) {
		synchronized (OSpecQueryCache.class) {
			m_oSpecQueryCache.put(oSpecName, _query);
		}
	}

	private CNFQuery lookupOSpecQuery(String oSpecName) {
		synchronized (OSpecQueryCache.class) {
			return m_oSpecQueryCache.get(oSpecName);
		}	
	}


	private OSpec lookupOSpec(String oSpecName) {
		return CampaignDB.getInstance().getOspec(oSpecName);
	}

	/**
	 * Implementation of the LinkedHashMap to make it a LRU type cache
	 * @author nipun
	 *
	 * @param <K>
	 * @param <V>
	 */
	class OSpecLinkedHashMap<K,V> extends LinkedHashMap<K,V> {
		 
		private static final long serialVersionUID = 1L;
		private int cacheSize;
		
		OSpecLinkedHashMap(int cacheSize) {
			super(cacheSize, 0.75f, true);
		}

		OSpecLinkedHashMap() {
			super(DEFAULT_MAX_CACHE_ENTRIES, 0.75f, true);
		}
		
		protected boolean removeEldestEntry(Map.Entry eldest) {
	        return size() > cacheSize;
	     }
		
	}

}
