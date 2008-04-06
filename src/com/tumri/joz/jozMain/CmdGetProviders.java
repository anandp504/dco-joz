// get-providers command

/* The get-providers command has the following format.
 It takes no parameters and returns a list of provider id/name tuples.
 */

package com.tumri.joz.jozMain;

import java.util.Set;

import org.apache.log4j.Logger;

import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpString;
import com.tumri.utils.sexp.SexpSymbol;
import com.tumri.content.data.dictionary.DictionaryManager;

public class CmdGetProviders extends CommandDeferWriting {
    
    private static Logger log = Logger.getLogger(CmdGetProviders.class);

    public CmdGetProviders(Sexp s) {
        super(s);
    }
    
    @SuppressWarnings("unchecked")
    public Sexp process() {
        try {
            SexpList retVal = new SexpList();
            ProductAttributeIndex<Integer,Handle> pai = ProductDB.getInstance().getIndex(IProduct.Attribute.kProvider);
            if (pai != null) {
                Set<Integer> providers = pai.getKeys();
                String providerStr = null;
                for (Integer provider: providers) {
                    providerStr = (String) DictionaryManager.getInstance().getValue(IProduct.Attribute.kProvider, provider);
                    if (providerStr != null) {
                        retVal.addLast(new SexpList(new SexpSymbol(providerStr), new SexpString(providerStr)));
                    }
                }
                
            }
            
            return retVal;
            
            /*
            HashSet<String> providers = new HashSet<String>();
            // FIXME: stubbed out until db support is ready
            Iterator<Handle> product_handles = new ArrayList<Handle>()
                    .iterator();
            
            
            while (product_handles.hasNext()) {
                
                 // Handle h = product_handles.next (); providers.add
                 // (p.get_merchant ());
                 
            }
            
            
            for (String s : providers) {
                l.addLast(new SexpList(new SexpString(s), new SexpSymbol(s)));
            }
            */
            
        } catch (Exception ex) {
            log.error("Error while fetching list of providers. Request:\"" + toString() + "\".",ex);
            return returnError(ex);
        }
    }
    
}
