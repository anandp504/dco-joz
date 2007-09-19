// get-providers command

/* The get-providers command has the following format.
 It takes no parameters and returns a list of provider id/name tuples.
 */

package com.tumri.joz.jozMain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.tumri.joz.products.Handle;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpString;
import com.tumri.utils.sexp.SexpSymbol;

public class CmdGetProviders extends CommandDeferWriting {
    
    public CmdGetProviders(Sexp e) {
        super(e);
    }
    
    public Sexp process() {
        Sexp e;
        
        try {
            HashSet<String> providers = new HashSet<String>();
            // FIXME: stubbed out until db support is ready
            Iterator<Handle> product_handles = new ArrayList<Handle>()
                    .iterator();
            
            while (product_handles.hasNext()) {
                /*
                 * Handle h = product_handles.next (); providers.add
                 * (p.get_merchant ());
                 */
            }
            
            SexpList l = new SexpList();
            
            for (String s : providers) {
                l.addLast(new SexpList(new SexpString(s), new SexpSymbol(s)));
            }
            
            e = l;
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
    
    private static Logger log = Logger.getLogger(CmdGetProviders.class);
}
