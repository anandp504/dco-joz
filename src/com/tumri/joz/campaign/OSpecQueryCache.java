package com.tumri.joz.campaign;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.tumri.cma.domain.OSpec;
import com.tumri.joz.Query.CNFQuery;

/**
 * Class to maintain the LRU cache of the OSpec queries. The queries are looked up using the OSpec name.
 * The size of the cache currently is set to 2000, but this can be tuned depending upon the number of Ospecs that are most often used.
 * @author nipun
 *
 */
public class OSpecQueryCache {
	private LinkedHashMap<String, CNFQuery> m_oSpecQueryCache = null;
	private static AtomicReference<OSpecQueryCache> g_queryCache = null;

	private OSpecQueryCache() {
		m_oSpecQueryCache = new LinkedHashMap<String, CNFQuery>(2000, 0.75f, true);
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
		clear();

		while(ospecIter.hasNext()) {
			OSpec theOSpec = ospecIter.next();
			CNFQuery _query = OSpecQueryCacheHelper.getQuery(theOSpec);
			addToOSpecQueryCache(theOSpec.getName(), _query);
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
	 * Clear the cache
	 */
	public void removeQuery(String oSpecName){
		synchronized (OSpecQueryCache.class) {
			m_oSpecQueryCache.remove(oSpecName);
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
	 * Clear the cache
	 *
	 */
	private void clear() {
		synchronized (OSpecQueryCache.class) {
			m_oSpecQueryCache.clear();
		}	
	}

}
