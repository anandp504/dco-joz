// t-spec-delete command

/* The t-spec-delete command has the following format.
   See https://secure.tumri.com/twiki/bin/view/Engineering/SoZandTC#t_spec_delete.

   (t-spec-delete :t-spec-name 'symbol)

   If `symbol' is not a valid t-spec then the request is ignored.
*/

package com.tumri.joz.jozMain;

//import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.*;

import com.tumri.joz.campaign.CampaignDataCache;

public class CmdTSpecDelete extends CommandDeferWriting
{
    public CmdTSpecDelete (Sexp e)
    {
	super (e);
    }

    public Sexp
    process ()
    {
	Sexp e;

	try
	{
	    if (! expr.isSexpList ())
		return SexpReader.readFromStringNoex ("(:error \"expected (t-spec-delete name)\")");
	    e = delete_tspec (expr.toSexpList ());
	}
	catch (Exception ex)
	{
	    // Convert {ex} to SexpString first so we can use its toString()
	    // method to escape "s.
	    SexpString ex_string = new SexpString (ex.toString ());
	    e = SexpReader.readFromStringNoex ("(:error "
					       + ex_string.toString ()
					       + ")");
	}

	return e;
    }

    // implementation details -------------------------------------------------

    private static Logger log = Logger.getLogger (CmdTSpecDelete.class);

    private Sexp
    delete_tspec (SexpList rqst)
	throws BadCommandException
    {
	if (rqst.size () != 2
	    || ! rqst.get (1).isSexpSymbol ())
	{
	    throw new BadCommandException ("expected (tspec-delete name)");
	}
	SexpSymbol name = rqst.get (1).toSexpSymbol ();

	CampaignDataCache c = CampaignDataCache.getInstance ();
	c.doTSpecDelete (rqst);

	// FIXME: not sure what the "success" result is
	return new SexpList ();
    }
}
