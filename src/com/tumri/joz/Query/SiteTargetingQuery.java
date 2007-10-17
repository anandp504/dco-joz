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
 * Targeting Query for Site related data.
 *
 * @author bpatel
 */
public class SiteTargetingQuery extends TargetingQuery {
    private int locationId;
    private String urlName;
    private String themeName;

    public SiteTargetingQuery(int locationId, String urlName, String themeName) {
        this.locationId = locationId;
        this.urlName    = urlName;
        this.themeName = themeName;
    }

    public Type getType() {
        return Type.kSite;
    }

    public SortedSet<Handle> exec() {
        SortedSet<Handle> locationResults      = execLocationQuery();
        SortedSet<Handle> urlResults           = execUrlQuery();
        SortedSet<Handle> themeResults         = execThemeQuery();
        //SortedSet<Handle> runOfNetworksResults = execRunOfNetworkQuery();

        MultiSortedSet<Handle> results = new MultiSortedSet<Handle>();
        if(locationResults != null) {
            results.add(locationResults);
        }
        if(urlResults != null) {
            results.add(urlResults);
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
        if(themeName != null && !themeName.equals("")) {
            AtomicAdpodIndex index = CampaignDB.getInstance().getThemeAdPodMappingIndex();
            results = index.get(themeName);
        }
        return results;
    }

    @SuppressWarnings({"unchecked"})
    private SortedSet<Handle> execUrlQuery() {
        MultiSortedSet<Handle> urlsResults = new MultiSortedSet<Handle>();
        SortedSet<Handle> results;
        if(urlName != null && !urlName.equals("")) {
            AtomicAdpodIndex index = CampaignDB.getInstance().getUrlAdPodMappingIndex();
            List<String> urls = UrlNormalizer.getAllPossibleNormalizedUrl(urlName);
            if(urls != null && urls.size() > 0) {
                double urlScore = TargetingScoreHelper.getInstance().getUrlScore();
                double delta    = 0.05;
                for (String url : urls) {
                    results = index.get(url);
                    SortedSet<Handle> clonedResults = null;
                    if(results != null) {
                        clonedResults = cloneResults(results, urlScore);
                    }
                    if(urlScore > delta) {
                        urlScore = urlScore - delta;
                    }
                    else {
                        urlScore = delta;    
                    }
                    if(clonedResults != null) {
                        urlsResults.add(clonedResults);
                    }
                }
            }

        }
        return urlsResults;
    }

    private SortedSet<Handle> cloneResults(SortedSet<Handle> results, double urlScore) {
        SortedArraySet<Handle> sortedArraySet = null;
        ArrayList<Handle> list;
        if(results != null) {
            if(results instanceof RWLocked) {
                ((RWLocked)results).readerLock();
            }
            try {
                Iterator<Handle> iterator = results.iterator();
                if(iterator != null) {
                    list = new ArrayList<Handle>();
                    while(iterator.hasNext()) {
                        Handle handle = iterator.next();
                        handle = handle.createHandle(urlScore);
                        list.add(handle);
                    }
                    sortedArraySet = new SortedArraySet<Handle>(list);
                }
            }
            finally {
                if(results instanceof RWLocked) {
                    ((RWLocked)results).readerUnlock();
                }
            }

        }

        return sortedArraySet;
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
