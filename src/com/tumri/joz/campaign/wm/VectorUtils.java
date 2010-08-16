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
import com.tumri.joz.utils.AppProperties;
import com.tumri.utils.strings.StringTokenizer;

import java.util.*;

/**
 * Utils class for WeightMatrix
 *
 * @author: nipun
 * Date: Aug 12, 2009
 * Time: 9:34:29 PM
 */
public class VectorUtils {
    private static final char MULTI_VALUE_DELIM = AppProperties.getInstance().getMultiValueDelimiter();

    private static List<VectorAttribute> rangeAttrs = null;

    private static VectorAttribute[] attrPosArr =
            {
                    VectorAttribute.kLineId,
                    VectorAttribute.kSiteId,
                    VectorAttribute.kAdId,
                    VectorAttribute.kCreativeId,
                    VectorAttribute.kBuyId,
                    VectorAttribute.kState,
                    VectorAttribute.kCity,
                    VectorAttribute.kZip,
                    VectorAttribute.kCountry,
                    VectorAttribute.kDMA,
                    VectorAttribute.kArea,
                    VectorAttribute.kT1,
                    VectorAttribute.kT2,
                    VectorAttribute.kT3,
                    VectorAttribute.kT4,
                    VectorAttribute.kT5,
                    VectorAttribute.kF1,
                    VectorAttribute.kF2,
                    VectorAttribute.kF3,
                    VectorAttribute.kF4,
                    VectorAttribute.kF5,
                    VectorAttribute.kUB,
                    VectorAttribute.kAdpodId,
                    VectorAttribute.kExpId,
            };

    private static HashMap<VectorAttribute, Integer> attrPosMap = new HashMap<VectorAttribute, Integer>();
    static {
        attrPosMap.put(VectorAttribute.kLineId,0);
        attrPosMap.put(VectorAttribute.kSiteId,1);
        attrPosMap.put(VectorAttribute.kAdId,2);
        attrPosMap.put(VectorAttribute.kCreativeId,3);
        attrPosMap.put(VectorAttribute.kBuyId,4);
        attrPosMap.put(VectorAttribute.kState,5);
        attrPosMap.put(VectorAttribute.kCity,6);
        attrPosMap.put(VectorAttribute.kZip,7);
        attrPosMap.put(VectorAttribute.kCountry,8);
        attrPosMap.put(VectorAttribute.kDMA,9);
        attrPosMap.put(VectorAttribute.kArea,10);
        attrPosMap.put(VectorAttribute.kT1,11);
        attrPosMap.put(VectorAttribute.kT2,12);
        attrPosMap.put(VectorAttribute.kT3,13);
        attrPosMap.put(VectorAttribute.kT4,14);
        attrPosMap.put(VectorAttribute.kT5,15);
        attrPosMap.put(VectorAttribute.kF1,16);
        attrPosMap.put(VectorAttribute.kF2,17);
        attrPosMap.put(VectorAttribute.kF3,18);
        attrPosMap.put(VectorAttribute.kF4,19);
        attrPosMap.put(VectorAttribute.kF5,20);
        attrPosMap.put(VectorAttribute.kUB,21);
        attrPosMap.put(VectorAttribute.kAdpodId,22);
        attrPosMap.put(VectorAttribute.kExpId,23);
    }
    public static VectorAttribute getAttribute(String type) {
        VectorAttribute attr = null;
        if ("lineid".equals(type)) {
            attr = VectorAttribute.kLineId;
        } else if ("EXTERNALPAGEID".equals(type)) {
            attr = VectorAttribute.kLineId;
        } else if ("siteid".equals(type)) {
            attr = VectorAttribute.kSiteId;
        } else if ("adid".equals(type)) {
            attr = VectorAttribute.kAdId;
        } else if ("creativeid".equals(type)) {
            attr = VectorAttribute.kCreativeId;
        } else if ("buyid".equals(type)) {
            attr = VectorAttribute.kBuyId;
        } else if ("state".equals(type)) {
            attr = VectorAttribute.kState;
        } else if ("city".equals(type)) {
            attr = VectorAttribute.kCity;
        } else if ("zip".equals(type)) {
            attr = VectorAttribute.kZip;
        } else if ("country".equals(type)) {
            attr = VectorAttribute.kCountry;
        } else if ("dma".equals(type)) {
            attr = VectorAttribute.kDMA;
        } else if ("area".equals(type)) {
            attr = VectorAttribute.kArea;
        } else if ("t1".equals(type)) {
            attr = VectorAttribute.kT1;
        } else if ("t2".equals(type)) {
            attr = VectorAttribute.kT2;
        } else if ("t3".equals(type)) {
            attr = VectorAttribute.kT3;
        } else if ("t4".equals(type)) {
            attr = VectorAttribute.kT4;
        } else if ("t5".equals(type)) {
            attr = VectorAttribute.kT5;
        } else if ("f1".equals(type)) {
            attr = VectorAttribute.kF1;
        } else if ("f2".equals(type)) {
            attr = VectorAttribute.kF2;
        } else if ("f3".equals(type)) {
            attr = VectorAttribute.kF3;
        } else if ("f4".equals(type)) {
            attr = VectorAttribute.kF4;
        } else if ("f5".equals(type)) {
            attr = VectorAttribute.kF5;
        } else if ("ub".equals(type)) {
            attr = VectorAttribute.kUB;
        } else if ("adpodid".equals(type)) {
            attr = VectorAttribute.kAdpodId;
        } else if ("expid".equals(type)) {
            attr = VectorAttribute.kExpId;
        }
        return attr;
    }

    public static VectorAttribute getAttribute(int pos) {
        VectorAttribute attr = null;
        if (pos < attrPosArr.length) {
            attr = attrPosArr[pos];
        }
        return attr;
    }

    public static int getAttributePos(VectorAttribute attr) {
        int pos = -1;
        Integer posO = attrPosMap.get(attr);
        if (posO!=null) {
            pos = posO.intValue();
        }
        return pos;
    }


    public static String getIndexName(VectorAttribute type) {
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
            case kUB:
                name = "ub";
                break;
            case kAdpodId:
                name = "adpodid";
                break;
            case kExpId:
                name = "expid";
                break;
            default:
                break;
        }
        return name;
    }

    /**
     * Get the none attribute for a given attribute
     * @param kAttr
     * @return
     */
    public static VectorAttribute getNoneAttribute(VectorAttribute kAttr) {
        VectorAttribute noneAttr = null;
        switch (kAttr) {
            case kLineId:
                noneAttr = VectorAttribute.kLineIdNone;
                break;
            case kSiteId:
                noneAttr = VectorAttribute.kSiteIdNone;
                break;
            case kCreativeId:
                noneAttr = VectorAttribute.kCreativeIdNone;
                break;
            case kBuyId:
                noneAttr = VectorAttribute.kBuyIdNone;
                break;
            case kAdId:
                noneAttr = VectorAttribute.kAdIdNone;
                break;
            case kState:
                noneAttr = VectorAttribute.kStateNone;
                break;
            case kCity:
                noneAttr = VectorAttribute.kCityNone;
                break;
            case kZip:
                noneAttr = VectorAttribute.kZipNone;
                break;
            case kDMA:
                noneAttr = VectorAttribute.kDMANone;
                break;
            case kCountry:
                noneAttr = VectorAttribute.kCountryNone;
                break;
            case kArea:
                noneAttr = VectorAttribute.kAreaNone;
                break;
            case kT1:
                noneAttr = VectorAttribute.kT1None;
                break;
            case kT2:
                noneAttr = VectorAttribute.kT2None;
                break;
            case kT3:
                noneAttr = VectorAttribute.kT3None;
                break;
            case kT4:
                noneAttr = VectorAttribute.kT4None;
                break;
            case kT5:
                noneAttr = VectorAttribute.kT5None;
                break;
            case kF1:
                noneAttr = VectorAttribute.kF1None;
                break;
            case kF2:
                noneAttr = VectorAttribute.kF2None;
                break;
            case kF3:
                noneAttr = VectorAttribute.kF3None;
                break;
            case kF4:
                noneAttr = VectorAttribute.kF4None;
                break;
            case kF5:
                noneAttr = VectorAttribute.kF5None;
                break;
            case kUB:
                noneAttr = VectorAttribute.kUBNone;
                break;
            case kAdpodId:
                noneAttr = null;
                break;
            case kExpId:
                noneAttr = null;
                break;
            default:
                noneAttr=kAttr;
                break;
        }
        return noneAttr;

    }

    public static Integer getDictId(String type, String val) {
        VectorAttribute attr = getAttribute(type);
        return getDictId(attr, val);
    }

    public static Integer getDictId(VectorAttribute attr, String val) {
        Integer ret = null;
        if (attr != null && val != null && !val.isEmpty()) {
            ret = VectorDictionaryManager.getInstance().getId(attr, val.toLowerCase());
        }
        return ret;
    }

    public static Integer getNoneDictId(VectorAttribute attr) {
        return getDictId(attr, "NONE");
    }

    public static String getDictValue(VectorAttribute attr, Integer id) {
        String val = null;
        if (attr != null && id != null) {
            val = (String) VectorDictionaryManager.getInstance().getValue(attr, id);
        }
        return val;
    }

    /**
     * Construct the context map from the ad request
     *
     * @param request
     * @return
     */
    public static Map<VectorAttribute, List<Integer>> getContextMap(int adpodId, int expId,  AdDataRequest request) {
        Map<VectorAttribute, List<Integer>> contextMap = new HashMap<VectorAttribute, List<Integer>>();
        //Always add default
        {
            List<Integer> defvalues = new ArrayList<Integer>();
            if (adpodId > 0) {
                defvalues.add(adpodId);
                contextMap.put(VectorAttribute.kAdpodId, defvalues);
            } else if (expId > 0) {
                defvalues.add(expId);
                contextMap.put(VectorAttribute.kExpId, defvalues);
            }
        }
        if (request.getPageId() != null) {
            List<String> values = parseValues(request.getPageId());
            updateContextMap(values, contextMap, VectorAttribute.kLineId);
        }
        if (request.getRegion() != null) {
            List<String> values = parseValues(request.getRegion());
            updateContextMap(values, contextMap, VectorAttribute.kState);
        }
        if (request.getDmacode() != null) {
            List<String> values = parseValues(request.getDmacode());
            updateContextMap(values, contextMap, VectorAttribute.kDMA);
        }
        if (request.getAreacode() != null) {
            List<String> values = parseValues(request.getAreacode());
            updateContextMap(values, contextMap, VectorAttribute.kArea);
        }
        if (request.get_zip_code() != null) {
            List<String> values = parseValues(request.get_zip_code());
            updateContextMap(values, contextMap, VectorAttribute.kZip);
        }
        if (request.getExternalTargetField1() != null) {
            List<String> values = parseValues(request.getExternalTargetField1());
            updateContextMap(values, contextMap, VectorAttribute.kT1);
        }
        if (request.getExternalTargetField2() != null) {
            List<String> values = parseValues(request.getExternalTargetField2());
            updateContextMap(values, contextMap, VectorAttribute.kT2);
        }
        if (request.getExternalTargetField3() != null) {
            List<String> values = parseValues(request.getExternalTargetField3());
            updateContextMap(values, contextMap, VectorAttribute.kT3);
        }
        if (request.getExternalTargetField4() != null) {
            List<String> values = parseValues(request.getExternalTargetField4());
            updateContextMap(values, contextMap, VectorAttribute.kT4);
        }
        if (request.getExternalTargetField5() != null) {
            List<String> values = parseValues(request.getExternalTargetField5());
            updateContextMap(values, contextMap, VectorAttribute.kT5);
        }
        if (request.getExternalFilterField1() != null) {
            List<String> values = parseValues(request.getExternalFilterField1());
            updateContextMap(values, contextMap, VectorAttribute.kF1);
        }
        if (request.getExternalFilterField2() != null) {
            List<String> values = parseValues(request.getExternalFilterField2());
            updateContextMap(values, contextMap, VectorAttribute.kF2);
        }
        if (request.getExternalFilterField3() != null) {
            List<String> values = parseValues(request.getExternalFilterField3());
            updateContextMap(values, contextMap, VectorAttribute.kF3);
        }
        if (request.getExternalFilterField4() != null) {
            List<String> values = parseValues(request.getExternalFilterField4());
            updateContextMap(values, contextMap, VectorAttribute.kF4);
        }
        if (request.getExternalFilterField5() != null) {
            List<String> values = parseValues(request.getExternalFilterField5());
            updateContextMap(values, contextMap, VectorAttribute.kF5);
        }
        if (request.getUserBucket() != null) {
            List<String> values = parseValues(request.getUserBucket());
            updateContextMap(values, contextMap, VectorAttribute.kUB);
        }
        return contextMap;
    }

    public static List<String> parseValues(String valueString) {
        List<String> retList = null;
        if (valueString != null) {
            StringTokenizer st = new StringTokenizer(valueString, MULTI_VALUE_DELIM);
            retList = st.getTokens();
        }
        return retList;
    }

    private static void updateContextMap(List<String> values, Map<VectorAttribute, List<Integer>> contextMap, VectorAttribute attr) {
        if (values != null) {
            for (String v : values) {
                if (v != null && !(v = v.trim()).isEmpty()) {
                    Integer id = VectorUtils.getDictId(attr, v);
                    if (id != null) {
                        if (attr != null) {
                            List<Integer> currList = contextMap.get(attr);
                            if (currList == null) {
                                currList = new ArrayList<Integer>();
                            }
                            currList.add(id);
                            contextMap.put(attr, currList);
                        }
                    }
                }
            }
        }
    }

    public static String getUniqueIntRangeString(String min, String max) {
        return min + ":" + max;
    }

    public static List<String> getParsedUniqueIntRangeString(String s) {
        List<String> retList = null;
        if (s != null) {
            StringTokenizer st = new StringTokenizer(s, ':');
            retList = st.getTokens();
        } else {
            retList = new ArrayList<String>();
        }
        return retList;
    }

    /**
     * Return the list of attributes that support range
     * @return
     */
    public static List<VectorAttribute> getRangeAttributes() {
        if (rangeAttrs==null) {
            rangeAttrs = new ArrayList<VectorAttribute>();
            rangeAttrs.add(VectorAttribute.kUB);
        }
        return rangeAttrs;
    }

    private static Set<VectorAttribute> requestSet = new HashSet<VectorAttribute>();
    static {
        requestSet.add(VectorAttribute.kLineId);
        requestSet.add(VectorAttribute.kSiteId);
        requestSet.add(VectorAttribute.kCreativeId);
        requestSet.add(VectorAttribute.kBuyId);
        requestSet.add(VectorAttribute.kAdId);
        requestSet.add(VectorAttribute.kState);
        requestSet.add(VectorAttribute.kZip);
        requestSet.add(VectorAttribute.kDMA);
        requestSet.add(VectorAttribute.kArea);
        requestSet.add(VectorAttribute.kCity);
        requestSet.add(VectorAttribute.kCountry);
        requestSet.add(VectorAttribute.kT1);
        requestSet.add(VectorAttribute.kT2);
        requestSet.add(VectorAttribute.kT3);
        requestSet.add(VectorAttribute.kT4);
        requestSet.add(VectorAttribute.kT5);
        requestSet.add(VectorAttribute.kF1);
        requestSet.add(VectorAttribute.kF2);
        requestSet.add(VectorAttribute.kF3);
        requestSet.add(VectorAttribute.kF4);
        requestSet.add(VectorAttribute.kF5);
        requestSet.add(VectorAttribute.kUB);
    }

    private static Set<VectorAttribute> noneAttrSet = new HashSet<VectorAttribute>();
    static {
        noneAttrSet.add(VectorAttribute.kLineIdNone);
        noneAttrSet.add(VectorAttribute.kSiteIdNone);
        noneAttrSet.add(VectorAttribute.kCreativeIdNone);
        noneAttrSet.add(VectorAttribute.kBuyIdNone);
        noneAttrSet.add(VectorAttribute.kAdIdNone);
        noneAttrSet.add(VectorAttribute.kStateNone);
        noneAttrSet.add(VectorAttribute.kZipNone);
        noneAttrSet.add(VectorAttribute.kDMANone);
        noneAttrSet.add(VectorAttribute.kAreaNone);
        noneAttrSet.add(VectorAttribute.kCityNone);
        noneAttrSet.add(VectorAttribute.kCountryNone);
        noneAttrSet.add(VectorAttribute.kT1None);
        noneAttrSet.add(VectorAttribute.kT2None);
        noneAttrSet.add(VectorAttribute.kT3None);
        noneAttrSet.add(VectorAttribute.kT4None);
        noneAttrSet.add(VectorAttribute.kT5None);
        noneAttrSet.add(VectorAttribute.kF1None);
        noneAttrSet.add(VectorAttribute.kF2None);
        noneAttrSet.add(VectorAttribute.kF3None);
        noneAttrSet.add(VectorAttribute.kF4None);
        noneAttrSet.add(VectorAttribute.kF5None);
        noneAttrSet.add(VectorAttribute.kUBNone);
    }

    public static Set<VectorAttribute> findNoneAttributes(Set<VectorAttribute> inSet) {
        Set<VectorAttribute> noneSet = new HashSet<VectorAttribute>();
        for (VectorAttribute attr: requestSet) {
            if (!inSet.contains(attr)) {
                VectorAttribute nAttr = getNoneAttribute(attr);
                if (nAttr!=null) {
                    noneSet.add(getNoneAttribute(attr));
                }
            }
        }
        return noneSet;
    }


    /**
     * Constructs an id from experience id and a vector id
     * @param expId
     * @param vectorId
     * @return
     */
    public static long createId(int expId, int vectorId) {
        long leid = expId;
        long id = (leid << 32) & 0xFFFFFFFF00000000L;
        id = id | (vectorId & 0x00000000FFFFFFFFL);
        return id;
    }

    /**
     * Breaks up the id back into the experience id and the vector id
     * @param id
     * @return - array of length 2. The 0th position is the vector id and the 1st position is the exp id.
     */
    public static int[] getIdDetails(long id) {
        int[] dets = new int[2];
        dets[0] = (int)id;
        long hiVal = id >> 32;
        dets[1]= (int)hiVal;
        return dets;
    }

}
