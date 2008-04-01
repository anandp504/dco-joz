// Repository of various Joz data.

package com.tumri.joz.jozMain;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.tumri.joz.campaign.CMAContentPoller;
import com.tumri.joz.campaign.CMAContentRefreshMonitor;
import com.tumri.joz.products.ContentHelper;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.LogUtils;
import com.tumri.lls.client.ListingLookupDataProviderImpl;
import com.tumri.lls.client.main.LlcListingProviderImpl;

public class JozData {
    
    public static void init() {
        // Read the datapath from the joz.properties file
        AppProperties props = AppProperties.getInstance();
        loadContent(props.getProperties());
        loadCampaignData();
        //This call will instantiate the listings provider class
        ListingProviderFactory.getProviderInstance(JOZTaxonomy.getInstance().getTaxonomy(),
                        MerchantDB.getInstance().getMerchantData());

    }
    
    /*
     * - sigh public static void init1 () throws Exception { String data_path =
     * "../data/joz";
     * 
     * try { String merchant_path = data_path + "/MD"; merchant_db = new
     * MerchantDB (merchant_path + "/attributes-and-metadata.lisp",
     * merchant_path + "/tabulated-search-results.lisp"); } catch (Exception e) {
     * log.error ("Unable to initialize Merchant DB: " + e.toString ()); }
     * 
     * try { //String mup_prefix = data_path + "/MUP/MUP-US0031-US-DEFAULT_";
     * String mup_prefix = data_path +
     * "/MUP/MUP-USpub0017_MUP_US0071-US-DEFAULT_";
     * 
     * mup_db = new TmpMUPDB (mup_prefix + ".ifasl", mup_prefix + ".strings",
     * mup_prefix + ".taxonomy", "../data/default-realm-js-friendly.lisp"); }
     * catch (Exception e) { log.error ("Unable to initialize MUP DB: " +
     * e.toString ()); }
     * 
     * try { tspec_db = new TmpTSpecDB (data_path + "/t-specs/t-specs.lisp"); }
     * catch (Exception e) { log.error ("Unable to initialize TSpec DB: " +
     * e.toString ()); }
     * 
     * try { mapping_db = new TmpMappingDB (data_path +
     * "/mappings/mapping.lisp"); } catch (Exception e) { log.error ("Unable to
     * initialize Mapping DB: " + e.toString ()); }
     * 
     * try { String lucene_path = data_path + "/lucene"; lucene_db = new
     * LuceneDB (lucene_path); } catch (Exception e) { log.error ("Unable to
     * initialize Lucene DB: " + e.toString ()); } }
     */

    // implementation details -------------------------------------------------
    private static Logger log = Logger.getLogger(JozData.class);
    
    protected static void loadContent(Properties props) {
        ContentHelper.init(props);
    }
    
    /**
     * Load the campaign data
     * 
     */
    protected static void loadCampaignData() {
        // Initialize the campaign content poller, which will also take care
        // loading the campaign db.
        try {
        	CMAContentRefreshMonitor.getInstance().init();
            CMAContentPoller.getInstance().init();
        } catch (Exception e) {
            log.error("Exception caught during the campaign data load");
            LogUtils.getFatalLog().fatal("Exception caught during campaign data load", e);
        } catch (Throwable t) {
            log.error("Unexpected runtime exception during the campaign data load");
            LogUtils.getFatalLog().fatal("Unexpected runtime exception during the campaign data load", t);
        }
    }
    
    /*
     * @SuppressWarnings("unchecked") private static void load_products (String
     * dir) throws Exception { String file = dir +
     * "/MUP/MUP-USpub0025_MUP_US0092-US-DEFAULT_.ifasl"; FileInputStream in =
     * new FileInputStream (file); FASLReader fr = new FASLReader (in);
     * fr.setReadKeywordsAsKeywords (true); FASLType t; ProductDB pdb =
     * ProductDB.getInstance (); // ???
     * 
     * try { int count = 0;
     * 
     * while ((t = fr.read ()) != null) { if (t.type () != FASLType.list) { //
     * ??? First sexp is apparently an empty string. Why? String s = t.toString
     * (); if (! s.equals ("")) log.info ("Ignoring `" + t.toString () + "'");
     * continue; } // FIXME: This generates an "unchecked" warning, but methinks //
     * the fix belongs in the zini code. I'd rather not cast if I // don't have
     * to. Iterator<FASLType> iter = t.iterator ();
     * 
     * IProduct p = null; try { p = read_product (t, iter); } catch (Exception
     * e) { log.error ("Bad MUP entry: " + e.toString ()); continue; }
     * 
     * pdb.addProduct (p);
     * 
     * ++count; if (count % 10000 == 0) log.info ("Loaded " + count + " entries
     * ... "); }
     * 
     * if (count > 0) log.info ("Loaded " + count + " entries."); } finally {
     * in.close (); } }
     * 
     * private static IProduct read_product (FASLType t, Iterator<FASLType>
     * iter) throws Exception { String guid = IFASLUtils.toString (iter.next (),
     * "guid"); String catalog = IFASLUtils.toString (iter.next (), "catalog");
     * String product_id = IFASLUtils.toString (iter.next (), "product id");
     * ArrayList<String> parents = new ArrayList<String> (); parents.add
     * (IFASLUtils.toString (iter.next (), "parents")); Float price =
     * IFASLUtils.toFloat (iter.next (), "price"); Float discount_price =
     * IFASLUtils.toFloat (iter.next (), "discount price"); String brand =
     * IFASLUtils.toString (iter.next (), "brand"); String retailer =
     * IFASLUtils.toString (iter.next (), "retailer"); String merchant =
     * IFASLUtils.toString (iter.next (), "merchant"); String name =
     * IFASLUtils.toString (iter.next (), "name"); String description =
     * IFASLUtils.toString (iter.next (), "description"); // FIXME: rank type
     * seems to be real // Integer rank = IFASLUtils.toInteger (iter.next (),
     * "rank");
     * 
     * iter.next (); // swallow rank Integer rank = new Integer (0); // end rank
     * temp hack String thumbnail_url = IFASLUtils.toString (iter.next (),
     * "thumbnail url"); String purchase_url = IFASLUtils.toString (iter.next
     * (), "purchase url"); String image_url = IFASLUtils.toString (iter.next
     * (), "image url"); Integer image_width = IFASLUtils.toInteger (iter.next
     * (), "image width"); Integer image_height = IFASLUtils.toInteger
     * (iter.next (), "image height"); Integer cpc = IFASLUtils.toInteger
     * (iter.next (), "cpc"); String currency = IFASLUtils.toString (iter.next
     * (), "currency"); String discount_currency = IFASLUtils.toString
     * (iter.next (), "discount currency"); Integer black_white_list_status =
     * IFASLUtils.toInteger (iter.next (), "black white list status"); String
     * product_type = IFASLUtils.toString (iter.next (), "product type"); Float
     * cpo_cpa = IFASLUtils.toFloat (iter.next (), "cpo/cpa"); String
     * base_product_number = IFASLUtils.toString (iter.next (), "base product
     * number"); // soz reads "long-description" here, but ignores it
     * 
     * ProductImpl p = new ProductImpl (); // E.g. _1296.US3660122 -> 3660122
     * String gid = product_id.substring (product_id.indexOf ('.')); while
     * (gid.charAt (0) < '0' || gid.charAt (0) > '9') gid = gid.substring (1);
     * 
     * p.setGId(gid); p.setCatalogStr(catalog); p.setCategoryStr(parents.get
     * (0)); p.setPriceStr(price != null ? price.toString () : null);
     * p.setDiscountPriceStr(discount_price != null ? discount_price.toString () :
     * null); p.setBrandStr(brand); p.setSupplierStr(retailer);
     * p.setProviderStr(merchant); p.setProductName(name);
     * p.setDescription(description); p.setRank(rank);
     * p.setThumbnail(thumbnail_url); p.setPurchaseUrl(purchase_url);
     * p.setImageUrl(image_url); p.setImageWidth(image_width);
     * p.setImageHeight(image_height); p.setCPCStr(cpc != null ? cpc.toString () :
     * null); p.setCurrencyStr(currency); p.setDiscountPriceCurrencyStr
     * (discount_currency); p.setBlackWhiteListStatus (black_white_list_status);
     * p.setProductTypeStr(product_type); p.setCPOStr(cpo_cpa != null ?
     * cpo_cpa.toString () : null); p.setBaseProductNumber(base_product_number);
     * 
     * return new ProductWrapper(p); }
     * 
     * private static void load_taxonomy (String dir) throws Exception { String
     * file = dir + "/MUP/MUP-USpub0025_MUP_US0092-US-DEFAULT_.taxonomy";
     * FileReader fr = new FileReader (file); LineNumberReader rdr = new
     * LineNumberReader (fr); JOZTaxonomy tax = JOZTaxonomy.getInstance ();
     * 
     * try { String s; char delim = '\t';
     * 
     * while ((s = rdr.readLine ()) != null) { // TODO: would be nice to allow
     * comments in the file // Methinks I'd rather use StringTokenizer here but
     * the docs // say its use is discouraged. Blech. String[] tokens = s.split
     * (String.valueOf (delim)); if (tokens.length != 4) throw new
     * BadMUPDataException ("taxonomy entry needs 4 elements: " + s); String id =
     * tokens[0]; // E.g. TUMRI_14385 String name = tokens[1]; // E.g. Action
     * Figures String parent_id = tokens[2]; // E.g. TUMRI_14384 String
     * parent_name = tokens[3]; // E.g. Toys & Games // FIXME: or the tax codes
     * ... tax.addNodes (parent_name, name); } } finally { fr.close (); }
     * 
     * CategoryIndex catIndex = (CategoryIndex) ProductDB.getInstance
     * ().getIndex (IProduct.Attribute.kCategory); catIndex.update (tax); }
     */
}
