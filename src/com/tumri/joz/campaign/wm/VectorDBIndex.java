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

import com.tumri.joz.products.Handle;
import com.tumri.utils.index.AbstractIndex;

import java.util.List;
import java.util.Map;

/**
 * Holds the relationship between the request attribute val to the Request Vectors
 *
 * @author: nipun
 * Date: Aug 3, 2009
 * Time: 1:26:38 PM
 */
public class VectorDBIndex<Key, Value> extends AbstractIndex<Handle, VectorAttribute, Key, Value> {

	private VectorAttribute type;

	public VectorDBIndex(VectorAttribute type) {
		this.type = type;
	}

	public VectorAttribute getType() {
		return type;
	}

	public List<Map.Entry<Key, Value>> getEntries(Handle p) {
		throw new UnsupportedOperationException("This method is not supported by this index.");
	}

}