package com.tumri.joz.campaign;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import com.tumri.cma.domain.OSpec;
import com.tumri.cma.domain.TSpec;
import com.tumri.joz.Query.CNFQuery;
import com.tumri.joz.Query.ConjunctQuery;
import com.tumri.joz.Query.ProductQueryProcessor;
import com.tumri.joz.utils.AppProperties;

/**
 * Class to maintain the LRU cache of the TSpec queries. The queries are looked up using the TSpec id.
 * The size of the cache currently is set to 2000, but this can be tuned depending upon the number of Tspecs that are most often used.
 * @author nipun
 *
 */
public class TSpecQueryCache {
	private LinkedHashMap<Integer, CNFQuery> m_oSpecQueryCache = null;
	private static AtomicReference<TSpecQueryCache> g_queryCache = null;
    private static final int DEFAULT_MAX_CACHE_ENTRIES = 2000;
    private static final String CONFIG_OSPEC_CACHE_SIZE = "com.tumri.campaign.querycache.size";
    
    private static Logger log = Logger.getLogger (TSpecQueryCache.class);
	private TSpecQueryCache() {
		int cacheSize = DEFAULT_MAX_CACHE_ENTRIES;
		try {
			cacheSize = Integer.parseInt(AppProperties.getInstance().getProperty(CONFIG_OSPEC_CACHE_SIZE));
		} catch(Exception e) {
			cacheSize = DEFAULT_MAX_CACHE_ENTRIES;
		}
		m_oSpecQueryCache = new TSpecLinkedHashMap<Integer, CNFQuery>(cacheSize);
	}

	public static TSpecQueryCache getInstance() {
		if (g_queryCache == null) {
			synchronized (TSpecQueryCache.class) {
				if (g_queryCache == null) {
					g_queryCache = new AtomicReference<TSpecQueryCache>();
					TSpecQueryCache qCache = new TSpecQueryCache();
					g_queryCache.set(qCache);
				}
			}
		}
		return g_queryCache.get();
	}

	/**
	 * Load the ospec query cache
	 * @param oSpecIterator
	 */
	public void load(Iterator<OSpec> oSpecIterator) {
		if (oSpecIterator!=null) {
			clear();

			while(oSpecIterator.hasNext()) {
				OSpec oSpec = oSpecIterator.next();
                List<TSpec> tspecList = oSpec.getTspecs();
                for (TSpec theTSpec : tspecList) {
                    CNFQuery _query = TSpecQueryCacheHelper.getQuery(theTSpec);
                    addToOSpecQueryCache(theTSpec.getId(), _query);
                }
			}
		}
	}

	/**
	 * return the Query for a given oSpec, from the cache - or build the query and then return
	 * @param tspecId
	 * @return
	 */
	public CNFQuery getCNFQuery(Integer tspecId) {
		CNFQuery query = null;
		synchronized (TSpecQueryCache.class) {
			query = lookupOSpecQuery(tspecId);
			if (query == null) {
				TSpec tSpec = lookupTSpec(tspecId);
				if (tSpec!=null) {
					query = TSpecQueryCacheHelper.getQuery(tSpec);
					addToOSpecQueryCache(tspecId, query);
				} else {
					log.warn("The targeted OSpec was not found in the cache : " + tspecId);
					//Adding an empty query as a safegaurd
	                query = new CNFQuery();
	                query.addQuery(new ConjunctQuery(new ProductQueryProcessor()));
	                addToOSpecQueryCache(tspecId, query);
				}
			}
		}

		return query;
	}

	/**
	 * Remove specfic query from the cache
	 */
	public void removeQuery(Integer tspecId){
		synchronized (TSpecQueryCache.class) {
			m_oSpecQueryCache.remove(tspecId);
		}	
	}

	/**
	 * Clear the cache
	 *
	 */
	public void clear() {
		synchronized (TSpecQueryCache.class) {
			m_oSpecQueryCache.clear();
		}	
	}
	
	private void addToOSpecQueryCache(Integer tSpecId, CNFQuery _query) {
		synchronized (TSpecQueryCache.class) {
			m_oSpecQueryCache.put(tSpecId, _query);
		}
	}

	private CNFQuery lookupOSpecQuery(Integer tSpecId) {
		synchronized (TSpecQueryCache.class) {
			return m_oSpecQueryCache.get(tSpecId);
		}	
	}


	private TSpec lookupTSpec(Integer tspecId) {
		return CampaignDB.getInstance().getTspec(tspecId);
	}

	/**
	 * Implementation of the LinkedHashMap to make it a LRU type cache
	 * @author nipun
	 *
	 * @param <K>
	 * @param <V>
	 */
	class TSpecLinkedHashMap<K,V> extends LinkedHashMap<K,V> {
		 
		private static final long serialVersionUID = 1L;
		private int cacheSize;
		
		TSpecLinkedHashMap(int cacheSize) {
			super(cacheSize, 0.75f, true);
			this.cacheSize = cacheSize;
		}

		TSpecLinkedHashMap() {
			super(DEFAULT_MAX_CACHE_ENTRIES, 0.75f, true);
		}
		
		protected boolean removeEldestEntry(Map.Entry eldest) {
	        return size() > cacheSize;
	     }
		
	}

}
