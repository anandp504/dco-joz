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

import com.tumri.joz.products.Handle;
import com.tumri.utils.data.RWLockedSortedArraySet;
import com.tumri.utils.data.RWLockedTreeMap;

import java.util.*;

/**
 * Index of all the weight information. Provides the methods to add/delete weight at Adpod level and recipe level (??)
 * @author: nipun
 * Date: Aug 3, 2009
 * Time: 12:55:22 PM
 */
public class WMDB {

    private static WMDB g_DB;

    private  RWLockedTreeMap<Integer, WMIndexCache> weightDBIndex = new RWLockedTreeMap<Integer, WMIndexCache>();

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
     * @param adPodId - the adpod Id
     * @param db - The weightDB
     */
    public void addWeightDB(Integer adPodId, WMIndexCache db){
        weightDBIndex.safePut(adPodId, db);
    }

    /**
     * Do safe delete of index
     * @param adPodId - the adpod id
     */
    public void deleteWeightDB(Integer adPodId) {
        weightDBIndex.safeRemove(adPodId);
    }

    /**
     * Check if index exists
     * @param adPodId - the adpod id
     */
    public boolean hasWeightDB(Integer adPodId) {
        return weightDBIndex.containsKey(adPodId);
    }

    /**
     * Safe gets the index
     * @param adPodId - The adpod id
     * @return - WeightDB object
     */
    public WMIndexCache getWeightDB(Integer adPodId) {
        WMIndexCache db = weightDBIndex.safeGet(adPodId);
        if (db==null) {
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
        if (deletedAdPods.size()>0) {
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
        private Hashtable<WMIndex.Attribute, WMIndex<?, WMHandle>> m_indices = new Hashtable<WMIndex.Attribute, WMIndex<?, WMHandle>>();
        private RWLockedSortedArraySet<WMHandle> m_allHandles = new RWLockedSortedArraySet<WMHandle>();

        public WMIndexCache() {
            //addIndex(WMIndex.Attribute.kDefault, new WMIndex<Integer, WMHandle>(WMIndex.Attribute.kDefault));
            addIndex(WMIndex.Attribute.kLineId, new WMIndex<Integer, WMHandle>(WMIndex.Attribute.kLineId));
            addIndex(WMIndex.Attribute.kState, new WMIndex<Integer, WMHandle>(WMIndex.Attribute.kState));
            addIndex(WMIndex.Attribute.kZip, new WMIndex<Integer, WMHandle>(WMIndex.Attribute.kZip));
            addIndex(WMIndex.Attribute.kDMA, new WMIndex<Integer, WMHandle>(WMIndex.Attribute.kDMA));
            addIndex(WMIndex.Attribute.kArea, new WMIndex<Integer, WMHandle>(WMIndex.Attribute.kArea));
            addIndex(WMIndex.Attribute.kCity, new WMIndex<Integer, WMHandle>(WMIndex.Attribute.kCity));
            addIndex(WMIndex.Attribute.kCountry, new WMIndex<Integer, WMHandle>(WMIndex.Attribute.kCountry));
        }

        public void addIndex(WMIndex.Attribute aAttribute, WMIndex<?, WMHandle> index) {
            m_indices.put(aAttribute, index);
        }

        public void deleteIndex(WMIndex.Attribute aAttribute) {
            m_indices.remove(aAttribute);
        }

        public WMIndex getIndex(WMIndex.Attribute aAttribute) {
            return m_indices.get(aAttribute);
        }

        public boolean hasIndex(WMIndex.Attribute aAttribute) {
            return m_indices.containsKey(aAttribute);
        }

        public Enumeration<WMIndex.Attribute> getIndices() {
            return m_indices.keys();
        }

        @SuppressWarnings("unchecked")
        public void updateIntegerIndex(WMIndex.Attribute type, TreeMap<Integer, ArrayList<WMHandle>> mindex) {
	        deleteIntegerIndex(type, mindex);
            ((WMIndex<Integer, WMHandle>) m_indices.get(type)).add(mindex);
        }

        @SuppressWarnings("unchecked")
        public void deleteIntegerIndex(WMIndex.Attribute type, TreeMap<Integer, ArrayList<WMHandle>> mindex) {
            ((WMIndex<Integer, WMHandle>) m_indices.get(type)).delete(mindex);
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
         * @param pid
         * @return Handle
         */
        public WMHandle getWMHandle(Long pid) {
            WMHandle p = new WMHandle(pid, null,null);
            Handle ph;
            try {
                m_allHandles.readerLock();
                ph = m_allHandles.find(p);
            } finally {
                m_allHandles.readerUnlock();
            }

            if (ph !=null) {
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
         * @param includedWMHandlesIdSet
         */
        public void purgeOldKeys(Set<Integer> includedWMHandlesIdSet) {
            Enumeration<WMIndex.Attribute> keys = getIndices();
            while (keys.hasMoreElements()) {
                WMIndex.Attribute type = keys.nextElement();
                Set<Integer> res = null;
                WMIndex<Integer, WMHandle> idx = (WMIndex<Integer, WMHandle>) m_indices.get(type);
                if (idx!=null) {
                    res = idx.getKeys();
                    TreeMap<Integer, ArrayList<WMHandle>> map = new TreeMap<Integer, ArrayList<WMHandle>>();
                    for (Integer key: res) {
                        SortedSet<WMHandle> values = idx.get(key);
                        ArrayList<WMHandle> deletedList = new ArrayList<WMHandle>();
                        for(WMHandle h: values) {
                            int id = (int)h.getOid();
                            if (!includedWMHandlesIdSet.contains(id)){
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

	    public int getNumHandles(){
		    return m_allHandles.size();
	    }
    }
}
