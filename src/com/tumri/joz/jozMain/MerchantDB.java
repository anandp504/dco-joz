// The merchant database.
// FIXME: wip
// TODO: OMG, OMG, this is all wrong.  Chill dude.  As things evolve this
// will get rewritten to be The Right Way.

package com.tumri.joz.jozMain;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.tumri.content.MerchantDataProvider;
import com.tumri.content.data.MerchantData;
import com.tumri.utils.sexp.BadSexpException;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;

public class MerchantDB
{
    
    protected static MerchantDB instance=null;
    
    protected static Sexp attributesAndMetadata = null;
    
    static {
        try {
	    StringBuilder s = new StringBuilder ();
	    s.append ("(");
	    s.append (" (");
	    s.append ("  (merchantid \"Merchant ID\") (merchantname \"Merchant Name\")");
	    s.append ("  (hascatalogname \"Catalog Name\") (merchantrating \"Merchant Rating\")");
	    s.append ("  (logourl \"Logo URL\") (homepageurl \"Home Page URL\")");
	    s.append ("  (suppliescategory \"Category\")");
	    s.append ("  (collectstax \"Collects Taxes?\")");
	    s.append ("  (catalogfilename \"Catalog File\")");
	    s.append ("  (catalogproductcount \"Product Count\")");
	    s.append ("  (reviewinfo \"Review Info\")");
	    s.append ("  (contactinfo \"Contact Info\")");
	    s.append ("  (shippingpromotiontext \"Shipping Promotion\")");
	    s.append ("  (returnpolicytext \"Return Policy\")");
	    s.append (" )");
	    s.append (" (merchantid merchantname)");
	    s.append (" (merchantid merchantname hascatalogname merchantrating logourl homepageurl suppliescategory collectstax catalogfilename catalogproductcount reviewinfo contactinfo shippingpromotiontext returnpolicytext)");
	    s.append (" (merchantid merchantname hascatalogname merchantrating logourl homepageurl suppliescategory collectstax catalogfilename catalogproductcount reviewinfo contactinfo shippingpromotiontext returnpolicytext)");
	    s.append (" (");
	    s.append ("  (merchantid merchant string yes no (listof))");
	    s.append ("  (merchantname merchant string yes no (listof))");
	    s.append ("  (hascatalogname merchant string yes no (listof))");
	    s.append ("  (merchantrating merchant string yes no (listof))");
	    s.append ("  (logourl merchant string yes no (listof))");
	    s.append ("  (homepageurl merchant string yes no (listof))");
	    s.append ("  (suppliescategory merchant class yes no (listof))");
	    s.append ("  (collectstax merchant boolean yes no (listof))");
	    s.append ("  (catalogfilename merchant string yes no (listof))");
	    s.append ("  (catalogproductcount merchant number yes no (listof))");
	    s.append ("  (reviewinfo merchant string yes no (listof))");
	    s.append ("  (contactinfo merchant string yes no (listof))");
	    s.append ("  (shippingpromotiontext merchant string yes no (listof))");
	    s.append ("  (returnpolicytext merchant string yes no (listof))");
	    s.append (" )");
	    s.append (")");
	    attributesAndMetadata = SexpReader.readFromString (s.toString ());
        } catch (IOException e) {
            // Not gonna happen.
        } catch (BadSexpException e) {
            // Not gonna happen.
        }
    }
    
    public static MerchantDB getInstance() {
        if (instance == null) {
            synchronized(MerchantDB.class) {
              if (instance == null) {
                instance = new MerchantDB();
              }
            }
          }
          return instance;
    }
    
    AtomicReference<MerchantDataProvider> data = new AtomicReference<MerchantDataProvider>(); 
    
    public MerchantDataProvider getMerchantData() {
        return data.get();
    }
    
    public void setMerchantData(MerchantDataProvider md) {
        data.set(md);
    }

    protected void appendMerchantAttribute(StringBuilder retString, String s, boolean required) {
        if (s == null) {
            if (!required) {
                retString.append("NIL ");
            } else {
                retString.append("((NIL NIL)) ");
            }
        } else {
            retString.append("((\"" + s + "\" \"" + s + "\")) ");
        }
    }

    // This is not an ideal implementation.
    public SexpList getTabulatedSearchResults() {
        StringBuilder retString = new StringBuilder();
        
        List<MerchantData> md = getMerchantData().getAll();
        
        // Begin Sexp
        retString.append("(");
        // Counts
        retString.append("(1 " + md.size() + " " + md.size() + " )" );
        // Pagination
        retString.append(" NIL "); 
        // Headers
        retString.append("(\"Merchant ID\" \"Merchant Name\" \"Catalog Name\" \"Merchant Rating\" \"Logo URL\" \"Home Page URL\" \"Category\" \"Collects Taxes?\" \"Catalog File\" \"Product Count\" \"Review Info\" \"Contact Info\" \"Shipping Promotion\" \"Return Policy\")");
        // Merchants
        retString.append("(");
        for (MerchantData m: md) {
            retString.append("(|");
            retString.append(m.getMerchant());
            retString.append("|");
            retString.append(" ");
            
            appendMerchantAttribute(retString, m.getMerchantId(), false);
            appendMerchantAttribute(retString, m.getMerchantName(), false);
            appendMerchantAttribute(retString, m.getHasCatalogName(), false);
            appendMerchantAttribute(retString, m.getMerchantRating(), false);
            appendMerchantAttribute(retString, m.getLogoUrl(), false);
            appendMerchantAttribute(retString, m.getHomePageUrl(), false);
            appendMerchantAttribute(retString, m.getSuppliesCategory(), false);
            appendMerchantAttribute(retString, m.getCollectsTax(), false);
            appendMerchantAttribute(retString, m.getCatalogFilename(), false);
            appendMerchantAttribute(retString, ((m.getCatalogProductCount()!=null)?Long.toString(m.getCatalogProductCount()):null), false);
            appendMerchantAttribute(retString, m.getReviewInfo(), false);
            appendMerchantAttribute(retString, m.getContactInfo(), false);
            appendMerchantAttribute(retString, m.getShippingPromotionText(), false);
            appendMerchantAttribute(retString, m.getReturnPolicyText(), true);
            retString.append(")");
            
        }
        retString.append(" )"); // End Merchants
        retString.append(" )"); // End Sexp
        Sexp s;
        try {
            s = SexpReader.readFromString(retString.toString());
        } catch (IOException e) {
            return null;
        } catch (BadSexpException e) {
            return null;
        }
        if (!s.isSexpList()) {
            return null;
        }
        return s.toSexpList();
    }
    
    public Sexp getAttributesAndMetadata() {
        return attributesAndMetadata;
    }
    

    /*
    MerchantDB (String attributes_and_metadata_path,
		String tabulated_search_results_path)
	throws FileNotFoundException, IOException, BadMerchantDataException
    {
	// init (attributes_and_metadata_path,tabulated_search_results_path);
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
	if (merchant == null)
	    return null;
	return _logo_url_db.get (merchant.toString ().toLowerCase ());
    }

    // Return the logo url for {merchant} or null if not provided.

    public String
    get_logo_url (String merchant)
    {
	if (merchant == null)
	    return null;
	return _logo_url_db.get (merchant.toLowerCase ());
    }

    // Return the shipping promo for {merchant} or null if not provided.
    // An example of a shipping promo is, I think,
    // "Free Shipping on Orders over $50".

    public String
    get_shipping_promo (EString merchant)
    {
	if (merchant == null)
	    return null;
	return _shipping_promo_db.get (merchant.toString ().toLowerCase ());
    }

    // Return the shipping promo for {merchant} or null if not provided.
    // An example of a shipping promo is, I think,
    // "Free Shipping on Orders over $50".

    public String
    get_shipping_promo (String merchant)
    {
	if (merchant == null)
	    return null;
	return _shipping_promo_db.get (merchant.toLowerCase ());
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

	// watch for nil
	if (e.isSexpList ())
	{
	    l = e.toSexpList ();
	    if (l.size () == 0)
		return "nil";
	    log.error ("Bad merchant data, expected nil or string for " + what
		       + ", got: " + e.toString ());
	    return "nil"; // FIXME
	}

	if (! e.isSexpString ())
	    log.error ("Bad merchant data, expected string for " + what
		       + ", got: " + e.toString ());
	return e.toStringValue ();
    }
    */
}
