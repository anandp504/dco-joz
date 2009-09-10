/*
 * WMIndex.java
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
package com.tumri.joz.campaign.wm;

import com.tumri.utils.index.AbstractIndex;

import java.util.List;
import java.util.Map;

/**
 * Holds the relationship between the request attribute val to the Request Vectors
 * @author: nipun
 * Date: Aug 3, 2009
 * Time: 1:26:38 PM
 */
public class WMIndex<Key, Value> extends AbstractIndex<WMHandle, WMIndex.Attribute, Key, Value> {

    private WMIndex.Attribute type;

    public WMIndex(WMIndex.Attribute type) {
        this.type = type;
    }
    public WMIndex.Attribute getType() {
        return type;
    }

    public List<Map.Entry<Key, Value>> getEntries(WMHandle p) {
        throw new UnsupportedOperationException("This method is not supported by this index.");
    }

    public enum Attribute {
        kRequestVector,
        kLineId,
        kZip,
        kDMA,
        kArea,
        kCity,
        kState,
        kCountry,
        kLineIdNone,
        kZipNone,
        kDMANone,
        kAreaNone,
        kCityNone,
        kStateNone,
        kCountryNone,
    }
}
