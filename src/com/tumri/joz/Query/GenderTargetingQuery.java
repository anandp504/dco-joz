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
public class GenderTargetingQuery extends TargetingQuery {
    private String gender;

    public GenderTargetingQuery(String gender) {
        this.gender = gender;
    }

    public Type getType() {
        return Type.kGender;
    }

    public SortedSet<Handle> exec() {
        SortedSet<Handle> genderResult      = execAgeQuery();
        SortedSet<Handle> nonGenderResult   = execNonAgeQuery();

        MultiSortedSet<Handle> results = new MultiSortedSet<Handle>();
        if(genderResult != null) {
            results.add(genderResult);
        }
        if(nonGenderResult != null) {
            results.add(nonGenderResult);
        }

        return results;
    }

    private SortedSet<Handle> execAgeQuery() {
        if(gender == null) {
            return null;
        }
        SortedSet<Handle> results;
        results = CampaignDB.getInstance().getAdpodGenderIndex().get(gender);
        return results;
    }

    private SortedSet<Handle> execNonAgeQuery() {
        return CampaignDB.getInstance().getAdpodGenderNoneIndex().get(AdpodIndex.GENDER_NONE);
    }

    public boolean accept(Handle v) {
        return false;
    }
}