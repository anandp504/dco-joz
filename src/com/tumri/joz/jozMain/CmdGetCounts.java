// get-counts command

/* The get-counts command has the following format:
   See https://secure.tumri.com/twiki/bin/view/Engineering/SoZandTC.

   (get-counts :t-spec-name 'symbol)

   If t-spec-name is nil then process the entire MUP, otherwise process
   all products in that t-spec's extension.  We count up the
   number of products in each category, the number of products in each
   brand and the number of products in each merchant.  Returns a 3-tuple:
   (category-counts brand-counts merchant-counts) where category counts
   is a list where cars are category symbols and cadrs are counts.   Only
   non-zero counts are included.  Similarly, for the brand-counts, the car
   is the brand and for the merchant-counts the car is the merchant name.
*/

package com.tumri.joz.jozMain;

//import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.*;

public class CmdGetCounts extends CommandDeferWriting
{
    public CmdGetCounts (Sexp e)
    {
	super (e);
    }

    public Sexp
    process ()
    {
	Sexp e;

	try
	{
	    e = JozData.mup_db.get_counts (null);
	}
	catch (Exception ex)
	{
	    e = SexpReader.readFromStringNoex ("(:error \""
					       + ex.toString ()
					       + "\")");
	}

	return e;
    }

    // implementation details -------------------------------------------------

    private static Logger log = Logger.getLogger (CmdGetCounts.class);
}
