package com.tumri.joz.campaign.wm;

import com.tumri.cma.domain.CAM;
import com.tumri.cma.domain.Campaign;
import com.tumri.cma.domain.Experience;
import com.tumri.cma.rules.CreativeInstance;
import com.tumri.cma.rules.CreativeSelector;
import com.tumri.cma.rules.CreativeSet;
import com.tumri.joz.Query.AdPodSetIntersector;
import com.tumri.joz.Query.VectorSetIntersector;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.rules.ListingClause;
import com.tumri.joz.index.Range;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.products.Handle;
import com.tumri.joz.ranks.IWeight;
import com.tumri.utils.Pair;
import com.tumri.utils.data.*;
import com.tumri.utils.index.AbstractIndex;
import com.tumri.utils.stats.PerformanceStats;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Class that will select a creative instance given a valid request
 */
public class VectorTargetingProcessor {

	private static final Logger log = Logger.getLogger(VectorTargetingProcessor.class);
	private static VectorTargetingProcessor processor = null;
	private static final String PROCESS_STATS_ID = "RS";


	public static VectorTargetingProcessor getInstance() {
		if (processor == null) {
			synchronized (VectorTargetingProcessor.class) {
				if (processor == null) {
					processor = new VectorTargetingProcessor();
				}
			}
		}
		return processor;
	}

	@SuppressWarnings("unchecked")
	/**
	 * Do the selection of recipe given the request
	 *
	 * @param request - request
	 * @return
	 */
	public VectorTargetingResult processRequest(int adpodId, int expId, CAM cam, AdDataRequest request, Features features)
			throws VectorSelectionException {
		PerformanceStats.getInstance().registerStartEvent(PROCESS_STATS_ID);
		boolean onlyDefault = false;
		if (expId > -1) {
			//Only for TC Campaigns
			Experience exp = CampaignDB.getInstance().getExperience(expId);
			if (exp != null) {
				Campaign camp = CampaignDB.getInstance().getCampaign(exp.getCampaignId());
				//If optimizeCTR is ON, then we will also look for other request context to optimize
				onlyDefault = !camp.isOptimizeCTR();
			}
		}
		SortedSet<Handle> matchingVectors = null;
		VectorTargetingResult vtr = new VectorTargetingResult();
		if (!onlyDefault) {
			Map<VectorAttribute, List<Integer>> contextMap = VectorUtils.getContextMap(adpodId, expId, request);
			SortedSet<Handle> resVectors = getMatchingVectors(contextMap);
			VectorHandleImpl tmpVH = new VectorHandleImpl(0L);
			matchingVectors = new SortedArraySet<Handle>(resVectors, tmpVH.new ImmutableVectorHandle(0.0));
		}

		CreativeSelector cs = cam.getSelector();
		CreativeSet cur = cam.getAllCreatives();
		ArrayList<Integer> vectorIdList = new ArrayList<Integer>(); //vectorids that have rules
		ArrayList<Integer> tmpVectorIdList = new ArrayList<Integer>(); //tmp array to help build vectorIdList
		ArrayList<Integer> lcVectorIdList = new ArrayList<Integer>(); //vectorids that have listing-clauses
		boolean skipRules = false;
		ListingClause lc = null;
		Random r = new Random();

		if (matchingVectors != null && !matchingVectors.isEmpty()) {
			List<SortedBag<Pair<CreativeSet, Double>>> rules = new ArrayList<SortedBag<Pair<CreativeSet, Double>>>();
			double prevScore = 0.0;
			for (Handle h : matchingVectors) {
				//Get the Listing clause details
				int[] dets = VectorHandleImpl.getIdDetails(h.getOid());
				if (dets[1] != adpodId && dets[1] != expId) {
					throw new VectorSelectionException("Selected handle did not match the incoming adpod or exp:"
							+ dets[1] + ":" + adpodId + "/" + expId);
				}
				{
					SortedBag<Pair<ListingClause, Double>> clauses = VectorDB.getInstance().getClauses(h.getOid());
					if (clauses != null && !clauses.isEmpty()) {
						try {
							if (clauses instanceof RWLocked) {
								((RWLocked) clauses).readerLock();
							}
							lcVectorIdList.add(dets[0]);
							lc = selectClause(lc, clauses);
						} finally {
							if (clauses instanceof RWLocked) {
								((RWLocked) clauses).readerUnlock();
							}
						}
					}
				}
				//Now check the creative rules
				if (!skipRules) {
					VectorHandle vector = (VectorHandle) h;
					double currentScore = vector.getScore();
					if (prevScore > 0 && (prevScore != currentScore) && !rules.isEmpty()) {
						while (cur.size() > 1 && rules.size() > 0) {
							int i = r.nextInt(rules.size());
							SortedBag<Pair<CreativeSet, Double>> tmpBag = rules.get(i);
							cur = cs.applyRules(tmpBag, cur);
							rules.remove(i);
							vectorIdList.add(tmpVectorIdList.get(i));
							tmpVectorIdList.remove(i);
						}
					}
					if (cur.size() == 1) {
						rules.clear();
						tmpVectorIdList.clear();
						skipRules = true;
					} else {
						prevScore = currentScore;
						{
							SortedBag<Pair<CreativeSet, Double>> trules = VectorDB.getInstance().getRules(h.getOid());
							if (trules != null && !trules.isEmpty()) {
								try {
									if (trules instanceof RWLocked) {
										((RWLocked) trules).readerLock();
									}
									tmpVectorIdList.add(dets[0]);
									rules.add(trules);
								} finally {
									if (trules instanceof RWLocked) {
										((RWLocked) trules).readerUnlock();
									}
								}
							} else {
								if (dets[0] != 1) {
									//This is not a default vector handle, and we dont have rules for it.
									log.warn("Rules not found for handle : " + h.getOid());
								}
							}
						}
					}
				}
			}

			if (rules != null && !rules.isEmpty() && !skipRules) {
				while (cur.size() > 1 && rules.size() > 0) {
					int i = r.nextInt(rules.size());
					SortedBag<Pair<CreativeSet, Double>> tmpBag = rules.get(i);
					cur = cs.applyRules(tmpBag, cur);
					rules.remove(i);
					vectorIdList.add(tmpVectorIdList.get(i));
					tmpVectorIdList.remove(i);
				}
			}
		}
		if (!vectorIdList.isEmpty()) {
			features.addFeatureDetail("RWM-ID", vectorIdList.toString());
		}

		if (!lcVectorIdList.isEmpty()) {
			features.addFeatureDetail("LC-WM-ID", lcVectorIdList.toString());
		}
		CreativeInstance ci = null;

		try {
			ci = cs.select(cur);
			vtr.setCi(ci);
		} catch (Exception e) {
			log.warn("Could not select a viable instance from the cam", e);
		}
		if (ci == null) {
			throw new VectorSelectionException("Vector Selection failed for this request");
		}
		if (lc != null) {
			vtr.setLc(lc);
		}
		return vtr;
	}

	@SuppressWarnings("unchecked")
	public SortedSet<Handle> getMatchingVectors(Map<VectorAttribute, List<Integer>> contextMap) {

		//Add context matches
		VectorSetIntersector intersector = new VectorSetIntersector(true);
		if (contextMap != null) {
			//Because of MVF we need to construct a VectorHandle with a map that contains just the keys from the contextMap
			VectorHandle rv = new VectorHandleImpl(0, 0, VectorHandleImpl.OPTIMIZATION, contextMap, true);
			Set<VectorAttribute> keys = contextMap.keySet();
			for (VectorAttribute attr : keys) {

				SortedSet<Handle> vectors = getVectorsFromIndex(attr, contextMap);
				if (vectors != null && vectors.size() > 0) {
					//Build intersector
					IWeight<Handle> wt = new VectorAttributeWeights(rv, attr);
					intersector.include(vectors, wt);
				}

			}
		}
		intersector.useTopK(true);
		return intersector.intersect();
	}

	/**
	 * Select one of the listing clause and "merge" with the existing main one.
	 */
	private ListingClause selectClause(ListingClause mainClause, SortedBag<Pair<ListingClause, Double>> bag) {
		if (bag != null) {
			Random r = new Random();
			List<ListingClause> list = new ArrayList<ListingClause>();
			Iterator<SortedBag.Group<Pair<ListingClause, Double>>> groups = bag.groupBy(null);
			while (groups.hasNext()) {
				SortedBag.Group<Pair<ListingClause, Double>> group = groups.next();
				Pair<ListingClause, Double> key = group.key();
				double score = 0;
				while (group.hasNext()) {
					score += group.next().getSecond();
				}
				group = bag.getGroup(key);
				double rand = (r.nextInt(1000) * score) / 1000;
				double wt = 0;
				while (group.hasNext()) {
					Pair<ListingClause, Double> pair = group.next();
					wt += pair.getSecond();
					if (wt >= rand || !group.hasNext()) {
						list.add(pair.getFirst());
						break;
					}
				}
			}
			if (!list.isEmpty()) {
				for (ListingClause clause : list) {
					if (mainClause == null) {
						mainClause = new ListingClause(clause);
					} else {
						mainClause.merge(clause);
					}
				}
			}
		}
		return mainClause;
	}

	/**
	 * For a given attribute, for each value, look up the handles from the index and add it to a return SortedSet
	 *
	 * @param attr
	 * @param contextMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private SortedSet<Handle> getVectorsFromIndex(VectorAttribute attr, Map<VectorAttribute, List<Integer>> contextMap) {
		MultiSortedSet<Handle> vectors = null;
		AbstractIndex idx = VectorDB.getInstance().getIndex(attr);
		VectorAttribute kNone = VectorUtils.getNoneAttribute(attr);
		AbstractIndex noneidx = VectorDB.getInstance().getIndex(kNone);

		if (idx != null) {
			List<Integer> contextVals = contextMap.get(attr);
			for (Integer contextVal : contextVals) {
				if (vectors == null) {
					vectors = new MultiSortedSet<Handle>();
				}
				SortedSet<Handle> fromIdx = null;
				if (VectorUtils.getRangeAttributes().contains(attr)) {
					//this lookup of dict value is necessary for range queries.
					String ubValS = VectorUtils.getDictValue(attr, contextVal);
					if (ubValS != null) { //if no userbucket is found within the dictionary.
						try {
							Integer ubVal = Integer.parseInt(ubValS);
							Range<Integer> r = new Range<Integer>(ubVal, ubVal);
							fromIdx = idx.get(r);
						} catch (NumberFormatException e) {
							log.error("Error: Non-Number received as user-bucket: " + ubValS);
						}
					}
				} else {
					fromIdx = idx.get(contextVal);
				}

				if (fromIdx != null && !fromIdx.isEmpty()) {
					vectors.add(fromIdx);
				}
				//Include the none list as well
				if (noneidx != null) {
					SortedSet<Handle> noneRes = noneidx.get(VectorUtils.getNoneDictId(kNone));
					vectors.add(noneRes);
				}
			}
		}
		return vectors;
	}


}
