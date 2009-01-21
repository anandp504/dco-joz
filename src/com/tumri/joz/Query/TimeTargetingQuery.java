package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.index.AdpodIndex;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.UrlNormalizer;
import com.tumri.joz.campaign.AdPodHandle;
import com.tumri.joz.targeting.TargetingScoreHelper;
import com.tumri.utils.data.MultiSortedSet;
import com.tumri.utils.data.SortedArraySet;
import com.tumri.utils.data.RWLocked;
import com.tumri.cma.domain.AdPod;
import com.tumri.cma.domain.Campaign;

import java.util.*;

/**
 * Targeting Query for Url related data.
 *
 * @author nipun
 */
public class TimeTargetingQuery extends TargetingQuery {
    public Type getType() {
        return Type.kTime;
    }

    public SortedSet<Handle> exec() {
        throw new UnsupportedOperationException("Method no supported");
    }

    public boolean hasIndex() {
        return false;
    }

    public boolean mustMatch() {
        return true;
    }

    public boolean accept(Handle v) {
        boolean bAccept = true;
        if (v==null) {
            return false;
        }
        Campaign theCampaign = CampaignDB.getInstance().getCampaign((AdPodHandle)v);
        if (theCampaign==null){
            return false;
        }
        Date campaignStartDate = theCampaign.getFlightStart();
        Date campaignEndDate = theCampaign.getFlightEnd();
        
        if (campaignStartDate!=null && campaignStartDate.after(new Date(System.currentTimeMillis()))) {
            return false;
        }
        
        if (campaignEndDate!=null && campaignEndDate.before(new Date(System.currentTimeMillis()))) {
            return false;
        }

        AdPod theAdPod = CampaignDB.getInstance().getAdPod((AdPodHandle)v);
        Date adPodStartDate = theAdPod.getStartDate();
        Date adPodEndDate = theAdPod.getEndDate();
        
        if (adPodStartDate!=null && adPodStartDate.after(new Date(System.currentTimeMillis()))) {
            return false;
        }
        
        if (adPodEndDate!=null && adPodEndDate.before(new Date(System.currentTimeMillis()))) {
            return false;
        }
        
        //Note: we can also add day parting logic here
        return bAccept;
    }
}