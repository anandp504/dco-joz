package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.index.AdpodIndex;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.utils.data.MultiSortedSet;

import java.util.SortedSet;

/**
 * Targeting query for geo data
 *
 * @author bpatel
 */
public class GeoTargetingQuery extends TargetingQuery {
    private String country;
    private String city;
    private String region;
    private String dmacode;
    private String zipcode;
    private String areacode;


    public GeoTargetingQuery(String country, String region, String city, String dmacode, String zipcode, String areacode) {
        this.country = country;
        this.city = city;
        this.region = region;
        this.dmacode = dmacode;
        this.zipcode = zipcode;
        this.areacode = areacode;
    }

    public Type getType() {
        return Type.kGeo;
    }

    public SortedSet<Handle> exec() {
        SortedSet<Handle> countriesResult      = execCountryGeoQuery();
        SortedSet<Handle> regionsResults       = execRegionGeoQuery();
        SortedSet<Handle> citiesResults        = execCityGeoQuery();
        SortedSet<Handle> dmacodesResults      = execDmacodeGeoQuery();
        SortedSet<Handle> zipcodesResults      = execZipcodeGeoQuery();
        SortedSet<Handle> areacodesResults     = execAreacodeGeoQuery();

        SortedSet<Handle> nonGeoAdPodResults   = execNonGeoSpecificAdPodQuery();

        MultiSortedSet<Handle> results = new MultiSortedSet<Handle>();
        if(countriesResult != null) {
            results.add(countriesResult);
        }
        if(regionsResults != null) {
            results.add(regionsResults);
        }
        if(citiesResults != null) {
            results.add(citiesResults);
        }
        if(dmacodesResults != null) {
            results.add(dmacodesResults);
        }
        if(zipcodesResults != null) {
            results.add(zipcodesResults);
        }
        if(areacodesResults != null) {
            results.add(areacodesResults);
        }
        if(nonGeoAdPodResults != null) {
            results.add(nonGeoAdPodResults);
        }

        return results;
    }

    private SortedSet<Handle> execCountryGeoQuery() {
        if(country == null) {
            return null;
        }
        SortedSet<Handle> results;
        results = CampaignDB.getInstance().getAdpodGeoCountryIndex().get(country);
        return results;
    }

    private SortedSet<Handle> execRegionGeoQuery() {
        if(region == null) {
            return null;
        }
        SortedSet<Handle> results = null;
        AdpodIndex<String, Handle> index = CampaignDB.getInstance().getAdpodGeoRegionIndex();
        if(index != null) {
            results = index.get(region);
        }
        return results;
    }
    private SortedSet<Handle> execCityGeoQuery() {
        if(city == null) {
            return null;
        }
        SortedSet<Handle> results;
        results = CampaignDB.getInstance().getAdpodGeoCityIndex().get(city);
        return results;
    }
    private SortedSet<Handle> execDmacodeGeoQuery() {
        if(dmacode == null) {
            return null;
        }
        SortedSet<Handle> results;
        results = CampaignDB.getInstance().getAdpodGeoDmacodeIndex().get(dmacode);
        return results;
    }
    private SortedSet<Handle> execZipcodeGeoQuery() {
        if(zipcode == null) {
            return null;
        }
        SortedSet<Handle> results;
        results = CampaignDB.getInstance().getAdpodGeoZipcodeIndex().get(zipcode);
        return results;
    }
    private SortedSet<Handle> execAreacodeGeoQuery() {
        if(areacode == null) {
            return null;
        }
        SortedSet<Handle> results;
        results = CampaignDB.getInstance().getAdpodGeoAreacodeIndex().get(areacode);
        return results;
    }

    private SortedSet<Handle> execNonGeoSpecificAdPodQuery() {
        SortedSet<Handle> results;
        results = CampaignDB.getInstance().getNonGeoAdPodIndex().get(AdpodIndex.GEO_NONE);
        return results;
    }

    public boolean accept(Handle v) {
        return false;
    }
}
