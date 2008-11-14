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
    private String multiValueQuery1 = null;
    private String multiValueQuery2 = null;
    private String multiValueQuery3 = null;
    private String multiValueQuery4 = null;
    private String multiValueQuery5 = null;
    
    private int adWidth = -1;
    private int adHeight = -1;

    
    //Geo
    private String countryCode = null;
    private String stateCode = null;
    private String cityCode = null;
    private String zipCode = null;
    private String areaCode = null;
    private String dmaCode = null;

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

    public String getMultiValueQuery1() {
        return multiValueQuery1;
    }

    public void setMultiValueQuery1(String multiValueQuery1) {
        this.multiValueQuery1 = multiValueQuery1;
    }

    public String getMultiValueQuery2() {
        return multiValueQuery2;
    }

    public void setMultiValueQuery2(String multiValueQuery2) {
        this.multiValueQuery2 = multiValueQuery2;
    }

    public String getMultiValueQuery3() {
        return multiValueQuery3;
    }

    public void setMultiValueQuery3(String multiValueQuery3) {
        this.multiValueQuery3 = multiValueQuery3;
    }

    public String getMultiValueQuery4() {
        return multiValueQuery4;
    }

    public void setMultiValueQuery4(String multiValueQuery4) {
        this.multiValueQuery4 = multiValueQuery4;
    }

    public String getMultiValueQuery5() {
        return multiValueQuery5;
    }

    public void setMultiValueQuery5(String multiValueQuery5) {
        this.multiValueQuery5 = multiValueQuery5;
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
		sb.append("multiValueQuery1 = ".toUpperCase() + multiValueQuery1 + " ");
		sb.append("multiValueQuery2 = ".toUpperCase() + multiValueQuery2 + " ");
		sb.append("multiValueQuery3 = ".toUpperCase() + multiValueQuery3 + " ");
		sb.append("multiValueQuery4 = ".toUpperCase() + multiValueQuery4 + " ");
		sb.append("multiValueQuery5 = ".toUpperCase() + multiValueQuery5 + " ");
		sb.append("adWidth = ".toUpperCase() + adWidth + " ");
		sb.append("adHeight = ".toUpperCase() + adHeight + " ");
		sb.append("bMineUrls = ".toUpperCase() + bMineUrls + " ");

		sb.append("countryCode = ".toUpperCase() + countryCode + " ");
		sb.append("stateCode = ".toUpperCase() + stateCode + " ");
		sb.append("cityCode = ".toUpperCase() + cityCode + " ");
		sb.append("zipCode = ".toUpperCase() + zipCode + " ");
		sb.append("areaCode = ".toUpperCase() + areaCode + " ");
		sb.append("dmaCode = ".toUpperCase() + dmaCode + " ");

		return sb.toString();
	}
}
