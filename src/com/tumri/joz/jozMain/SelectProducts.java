// Given an AdRequest, TSpec and Realm, fetch a set of products.

package com.tumri.joz.jozMain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

	for (int i = 0; i < n; ++i)
	{
	    int r = random.nextInt (100000);
	    MUPProductObj p = JozData.mup_db.get_entry (r);
	    l.add (new SelectedProduct (p));
	}

	return l;
    }

    // implementation details -------------------------------------------------

    static private Random random = new Random ();
}
