package com.tumri.joz.campaign.wm;

import com.tumri.cma.rules.CreativeSet;
import com.tumri.joz.index.AbstractRangeIndex;
import com.tumri.joz.index.Range;
import com.tumri.joz.products.Handle;
import com.tumri.joz.rules.ListingClause;
import com.tumri.utils.Pair;
import com.tumri.utils.data.*;
import com.tumri.utils.index.AbstractIndex;

import java.util.*;

/**
 * User: scbraun
 * Date: Apr 27, 2011
 * Time: 12:46:13 PM
 */
public class ExperienceVectorDB {

	private static ExperienceVectorDB g_DB;

	public static ExperienceVectorDB getInstance() {
		if (g_DB == null) {
			synchronized (ExperienceVectorDB.class) {
				if (g_DB == null) {
					g_DB = new ExperienceVectorDB();
				}
			}
		}
		return g_DB;
	}

	private Hashtable<VectorAttribute, AbstractIndex<Handle, VectorAttribute, ?, Handle>> m_indices =
			new Hashtable<VectorAttribute, AbstractIndex<Handle, VectorAttribute, ?, Handle>>();

	private RWLockedSortedArraySet<VectorHandle> m_allOptHandles = new RWLockedSortedArraySet<VectorHandle>();

	private RWLockedSortedArraySet<VectorHandle> m_allDefHandles = new RWLockedSortedArraySet<VectorHandle>();

	private RWLockedTreeMap<Long, RWLockedSortedBag<Pair<Integer, Double>>> m_ruleMap =
			new RWLockedTreeMap<Long, RWLockedSortedBag<Pair<Integer, Double>>>();

	public ExperienceVectorDB() {
		addIndex(VectorAttribute.kAdpodId, new VectorDBIndex<Integer, Handle>(VectorAttribute.kAdpodId));
		addIndex(VectorAttribute.kLineId, new VectorDBIndex<Integer, Handle>(VectorAttribute.kLineId));
		addIndex(VectorAttribute.kSiteId, new VectorDBIndex<Integer, Handle>(VectorAttribute.kSiteId));
		addIndex(VectorAttribute.kCreativeId, new VectorDBIndex<Integer, Handle>(VectorAttribute.kCreativeId));
		addIndex(VectorAttribute.kBuyId, new VectorDBIndex<Integer, Handle>(VectorAttribute.kBuyId));
		addIndex(VectorAttribute.kAdId, new VectorDBIndex<Integer, Handle>(VectorAttribute.kAdId));
		addIndex(VectorAttribute.kState, new VectorDBIndex<Integer, Handle>(VectorAttribute.kState));
		addIndex(VectorAttribute.kZip, new VectorDBIndex<Integer, Handle>(VectorAttribute.kZip));
		addIndex(VectorAttribute.kDMA, new VectorDBIndex<Integer, Handle>(VectorAttribute.kDMA));
		addIndex(VectorAttribute.kArea, new VectorDBIndex<Integer, Handle>(VectorAttribute.kArea));
		addIndex(VectorAttribute.kCity, new VectorDBIndex<Integer, Handle>(VectorAttribute.kCity));
		addIndex(VectorAttribute.kCountry, new VectorDBIndex<Integer, Handle>(VectorAttribute.kCountry));
		addIndex(VectorAttribute.kT1, new VectorDBIndex<Integer, Handle>(VectorAttribute.kT1));
		addIndex(VectorAttribute.kT2, new VectorDBIndex<Integer, Handle>(VectorAttribute.kT2));
		addIndex(VectorAttribute.kT3, new VectorDBIndex<Integer, Handle>(VectorAttribute.kT3));
		addIndex(VectorAttribute.kT4, new VectorDBIndex<Integer, Handle>(VectorAttribute.kT4));
		addIndex(VectorAttribute.kT5, new VectorDBIndex<Integer, Handle>(VectorAttribute.kT5));
		addIndex(VectorAttribute.kF1, new VectorDBIndex<Integer, Handle>(VectorAttribute.kF1));
		addIndex(VectorAttribute.kF2, new VectorDBIndex<Integer, Handle>(VectorAttribute.kF2));
		addIndex(VectorAttribute.kF3, new VectorDBIndex<Integer, Handle>(VectorAttribute.kF3));
		addIndex(VectorAttribute.kF4, new VectorDBIndex<Integer, Handle>(VectorAttribute.kF4));
		addIndex(VectorAttribute.kF5, new VectorDBIndex<Integer, Handle>(VectorAttribute.kF5));
		addIndex(VectorAttribute.kUB, new VectorRangeIndex<Integer, Handle>(VectorAttribute.kUB));
		addIndex(VectorAttribute.kAge, new VectorDBIndex<Integer, Handle>(VectorAttribute.kAge));
		addIndex(VectorAttribute.kGender, new VectorDBIndex<Integer, Handle>(VectorAttribute.kGender));
		addIndex(VectorAttribute.kBT, new VectorDBIndex<Integer, Handle>(VectorAttribute.kBT));
		addIndex(VectorAttribute.kHHI, new VectorDBIndex<Integer, Handle>(VectorAttribute.kHHI));
		addIndex(VectorAttribute.kChildCount, new VectorDBIndex<Integer, Handle>(VectorAttribute.kChildCount));
		addIndex(VectorAttribute.kLineIdNone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kLineIdNone));
		addIndex(VectorAttribute.kSiteIdNone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kSiteIdNone));
		addIndex(VectorAttribute.kCreativeIdNone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kCreativeIdNone));
		addIndex(VectorAttribute.kBuyIdNone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kBuyIdNone));
		addIndex(VectorAttribute.kAdIdNone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kAdIdNone));
		addIndex(VectorAttribute.kStateNone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kStateNone));
		addIndex(VectorAttribute.kZipNone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kZipNone));
		addIndex(VectorAttribute.kDMANone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kDMANone));
		addIndex(VectorAttribute.kAreaNone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kAreaNone));
		addIndex(VectorAttribute.kCityNone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kCityNone));
		addIndex(VectorAttribute.kCountryNone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kCountryNone));
		addIndex(VectorAttribute.kT1None, new VectorDBIndex<Integer, Handle>(VectorAttribute.kT1None));
		addIndex(VectorAttribute.kT2None, new VectorDBIndex<Integer, Handle>(VectorAttribute.kT2None));
		addIndex(VectorAttribute.kT3None, new VectorDBIndex<Integer, Handle>(VectorAttribute.kT3None));
		addIndex(VectorAttribute.kT4None, new VectorDBIndex<Integer, Handle>(VectorAttribute.kT4None));
		addIndex(VectorAttribute.kT5None, new VectorDBIndex<Integer, Handle>(VectorAttribute.kT5None));
		addIndex(VectorAttribute.kF1None, new VectorDBIndex<Integer, Handle>(VectorAttribute.kF1None));
		addIndex(VectorAttribute.kF2None, new VectorDBIndex<Integer, Handle>(VectorAttribute.kF2None));
		addIndex(VectorAttribute.kF3None, new VectorDBIndex<Integer, Handle>(VectorAttribute.kF3None));
		addIndex(VectorAttribute.kF4None, new VectorDBIndex<Integer, Handle>(VectorAttribute.kF4None));
		addIndex(VectorAttribute.kF5None, new VectorDBIndex<Integer, Handle>(VectorAttribute.kF5None));
		addIndex(VectorAttribute.kUBNone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kUBNone));
		addIndex(VectorAttribute.kAgeNone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kAgeNone));
		addIndex(VectorAttribute.kGenderNone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kGenderNone));
		addIndex(VectorAttribute.kBTNone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kBTNone));
		addIndex(VectorAttribute.kHHINone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kHHINone));
		addIndex(VectorAttribute.kChildCountNone, new VectorDBIndex<Integer, Handle>(VectorAttribute.kChildCountNone));
		addIndex(VectorAttribute.kExpId, new VectorDBIndex<Integer, Handle>(VectorAttribute.kExpId));
	}

	private void addIndex(VectorAttribute aAttribute, AbstractIndex<Handle, VectorAttribute, ?, Handle> index) {
		m_indices.put(aAttribute, index);
	}

	private void deleteIndex(VectorAttribute aAttribute) {
		m_indices.remove(aAttribute);
	}

	public AbstractIndex getIndex(VectorAttribute aAttribute) {
		if (aAttribute == null) {
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
	public void updateRangeIndex(VectorAttribute type, TreeMap<Range<Integer>, ArrayList<Handle>> mindex) {
		deleteRangeIndex(type, mindex);
		((AbstractRangeIndex<VectorAttribute, Integer, Handle>) m_indices.get(type)).add(mindex);
	}

	@SuppressWarnings("unchecked")
	public void deleteRangeIndex(VectorAttribute type, TreeMap<Range<Integer>, ArrayList<Handle>> mindex) {
		((AbstractRangeIndex<VectorAttribute, Integer, Handle>) m_indices.get(type)).delete(mindex);
	}

	@SuppressWarnings("unchecked")
	public void updateIntegerIndex(VectorAttribute type, TreeMap<Integer, ArrayList<Handle>> mindex) {
		deleteIntegerIndex(type, mindex);
		((AbstractIndex<Handle, VectorAttribute, Integer, Handle>) m_indices.get(type)).add(mindex);
	}

	@SuppressWarnings("unchecked")
	public void deleteIntegerIndex(VectorAttribute type, TreeMap<Integer, ArrayList<Handle>> mindex) {
		((AbstractIndex<Handle, VectorAttribute, Integer, Handle>) m_indices.get(type)).delete(mindex);
	}

	@SuppressWarnings("unchecked")
	public void materializeRangeIndices() {
		for (VectorAttribute attr : VectorUtils.getRangeAttributes()) {
			((VectorRangeIndex<Integer, Handle>) m_indices.get(attr)).materialize();
		}
	}

	public void addRules(long id, SortedBag<Pair<Integer, Double>> rules) {
		RWLockedSortedBag<Pair<Integer, Double>> newRules = new RWLockedSortedListBag<Pair<Integer, Double>>();
		newRules.addAll(rules);
		try {
			m_ruleMap.writerLock();
			m_ruleMap.put(id, newRules);
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


	public SortedBag<Pair<Integer, Double>> getRules(long id) {
		SortedBag<Pair<Integer, Double>> rules = null;
		try {
			m_ruleMap.readerLock();
			rules = m_ruleMap.get(id);
		} finally {
			m_ruleMap.readerUnlock();
		}
		return rules;
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

	/**
	 * Check if the prod exists - else return null
	 *
	 * @param id
	 * @return Handle
	 */
	public VectorHandle getVectorHandle(long id, int type) {
		VectorHandle p = new VectorHandleImpl(id);
		Handle ph = null;
		switch (type) {
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
		List<VectorHandle> res = new ArrayList<VectorHandle>();
		try {
			m_allDefHandles.readerLock();
			res.addAll(m_allDefHandles);
		} finally {
			m_allDefHandles.readerUnlock();
		}
		return res.iterator();
	}

	public Iterator<VectorHandle> getAllOptHandles() {
		List<VectorHandle> res = new ArrayList<VectorHandle>();
		try {
			m_allOptHandles.readerLock();
			res.addAll(m_allOptHandles);
		} finally {
			m_allOptHandles.readerUnlock();
		}
		return res.iterator();
	}

	/**
	 * Delete the given items from the relevant indexes
	 *
	 * @param type
	 * @param allHandles
	 */
	public void deleteOldKeys(int type, SortedSet<VectorHandle> allHandles) {
		if (allHandles.isEmpty()) {
			return;
		}
		Set<VectorHandle> deletedHandles = new SortedArraySet<VectorHandle>();

		switch (type) {
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
			default:
		}

		for (VectorHandle h : deletedHandles) {
			Map<VectorAttribute, List<Integer>> cmap = h.getContextMap();
			for (VectorAttribute attr : cmap.keySet()) {
				List<Integer> values = cmap.get(attr);
				if (values != null) {
					for (Integer i : values) {
						ArrayList<Handle> deletedList = new ArrayList<Handle>();
						deletedList.add(h);
						if (VectorUtils.getRangeAttributes().contains(attr)) {
							String s = VectorUtils.getDictValue(attr, i);
							List<String> list = VectorUtils.getParsedUniqueIntRangeString(s);
							if (list != null && list.size() == 2) {
								int min = Integer.parseInt(list.get(0));
								int max = Integer.parseInt(list.get(1));
								Range<Integer> r = new Range<Integer>(min, max);
								TreeMap<Range<Integer>, ArrayList<Handle>> map = new TreeMap<Range<Integer>, ArrayList<Handle>>();
								map.put(r, deletedList);
								deleteRangeIndex(attr, map);
							}
						} else {
							TreeMap<Integer, ArrayList<Handle>> map = new TreeMap<Integer, ArrayList<Handle>>();
							map.put(i, deletedList);
							deleteIntegerIndex(attr, map);
						}
					}
				}
			}
			deleteRules(h.getOid());
		}
	}


	public int getNumHandles() {
		return m_allDefHandles.size() + m_allOptHandles.size();
	}


}
