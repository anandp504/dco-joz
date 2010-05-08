/*
 * WMDB.java
 *
 * COPYRIGHT (C)  TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (@tumri.com)
 * @version 1.0     
 *
 */
package com.tumri.joz.campaign.wm;

import com.tumri.joz.index.AbstractRangeIndex;
import com.tumri.joz.products.Handle;
import com.tumri.utils.data.RWLockedSortedArraySet;
import com.tumri.utils.data.RWLockedTreeMap;
import com.tumri.utils.index.AbstractIndex;
import com.tumri.joz.index.Range;
import com.tumri.joz.campaign.wm.WMRangeIndex;

import java.util.*;

/**
 * Index of all the weight information. Provides the methods to add/delete weight at Adpod level and recipe level (??)
 *
 * @author: nipun
 * Date: Aug 3, 2009
 * Time: 12:55:22 PM
 */
public class WMDB {

	private static WMDB g_DB;

	private RWLockedTreeMap<Integer, WMIndexCache> weightDBIndex = new RWLockedTreeMap<Integer, WMIndexCache>();

	public static WMDB getInstance() {
		if (g_DB == null) {
			synchronized (WMDB.class) {
				if (g_DB == null) {
					g_DB = new WMDB();
				}
			}
		}
		return g_DB;
	}

	/**
	 * Do safe add of the index
	 *
	 * @param adPodId - the adpod Id
	 * @param db      - The weightDB
	 */
	public void addWeightDB(Integer adPodId, WMIndexCache db) {
		weightDBIndex.safePut(adPodId, db);
	}

	/**
	 * Do safe delete of index
	 *
	 * @param adPodId - the adpod id
	 */
	public void deleteWeightDB(Integer adPodId) {
		weightDBIndex.safeRemove(adPodId);
	}

	/**
	 * Check if index exists
	 *
	 * @param adPodId - the adpod id
	 */
	public boolean hasWeightDB(Integer adPodId) {
		return weightDBIndex.containsKey(adPodId);
	}

	/**
	 * Safe gets the index
	 *
	 * @param adPodId - The adpod id
	 * @return - WeightDB object
	 */
	public WMIndexCache getWeightDB(Integer adPodId) {
		WMIndexCache db = weightDBIndex.safeGet(adPodId);
		if (db == null) {
			db = new WMIndexCache();
			addWeightDB(adPodId, db);
		}
		return db;
	}

	public void clearDB() {
		weightDBIndex.clear();
	}

	public void purgeOldEntries(List<Integer> inclusionList) {
		Set<Integer> currAdPods = weightDBIndex.keySet();
		List<Integer> deletedAdPods = new ArrayList<Integer>();
		for (Integer id : currAdPods) {
			if (!inclusionList.contains(id)) {
				deletedAdPods.add(id);
			}
		}
		if (deletedAdPods.size() > 0) {
			for (Integer adPodId : deletedAdPods) {
				weightDBIndex.safeRemove(adPodId);
			}
		}
	}

	public Set<Integer> getAdpodIds() {
		return weightDBIndex.keySet();
	}

	public class WMIndexCache {
		// All indices are maintained in the class in a hashtable
		private Hashtable<WMAttribute, AbstractIndex<WMHandle, WMAttribute, ?, WMHandle>> m_indices = new Hashtable<WMAttribute, AbstractIndex<WMHandle, WMAttribute, ?, WMHandle>>();
		private RWLockedSortedArraySet<WMHandle> m_allHandles = new RWLockedSortedArraySet<WMHandle>();

		public WMIndexCache() {
			//addIndex(WMAttribute.kDefault, new WMIndex<Integer, WMHandle>(WMAttribute.kDefault));
			addIndex(WMAttribute.kLineId, new WMIndex<Integer, WMHandle>(WMAttribute.kLineId));
			addIndex(WMAttribute.kSiteId, new WMIndex<Integer, WMHandle>(WMAttribute.kSiteId));
			addIndex(WMAttribute.kCreativeId, new WMIndex<Integer, WMHandle>(WMAttribute.kCreativeId));
			addIndex(WMAttribute.kBuyId, new WMIndex<Integer, WMHandle>(WMAttribute.kBuyId));
			addIndex(WMAttribute.kAdId, new WMIndex<Integer, WMHandle>(WMAttribute.kAdId));
			addIndex(WMAttribute.kState, new WMIndex<Integer, WMHandle>(WMAttribute.kState));
			addIndex(WMAttribute.kZip, new WMIndex<Integer, WMHandle>(WMAttribute.kZip));
			addIndex(WMAttribute.kDMA, new WMIndex<Integer, WMHandle>(WMAttribute.kDMA));
			addIndex(WMAttribute.kArea, new WMIndex<Integer, WMHandle>(WMAttribute.kArea));
			addIndex(WMAttribute.kCity, new WMIndex<Integer, WMHandle>(WMAttribute.kCity));
			addIndex(WMAttribute.kCountry, new WMIndex<Integer, WMHandle>(WMAttribute.kCountry));
			addIndex(WMAttribute.kT1, new WMIndex<Integer, WMHandle>(WMAttribute.kT1));
			addIndex(WMAttribute.kT2, new WMIndex<Integer, WMHandle>(WMAttribute.kT2));
			addIndex(WMAttribute.kT3, new WMIndex<Integer, WMHandle>(WMAttribute.kT3));
			addIndex(WMAttribute.kT4, new WMIndex<Integer, WMHandle>(WMAttribute.kT4));
			addIndex(WMAttribute.kT5, new WMIndex<Integer, WMHandle>(WMAttribute.kT5));
			addIndex(WMAttribute.kF1, new WMIndex<Integer, WMHandle>(WMAttribute.kF1));
			addIndex(WMAttribute.kF2, new WMIndex<Integer, WMHandle>(WMAttribute.kF2));
			addIndex(WMAttribute.kF3, new WMIndex<Integer, WMHandle>(WMAttribute.kF3));
			addIndex(WMAttribute.kF4, new WMIndex<Integer, WMHandle>(WMAttribute.kF4));
			addIndex(WMAttribute.kF5, new WMIndex<Integer, WMHandle>(WMAttribute.kF5));
			addIndex(WMAttribute.kUB, new WMRangeIndex<Integer, WMHandle>(WMAttribute.kUB));
            addIndex(WMAttribute.kLineIdNone, new WMIndex<Integer, WMHandle>(WMAttribute.kLineIdNone));
			addIndex(WMAttribute.kSiteIdNone, new WMIndex<Integer, WMHandle>(WMAttribute.kSiteIdNone));
			addIndex(WMAttribute.kCreativeIdNone, new WMIndex<Integer, WMHandle>(WMAttribute.kCreativeIdNone));
			addIndex(WMAttribute.kBuyIdNone, new WMIndex<Integer, WMHandle>(WMAttribute.kBuyIdNone));
			addIndex(WMAttribute.kAdIdNone, new WMIndex<Integer, WMHandle>(WMAttribute.kAdIdNone));
			addIndex(WMAttribute.kStateNone, new WMIndex<Integer, WMHandle>(WMAttribute.kStateNone));
			addIndex(WMAttribute.kZipNone, new WMIndex<Integer, WMHandle>(WMAttribute.kZipNone));
			addIndex(WMAttribute.kDMANone, new WMIndex<Integer, WMHandle>(WMAttribute.kDMANone));
			addIndex(WMAttribute.kAreaNone, new WMIndex<Integer, WMHandle>(WMAttribute.kAreaNone));
			addIndex(WMAttribute.kCityNone, new WMIndex<Integer, WMHandle>(WMAttribute.kCityNone));
			addIndex(WMAttribute.kCountryNone, new WMIndex<Integer, WMHandle>(WMAttribute.kCountryNone));
			addIndex(WMAttribute.kT1None, new WMIndex<Integer, WMHandle>(WMAttribute.kT1None));
			addIndex(WMAttribute.kT2None, new WMIndex<Integer, WMHandle>(WMAttribute.kT2None));
			addIndex(WMAttribute.kT3None, new WMIndex<Integer, WMHandle>(WMAttribute.kT3None));
			addIndex(WMAttribute.kT4None, new WMIndex<Integer, WMHandle>(WMAttribute.kT4None));
			addIndex(WMAttribute.kT5None, new WMIndex<Integer, WMHandle>(WMAttribute.kT5None));
			addIndex(WMAttribute.kF1None, new WMIndex<Integer, WMHandle>(WMAttribute.kF1None));
			addIndex(WMAttribute.kF2None, new WMIndex<Integer, WMHandle>(WMAttribute.kF2None));
			addIndex(WMAttribute.kF3None, new WMIndex<Integer, WMHandle>(WMAttribute.kF3None));
			addIndex(WMAttribute.kF4None, new WMIndex<Integer, WMHandle>(WMAttribute.kF4None));
			addIndex(WMAttribute.kF5None, new WMIndex<Integer, WMHandle>(WMAttribute.kF5None));
			addIndex(WMAttribute.kUBNone, new WMIndex<Integer, WMHandle>(WMAttribute.kUBNone));
		}

		public void addIndex(WMAttribute aAttribute, AbstractIndex<WMHandle, WMAttribute, ?, WMHandle> index) {
			m_indices.put(aAttribute, index);
		}

		public void deleteIndex(WMAttribute aAttribute) {
			m_indices.remove(aAttribute);
		}

		public AbstractIndex getIndex(WMAttribute aAttribute) {
			return m_indices.get(aAttribute);
		}

		public boolean hasIndex(WMAttribute aAttribute) {
			return m_indices.containsKey(aAttribute);
		}

		public Enumeration<WMAttribute> getIndices() {
			return m_indices.keys();
		}

		@SuppressWarnings("unchecked")
		public void updateRangeIndex(WMAttribute type, TreeMap<Range<Integer>, ArrayList<WMHandle>> mindex) {
			deleteRangeIndex(type, mindex);
			((AbstractRangeIndex<WMAttribute, Integer, WMHandle>) m_indices.get(type)).add(mindex);
		}

		@SuppressWarnings("unchecked")
		public void deleteRangeIndex(WMAttribute type, TreeMap<Range<Integer>, ArrayList<WMHandle>> mindex) {
			((AbstractRangeIndex<WMAttribute, Integer, WMHandle>) m_indices.get(type)).delete(mindex);
		}

		@SuppressWarnings("unchecked")
		public void updateIntegerIndex(WMAttribute type, TreeMap<Integer, ArrayList<WMHandle>> mindex) {
			deleteIntegerIndex(type, mindex);
			((AbstractIndex<WMHandle, WMAttribute, Integer, WMHandle>) m_indices.get(type)).add(mindex);
		}

		@SuppressWarnings("unchecked")
		public void deleteIntegerIndex(WMAttribute type, TreeMap<Integer, ArrayList<WMHandle>> mindex) {
			((AbstractIndex<WMHandle, WMAttribute, Integer, WMHandle>) m_indices.get(type)).delete(mindex);
		}

        @SuppressWarnings("unchecked")
		public void materializeRangeIndices() {
			for (WMAttribute attr : WMUtils.getRangeAttributes()) {
				((WMRangeIndex<Integer, WMHandle>) m_indices.get(attr)).materialize();
			}
		}

		/**
		 * Add the new products into the database.
		 */
		public void addNewHandles(SortedSet<WMHandle> newHandles) {
			try {
				m_allHandles.writerLock();
				m_allHandles.removeAll(newHandles);
				m_allHandles.addAll(newHandles);
			} finally {
				m_allHandles.writerUnlock();
			}
		}


		/**
		 * Get Handle without checking a lock, reader should call readerLock()
		 * Check if the prod exists - else return null
		 *
		 * @param pid
		 * @return Handle
		 */
		public WMHandle getWMHandle(Long pid, Long sid) {
			WMHandle p = new WMHandle(pid, sid, null, null);
			Handle ph;
			try {
				m_allHandles.readerLock();
				ph = m_allHandles.find(p);
			} finally {
				m_allHandles.readerUnlock();
			}

			if (ph != null) {
				p = (WMHandle) ph;
			} else {
				p = null;
			}
			return p;
		}


		public Iterator<WMHandle> getAllHandles() {
			return m_allHandles.iterator();
		}

		/**
		 * Delete any WM Handles that are not current.
		 *
		 * @param includedWMHandlesIdSet
		 */
        @SuppressWarnings("unchecked")
		public void purgeOldKeys(Set<Integer> includedWMHandlesIdSet) {
			Enumeration<WMAttribute> keys = getIndices();
			while (keys.hasMoreElements()) {
				WMAttribute type = keys.nextElement();
				if (WMUtils.getRangeAttributes().contains(type)) {
					WMRangeIndex<Integer, WMHandle> idx = (WMRangeIndex<Integer, WMHandle>) m_indices.get(type);
					Set<Range<Integer>> res = null;
					if (idx != null) {
						res = idx.getKeys();
						TreeMap<Range<Integer>, ArrayList<WMHandle>> map = new TreeMap<Range<Integer>, ArrayList<WMHandle>>();
						for (Range<Integer> key : res) {
							SortedSet<WMHandle> values = idx.get(key);
							ArrayList<WMHandle> deletedList = new ArrayList<WMHandle>();
							for (WMHandle h : values) {
								int id = (int) h.getOid();
								if (!includedWMHandlesIdSet.contains(id)) {
									deletedList.add(h);
								}
							}
							if (!deletedList.isEmpty()) {
								m_allHandles.removeAll(deletedList);
								map.put(key, deletedList);
							}
						}
						if (!map.isEmpty()) {
							deleteRangeIndex(type, map);
						}
					}
				} else {
					WMIndex<Integer, WMHandle> idx = (WMIndex<Integer, WMHandle>) m_indices.get(type);
					Set<Integer> res = null;
					if (idx != null) {
						res = idx.getKeys();
						TreeMap<Integer, ArrayList<WMHandle>> map = new TreeMap<Integer, ArrayList<WMHandle>>();
						for (Integer key : res) {
							SortedSet<WMHandle> values = idx.get(key);
							ArrayList<WMHandle> deletedList = new ArrayList<WMHandle>();
							for (WMHandle h : values) {
								int id = (int) h.getOid();
								if (!includedWMHandlesIdSet.contains(id)) {
									deletedList.add(h);
								}
							}
							if (!deletedList.isEmpty()) {
								m_allHandles.removeAll(deletedList);
								map.put(key, deletedList);
							}
						}
						if (!map.isEmpty()) {
							deleteIntegerIndex(type, map);
						}
					}
				}
			}
		}

		public int getNumHandles() {
			return m_allHandles.size();
		}
	}
}
