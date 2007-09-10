package com.tumri.joz.index;

import com.tumri.joz.campaign.JAdPod;
import com.tumri.joz.products.Handle;


import java.util.Map;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class AdpodIndex extends AbstractAdpodIndex<Integer, Handle> {
    public static final String RUN_OF_NETWORK = "RUN_OF_NETWORK";
    public static final String DEFAULT_REALM = "DEFAULT_REALM";

    private Attribute type;
    public AdpodIndex(AdpodIndex.Attribute type) {
        this.type = type;
    }
    public Attribute getType() {
        return type;
    }

    public List<Map.Entry<Integer, Handle>> getEntries(JAdPod p) {
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
    kGeoNone
  }
}
