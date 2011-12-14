package com.tumri.joz.campaign.wm.loader;

import com.tumri.joz.campaign.wm.VectorAttribute;
import com.tumri.joz.campaign.wm.VectorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: scbraun
 * Date: Dec 14, 2011
 */
public class VNodeHandlerUtils {


	public static Map<VectorAttribute, List<Integer>> explodeRequestMap(Map<VectorAttribute, String> reqMap) {
		//Get the map of att to list of integers
		Map<VectorAttribute, List<Integer>> idMap = new HashMap<VectorAttribute, List<Integer>>();
		int count = 0;
		for (VectorAttribute attr : reqMap.keySet()) {
			List<String> parsedList = VectorUtils.parseValues(reqMap.get(attr));
			for (String val : parsedList) {
				if (val == null) {
					continue;
				}
				if (VectorAttribute.kUB.equals(attr)) { //block for special handling of UB range: add every value from min thru max to dictionary.
					List<String> ubVals = VectorUtils.getParsedUniqueIntRangeString(val);
					if (ubVals.size() == 2) {
						String minS = ubVals.get(0);
						String maxS = ubVals.get(1);
						int min = Integer.parseInt(minS);
						int max = Integer.parseInt(maxS);
						while (min <= max) {
							Integer id = VectorUtils.getDictId(attr, Integer.toString(min));
							min++;
						}
					}
				}
				Integer id = VectorUtils.getDictId(attr, val);
				List<Integer> idList = idMap.get(attr);
				if (idList == null) {
					idList = new ArrayList<Integer>();
				}
				idList.add(id);
				idMap.put(attr, idList);
			}
			if (count == 0) {
				count = parsedList.size();
			} else {
				count = count * parsedList.size();
			}
		}
		return idMap;
	}
}
