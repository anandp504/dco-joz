package com.tumri.joz.Query;

import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.UrlNormalizer;
import com.tumri.joz.index.AdpodIndex;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.targeting.TargetingScoreHelper;
import com.tumri.utils.data.MultiSortedSet;
import com.tumri.utils.data.RWLocked;
import com.tumri.utils.data.SortedArraySet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

/**
 * Targeting Query for Url related data.
 *
 * @author nipun
 */
public class UrlTargetingQuery extends TargetingQuery {
    private String urlName;

    public UrlTargetingQuery(String urlName) {
        this.urlName    = urlName;
    }

    public Type getType() {
        return Type.kUrl;
    }

    public SortedSet<Handle> exec() {
        SortedSet<Handle> urlResults           = execUrlQuery();
        SortedSet<Handle> nonUrlResults = execNonUrlAdpodQuery();


        MultiSortedSet<Handle> results = new MultiSortedSet<Handle>();
        if(urlResults != null) {
            results.add(urlResults);
        }
        if(nonUrlResults != null) {
            results.add(nonUrlResults);
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

    private SortedSet<Handle> execNonUrlAdpodQuery() {
        return CampaignDB.getInstance().getNonUrlAdPodIndex().get(AdpodIndex.URL_NONE);
    }

    public boolean accept(Handle v) {
        return false;
    }
}