// t-spec-add command

/* The t-spec-add command has the following format.
   See https://secure.tumri.com/twiki/bin/view/Engineering/SoZandTC#t_spec_add

   (t-spec-add
    :name '<name-of-t-spec>
    :version -1
    :modified <number|string>
    :max-prods <integer> ;; maximum number of prods in this t-spec (defaults to 5,000)
    :load-time-keyword-expr [nil|string]
    :zini-keywords ;; deprecated
    :include-categories '()   
    :exclude-categories '()
    :attr-inclusions '()
    :attr-exclusions '()
    :include-products '()
    :exclude-products '()
    ;; car is lower %ile, cdr is upper %ile, nil = none:
    :income-percentile '()
    ;; ditto:
    :ref-price-constraints
    ;; ditto:
    :rank-constraints
    ;; ditto:
    :CPC-range
    ;; ditto:
    :CPO-range
    :sex nil ;; not currently used
    :age nil ;; not currently used
    :weight-map-name nil ;; not currently used
    :weight-map-combo-scheme (or :both :just-global :just-local))
    :psychographics-p [t|nil|:maybe]  ;; use psychographic curves when building roulette wheel
    :mine-ref-URL-p [t|nil|:maybe]   ;; not currently used
    :mine-pub-URL-p [t|nil|:maybe]   ;; pull keywords out of publisher URL and get products based on them
    :irrelevad-p [t|nil|:maybe]  ;; not currently used
    :script-keywords-within-t-spec-only [nil|t] ;; see note below
    :pub-URL-keywords-within-t-spec-only [nil|t] ;; see note below
    :pub-URL-keywords-stop-words [nil|string] ;; see note below
    :pub-URL-keywords-scavenger ;; see note below
    :pub-URL-keywords-q-wrapper ;; see note below
    :pub-URL-keywords-query-names ;; see note below
    :allow-too-small-leaf-cats-p [t|nil]
    )

    Only :name and :version are mandatory.

*/

package com.tumri.joz.jozMain;

//import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.*;

public class CmdTSpecAdd extends CommandDeferWriting
{
    public CmdTSpecAdd (Sexp e)
    {
	super (e);
    }

    public Sexp
    process ()
    {
	Sexp e;

	try
	{
	    if (! expr.isSexpSymbol ())
		return SexpReader.readFromStringNoex ("(:error \"expected t-spec name\")");
	    e = JozData.mup_db.get_counts (expr.toString ());
	}
	catch (Exception ex)
	{
	    e = SexpReader.readFromStringNoex ("(:error \""
					       // FIXME: need to escape "s
					       + ex.toString ()
					       + "\")");
	}

	return e;
    }

    // implementation details -------------------------------------------------

    private static Logger log = Logger.getLogger (CmdTSpecAdd.class);
}
