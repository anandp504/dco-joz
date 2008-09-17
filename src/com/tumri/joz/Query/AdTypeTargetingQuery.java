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
 * Targeting Filter impementation for Adtype.
 *
 * @author nipun
 */
public class AdTypeTargetingQuery extends TargetingQuery {

    private String adType = null;
    private static final String DEFAULT_AD_TYPE = "mediumrectangle";

    public AdTypeTargetingQuery(String at) {
       if (at==null || at.equals("")) {
           at = DEFAULT_AD_TYPE;
       }
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
        AdPod aPod = CampaignDB.getInstance().getAdPod((AdPodHandle)v);
        String currAdType = aPod.getAdType();
        boolean bAccept = currAdType.equalsIgnoreCase(adType);
        return bAccept;
    }
}