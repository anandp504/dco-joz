/*
 * ListingsQueryHandler.java
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
package com.tumri.joz.server.domain;

import com.tumri.utils.tcp.server.domain.QueryInputData;
import com.tumri.utils.tcp.server.domain.QueryId;

public class JozTSpecRequest extends QueryInputData {
	public static final String KEY_COMMAND="type";
	public static final String KEY_GET_PRODUCTS_TSPEC = "tSpecDetailsByTSpec";
	public static final String KEY_GET_PRODUCTS_COUNTS_TSPEC = "tSpecDetailsCountsByTSpec";
	public static final String KEY_GET_PRODUCTS_COUNTS_TSPEC_ID = "tSpecDetailsCountsByTSpecId";
	public static final String KEY_GET_COUNTS_TSPEC = "tSpecCountsByTSpec";
	public static final String KEY_GET_PRODUCTS_TSPEC_ID = "tSpecDetailsByTSpecId";
	public static final String KEY_GET_COUNTS_TSPEC_ID = "tSpecCountsByTSpecId";
	public static final String KEY_TSPEC = "tSpec";
	public static final String KEY_TSPEC_ID = "tSpecId";
	public static final String KEY_PAGE_NUM = "pageNum";
	public static final String KEY_PAGE_SIZE = "pageSize";
    public QueryId getQueryId() {
        return QueryId.TSPEC;
    }
}