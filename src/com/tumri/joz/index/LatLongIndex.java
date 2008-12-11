/*
 * LatLongIndex.java
 *
 * COPYRIGHT (C)  TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (@tumri.com)
 * @version 1.0     
 *
 */
package com.tumri.joz.index;

import com.tumri.utils.index.AbstractIndex;

import java.util.Map;
import java.util.List;

/**
 * Lat Long Index class
 * @author: nipun
 * Date: Dec 9, 2008
 * Time: 10:47:40 AM
 */
public class LatLongIndex<Key, Value> extends AbstractIndex<Double, LatLongIndex.Attribute, Key, Value> {
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";

    private Attribute type;

    public LatLongIndex(LatLongIndex.Attribute type) {
        this.type = type;
    }

    public Attribute getType() {
        return type;
    }

    public List<Map.Entry<Key, Value>> getEntries(Double d) {
        throw new UnsupportedOperationException("This method is not supported by this index. Use put(Map) method instead");
    }

    public enum Attribute {
        kLatitude,
        kLongitude,
    }
}