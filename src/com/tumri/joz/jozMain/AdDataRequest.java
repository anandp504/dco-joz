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
                :num-products [t|&lt;integer&gt;] ;; t means return all products
                ;; if row-size and which-row are non-nil and integers then deterministically return a row/page of results:
                :row-size [nil|&lt;integer&gt;] 
                :which-row [nil|&lt;integer&gt;]
                :revert-to-default-realm [nil|t] ;; if t and don't get enough products then retry on default realm, defaults to t
                :keywords [nil|&lt;string&gt;] ;; white-space-separated keywords from search box in widget
                :script-keywords [nil|&lt;string&gt;] ;; white-space-separated keywords from publisher's script
                :include-cat-counts [nil|t] ;; add category-counts to output?
                :seed [nil|&lt;integer&gt;] ;; plug seed in here to deterministically replay a previous call
               ;; product selection parameters:
                :psychographics-p [t|nil|:maybe] ;; use psychographic curves when building roulette wheel
                :mine-ref-URL-p [t|nil|:maybe] ;; not currently used
                :mine-pub-URL-p [t|nil|:maybe] ;; pull keywords out of publisher URL and get products based on them
                :irrelevad-p [t|nil|:maybe] ;; not currently used
                :allow-too-few-products [t|nil|:maybe] ;; if t then we don't care about too few products
                :ad-width [nil|&lt;number&gt;] ;; for leadgens, only return offers of these dimensions
                :ad-height [nil|&lt;number&gt;]
                ;; if ad-offer-type is :product-only or :leadgen-only then appropriately filter the offers returned:
                :ad-offer-type [:product-only|:leadgen-only:product-leadgen]  ;; defaults to :product-only
                :min-num-leadgens [nil|&lt;integer&gt;] ;; if non-nil, *try* to put this many leadgens in the result
                :output-format [:normal|:js-friendly]  ;; defaults to :normal
                :output-order [:uniform-random|:deterministic-best-first|:perturbed-best-first] ;; defaults to :uniform-random, see note below
                :output-order-noise-stddev &lt;number&gt; ;; defaults to 0.1
*/

/*
  NOTE: There are no setter methods on purpose.

  NOTE: a null value means the parameter is unspecified
*/

package com.tumri.joz.jozMain;

//import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.tumri.utils.sexp.*;

public class AdDataRequest
{
    private static Logger log = null;

    static
    {
	String filename = System.getProperty ("LOG4J_PROPS");
	if (filename != null)
	{
	    System.out.println ("Loading log4j properties from " + filename);
	    PropertyConfigurator.configure (filename);
	}
	log = Logger.getLogger (AdDataRequest.class);
    }

    public AdDataRequest (Sexp expr)
	throws BadCommandException
    {
	parse_request (expr);
    }

    public String get_url () { return _url; }
    public String get_theme () { return _theme; }
    public String get_store_id () { return _store_id; }
    public String get_category () { return _category; }
    public String get_t_spec () { return _t_spec; }
    public String get_referrer () { return _referrer; }
    public String get_zip_code () { return _zip_code; }
    public String get_num_products () { return _num_products; }
    public String get_row_size () { return _row_size; }
    public String get_which_row () { return _which_row; }
    public Boolean get_revert_to_default_realm () { return _revert_to_default_realm; }
    public String get_keywords () { return _keywords; }
    public String get_script_keywords () { return _script_keywords; }
    public Boolean get_include_cat_counts () { return _include_cat_counts; }
    public String get_seed () { return _seed; }
    public Enums.MaybeBoolean get_psychographics_p () { return _psychographics_p; }
    public Enums.MaybeBoolean get_mine_pub_url_p () { return _mine_pub_url_p; }
    public Enums.MaybeBoolean get_allow_too_few_products () { return _allow_too_few_products; }
    public String get_ad_width () { return _ad_width; }
    public String get_ad_height () { return _ad_height; }
    public String get_ad_offer_type () { return _ad_offer_type; }
    public Integer get_min_num_leadgens () { return _min_num_leadgens; }
    public String get_output_format () { return _output_format; }
    public String get_output_order () { return _output_order; }
    public Double get_output_order_noise_stddev () { return _output_order_noise_stddev; }

    public String
    toString ()
    {
	return toString (false);
    }

    public String
    toString (boolean include_defaults)
    {
	StringBuilder b = new StringBuilder ();

	b.append ("(get-ad-data");

	if (include_defaults)
	{
	    b.append (" :url ").append (_url);
	    b.append (" :theme ").append (_theme);
	    b.append (" :store_id ").append (_store_id);
	    b.append (" :category ").append (_category);
	    b.append (" :t_spec ").append (_t_spec);
	    b.append (" :referrer ").append (_referrer);
	    b.append (" :zip_code ").append (_zip_code);
	    b.append (" :num_products ").append (_num_products);
	    b.append (" :row_size ").append (_row_size);
	    b.append (" :which_row ").append (_which_row);
	    b.append (" :revert_to_default_realm ").append (_revert_to_default_realm);
	    b.append (" :keywords ").append (_keywords);
	    b.append (" :script_keywords ").append (_script_keywords);
	    b.append (" :include_cat_counts ").append (_include_cat_counts);
	    b.append (" :seed ").append (_seed);
	    b.append (" :psychographics_p ").append (_psychographics_p);
	    b.append (" :mine_pub_url_p ").append (_mine_pub_url_p);
	    b.append (" :allow_too_few_products ").append (_allow_too_few_products);
	    b.append (" :ad_width ").append (_ad_width);
	    b.append (" :ad_height ").append (_ad_height);
	    b.append (" :ad_offer_type ").append (_ad_offer_type);
	    b.append (" :min_num_leadgens ").append (_min_num_leadgens);
	    b.append (" :output_format ").append (_output_format);
	    b.append (" :output_order ").append (_output_order);
	    b.append (" :output_order_noise_stddev ").append (_output_order_noise_stddev);
	}
	else
	{
	    if (_url != DEFAULT_URL)
		b.append (" :url ").append (_url);
	    if (_theme != DEFAULT_THEME)
		b.append (" :theme ").append (_theme);
	    if (_store_id != DEFAULT_STORE_ID)
		b.append (" :store_id ").append (_store_id);
	    if (_category != DEFAULT_CATEGORY)
		b.append (" :category ").append (_category);
	    if (_t_spec != DEFAULT_T_SPEC)
		b.append (" :t_spec ").append (_t_spec);
	    if (_referrer != DEFAULT_REFERRER)
		b.append (" :referrer ").append (_referrer);
	    if (_zip_code != DEFAULT_ZIP_CODE)
		b.append (" :zip_code ").append (_zip_code);
	    if (_num_products != DEFAULT_NUM_PRODUCTS)
		b.append (" :num_products ").append (_num_products);
	    if (_row_size != DEFAULT_ROW_SIZE)
		b.append (" :row_size ").append (_row_size);
	    if (_which_row != DEFAULT_WHICH_ROW)
		b.append (" :which_row ").append (_which_row);
	    if (_revert_to_default_realm != DEFAULT_REVERT_TO_DEFAULT_REALM)
		b.append (" :revert_to_default_realm ").append (_revert_to_default_realm);
	    if (_keywords != DEFAULT_KEYWORDS)
		b.append (" :keywords ").append (_keywords);
	    if (_script_keywords != DEFAULT_SCRIPT_KEYWORDS)
		b.append (" :script_keywords ").append (_script_keywords);
	    if (_include_cat_counts != DEFAULT_INCLUDE_CAT_COUNTS)
		b.append (" :include_cat_counts ").append (_include_cat_counts);
	    if (_seed != DEFAULT_SEED)
		b.append (" :seed ").append (_seed);
	    if (_psychographics_p != DEFAULT_PSYCHOGRAPHICS_P)
		b.append (" :psychographics_p ").append (_psychographics_p);
	    if (_mine_pub_url_p != DEFAULT_MINE_PUB_URL_P)
		b.append (" :mine_pub_url_p ").append (_mine_pub_url_p);
	    if (_allow_too_few_products != DEFAULT_ALLOW_TOO_FEW_PRODUCTS)
		b.append (" :allow_too_few_products ").append (_allow_too_few_products);
	    if (_ad_width != DEFAULT_AD_WIDTH)
		b.append (" :ad_width ").append (_ad_width);
	    if (_ad_height != DEFAULT_AD_HEIGHT)
		b.append (" :ad_height ").append (_ad_height);
	    if (_ad_offer_type != DEFAULT_AD_OFFER_TYPE)
		b.append (" :ad_offer_type ").append (_ad_offer_type);
	    if (_min_num_leadgens != DEFAULT_MIN_NUM_LEADGENS)
		b.append (" :min_num_leadgens ").append (_min_num_leadgens);
	    if (_output_format != DEFAULT_OUTPUT_FORMAT)
		b.append (" :output_format ").append (_output_format);
	    if (_output_order != DEFAULT_OUTPUT_ORDER)
		b.append (" :output_order ").append (_output_order);
	    if (_output_order_noise_stddev != DEFAULT_OUTPUT_ORDER_NOISE_STDDEV)
		b.append (" :output_order_noise_stddev ").append (_output_order_noise_stddev);
	}

	b.append (")");

	return b.toString ();
    }

    // implementation details -------------------------------------------------

    private enum RqstParam
    {
	URL,
	THEME,
	STORE_ID,
	CATEGORY,
	T_SPEC,
	REFERRER,
	ZIP_CODE,
	NUM_PRODUCTS,
	ROW_SIZE,
	WHICH_ROW,
	REVERT_TO_DEFAULT_REALM,
	KEYWORDS,
	SCRIPT_KEYWORDS,
	INCLUDE_CAT_COUNTS,
	SEED,
	PSYCHOGRAPHICS_P,
	MINE_PUB_URL_P,
	ALLOW_TOO_FEW_PRODUCTS,
	AD_WIDTH,
	AD_HEIGHT,
	AD_OFFER_TYPE,
	MIN_NUM_LEADGENS,
	OUTPUT_FORMAT,
	OUTPUT_ORDER,
	OUTPUT_ORDER_NOISE_STDDEV,
    }

    private static HashMap<String, RqstParam> rqst_params =
	new HashMap<String, RqstParam> ();

    static
    {
	// FIXME: collisions?
	rqst_params.put (":url", RqstParam.URL);
	rqst_params.put (":theme", RqstParam.THEME);
	rqst_params.put (":store-ID", RqstParam.STORE_ID);
	rqst_params.put (":category", RqstParam.CATEGORY);
	rqst_params.put (":t-spec", RqstParam.T_SPEC);
	rqst_params.put (":referrer", RqstParam.REFERRER);
	rqst_params.put (":zip-code", RqstParam.ZIP_CODE);
	rqst_params.put (":num-products", RqstParam.NUM_PRODUCTS);
	rqst_params.put (":row-size", RqstParam.ROW_SIZE);
	rqst_params.put (":which-row", RqstParam.WHICH_ROW);
	rqst_params.put (":revert-to-default-realm", RqstParam.REVERT_TO_DEFAULT_REALM);
	rqst_params.put (":keywords", RqstParam.KEYWORDS);
	rqst_params.put (":script-keywords", RqstParam.SCRIPT_KEYWORDS);
	rqst_params.put (":include-cat-counts", RqstParam.INCLUDE_CAT_COUNTS);
	rqst_params.put (":seed", RqstParam.SEED);
	rqst_params.put (":psychographics-p", RqstParam.PSYCHOGRAPHICS_P);
	rqst_params.put (":min-pub-URL-p", RqstParam.MINE_PUB_URL_P);
	rqst_params.put (":allow-too-few-products", RqstParam.ALLOW_TOO_FEW_PRODUCTS);
	rqst_params.put (":ad-width", RqstParam.AD_WIDTH);
	rqst_params.put (":ad-height", RqstParam.AD_HEIGHT);
	rqst_params.put (":ad-offer-type", RqstParam.AD_OFFER_TYPE);
	rqst_params.put (":min-num-leadgens", RqstParam.MIN_NUM_LEADGENS);
	rqst_params.put (":output-format", RqstParam.OUTPUT_FORMAT);
	rqst_params.put (":output-order", RqstParam.OUTPUT_ORDER);
	rqst_params.put (":output-order-noise-stddev", RqstParam.OUTPUT_ORDER_NOISE_STDDEV);
    }

    // WARNING: If the default value is not null, you're probably doing
    // something wrong.  "null" here means "unspecified", and that is generally
    // the connotation you want to have for default values here.  If you do
    // specify a non-null default, a comment describing WHY it is non-null is
    // REQUIRED.

    private static final String DEFAULT_URL = null;
    private static final String DEFAULT_THEME = null;
    private static final String DEFAULT_STORE_ID = null;
    private static final String DEFAULT_CATEGORY = null;
    private static final String DEFAULT_T_SPEC = null;
    private static final String DEFAULT_REFERRER = null;
    private static final String DEFAULT_ZIP_CODE = null;
    private static final String DEFAULT_NUM_PRODUCTS = null;
    private static final String DEFAULT_ROW_SIZE = null;
    private static final String DEFAULT_WHICH_ROW = null;
    private static final Boolean DEFAULT_REVERT_TO_DEFAULT_REALM = new Boolean (true);
    private static final String DEFAULT_KEYWORDS = null;
    private static final String DEFAULT_SCRIPT_KEYWORDS = null;
    // FIXME: should be null, fix.
    private static final Boolean DEFAULT_INCLUDE_CAT_COUNTS = new Boolean (false); // FIXME: check default value
    private static final String DEFAULT_SEED = null;
    private static final Enums.MaybeBoolean DEFAULT_PSYCHOGRAPHICS_P = null;
    private static final Enums.MaybeBoolean DEFAULT_MINE_PUB_URL_P = null;
    private static final Enums.MaybeBoolean DEFAULT_ALLOW_TOO_FEW_PRODUCTS = null;
    private static final String DEFAULT_AD_WIDTH = null;
    private static final String DEFAULT_AD_HEIGHT = null;
    private static final String DEFAULT_AD_OFFER_TYPE = null;
    private static final Integer DEFAULT_MIN_NUM_LEADGENS = null;
    private static final String DEFAULT_OUTPUT_FORMAT = ":normal";
    private static final String DEFAULT_OUTPUT_ORDER = null;
    // FIXME: should be null, fix.
    private static final Double DEFAULT_OUTPUT_ORDER_NOISE_STDDEV = new Double (0.1);

    String _url = DEFAULT_URL;
    String _theme = DEFAULT_THEME;
    String _store_id = DEFAULT_STORE_ID;
    String _category = DEFAULT_CATEGORY;
    // if non-null, ignore :url, :theme, and :store-id
    String _t_spec = DEFAULT_T_SPEC;
    String _referrer = DEFAULT_REFERRER;
    String _zip_code = DEFAULT_ZIP_CODE;
    // "all" means return all products
    String _num_products = DEFAULT_NUM_PRODUCTS;
    // If row-size and which-row are non-nil and integers then
    // deterministically return a row/page of results.
    String _row_size = DEFAULT_ROW_SIZE;
    String _which_row = DEFAULT_WHICH_ROW;
    Boolean _revert_to_default_realm = DEFAULT_REVERT_TO_DEFAULT_REALM;
    // white-space-separated keywords from search box in widget
    String _keywords = DEFAULT_KEYWORDS;
    // white-space-separated keywords from publisher's script
    String _script_keywords = DEFAULT_SCRIPT_KEYWORDS;
    // add category-counts to output?
    Boolean _include_cat_counts = DEFAULT_INCLUDE_CAT_COUNTS;
    // plug seed in here to deterministically replay a previous call
    String _seed = DEFAULT_SEED;
    // use psychographic curves when building roulette wheel
    Enums.MaybeBoolean _psychographics_p = DEFAULT_PSYCHOGRAPHICS_P;
    // pull keywords out of publisher URL and get products based on them
    Enums.MaybeBoolean _mine_pub_url_p = DEFAULT_MINE_PUB_URL_P;
    // if t then we don't care about too few products
    Enums.MaybeBoolean _allow_too_few_products = DEFAULT_ALLOW_TOO_FEW_PRODUCTS;
    // for leadgens, only return offers of these dimensions
    String _ad_width = DEFAULT_AD_WIDTH;
    String _ad_height = DEFAULT_AD_HEIGHT;
    // one of :product-only, :leadgen-only, or product-leadgen
    String _ad_offer_type = DEFAULT_AD_OFFER_TYPE;
    // if non-null, *try* to put this many leadgens in the result
    Integer _min_num_leadgens = DEFAULT_MIN_NUM_LEADGENS;
    // one of :normal, :js-friendly, defaults to :normal
    String _output_format = DEFAULT_OUTPUT_FORMAT;
    // one of :uniform-random|:deterministic-best-first|:perturbed-best-first
    // default is :uniform-random
    String _output_order = DEFAULT_OUTPUT_ORDER;
    // default is 0.1
    Double _output_order_noise_stddev = DEFAULT_OUTPUT_ORDER_NOISE_STDDEV;

    private void
    parse_request (Sexp expr)
	throws BadCommandException
    {
	if (! expr.isSexpList ())
	    throw new BadCommandException ("get-ad-data request not a list");
	SexpList l = expr.toSexpList ();
	int n = l.size ();

	Iterator<Sexp> iter = l.iterator ();

	while (iter.hasNext ())
	{
	    Sexp elm = iter.next ();

	    if (! elm.isSexpKeyword ())
	    {
		// FIXME: TODO
		assert (false);
	    }

	    SexpKeyword k = elm.toSexpKeyword ();
	    String name = k.toStringValue ();

	    RqstParam p = rqst_params.get (name);
	    if (p == null)
	    {
		// bad/unsupported parameter
		// FIXME: TODO
		continue;
	    }

	    String str = null;

	    try
	    {
		switch (p)
		{
		case URL:
		    this._url = SexpUtils.get_next_string (name, iter);
		    break;

		case THEME:
		    this._theme = SexpUtils.get_next_string (name, iter);
		    break;

		case STORE_ID:
		    this._store_id = SexpUtils.get_next_string (name, iter);
		    break;

		case CATEGORY:
		    this._category = SexpUtils.get_next_string (name, iter);
		    break;

		case T_SPEC:
		    this._t_spec = SexpUtils.get_next_string (name, iter);
		    break;

		case REFERRER:
		    this._referrer = SexpUtils.get_next_string (name, iter);
		    break;

		case ZIP_CODE:
		    this._zip_code = SexpUtils.get_next_string (name, iter);
		    break;

		case NUM_PRODUCTS:
		    this._num_products = SexpUtils.get_next_string (name, iter);
		    break;

		case ROW_SIZE:
		    this._row_size = SexpUtils.get_next_string (name, iter);
		    break;

		case WHICH_ROW:
		    this._which_row = SexpUtils.get_next_string (name, iter);
		    break;

		case REVERT_TO_DEFAULT_REALM:
		    this._revert_to_default_realm = SexpUtils.get_next_boolean (name, iter);
		    break;

		case KEYWORDS:
		    this._keywords = SexpUtils.get_next_string (name, iter);
		    break;

		case SCRIPT_KEYWORDS:
		    this._script_keywords = SexpUtils.get_next_string (name, iter);
		    break;

		case INCLUDE_CAT_COUNTS:
		    this._include_cat_counts = SexpUtils.get_next_boolean (name, iter);
		    break;

		case SEED:
		    this._seed = SexpUtils.get_next_string (name, iter);
		    break;

		case PSYCHOGRAPHICS_P:
		    this._psychographics_p = SexpUtils.get_next_maybe_boolean (name, iter);
		    break;

		case MINE_PUB_URL_P:
		    this._mine_pub_url_p = SexpUtils.get_next_maybe_boolean (name, iter);
		    break;

		case ALLOW_TOO_FEW_PRODUCTS:
		    this._allow_too_few_products = SexpUtils.get_next_maybe_boolean (name, iter);
		    break;

		case AD_WIDTH:
		    this._ad_width = SexpUtils.get_next_string (name, iter);
		    break;

		case AD_HEIGHT:
		    this._ad_height = SexpUtils.get_next_string (name, iter);
		    break;

		case AD_OFFER_TYPE:
		    this._ad_offer_type = SexpUtils.get_next_string (name, iter);
		    break;

		case MIN_NUM_LEADGENS:
		    this._min_num_leadgens = SexpUtils.get_next_integer (name, iter);
		    break;

		case OUTPUT_FORMAT:
		    this._output_format = SexpUtils.get_next_string (name, iter);
		    break;

		case OUTPUT_ORDER:
		    this._output_order = SexpUtils.get_next_string (name, iter);
		    break;

		case OUTPUT_ORDER_NOISE_STDDEV:
		    this._output_order_noise_stddev = SexpUtils.get_next_double (name, iter);
		    break;

		default:
		    assert (false);
		}
	    }
	    catch (SexpUtils.BadGetNextException ex)
	    {
		throw new BadCommandException (ex.getMessage ());
	    }
	}
    }
}
