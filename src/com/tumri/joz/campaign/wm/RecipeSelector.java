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

import com.tumri.utils.data.SortedArraySet;
import com.tumri.utils.stats.PerformanceStats;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.jozMain.Features;
import com.tumri.cma.domain.Recipe;

import java.util.*;

import org.apache.log4j.Logger;

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
     * @param requestMap - request Context Map
     * @return
     */
    public Recipe getRecipe(int adPodId, List<Recipe> recipeList, Map<WMIndex.Attribute, String> requestMap, Features features) {
        PerformanceStats.getInstance().registerStartEvent(PROCESS_STATS_ID);

        if (recipeList == null || recipeList.isEmpty()) {
            return null;
        }
        Map<Integer, RecipeWeight> defWeights = new HashMap<Integer, RecipeWeight>();
        List<RecipeWeight> allRecipeWeights = new ArrayList<RecipeWeight>();

        boolean isDirty = false;
        for(Recipe r : recipeList) {
            RecipeWeight rw = new RecipeWeight(r.getId(), r.getWeight());
            allRecipeWeights.add(rw);
            //If any recipe is dirty then we do not do line id based selection
            if (r.isTestDirty() && !isDirty) {
                isDirty = true;
            }
            defWeights.put(r.getId(), rw);
        }

        List<WMHandle> listVectors = null;
        String rwmId = "DEFAULT";
        Map<WMIndex.Attribute, Integer> contextMap = new HashMap<WMIndex.Attribute, Integer>();

        if (!isDirty) {
            WMDB.WMIndexCache currWtDB = WMDB.getInstance().getWeightDB(adPodId);
            if (requestMap!=null) {
                for (WMIndex.Attribute attr: requestMap.keySet()) {
                    String val = requestMap.get(attr);
                    Integer id = WMUtils.getDictId(attr, val);
                    if (id != null) {
                        contextMap.put(attr, WMUtils.getDictId(attr, val));
                    }
                }
            }
            listVectors = getMatchingVectors(currWtDB,contextMap);
        }
        if (listVectors != null) {
            WMHandle rv = WMHandleFactory.getInstance().getHandle(0, contextMap, null);
            List<RecipeWeight> recipeInfos = pickOneRecipeList(listVectors, rv, features);

            for (RecipeWeight rw: recipeInfos) {
                RecipeWeight r = defWeights.get(rw.getRecipeId());
                if (r!=null) {
                    r.setWeight(rw.getWeight());
                }
            }
            rwmId = features.getFeaturesDetail(RWM_ID);
        } else {
            log.warn("No matching vector found for incoming request, using default weights");
            features.addFeatureDetail("RWM_ID",rwmId);
        }
        Recipe r = pickOneRecipe(allRecipeWeights);
        PerformanceStats.getInstance().registerFinishEvent(PROCESS_STATS_ID, rwmId);

        return r;
    }

    private Recipe pickOneRecipe(List<RecipeWeight> list) {
        RecipeWeight r = null;
        double totalWeight = 0;
        double weightRatio;
        double[] weightArray = new double[list.size()];
        for(int i=0; i<list.size(); i++) {
            weightArray[i] = Math.abs(list.get(i).getWeight());
            totalWeight += list.get(i).getWeight();
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

        for(RecipeWeight aRecipe : list) {
            double weight = aRecipe.getWeight();
            weight = weight + additionFactor;
            if(weight > weightRatio) {
                r = aRecipe;
                break;
            }
            additionFactor = weight;
        }
        return CampaignDB.getInstance().getRecipe(r.getRecipeId());
    }

    /**
     * Use the given set of indexes to select the recipe
     * @param listVectors
     * @return
     */
    private List<RecipeWeight> pickOneRecipeList(List<WMHandle> listVectors, WMHandle rv,Features features) {
        //Do dot product and select max
        WMHandle bestMatch = null;
        ArrayList<WMHandle> bestMatchAL = new ArrayList<WMHandle>();
        SortedSet<WMHandle> sortedRv = new SortedArraySet<WMHandle>();
        for (WMHandle curr: listVectors) {
            double tmpScore = curr.dot(rv);
            WMHandle cloneRv = (WMHandle)curr.clone();
            cloneRv.setScore(tmpScore);
            sortedRv.add(cloneRv);
        }

        Iterator<WMHandle> iterator = sortedRv.iterator();
        double score = 0.0;
        if(iterator != null) {
            int i = 0;
            double currentScore;
            while(iterator.hasNext()) {
                WMHandle vector = iterator.next();
                currentScore = vector.getScore();
                if(i == 0) {
                    score = currentScore;
                }
                else if(i > 0) {
                    if(score != currentScore) {
                        break;
                    }
                }
                i++;
                bestMatchAL.add(vector);
            }
        }
        if (bestMatchAL.isEmpty()) {
            return null;
        }  else if (bestMatchAL.size()==1) {
            bestMatch = bestMatchAL.get(0) ;
        }  else {
            int i = new Random().nextInt(bestMatchAL.size());
            bestMatch = bestMatchAL.get(i);
        }
        features.addFeatureDetail(RWM_ID,new Long(bestMatch.getOid()).toString());
        return bestMatch.getRecipeList();
    }

    private List<WMHandle> getMatchingVectors(WMDB.WMIndexCache wtDB, Map<WMIndex.Attribute, Integer> contextMap) {
        List<WMHandle> res = null;

        //Add context matches
        if (contextMap!=null) {
            Set<WMIndex.Attribute> keys = contextMap.keySet();
            for (WMIndex.Attribute attr: keys) {
                WMIndex idx = wtDB.getIndex(attr);
                if (idx != null) {
                    SortedSet<WMHandle> vectors = idx.get(contextMap.get(attr));
                    if (vectors!=null && vectors.size()>0) {
                        res = new ArrayList<WMHandle>();
                        for (WMHandle rv: vectors) {
                            if (!res.contains(rv)){
                                res.add(rv);
                            }
                        }
                    }
                }

            }
        }

        return res;
    }


}
