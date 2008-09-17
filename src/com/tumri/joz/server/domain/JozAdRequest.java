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

import com.tumri.utils.tcp.server.domain.QueryId;
import com.tumri.utils.tcp.server.domain.QueryInputData;

/**
 * JozAdRequest is the request object for the AdData call.
 * The request properties are set using the "setValue()" method that takes
 * in a Key, which are defined as constants in this class and the value to the corresponding string request
 * value.
 *
 * For example, to set create a JozAdRequest object and set a few values in it, do the following:
 *       <p>
 *       <code>
 *       JozAdRequest aquery = new JozAdRequest(); <br>
 *       aquery.setValue(JozAdRequest.KEY_URL, "http://yahoo.com/");<br>
 *       aquery.setValue(JozAdRequest.KEY_THEME, "nipun-theme2");<br>
 *       aquery.setValue(JozAdRequest.KEY_NUM_PRODUCTS, "12");<br>
 *       </code>
 *       <p>
 * @author: nipun
 * Date: Jun 9, 2008
 * Time: 2:21:34 PM
 */
public class JozAdRequest extends QueryInputData {

    static final long serialVersionUID = 1L;


    public static final String KEY_URL=                      "url";
    public static final String KEY_THEME=                    "theme";
    public static final String KEY_STORE_ID=                 "store-ID";
    public static final String KEY_LOCATION_ID=              "location-id";
    public static final String KEY_CATEGORY=                 "category";
    public static final String KEY_T_SPEC=                   "t-spec";
    //public static final String KEY_STRATEGY=                 "strategy";
    //public static final String KEY_REFERRER=                 "referrer";
    public static final String KEY_NUM_PRODUCTS=             "num-products";
    public static final String KEY_ROW_SIZE=                 "row-size";
    public static final String KEY_WHICH_ROW=                "which-row";
    public static final String KEY_REVERT_TO_DEFAULT_REALM=  "revert-to-default-realm";
    public static final String KEY_KEYWORDS=                 "keywords";
    public static final String KEY_SCRIPT_KEYWORDS=          "script-keywords";
    //public static final String KEY_INCLUDE_CAT_COUNTS=       "include-cat-counts";
    //public static final String KEY_SEED=                     "seed";
    //public static final String KEY_PSYCHOGRAPHICS_P=         "psychographics-p";
    //public static final String KEY_MINE_PUB_URL_P=           "min-pub-URL-p";
    public static final String KEY_ALLOW_TOO_FEW_PRODUCTS=   "allow-too-few-products";
    public static final String KEY_AD_WIDTH=                 "ad-width";
    public static final String KEY_AD_HEIGHT=                "ad-height";
    public static final String KEY_AD_OFFER_TYPE=            "ad-offer-type";
    public static final String KEY_MIN_NUM_LEADGENS=         "min-num-leadgens";
    //public static final String KEY_OUTPUT_FORMAT=            "output-format";
    //public static final String KEY_OUTPUT_ORDER=             "output-order";
    //public static final String KEY_OUTPUT_ORDER_NOISE_STDDEV="output-order-noise-stddev";
    public static final String KEY_MAX_PROD_DESC_LEN=        "max-prod-desc-len";
    public static final String KEY_COUNTRY=                  "country-name";
    public static final String KEY_REGION=                   "region";
    public static final String KEY_CITY=                     "city";
    public static final String KEY_DMACODE=                  "dma";
    public static final String KEY_AREACODE=                 "area-code";
    public static final String KEY_ZIP_CODE=                 "zip-code";
    public static final String KEY_LATITUDE=                 "latitude";
    public static final String KEY_LONGITUDE=                "longitude";
    public static final String KEY_AD_TYPE=                  "ad-type";
    public static final String KEY_RECIPE_ID=                "recipe-id";
    public static final String KEY_MULTI_VALUE_FIELD1=       "multivaluefield1";
    public static final String KEY_MULTI_VALUE_FIELD2=       "multivaluefield2";
    public static final String KEY_MULTI_VALUE_FIELD3=       "multivaluefield3";
    public static final String KEY_MULTI_VALUE_FIELD4=       "multivaluefield4";
    public static final String KEY_MULTI_VALUE_FIELD5=       "multivaluefield5";

    public JozAdRequest() {
        super();
    }

    public QueryId getQueryId() {
        return QueryId.AD_REQUEST;
    }

}
