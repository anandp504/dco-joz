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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Holds the relationship between the request attribute val to the Request Vectors
 *
 * @author: nipun
 * Date: Aug 3, 2009
 * Time: 1:26:38 PM
 */
public class WMIndex<Key, Value> extends AbstractIndex<WMHandle, WMAttribute, Key, Value> {

	private WMAttribute type;

	public WMIndex(WMAttribute type) {
		this.type = type;
	}

	public WMAttribute getType() {
		return type;
	}

	public List<Map.Entry<Key, Value>> getEntries(WMHandle p) {
		throw new UnsupportedOperationException("This method is not supported by this index.");
	}

	public List<WMAttribute> getAllowdAttributes() {
		List<WMAttribute> retList = new ArrayList<WMAttribute>();
		retList.add(WMAttribute.kRequestVector);
		retList.add(WMAttribute.kLineId);
		retList.add(WMAttribute.kSiteId);
		retList.add(WMAttribute.kBuyId);
		retList.add(WMAttribute.kCreativeId);
		retList.add(WMAttribute.kAdId);
		retList.add(WMAttribute.kZip);
		retList.add(WMAttribute.kDMA);
		retList.add(WMAttribute.kArea);
		retList.add(WMAttribute.kCity);
		retList.add(WMAttribute.kState);
		retList.add(WMAttribute.kCountry);
		retList.add(WMAttribute.kT1);
		retList.add(WMAttribute.kT2);
		retList.add(WMAttribute.kT3);
		retList.add(WMAttribute.kT4);
		retList.add(WMAttribute.kT5);
		retList.add(WMAttribute.kF1);
		retList.add(WMAttribute.kF2);
		retList.add(WMAttribute.kF3);
		retList.add(WMAttribute.kF4);
		retList.add(WMAttribute.kF5);
		retList.add(WMAttribute.kLineIdNone);
		retList.add(WMAttribute.kZipNone);
		retList.add(WMAttribute.kDMANone);
		retList.add(WMAttribute.kAreaNone);
		retList.add(WMAttribute.kCityNone);
		retList.add(WMAttribute.kStateNone);
		retList.add(WMAttribute.kCountryNone);
		return retList;
	}
}
