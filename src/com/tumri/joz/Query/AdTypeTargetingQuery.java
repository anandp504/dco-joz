package com.tumri.joz.Query;

import com.tumri.cma.domain.AdPod;
import com.tumri.joz.campaign.AdPodHandle;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.products.Handle;

import java.util.SortedSet;

/**
 * Targeting Filter impementation for Adtype.
 *
 * @author nipun
 */
public class AdTypeTargetingQuery extends TargetingQuery {

    private String adType = null;

    public AdTypeTargetingQuery(String at) {
       this.adType = at;
    }
    
    public Type getType() {
        return Type.kAdType;
    }

    public SortedSet<Handle> exec() {
        throw new UnsupportedOperationException("Method not supported");
    }

    public boolean hasIndex() {
        return false;
    }

    public boolean mustMatch() {
        return true;
    }

    public boolean accept(Handle v) {
        if (adType==null||"".equals(adType)) {
            //If adtype is not specified in request, we select the adpod
            return true;
        }
        AdPod aPod = CampaignDB.getInstance().getAdPod((AdPodHandle)v);
        String currAdType = aPod.getAdType();
        return currAdType.equalsIgnoreCase(adType);
    }
}