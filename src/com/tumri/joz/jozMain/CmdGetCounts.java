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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.tumri.content.data.Category;
import com.tumri.content.data.Taxonomy;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpInteger;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpString;
import com.tumri.utils.sexp.SexpSymbol;

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
	    // Convert {ex} to SexpString first so we can use its toString()
	    // method to escape "s.
	    SexpString ex_string = new SexpString (ex.toString ());
	    e = SexpReader.readFromStringNoex ("(:error "
					       + ex_string.toString ()
					       + ")");
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
	Taxonomy t = tax.getTaxonomy();
	List<String> result = new ArrayList<String>();
	HashSet<Integer> idSet = new HashSet<Integer>();
	for (String c: cats) {
	    result.add(c);
	    Category c1 = t.getCategory(c);
        idSet.add(c1.getGlassId());
	    Category p = null;
	    do {
	        p = c1.getParent();
	        if (idSet.contains(p.getGlassId())) {
	            break;
	        }
	        result.add(c1.getParent().getGlassIdStr());
	    } while (p != null);
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
	Iterator<Handle> product_handles;
	JOZTaxonomy tax = JOZTaxonomy.getInstance ();

	if (! tspec_name.equals ("nil"))
	{
//	    TSpec tspec = CampaignDB.getInstance().getOspec(tspec_name);
//	    if (tspec == null)
//		throw new RuntimeException ("bad tspec name: " + tspec_name);
	    // FIXME: stubbed out for now
	    product_handles = new ArrayList<Handle> ().iterator ();
	}
	else // search entire mup
	{
	    // FIXME: stubbed out until db support is ready
	    product_handles = new ArrayList<Handle> ().iterator ();
	}

/*
	while (product_handles.hasNext ())
	{
	    Handle h = product_handles.next ();
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
*/

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
