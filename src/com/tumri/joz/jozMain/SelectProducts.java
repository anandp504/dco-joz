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

	for (int i = 0; i < 6; ++i)
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
