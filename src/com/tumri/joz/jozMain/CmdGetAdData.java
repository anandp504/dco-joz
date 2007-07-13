// get-ad-data command

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
    - result = morph-product-list-into-sexpr
  otherwise error "null t-spec, most likely no default realm"

*/

package com.tumri.joz.jozMain;

//import java.io.PrintWriter;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.tumri.utils.sexp.Sexp;

public class CmdGetAdData extends Command
{
    private static Logger log = null;

    static
    {
	String filename = System.getProperty ("LOG4J_PROPS");
	if (filename != null)
	{
	    System.out.println ("Loading log4j properties from " + filename);
	    PropertyConfigurator.configure (filename);
	}
	log = Logger.getLogger (CmdGetAdData.class);
    }

    public CmdGetAdData (Sexp e)
    {
	super (e);
    }

    public Sexp
    process ()
    {
	Sexp e;

	try
	{
	    AdDataRequest rqst = new AdDataRequest (expr);
	    e = choose_products (rqst);
	}
	catch (Exception ex)
	{
	    log.error (ex);
	    e = JozData.mup_db.get_default_realm_response ();
	}

	return e;
    }

    // implementation details -------------------------------------------------

    private Sexp
    choose_products (AdDataRequest rqst)
    {
	TSpecAndRealm tsar = choose_t_spec_and_realm (rqst);
	TSpec t_spec = tsar._t_spec;
	Realm realm = tsar._realm;
	List<SelectedProduct> products = choose_products (rqst, t_spec, realm);
	Sexp e = morph_product_list_into_sexp (products, rqst, t_spec, realm);
	return e;
    }

    private class TSpecAndRealm
    {
	public TSpec _t_spec = null;
	public Realm _realm = null;

	public TSpecAndRealm (TSpec t, Realm r)
	{
	    _t_spec = t;
	    _realm = r;
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
	Uri uri; // not initialized on purpose

	String t_spec_param = rqst.get_t_spec ();

	if (t_spec_param != null)
	{
	    t_spec_name = t_spec_param;
	    String url = rqst.get_url ();
	    if (url != null)
	    {
		realm = choose_best_realm_for_uri (Uri.build_lax_uri (url));
		if (realm == null)
		    realm = new Realm (Uri.build_lax_uri (JozData.tspec_db.get_default_realm_url ()));
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
		 && (t_spec_name = choose_t_spec_for_theme (rqst)) != null)
	{
	    // got it
	    realm = new Realm (theme);
	}
	// NOTE: We're picking up {theme} set in the previous test.
	else if (theme != null
		 && (uri = Uri.build_lax_uri (theme)) != null
		 && (t_spec_name = choose_t_spec_for_uri (uri)) != null)
	{
	    // got it
	    realm = new Realm (uri);
	}
	else if (rqst.get_url () != null
		 && (uri = Uri.build_lax_uri (rqst.get_url ())) != null
		 && (t_spec_name = choose_t_spec_for_uri (uri)) != null)
	{
	    // got it
	    realm = new Realm (uri);
	}
	else
	{
	    String default_realm_url = JozData.tspec_db.get_default_realm_url ();
	    Uri default_realm_uri = Uri.build_lax_uri (default_realm_url);
	    t_spec_name = choose_t_spec_for_realm (default_realm_uri);
	    realm = new Realm (default_realm_uri);
	}

	TSpec t_spec = JozData.tspec_db.get (t_spec_name);
	TSpecAndRealm tasr = new TSpecAndRealm (t_spec, realm);
	return tasr;
    }

    private List<SelectedProduct>
    choose_products (AdDataRequest rqst, TSpec t_spec, Realm realm)
    {
	return null; // FIXME: wip
    }

    private Sexp
    morph_product_list_into_sexp (List<SelectedProduct> products,
				  AdDataRequest rqst, TSpec t_spec, Realm realm)
    {
	return null; // FIXME: wip
    }

    private Realm
    choose_best_realm_for_uri (Uri uri)
    {
	return null; // FIXME: wip
    }

    private String
    choose_t_spec_for_store_id (String store_id)
    {
	return null; // FIXME: wip
    }

    private String
    choose_t_spec_for_theme (AdDataRequest rqst)
    {
	return null; // FIXME: wip
    }

    private String
    choose_t_spec_for_uri (Uri uri)
    {
	return null; // FIXME: wip
    }

    private String
    choose_t_spec_for_realm (Uri realm_uri)
    {
	return null; // FIXME: wip
    }
}
