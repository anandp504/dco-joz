// A t-spec.
// NOTE: There are no setter methods on purpose!
// NOTE: null means "unspecified".
// FIXME: wip

package com.tumri.joz.jozMain;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.*;

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
    }

    public TSpec (String name, Integer version, Integer max_prods,
		  Long modified)
    {
	_name = name;
	_version = version;
	_max_prods = max_prods;
	_modified_time = modified;
    }

    public String get_name () { return _name; }
    public Integer get_version () { return _version; }
    public Integer get_max_prods () { return _max_prods; }
    public Long get_modified_time () { return _modified_time; }

    // FIXME: for now
    public boolean private_label_p () { return false; }

    // implementation details -------------------------------------------------

    String _name = null;
    Integer _version = -1;
    Integer _max_prods = 42; // FIXME, max-prods-per-realm
    Long _modified_time = System.currentTimeMillis ();

    // Used to give unique names to anonymous t-specs.
    static long next_anonymous = 0;

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

	    String str = null;

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
		    break;

		case ZINI_KEYWORDS:
		    t = iter.next ();
		    break;

		case INCLUDE_CATEGORIES:
		    t = iter.next ();
		    break;

		case EXCLUDE_CATEGORIES:
		    t = iter.next ();
		    break;

		case ATTR_INCLUSIONS:
		    t = iter.next ();
		    break;

		case ATTR_EXCLUSIONS:
		    t = iter.next ();
		    break;

		case INCOME_PERCENTILE:
		    t = iter.next ();
		    break;

		case REF_PRICE_CONSTRAINTS:
		    t = iter.next ();
		    break;

		case RANK_CONSTRAINTS:
		    t = iter.next ();
		    break;

		case CPC_RANGE:
		    t = iter.next ();
		    break;

		case CPO_RANGE:
		    t = iter.next ();
		    break;

		default:
		    assert (false);
		}
	    }
	    catch (SexpUtils.BadGetNextException ex)
	    {
		throw new BadTSpecException (ex.getMessage ());
	    }
	}
    }
}
