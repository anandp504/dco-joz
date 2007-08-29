// get-counts command

/* The get-counts command has the following format:
   See https://secure.tumri.com/twiki/bin/view/Engineering/SoZandTC.

   (get-counts :t-spec-name 'symbol)

   If `symbol' is `nil' then process the entire MUP, otherwise process
   all products selected by the t-spec.  We count up the
   number of products in each category, the number of products in each
   brand and the number of products in each merchant.  Returns a 3-tuple:
   (category-counts brand-counts merchant-counts) where category counts
   is a list where cars are category strings and cadrs are counts.   Only
   non-zero counts are included.  Similarly, for the brand-counts, the car
   is the brand and for the merchant-counts the car is the merchant name.
*/

package com.tumri.joz.jozMain;

//import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.*;

import com.tumri.joz.products.*;
import com.tumri.joz.index.DictionaryManager;

public class CmdGetCounts extends CommandDeferWriting
{
    public CmdGetCounts (Sexp e)
    {
	super (e);
    }

    public Sexp
    process ()
    {
	Sexp e;

	try
	{
	    if (! expr.isSexpList ())
		throw new BadCommandException ("expecting (get-counts t-spec-name)");
	    SexpList l = expr.toSexpList ();
	    if (l.size () != 2)
		throw new BadCommandException ("expecting (get-counts t-spec-name)");
	    Sexp arg = l.get (1);
	    if (! arg.isSexpSymbol ())
		return SexpReader.readFromStringNoex ("(:error \"expected t-spec name\")");
	    SexpSymbol sym = arg.toSexpSymbol ();
	    e = get_counts (sym.toString ());
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

    private static Logger log = Logger.getLogger (CmdGetCounts.class);

    private class Counter
    {
	public int count;

	public Counter (int i) { count = i; }

	public void inc () { ++count; }

	public int get () { return count; }
    }

    // Return uniqified list of all categories in cats and their parents.

    private static List<String>
    get_all_categories (List<String> cats)
    {
	JOZTaxonomy tax = JOZTaxonomy.getInstance ();
	DictionaryManager dm = DictionaryManager.getInstance ();
	HashSet<Integer> cat_set = new HashSet<Integer> ();
	for (String c : cats)
	{
	    Integer id = dm.getId (IProduct.Attribute.kCategory, c);
	    cat_set.add (id);
	    Integer pid = id;
	    do {
		pid = tax.getParent (pid);
		if (pid != null)
		    cat_set.add (pid);
	    } while (pid != null);
	}
	List<String> result = new ArrayList<String> ();
	for (Integer i : cat_set)
	{
	    // FIXME: ignore root category
	    Object v = dm.getValue (IProduct.Attribute.kCategory, i.intValue ());
	    String s = (String) v;
	    result.add (s);
	}
	return result;
    }

    // See docs for the get-counts external API call for a description
    // of the format of the result.

    public Sexp
    get_counts (String tspec_name)
    {
	HashMap<String, Counter> category_counts =
	    new HashMap<String, Counter> ();
	HashMap<String, Counter> brand_counts =
	    new HashMap<String, Counter> ();
	HashMap<String, Counter> merchant_counts =
	    new HashMap<String, Counter> ();
	Iterator<SelectedProduct> products;
	JOZTaxonomy tax = JOZTaxonomy.getInstance ();

	if (! tspec_name.equals ("nil"))
	{
	    TSpec tspec = JozData.tspec_db.get (tspec_name);
	    if (tspec == null)
		throw new RuntimeException ("bad tspec name: " + tspec_name);
	    products = SelectProducts.get_products_for_tspec (tspec);
	}
	else // search entire mup
	{
	    products = SelectProducts.get_entire_mup ();
	}

	while (products.hasNext ())
	{
	    SelectedProduct p = products.next ();
	    Counter ctr;

	    List<String> parents = p.get_parents ();
	    List<String> categories = get_all_categories (parents);
	    for (String cat : categories)
	    {
		ctr = category_counts.get (cat);
		if (ctr == null)
		    category_counts.put (cat, new Counter (1));
		else
		    ctr.inc ();
	    }

	    String brand = p.get_brand ();
	    ctr = brand_counts.get (brand);
	    if (ctr == null)
		brand_counts.put (brand, new Counter (1));
	    else
		ctr.inc ();

	    String merchant = p.get_merchant ();
	    ctr = merchant_counts.get (merchant);
	    if (ctr == null)
		merchant_counts.put (merchant, new Counter (1));
	    else
		ctr.inc ();
	}

	SexpList category_list = new SexpList ();
	Set<Map.Entry<String, Counter>> cat_counts = category_counts.entrySet ();
	for (Map.Entry<String, Counter> count : cat_counts)
	{
	    SexpList l = new SexpList (new SexpString (count.getKey ()),
				       new SexpInteger (count.getValue ().get ()));
	    category_list.addLast (l);
	}

	SexpList brand_list = new SexpList ();
	Set<Map.Entry<String, Counter>> br_counts = brand_counts.entrySet ();
	for (Map.Entry<String, Counter> count : br_counts)
	{
	    SexpList l = new SexpList (new SexpString (count.getKey ()),
				       new SexpInteger (count.getValue ().get ()));
	    brand_list.addLast (l);
	}

	SexpList merchant_list = new SexpList ();
	Set<Map.Entry<String, Counter>> mer_counts = merchant_counts.entrySet ();
	for (Map.Entry<String, Counter> count : mer_counts)
	{
	    SexpList l = new SexpList (new SexpString (count.getKey ()),
				       new SexpInteger (count.getValue ().get ()));
	    merchant_list.addLast (l);
	}

	SexpList result = new SexpList ();
	result.addLast (category_list);
	result.addLast (brand_list);
	result.addLast (merchant_list);
	return result;
    }
}
