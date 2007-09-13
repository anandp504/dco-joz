package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.index.AdpodIndex;
import com.tumri.utils.data.MultiSortedSet;

import java.util.SortedSet;
import java.util.List;

/**
 * Targeting query for geo data
 *
 * @author bpatel
 */
public class GeoTargetingQuery extends TargetingQuery {
    private List<String> countries;
    private List<String> cities;
    private List<String> regions;
    private List<String> dmacodes;
    private List<String> zipcodes;
    private List<String> areacodes;



    public GeoTargetingQuery(List<String> countries, List<String> regions, List<String> cities, List<String> dmacodes, List<String> zipcodes, List<String> areacodes) {
        this.countries = countries;
        this.cities    = cities;
        this.regions   = regions;
        this.dmacodes  = dmacodes;
        this.zipcodes  = zipcodes;
        this.areacodes = areacodes;
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
        SortedSet<Handle> results;
        results = CampaignDB.getInstance().getAdpodGeoCountryIndex().get(countries);
        return results;
    }

    private SortedSet<Handle> execRegionGeoQuery() {
        SortedSet<Handle> results = null;
        AdpodIndex<String, Handle> index = CampaignDB.getInstance().getAdpodGeoRegionIndex();
        if(index != null) {
            results = index.get(regions);
        }
        return results;
    }
    private SortedSet<Handle> execCityGeoQuery() {
        SortedSet<Handle> results;
        results = CampaignDB.getInstance().getAdpodGeoCityIndex().get(cities);
        return results;
    }
    private SortedSet<Handle> execDmacodeGeoQuery() {
        SortedSet<Handle> results;
        results = CampaignDB.getInstance().getAdpodGeoDmacodeIndex().get(dmacodes);
        return results;
    }
    private SortedSet<Handle> execZipcodeGeoQuery() {
        SortedSet<Handle> results;
        results = CampaignDB.getInstance().getAdpodGeoZipcodeIndex().get(zipcodes);
        return results;
    }
    private SortedSet<Handle> execAreacodeGeoQuery() {
        SortedSet<Handle> results;
        results = CampaignDB.getInstance().getAdpodGeoAreacodeIndex().get(areacodes);
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
