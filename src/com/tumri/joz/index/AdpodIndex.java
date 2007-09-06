package com.tumri.joz.index;

import com.tumri.joz.campaign.JAdPod;
import com.tumri.utils.index.AbstractIndex;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public abstract class AdpodIndex<Key,Value> extends AbstractIndex<JAdPod, AdpodIndex.Attribute, Key, Value> {
  public static final String NONE = "123NONE"; // Special key to index RunofNetwork or Geo None

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
