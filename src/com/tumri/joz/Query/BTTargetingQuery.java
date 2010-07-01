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
public class BTTargetingQuery extends TargetingQuery {
    private String bt;

    public BTTargetingQuery(String bt) {
        this.bt = bt;
    }

    public Type getType() {
        return Type.kBT;
    }

    public SortedSet<Handle> exec() {
        SortedSet<Handle> btResult      = execAgeQuery();
        SortedSet<Handle> nonBTResults   = execNonAgeQuery();

        MultiSortedSet<Handle> results = new MultiSortedSet<Handle>();
        if(btResult != null) {
            results.add(btResult);
        }
        if(nonBTResults != null) {
            results.add(nonBTResults);
        }

        return results;
    }

    private SortedSet<Handle> execAgeQuery() {
        if(bt == null) {
            return null;
        }
        SortedSet<Handle> results;
        results = CampaignDB.getInstance().getAdpodBTIndex().get(bt);
        return results;
    }

    private SortedSet<Handle> execNonAgeQuery() {
        return CampaignDB.getInstance().getAdpodBTNoneIndex().get(AdpodIndex.BT_NONE);
    }

    public boolean accept(Handle v) {
        return false;
    }
}