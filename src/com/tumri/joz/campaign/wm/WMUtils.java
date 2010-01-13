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

import com.tumri.joz.jozMain.AdDataRequest;

import java.util.HashMap;
import java.util.Map;

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
        } else if ("siteid".equals(type)) {
            attr = WMIndex.Attribute.kSiteId;
        } else if ("adid".equals(type)) {
            attr = WMIndex.Attribute.kAdId;
        } else if ("creativeid".equals(type)) {
            attr = WMIndex.Attribute.kCreativeId;
        } else if ("buyid".equals(type)) {
            attr = WMIndex.Attribute.kBuyId;
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
        } else if ("area".equals(type)) {
            attr = WMIndex.Attribute.kArea;
        } else if ("t1".equals(type)) {
            attr = WMIndex.Attribute.kT1;
        } else if ("t2".equals(type)) {
            attr = WMIndex.Attribute.kT2;
        } else if ("t3".equals(type)) {
            attr = WMIndex.Attribute.kT3;
        } else if ("t4".equals(type)) {
            attr = WMIndex.Attribute.kT4;
        } else if ("t5".equals(type)) {
            attr = WMIndex.Attribute.kT5;
        } else if ("f1".equals(type)) {
            attr = WMIndex.Attribute.kF1;
        } else if ("f2".equals(type)) {
            attr = WMIndex.Attribute.kF2;
        } else if ("f3".equals(type)) {
            attr = WMIndex.Attribute.kF3;
        } else if ("f4".equals(type)) {
            attr = WMIndex.Attribute.kF4;
        } else if ("f5".equals(type)) {
            attr = WMIndex.Attribute.kF5;
        }
        return attr;
    }

    public static String getIndexName(WMIndex.Attribute type) {
        String name = null;
        switch (type) {
            case kLineId:
                name = "lineid";
                break;
            case kSiteId:
                name = "siteid";
                break;
            case kCreativeId:
                name = "creativeid";
                break;
            case kBuyId:
                name = "buyid";
                break;
            case kAdId:
                name = "adid";
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
            case kArea:
                name = "area";
                break;
            case kT1:
                name = "t1";
                break;
            case kT2:
                name = "t2";
                break;
            case kT3:
                name = "t3";
                break;
            case kT4:
                name = "t4";
                break;
            case kT5:
                name = "t5";
                break;
            case kF1:
                name = "f1";
                break;
            case kF2:
                name = "f2";
                break;
            case kF3:
                name = "f3";
                break;
            case kF4:
                name = "f4";
                break;
            case kF5:
                name = "f5";
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

    /**
     * Construct the context map from the ad request
     * @param request
     * @return
     */
    public static Map<WMIndex.Attribute, Integer> getContextMap(AdDataRequest request) {
        Map<WMIndex.Attribute, Integer> contextMap = new HashMap<WMIndex.Attribute, Integer>();
        if (request.getPageId()!=null) {
            Integer id = WMUtils.getDictId(WMIndex.Attribute.kLineId, request.getPageId());
            if (id != null) {
                contextMap.put(WMIndex.Attribute.kLineId, id);
            }
        }
        if (request.getRegion()!=null) {
            Integer id = WMUtils.getDictId(WMIndex.Attribute.kState, request.getRegion());
            if (id != null) {
                contextMap.put(WMIndex.Attribute.kState, id);
            }
        }
        if (request.getDmacode()!=null) {
            Integer id = WMUtils.getDictId(WMIndex.Attribute.kDMA, request.getDmacode());
            if (id != null) {
                contextMap.put(WMIndex.Attribute.kDMA, id);
            }
        }
        if (request.getAreacode()!=null) {
            Integer id = WMUtils.getDictId(WMIndex.Attribute.kArea, request.getAreacode());
            if (id != null) {
                contextMap.put(WMIndex.Attribute.kArea, id);
            }
        }
        if (request.getExternalTargetField1()!=null) {
            Integer id = WMUtils.getDictId(WMIndex.Attribute.kT1, request.getExternalTargetField1());
            if (id != null) {
                contextMap.put(WMIndex.Attribute.kT1, id);
            }
        }
        if (request.getExternalTargetField2()!=null) {
            Integer id = WMUtils.getDictId(WMIndex.Attribute.kT2, request.getExternalTargetField2());
            if (id != null) {
                contextMap.put(WMIndex.Attribute.kT2, id);
            }
        }
        if (request.getExternalTargetField3()!=null) {
            Integer id = WMUtils.getDictId(WMIndex.Attribute.kT3, request.getExternalTargetField3());
            if (id != null) {
                contextMap.put(WMIndex.Attribute.kT3, id);
            }
        }
        if (request.getExternalTargetField4()!=null) {
            Integer id = WMUtils.getDictId(WMIndex.Attribute.kT4, request.getExternalTargetField4());
            if (id != null) {
                contextMap.put(WMIndex.Attribute.kT4, id);
            }
        }
        if (request.getExternalTargetField5()!=null) {
            Integer id = WMUtils.getDictId(WMIndex.Attribute.kT5, request.getExternalTargetField5());
            if (id != null) {
                contextMap.put(WMIndex.Attribute.kT5, id);
            }
        }
        if (request.getExternalFilterField1()!=null) {
            Integer id = WMUtils.getDictId(WMIndex.Attribute.kF1, request.getExternalFilterField1());
            if (id != null) {
                contextMap.put(WMIndex.Attribute.kF1, id);
            }
        }
        if (request.getExternalFilterField2()!=null) {
            Integer id = WMUtils.getDictId(WMIndex.Attribute.kF2, request.getExternalFilterField2());
            if (id != null) {
                contextMap.put(WMIndex.Attribute.kF2, id);
            }
        }
        if (request.getExternalFilterField3()!=null) {
            Integer id = WMUtils.getDictId(WMIndex.Attribute.kF3, request.getExternalFilterField3());
            if (id != null) {
                contextMap.put(WMIndex.Attribute.kF3, id);
            }
        }
        if (request.getExternalFilterField4()!=null) {
            Integer id = WMUtils.getDictId(WMIndex.Attribute.kF4, request.getExternalFilterField4());
            if (id != null) {
                contextMap.put(WMIndex.Attribute.kF4, id);
            }
        }
        if (request.getExternalFilterField5()!=null) {
            Integer id = WMUtils.getDictId(WMIndex.Attribute.kF5, request.getExternalFilterField5());
            if (id != null) {
                contextMap.put(WMIndex.Attribute.kF5, id);
            }
        }
        return contextMap;
    }
}
