// get-root-categories command

/* The get-root-categories command has the following format.
 ???

 NOTE: soz doesn't currently implement this command, so we just return
 an error response.
 */

package com.tumri.joz.jozMain;

// import java.io.PrintWriter;
import org.apache.log4j.Logger;

import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpString;
import com.tumri.content.data.dictionary.DictionaryManager;

public class CmdGetRootCategories extends CommandDeferWriting {
    
    public CmdGetRootCategories(Sexp e) {
        super(e);
    }
    
    public Sexp process() {
        Sexp e;
        
        try {
            DictionaryManager dm = DictionaryManager.getInstance();
            JOZTaxonomy tax = JOZTaxonomy.getInstance();
            throw new RuntimeException(
                    "get-root-categories not implemented (nobody uses it anymore)");
        } catch (Exception ex) {
            // Convert {ex} to SexpString first so we can use its toString()
            // method to escape "s.
            SexpString ex_string = new SexpString(ex.toString());
            e = SexpReader.readFromStringNoex("(:error " + ex_string.toString()
                    + ")");
        }
        
        return e;
    }
    
    // implementation details -------------------------------------------------
    
    private static Logger log = Logger.getLogger(CmdGetRootCategories.class);
}
