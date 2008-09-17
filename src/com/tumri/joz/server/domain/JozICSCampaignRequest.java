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

public class JozICSCampaignRequest extends QueryInputData {
	public static final String KEY_COMMAND="type";
	public static final String KEY_CAMPAIGN = "campaign";
	public static final String KEY_ADVERTISER_ID = "advertiserId";
	public static final String COMMAND_GET_ALL_ADVERTISERS="getAllAdvertisers";
	public static final String COMMAND_CAMPAIGN_FOR_ADVERTISER="getCampaignForAdvertiser";
	public static final String COMMAND_CAMPAIGN_FOR_ALL_ADVERTISERS="getCampaignForAllAdvertiser";
    public QueryId getQueryId() {
        return QueryId.CAMPAIGN_DATA_ICS;
    }
}