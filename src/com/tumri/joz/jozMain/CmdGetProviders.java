// get-providers command

/* The get-providers command has the following format.
   It takes no parameters and returns a list of provider id/name tuples.
*/

package com.tumri.joz.jozMain;

//import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.*;

public class CmdGetProviders extends CommandDeferWriting
{
    public CmdGetProviders (Sexp e)
    {
	super (e);
    }

    public Sexp
    process ()
    {
	Sexp e;

	try
	{
	    HashSet<String> providers = new HashSet<String> ();
	    Iterator<SelectedProduct> products = SelectProducts.get_entire_mup ();

	    while (products.hasNext ())
	    {
		SelectedProduct p = products.next ();
		providers.add (p.get_merchant ());
	    }

	    SexpList l = new SexpList ();

	    for (String s : providers)
	    {
		l.addLast (new SexpList (new SexpString (s),
					 new SexpSymbol (s)));
	    }

	    e = l;
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

    private static Logger log = Logger.getLogger (CmdGetProviders.class);
}