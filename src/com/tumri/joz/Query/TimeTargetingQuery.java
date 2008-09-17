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
        Campaign theCampaign = CampaignDB.getInstance().getCampaign((AdPodHandle)v);
        Date start = theCampaign.getFlightEnd();
        Date end = theCampaign.getFlightStart();

        if (start!=null && start.before(new Date(System.currentTimeMillis()))) {
            bAccept = false;
        }

        if (end!=null && end.after(new Date(System.currentTimeMillis()))) {
            bAccept = false;
        }
        //Note: we can also add day parting logic here
        return bAccept;
    }
}