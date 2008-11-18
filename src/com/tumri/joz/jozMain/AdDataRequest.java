// Container class for a get-ad-data request.

/* The get-ad-data command has the following format:
 See https://secure.tumri.com/twiki/bin/view/Engineering/JozPublicAPI.

 get-ad-data  :url "..."
 :theme "..."
 :store-ID 'store-ID-symbol
 :category 'category-symbol
 :t-spec 't-spec-symbol ;; if non-nil, ignore :url and :theme
 :strategy 't-spec-symbol ;; deprecated, synonym for :t-spec
 :referrer "...."
 :zip-code "..."
 :num-products [t|<integer>] ;; t means return all products
 ;; if row-size and which-row are non-nil and integers then deterministically return a row/page of results:
 :row-size [nil|<integer>] 
 :which-row [nil|<integer>]
 :revert-to-default-realm [nil|t] ;; if t and don't get enough products then retry on default realm, defaults to t
 :keywords [nil|<string>] ;; white-space-separated keywords from search box in widget
 :script-keywords [nil|<string>] ;; white-space-separated keywords from publisher's script
 :include-cat-counts [nil|t] ;; add category-counts to output?
 :seed [nil|<integer>] ;; plug seed in here to deterministically replay a previous call
 ;; product selection parameters:
 :psychographics-p [t|nil|:maybe] ;; use psychographic curves when building roulette wheel
 :mine-ref-URL-p [t|nil|:maybe] ;; not currently used
 :mine-pub-URL-p [t|nil|:maybe] ;; pull keywords out of publisher URL and get products based on them
 :irrelevad-p [t|nil|:maybe] ;; not currently used
 :allow-too-few-products [t|nil|:maybe] ;; if t then we don't care about too few products
 :ad-width [nil|<number>] ;; for leadgens, only return offers of these dimensions
 :ad-height [nil|<number>]
 ;; if ad-offer-type is :product-only or :leadgen-only then appropriately filter the offers returned:
 :ad-offer-type [:product-only|:leadgen-only:product-leadgen]  ;; defaults to :product-only
 :min-num-leadgens [nil|<integer>] ;; if non-nil, *try* to put this many leadgens in the result
 :output-format [:normal|:js-friendly]  ;; defaults to :normal
 :output-order [:uniform-random|:deterministic-best-first|:perturbed-best-first] ;; defaults to :uniform-random, see note below
 :output-order-noise-stddev <number> ;; defaults to 0.1
 :max-prod-desc-len <integer> ;; FIXME: ???
 */

/*
 NOTE: There are no setter methods on purpose.

 NOTE: a null value means the parameter is unspecified
 */

package com.tumri.joz.jozMain;

// import java.io.PrintWriter;
import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.utils.sexp.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;

public class AdDataRequest {
    
    public AdDataRequest(Sexp expr) throws BadCommandException {
        parse_request(expr);
    }

    /**
     * Constructor that will get the values from the request
     * @param req
     */
    public AdDataRequest(JozAdRequest req) {
        this._url = req.getValue(JozAdRequest.KEY_URL);
        this._theme = req.getValue(JozAdRequest.KEY_THEME);
        this._store_id = req.getValue(JozAdRequest.KEY_LOCATION_ID);
        this._category = req.getValue(JozAdRequest.KEY_CATEGORY);
        this._t_spec = req.getValue(JozAdRequest.KEY_T_SPEC);
//        this._referrer = req.getValue(JozAdRequest.KEY_REFERRER);
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

        this._revert_to_default_realm = "true".equalsIgnoreCase(req.getValue(JozAdRequest.KEY_REVERT_TO_DEFAULT_REALM))?true:false;
        this._keywords =req.getValue(JozAdRequest.KEY_KEYWORDS);
        this._script_keywords =req.getValue(JozAdRequest.KEY_SCRIPT_KEYWORDS);
        //this._include_cat_counts = "true".equalsIgnoreCase(req.getValue(JozAdRequest.KEY_INCLUDE_CAT_COUNTS))?true:false;
//        try {
//            this._seed = Integer.parseInt(req.getValue(JozAdRequest.KEY_SEED));
//        } catch (Exception e) {
//            this._seed = null;
//        }

        //this._psychographics_p = req.getValue(JozAdRequest.KEY_URL);
        //this._mine_pub_url_p = req.getValue(JozAdRequest.KEY_URL);
        //this._allow_too_few_products = req.getValue(JozAdRequest.KEY_URL);

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

        this._ad_offer_type = "LEADGEN".equals(req.getValue(JozAdRequest.KEY_AD_OFFER_TYPE))?AdDataRequest.AdOfferType.LEADGEN_ONLY:AdDataRequest.AdOfferType.PRODUCT_LEADGEN;
        try {
            this._min_num_leadgens = Integer.parseInt(req.getValue(JozAdRequest.KEY_MIN_NUM_LEADGENS));
        } catch (Exception e) {
            this._min_num_leadgens = null;
        }

        try {
            this._max_prod_desc_len = Integer.parseInt(req.getValue(JozAdRequest.KEY_MAX_PROD_DESC_LEN));
        } catch (Exception e) {
            this._max_prod_desc_len = null;
        }

        this._country = req.getValue(JozAdRequest.KEY_COUNTRY);
        this._region = req.getValue(JozAdRequest.KEY_REGION);
        this._city = req.getValue(JozAdRequest.KEY_CITY);
        this._dmacode = req.getValue(JozAdRequest.KEY_DMACODE);
        this._areacode = req.getValue(JozAdRequest.KEY_AREACODE);
        this._zip_code = req.getValue(JozAdRequest.KEY_ZIP_CODE);
        this._latitude = req.getValue(JozAdRequest.KEY_LATITUDE);
        this._longitude = req.getValue(JozAdRequest.KEY_LONGITUDE);
        this.adType = req.getValue(JozAdRequest.KEY_AD_TYPE);
        try{
        	this.recipeId=Integer.parseInt(req.getValue(JozAdRequest.KEY_RECIPE_ID));
        }catch (Exception e) {
            this.recipeId = null;
        }
	    this.multiValueField1 = req.getValue(JozAdRequest.KEY_MULTI_VALUE_FIELD1);
	    this.multiValueField2 = req.getValue(JozAdRequest.KEY_MULTI_VALUE_FIELD2);
	    this.multiValueField3 = req.getValue(JozAdRequest.KEY_MULTI_VALUE_FIELD3);
	    this.multiValueField4 = req.getValue(JozAdRequest.KEY_MULTI_VALUE_FIELD4);
	    this.multiValueField5 = req.getValue(JozAdRequest.KEY_MULTI_VALUE_FIELD5);
    }
    
    public enum AdOfferType {
        PRODUCT_ONLY, LEADGEN_ONLY, PRODUCT_LEADGEN
    }
    
    public enum OutputFormat {
        NORMAL, JS_FRIENDLY
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
    
    public String get_referrer() {
        return _referrer;
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
    
    // null represents "nil" or "unspecified"
    public Integer get_seed() {
        return _seed;
    }
    
    public SexpUtils.MaybeBoolean get_psychographics_p() {
        return _psychographics_p;
    }
    
    public SexpUtils.MaybeBoolean get_mine_pub_url_p() {
        return _mine_pub_url_p;
    }
    
    public SexpUtils.MaybeBoolean get_allow_too_few_products() {
        return _allow_too_few_products;
    }
    
    // null represents "nil" or "unspecified"
    public Integer get_ad_width() {
        return _ad_width;
    }
    
    // null represents "nil" or "unspecified"
    public Integer get_ad_height() {
        return _ad_height;
    }
    
    public AdOfferType get_ad_offer_type() {
        return _ad_offer_type;
    }
    
    // null represents "nil" or "unspecified"
    public Integer get_min_num_leadgens() {
        return _min_num_leadgens;
    }
    
    public OutputFormat get_output_format() {
        return _output_format;
    }
    
    public String get_output_order() {
        return _output_order;
    }
    
    public Double get_output_order_noise_stddev() {
        return _output_order_noise_stddev;
    }
    
    // null represents "nil" or "unspecified"
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

    public String getMultiValueField1() {
        return multiValueField1;
    }

    public String getMultiValueField2() {
        return multiValueField2;
    }

    public String getMultiValueField3() {
        return multiValueField3;
    }

    public String getMultiValueField4() {
        return multiValueField4;
    }

    public String getMultiValueField5() {
        return multiValueField5;
    }

    public String getTargetedRealm() {
        return targetedRealm;
    }

    public void setTargetedRealm(String targetedRealm) {
        this.targetedRealm = targetedRealm;
    }

    public String getAdType(){
        return adType;
    }

    public Integer getRecipeId() {
        if (recipeId != null) {
            return recipeId;
        } else {
            return -1;
        }
    }

    public String toString() {
        return toString(false);
    }
    
    public String toString(boolean include_defaults) {
        StringBuilder b = new StringBuilder();
        
        b.append("(get-ad-data");
        
        if (include_defaults) {
            b.append(" :url ").append(_url);
            b.append(" :theme ").append(_theme);
            b.append(" :store-id ").append(_store_id);
            b.append(" :category ").append(_category);
            b.append(" :t-spec ").append(_t_spec);
            b.append(" :referrer ").append(_referrer);
            b.append(" :zip-code ").append(_zip_code);
            b.append(" :num-products ").append(
                    _num_products != null ? _num_products : "null");
            b.append(" :row-size ").append(
                    _row_size != null ? _row_size : "null");
            b.append(" :which-row ").append(
                    _which_row != null ? _which_row : "null");
            b.append(" :revert-to-default-realm ").append(
                    _revert_to_default_realm);
            b.append(" :keywords ").append(_keywords);
            b.append(" :script-keywords ").append(_script_keywords);
            b.append(" :include-cat-counts ").append(_include_cat_counts);
            b.append(" :seed ").append(_seed != null ? _seed : "null");
            b.append(" :psychographics-p ").append(_psychographics_p);
            b.append(" :mine-pub-url-p ").append(_mine_pub_url_p);
            b.append(" :allow-too-few-products ").append(
                    _allow_too_few_products);
            b.append(" :ad-width ").append(
                    _ad_width != null ? _ad_width : "null");
            b.append(" :ad-height ").append(
                    _ad_height != null ? _ad_height : "null");
            b.append(" :ad-offer-type ").append(_ad_offer_type);
            b.append(" :min-num-leadgens ").append(
                    _min_num_leadgens != null ? _min_num_leadgens : "null");
            b.append(" :output-format ").append(_output_format);
            b.append(" :output-order ").append(_output_order);
            b.append(" :output-order-noise-stddev ").append(
                    _output_order_noise_stddev);
            b.append(" :max-prod-desc-len ").append(
                    _max_prod_desc_len != null ? _max_prod_desc_len : "null");
            b.append(" :country-name ").append(_country);
            b.append(" :region ").append(_region);
            b.append(" :city ").append(_city);
            b.append(" :dma ").append(_dmacode);
            b.append(" :area-code ").append(_areacode);

            
            b.append(" :latitude ").append(_latitude);
            b.append(" :longitude ").append(_longitude);
            b.append(" :multivaluefield1 ").append(multiValueField1);
            b.append(" :multivaluefield2 ").append(multiValueField2);
            b.append(" :multivaluefield3 ").append(multiValueField3);
            b.append(" :multivaluefield3 ").append(multiValueField4);
            b.append(" :multivaluefield5 ").append(multiValueField5);

            
            b.append(" :ad-type ").append(
                    adType != null ? adType : "null");
            b.append(" :recipe-id ").append(
                    recipeId != null ? recipeId : "null");
        } else {
            if (_url != DEFAULT_URL)
                b.append(" :url ").append(_url);
            if (_theme != DEFAULT_THEME)
                b.append(" :theme ").append(_theme);
            if (_store_id != DEFAULT_STORE_ID)
                b.append(" :store-id ").append(_store_id);
            if (_category != DEFAULT_CATEGORY)
                b.append(" :category ").append(_category);
            if (_t_spec != DEFAULT_T_SPEC)
                b.append(" :t-spec ").append(_t_spec);
            if (_referrer != DEFAULT_REFERRER)
                b.append(" :referrer ").append(_referrer);
            if (_zip_code != DEFAULT_ZIP_CODE)
                b.append(" :zip-code ").append(_zip_code);
            if (_num_products != DEFAULT_NUM_PRODUCTS)
                b.append(" :num-products ").append(_num_products);
            if (_row_size != DEFAULT_ROW_SIZE)
                b.append(" :row-size ").append(_row_size);
            if (_which_row != DEFAULT_WHICH_ROW)
                b.append(" :which-row ").append(_which_row);
            if (_revert_to_default_realm != DEFAULT_REVERT_TO_DEFAULT_REALM)
                b.append(" :revert-to-default-realm ").append(
                        _revert_to_default_realm);
            if (_keywords != DEFAULT_KEYWORDS)
                b.append(" :keywords ").append(_keywords);
            if (_script_keywords != DEFAULT_SCRIPT_KEYWORDS)
                b.append(" :script-keywords ").append(_script_keywords);
            if (_include_cat_counts != DEFAULT_INCLUDE_CAT_COUNTS)
                b.append(" :include-cat-counts ").append(_include_cat_counts);
            if (_seed != DEFAULT_SEED)
                b.append(" :seed ").append(_seed);
            if (_psychographics_p != DEFAULT_PSYCHOGRAPHICS_P)
                b.append(" :psychographics-p ").append(_psychographics_p);
            if (_mine_pub_url_p != DEFAULT_MINE_PUB_URL_P)
                b.append(" :mine-pub-url-p ").append(_mine_pub_url_p);
            if (_allow_too_few_products != DEFAULT_ALLOW_TOO_FEW_PRODUCTS)
                b.append(" :allow-too-few-products ").append(
                        _allow_too_few_products);
            if (_ad_width != DEFAULT_AD_WIDTH)
                b.append(" :ad-width ").append(_ad_width);
            if (_ad_height != DEFAULT_AD_HEIGHT)
                b.append(" :ad-height ").append(_ad_height);
            if (_ad_offer_type != DEFAULT_AD_OFFER_TYPE)
                b.append(" :ad-offer-type ").append(_ad_offer_type);
            if (_min_num_leadgens != DEFAULT_MIN_NUM_LEADGENS)
                b.append(" :min-num-leadgens ").append(_min_num_leadgens);
            if (_output_format != DEFAULT_OUTPUT_FORMAT)
                b.append(" :output-format ").append(_output_format);
            if (_output_order != DEFAULT_OUTPUT_ORDER)
                b.append(" :output-order ").append(_output_order);
            if (_output_order_noise_stddev != DEFAULT_OUTPUT_ORDER_NOISE_STDDEV)
                b.append(" :output-order-noise-stddev ").append(
                        _output_order_noise_stddev);
            if (adType != DEFAULT_AD_TYPE)
                b.append(" :ad-type ").append(adType);
            if (recipeId != DEFAULT_RECIPE_ID)
                b.append(" :recipe-id ").append(recipeId);
            
        }
        
        b.append(")");
        
        return b.toString();
    }
    
    // implementation details -------------------------------------------------
    
    private static Logger log = Logger.getLogger(AdDataRequest.class);
    
    private enum RqstParam {
        URL, THEME, STORE_ID, CATEGORY, T_SPEC, STRATEGY, REFERRER, ZIP_CODE, NUM_PRODUCTS, ROW_SIZE, WHICH_ROW, REVERT_TO_DEFAULT_REALM, KEYWORDS, 
        SCRIPT_KEYWORDS, INCLUDE_CAT_COUNTS, SEED, PSYCHOGRAPHICS_P, MINE_PUB_URL_P, ALLOW_TOO_FEW_PRODUCTS, AD_WIDTH, AD_HEIGHT, AD_OFFER_TYPE, 
        MULTIVALUE_FIELD1, MULTIVALUE_FIELD2, MULTIVALUE_FIELD3, MULTIVALUE_FIELD4, MULTIVALUE_FIELD5,
        MIN_NUM_LEADGENS, OUTPUT_FORMAT, OUTPUT_ORDER, OUTPUT_ORDER_NOISE_STDDEV, MAX_PROD_DESC_LEN, COUNTRY, REGION, CITY, DMACODE, AREACODE, LATITUDE, ADTYPE,RECIPE_ID,LONGITUDE,
    }
    
    private static HashMap<String, RqstParam> rqst_params = new HashMap<String, RqstParam>();
    
    static {
        // FIXME: collisions?
        rqst_params.put(":url", RqstParam.URL);
        rqst_params.put(":theme", RqstParam.THEME);
        rqst_params.put(":store-ID", RqstParam.STORE_ID);
        rqst_params.put(":category", RqstParam.CATEGORY);
        rqst_params.put(":t-spec", RqstParam.T_SPEC);
        rqst_params.put(":strategy", RqstParam.STRATEGY); 
        rqst_params.put(":referrer", RqstParam.REFERRER);
        rqst_params.put(":num-products", RqstParam.NUM_PRODUCTS);
        rqst_params.put(":row-size", RqstParam.ROW_SIZE);
        rqst_params.put(":which-row", RqstParam.WHICH_ROW);
        rqst_params.put(":revert-to-default-realm",
                RqstParam.REVERT_TO_DEFAULT_REALM);
        rqst_params.put(":keywords", RqstParam.KEYWORDS);
        rqst_params.put(":script-keywords", RqstParam.SCRIPT_KEYWORDS);
        rqst_params.put(":include-cat-counts", RqstParam.INCLUDE_CAT_COUNTS);
        rqst_params.put(":seed", RqstParam.SEED);
        rqst_params.put(":psychographics-p", RqstParam.PSYCHOGRAPHICS_P);
        rqst_params.put(":min-pub-URL-p", RqstParam.MINE_PUB_URL_P);
        rqst_params.put(":allow-too-few-products",
                RqstParam.ALLOW_TOO_FEW_PRODUCTS);
        rqst_params.put(":ad-width", RqstParam.AD_WIDTH);
        rqst_params.put(":ad-height", RqstParam.AD_HEIGHT);
        rqst_params.put(":ad-offer-type", RqstParam.AD_OFFER_TYPE);
        rqst_params.put(":min-num-leadgens", RqstParam.MIN_NUM_LEADGENS);
        rqst_params.put(":output-format", RqstParam.OUTPUT_FORMAT);
        rqst_params.put(":output-order", RqstParam.OUTPUT_ORDER);
        rqst_params.put(":output-order-noise-stddev",
                RqstParam.OUTPUT_ORDER_NOISE_STDDEV);
        rqst_params.put(":max-prod-desc-len", RqstParam.MAX_PROD_DESC_LEN);
        rqst_params.put(":country-name", RqstParam.COUNTRY);
        rqst_params.put(":region", RqstParam.REGION);
        rqst_params.put(":city", RqstParam.CITY);
        rqst_params.put(":dma", RqstParam.DMACODE);
        rqst_params.put(":area-code", RqstParam.AREACODE);
        rqst_params.put(":zip-code", RqstParam.ZIP_CODE);
        rqst_params.put(":latitude", RqstParam.LATITUDE);
        rqst_params.put(":longitude", RqstParam.LONGITUDE);
        rqst_params.put(":multivaluefield1", RqstParam.MULTIVALUE_FIELD1);
        rqst_params.put(":multivaluefield2", RqstParam.MULTIVALUE_FIELD2);
        rqst_params.put(":multivaluefield3", RqstParam.MULTIVALUE_FIELD3);
        rqst_params.put(":multivaluefield4", RqstParam.MULTIVALUE_FIELD4);
        rqst_params.put(":multivaluefield5", RqstParam.MULTIVALUE_FIELD5);
        rqst_params.put(":ad-type", RqstParam.ADTYPE);
        rqst_params.put(":recipe-id", RqstParam.RECIPE_ID);
    }
    
    // WARNING: If the default value is not null, you're probably doing
    // something wrong. "null" here means "unspecified", and that is generally
    // the connotation you want to have for default values here. If you do
    // specify a non-null default, a comment describing WHY it is non-null is
    // REQUIRED.
    
    private static final String DEFAULT_URL = null;
    
    private static final String DEFAULT_THEME = null;
    
    private static final String DEFAULT_STORE_ID = null;
    
    private static final String DEFAULT_CATEGORY = null;
    
    private static final String DEFAULT_T_SPEC = null;
    
    private static final String DEFAULT_REFERRER = null;
    
    private static final String DEFAULT_ZIP_CODE = null;
    
    private static final String DEFAULT_LATITUDE = null;
    
    private static final String DEFAULT_LONGITUDE = null;
    
    private static final Integer DEFAULT_NUM_PRODUCTS = null;
    
    private static final Integer DEFAULT_ROW_SIZE = null;
    
    private static final Integer DEFAULT_WHICH_ROW = null;
    
    private static final Boolean DEFAULT_REVERT_TO_DEFAULT_REALM = new Boolean(
            true);
    
    private static final String DEFAULT_KEYWORDS = null;
    
    private static final String DEFAULT_SCRIPT_KEYWORDS = null;
    
    private static final String DEFAULT_AD_TYPE = null;
    
    private static final Integer DEFAULT_RECIPE_ID = null;
    
    // FIXME: should be null, fix.
    private static final Boolean DEFAULT_INCLUDE_CAT_COUNTS = new Boolean(false); // FIXME:
    
    // check
    // default
    // value
    
    private static final Integer DEFAULT_SEED = null;
    
    private static final SexpUtils.MaybeBoolean DEFAULT_PSYCHOGRAPHICS_P = null;
    
    private static final SexpUtils.MaybeBoolean DEFAULT_MINE_PUB_URL_P = null;
    
    private static final SexpUtils.MaybeBoolean DEFAULT_ALLOW_TOO_FEW_PRODUCTS = null;
    
    private static final Integer DEFAULT_AD_WIDTH = null;
    
    private static final Integer DEFAULT_AD_HEIGHT = null;
    
    private static final AdOfferType DEFAULT_AD_OFFER_TYPE = AdOfferType.PRODUCT_ONLY;
    
    private static final Integer DEFAULT_MIN_NUM_LEADGENS = null;
    
    private static final OutputFormat DEFAULT_OUTPUT_FORMAT = OutputFormat.NORMAL;
    
    private static final String DEFAULT_OUTPUT_ORDER = null;
    
    // FIXME: should be null, fix.
    private static final Double DEFAULT_OUTPUT_ORDER_NOISE_STDDEV = new Double(
            0.1);
    
    private static final Integer DEFAULT_MAX_PROD_DESC_LEN = null;
    
    String _url = DEFAULT_URL;
    
    String _theme = DEFAULT_THEME;
    
    String _store_id = DEFAULT_STORE_ID;
    
    String _category = DEFAULT_CATEGORY;
    
    // if non-null, ignore :url, :theme, and :store-id
    String _t_spec = DEFAULT_T_SPEC;
    
    String _referrer = DEFAULT_REFERRER;
    
    String _zip_code = DEFAULT_ZIP_CODE;
    
    String _latitude = DEFAULT_LATITUDE;
    
    String _longitude = DEFAULT_LONGITUDE;
    
    // "all" means return all products
    Integer _num_products = DEFAULT_NUM_PRODUCTS;
    
    // If row-size and which-row are non-null and integers then
    // deterministically return a row/page of results.
    Integer _row_size = DEFAULT_ROW_SIZE;
    
    Integer _which_row = DEFAULT_WHICH_ROW;
    
    Boolean _revert_to_default_realm = DEFAULT_REVERT_TO_DEFAULT_REALM;
    
    // white-space-separated keywords from search box in widget
    String _keywords = DEFAULT_KEYWORDS;
    
    // white-space-separated keywords from publisher's script
    String _script_keywords = DEFAULT_SCRIPT_KEYWORDS;
    
    // add category-counts to output?
    Boolean _include_cat_counts = DEFAULT_INCLUDE_CAT_COUNTS;
    
    // plug seed in here to deterministically replay a previous call
    Integer _seed = DEFAULT_SEED;
    
    // use psychographic curves when building roulette wheel
    SexpUtils.MaybeBoolean _psychographics_p = DEFAULT_PSYCHOGRAPHICS_P;
    
    // pull keywords out of publisher URL and get products based on them
    SexpUtils.MaybeBoolean _mine_pub_url_p = DEFAULT_MINE_PUB_URL_P;
    
    // if t then we don't care about too few products
    SexpUtils.MaybeBoolean _allow_too_few_products = DEFAULT_ALLOW_TOO_FEW_PRODUCTS;
    
    // for leadgens, only return offers of these dimensions
    Integer _ad_width = DEFAULT_AD_WIDTH;
    
    Integer _ad_height = DEFAULT_AD_HEIGHT;
    
    // one of :product-only, :leadgen-only, or product-leadgen
    AdOfferType _ad_offer_type = DEFAULT_AD_OFFER_TYPE;
    
    // if non-null, *try* to put this many leadgens in the result
    Integer _min_num_leadgens = DEFAULT_MIN_NUM_LEADGENS;
    
    // one of :normal, :js-friendly, defaults to :normal
    OutputFormat _output_format = DEFAULT_OUTPUT_FORMAT;
    
    // one of :uniform-random|:deterministic-best-first|:perturbed-best-first
    // default is :uniform-random
    String _output_order = DEFAULT_OUTPUT_ORDER;
    
    // default is 0.1
    Double _output_order_noise_stddev = DEFAULT_OUTPUT_ORDER_NOISE_STDDEV;
    
    Integer _max_prod_desc_len = DEFAULT_MAX_PROD_DESC_LEN;
    
    String _country;
    
    String _region;
    
    String _city;
    
    String _zipcode;
    
    String _dmacode;
    
    String _areacode;

    String targetedRealm = "";

    String multiValueField1 = null;
    String multiValueField2 = null;
    String multiValueField3 = null;
    String multiValueField4 = null;
    String multiValueField5 = null;
    String adType = "";

    
    Integer recipeId = null;
    
    private void parse_request(Sexp expr) throws BadCommandException {
        if (!expr.isSexpList())
            throw new BadCommandException("get-ad-data request not a list");
        SexpList l = expr.toSexpList();
        int n = l.size();
        
        Iterator<Sexp> iter = l.iterator();
        
        Sexp get_ad_data = iter.next(); // get-ad-data symbol
        
        while (iter.hasNext()) {
            Sexp elm = iter.next();
            
            if (!elm.isSexpKeyword()) {
                log.error("Expected keyword, got: " + elm);
                continue;
            }
            
            SexpKeyword k = elm.toSexpKeyword();
            String name = k.toStringValue();
            
            RqstParam p = rqst_params.get(name);
            if (p == null) {
                log.error("Unknown parameter: " + name);
                // swallow the value
                iter.next();
                continue;
            }
            
            try {
                switch (p) {
                    case URL:
                        this._url = SexpUtils.get_next_string(name, iter);
                        break;
                    
                    case THEME:
                        this._theme = SexpUtils.get_next_string(name, iter);
                        break;
                    
                    case STORE_ID:
                    	this._store_id = SexpUtils.get_next_string_or_symbol(name, iter);
                        break;
                    
                    case CATEGORY:
                        this._category = SexpUtils.get_next_string_or_symbol(name, iter);
                        break;
                    
                    case T_SPEC:
                        this._t_spec = SexpUtils.get_next_symbol(name, iter);
                        break;
                        
                    case STRATEGY:
                        this._t_spec = SexpUtils.get_next_string_or_symbol(name, iter);
                        break;
                        
                    case REFERRER:
                        this._referrer = SexpUtils.get_next_string(name, iter);
                        break;
                    
                    case NUM_PRODUCTS:
                        this._num_products = get_next_integer_or_t_null(name,
                                iter);
                        break;
                    
                    case ROW_SIZE:
                        this._row_size = get_next_integer_or_nil_null(name,
                                iter);
                        break;
                    
                    case WHICH_ROW:
                        this._which_row = get_next_integer_or_nil_null(name,
                                iter);
                        break;
                    
                    case REVERT_TO_DEFAULT_REALM:
                        this._revert_to_default_realm = SexpUtils
                                .get_next_boolean(name, iter);
                        break;
                    
                    case KEYWORDS:
                        this._keywords = SexpUtils.get_next_string(name, iter);
                        break;
                    
                    case SCRIPT_KEYWORDS:
                        this._script_keywords = SexpUtils.get_next_string(name,
                                iter);
                        break;
                    
                    case INCLUDE_CAT_COUNTS:
                        this._include_cat_counts = SexpUtils.get_next_boolean(
                                name, iter);
                        break;
                    
                    case SEED:
                        this._seed = get_next_integer_or_nil_null(name, iter);
                        break;
                    
                    case PSYCHOGRAPHICS_P:
                        this._psychographics_p = SexpUtils
                                .get_next_maybe_boolean(name, iter);
                        break;
                    
                    case MINE_PUB_URL_P:
                        this._mine_pub_url_p = SexpUtils
                                .get_next_maybe_boolean(name, iter);
                        break;
                    
                    case ALLOW_TOO_FEW_PRODUCTS:
                        this._allow_too_few_products = SexpUtils
                                .get_next_maybe_boolean(name, iter);
                        break;
                    
                    case AD_WIDTH:
                        this._ad_width = get_next_integer_or_nil_null(name,
                                iter);
                        break;
                    
                    case AD_HEIGHT:
                        this._ad_height = get_next_integer_or_nil_null(name,
                                iter);
                        break;
                    
                    case AD_OFFER_TYPE:
                        this._ad_offer_type = get_next_ad_offer_type(name, iter);
                        break;
                    
                    case MIN_NUM_LEADGENS:
                        this._min_num_leadgens = get_next_integer_or_nil_null(
                                name, iter);
                        break;
                    
                    case OUTPUT_FORMAT:
                        this._output_format = get_next_output_format(name, iter);
                        break;
                    
                    case OUTPUT_ORDER:
                        this._output_order = SexpUtils.get_next_string(name,
                                iter);
                        break;
                    
                    case OUTPUT_ORDER_NOISE_STDDEV:
                        this._output_order_noise_stddev = SexpUtils
                                .get_next_double(name, iter);
                        break;
                    
                    case MAX_PROD_DESC_LEN:
                        this._max_prod_desc_len = get_next_integer_or_nil_null(
                                name, iter);
                        break;
                    
                    case COUNTRY:
                        this._country = SexpUtils.get_next_string(name, iter);
                        break;
                    
                    case REGION:
                        this._region = SexpUtils.get_next_string(name, iter);
                        break;
                    
                    case CITY:
                        this._city = SexpUtils.get_next_string(name, iter);
                        break;
                    
                    case DMACODE:
                        this._dmacode = SexpUtils.get_next_string(name, iter);
                        break;
                    
                    case AREACODE:
                        this._areacode = SexpUtils.get_next_string(name, iter);
                        break;
                    
                    case ZIP_CODE:
                        this._zip_code = SexpUtils.get_next_string(name, iter);
                        break;
                        
                    case LATITUDE:
                        this._latitude = SexpUtils.get_next_string(name, iter);
                        break;
                        
                    case LONGITUDE:
                        this._longitude = SexpUtils.get_next_string(name, iter);
                        break;   
                    
                    
                    case MULTIVALUE_FIELD1:
                        this.multiValueField1 = SexpUtils.get_next_string(name, iter);
                        
                    case ADTYPE:
                        this.adType = SexpUtils.get_next_string(name, iter);
                        break;

                    case MULTIVALUE_FIELD2:
                        this.multiValueField3 = SexpUtils.get_next_string(name, iter);
                        break;

                    case MULTIVALUE_FIELD3:
                        this.multiValueField3 = SexpUtils.get_next_string(name, iter);
                        break;

                    case MULTIVALUE_FIELD4:
                        this.multiValueField4 = SexpUtils.get_next_string(name, iter);
                        break;

                    case MULTIVALUE_FIELD5:
                        this.multiValueField5 = SexpUtils.get_next_string(name, iter);
                        break;

                        
                    case RECIPE_ID:
                        this.recipeId = get_next_integer_or_nil_null(name, iter);
                        break;
                        
                    default:
                        log
                                .error("Program error, unrecognized request parameter: "
                                        + elm);
                        continue;
                }
            } catch (SexpUtils.BadGetNextException ex) {
                throw new BadCommandException(ex.getMessage());
            }
        }
    }
    
    // Same as SexpUtils.get_next_integer except t -> null.
    
    private static Integer get_next_integer_or_t_null(String param_name,
            Iterator<Sexp> iter) throws SexpUtils.BadGetNextException {
        if (!iter.hasNext())
            throw new SexpUtils.BadGetNextException("missing value for "
                    + param_name);
        Sexp e = iter.next();
        if (e.isSexpSymbol()) {
            SexpSymbol s = e.toSexpSymbol();
            if (s.equalsStringIgnoreCase("t"))
                return null;
            throw new SexpUtils.BadGetNextException("bad value for "
                    + param_name);
        }
        if (!e.isSexpInteger())
            throw new SexpUtils.BadGetNextException("bad value for "
                    + param_name);
        SexpInteger n = e.toSexpInteger();
        try {
            return n.toNativeInteger32();
        } catch (NumberFormatException ex) {
            throw new SexpUtils.BadGetNextException(
                    "non-32-bit-integer value for " + param_name);
        }
    }
    
    // Same as SexpUtils.get_next_integer except nil -> null.
    
    private static Integer get_next_integer_or_nil_null(String param_name,
            Iterator<Sexp> iter) throws SexpUtils.BadGetNextException {
        if (!iter.hasNext())
            throw new SexpUtils.BadGetNextException("missing value for "
                    + param_name);
        Sexp e = iter.next();
        // nil = empty list = null
        if (e.isSexpList()) {
            SexpList l = e.toSexpList();
            if (l.size() == 0)
                return null;
            throw new SexpUtils.BadGetNextException("bad value for " + param_name + ": "
                    + e.toString());
        }
        if (e.isSexpSymbol()) {
            SexpSymbol s = e.toSexpSymbol();
            if (s.equalsStringIgnoreCase("nil"))
                return null;
            throw new SexpUtils.BadGetNextException("bad value for "
                    + param_name);
        }
        if (!e.isSexpInteger())
            throw new SexpUtils.BadGetNextException("bad value for "
                    + param_name);
        SexpInteger n = e.toSexpInteger();
        try {
            return n.toNativeInteger32();
        } catch (NumberFormatException ex) {
            throw new SexpUtils.BadGetNextException(
                    "non-32-bit-integer value for " + param_name);
        }
    }
    
    // Parse an :ad-offer-type parameter.
    
    private static AdOfferType get_next_ad_offer_type(String param_name,
            Iterator<Sexp> iter) throws SexpUtils.BadGetNextException {
        if (!iter.hasNext())
            throw new SexpUtils.BadGetNextException("missing value for "
                    + param_name);
        Sexp e = iter.next();
        if (!e.isSexpKeyword())
            throw new SexpUtils.BadGetNextException("bad value for "
                    + param_name);
        SexpKeyword kw = e.toSexpKeyword();
        if (kw.equalsString(":product-only"))
            return AdOfferType.PRODUCT_ONLY;
        if (kw.equalsString(":leadgen-only"))
            return AdOfferType.LEADGEN_ONLY;
        if (kw.equalsString(":product-leadgen"))
            return AdOfferType.PRODUCT_LEADGEN;
        throw new SexpUtils.BadGetNextException("invalid value for "
                + param_name);
    }
    
    // Parse an :output-format parameter.
    
    private static OutputFormat get_next_output_format(String param_name,
            Iterator<Sexp> iter) throws SexpUtils.BadGetNextException {
        if (!iter.hasNext())
            throw new SexpUtils.BadGetNextException("missing value for "
                    + param_name);
        Sexp e = iter.next();
        if (!e.isSexpKeyword())
            throw new SexpUtils.BadGetNextException("bad value for "
                    + param_name);
        SexpKeyword kw = e.toSexpKeyword();
        if (kw.equalsString(":normal"))
            return OutputFormat.NORMAL;
        if (kw.equalsString(":js-friendly"))
            return OutputFormat.JS_FRIENDLY;
        throw new SexpUtils.BadGetNextException("invalid value for "
                + param_name);
    }
}
