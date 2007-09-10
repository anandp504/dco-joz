package com.tumri.joz.index;

import com.tumri.joz.campaign.JAdPod;
import com.tumri.utils.index.AbstractIndex;

/**
 * Abstract AdPod Index class that will be base class for all the targeting specific indices.
 *
 * @owner snawathe, bpatel
 */
public abstract class AbstractAdpodIndex<Key, Value> extends AbstractIndex<JAdPod, AdpodIndex.Attribute, Key, Value> {
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
