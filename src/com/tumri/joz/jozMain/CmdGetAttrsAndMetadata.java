// get-attributes-and-metadata command

package com.tumri.joz.jozMain;

//import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.tumri.utils.sexp.Sexp;

public class CmdGetAttrsAndMetadata extends Command
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
	log = Logger.getLogger (CmdGetAttrsAndMetadata.class);
    }

    public CmdGetAttrsAndMetadata (Sexp e)
    {
	super (e);
    }

    public Sexp
    process ()
    {
	Sexp e = JozData.merchant_db.get_attributes_and_metadata ();
	return e;
    }

    // At least one part of ZiniJavaAPI *requires* uppercase symbols.
    // E.g. {Row.valueMap} maps attribute names to values and {AdObject}
    // uses uppercase values for the attribute names.  This affects
    // processing of the tabulate-search-results command.
    public boolean need_uppercase_syms () { return true; }
}
