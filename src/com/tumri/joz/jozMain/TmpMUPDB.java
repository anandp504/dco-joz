// The MUP database.
// FIXME: This is just scaffolding until the design is worked out.

package com.tumri.joz.jozMain;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.LineNumberReader;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.tumri.zini.transport.FASLType;
import com.tumri.zini.transport.FASLReader;

import com.tumri.utils.strings.EString;
import com.tumri.utils.strings.ProductName;
import com.tumri.utils.sexp.*;
import com.tumri.utils.ifasl.IFASLUtils;

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
	return null;
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

    // See docs for the get-counts external API call for a description
    // of the format of the result.

    public Sexp
    get_counts (EString tspec)
    {
	HashMap<EString, Integer> category_counts =
	    new HashMap<EString, Integer> ();
	HashMap<EString, Integer> brand_counts =
	    new HashMap<EString, Integer> ();
	HashMap<EString, Integer> merchant_counts =
	    new HashMap<EString, Integer> ();

	SexpList category_list = new SexpList ();

	SexpList brand_list = new SexpList ();

	SexpList merchant_list = new SexpList ();

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
    ArrayList<ProductName> _product_name_table;

    // Given a product's name, return its internal product id.
    HashMap<ProductName, Integer> _product_name_map;

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
	_product_name_table = new ArrayList<ProductName> (120000);
	_product_name_map = new HashMap<ProductName, Integer> (120000, 0.85f);

	log.info ("Loading MUP products from " + ifasl_path);
	load_products_from_ifasl_file (ifasl_path);

	log.info ("Loading MUP strings from " + strings_path);
	load_strings_from_ifasl_file (strings_path);

	log.info ("Loading MUP taxonomy from " + taxonomy_path);
	load_taxonomy_from_text_file (taxonomy_path);

	log.info ("Loading default realm data from " + default_realm_path);
	Sexp e = load_sexp_from_file (default_realm_path);
	_default_realm_data = e;
    }

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

		// FIXME: This generates a warning, but methinks the fix
		// belongs in the zini code.  I'd rather not cast if I don't
		// have to.
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

		ProductName pname = obj.get_guid ();
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

		// FIXME: This generates a warning, but methinks the fix
		// belongs in the zini code.  I'd rather not cast if I don't
		// have to.
		Iterator<FASLType> iter = t.iterator ();
		FASLType name = iter.next ();
		if (name.type () != FASLType.zini_symbol)
		    throw new BadMUPDataException ("bad string entry: " + t.toString ());
		ProductName pname = ifasl_to_product_name (name);

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

    private static Sexp
    load_sexp_from_file (String path)
    {
	Sexp expr = null;

	try
	{
	    expr = SexpReader.readFromFile (path);

	    if (expr == null)
		log.info ("empty attributes and metadata file"); // FIXME
	}
	catch (FileNotFoundException e)
	{
	    log.info (e.toString ()); // FIXME
	}
	catch (IOException e)
	{
	    log.info (e.toString ()); // FIXME
	}
	catch (BadSexpException e)
	{
	    log.info (e.toString ()); // FIXME
	}

	return expr;
    }

    private ProductName
    ifasl_to_product_name (FASLType t)
    {
	switch (t.type ())
	{
	case FASLType.zini_symbol:
	    if (IFASLUtils.isUnsigned16Symbol (t))
		return new ProductName (Sexp.ENCODE_UCS8OR16BE,
					t.getBytes ());
	    else
		return new ProductName (Sexp.ENCODE_UCS8, t.getBytes ());
	case FASLType.string:
	    return new ProductName (Sexp.ENCODE_UCS8, t.getBytes ());
	default:
	    assert (false);
	    return null;
	}
    }

    private int
    lookup_or_alloc_product (ProductName name)
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
