// get-complete-taxonomy command

/* The get-complete-taxonomy command has the following format.
 It takes no parameters and returns the taxonomy formatted as:

 ((root "root_name")
 (((child1 "child1_name")
 (((grandchild11 "grandchild11_name") NIL)
 ((grandchild12 "grandchild12_name") NIL)))
 ((child2 "child2_name")
 (((grandchild21 "grandchild21_name") NIL)
 ((grandchild22 "grandchild22_name") NIL)))))

 i.e. a recursive data structure where each element is

 ((name "pretty_name") (child1-spec child2-spec ...))
 */

package com.tumri.joz.jozMain;

// import java.io.PrintWriter;
import org.apache.log4j.Logger;

import com.tumri.content.data.Category;
import com.tumri.content.data.Taxonomy;
import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpString;
import com.tumri.utils.sexp.SexpSymbol;

public class CmdGetCompleteTaxonomy extends CommandDeferWriting {
    
    public CmdGetCompleteTaxonomy(Sexp e) {
        super(e);
    }
    
    public Sexp process() {
        Sexp e;
        
        try {
            DictionaryManager dm = DictionaryManager.getInstance();
            JOZTaxonomy tax = JOZTaxonomy.getInstance();
            Taxonomy t = tax.getTaxonomy();
            e = get_taxonomy(dm, t, t.getRootCategory());
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
    
    private static Logger log = Logger.getLogger(CmdGetCompleteTaxonomy.class);
    
    // Return the taxonomy tree for {category_id} and its children.
    
    private static SexpList get_taxonomy(DictionaryManager dm, Taxonomy tax,
            Category category) {
        // This id is the GlassView.... pretty name.
        String glassIdStr = category.getGlassIdStr();
        String name = category.getName();
        SexpList this_nodes_name = new SexpList(new SexpSymbol(glassIdStr),
                new SexpString(name));
        
        SexpList result = new SexpList();
        result.addLast(this_nodes_name);
        
        SexpList children = new SexpList();
        Category[] childrens = category.getChildren();
        
        if (childrens != null) {
            for (Category child_id : childrens) {
                children.addLast(get_taxonomy(dm, tax, child_id));
            }
        }
        
        result.addLast(children);
        return result;
    }
}
