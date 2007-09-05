// tabulate-search-results command

package com.tumri.joz.jozMain;

//import java.io.PrintWriter;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.Sexp;

public class CmdTabulateSearchResults extends CommandDeferWriting
{
    public CmdTabulateSearchResults (Sexp e)
    {
	super (e);
    }

    public Sexp
    process ()
    {
	Sexp e = MerchantDB.getInstance().getTabulatedSearchResults();
	return e;
    }

    // At least one part of ZiniJavaAPI *requires* uppercase symbols.
    // E.g. {Row.valueMap} maps attribute names to values and {AdObject}
    // uses uppercase values for the attribute names.  This affects
    // processing of the tabulate-search-results command.
    public boolean need_uppercase_syms () { return true; }

    // implementation details -------------------------------------------------

    private static Logger log = Logger.getLogger (CmdTabulateSearchResults.class);
}
