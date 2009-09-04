/*
 * RecipeSelector.java
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

import com.tumri.cma.domain.Recipe;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.products.Handle;
import com.tumri.joz.ranks.IWeight;
import com.tumri.utils.stats.PerformanceStats;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Queries the WeightDB for the weight info matching the request
 * @author: nipun
 * Date: Aug 10, 2009
 * Time: 11:15:46 AM
 */
public class RecipeSelector {

    private static final Logger log = Logger.getLogger(RecipeSelector.class);
    private static RecipeSelector processor = null;
    public static final String PROCESS_STATS_ID = "RS";
    private static final String RWM_ID = "RWM-ID:";

    private RecipeSelector() {
    }

    public static RecipeSelector getInstance() {
        if (processor == null) {
            synchronized (RecipeSelector.class) {
                if (processor == null) {
                    processor = new RecipeSelector();
                }
            }
        }
        return processor;
    }

    /**
     * Do the selection of recipe given the request
     * @param adPodId - Current AdPod Id
     * @param contextMap - request Context Map
     * @return
     */
    public Recipe getRecipe(int adPodId, List<Recipe> recipeList, Map<WMIndex.Attribute, Integer> contextMap, Features features) {
        PerformanceStats.getInstance().registerStartEvent(PROCESS_STATS_ID);

        if (recipeList == null || recipeList.isEmpty()) {
            return null;
        }
        Map<Integer, Double> defWeights = new HashMap<Integer, Double>();

        boolean isDirty = false;
        boolean isLiO = false;
        for(Recipe r : recipeList) {
            //If any recipe is dirty then we do not do line id based selection
            if (r.isTestDirty() && !isDirty) {
                isDirty = true;
            }
            if (r.isLineIdOptimized() && !isLiO) {
                isLiO = true;
            }
            defWeights.put(r.getId(), r.getWeight());
        }

        List<WMHandle> listVectors = null;
        String rwmId = "DEFAULT";

        //Only optimize by line id if dirty flag is not set and isLiO flag is set
        if (!isDirty) {
            WMDB.WMIndexCache currWtDB = WMDB.getInstance().getWeightDB(adPodId);
            WMHandle rv = new WMHandle(0, contextMap, null);
            listVectors = getMatchingVectors(currWtDB,contextMap,rv);
        }
        if (listVectors != null && listVectors.size() >0) {
            List<RecipeWeight> recipeInfos = pickOneRecipeList(listVectors, features);

            for (RecipeWeight rw: recipeInfos) {
                if (defWeights.containsKey(rw.getRecipeId())) {
                    defWeights.put(rw.getRecipeId(), rw.getWeight());
                }
            }
            rwmId = features.getFeaturesDetail(RWM_ID);
        }

        features.addFeatureDetail("RWM_ID",rwmId);
        log.debug("Current context = " + rwmId);
        
        Recipe r = pickOneRecipe(recipeList, defWeights);
        PerformanceStats.getInstance().registerFinishEvent(PROCESS_STATS_ID, rwmId);

        return r;
    }

    private Recipe pickOneRecipe(List<Recipe> list, Map<Integer, Double> wtMap) {
        Recipe r = null;
        double totalWeight = 0;
        double weightRatio;
        double[] weightArray = new double[list.size()];
        for(int i=0; i<list.size(); i++) {
            Double wt = wtMap.get(list.get(i).getId());
            if (wt==null) {
                wt = 0.0;
            }
            weightArray[i] = Math.abs(wt);
            totalWeight += wt;
        }
        if(totalWeight <= 0) {
            log.warn("Total weight assigned to recipes is 0. Skipping Recipe selection");
            return null;
        }
        try {
            weightRatio = new Random().nextInt((int)totalWeight);
        }
        catch(IllegalArgumentException e) {
            weightRatio = 0;
            log.warn("Calculated totalWeight was not positive. totalWeight:" + totalWeight);
        }
        Arrays.sort(weightArray);
        double additionFactor = 0;

        for(Recipe aRecipe : list) {
            double weight = wtMap.get(aRecipe.getId());
            weight = weight + additionFactor;
            if(weight > weightRatio) {
                r = aRecipe;
                break;
            }
            additionFactor = weight;
        }
        return r;
    }

    /**
     * Use the given set of indexes to select the recipe
     * @param bestMatchAL - best matches
     * @param features - features
     * @return - List of recipe weigt
     */
    private List<RecipeWeight> pickOneRecipeList(List<WMHandle> bestMatchAL,Features features) {
        WMHandle bestMatch = null;
        if (bestMatchAL.isEmpty()) {
            return null;
        }  else if (bestMatchAL.size()==1) {
            bestMatch = bestMatchAL.get(0) ;
        }  else {
            int i = new Random().nextInt(bestMatchAL.size());
            bestMatch = bestMatchAL.get(i);
        }
        features.addFeatureDetail(RWM_ID,Long.toString(bestMatch.getOid()));
        return bestMatch.getRecipeList();
    }

    private List<WMHandle> getMatchingVectors(WMDB.WMIndexCache wtDB, Map<WMIndex.Attribute, Integer> contextMap, WMHandle rv) {
        List<WMHandle> res = null;

        //Add context matches
        WMSetIntersector intersector = new WMSetIntersector(true);
        if (contextMap!=null) {
            Set<WMIndex.Attribute> keys = contextMap.keySet();
            for (WMIndex.Attribute attr: keys) {
                WMIndex idx = wtDB.getIndex(attr);
                if (idx != null) {
                    SortedSet<Handle> vectors = idx.get(contextMap.get(attr));
                    if (vectors!=null && vectors.size()>0) {
                        //Build intersector
                        IWeight<Handle> wt = new WMAttributeWeights(rv, attr);
                        intersector.include(vectors,wt);
                    }
                }
            }
        }
        SortedSet<Handle> results = intersector.intersect();
        if (results!=null) {
            res = new ArrayList<WMHandle>();
            double score = 0.0;
            int i = 0;
            for (Handle h : results) {
                WMHandle vector = (WMHandle)h;
                double currentScore = vector.getScore();
                if(i == 0) {
                    score = currentScore;
                }
                else if(i > 0) {
                    if(score != currentScore) {
                        break;
                    }
                }
                i++;
                res.add(vector);
            }
        }
        return res;
    }


}
