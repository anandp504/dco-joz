package com.tumri.joz.campaign.wm;

import com.tumri.cma.domain.*;
import com.tumri.cma.rules.CreativeInstance;
import com.tumri.cma.rules.CreativeSelector;
import com.tumri.cma.rules.CreativeSet;
import com.tumri.joz.Query.AdPodSetIntersector;
import com.tumri.joz.campaign.AdPodHandle;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.index.Range;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.products.Handle;
import com.tumri.joz.ranks.IWeight;
import com.tumri.joz.rules.ListingClause;
import com.tumri.utils.Pair;
import com.tumri.utils.data.MultiSortedSet;
import com.tumri.utils.data.RWLocked;
import com.tumri.utils.data.SortedArraySet;
import com.tumri.utils.data.SortedBag;
import com.tumri.utils.index.AbstractIndex;
import com.tumri.utils.stats.PerformanceStats;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * User: scbraun
 * Date: May 2, 2011
 * Time: 11:26:30 AM
 */
public class ExperienceVectorTargetingProcessor {

	private static final Logger log = Logger.getLogger(VectorTargetingProcessor.class);
	private static ExperienceVectorTargetingProcessor processor = null;
	private static final String PROCESS_STATS_ID = "RS";

	public static ExperienceVectorTargetingProcessor getInstance() {
		if (processor == null) {
			synchronized (ExperienceVectorTargetingProcessor.class) {
				if (processor == null) {
					processor = new ExperienceVectorTargetingProcessor();
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
	public Experience processRequest(int adpodId, AdDataRequest request, boolean isCTROpt, Features features)
			throws VectorSelectionException {
		PerformanceStats.getInstance().registerStartEvent(PROCESS_STATS_ID);
		boolean onlyDefault = false;
		AdPod adpod = null;
		if (adpodId > -1) {
			//Only for TC Campaigns
			adpod = CampaignDB.getInstance().getAdPod(adpodId);
			if (adpod != null) {
				//If optimizeCTR is ON, then we will also look for other request context to optimize
				onlyDefault = !isCTROpt;
			}
		}
		SortedSet<Handle> matchingVectors = null;
		VectorTargetingResult vtr = new VectorTargetingResult();
		if (!onlyDefault && adpod != null && adpod.getAdPodExperienceLocationMappings() != null) {
			Map<VectorAttribute, List<Integer>> contextMap = VectorUtils.getContextMap(adpodId, -1, request);
			SortedSet<Handle> resVectors = getMatchingVectors(contextMap);
			matchingVectors = new SortedArraySet<Handle>(resVectors, new VectorHandleImpl(0L));
		}

		boolean skipRules = false;
		Experience experience = null;
		Random r = new Random();

		Handle h = null;
		if (matchingVectors != null && !matchingVectors.isEmpty()) {
			double totalScore = 0.0;
			Iterator<Handle> iter = matchingVectors.iterator();
			double prevScore = 0.0;
			List<Handle> groupedHandles = new ArrayList<Handle>();
			boolean firstPass = true;
			while (iter.hasNext()) {
				Handle tmph = iter.next();
				double currScore = tmph.getScore();
				if (prevScore != currScore && !firstPass) {
					break;
				}
				firstPass = false;
				groupedHandles.add(tmph);
				prevScore = currScore;
			}
			int index = r.nextInt(groupedHandles.size());
			h = groupedHandles.get(index);
		}
		if (h != null) {
			SortedBag<Pair<Integer, Double>> experiences = ExperienceVectorDB.getInstance().getRules(h.getOid());
			if (experiences != null && !experiences.isEmpty()) {
				features.addFeatureDetail("CROSS-EXP-WM-ID", VectorHandleImpl.getIdDetails(h.getOid())[0] + "");
				try {
					if (experiences instanceof RWLocked) {
						((RWLocked) experiences).readerLock();
					}
					experience = chooseExperiences(adpod, experiences);
				} finally {
					if (experiences instanceof RWLocked) {
						((RWLocked) experiences).readerUnlock();
					}
				}
			} else {
				experience = chooseExperiences(adpod, null);
			}
		} else {
			experience = chooseExperiences(adpod, null);
		}


		CreativeInstance ci = null;

		return experience;
	}

	private Experience chooseExperiences(AdPod adpod, SortedBag<Pair<Integer, Double>> optExpBag) {
		List<AdPodExperienceLocationMapping> origExperiences = adpod.getAdPodExperienceLocationMappings();
		Map<Integer, Double> tmpMap = new HashMap<Integer, Double>();

		if (optExpBag != null) {
			for (Pair<Integer, Double> pair : optExpBag) {
				tmpMap.put(pair.getFirst(), pair.getSecond());
			}
		}

		double totalScore = 0.0;
		Map<Integer, Double> tmpMap2 = new HashMap<Integer, Double>();
		Integer chosenKey = null;
		if (origExperiences != null) {
			for (AdPodExperienceLocationMapping mapping : origExperiences) {
				int expId = mapping.getExperienceId();
				Double score = tmpMap.get(expId);
				if (score == null) {
					score = mapping.getExperienceWeight();
				}
				tmpMap2.put(expId, score);
				totalScore += score;
			}

			Random r = new Random();
			double currScore = 0.0;
			double rand = (r.nextDouble() * totalScore);
			Set<Integer> keys = tmpMap2.keySet();
			for (Integer key : keys) {
				chosenKey = key;
				currScore += tmpMap2.get(key);
				if (currScore >= rand) {
					break;
				}
			}
		} else {
			chosenKey = adpod.getExperienceId();
		}

		Experience retExp = CampaignDB.getInstance().getExperience(chosenKey);
		return retExp;
	}

	@SuppressWarnings("unchecked")
	public SortedSet<Handle> getMatchingVectors(Map<VectorAttribute, List<Integer>> contextMap) {
		//Add context matches
		AdPodSetIntersector intersector = new AdPodSetIntersector(true);
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
		return intersector.intersect();
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
		AbstractIndex idx = ExperienceVectorDB.getInstance().getIndex(attr);
		VectorAttribute kNone = VectorUtils.getNoneAttribute(attr);
		AbstractIndex noneidx = ExperienceVectorDB.getInstance().getIndex(kNone);

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

}
