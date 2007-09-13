package com.tumri.joz.index;

import com.tumri.utils.index.AbstractIndex;
import com.tumri.cma.domain.AdPod;

/**
 * Abstract AdPod Index class that will be base class for all the targeting specific indices.
 *
 * @owner snawathe, bpatel
 */
public abstract class AbstractAdpodIndex<Key, Value> extends AbstractIndex<AdPod, AdpodIndex.Attribute, Key, Value> {
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
