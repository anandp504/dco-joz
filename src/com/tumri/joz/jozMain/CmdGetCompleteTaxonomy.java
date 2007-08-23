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

//import java.io.PrintWriter;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.*;

import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.joz.products.IProduct;

public class CmdGetCompleteTaxonomy extends CommandDeferWriting
{
    public CmdGetCompleteTaxonomy (Sexp e)
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
	    e = get_taxonomy (dm, tax, tax.getRoot ());
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

    private static Logger log = Logger.getLogger (CmdGetCompleteTaxonomy.class);

    // Return the taxonomy tree for {category_id} and its children.

    private static SexpList
    get_taxonomy (DictionaryManager dm, JOZTaxonomy tax, Integer category_id)
    {
	Object v = dm.getValue (IProduct.Attribute.kCategory,
				category_id.intValue ());
	String name = (String) v;
	// FIXME: This should be (GLASSVIEW.TUMRI_nnnnn "pretty name")
	// but the current taxonomy database doesn't record both.
	SexpList this_nodes_name =
	    new SexpList (new SexpSymbol (name),
			  new SexpString (name));

	SexpList result = new SexpList ();
	result.addLast (this_nodes_name);

	SexpList children = new SexpList ();
	TreeSet<Integer> children_ids = tax.getChildren (category_id);

	for (Integer child_id : children_ids)
	{
	    children.addLast (get_taxonomy (dm, tax, child_id));
	}

	result.addLast (children);
	return result;
    }
}
