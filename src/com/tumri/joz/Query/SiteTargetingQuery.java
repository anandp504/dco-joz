package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;
import com.tumri.joz.index.AdpodIndex;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.UrlNormalizer;
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
        SortedSet<Handle> runOfNetworksResults = execRunOfNetworkQuery();

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
        if(runOfNetworksResults != null) {
            results.add(runOfNetworksResults);
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
                //@todo replace this with more appropriate scoring
                double urlScore = 0.9;
                double delta    = 0.05;
                for (String url : urls) {
                    results = index.get(url);
                    SortedSet<Handle> clonedResults = cloneResults(results, urlScore);
                    urlScore = urlScore - delta;
                    if(urlScore < 0.05) {
                        urlScore = delta;    
                    }
                    urlsResults.add(clonedResults);
                }
            }

        }
        return urlsResults;
    }

    private SortedSet<Handle> cloneResults(SortedSet<Handle> results, double urlScore) {
        SortedArraySet<Handle> sortedArraySet = null;
        ArrayList<Handle> list = null;
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
                    sortedArraySet = new SortedArraySet(list);
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
    
    @SuppressWarnings({"unchecked"})
    private SortedSet<Handle> execRunOfNetworkQuery() {
        SortedSet<Handle> results;
        AtomicAdpodIndex index = CampaignDB.getInstance().getRunOfNetworkAdPodIndex();
        results = index.get(AdpodIndex.RUN_OF_NETWORK);
        return results;
    }

//    private static List<String> parseUrl(String urlName) {
//        List<String> parsedUrls = new ArrayList<String>();
//        if(urlName != null && !"".equals(urlName)) {
//            //check if http:// is present, if yes, move it to prefix
//            String prefix = "";
//            boolean prefixPresent = urlName.regionMatches(true, 0, "http://", 0, 7);
//            if(prefixPresent) {
//                prefix = urlName.substring(0, 7);
//                urlName = urlName.substring(6);
//            }
//            String[] tokens = urlName.split("/");
//            if(tokens != null && tokens.length > 0) {
//                String currentStr = "";
//
//                int i = 0;
//                //if prefix http:// is present, ignore first token
//                if(prefixPresent) {
//                    i = 1;
//                }
//                while (i< tokens.length) {
//                    if("".equals(currentStr)) {
//                        currentStr = tokens[i];
//                    }
//                    else {
//                        currentStr += "/" + tokens[i];
//                    }
//                    if(!"".equals(currentStr)) {
//                        parsedUrls.add(prefix + currentStr);
//                    }
//                    i++;
//                }
//            }
//        }
//
//        return parsedUrls;
//    }

    public boolean accept(Handle v) {
        return false;
    }
}
