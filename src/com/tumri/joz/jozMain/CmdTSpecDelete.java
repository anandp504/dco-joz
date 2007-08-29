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
	    if (! expr.isSexpSymbol ())
		return SexpReader.readFromStringNoex ("(:error \"expected t-spec name\")");
	    e = new SexpList (); // FIXME: wip
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

    private static Logger log = Logger.getLogger (CmdTSpecDelete.class);
}
