/**
 * 
 */
package com.tumri.joz.targeting;

import com.tumri.cma.domain.*;

import java.util.ArrayList;

/**
 * @author omprakash
 * @date May 19, 2014
 * @time 1:08:12 PM
 */
public class DomainTestDataProvider {
	private static int counter = 1;

	public static Geocode getNewGeocode() {
		Geocode geocode = new Geocode();
		ArrayList<String> countries = new ArrayList<String>();
		if (System.currentTimeMillis() % 3 == 0) {
			countries.add("USA");
		}
		countries.add("CN");
		ArrayList<String> states = new ArrayList<String>();
		if (System.currentTimeMillis() % 3 == 0) {
			states.add("CA");
		}
		states.add("FA");
		ArrayList<String> cities = new ArrayList<String>();
		cities.add("LA");
		cities.add("SF");
		ArrayList<String> zipcodes = new ArrayList<String>();
		if (System.currentTimeMillis() % 2 == 0) {
			zipcodes.add("94404");
		}
		zipcodes.add("94555");
		ArrayList<String> dmacodes = new ArrayList<String>();
		ArrayList<String> areacodes = new ArrayList<String>();
		if (System.currentTimeMillis() % 3 == 0) {
			dmacodes.add("DMA1");
			dmacodes.add("DMA2");
			areacodes.add("650");
			areacodes.add("408");
		}
		geocode.setCountries(countries);
		geocode.setStates(states);
		geocode.setCities(cities);
		geocode.setZipcodes(zipcodes);
		geocode.setDmaCodes(dmacodes);
		geocode.setAreaCodes(areacodes);
		return geocode;
	}

	public static Url getNewShallowUrl() {
		Url url = new Url();
		url.setName("URL " + System.currentTimeMillis());
		url.setPublish(true);
		url.setOwnerId("advuser1");
		url.setRegion("USA");
		url.setSource("ADVERTISER");
		return url;
	}

	public static Location getNewShallowLocation() {
		Location location = new Location();
		location.setName("Location " + System.currentTimeMillis());
		location.setPublish(true);
		location.setOwnerId("advuser1");
		location.setRegion("USA");
		location.setSource("ADVERTISER");
		return location;
	}

	public static Campaign getNewShallowCampaign() {
		String name = "Campaign " + counter++ + System.currentTimeMillis();
		return getNewShallowCampaign(name);
	}

	public static Campaign getNewShallowCampaign(String name) {
		Campaign campaign = new Campaign();
		campaign.setName(name);
		campaign.setPublish(true);
		campaign.setSource("ADVERTISER");
		campaign.setRegion("USA");
		campaign.setOwnerId("advuser1");

		return campaign;
	}

	public static AdPod getNewAdPod() {
		String name = "AdPod " + counter++ + System.currentTimeMillis();
		return getNewAdPod(name);
	}

	public static AdPod getNewAdPod(String name) {
		AdPod adPod = new AdPod();
		adPod.setName(name);
		adPod.setPublish(true);

		adPod.setSource("ADVERTISER");
		adPod.setRegion("USA");
		adPod.setOwnerId("advuser1");

		return adPod;
	}

	public static OSpec getNewOSpec() {
		return getNewOSpec("OSpec " + counter++ + System.currentTimeMillis());
	}

	public static OSpec getNewOSpec(String name) {
		return getNewOSpec(name, "advuser1", "ADVERTISER", "USA");
	}

	public static OSpec getNewOSpec(String name, String ownerId, String source,
			String region) {
		OSpec oSpec = new OSpec();
		oSpec.setName(name);
		oSpec.setOwnerId(ownerId);
		oSpec.setPublish(true);
		oSpec.setRegion(region);
		oSpec.setSource(source);
		if (System.currentTimeMillis() % 2 == 0) {
			oSpec.setMinePubUrl(true);
		}
		return oSpec;
	}

	public static TSpec getNewTSpec() {
		TSpec tSpec = new TSpec();
		tSpec.setPublicURL("http://www." + counter++ + ".com");
		tSpec.setLoadTimeKeywordExpression("ltke");
		tSpec.setPublicURLKeyword(true);
		tSpec.setPublicURLQueryNames("puqn");
		tSpec.setPublicUrlStopWords("stop words");
		tSpec.setSpecType(":LEADGEN");
		tSpec.setLowPrice(5 + counter++);
		tSpec.setLowCPC(counter++);
		tSpec.setHighCPC(10 + counter++);
		tSpec.setHighPrice(20 + counter++);
		tSpec.setCountryFilter("US");
		tSpec.setStateFilter("CA,AL");
		tSpec.setCityFilter("Redwood City,Mountain View");
		tSpec.setZipCodeFilter("95119,94065");
		tSpec.setDmaCodeFilter("650");
		tSpec.setAreaCodeFilter("650");
		// tSpec.setGeoEnabledFlag(true);
		// tSpec.setGlobalId("22");

		if (System.currentTimeMillis() % 2 == 0) {
			tSpec.setPublicURL("Just Inserted TSpec");
			ArrayList<CategoryAttribute> attribs = new ArrayList<CategoryAttribute>();
			CategoryAttribute ca1 = new CategoryAttribute(
					"AttribName1@@AttribValue1");
			CategoryAttribute ca2 = new CategoryAttribute(
					"AttribName2@@10@@2000");
			attribs.add(ca1);
			attribs.add(ca2);

			CategoryInfo cInfo1 = new CategoryInfo();
			cInfo1.setDisplayName("Sample Category 1");
			cInfo1.setName("cat1");
			cInfo1.addAttributes(attribs);
			CategoryInfo cInfo2 = new CategoryInfo();
			cInfo2.setDisplayName("Sample Category 2");
			cInfo2.setName("cat2");
			cInfo1.addAttributes(attribs);
			CategoryInfo cInfo3 = new CategoryInfo();
			cInfo3.setDisplayName("Sample Category 3");
			cInfo3.setName("cat3");
			cInfo1.addAttributes(attribs);
			CategoryInfo cInfo4 = new CategoryInfo();
			cInfo4.setDisplayName("Sample Category 4");
			cInfo4.setName("cat4");
			// cInfo4.setAttribs(cia);

			ArrayList<CategoryInfo> cInfoList = new ArrayList<CategoryInfo>();
			cInfoList.add(cInfo1);
			cInfoList.add(cInfo2);
			cInfoList.add(cInfo3);
			cInfoList.add(cInfo4);
			ArrayList<MerchantInfo> includedMerchants = new ArrayList();
			includedMerchants.add(new MerchantInfo("merchant1"));
			includedMerchants.add(new MerchantInfo("merchant2"));
			includedMerchants.add(new MerchantInfo("merchant3"));
			ArrayList<ProviderInfo> includeProviders = new ArrayList();
			includeProviders.add(new ProviderInfo("prvdr " + counter++));
			tSpec.setIncludedCategories(cInfoList);
			tSpec.setIncludedMerchants(includedMerchants);
			tSpec.setIncludedProviders(includeProviders);
		} else {
			tSpec.setLowIncome("100K+");
			tSpec.setHighIncome("1Million+");
			BrandInfo bInfo1 = new BrandInfo();
			BrandInfo bInfo2 = new BrandInfo();
			BrandInfo bInfo3 = new BrandInfo();
			bInfo1.setName("br1");
			bInfo1.setDisplayName("brand " + counter++);
			bInfo2.setName("br2");
			bInfo2.setDisplayName("brand " + counter++);
			bInfo3.setName("br3");
			bInfo3.setDisplayName("brand " + counter++);
			ArrayList<BrandInfo> bInfoList = new ArrayList<BrandInfo>();
			bInfoList.add(bInfo1);
			bInfoList.add(bInfo2);
			bInfoList.add(bInfo3);
			tSpec.setIncludedBrands(bInfoList);
		}
		return tSpec;
	}
}
