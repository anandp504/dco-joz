// get-ad-data command
// TODO: OMG, OMG, this is all wrong.  Chill dude.  As things evolve this
// will get rewritten to be The Right Way.

/*
   See https://secure.tumri.com/twiki/bin/view/Engineering/JozPublicAPI
   for the format of get-ad-data requests.

get-ad-data steps
-----------------
- if given seed, restore random state
- begin timing
- process arguments, converting nil's to a usable value, etc.
- split arguments into two pieces: psp, h-p
  (product selection parameters, http payload)
- assign t-spec, realm
  - if t-spec given, use it, otherwise choose one with
    mux-choose-t-spec
  - if t-spec given,
      if url given, realm = mux-choose-best-realm-for-uri,
        or default realm if that fails,
      otherwise realm = t-spec
    otherwise realm = mux-choose-t-spec
- if t-spec is non-nil,
    - (products num-prods t-spec-name realm) = (ad-request ...)
    - send result back to client
  otherwise error "null t-spec, most likely no default realm"

*/

package com.tumri.joz.jozMain;

//import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.*;
import com.tumri.utils.strings.EString;
import com.tumri.utils.strings.RFC1630Encoder;

public class CmdGetAdData extends CommandOwnWriting
{
    public CmdGetAdData (Sexp e)
    {
	super (e);
    }

    public void
    process_and_write (OutputStream out)
    {
	try
	{
/*
	    throw new Exception ("temp hack, send default realm only");
*/
	    AdDataRequest rqst = new AdDataRequest (expr);
	    choose_and_write_products (rqst, out);
	}
	catch (IOException e)
	{
	    // Blech, may be in middle of transmission.
	    // FIXME: What to do?  drop the connection and rely on client
	    // to detect dropped connection as an error indicator and
	    // reconnect?
	    log.error (e);
	}
	catch (Exception e)
	{
	    log.error (e);
	    // FIXME: What to do?  We should be written such that we cannot
	    // get here in the middle of transmitting a response.  Sending the
	    // default realm may be reasonable except that there are other
	    // failure modes for which we might also want to show the default
	    // realm; it would be preferable to handle as many of them as
	    // possible in one place, and it's not yet clear this is that
	    // place.
/*
	    Sexp sexp = JozData.mup_db.get_default_realm_response ();
	    SexpIFASLWriter.writeOne (out, sexp);
*/
	}
    }

    // implementation details -------------------------------------------------

    private static Logger log = Logger.getLogger (CmdGetAdData.class);

    private static int dump_seq = 0;

    // Main entry point to product selection once the request has been read
    // and parsed.

    private void
    choose_and_write_products (AdDataRequest rqst, OutputStream out)
	throws IOException, Exception
    {
	TSpecAndRealm tsar = choose_t_spec_and_realm (rqst);
	TSpec t_spec = tsar._t_spec;
	Realm realm = tsar._realm;
	Integer seed = rqst.get_seed (); // FIXME: wip
	if (seed == null)
	    seed = new Integer (42); // FIXME: wip
	Features features = new Features (seed);
	boolean private_label_p = t_spec.private_label_p ();

	// This does the real work of selecting a set of products.
	long start_time = System.nanoTime ();
	List<SelectedProduct> products =
	    SelectProducts.select_products (rqst, t_spec, realm);
	long end_time = System.nanoTime ();
	long elapsed_time = end_time - start_time;

	// Send the result back to the client.
	write_result (rqst, t_spec, realm,
		      private_label_p, features, elapsed_time,
		      products,
		      out);

	// Log the result for debugging.
	String dump_file = "/tmp/soz3-" + dump_seq + ".dump";
	++dump_seq;
	try
	{
	    FileOutputStream f = new FileOutputStream (dump_file);
	    write_result (rqst, t_spec, realm,
			  private_label_p, features, elapsed_time,
			  products,
			  f);
	}
	catch (Exception e)
	{
	    log.error ("Unable to dump result: " + e.toString ());
	}
    }

    // If a t-spec is provided, use it.
    // Otherwise pick one based on the parameters, trying these until
    // a suitable t-spec is found: store-id, theme, url.
    // If that fails use the default realm as the t-spec.
    //
    // The realm returned is the realm used to map into the t-spec, if we
    // choose the t-spec based on a URL.  Otherwise ???.

    private TSpecAndRealm
    choose_t_spec_and_realm (AdDataRequest rqst)
    {
	String t_spec_name; // not initialized on purpose
	Realm realm; // not initialized on purpose
	String store_id = null;
	String theme = null;
	JozURI uri; // not initialized on purpose

	String t_spec_param = rqst.get_t_spec ();

	if (t_spec_param != null) // FIXME: todo: (and name-is-t-spec-p)
	{
	    t_spec_name = t_spec_param;
	    String url = rqst.get_url ();
	    if (url != null)
	    {
		realm = choose_best_realm_for_uri (JozURI.build_lax_uri (url));
		if (realm == null)
		    realm = new Realm (JozURI.build_lax_uri (JozData.tspec_db.get_default_realm_url ()));
	    }
	    else
	    {
		realm = new Realm (t_spec_name);
	    }
	}
	else if ((store_id = rqst.get_store_id ()) != null
		 && (t_spec_name = choose_t_spec_for_store_id (store_id)) != null)
	{
	    // got it
	    realm = new Realm (store_id);
	}
	else if ((theme = rqst.get_theme ()) != null
		 && (t_spec_name = choose_t_spec_for_theme (theme)) != null)
	{
	    // got it
	    realm = new Realm (theme);
	}
	// NOTE: We're picking up {theme} set in the previous test.
	else if (theme != null
		 && (uri = JozURI.build_lax_uri (theme)) != null
		 && (t_spec_name = choose_t_spec_for_uri (uri)) != null)
	{
	    // got it
	    realm = new Realm (uri);
	}
	else if (rqst.get_url () != null
		 && (uri = JozURI.build_lax_uri (rqst.get_url ())) != null
		 && (t_spec_name = choose_t_spec_for_uri (uri)) != null)
	{
	    // got it
	    realm = new Realm (uri);
	}
	else
	{
	    String default_realm_url = JozData.tspec_db.get_default_realm_url ();
	    JozURI default_realm_uri = JozURI.build_lax_uri (default_realm_url);
	    t_spec_name = choose_t_spec_for_realm (default_realm_uri);
	    realm = new Realm (default_realm_uri);
	}

	TSpec t_spec = JozData.tspec_db.get (t_spec_name);
	TSpecAndRealm tasr = new TSpecAndRealm (t_spec, realm);
	return tasr;
    }

    private Realm
    choose_best_realm_for_uri (JozURI uri)
    {
	// FIXME: for now
	MappingObjList mol = JozData.mapping_db.get_url_t_specs (uri);
	if (mol == null || mol.size () == 0)
	    return null;
	List<MappingObj> lmo = mol.get_list ();
	MappingObj mo = lmo.get (0);
	return new Realm (mo.get_t_spec ());
    }

    // If there are multiple strategies for this theme then pick one randomly.
    // If nothing matches, return nil.  Returns the NAME of the t-spec, not the
    // t-spec itself.

    private String
    choose_t_spec_for_store_id (String store_id)
    {
	// FIXME: for now
	MappingObjList mol = JozData.mapping_db.get_store_id_t_specs (store_id);
	if (mol == null || mol.size () == 0)
	    return null;
	List<MappingObj> lmo = mol.get_list ();
	MappingObj mo = lmo.get (0);
	return mo.get_t_spec ();
    }

    private String
    choose_t_spec_for_theme (String theme)
    {
	// FIXME: for now
	MappingObjList mol = JozData.mapping_db.get_theme_t_specs (theme);
	if (mol == null || mol.size () == 0)
	    return null;
	List<MappingObj> lmo = mol.get_list ();
	MappingObj mo = lmo.get (0);
	return mo.get_t_spec ();
    }

    private String
    choose_t_spec_for_uri (JozURI uri)
    {
	// FIXME: for now
	MappingObjList mol = JozData.mapping_db.get_url_t_specs (uri);
	if (mol == null || mol.size () == 0)
	    return null;
	List<MappingObj> lmo = mol.get_list ();
	MappingObj mo = lmo.get (0);
	return mo.get_t_spec ();
    }

    private String
    choose_t_spec_for_realm (JozURI realm_uri)
    {
	// FIXME: for now
	MappingObjList mol = JozData.mapping_db.get_url_t_specs (realm_uri);
	if (mol == null || mol.size () == 0)
	    return null;
	List<MappingObj> lmo = mol.get_list ();
	MappingObj mo = lmo.get (0);
	return mo.get_t_spec ();
    }

    // Write the chosen product list back to the client.
    // The format is:
    //
    // (
    //  ("VERSION" "1.0")
    //  ("PRODUCTS" product-list)
    //  ("PROD-IDS" product-id-list)
    //  ("CATEGORIES" category-list)
    //  ("CAT-NAMES" category-name-list)
    //  ("REALM" realm)
    //  ("STRATEGY" t-spec-name)
    //  ("IS-PRIVATE-LABEL-P" is-private-label-p)
    //  ("SOZFEATURES" feature-list-or-nil)
    // )
    //
    // FIXME: version number needs to be spec'd to be first, remainder should
    // allow for optional parameters, (consider precedent of IS-PRIVATE-LABEL-P
    // and what happens over time as more are added).
    //
    // NOTE: In SoZ this is the "js-friendly" format, js for JSON
    // http://www.json.org.

    private void
    write_result (AdDataRequest rqst,
		  TSpec t_spec, Realm realm,
		  boolean private_label_p,
		  Features features,
		  long elapsed_time,
		  List<SelectedProduct> products,
		  OutputStream out)
	throws IOException, Exception
    {
	SexpIFASLWriter w = new SexpIFASLWriter (out);

	w.writeVersion ();

	w.startDocument ();

	// See above, 9 elements in result list.
	w.startList (9);

	write_elm (w, "VERSION", new SexpString ("1.0"));

	// This is a big part of the result, write directly.
	w.startList (2);
	w.writeString8 ("PRODUCTS");
	write_products (w, products);
	w.endList ();

	String product_ids = products_to_id_list (products);
	write_elm (w, "PROD-IDS", product_ids);

	List<String> cat_list = products_to_cat_list (products);
	String categories = cat_list_to_result_categories (cat_list);
	write_elm (w, "CATEGORIES", categories);
	String cat_names = cat_list_to_result_cat_names (cat_list);
	write_elm (w, "CAT-NAMES", cat_names);

	write_elm (w, "REALM", realm.toString ());

	write_elm (w, "STRATEGY", t_spec.get_name ());

	write_elm (w, "IS-PRIVATE-LABEL-P", (private_label_p ? "T" : "NIL"));

	SexpList sexp_features = features.toSexpList (elapsed_time);
	write_elm (w, "SOZFEATURES", sexp_features);

	w.endList ();

	w.endDocument ();
    }

    // Write the list of selected products to {out}.
    // This is the biggest part of the result of get-ad-data, so we write
    // each product out individually instead of building an object describing
    // all of them and then write that out.
    // Things are complicated because the value that is written is a single
    // string containing all the products.

    private void
    write_products (SexpIFASLWriter w, List<SelectedProduct> products)
	throws IOException
    {
	Iterator<SelectedProduct> iter = products.iterator ();
	StringBuilder b = new StringBuilder ();

	// Ahh!!!  The IFASL format requires a leading length of the
	// string.  That means we pretty much have to build the entire string
	// of all products' data before we can send it.

	b.append ("[");

	boolean done1 = false;
	while (iter.hasNext ())
	{
	    if (done1)
		b.append (",");
	    SelectedProduct p = iter.next ();
	    b.append (p.toAdDataResultString ());
	    done1 = true;
	}

	b.append ("]");

	String s = b.toString ();

	// Don't construct huge string unnecessarily.
	if (log.isDebugEnabled ())
	    log.debug ("Product string: " + s);

	w.writeString8 (s);
    }

    // Write an element of the result.

    private void
    write_elm (SexpIFASLWriter w, String name, Sexp sexp)
	throws IOException, Exception
    {
	// Don't construct string unnecessarily.
	if (log.isDebugEnabled ())
	    log.debug ("Writing " + name + ": " + sexp.toString ());

	w.startList (2);
	w.writeString8 (name);
	w.visit (sexp);
	w.endList ();
    }

    private void
    write_elm (SexpIFASLWriter w, String name, String s)
	throws IOException
    {
	// Don't construct string unnecessarily.
	if (log.isDebugEnabled ())
	    log.debug ("Writing " + name + ": " + s);

	w.startList (2);
	w.writeString8 (name);
	// FIXME: assumes ASCII
	w.writeString8 (s);
	w.endList ();
    }

    private static String
    products_to_id_list (List<SelectedProduct> products)
    {
	StringBuilder b = new StringBuilder ();
	boolean done_one = false;

	for (SelectedProduct sp : products)
	{
	    if (done_one)
		b.append (",");
	    String id = sp.get_product_id ();
	    b.append (id);
	    done_one = true;
	}

	return b.toString ();
    }

    // Return uniqified list of all categories in {products}.

    private static List<String>
    products_to_cat_list (List<SelectedProduct> products)
    {
	HashSet<String> categories = new HashSet<String> ();

	for (SelectedProduct sp : products)
	{
	    List<String> parents = sp.get_parents ();
	    for (String p : parents)
		categories.add (p);
	}

	List<String> l = new ArrayList<String> ();

	for (String c : categories)
	    l.add (c);

	return l;
    }

    private static String
    cat_list_to_result_categories (List<String> cats)
    {
	StringBuilder sb = new StringBuilder ();

	sb.append ("[");
	boolean done_one = false;

	for (String c : cats)
	{
	    if (done_one)
		sb.append (",");
	    sb.append ("{categoryName:\"");
	    sb.append ("GLASSVIEW.TUMRI_");
	    sb.append (c);
	    sb.append ("\",categoryDisplayName:\"");
	    // FIXME: See soz-taxonomy.lisp:print-name, what's this about?
	    sb.append (c);
	    sb.append ("\"}");
	    done_one = true;
	}

	sb.append ("]");

	return sb.toString ();
    }

    private static String
    cat_list_to_result_cat_names (List<String> cats)
    {
	StringBuilder sb = new StringBuilder ();

	boolean done_one = false;

	for (String c : cats)
	{
	    if (done_one)
		sb.append ("||");
	    // FIXME: See soz-taxonomy.lisp:print-name, what's this about?
	    sb.append (c);
	    done_one = true;
	}

	return sb.toString ();
    }
}
