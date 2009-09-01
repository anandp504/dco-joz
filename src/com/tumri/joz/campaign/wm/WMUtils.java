/*
 * WMUtils.java
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

/**
 * Utils class for WeightMatrix
 * @author: nipun
 * Date: Aug 12, 2009
 * Time: 9:34:29 PM
 */
public class WMUtils {

    public static WMIndex.Attribute getAttribute(String type) {
        WMIndex.Attribute attr = null;
        if ("lineid".equals(type)) {
            attr = WMIndex.Attribute.kLineId;
        } else if ("EXTERNALPAGEID".equals(type)) {
            attr = WMIndex.Attribute.kLineId;
        } else if ("state".equals(type)) {
            attr = WMIndex.Attribute.kState;
        } else if ("city".equals(type)) {
            attr = WMIndex.Attribute.kCity;
        } else if ("zip".equals(type)) {
            attr = WMIndex.Attribute.kZip;
        } else if ("country".equals(type)) {
            attr = WMIndex.Attribute.kCountry;
        } else if ("dma".equals(type)) {
            attr = WMIndex.Attribute.kDMA;
        }
        return attr;
    }

    public static String getIndexName(WMIndex.Attribute type) {
        String name = null;
        switch (type) {
            case kLineId:
                name = "lineid";
                break;
            case kState:
                name = "state";
                break;
            case kCity:
                name = "city";
                break;
            case kZip:
                name = "zip";
                break;
            case kDMA:
                name = "dma";
                break;
            case kCountry:
                name = "country";
                break;
            default:
                break;
        }
        return name;
    }

    public static Integer getDictId(String type, String val) {
        WMIndex.Attribute attr = getAttribute(type);
        return getDictId(attr, val);
    }

    public static Integer getDictId(WMIndex.Attribute attr, String val) {
        Integer ret = null;
        if (attr != null && val!=null && !val.isEmpty()) {
            ret = WMDictionaryManager.getInstance().getId(WMIndex.Attribute.kRequestVector, attr + val.toLowerCase());
        }
        return ret;
    }

    public static String getDictValue(WMIndex.Attribute attr, Integer id) {
        String val = null;
        if ( attr != null && id !=null) {
            String tmpVal = (String)WMDictionaryManager.getInstance().getValue(WMIndex.Attribute.kRequestVector, id);
            if (tmpVal!=null && tmpVal.length()>attr.name().length()) {
                val = tmpVal.substring(attr.name().length(), tmpVal.length());
            }
        }
        return val;
    }
}
