// Given an AdRequest, TSpec and Realm, fetch a set of products.

package com.tumri.joz.jozMain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.tumri.joz.products.*;
import com.tumri.joz.Query.*;
import com.tumri.joz.utils.Result;

public class SelectProducts
{
    public static List<SelectedProduct>
    select_products (AdDataRequest rqst, TSpec t_spec, Realm realm)
    {
	List<SelectedProduct> l = new ArrayList<SelectedProduct> ();

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

	log.info ("Running query for " + t_spec.get_name () + " ...");

	ProductDB pdb = ProductDB.getInstance ();
	Handle ref = pdb.genReference ();
	ConjunctQuery cjq = t_spec.get_query ().getQueries ().get (0);
	cjq.setStrict (true);
	cjq.setReference (ref);
	SortedSet<Result> results = cjq.exec ();

	int nr_results = results.size ();

	log.info ("Obtained " + nr_results + " products.");
	int i = 0;
	for (Result res : results)
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

	while (l.size () < n)
	{
	    for (Result res : results)
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

    // implementation details -------------------------------------------------

    static private Random random = new Random ();

    private static Logger log = Logger.getLogger (SelectProducts.class);
}
