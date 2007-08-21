// The MUP database.
// FIXME: This is just scaffolding until the design is worked out.

package com.tumri.joz.jozMain;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.LineNumberReader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tumri.zini.transport.FASLType;
import com.tumri.zini.transport.FASLReader;

import com.tumri.utils.strings.EString;
import com.tumri.utils.sexp.*;
import com.tumri.utils.ifasl.IFASLUtils;

import com.tumri.joz.products.*;
import com.tumri.joz.index.DictionaryManager;

public class TmpMUPDB implements MUPDB
{
    TmpMUPDB (String ifasl_path,
	      String strings_path,
	      String taxonomy_path,
	      String default_realm_path)
	throws FileNotFoundException, IOException, BadMUPDataException
    {
	init (ifasl_path,
	      strings_path,
	      taxonomy_path,
	      default_realm_path);
    }

    public int
    get_count ()
    {
	return _count;
    }

    public MUPProductObj
    get_entry (int entry_nr)
    {
	return _product_db.get (entry_nr);
    }

    public MUPProductObj
    get_id (int id)
    {
	return null;
    }

    public Sexp
    get_default_realm_response ()
    {
	return _default_realm_data;
    }

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

    // implementation details -------------------------------------------------

    private int _count;
    private int _next_id;

    // Given an internal product id, return the product's name.
    ArrayList<String> _product_name_table;

    // Given a product's name, return its internal product id.
    HashMap<String, Integer> _product_name_map;

    // FIXME: wip
    HashMap<Integer, MUPProductObj> _product_db;

    // FIXME: wip
    ArrayList<MUPStringObj> _string_db;

    // FIXME: wip
    HashMap<String, MUPTaxonomyObj> _taxonomy_db;

    // FIXME: wip
    private Sexp _default_realm_data;

    private static Logger log = Logger.getLogger (TmpMUPDB.class);

    private void
    init (String ifasl_path,
	  String strings_path,
	  String taxonomy_path,
	  String default_realm_path)
	throws FileNotFoundException, IOException, BadMUPDataException
    {
	_count = 0;
	_next_id = 0;

	// The values chosen here are intended to allow sufficient slop
	// when the table grows to handle the current 200k products
	// so that it won't grow again for awhile.
	// PERF: tuning opportunities
	_product_name_table = new ArrayList<String> (120000);
	_product_name_map = new HashMap<String, Integer> (120000, 0.85f);

	log.info ("Loading MUP products from " + ifasl_path);
	load_products_from_ifasl_file (ifasl_path);

	log.info ("Loading MUP strings from " + strings_path);
	load_strings_from_ifasl_file (strings_path);

	log.info ("Loading MUP taxonomy from " + taxonomy_path);
	load_taxonomy_from_text_file (taxonomy_path);

	log.info ("Loading default realm data from " + default_realm_path);
	Sexp e = null;
	try
	{
	    e = SexpReader.readFromFile (default_realm_path);
	}
	catch (BadSexpException ex)
	{
	    throw new BadMUPDataException ("error reading sexp");
	}
	_default_realm_data = e;
    }

    @SuppressWarnings("unchecked")
    private void
    load_products_from_ifasl_file (String file)
	throws FileNotFoundException, IOException, BadMUPDataException
    {
	_product_db = new HashMap<Integer, MUPProductObj> ();

	FileInputStream in = new FileInputStream (file);

	try
	{
	    FASLReader fr = new FASLReader (in);
	    fr.setReadKeywordsAsKeywords (true);
	    FASLType t;
	    int count = 0;
	    Runtime runtime = Runtime.getRuntime ();
	    runtime.gc ();
	    log.info ("Initially " + runtime.freeMemory () + " free.");

	    while ((t = fr.read ()) != null)
	    {
		if (t.type () != FASLType.list)
		{
		    // ??? First sexp is apparently an empty string.  Why?
		    String s = t.toString ();
		    if (! s.equals (""))
			log.info ("Ignoring `" + t.toString () + "'");
		    continue;
		}

		// FIXME: This generates an "unchecked" warning, but methinks
		// the fix belongs in the zini code.  I'd rather not cast if I
		// don't have to.
		Iterator<FASLType> iter = t.iterator ();

		MUPProductObj obj = null;
		try
		{
		    obj = new MUPProductObj (t, iter);
		}
		catch (Exception e)
		{
		    log.error ("Bad MUP entry: " + e.toString ());
		    continue;
		}

		String pname = obj.get_guid ();
		int internal_id = lookup_or_alloc_product (pname);
		// FIXME: wip
		_product_db.put (new Integer (internal_id), obj);

		++count;
		if (count % 10000 == 0)
		    log.info ("Loaded " + count + " entries, "
			      + runtime.freeMemory () + " free ...");
	    }

	    if (count > 0)
	    {
		log.info ("Loaded " + count + " entries, "
			  + runtime.freeMemory () + " free.");
		runtime.gc ();
		log.info ("After gc " + runtime.freeMemory () + " free.");
		log.info ("NOTE: these values are approximate!");
	    }
	}
	finally
	{
	    in.close ();
	}
    }

    @SuppressWarnings("unchecked")
    private void
    load_strings_from_ifasl_file (String file)
	throws FileNotFoundException, IOException, BadMUPDataException
    {
	_string_db = new ArrayList<MUPStringObj> (10000);

	FileInputStream in = new FileInputStream (file);

	try
	{
	    FASLReader fr = new FASLReader (in);
	    fr.setReadKeywordsAsKeywords (true);
	    FASLType t;
	    int count = 0;
	    Runtime runtime = Runtime.getRuntime ();
	    runtime.gc ();
	    log.info ("Initially " + runtime.freeMemory () + " free.");

	    while ((t = fr.read ()) != null)
	    {
		if (t.type () != FASLType.list)
		{
		    // ??? First sexp is apparently an empty string.  Why?
		    String s = t.toString ();
		    if (! s.equals (""))
			log.info ("Ignoring `" + t.toString () + "'");
		    continue;
		}

		// FIXME: This generates an "unchecked" warning, but methinks
		// the fix belongs in the zini code.  I'd rather not cast if I
		// don't have to.
		Iterator<FASLType> iter = t.iterator ();
		FASLType name = iter.next ();
		if (name.type () != FASLType.zini_symbol)
		    throw new BadMUPDataException ("bad string entry: " + t.toString ());
		String pname = ifasl_to_product_name (name);

		MUPStringObj obj = new MUPStringObj (t, iter);

		int internal_id = lookup_or_alloc_product (pname);
		// FIXME: wip
		//_string_db.add (obj);

		++count;
		if (count % 10000 == 0)
		    log.info ("Loaded " + count + " entries, "
			      + runtime.freeMemory () + " free ...");
	    }

	    if (count > 0)
	    {
		log.info ("Loaded " + count + " entries, "
			  + runtime.freeMemory () + " free.");
/* PERF:
		_product_name_table.trimToSize ();
*/
		runtime.gc ();
		log.info ("After gc " + runtime.freeMemory () + " free.");
		log.info ("NOTE: these values are approximate!");
	    }
	}
	finally
	{
	    in.close ();
	}
    }

    private void
    load_taxonomy_from_text_file (String file)
	throws FileNotFoundException, IOException, BadMUPDataException
    {
	_taxonomy_db = new HashMap<String, MUPTaxonomyObj> ();

	FileReader fr = new FileReader (file);

	try
	{
	    LineNumberReader rdr = new LineNumberReader (fr);
	    String s;

	    while ((s = rdr.readLine ()) != null)
	    {
		// TODO: would be nice to allow comments in the file

		MUPTaxonomyObj obj = new MUPTaxonomyObj (s, '\t');
		_taxonomy_db.put (obj.get_id (), obj);
	    }
	}
	finally
	{
	    fr.close ();
	}
    }

    private String
    ifasl_to_product_name (FASLType t)
    {
	switch (t.type ())
	{
	case FASLType.zini_symbol:
	case FASLType.string:
	    return t.toString ();
	default:
	    assert (false);
	    return null;
	}
    }

    private int
    lookup_or_alloc_product (String name)
    {
	Integer entry = _product_name_map.get (name);
	if (entry != null)
	    return entry.intValue ();
	int id = _next_id++;
	// FIXME: wip
	_product_name_table.add (name);
	_product_name_map.put (name, new Integer (id));
	return id;
    }
}
