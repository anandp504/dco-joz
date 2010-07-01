package com.tumri.joz.Query;

import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.index.AdpodIndex;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.products.Handle;
import com.tumri.utils.data.MultiSortedSet;

import java.util.SortedSet;

/**
 * Targeting query for age data
 *
 * @author nipun
 */
public class AgeTargetingQuery extends TargetingQuery {
    private String age;

    public AgeTargetingQuery(String age) {
        this.age = age;
    }

    public Type getType() {
        return Type.kAge;
    }

    public SortedSet<Handle> exec() {
        SortedSet<Handle> ageResult      = execAgeQuery();
        SortedSet<Handle> nonAgeResults   = execNonAgeQuery();

        MultiSortedSet<Handle> results = new MultiSortedSet<Handle>();
        if(ageResult != null) {
            results.add(ageResult);
        }
        if(nonAgeResults != null) {
            results.add(nonAgeResults);
        }

        return results;
    }

    private SortedSet<Handle> execAgeQuery() {
        if(age == null) {
            return null;
        }
        SortedSet<Handle> results;
        results = CampaignDB.getInstance().getAdpodAgeIndex().get(age);
        return results;
    }

    private SortedSet<Handle> execNonAgeQuery() {
        return CampaignDB.getInstance().getAdpodAgeNoneIndex().get(AdpodIndex.AGE_NONE);
    }

    public boolean accept(Handle v) {
        return false;
    }
}