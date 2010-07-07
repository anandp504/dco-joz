package com.tumri.joz.campaign.wm;

import com.tumri.cma.domain.CAM;
import com.tumri.cma.rules.CreativeInstance;
import com.tumri.cma.rules.CreativeSelector;
import com.tumri.cma.rules.CreativeSet;
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

	/**
	 * Do the selection of recipe given the request
	 *
	 * @param request - request
	 * @return
	 */
	public VectorTargetingResult processRequest(int adpodId, CAM cam, AdDataRequest request, Features features)
			throws VectorSelectionException {
		PerformanceStats.getInstance().registerStartEvent(PROCESS_STATS_ID);
		Map<VectorAttribute, List<Integer>> contextMap = VectorUtils.getContextMap(adpodId, request);
		VectorTargetingResult vtr = new VectorTargetingResult();

		SortedSet<Handle> resVectors = getMatchingVectors(contextMap);
		SortedSet<Handle> matchingVectors = new SortedArraySet<Handle>(resVectors, new VectorHandleImpl(0L));

		CreativeSelector cs = cam.getSelector();
		CreativeSet cur = cam.getAllCreatives();
		ArrayList<Integer> vectorIdList = new ArrayList<Integer>();
		boolean skipRules = false;
		ListingClause lc = null;
		if (!matchingVectors.isEmpty()) {
			SortedBag<Pair<CreativeSet, Double>> rules = new SortedListBag<Pair<CreativeSet, Double>>();
			double prevScore = 0.0;
			VectorHandle prevHandle = null;
			for (Handle h : matchingVectors) {
				//Get the Listing clause details
				{
					SortedBag<Pair<ListingClause, Double>> clauses = VectorDB.getInstance().getClauses(h.getOid());
					if (clauses != null) {
						try {
							if (clauses instanceof RWLocked) {
								((RWLocked) clauses).readerLock();
							}
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
					if (prevScore > 0 && (prevScore != currentScore || !vector.isMatch(prevHandle)) && !rules.isEmpty()) {
						cur = cs.applyRules(rules, cur);
						rules = new SortedListBag<Pair<CreativeSet, Double>>();
					}
					int[] dets = VectorHandleImpl.getIdDetails(h.getOid());
					vectorIdList.add(dets[0]);
					{
						SortedBag<Pair<CreativeSet, Double>> trules = VectorDB.getInstance().getRules(h.getOid());
						try {
							if (trules instanceof RWLocked) {
								((RWLocked) trules).readerLock();
							}
							//TODO: Avoid addAll since it is expensive - use BagUnion ( need a RW locked version of it )
							rules.addAll(trules);

						} finally {
							if (trules instanceof RWLocked) {
								((RWLocked) trules).readerUnlock();
							}
						}
					}
					if (cur.size() == 1) {
						skipRules = true;
					} else {
						prevScore = currentScore;
						prevHandle = vector;
					}
				}
			}

			if (rules != null && !rules.isEmpty()) {
				cur = cs.applyRules(rules, cur);
			}
		}
		if (!vectorIdList.isEmpty()) {
			features.addFeatureDetail("RWM-ID", vectorIdList.toString());
		} else {
			log.warn("Could not select a Vector for the given request");
		}
		CreativeInstance ci = null;

		try {
			ci = cs.select(cur);
			vtr.setCi(ci);
		} catch (Exception e) {
			log.warn("Could not select a viable instance from the cam", e);
		}
		if (lc != null) {
			vtr.setLc(lc);
		}
		return vtr;
	}

	@SuppressWarnings("unchecked")
	private SortedSet<Handle> getMatchingVectors(Map<VectorAttribute, List<Integer>> contextMap) {

		//Add context matches
		VectorSetIntersector intersector = new VectorSetIntersector(true);
		if (contextMap != null) {
			Set<VectorAttribute> keys = contextMap.keySet();
			for (VectorAttribute attr : keys) {

				SortedSet<Handle> vectors = getVectorsFromIndex(attr, contextMap);
				if (vectors != null && vectors.size() > 0) {
					//Build intersector
					IWeight<Handle> wt = getHandleWeight(attr, contextMap);
					intersector.include(vectors, wt);
				}

			}
			//Include all other none sets
			Set<VectorAttribute> nonAttrs = VectorUtils.findNoneAttributes(contextMap.keySet());
			for (VectorAttribute na : nonAttrs) {
				AbstractIndex noneidx = VectorDB.getInstance().getIndex(na);
				SortedSet<Handle> noneRes = ((VectorDBIndex<Integer, Handle>) noneidx).get(VectorUtils.getNoneDictId(na));
				//Build intersector
				IWeight<Handle> wt = getHandleWeight(na, contextMap);
				intersector.include(noneRes, wt);
			}
		}
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
					try {
						Integer ubVal = Integer.parseInt(ubValS);
						Range<Integer> r = new Range<Integer>(ubVal, ubVal);
						fromIdx = idx.get(r);
					} catch (NumberFormatException e) {
						log.error("Error: Non-Number received as user-bucket: " + ubValS);
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

	/**
	 * Because of MVF we need to construct a VectorHandle with a map that contains just the keys from the contextMap
	 *
	 * @param attr
	 * @param contextMap
	 * @return
	 */
	private IWeight<Handle> getHandleWeight(VectorAttribute attr, Map<VectorAttribute, List<Integer>> contextMap) {
		VectorHandle rv = new VectorHandleImpl(0, 0, VectorHandleImpl.OPTIMIZATION, contextMap, true);
		IWeight<Handle> wt = new VectorAttributeWeights(rv, attr);
		return wt;
	}


}
