// A t-spec.
// NOTE: There are no setter methods on purpose!
// NOTE: null means "unspecified".
// FIXME: wip

package com.tumri.joz.jozMain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.*;

import com.tumri.joz.Query.*;
import com.tumri.joz.products.*;
import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.index.CategoryIndex;

public class TSpec
{
    public TSpec (Iterator<Sexp> iter)
	throws BadTSpecException
    {
	process_iter (iter);

	// Generate a name if not provided.
	if (_name == null)
	{
	    _name = "t-spec-" + String.valueOf (next_anonymous);
	    ++next_anonymous;
	}

	_query.addQuery (_cjquery);
    }

    public TSpec (String name, Integer version, Integer max_prods,
		  Long modified)
    {
	_name = name;
	_version = version;
	_max_prods = max_prods;
	_modified_time = modified;

	_query.addQuery (_cjquery);
    }

    public String get_name () { return _name; }
    public Integer get_version () { return _version; }
    public Integer get_max_prods () { return _max_prods; }
    public Long get_modified_time () { return _modified_time; }

    // FIXME: for now
    public boolean private_label_p () { return false; }

    public CNFQuery get_query () { return _query; }

    // implementation details -------------------------------------------------

    private String _name = null;
    private Integer _version = -1;
    private Integer _max_prods = 42; // FIXME, max-prods-per-realm
    private Long _modified_time = System.currentTimeMillis ();

    private CNFQuery _query = new CNFQuery ();
    private ConjunctQuery _cjquery = new ConjunctQuery ();

    // Used to give unique names to anonymous t-specs.
    private static long next_anonymous = 0;

    private static Logger log = Logger.getLogger (TSpec.class);

    // FIXME: wip
    private enum TSpecParam
    {
	NAME,
	VERSION,
	MAX_PRODS,
	MODIFIED_TIME,
	LOAD_TIME_KEYWORD_EXPR,
	ZINI_KEYWORDS,
	INCLUDE_CATEGORIES,
	EXCLUDE_CATEGORIES,
	ATTR_INCLUSIONS,
	ATTR_EXCLUSIONS,
	INCOME_PERCENTILE,
	REF_PRICE_CONSTRAINTS,
	RANK_CONSTRAINTS,
	CPC_RANGE,
	CPO_RANGE,
    }

    private static HashMap<String, TSpecParam> tspec_params =
	new HashMap<String, TSpecParam> ();

    static
    {
	// FIXME: collisions?
	tspec_params.put (":name", TSpecParam.NAME);
	tspec_params.put (":version", TSpecParam.VERSION);
	tspec_params.put (":max-prods", TSpecParam.MAX_PRODS);
	tspec_params.put (":modified", TSpecParam.MODIFIED_TIME);
	tspec_params.put (":load-time-keyword-expr", TSpecParam.LOAD_TIME_KEYWORD_EXPR);
	tspec_params.put (":zini-keywords", TSpecParam.ZINI_KEYWORDS);
	tspec_params.put (":include-categories", TSpecParam.INCLUDE_CATEGORIES);
	tspec_params.put (":exclude-categories", TSpecParam.EXCLUDE_CATEGORIES);
	tspec_params.put (":attr-inclusions", TSpecParam.ATTR_INCLUSIONS);
	tspec_params.put (":attr-exclusions", TSpecParam.ATTR_EXCLUSIONS);
	tspec_params.put (":income-percentile", TSpecParam.INCOME_PERCENTILE);
	tspec_params.put (":ref-price-constraints", TSpecParam.REF_PRICE_CONSTRAINTS);
	tspec_params.put (":rank-constraints", TSpecParam.RANK_CONSTRAINTS);
	tspec_params.put (":cpc-range", TSpecParam.CPC_RANGE);
	tspec_params.put (":cpo-range", TSpecParam.CPO_RANGE);
    }

    private void
    process_iter (Iterator<Sexp> iter)
	throws BadTSpecException
    {
	while (iter.hasNext ())
	{
	    Sexp elm = iter.next ();
	    Sexp t;

	    if (! elm.isSexpKeyword ())
	    {
		// FIXME: TODO
		assert (false);
	    }

	    SexpKeyword k = elm.toSexpKeyword ();
	    String name = k.toStringValue ();

	    TSpecParam p = tspec_params.get (name.toLowerCase ());
	    if (p == null)
	    {
		// bad/unsupported parameter
		// FIXME: TODO, we still don't support all the current ones
		log.error ("unsupported t-spec parameter: " + name);
		t = iter.next (); // gobble up the parameter value
		continue;
	    }

	    String value = null;
	    SimpleQuery sq = null;

	    try
	    {
		switch (p)
		{
		case NAME:
		    this._name = SexpUtils.get_next_symbol (name, iter);
		    break;

		case VERSION:
		    this._version = SexpUtils.get_next_integer (name, iter);
		    break;

		case MAX_PRODS:
		    this._max_prods = SexpUtils.get_next_integer (name, iter);
		    break;

		case MODIFIED_TIME:
		{
		    // FIXME: wip
		    //this._modified_time = ???
		    t = iter.next ();
		    break;
		}

		case LOAD_TIME_KEYWORD_EXPR:
		    t = iter.next ();
		    // FIXME: wip
		    break;

		case ZINI_KEYWORDS:
		    t = iter.next ();
		    // FIXME: wip
		    break;

		case INCLUDE_CATEGORIES:
		{
		    value = SexpUtils.get_next_string (name, iter);
		    ArrayList<Integer> values = getValues (IProduct.Attribute.kCategory, value);
		    sq =  new AttributeQuery (IProduct.Attribute.kCategory, values);
		    break;
		}

		case EXCLUDE_CATEGORIES:
		{
		    value = SexpUtils.get_next_string (name, iter);
		    ArrayList<Integer> values = getValues (IProduct.Attribute.kCategory, value);
		    sq =  new AttributeQuery (IProduct.Attribute.kCategory, values);
		    sq.setNegation (true);
		    break;
		}

		case ATTR_INCLUSIONS:
		{
		    value = SexpUtils.get_next_string (name, iter);
		    // FIXME: wip
		    break;
		}

		case ATTR_EXCLUSIONS:
		{
		    value = SexpUtils.get_next_string (name, iter);
		    // FIXME: wip
		    break;
		}

		case INCOME_PERCENTILE:
		{
		    value = SexpUtils.get_next_string (name, iter);
		    // FIXME: wip
		    break;
		}

		case REF_PRICE_CONSTRAINTS:
		{
		    value = SexpUtils.get_next_string (name, iter);
		    ArrayList<Double> values = getRangeValues (value);
		    sq =  new RangeQuery (IProduct.Attribute.kPrice, values.get (0), values.get (1));
		    break;
		}

		case RANK_CONSTRAINTS:
		{
		    value = SexpUtils.get_next_string (name, iter);
		    // FIXME: wip
		    break;
		}

		case CPC_RANGE:
		{
		    value = SexpUtils.get_next_string (name, iter);
		    ArrayList<Double> values = getRangeValues (value);
		    sq =  new RangeQuery (IProduct.Attribute.kCPC, values.get (0), values.get (1));
		    break;
		}

		case CPO_RANGE:
		{
		    value = SexpUtils.get_next_string (name, iter);
		    // FIXME: wip
		    break;
		}

		default:
		    assert (false);
		}
	    }
	    catch (SexpUtils.BadGetNextException ex)
	    {
		throw new BadTSpecException (ex.getMessage ());
	    }

	    if (sq != null)
	    {
		_cjquery.addQuery (sq);
	    }
	}
    }

    private ArrayList<Integer>
    getValues (IProduct.Attribute attr, String value)
    {
	// FIXME: use of StringTokenizer is discouraged
	StringTokenizer tokens = new StringTokenizer (value," ",false);
	DictionaryManager dm = DictionaryManager.getInstance();
	ArrayList<Integer> vals = new ArrayList<Integer>();
	String last = null;
	while(tokens.hasMoreTokens())
	{
	    String token = tokens.nextToken().trim();
	    if (last == null)
	    {
		if (token.startsWith("|"))
		{
		    last = token;
		    if (token.endsWith("|"))
		    {
			last = last.substring(1,last.length()-1);
			vals.add(dm.getId(attr,last));
			last = null;
		    }
		}
		else
		{
		    vals.add(dm.getId(attr,token));
		}
	    }
	    else
	    {
		last = last + " " + token;
		if (token.endsWith("|"))
		{
		    last = last.substring(1,last.length()-1);
		    vals.add(dm.getId(attr,last));
		    last = null;
		}
	    }
	}
	return vals;
    }

    private ArrayList<Double>
    getRangeValues(String value)
    {
	// FIXME: use of StringTokenizer is discouraged
	StringTokenizer tokens = new StringTokenizer(value, " ", false);
	ArrayList<Double> vals = new ArrayList<Double>();
	while (tokens.hasMoreTokens())
	{
	    String token = tokens.nextToken().trim();
	    try
	    {
		vals.add(Double.parseDouble(token));
	    }
	    catch (NumberFormatException e)
	    {
		vals.add(new Double(vals.size() == 0 ? 0 : 100000));
	    }
	}
	while (vals.size() < 2)
	{
	    vals.add(new Double(vals.size() == 0 ? 0 : 100000));
	}
	return vals;
    }
}
