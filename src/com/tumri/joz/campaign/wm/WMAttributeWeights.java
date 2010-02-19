package com.tumri.joz.campaign.wm;

import com.tumri.joz.products.Handle;
import com.tumri.joz.ranks.IWeight;

import java.util.Map;
import java.util.Set;

/**
 * AttributeWeights implementation
 */
public class WMAttributeWeights implements IWeight<Handle> {

	private static double kLineId = 1.0;
	private static double kSiteId = 1.0;
	private static double kCreativeId = 1.0;
	private static double kAdId = 1.0;
	private static double kBuyId = 1.0;
	private static double kState = 1.0;
	private static double kCountry = 1.0;
	private static double kCity = 1.0;
	private static double kArea = 1.0;
	private static double kZip = 1.0;
	private static double kDMA = 1.0;
	private static double kT1 = 1.0;
	private static double kT2 = 1.0;
	private static double kT3 = 1.0;
	private static double kT4 = 1.0;
	private static double kT5 = 1.0;
	private static double kF1 = 1.0;
	private static double kF2 = 1.0;
	private static double kF3 = 1.0;
	private static double kF4 = 1.0;
	private static double kF5 = 1.0;
	private static double ub = 1.0;

	private WMHandle requestHandle = null;
	private WMAttribute attr = null;

	public WMAttributeWeights(WMHandle requestHandle, WMAttribute kAttr) {
		this.requestHandle = requestHandle;
		attr = kAttr;
	}

	/**
	 * The weight returned here is a calculation based on the WMHandle's normalization factor
	 * and the request handles norm factor
	 * N factor of handle * N factor of request * ( SUM of square of Attribute Weights )
	 *
	 * @param v
	 * @return
	 */
	public double getWeight(Handle v) {
		WMHandle h = (WMHandle) v;
		//Return 0 if the this handle is not a proper subset of the request context
		Map<WMAttribute, Integer> reqMap = requestHandle.getContextMap();
		if (reqMap != null && !reqMap.isEmpty()) {
			Set<WMAttribute> attrs = reqMap.keySet();
			Map<WMAttribute, Integer> conMap = h.getContextMap();
			if (conMap == null || conMap.size() == 0 || reqMap.size() < conMap.size()) {
				return 0.0;
			} else {
				Set<WMAttribute> cattrs = conMap.keySet();
				if (!attrs.containsAll(cattrs)) {
					//Not a proper subset
					return 0.0;
				}
			}
		} else {
			return 0.0;
		}
		double wt = getDefaultAttributeWeight(attr);
		return (wt * wt) * h.getNormFactor() * requestHandle.getNormFactor();
	}

	public int match(Handle v) {
		return 1;
	}

	public boolean mustMatch() {
		return true;
	}

	public static double getDefaultAttributeWeight(WMAttribute attr) {
		switch (attr) {
			case kLineId:
				return kLineId;
			case kSiteId:
				return kSiteId;
			case kCreativeId:
				return kCreativeId;
			case kAdId:
				return kAdId;
			case kBuyId:
				return kBuyId;
			case kState:
				return kState;
			case kCountry:
				return kCountry;
			case kCity:
				return kCity;
			case kArea:
				return kArea;
			case kZip:
				return kZip;
			case kDMA:
				return kDMA;
			case kT1:
				return kT1;
			case kT2:
				return kT2;
			case kT3:
				return kT3;
			case kT4:
				return kT4;
			case kT5:
				return kT5;
			case kF1:
				return kF1;
			case kF2:
				return kF2;
			case kF3:
				return kF3;
			case kF4:
				return kF4;
			case kF5:
				return kF5;
			case ub:
				return ub;
			default:
		}
		return 1.0;
	}
}
