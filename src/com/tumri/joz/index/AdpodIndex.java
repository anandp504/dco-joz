package com.tumri.joz.index;

import com.tumri.utils.index.AbstractIndex;
import com.tumri.cma.domain.AdPod;


import java.util.Map;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class AdpodIndex<Key, Value> extends AbstractIndex<AdPod, AdpodIndex.Attribute, Key, Value> {
    public static final String RUN_OF_NETWORK = "RUN_OF_NETWORK";
    public static final String DEFAULT_REALM  = "DEFAULT_REALM";
    public static final String GEO_NONE       = "GEO_NONE";
    public static final String URL_NONE       = "URL_NONE";
    public static final String EXTERNAL_VARIABLE_NONE       = "EXTERNAL_VARIABLE_NONE";

    private Attribute type;
    public AdpodIndex(AdpodIndex.Attribute type) {
        this.type = type;
    }
    public Attribute getType() {
        return type;
    }

    public List<Map.Entry<Key, Value>> getEntries(AdPod p) {
        throw new UnsupportedOperationException("This method is not supported by this index. Use put(Map) method instead");
    }

    public enum Attribute {
        // Site related codes
        kUrl,
        kTheme,
        kLocation,
        kRunofNetwork,
        // Geo related code
        kZipCode,
        kDMACode,
        kAreaCode,
        kCityCode,
        kRegionCode,
        kCountryCode,
        kGeoNone,
        kTime,
        kAdType,
        kRecipe,
        kUrlNone,
        kExtTarget,
        kExtTargetNone,
    }
}
