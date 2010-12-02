/**
 * Container class for the ad data request
 */

package com.tumri.joz.jozMain;


import com.tumri.joz.server.domain.JozAdRequest;

import java.util.HashMap;

public class AdDataRequest {

    private String _url = null;

	private String _theme = null;

	private String _store_id = null;

	private String _category = null;

	// if non-null, ignore :url, :theme, and :store-id
	private String _t_spec = null;

	private String _referrer = null;

    private String targetedRealm = null;

	private String _zip_code = null;

	private String _latitude = null;

	private String _longitude = null;

	// "all" means return all products
	private Integer _num_products = null;

	// If row-size and which-row are non-null and integers then
	// deterministically return a row/page of results.
	private Integer _row_size = null;

	private Integer _which_row = null;

	private Boolean _revert_to_default_realm = null;

	// white-space-separated keywords from search box in widget
	private String _keywords = null;

	// white-space-separated keywords from publisher's script
	private String _script_keywords = null;

	// add category-counts to output?
	private Boolean _include_cat_counts = null;

	// for leadgens, only return offers of these dimensions
	private Integer _ad_width = null;

	private Integer _ad_height = null;

	// one of :product-only, :leadgen-only, or product-leadgen
	private AdOfferType _ad_offer_type = null;

	private Integer _max_prod_desc_len = null;

	private String _country;

	private String _region;

	private String _city;

	private String _dmacode;

	private String _areacode;

	private String externalFilterField1 = null;
	private String externalFilterField2 = null;
	private String externalFilterField3 = null;
	private String externalFilterField4 = null;
	private String externalFilterField5 = null;

	private String externalTargetField1 = null;
	private String externalTargetField2 = null;
	private String externalTargetField3 = null;
	private String externalTargetField4 = null;
	private String externalTargetField5 = null;
	private String adType = "";

	private Long recipeId = null;

	private String buyId = null;
	private String siteId = null;
	private String pageId = null;
	private String adId = null;
	private String creativeId = null;
	private String userAgent = null;

	private String ub = null;

    private Integer expId = null;
    private Integer variationId = null;
    private String age=null;
    private String gender = null;
    private String hhi = null;
    private String ms = null;
    private String bt = null;

    private String ut1 = null;
    private String ut2 = null;
    private String ut3 = null;
    private String ut4 = null;
    private String ut5 = null;

    private String useTopK = null;

	/**
	 * Constructor that will get the values from the request
	 *
	 * @param req
	 */
	public AdDataRequest(JozAdRequest req) {
		this._url = req.getValue(JozAdRequest.KEY_URL);
		this._theme = req.getValue(JozAdRequest.KEY_THEME);
		this._store_id = req.getValue(JozAdRequest.KEY_LOCATION_ID);
		this._category = req.getValue(JozAdRequest.KEY_CATEGORY);
		this._t_spec = req.getValue(JozAdRequest.KEY_T_SPEC);
		try {
			this._num_products = Integer.parseInt(req.getValue(JozAdRequest.KEY_NUM_PRODUCTS));
		} catch (Exception e) {
			this._num_products = null;
		}
		try {
			this._row_size = Integer.parseInt(req.getValue(JozAdRequest.KEY_ROW_SIZE));
		} catch (Exception e) {
			this._row_size = null;
		}
		try {
			this._which_row = Integer.parseInt(req.getValue(JozAdRequest.KEY_WHICH_ROW));
		} catch (Exception e) {
			this._which_row = null;
		}

		this._revert_to_default_realm = "true".equalsIgnoreCase(req.getValue(JozAdRequest.KEY_REVERT_TO_DEFAULT_REALM)) ? true : false;
		this._keywords = req.getValue(JozAdRequest.KEY_KEYWORDS);
		this._script_keywords = req.getValue(JozAdRequest.KEY_SCRIPT_KEYWORDS);

		try {
			this._ad_width = Integer.parseInt(req.getValue(JozAdRequest.KEY_AD_WIDTH));
		} catch (Exception e) {
			this._ad_width = null;
		}

		try {
			this._ad_height = Integer.parseInt(req.getValue(JozAdRequest.KEY_AD_HEIGHT));
		} catch (Exception e) {
			this._ad_height = null;
		}

		this._ad_offer_type = "LEADGEN".equals(req.getValue(JozAdRequest.KEY_AD_OFFER_TYPE)) ? AdDataRequest.AdOfferType.LEADGEN_ONLY : AdDataRequest.AdOfferType.PRODUCT_LEADGEN;

		try {
			this._max_prod_desc_len = Integer.parseInt(req.getValue(JozAdRequest.KEY_MAX_PROD_DESC_LEN));
		} catch (Exception e) {
			this._max_prod_desc_len = null;
		}

		this._country = req.getValue(JozAdRequest.KEY_COUNTRY);
		if (_country != null) _country = _country.toLowerCase();
		this._region = req.getValue(JozAdRequest.KEY_REGION);
		if (_region != null) _region = _region.toLowerCase();
		this._city = req.getValue(JozAdRequest.KEY_CITY);
		if (_city != null) _city = _city.toLowerCase();
		this._dmacode = req.getValue(JozAdRequest.KEY_DMACODE);
		if (_dmacode != null) _dmacode = _dmacode.toLowerCase();
		this._areacode = req.getValue(JozAdRequest.KEY_AREACODE);
		if (_areacode != null) _areacode = _areacode.toLowerCase();
		this._zip_code = req.getValue(JozAdRequest.KEY_ZIP_CODE);
		if (_zip_code != null) _zip_code = _zip_code.toLowerCase();
		this._latitude = req.getValue(JozAdRequest.KEY_LATITUDE);
		this._longitude = req.getValue(JozAdRequest.KEY_LONGITUDE);
		this.adType = req.getValue(JozAdRequest.KEY_AD_TYPE);
		try {
			this.recipeId = Long.parseLong(req.getValue(JozAdRequest.KEY_RECIPE_ID));
		} catch (Exception e) {
			this.recipeId = null;
		}
		//For Flexible Product Filtering
		this.externalFilterField1 = req.getValue(JozAdRequest.KEY_EXTERNAL_FILTER_FIELD1);
		if (externalFilterField1 != null) {
			externalFilterField1 = externalFilterField1.trim();
		}
		this.externalFilterField2 = req.getValue(JozAdRequest.KEY_EXTERNAL_FILTER_FIELD2);
		if (externalFilterField2 != null) {
			externalFilterField2 = externalFilterField2.trim();
		}
		this.externalFilterField3 = req.getValue(JozAdRequest.KEY_EXTERNAL_FILTER_FIELD3);
		if (externalFilterField3 != null) {
			externalFilterField3 = externalFilterField3.trim();
		}
		this.externalFilterField4 = req.getValue(JozAdRequest.KEY_EXTERNAL_FILTER_FIELD4);
		if (externalFilterField4 != null) {
			externalFilterField4 = externalFilterField4.trim();
		}
		this.externalFilterField5 = req.getValue(JozAdRequest.KEY_EXTERNAL_FILTER_FIELD5);
		if (externalFilterField5 != null) {
			externalFilterField5 = externalFilterField5.trim();
		}
		//For Flexible AdPod Targeting
		this.externalTargetField1 = req.getValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD1);
		if (externalTargetField1 != null) {
			externalTargetField1 = externalTargetField1.trim();
		}
		this.externalTargetField2 = req.getValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD2);
		if (externalTargetField2 != null) {
			externalTargetField2 = externalTargetField2.trim();
		}
		this.externalTargetField3 = req.getValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD3);
		if (externalTargetField3 != null) {
			externalTargetField3 = externalTargetField3.trim();
		}
		this.externalTargetField4 = req.getValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD4);
		if (externalTargetField4 != null) {
			externalTargetField4 = externalTargetField4.trim();
		}
		this.externalTargetField5 = req.getValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD5);
		if (externalTargetField5 != null) {
			externalTargetField5 = externalTargetField5.trim();
		}
		this.ub = req.getValue(JozAdRequest.KEY_USER_BUCKET);
		if (ub != null) {
			ub = ub.trim();
		}
		this.buyId = req.getValue(JozAdRequest.KEY_EXTERNAL_BUY_ID);
		if (buyId != null) {
			buyId = buyId.trim();
		}
		this.siteId = req.getValue(JozAdRequest.KEY_EXTERNAL_SITE_ID);
		if (siteId != null) {
			siteId = siteId.trim();
		}
		this.creativeId = req.getValue(JozAdRequest.KEY_EXTERNAL_CREATIVE_ID);
		if (creativeId != null) {
			creativeId = creativeId.trim();
		}
		this.adId = req.getValue(JozAdRequest.KEY_EXTERNAL_AD_ID);
		if (adId != null) {
			adId = adId.trim();
		}
		this.pageId = req.getValue(JozAdRequest.KEY_EXTERNAL_PAGE_ID);
		if (pageId != null) {
			pageId = pageId.trim();
		}
		this.userAgent = req.getValue(JozAdRequest.KEY_USER_AGENT);
		if (userAgent != null) {
			userAgent = userAgent.trim();
		}
        try {
            this.expId = Integer.parseInt(req.getValue(JozAdRequest.KEY_EXPERIENCE_ID));
        } catch (Exception e) {
            this.expId = null;
        }

        try {
            this.variationId = Integer.parseInt(req.getValue(JozAdRequest.KEY_VARIATION_ID));
        } catch (Exception e) {
            this.variationId = null;
        }

        this.age = req.getValue(JozAdRequest.KEY_AGE);
        if (age != null) {
            age = age.trim();
        }
        this.gender = req.getValue(JozAdRequest.KEY_GENDER);
        if (gender != null) {
            gender = gender.trim();
        }
        this.hhi = req.getValue(JozAdRequest.KEY_HOUSEHOLD_INCOME);
        if (hhi != null) {
            hhi = hhi.trim();
        }
        this.ms = req.getValue(JozAdRequest.KEY_MARITAL_STATUS);
        if (ms != null) {
            ms = ms.trim();
        }
        this.bt = req.getValue(JozAdRequest.KEY_BT);
        if (bt != null) {
            bt = bt.trim();
        }
        this.ut1 = req.getValue(JozAdRequest.KEY_RETARGETING_UT1);
        if (ut1 != null) {
            ut1 = ut1.trim();
        }
        this.ut2 = req.getValue(JozAdRequest.KEY_RETARGETING_UT2);
        if (ut2 != null) {
            ut2 = ut2.trim();
        }
        this.ut3 = req.getValue(JozAdRequest.KEY_RETARGETING_UT3);
        if (ut3 != null) {
            ut3 = ut3.trim();
        }
        this.ut4 = req.getValue(JozAdRequest.KEY_RETARGETING_UT4);
        if (ut4 != null) {
            ut4 = ut4.trim();
        }
        this.ut5 = req.getValue(JozAdRequest.KEY_RETARGETING_UT5);
        if (ut5 != null) {
            ut5 = ut5.trim();
        }
        this.useTopK = req.getValue(JozAdRequest.KEY_TOPK);
        if (useTopK != null) {
            useTopK = useTopK.trim();
        }


	}

	public enum AdOfferType {
		PRODUCT_ONLY, LEADGEN_ONLY, PRODUCT_LEADGEN
	}

	public enum OutputFormat {
		NORMAL, JS_FRIENDLY
	}

    	public String getTargetedRealm() {
		return targetedRealm;
	}

	public void setTargetedRealm(String targetedRealm) {
		this.targetedRealm = targetedRealm;
	}

	public String get_url() {
		return _url;
	}

	public String get_theme() {
		return _theme;
	}

	public String get_store_id() {
		return _store_id;
	}

	public String get_category() {
		return _category;
	}

	public String get_t_spec() {
		return _t_spec;
	}

	// null represents "t" or "all products"
	public Integer get_num_products() {
		return _num_products;
	}

	// null represents "nil" or "unspecified"

	public Integer get_row_size() {
		return _row_size;
	}

	// null represents "nil" or "unspecified"

	public Integer get_which_row() {
		return _which_row;
	}

	public Boolean get_revert_to_default_realm() {
		return _revert_to_default_realm;
	}

	public String get_keywords() {
		return _keywords;
	}

	public String get_script_keywords() {
		return _script_keywords;
	}

	public Boolean get_include_cat_counts() {
		return _include_cat_counts;
	}

	public Integer get_ad_width() {
		return _ad_width;
	}

	public Integer get_ad_height() {
		return _ad_height;
	}

	public AdOfferType get_ad_offer_type() {
		return _ad_offer_type;
	}

	public Integer get_max_prod_desc_len() {
		return _max_prod_desc_len;
	}

	public String getCountry() {
		return _country;
	}

	public String getRegion() {
		return _region;
	}

	public String getCity() {
		return _city;
	}

	public String getDmacode() {
		return _dmacode;
	}

	public String getAreacode() {
		return _areacode;
	}

	public String get_zip_code() {
		return _zip_code;
	}

	public String getLatitude() {
		return _latitude;
	}

	public String getLongitude() {
		return _longitude;
	}

	public String getExternalFilterField1() {
		return externalFilterField1;
	}

	public String getExternalFilterField2() {
		return externalFilterField2;
	}

	public String getExternalFilterField3() {
		return externalFilterField3;
	}

	public String getExternalFilterField4() {
		return externalFilterField4;
	}

	public String getExternalFilterField5() {
		return externalFilterField5;
	}

	public String getExternalTargetField1() {
		return externalTargetField1;
	}

	public String getExternalTargetField2() {
		return externalTargetField2;
	}

	public String getExternalTargetField3() {
		return externalTargetField3;
	}

	public String getExternalTargetField4() {
		return externalTargetField4;
	}

	public String getExternalTargetField5() {
		return externalTargetField5;
	}

	public String getUserBucket() {
		return ub;
	}

	public String getBuyId() {
		return buyId;
	}

	public String getSiteId() {
		return siteId;
	}

	public String getPageId() {
		return pageId;
	}

	public String getAdId() {
		return adId;
	}

	public String getCreativeId() {
		return creativeId;
	}

	public String getUserAgent() {
		return userAgent;
	}

    public HashMap<String, String> getExtTargetFields() {
		HashMap<String, String> extVarsMap = new HashMap<String, String>();
		String extField1 = getExternalTargetField1();
		if (extField1 == null)
			extField1 = "";
		extVarsMap.put("x2_t1", extField1);

		String extField2 = getExternalTargetField2();
		if (extField2 == null)
			extField2 = "";
		extVarsMap.put("x2_t2", extField2);

		String extField3 = getExternalTargetField3();
		if (extField3 == null)
			extField3 = "";
		extVarsMap.put("x2_t3", extField3);

		String extField4 = getExternalTargetField4();
		if (extField4 == null)
			extField4 = "";
		extVarsMap.put("x2_t4", extField4);

		String extField5 = getExternalTargetField5();
		if (extField5 == null)
			extField5 = "";
		extVarsMap.put("x2_t5", extField5);

		String ut1 = getUt1();
		if (ut1 == null)
			ut1 = "";
		extVarsMap.put("ut1", ut1);

		String ut2 = getUt2();
		if (ut2 == null)
			ut2 = "";
		extVarsMap.put("ut2", ut2);

		String ut3 = getUt3();
		if (ut3 == null)
			ut3 = "";
		extVarsMap.put("ut3", ut3);

		String ut4 = getUt1();
		if (ut4 == null)
			ut4 = "";
		extVarsMap.put("ut4", ut4);

		String ut5 = getUt5();
		if (ut5 == null)
			ut5 = "";
		extVarsMap.put("ut5", ut5);

		return extVarsMap;
	}

	public String getAdType() {
		return adType;
	}

	public Long getRecipeId() {
		if (recipeId != null) {
			return recipeId;
		} else {
			return -1L;
		}
	}

    public String getUt5() {
        return ut5;
    }

    public String getUt4() {
        return ut4;
    }

    public String getUt3() {
        return ut3;
    }

    public String getUt2() {
        return ut2;
    }

    public String getUt1() {
        return ut1;
    }

    public String getBt() {
        return bt;
    }

    public String getMs() {
        return ms;
    }

    public String getHhi() {
        return hhi;
    }

    public String getGender() {
        return gender;
    }

    public String getAge() {
        return age;
    }

    public Integer getExpId() {
        if (expId != null) {
			return expId;
		} else {
			return -1;
		}
    }

    public Integer getVariationId() {
        if (variationId != null) {
			return variationId;
		} else {
			return -1;
		}
    }

    public String getUseTopK() {
        return useTopK;
    }

    public void setUseTopK(String useTopK) {
        this.useTopK = useTopK;
    }

    public String toString() {
		StringBuilder b = new StringBuilder();

		b.append("(get-ad-data");

			if (_url != null)
				b.append(" :url ").append(_url);
			if (_theme != null)
				b.append(" :theme ").append(_theme);
			if (_store_id != null)
				b.append(" :store-id ").append(_store_id);
			if (_category != null)
				b.append(" :category ").append(_category);
			if (_t_spec != null)
				b.append(" :t-spec ").append(_t_spec);
			if (_referrer != null)
				b.append(" :referrer ").append(_referrer);
			if (_zip_code != null)
				b.append(" :zip-code ").append(_zip_code);
			if (_num_products != null)
				b.append(" :num-products ").append(_num_products);
			if (_row_size != null)
				b.append(" :row-size ").append(_row_size);
			if (_which_row != null)
				b.append(" :which-row ").append(_which_row);
			if (_revert_to_default_realm != null)
				b.append(" :revert-to-default-realm ").append(
						_revert_to_default_realm);
			if (_keywords != null)
				b.append(" :keywords ").append(_keywords);
			if (_script_keywords != null)
				b.append(" :script-keywords ").append(_script_keywords);
			if (_include_cat_counts != null)
				b.append(" :include-cat-counts ").append(_include_cat_counts);
			if (_ad_width != null)
				b.append(" :ad-width ").append(_ad_width);
			if (_ad_height != null)
				b.append(" :ad-height ").append(_ad_height);
			if (_ad_offer_type != null)
				b.append(" :ad-offer-type ").append(_ad_offer_type);
			if (adType != null)
				b.append(" :ad-type ").append(adType);
			if (recipeId != null)
				b.append(" :recipe-id ").append(recipeId);


		b.append(")");

		return b.toString();
	}



}
