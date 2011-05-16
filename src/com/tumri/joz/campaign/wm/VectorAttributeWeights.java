package com.tumri.joz.campaign.wm;

import com.tumri.joz.products.Handle;
import com.tumri.joz.ranks.IWeight;

/**
 * AttributeWeights implementation
 */
public class VectorAttributeWeights implements IWeight<Handle> {

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
	private static double hhi = 1.0;
	private static double gender = 1.0;
	private static double cc = 1.0;
	private static double bt = 1.0;
	private static double age = 1.0;
	private static double kExpId = 0.2;
	private static double kAdpodId = 0.2;
	private static double kLineIdNone = 0.2;
	private static double kSiteIdNone = 0.2;
	private static double kCreativeIdNone = 0.2;
	private static double kAdIdNone = 0.2;
	private static double kBuyIdNone = 0.2;
	private static double kStateNone = 0.2;
	private static double kCountryNone = 0.2;
	private static double kCityNone = 0.2;
	private static double kAreaNone = 0.2;
	private static double kZipNone = 0.2;
	private static double kDMANone = 0.2;
	private static double kT1None = 0.2;
	private static double kT2None = 0.2;
	private static double kT3None = 0.2;
	private static double kT4None = 0.2;
	private static double kT5None = 0.2;
	private static double kF1None = 0.2;
	private static double kF2None = 0.2;
	private static double kF3None = 0.2;
	private static double kF4None = 0.2;
	private static double kF5None = 0.2;
	private static double ubNone = 0.2;
	private static double kDefault = 0.5;

	private VectorHandle requestHandle = null;
	private VectorAttribute attr = null;

	public VectorAttributeWeights(VectorHandle requestHandle, VectorAttribute kAttr) {
		this.requestHandle = requestHandle;
		attr = kAttr;
	}

	/**
	 * The weight returned here is a calculation based on the VectorHandle's normalization factor
	 * and the request handles norm factor
	 * N factor of handle * N factor of request * ( SUM of square of Attribute Weights )
	 *
	 * @param v
	 * @return
	 */
	public double getWeight(Handle v, double minWeight) {
		VectorHandle h = (VectorHandle) v;
		if (h.isNoneHandle()) {
			VectorAttribute currAttr = VectorUtils.getNoneAttribute(attr);
			return getDefaultAttributeWeight(currAttr);
		}
		//Return 0 if the this handle is not a proper subset of the request context
		if (!requestHandle.isDimensionSubset(h)) {
			return 0.0;
		}

		return getDefaultAttributeWeight(attr);
	}

	public int match(Handle v) {
		return 1;
	}

	public boolean mustMatch() {
		return true;
	}

	public double getMaxWeight() {
		return 1;
	}

	public double getMinWeight() {
		return .2;
	}

	public static double getDefaultAttributeWeight(VectorAttribute attr) {
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
			case kGender:
				return gender;
			case kHHI:
				return hhi;
			case kChildCount:
				return cc;
			case kAge:
				return age;
			case kBT:
				return bt;
			case kLineIdNone:
				return kLineIdNone;
			case kSiteIdNone:
				return kSiteIdNone;
			case kCreativeIdNone:
				return kCreativeIdNone;
			case kAdIdNone:
				return kAdIdNone;
			case kBuyIdNone:
				return kBuyIdNone;
			case kStateNone:
				return kStateNone;
			case kCountryNone:
				return kCountryNone;
			case kCityNone:
				return kCityNone;
			case kAreaNone:
				return kAreaNone;
			case kZipNone:
				return kZipNone;
			case kDMANone:
				return kDMANone;
			case kT1None:
				return kT1None;
			case kT2None:
				return kT2None;
			case kT3None:
				return kT3None;
			case kT4None:
				return kT4None;
			case kT5None:
				return kT5None;
			case kF1None:
				return kF1None;
			case kF2None:
				return kF2None;
			case kF3None:
				return kF3None;
			case kF4None:
				return kF4None;
			case kF5None:
				return kF5None;
			case kUBNone:
				return ubNone;
			case kAdpodId:
				return kAdpodId;
			case kExpId:
				return kExpId;
			default:
		}
		return 0.2;
	}
}
