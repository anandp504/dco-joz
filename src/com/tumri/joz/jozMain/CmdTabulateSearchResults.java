// tabulate-search-results command
//
// The result is a list of four elements:
//
// - (1 num-merchants num-merchants)
// - nil
// - expected column ordering, a list of strings:
//   ("Merchant ID" "Merchant Name" "Catalog Name" "Merchant Rating"
//    "Logo URL" "Home Page URL" "Category" "Collects Taxes?"
//    "Catalog File" "Product Count" "Review Info" "Contact Info"
//    "Shipping Promotion" "Return Policy")
// - list of merchant data, one element per merchant:
//   (merchant-name nil col2 col3 ... colN)
//   where `col2 col3 ... colN' are the expected column ordering fields
//   with "Merchant ID" removed, and the value is nil if there is no data,
//   otherwise a list of a list of two elements with the same value, namely the
//   value for that column, e.g. (("Runnersgear.com" "Runnersgear.com")) for
//   the "Merchant Name" column

package com.tumri.joz.jozMain;

//import java.io.PrintWriter;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.Sexp;

public class CmdTabulateSearchResults extends CommandDeferWriting {
    
    public CmdTabulateSearchResults(Sexp e) {
        super(e);
    }
    
    public Sexp process() {
        Sexp e = MerchantDB.getInstance().getTabulatedSearchResults();
        return e;
    }
    
    // At least one part of ZiniJavaAPI *requires* uppercase symbols.
    // E.g. {Row.valueMap} maps attribute names to values and {AdObject}
    // uses uppercase values for the attribute names. This affects
    // processing of the tabulate-search-results command.
    public boolean need_uppercase_syms() {
        return true;
    }
    
    // implementation details -------------------------------------------------
    
    private static Logger log = Logger
            .getLogger(CmdTabulateSearchResults.class);
}
