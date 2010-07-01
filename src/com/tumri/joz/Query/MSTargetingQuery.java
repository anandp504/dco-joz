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
public class MSTargetingQuery extends TargetingQuery {
    private String ms;

    public MSTargetingQuery(String ms) {
        this.ms = ms;
    }

    public Type getType() {
        return Type.kMS;
    }

    public SortedSet<Handle> exec() {
        SortedSet<Handle> msResult      = execMSQuery();
        SortedSet<Handle> nonMSResults   = execNonMSQuery();

        MultiSortedSet<Handle> results = new MultiSortedSet<Handle>();
        if(msResult != null) {
            results.add(msResult);
        }
        if(nonMSResults != null) {
            results.add(nonMSResults);
        }

        return results;
    }

    private SortedSet<Handle> execMSQuery() {
        if(ms == null) {
            return null;
        }
        SortedSet<Handle> results;
        results = CampaignDB.getInstance().getAdpodMSIndex().get(ms);
        return results;
    }

    private SortedSet<Handle> execNonMSQuery() {
        return CampaignDB.getInstance().getAdpodMSNoneIndex().get(AdpodIndex.MS_NONE);
    }

    public boolean accept(Handle v) {
        return false;
    }
}