/*
 * ListingsQueryHandler.java
 *
 * COPYRIGHT (C) ${year} TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (${user}@tumri.com)
 * @version 1.0     ${date}
 *
 */
package com.tumri.joz.server.domain;

import com.tumri.utils.tcp.server.domain.QueryResponseData;
import com.tumri.utils.strings.StringUtils;

import java.util.HashMap;
import java.io.*;

/**
 * @author: nipun
 * Date: Jun 9, 2008
 * Time: 2:27:30 PM
 */
public class JozAdResponse extends QueryResponseData {
    static final long serialVersionUID = 2L;

    public static final String KEY_PRODUCTS = "PRODUCTS";
    public static final String KEY_PRODIDS = "PROD-IDS";
    public static final String KEY_CATEGORIES = "CATEGORIES";
    public static final String KEY_CATNAMES = "CAT-NAMES";
    public static final String KEY_REALM = "REALM";
    public static final String KEY_STRATEGY = "STRATEGY";
    public static final String KEY_ISPRIVATELABEL = "IS-PRIVATE-LABEL-P";
    public static final String KEY_SOZFEATURES = "SOZFEATURES";
    public static final String KEY_RECIPE = "RECIPE";
    public static final String KEY_RECIPE_ID = "RECIPE-ID";
    public static final String KEY_RECIPE_NAME = "RECIPE-NAME";
    public static final String KEY_SLOT_ID = "SLOT-ID";
    public static final String KEY_CAMPAIGN_ID = "CAMPAIGN-ID";
    public static final String KEY_CAMPAIGN_NAME = "CAMPAIGN-NAME";
    public static final String KEY_CAMPAIGN_CLIENT_ID = "CAMPAIGN-CLIENT-ID";
    public static final String KEY_CAMPAIGN_CLIENT_NAME = "CAMPAIGN-CLIENT-NAME";
    public static final String KEY_ADPOD_ID = "ADPOD-ID";
    public static final String KEY_ADPOD_NAME = "ADPOD-NAME";
    public static final String KEY_LOCATION_ID = "LOCATION-ID";
    public static final String KEY_LOCATION_NAME = "LOCATION-NAME";
    public static final String KEY_LOCATION_CLIENT_ID = "LOCATION-CLIENT-ID";
    public static final String KEY_LOCATION_CLIENT_NAME = "LOCATION-CLIENT-NAME";
    public static final String KEY_GEO_USED = "GEO-USED";
    public static final String KEY_ERROR = "ERROR";

    public JozAdResponse(){
    }

    public JozAdResponse(byte[] res) {
        this.response = res;
        this.resultMap = getResultMap();
    }

    public void setResultMap(HashMap<String, String> result) {
        this.resultMap = result;
    }

}
