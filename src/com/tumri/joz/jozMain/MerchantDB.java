// The merchant database.
// FIXME: wip
// TODO: OMG, OMG, this is all wrong.  Chill dude.  As things evolve this
// will get rewritten to be The Right Way.

package com.tumri.joz.jozMain;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.tumri.utils.strings.EString;
import com.tumri.utils.sexp.*;

public class MerchantDB
{
    MerchantDB (String attributes_and_metadata_path,
		String tabulated_search_results_path)
	throws FileNotFoundException, IOException, BadMerchantDataException
    {
	init (attributes_and_metadata_path,
	      tabulated_search_results_path);
    }

    public Sexp
    get_attributes_and_metadata ()
    {
	return _attributes_and_metadata;
    }

    public SexpList
    get_tabulated_search_results ()
    {
	return _tabulated_search_results;
    }

    // Return the logo url for {merchant} or null if not provided.

    public String
    get_logo_url (EString merchant)
    {
	return _logo_url_db.get (merchant.toString ().toLowerCase ());
    }

    // Return the shipping promo for {merchant} or null if not provided.
    // An example of a shipping promo is, I think,
    // "Free Shipping on Orders over $50".

    public String
    get_shipping_promo (EString merchant)
    {
	return _shipping_promo_db.get (merchant.toString ().toLowerCase ());
    }

    // implementation details -------------------------------------------------

    // Content of soz's inputs/MD/attributes-and-metadata.lisp.
    private Sexp _attributes_and_metadata = null;

    // Content of soz's inputs/MD/attributes-and-metadata.lisp.
    private SexpList _tabulated_search_results = null;

    private HashMap<String, String> _logo_url_db = null;

    private HashMap<String, String> _shipping_promo_db = null;

    private static Logger log = Logger.getLogger (MerchantDB.class);

    private void
    init (String attributes_and_metadata_path,
	  String tabulated_search_results_path)
	throws FileNotFoundException, IOException, BadMerchantDataException
    {
	log.info ("Loading merchant data from "
		  + attributes_and_metadata_path);

	Sexp e = null;
	try
	{
	    e = SexpReader.readFromFile (attributes_and_metadata_path);
	}
	catch (BadSexpException ex)
	{
	    throw new BadMerchantDataException ("error reading sexp");
	}
	_attributes_and_metadata = e;

	log.info ("Loading tabulated search results from "
		  + tabulated_search_results_path);

	e = null;
	try
	{
	    e = SexpReader.readFromFile (tabulated_search_results_path);
	}
	catch (BadSexpException ex)
	{
	    throw new BadMerchantDataException ("error reading sexp");
	}
	if (! e.isSexpList ())
	    throw new BadMerchantDataException ("tabulated-search-results is not a list");
	SexpList l = e.toSexpList ();
	if (l.size () != 4)
	    throw new BadMerchantDataException ("tabulated-search-results is not a list of four elements");
	_tabulated_search_results = l;

	extract_merchant_logos_and_shipping_promos ();
    }

    private void
    extract_merchant_logos_and_shipping_promos ()
	throws BadMerchantDataException
    {
	// tabulated-search-results is a list of four elements:
	// ((# # #) nil (column-name-list) (merchant-info-list))

	SexpList tsr = _tabulated_search_results;
	Sexp e;
	SexpList col_names;
	SexpList data;

	_logo_url_db = new HashMap<String, String> ();
	_shipping_promo_db = new HashMap<String, String> ();

	e = tsr.get (2);
	if (! e.isSexpList ())
	    throw new BadMerchantDataException ("t-s-r column names not a list");
	col_names = e.toSexpList ();

	e = tsr.get (3);
	if (! e.isSexpList ())
	    throw new BadMerchantDataException ("t-s-r merchant info not a list");
	data = e.toSexpList ();

	int logo_url_idx = get_position (col_names, "Logo URL");
	int promo_idx = get_position (col_names, "Shipping Promotion");

	Iterator<Sexp> iter = data.iterator ();
	while (iter.hasNext ())
	{
	    e = iter.next ();
	    if (! e.isSexpList ())
		throw new BadMerchantDataException ("t-s-r merchant entry is not a list");
	    SexpList l = e.toSexpList ();
	    if (l.size () == 0)
		continue; // ignore empty entries

	    Sexp id = l.get (0);
	    if (! id.isSexpSymbol ())
		throw new BadMerchantDataException ("t-s-r merchant id not a symbol: " + id.toString ());
	    String idstr = id.toString ().toLowerCase ();

	    String logo_url = get_caar_string (l, logo_url_idx, "logo url");
	    if (logo_url != null)
		_logo_url_db.put (idstr, logo_url); // FIXME: collisions?

	    String promo = get_caar_string (l, promo_idx, "shipping promo");
	    if (promo != null)
		_shipping_promo_db.put (idstr, promo); // FIXME: collisions?
	}
    }

    // Return the position of {s} in {l} or -1 if not present.

    private static int
    get_position (SexpList l, String s)
    {
	Iterator<Sexp> iter = l.iterator ();
	int i = 0;

	while (iter.hasNext ())
	{
	    Sexp n = iter.next ();
	    if (! n.isSexpString ())
		continue;
	    if (n.toSexpString ().toStringValue ().equals (s))
		return i;
	    ++i;
	}

	return -1;
    }

    // Fetch element {n} from list {l}, and if it is (("string1" ...)) then
    // return string1; otherwise return null.
    // {what} is used in error text.
    // Returns null if list is too small or the string is not present.

    private static String
    get_caar_string (SexpList l, int n, String what)
    {
	if (l.size () <= n)
	    return null;
	Sexp e = l.get (n);
	if (! e.isSexpList ())
	    return null;
	l = e.toSexpList ();
	if (l.size () == 0)
	    return null;
	e = l.get (0);
	if (! e.isSexpList ())
	    return null;
	l = e.toSexpList ();
	if (l.size () == 0)
	    return null;
	e = l.get (0);
	if (! e.isSexpString ())
	    log.error ("Bad merchant data, expected string for " + what
		       + ", got: " + e.toString ());
	return e.toStringValue ();
    }
}
