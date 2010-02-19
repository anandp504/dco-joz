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
public class WMUtils {
	private static final char MULTI_VALUE_DELIM = AppProperties.getInstance().getMultiValueDelimiter();

	public static WMAttribute getAttribute(String type) {
		WMAttribute attr = null;
		if ("lineid".equals(type)) {
			attr = WMAttribute.kLineId;
		} else if ("EXTERNALPAGEID".equals(type)) {
			attr = WMAttribute.kLineId;
		} else if ("siteid".equals(type)) {
			attr = WMAttribute.kSiteId;
		} else if ("adid".equals(type)) {
			attr = WMAttribute.kAdId;
		} else if ("creativeid".equals(type)) {
			attr = WMAttribute.kCreativeId;
		} else if ("buyid".equals(type)) {
			attr = WMAttribute.kBuyId;
		} else if ("state".equals(type)) {
			attr = WMAttribute.kState;
		} else if ("city".equals(type)) {
			attr = WMAttribute.kCity;
		} else if ("zip".equals(type)) {
			attr = WMAttribute.kZip;
		} else if ("country".equals(type)) {
			attr = WMAttribute.kCountry;
		} else if ("dma".equals(type)) {
			attr = WMAttribute.kDMA;
		} else if ("area".equals(type)) {
			attr = WMAttribute.kArea;
		} else if ("t1".equals(type)) {
			attr = WMAttribute.kT1;
		} else if ("t2".equals(type)) {
			attr = WMAttribute.kT2;
		} else if ("t3".equals(type)) {
			attr = WMAttribute.kT3;
		} else if ("t4".equals(type)) {
			attr = WMAttribute.kT4;
		} else if ("t5".equals(type)) {
			attr = WMAttribute.kT5;
		} else if ("f1".equals(type)) {
			attr = WMAttribute.kF1;
		} else if ("f2".equals(type)) {
			attr = WMAttribute.kF2;
		} else if ("f3".equals(type)) {
			attr = WMAttribute.kF3;
		} else if ("f4".equals(type)) {
			attr = WMAttribute.kF4;
		} else if ("f5".equals(type)) {
			attr = WMAttribute.kF5;
		} else if ("ub".equals(type)) {
			attr = WMAttribute.ub;
		}
		return attr;
	}

	public static String getIndexName(WMAttribute type) {
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
		WMAttribute attr = getAttribute(type);
		return getDictId(attr, val);
	}

	public static Integer getDictId(WMAttribute attr, String val) {
		Integer ret = null;
		if (attr != null && val != null && !val.isEmpty()) {
			ret = WMDictionaryManager.getInstance().getId(WMAttribute.kRequestVector, attr + val.toLowerCase());
		}
		return ret;
	}

	public static String getDictValue(WMAttribute attr, Integer id) {
		String val = null;
		if (attr != null && id != null) {
			String tmpVal = (String) WMDictionaryManager.getInstance().getValue(WMAttribute.kRequestVector, id);
			if (tmpVal != null && tmpVal.length() > attr.name().length()) {
				val = tmpVal.substring(attr.name().length(), tmpVal.length());
			}
		}
		return val;
	}

	/**
	 * Construct the context map from the ad request
	 *
	 * @param request
	 * @return
	 */
	public static Map<WMAttribute, List<Integer>> getContextMap(AdDataRequest request) {
		Map<WMAttribute, List<Integer>> contextMap = new HashMap<WMAttribute, List<Integer>>();
		if (request.getPageId() != null) {
			List<String> values = parseValues(request.getPageId());
			updateContextMap(values, contextMap, WMAttribute.kLineId);
		}
		if (request.getRegion() != null) {
			List<String> values = parseValues(request.getRegion());
			updateContextMap(values, contextMap, WMAttribute.kState);
		}
		if (request.getDmacode() != null) {
			List<String> values = parseValues(request.getDmacode());
			updateContextMap(values, contextMap, WMAttribute.kDMA);
		}
		if (request.getAreacode() != null) {
			List<String> values = parseValues(request.getAreacode());
			updateContextMap(values, contextMap, WMAttribute.kArea);
		}
		if (request.getExternalTargetField1() != null) {
			List<String> values = parseValues(request.getExternalTargetField1());
			updateContextMap(values, contextMap, WMAttribute.kT1);
		}
		if (request.getExternalTargetField2() != null) {
			List<String> values = parseValues(request.getExternalTargetField2());
			updateContextMap(values, contextMap, WMAttribute.kT2);
		}
		if (request.getExternalTargetField3() != null) {
			List<String> values = parseValues(request.getExternalTargetField3());
			updateContextMap(values, contextMap, WMAttribute.kT3);
		}
		if (request.getExternalTargetField4() != null) {
			List<String> values = parseValues(request.getExternalTargetField4());
			updateContextMap(values, contextMap, WMAttribute.kT4);
		}
		if (request.getExternalTargetField5() != null) {
			List<String> values = parseValues(request.getExternalTargetField5());
			updateContextMap(values, contextMap, WMAttribute.kT5);
		}
		if (request.getExternalFilterField1() != null) {
			List<String> values = parseValues(request.getExternalFilterField1());
			updateContextMap(values, contextMap, WMAttribute.kF1);
		}
		if (request.getExternalFilterField2() != null) {
			List<String> values = parseValues(request.getExternalFilterField2());
			updateContextMap(values, contextMap, WMAttribute.kF2);
		}
		if (request.getExternalFilterField3() != null) {
			List<String> values = parseValues(request.getExternalFilterField3());
			updateContextMap(values, contextMap, WMAttribute.kF3);
		}
		if (request.getExternalFilterField4() != null) {
			List<String> values = parseValues(request.getExternalFilterField4());
			updateContextMap(values, contextMap, WMAttribute.kF4);
		}
		if (request.getExternalFilterField5() != null) {
			List<String> values = parseValues(request.getExternalFilterField5());
			updateContextMap(values, contextMap, WMAttribute.kF5);
		}
		if (request.getUserBucket() != null) {
			List<String> values = parseValues(request.getUserBucket());
			updateContextMap(values, contextMap, WMAttribute.ub);
		}
		return contextMap;
	}

	private static List<String> parseValues(String valueString) {
		List<String> retList = null;
		if (valueString != null) {
			StringTokenizer st = new StringTokenizer(valueString, MULTI_VALUE_DELIM);
			retList = st.getTokens();
		}
		return retList;
	}

	private static void updateContextMap(List<String> values, Map<WMAttribute, List<Integer>> contextMap, WMAttribute attr) {
		List<Integer> retList = new ArrayList<Integer>();
		if (values != null) {
			for (String v : values) {
				if (v != null && !(v = v.trim()).isEmpty()) {
					Integer id = WMUtils.getDictId(attr, v);
					if (id != null) {
						if (attr != null && id != null) {
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

}
