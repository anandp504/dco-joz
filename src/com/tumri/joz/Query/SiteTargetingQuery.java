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
    private String themeName;
    private String adType;

    public SiteTargetingQuery(int locationId, String themeName, String adType) {
        this.locationId = locationId;
        this.themeName = themeName;
        this.adType = adType;
    }

    public Type getType() {                                                                   
        return Type.kSite;
    }

    public SortedSet<Handle> exec() {
        SortedSet<Handle> locationResults      = execLocationQuery();
        SortedSet<Handle> themeResults         = execThemeQuery();
        //SortedSet<Handle> runOfNetworksResults = execRunOfNetworkQuery();


        MultiSortedSet<Handle> results = new MultiSortedSet<Handle>();
        if(locationResults != null) {
            results.add(locationResults);
        }
        if(themeResults != null) {
            results.add(themeResults);
        }
//        if(runOfNetworksResults != null) {
//            results.add(runOfNetworksResults);
//        }

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

    @SuppressWarnings({"unchecked"})
    private SortedSet<Handle> execThemeQuery() {
        SortedSet<Handle> results = null;
        if(themeName != null && !themeName.equals("") && adType != null && !adType.equals("")) {
            //Do a look up for the Location ID for the given theme. ThemeQuery is executed as a location query internally
            Integer thmLocId = CampaignDB.getInstance().getLocationIdForName(themeName+adType);
            if (thmLocId != null) {
                locationId = thmLocId.intValue();
            } else {
                locationId = 0;
            }
            results = execLocationQuery();
        }
        return results;
    }

//    private SortedSet<Handle> execRunOfNetworkQuery() {
//        SortedSet<Handle> results;
//        AtomicAdpodIndex index = CampaignDB.getInstance().getRunOfNetworkAdPodIndex();
//        results = index.get(AdpodIndex.RUN_OF_NETWORK);
//        return results;
//    }

    public boolean accept(Handle v) {
        return false;
    }
}
