package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.UrlNormalizer;
import com.tumri.joz.targeting.TargetingScoreHelper;
import com.tumri.utils.data.MultiSortedSet;
import com.tumri.utils.data.SortedArraySet;
import com.tumri.utils.data.RWLocked;

import java.util.SortedSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Targeting Query for Theme and Location data.
 *
 * @author bpatel
 * @author nipun
 */
public class SiteTargetingQuery extends TargetingQuery {
    private int locationId;

    public SiteTargetingQuery(int locationId) {
        this.locationId = locationId;
    }

    public Type getType() {                                                                   
        return Type.kSite;
    }

    public SortedSet<Handle> exec() {
        SortedSet<Handle> locationResults      = execLocationQuery();

        MultiSortedSet<Handle> results = new MultiSortedSet<Handle>();
        if(locationResults != null) {
            results.add(locationResults);
        }

        return results;
    }

    @SuppressWarnings({"unchecked"})
    private SortedSet<Handle> execLocationQuery() {
        SortedSet<Handle> results = null;
        if(locationId > 0) {
            AtomicAdpodIndex index = CampaignDB.getInstance().getLocationAdPodMappingIndex();
            results = index.get(locationId);
        }
        return results;
    }

    public boolean accept(Handle v) {
        return false;
    }

	@Override
	public boolean mustMatch(){
		return true;
	}
}
