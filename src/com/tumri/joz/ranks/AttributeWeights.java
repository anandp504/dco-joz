package com.tumri.joz.ranks;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
abstract public class AttributeWeights implements IWeight<Handle> {
	private static double kCategory = 1.2;
	private static double kPrice = 1.2;
	private static double kBrand = 1.2;
	private static double kSupplier = 1.2;
	private static double kProvider = 1.2;
	private static double kCPC = 1.2;
	private static double kCPO = 1.2;
	private static double kKeywords = 3.0;
	private static double kRank = 1.0;
	private static double kDiscount = 1.0;
	private static double kProductType = 1.2;
	private static double kImageHeight = 1.2;
	private static double kImageWidth = 1.2;
	private static double kCountry = 1.05;
	private static double kState = 1.10;
	private static double kCity = 1.15;
	private static double kZipCode = 1.40;
	private static double kRadius = 1.10;
	private static double kDmaCode = 1.20;
	private static double kAreaCode = 1.05;
	private static double kGeoEnabled = 1.0;
	private static double kGlobalId = 1.2;
	private static double kMultiValueTextField = 1.05;
	private static double kOpt = 1.001;
	private static double kNone = 1.0;

	protected static double getMaxWeight(IProduct.Attribute attr) {
		switch (attr) {
			case kCategory:
				return kCategory;
			case kPrice:
				return kPrice;
			case kBrand:
				return kBrand;
			case kSupplier:
				return kSupplier;
			case kProvider:
				return kProvider;
			case kCPC:
				return kCPC;
			case kCPO:
				return kCPO;
			case kKeywords:
				return kKeywords;
			case kProductType:
				return kProductType;
			case kImageHeight:
				return kImageHeight;
			case kImageWidth:
				return kImageWidth;
			case kCountry:
				return kCountry;
			case kState:
				return kState;
			case kCity:
				return kCity;
			case kZip:
				return kZipCode;
			case kRadius:
				return kRadius;
			case kDMA:
				return kDmaCode;
			case kArea:
				return kAreaCode;
			case kGeoEnabledFlag:
				return kGeoEnabled;
			case kGlobalId:
				return kGlobalId;
			case kHHI:
			case kBT:
			case kCC:
			case kAge:
			case kGender:
			case kMultiValueTextField:
				return kMultiValueTextField;
			case kDiscount:
				return kDiscount;
			case kRank:
				return kRank;
			case kExperienceId:
			case kExperienceIdF1:
			case kExperienceIdF2:
			case kExperienceIdF3:
			case kExperienceIdF4:
			case kExperienceIdF5:
			case kExperienceIdUT1:
			case kExperienceIdUT2:
			case kExperienceIdUT3:
			case kExperienceIdUT4:
			case kExperienceIdUT5:
				return kOpt;
			case kNone:
				return kNone;
			default:
		}
		return 1.0;
	}

	public int match(Handle h) {
		return 1;
	}

	public static IWeight<Handle> getWeight(IProduct.Attribute attr) {
		switch (attr) {
			case kCategory:
				return CategoryWeight.getInstance();
			case kPrice:
				return PriceWeight.getInstance();
			case kBrand:
				return BrandWeight.getInstance();
			case kSupplier:
				return SupplierWeight.getInstance();
			case kProvider:
				return ProviderWeight.getInstance();
			case kCPC:
				return CPCWeight.getInstance();
			case kCPO:
				return CPOWeight.getInstance();
			case kKeywords:
				return KeywordsWeight.getInstance();
			case kProductType:
				return ProductTypeWeight.getInstance();
			case kImageHeight:
				return ImageHeightWeight.getInstance();
			case kImageWidth:
				return ImageWidthWeight.getInstance();
			case kCountry:
				return CountryWeight.getInstance();
			case kState:
				return StateWeight.getInstance();
			case kCity:
				return CityWeight.getInstance();
			case kZip:
				return ZipCodeWeight.getInstance();
			case kRadius:
				return RadiusWeight.getInstance();
			case kLatitude:
				return RadiusWeight.getInstance();
			case kLongitude:
				return RadiusWeight.getInstance();
			case kArea:
				return AreaCodeWeight.getInstance();
			case kDMA:
				return DmaCodeWeight.getInstance();
			case kGeoEnabledFlag:
				return GeoEnabledWeight.getInstance();
			case kGlobalId:
				return GlobalIdWeight.getInstance();
			case kBT:
			case kHHI:
			case kCC:
			case kAge:
			case kGender:
			case kMultiValueTextField:
				return MultiValueTextFieldWeight.getInstance();
			case kRank:
				return RankWeight.getInstance();
			case kDiscount:
				return DiscountWeight.getInstance();
			case kExperienceId:
			case kExperienceIdF1:
			case kExperienceIdF2:
			case kExperienceIdF3:
			case kExperienceIdF4:
			case kExperienceIdF5:
			case kExperienceIdUT1:
			case kExperienceIdUT2:
			case kExperienceIdUT3:
			case kExperienceIdUT4:
			case kExperienceIdUT5:
				return OptTextFieldWeight.getInstance();
			case kNone:
			default:
				return NeutralWeight.getInstance();
		}
	}

	protected static double getAttributeWeight(IProduct.Attribute attr) {
		switch (attr) {
			case kCategory:
				return kCategory;
			case kPrice:
				return kPrice;
			case kBrand:
				return kBrand;
			case kSupplier:
				return kSupplier;
			case kProvider:
				return kProvider;
			case kCPC:
				return kCPC;
			case kCPO:
				return kCPO;
			case kKeywords:
				return kKeywords;
			case kProductType:
				return kProductType;
			case kImageHeight:
				return kImageHeight;
			case kImageWidth:
				return kImageWidth;
			case kCountry:
				return kCountry;
			case kState:
				return kState;
			case kCity:
				return kCity;
			case kZip:
				return kZipCode;
			case kRadius:
				return kRadius;
			case kDMA:
				return kDmaCode;
			case kArea:
				return kAreaCode;
			case kGeoEnabledFlag:
				return kGeoEnabled;
			case kGlobalId:
				return kGlobalId;
			case kHHI:
			case kBT:
			case kCC:
			case kAge:
			case kGender:
			case kMultiValueTextField:
				return kMultiValueTextField;
			case kDiscount:
				return kDiscount;
			case kRank:
				return kRank;
			case kExperienceId:
			case kExperienceIdF1:
			case kExperienceIdF2:
			case kExperienceIdF3:
			case kExperienceIdF4:
			case kExperienceIdF5:
			case kExperienceIdUT1:
			case kExperienceIdUT2:
			case kExperienceIdUT3:
			case kExperienceIdUT4:
			case kExperienceIdUT5:
				return kOpt;
			case kNone:
				return kNone;
			default:
		}
		return 1.0;
	}


	/**
	 * This returns false by default. Therefore allowing second best match in all derived cases
	 * Overridden in other classes such as ProviderWight to return true. Therefore if provider match is
	 * no found then the result is rejected.
	 *
	 * @return false
	 */
	public boolean mustMatch() {
		return false;
	}
}
