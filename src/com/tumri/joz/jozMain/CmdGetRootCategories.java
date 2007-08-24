// get-root-categories command

/* The get-root-categories command has the following format.
   ???

   NOTE: soz doesn't currently implement this command, so we just return
   an error response.
*/

package com.tumri.joz.jozMain;

//import java.io.PrintWriter;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.*;

import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.joz.products.IProduct;

public class CmdGetRootCategories extends CommandDeferWriting
{
    public CmdGetRootCategories (Sexp e)
    {
	super (e);
    }

    public Sexp
    process ()
    {
	Sexp e;

	try
	{
	    DictionaryManager dm = DictionaryManager.getInstance ();
	    JOZTaxonomy tax = JOZTaxonomy.getInstance ();
	    throw new RuntimeException ("get-root-categories not implemented");
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

    private static Logger log = Logger.getLogger (CmdGetRootCategories.class);
}
