package com.tumri.joz.Query;

import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.index.AdpodIndex;
import com.tumri.joz.products.Handle;
import com.tumri.utils.data.MultiSortedSet;

import java.util.SortedSet;

/**
 * Targeting query for age data
 *
 * @author nipun
 */
public class HHITargetingQuery extends TargetingQuery {
    private String hhi;

    public HHITargetingQuery(String hhi) {
        this.hhi = hhi;
    }

    public Type getType() {
        return Type.kHHI;
    }

    public SortedSet<Handle> exec() {
        SortedSet<Handle> hhiResult      = execAgeQuery();
        SortedSet<Handle> nonHHIResult   = execNonAgeQuery();

        MultiSortedSet<Handle> results = new MultiSortedSet<Handle>();
        if(hhiResult != null) {
            results.add(hhiResult);
        }
        if(nonHHIResult != null) {
            results.add(nonHHIResult);
        }

        return results;
    }

    private SortedSet<Handle> execAgeQuery() {
        if(hhi == null) {
            return null;
        }
        SortedSet<Handle> results;
        results = CampaignDB.getInstance().getAdpodHHIIndex().get(hhi);
        return results;
    }

    private SortedSet<Handle> execNonAgeQuery() {
        return CampaignDB.getInstance().getAdpodHHINoneIndex().get(AdpodIndex.HHI_NONE);
    }

    public boolean accept(Handle v) {
        return false;
    }
}