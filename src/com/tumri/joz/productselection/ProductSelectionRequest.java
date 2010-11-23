/*
 * ListingsQueryHandler.java
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
package com.tumri.joz.productselection;

import com.tumri.joz.rules.ListingClause;
import com.tumri.joz.jozMain.AdDataRequest;

/**
 * Request Object for the TSpecExecutor
 * @author: nipun
 * Date: Jun 26, 2008
 * Time: 3:43:53 PM
 */
public class ProductSelectionRequest {

    private boolean bRandomize = false;
    private boolean bMineUrls = false;
    private boolean bSearchWithinTSpec = true;
    private String url = null;

    private int pageSize = -1;
    private int currPage = -1;
    private boolean bPaginate = false;

    private String requestKeyWords = "";
    private String requestCategory = null;
    private AdDataRequest.AdOfferType offerType = null;
    private String externalFilterQuery1 = null;
    private String externalFilterQuery2 = null;
    private String externalFilterQuery3 = null;
    private String externalFilterQuery4 = null;
    private String externalFilterQuery5 = null;
    
    private String age = null;
    private String gender = null;
    private String hhi = null;
    private String bt = null;
    private String ms = null;

	private String ut1 = null;
    private String ut2 = null;
    private String ut3 = null;
    private String ut4 = null;
    private String ut5 = null;

    private int adWidth = -1;
    private int adHeight = -1;

    
    //Geo
    private String countryCode = null;
    private String stateCode = null;
    private String cityCode = null;
    private String zipCode = null;
    private String areaCode = null;
    private String dmaCode = null;

    private String advertiser = null;

    private ListingClause listingClause = null;
    private boolean useTopK = false;

    public boolean isBRandomize() {
        return bRandomize;
    }

    public void setBRandomize(boolean bRandomize) {
        this.bRandomize = bRandomize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrPage() {
        return currPage;
    }

    public void setCurrPage(int currPage) {
        this.currPage = currPage;
    }

    public String getRequestKeyWords() {
        return requestKeyWords;
    }

    public void setRequestKeyWords(String requestKeyWords) {
        this.requestKeyWords = requestKeyWords;
    }

    public String getRequestCategory() {
        return requestCategory;
    }

    public void setRequestCategory(String requestCategory) {
        this.requestCategory = requestCategory;
    }

    public AdDataRequest.AdOfferType getOfferType() {
        return offerType;
    }

    public void setOfferType(AdDataRequest.AdOfferType offerType) {
        this.offerType = offerType;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getDmaCode() {
        return dmaCode;
    }

    public void setDmaCode(String dmaCode) {
        this.dmaCode = dmaCode;
    }

    public boolean isBMineUrls() {
        return bMineUrls;
    }

    public void setBMineUrls(boolean bMineUrls) {
        this.bMineUrls = bMineUrls;
    }

    public int getAdHeight() {
        return adHeight;
    }

    public void setAdHeight(int adHeight) {
        this.adHeight = adHeight;
    }

    public int getAdWidth() {
        return adWidth;
    }

    public void setAdWidth(int adWidth) {
        this.adWidth = adWidth;
    }

    public boolean isBSearchWithinTSpec() {
        return bSearchWithinTSpec;
    }

    public void setBSearchWithinTSpec(boolean bSearchWithinTSpec) {
        this.bSearchWithinTSpec = bSearchWithinTSpec;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isBPaginate() {
        return bPaginate;
    }

    public void setBPaginate(boolean bPaginate) {
        this.bPaginate = bPaginate;
    }

    public String getExternalFilterQuery1() {
        return externalFilterQuery1;
    }

    public void setExternalFilterQuery1(String externalFilterQuery1) {
        this.externalFilterQuery1 = externalFilterQuery1;
    }

    public String getExternalFilterQuery2() {
        return externalFilterQuery2;
    }

    public void setExternalFilterQuery2(String externalFilterQuery2) {
        this.externalFilterQuery2 = externalFilterQuery2;
    }

    public String getExternalFilterQuery3() {
        return externalFilterQuery3;
    }

    public void setExternalFilterQuery3(String externalFilterQuery3) {
        this.externalFilterQuery3 = externalFilterQuery3;
    }

    public String getExternalFilterQuery4() {
        return externalFilterQuery4;
    }

    public void setExternalFilterQuery4(String externalFilterQuery4) {
        this.externalFilterQuery4 = externalFilterQuery4;
    }

    public String getExternalFilterQuery5() {
        return externalFilterQuery5;
    }

    public void setExternalFilterQuery5(String externalFilterQuery5) {
        this.externalFilterQuery5 = externalFilterQuery5;
    }

    public String getAdvertiser() {
        return advertiser;
    }

    public void setAdvertiser(String advertiser) {
        this.advertiser = advertiser;
    }

    public ListingClause getListingClause() {
        return listingClause;
    }

    public void setListingClause(ListingClause listingClause) {
        this.listingClause = listingClause;
    }

    public String getMs() {
        return ms;
    }

    public void setMs(String ms) {
        this.ms = ms;
    }

	public String getUt1() {
		return ut1;
	}

	public void setUt1(String ut1) {
		this.ut1 = ut1;
	}

	public String getUt2() {
		return ut2;
	}

	public void setUt2(String ut2) {
		this.ut2 = ut2;
	}

	public String getUt3() {
		return ut3;
	}

	public void setUt3(String ut3) {
		this.ut3 = ut3;
	}

	public String getUt4() {
		return ut4;
	}

	public void setUt4(String ut4) {
		this.ut4 = ut4;
	}

	public String getUt5() {
		return ut5;
	}

	public void setUt5(String ut5) {
		this.ut5 = ut5;
	}

	public String getBt() {
        return bt;
    }

    public void setBt(String bt) {
        this.bt = bt;
    }

    public String getHhi() {
        return hhi;
    }

    public void setHhi(String hhi) {
        this.hhi = hhi;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public boolean isUseTopK() {
        return useTopK;
    }

    public void setUseTopK(boolean useTopK) {
        this.useTopK = useTopK;
    }

    public String toString(){
		StringBuffer sb = new StringBuffer();

		sb.append("bRandomize = ".toUpperCase() + bRandomize + " ");
		sb.append("bMineUrls = ".toUpperCase() + bMineUrls + " ");
		sb.append("bSearchWithinTSpec = ".toUpperCase() + bSearchWithinTSpec + " ");
		sb.append("url = ".toUpperCase() + url + " ");
		sb.append("pageSize = ".toUpperCase() + pageSize + " ");
		sb.append("currPage = ".toUpperCase() + currPage + " ");
		sb.append("bPaginate = ".toUpperCase() + bPaginate + " ");
		sb.append("requestKeyWords = ".toUpperCase() + requestKeyWords + " ");
		sb.append("requestCategory = ".toUpperCase() + requestCategory + " ");
		sb.append("offerType = ".toUpperCase() + offerType + " ");
		sb.append("externalFilterQuery1 = ".toUpperCase() + externalFilterQuery1 + " ");
		sb.append("externalFilterQuery2 = ".toUpperCase() + externalFilterQuery2 + " ");
		sb.append("externalFilterQuery3 = ".toUpperCase() + externalFilterQuery3 + " ");
		sb.append("externalFilterQuery4 = ".toUpperCase() + externalFilterQuery4 + " ");
		sb.append("externalFilterQuery5 = ".toUpperCase() + externalFilterQuery5 + " ");
		sb.append("adWidth = ".toUpperCase() + adWidth + " ");
		sb.append("adHeight = ".toUpperCase() + adHeight + " ");
		sb.append("bMineUrls = ".toUpperCase() + bMineUrls + " ");

		sb.append("countryCode = ".toUpperCase() + countryCode + " ");
		sb.append("stateCode = ".toUpperCase() + stateCode + " ");
		sb.append("cityCode = ".toUpperCase() + cityCode + " ");
		sb.append("zipCode = ".toUpperCase() + zipCode + " ");
		sb.append("areaCode = ".toUpperCase() + areaCode + " ");
		sb.append("dmaCode = ".toUpperCase() + dmaCode + " ");

        sb.append("advertiser = ".toUpperCase() + advertiser + " ");

		return sb.toString();
	}
}
