package com.tumri.joz.campaign.wm;

import com.tumri.cma.rules.CreativeSet;
import com.tumri.cma.rules.ListingClause;
import com.tumri.joz.index.AbstractRangeIndex;
import com.tumri.joz.index.Range;
import com.tumri.joz.products.Handle;
import com.tumri.utils.Pair;
import com.tumri.utils.data.*;
import com.tumri.utils.index.AbstractIndex;

import java.util.*;

/**
 * Class that will serve as the cache for all rules in the system. A rule is a association of a request context to a set of
 * CreativeSets (and/or) ListingSets
 *
 * User: nipun
 */
public class VectorDB {
    private static VectorDB g_DB;

    public static VectorDB getInstance() {
        if (g_DB == null) {
            synchronized (VectorDB.class) {
                if (g_DB == null) {
                    g_DB = new VectorDB();
                }
            }
        }
        return g_DB;
    }

    private Hashtable<VectorAttribute, AbstractIndex<VectorHandle, VectorAttribute, ?, VectorHandle>> m_indices =
            new Hashtable<VectorAttribute, AbstractIndex<VectorHandle, VectorAttribute, ?, VectorHandle>>();

    private RWLockedSortedArraySet<VectorHandle> m_allOptHandles = new RWLockedSortedArraySet<VectorHandle>();

    private RWLockedSortedArraySet<VectorHandle> m_allPersHandles = new RWLockedSortedArraySet<VectorHandle>();

    private RWLockedSortedArraySet<VectorHandle> m_allDefHandles = new RWLockedSortedArraySet<VectorHandle>();

    private RWLockedTreeMap<Long, SortedBag<Pair<CreativeSet, Double>>> m_ruleMap =
            new  RWLockedTreeMap<Long, SortedBag<Pair<CreativeSet, Double>>>();

    private RWLockedTreeMap<Long, SortedBag<Pair<ListingClause, Double>>> m_clauseMap =
            new  RWLockedTreeMap<Long, SortedBag<Pair<ListingClause, Double>>>();

    public VectorDB() {
        addIndex(VectorAttribute.kDefault, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kDefault));
        addIndex(VectorAttribute.kLineId, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kLineId));
        addIndex(VectorAttribute.kSiteId, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kSiteId));
        addIndex(VectorAttribute.kCreativeId, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kCreativeId));
        addIndex(VectorAttribute.kBuyId, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kBuyId));
        addIndex(VectorAttribute.kAdId, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kAdId));
        addIndex(VectorAttribute.kState, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kState));
        addIndex(VectorAttribute.kZip, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kZip));
        addIndex(VectorAttribute.kDMA, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kDMA));
        addIndex(VectorAttribute.kArea, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kArea));
        addIndex(VectorAttribute.kCity, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kCity));
        addIndex(VectorAttribute.kCountry, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kCountry));
        addIndex(VectorAttribute.kT1, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kT1));
        addIndex(VectorAttribute.kT2, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kT2));
        addIndex(VectorAttribute.kT3, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kT3));
        addIndex(VectorAttribute.kT4, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kT4));
        addIndex(VectorAttribute.kT5, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kT5));
        addIndex(VectorAttribute.kF1, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kF1));
        addIndex(VectorAttribute.kF2, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kF2));
        addIndex(VectorAttribute.kF3, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kF3));
        addIndex(VectorAttribute.kF4, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kF4));
        addIndex(VectorAttribute.kF5, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kF5));
        addIndex(VectorAttribute.kUB, new VectorRangeIndex<Integer, VectorHandle>(VectorAttribute.kUB));
        addIndex(VectorAttribute.kLineIdNone, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kLineIdNone));
        addIndex(VectorAttribute.kSiteIdNone, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kSiteIdNone));
        addIndex(VectorAttribute.kCreativeIdNone, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kCreativeIdNone));
        addIndex(VectorAttribute.kBuyIdNone, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kBuyIdNone));
        addIndex(VectorAttribute.kAdIdNone, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kAdIdNone));
        addIndex(VectorAttribute.kStateNone, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kStateNone));
        addIndex(VectorAttribute.kZipNone, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kZipNone));
        addIndex(VectorAttribute.kDMANone, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kDMANone));
        addIndex(VectorAttribute.kAreaNone, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kAreaNone));
        addIndex(VectorAttribute.kCityNone, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kCityNone));
        addIndex(VectorAttribute.kCountryNone, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kCountryNone));
        addIndex(VectorAttribute.kT1None, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kT1None));
        addIndex(VectorAttribute.kT2None, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kT2None));
        addIndex(VectorAttribute.kT3None, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kT3None));
        addIndex(VectorAttribute.kT4None, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kT4None));
        addIndex(VectorAttribute.kT5None, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kT5None));
        addIndex(VectorAttribute.kF1None, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kF1None));
        addIndex(VectorAttribute.kF2None, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kF2None));
        addIndex(VectorAttribute.kF3None, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kF3None));
        addIndex(VectorAttribute.kF4None, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kF4None));
        addIndex(VectorAttribute.kF5None, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kF5None));
        addIndex(VectorAttribute.kUBNone, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kUBNone));
        addIndex(VectorAttribute.kDefault, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kDefault));
        addIndex(VectorAttribute.kDefaultNone, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kDefaultNone));
        addIndex(VectorAttribute.kExpId, new VectorDBIndex<Integer, VectorHandle>(VectorAttribute.kExpId));
    }

    public void addIndex(VectorAttribute aAttribute, AbstractIndex<VectorHandle, VectorAttribute, ?, VectorHandle> index) {
        m_indices.put(aAttribute, index);
    }

    public void deleteIndex(VectorAttribute aAttribute) {
        m_indices.remove(aAttribute);
    }

    public AbstractIndex getIndex(VectorAttribute aAttribute) {
        if (aAttribute==null){
            return null;
        }
        return m_indices.get(aAttribute);
    }

    public boolean hasIndex(VectorAttribute aAttribute) {
        return m_indices.containsKey(aAttribute);
    }

    public Enumeration<VectorAttribute> getIndices() {
        return m_indices.keys();
    }

    @SuppressWarnings("unchecked")
    public void updateRangeIndex(VectorAttribute type, TreeMap<Range<Integer>, ArrayList<VectorHandle>> mindex) {
        deleteRangeIndex(type, mindex);
        ((AbstractRangeIndex<VectorAttribute, Integer, VectorHandle>) m_indices.get(type)).add(mindex);
    }

    @SuppressWarnings("unchecked")
    public void deleteRangeIndex(VectorAttribute type, TreeMap<Range<Integer>, ArrayList<VectorHandle>> mindex) {
        ((AbstractRangeIndex<VectorAttribute, Integer, VectorHandle>) m_indices.get(type)).delete(mindex);
    }

    @SuppressWarnings("unchecked")
    public void updateIntegerIndex(VectorAttribute type, TreeMap<Integer, ArrayList<VectorHandle>> mindex) {
        deleteIntegerIndex(type, mindex);
        ((AbstractIndex<VectorHandle, VectorAttribute, Integer, VectorHandle>) m_indices.get(type)).add(mindex);
    }

    @SuppressWarnings("unchecked")
    public void deleteIntegerIndex(VectorAttribute type, TreeMap<Integer, ArrayList<VectorHandle>> mindex) {
        ((AbstractIndex<VectorHandle, VectorAttribute, Integer, VectorHandle>) m_indices.get(type)).delete(mindex);
    }

    @SuppressWarnings("unchecked")
    public void materializeRangeIndices() {
        for (VectorAttribute attr : VectorUtils.getRangeAttributes()) {
            ((VectorRangeIndex<Integer, VectorHandle>) m_indices.get(attr)).materialize();
        }
    }

    public void addRules(long id, SortedBag<Pair<CreativeSet, Double>> rules) {
        try {
            m_ruleMap.writerLock();
            m_ruleMap.put(id, rules);
        } finally {
            m_ruleMap.writerUnlock();
        }
    }

    public void deleteRules(long id) {
        try {
            m_ruleMap.writerLock();
            m_ruleMap.remove(id);
        } finally {
            m_ruleMap.writerUnlock();
        }
    }

    public void addClause(long id, SortedBag<Pair<ListingClause, Double>> rules) {
        try {
            m_clauseMap.writerLock();
            m_clauseMap.put(id, rules);
        } finally {
            m_clauseMap.writerUnlock();
        }
    }

    public void deleteClause(long id) {
        try {
            m_clauseMap.writerLock();
            m_clauseMap.remove(id);
        } finally {
            m_clauseMap.writerUnlock();
        }
    }

    /**
     * Add the new products into the database.
     */
    public void addOpsNewHandles(SortedSet<VectorHandle> newHandles) {
        //First delete all old handles from indexes
        deleteOldKeys(VectorHandle.OPTIMIZATION, newHandles);
        try {
            m_allOptHandles.writerLock();
            m_allOptHandles.clear();
            m_allOptHandles.addAll(newHandles);
        } finally {
            m_allOptHandles.writerUnlock();
        }
    }

    public void addPersNewHandles(SortedSet<VectorHandle> newHandles) {
        //First delete all old handles from indexes
        deleteOldKeys(VectorHandle.PERSONALIZATON, newHandles);
        try {
            m_allPersHandles.writerLock();
            m_allPersHandles.clear();
            m_allPersHandles.addAll(newHandles);
        } finally {
            m_allPersHandles.writerUnlock();
        }
    }

    public void addDefNewHandles(SortedSet<VectorHandle> newHandles) {
        //First delete all old handles from indexes
        deleteOldKeys(VectorHandle.DEFAULT, newHandles);
        try {
            m_allDefHandles.writerLock();
            m_allDefHandles.clear();
            m_allDefHandles.addAll(newHandles);
        } finally {
            m_allDefHandles.writerUnlock();
        }
    }

    public SortedBag<Pair<CreativeSet, Double>> getRules(long id) {
        SortedBag<Pair<CreativeSet, Double>> rules = null;
        try {
            m_ruleMap.readerLock();
            rules = m_ruleMap.get(id);
        } finally {
            m_ruleMap.readerUnlock();
        }
        return rules;
    }

    public SortedBag<Pair<ListingClause, Double>> getClauses(long id) {
        SortedBag<Pair<ListingClause, Double>> rules = null;
        try {
            m_clauseMap.readerLock();
            rules = m_clauseMap.get(id);
        } finally {
            m_clauseMap.readerUnlock();
        }
        return rules;
    }


    /**
     * Get Handle without checking a lock, reader should call readerLock()
     * Check if the prod exists - else return null
     *
     * @param id
     * @return Handle
     */
    public VectorHandle getVectorHandle(long id, int type) {
        VectorHandle p = new VectorHandleImpl(id);
        Handle ph = null;
        switch(type) {
            case 0:
                try {
                    m_allDefHandles.readerLock();
                    ph = m_allDefHandles.find(p);
                } finally {
                    m_allDefHandles.readerUnlock();
                }
                break;
            case 1:
                try {
                    m_allOptHandles.readerLock();
                    ph = m_allOptHandles.find(p);
                } finally {
                    m_allOptHandles.readerUnlock();
                }
                break;
            case 2:
                try {
                    m_allPersHandles.readerLock();
                    ph = m_allPersHandles.find(p);
                } finally {
                    m_allPersHandles.readerUnlock();
                }
                break;

            default:
                break;
        }

        if (ph != null) {
            p = (VectorHandle) ph;
        } else {
            p = null;
        }
        return p;
    }

    public VectorHandle getVectorHandle(int expId, int vecId, int type) {
        VectorHandle p = new VectorHandleImpl(expId, vecId);
        return getVectorHandle(p.getOid(), type);
    }


    public Iterator<VectorHandle> getAllDefHandles() {
        return m_allDefHandles.iterator();
    }

    public Iterator<VectorHandle> getAllPersHandles() {
        return m_allPersHandles.iterator();
    }

    public Iterator<VectorHandle> getAllOptHandles() {
        return m_allOptHandles.iterator();
    }

    /**
     * Delete the given items from the relevant indexes
     * @param type
     * @param allHandles
     */
    public void deleteOldKeys(int type, SortedSet<VectorHandle> allHandles) {
        if (allHandles.isEmpty()) {
            return;
        }
        Set<VectorHandle> deletedHandles = new SortedArraySet<VectorHandle>();

        switch(type) {
            case 0:
                SortedSet<VectorHandle> currDefHandles = new SortedArraySet<VectorHandle>();
                try {
                    m_allDefHandles.readerLock();
                    currDefHandles.addAll(m_allDefHandles);
                } finally {
                    m_allDefHandles.readerUnlock();
                }
                deletedHandles = new SetDifference<VectorHandle>(currDefHandles, allHandles);
                break;
            case 1:
                SortedSet<VectorHandle> currOptHandles = new SortedArraySet<VectorHandle>();
                try {
                    m_allOptHandles.readerLock();
                    currOptHandles.addAll(m_allOptHandles);
                } finally {
                    m_allOptHandles.readerUnlock();
                }
                deletedHandles = new SetDifference<VectorHandle>(currOptHandles, allHandles);
                break;
            case 2:
                SortedSet<VectorHandle> currPerHandles = new SortedArraySet<VectorHandle>();
                try {
                    m_allPersHandles.readerLock();
                    currPerHandles.addAll(m_allPersHandles);
                } finally {
                    m_allPersHandles.readerUnlock();
                }
                deletedHandles = new SetDifference<VectorHandle>(currPerHandles, allHandles);
                break;
            default:
        }

        for (VectorHandle h : deletedHandles) {
            Map<VectorAttribute, List<Integer>> cmap = ((VectorHandleImpl)h).getContextMap();
            for (VectorAttribute attr: cmap.keySet()) {
                List<Integer> values = cmap.get(attr);
                if (values!=null) {
                    for (Integer i :values) {
                        ArrayList<VectorHandle> deletedList = new ArrayList<VectorHandle>();
                        deletedList.add(h);
                        if (VectorUtils.getRangeAttributes().contains(attr)) {
                            Range<Integer> r = new Range<Integer>(i, i);
                            TreeMap<Range<Integer>, ArrayList<VectorHandle>> map = new TreeMap<Range<Integer>, ArrayList<VectorHandle>>();
                            map.put(r, deletedList);
                            deleteRangeIndex(attr, map);
                        } else {
                            TreeMap<Integer, ArrayList<VectorHandle>> map = new TreeMap<Integer, ArrayList<VectorHandle>>();
                            map.put(i, deletedList);
                            deleteIntegerIndex(attr,map);
                        }
                    }
                }
            }
            deleteClause(h.getOid());
            deleteRules(h.getOid());
        }
    }


    public int getNumHandles() {
        return m_allDefHandles.size() + m_allOptHandles.size() + m_allPersHandles.size();
    }

}
