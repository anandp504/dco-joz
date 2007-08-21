// Given an AdRequest, TSpec and Realm, fetch a set of products.

package com.tumri.joz.jozMain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.tumri.joz.products.*;
import com.tumri.joz.Query.*;
import com.tumri.joz.utils.Result;

public class SelectProducts
{
    public static List<SelectedProduct>
    select_products (AdDataRequest rqst, TSpec t_spec, Realm realm)
    {
	// FIXME: temp hack
	Integer num_products = rqst.get_num_products ();
	int n = num_products.intValue ();
	if (n < 12)
	    n = 12;
	if (n > 100)
	    n = 100;

/* temp hack, kept in for now
	for (int i = 0; i < n; ++i)
	{
	    int r = random.nextInt (100000);
	    MUPProductObj p = JozData.mup_db.get_entry (r);
	    l.add (new SelectedProduct (p));
	}
*/

	ProductDB pdb = ProductDB.getInstance ();
	Handle ref = pdb.genReference ();
	SortedSet<Handle> results = null;

	if (rqst.get_keywords () != null)
	{
	    String keywords = rqst.get_keywords ();
	    log.info ("Running query for keywords " + keywords + " ...");
	    KeywordQuery kq = new KeywordQuery (keywords);
	    results = kq.exec ();
	}
	else
	{
	    log.info ("Running query for " + t_spec.get_name () + " ...");
	    ConjunctQuery cjq = t_spec.get_query ().getQueries ().get (0);
	    cjq.setStrict (true);
	    cjq.setReference (ref);
	    results = cjq.exec ();
	}

	int nr_results = results.size ();

	log.info ("Obtained " + nr_results + " products.");
	int i = 0;
	for (Handle res : results)
	{
	    if (++i > 12)
	    {
		log.info ("Remaining products elided.");
		break;
	    }
	    int id = res.getOid ();
	    IProduct ip = pdb.get (id);
	    log.info (ip.getGId ());
	}

	List<SelectedProduct> l = new ArrayList<SelectedProduct> ();

	while (l.size () < n)
	{
	    for (Handle res : results)
	    {
		int id = res.getOid ();
		IProduct ip = pdb.get (id);
		MUPProductObj p = new MUPProductObj (ip);
		l.add (new SelectedProduct (p));
		if (l.size () == n)
		    break;
	    }
	}

	return l;
    }

    public static Iterator<SelectedProduct>
    get_products_for_tspec (TSpec t_spec)
    {
	ProductDB pdb = ProductDB.getInstance ();
	Handle ref = pdb.genReference ();
	SortedSet<Handle> results = null;

	log.info ("Running query for " + t_spec.get_name () + " ...");
	ConjunctQuery cjq = t_spec.get_query ().getQueries ().get (0);
	cjq.setStrict (true);
	cjq.setReference (ref);
	results = cjq.exec ();

	int nr_results = results.size ();

	log.info ("Obtained " + nr_results + " products.");

	List<SelectedProduct> l = new ArrayList<SelectedProduct> ();

	for (Handle res : results)
	{
	    int id = res.getOid ();
	    IProduct ip = pdb.get (id);
	    MUPProductObj p = new MUPProductObj (ip);
	    l.add (new SelectedProduct (p));
	}

	return l.iterator ();
    }

    // Wrapper class around Handle iterator so we can return SelectedProducts
    // instead of handles.

    private static class SelectedProductIterator implements Iterator<SelectedProduct>
    {
	private Iterator<Handle> _underlying_iterator;

	public SelectedProductIterator (Iterator<Handle> ui)
	{
	    _underlying_iterator = ui;
	}

	public boolean hasNext () { return _underlying_iterator.hasNext (); }

	public SelectedProduct next ()
	    throws NoSuchElementException
	{
	    Handle h = _underlying_iterator.next ();
	    ProductDB pdb = ProductDB.getInstance();
	    IProduct ip = pdb.get (h);
	    MUPProductObj p = new MUPProductObj (ip);
	    return new SelectedProduct (p);
	}

	public void
	remove ()
	{
	    throw new RuntimeException ("SelectedProductIterator.remove should never be called");
	}
    }

    // Return the entire mup.
    // The result is an iterator so that we don't have to construct a list
    // of the entire mup, callers want to iterate over the products anyway.
    // It also lets us hide the details of how and where we get the products.

    public static Iterator<SelectedProduct>
    get_entire_mup ()
    {
	ProductDB pdb = ProductDB.getInstance();
	Iterator<Handle> ih = pdb.getAllProducts ();
	return new SelectedProductIterator (ih);
    }

    // implementation details -------------------------------------------------

    static private Random random = new Random ();

    private static Logger log = Logger.getLogger (SelectProducts.class);
}
