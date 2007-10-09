/**
 * get-ad-data API implementation class
 */
package com.tumri.joz.jozMain;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.tumri.cma.domain.OSpec;
import com.tumri.content.data.Category;
import com.tumri.content.data.MerchantData;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.productselection.ProductRequestProcessor;
import com.tumri.joz.productselection.ProductSelectionResults;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpIFASLWriter;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpString;
import com.tumri.utils.strings.EString;

public class CmdGetAdData extends CommandOwnWriting {
    
    public CmdGetAdData(Sexp e) {
        super(e);
    }
    
    public void process_and_write(OutputStream out) {
        try {
            AdDataRequest rqst = new AdDataRequest(expr);
            choose_and_write_products(rqst, out);
        } catch (IOException e) {
        	log.error("IOException : Connection lost with the client - cannot write to outputstream", e);
        } catch (Exception e) {
            log.error("Unknown Exception", e);
            try {
            	writeErrorMessage("Unexpected exception caught during processing ad request", out);
            } catch (Exception ioe) {
            	log.error("Could not write the error to the output stream", e);
            }
        }
    }
    
    // implementation details -------------------------------------------------
    
    private static Logger log = Logger.getLogger(CmdGetAdData.class);
    
    
    // Main entry point to product selection once the request has been read
    // and parsed.
    
    private void choose_and_write_products(AdDataRequest rqst, OutputStream out)
            throws IOException, Exception {
        Features features = null;
        boolean private_label_p = false; // FIXME: t_spec.private_label_p ();
        
        // This does the real work of selecting a set of products.
        long start_time = System.nanoTime();
        ProductRequestProcessor prp = new ProductRequestProcessor();
        ProductSelectionResults prs = prp.processRequest(rqst);
        if (prs!=null) {
            ArrayList<Handle> product_handles = prs.getResults();
            OSpec targetedOSpec = prs.getTargetedOSpec();
            HashMap<String, String> featuresMap = prs.getFeaturesMap();
            features = new Features(featuresMap);
            long end_time = System.nanoTime();
            long elapsed_time = end_time - start_time;
            
            // Send the result back to the client.
            write_result(rqst, targetedOSpec, null /* FIXME:wip */,
                    private_label_p, features, elapsed_time, product_handles, out);
        } else {
        	log.error("No results were returned during product selecion");
        	writeErrorMessage("null t-spec, most likely no default realm", out);
        }
        
    }
    
    /**
     * Helper method to write the error string into the output stream.
     * @param errorString
     */
    private void writeErrorMessage(String errorString,OutputStream out) throws IOException, Exception { 
        SexpIFASLWriter w = new SexpIFASLWriter(out);
        w.startDocument();
        write_elm(w, "error", new SexpString(errorString));
        w.endDocument();
    }
    
    // Write the chosen product list back to the client.
    // The format is:
    //
    // (
    // ("VERSION" "1.0")
    // ("PRODUCTS" product-list)
    // ("PROD-IDS" product-id-list)
    // ("CATEGORIES" category-list)
    // ("CAT-NAMES" category-name-list)
    // ("REALM" realm)
    // ("STRATEGY" t-spec-name)
    // ("IS-PRIVATE-LABEL-P" is-private-label-p)
    // ("SOZFEATURES" feature-list-or-nil)
    // )
    //
    // FIXME: version number needs to be spec'd to be first, remainder should
    // allow for optional parameters, (consider precedent of IS-PRIVATE-LABEL-P
    // and what happens over time as more are added).
    //
    // NOTE: In SoZ this is the "js-friendly" format, js for JSON
    // http://www.json.org.
    
    private void write_result(AdDataRequest rqst, OSpec ospec,
            Realm realm, // FIXME: wip
            boolean private_label_p, Features features, long elapsed_time,
            ArrayList<Handle> product_handles, OutputStream out)
            throws IOException, Exception {
        SexpIFASLWriter w = new SexpIFASLWriter(out);
        Integer maxDescLength = rqst.get_max_prod_desc_len();
        
        w.writeVersion();
        
        w.startDocument();
        
        // See above, 9 elements in result list.
        w.startList(9);
        
        write_elm(w, "VERSION", new SexpString("1.0"));
        
        // This is a big part of the result, write directly.
        w.startList(2);
        w.writeString8("PRODUCTS");
        write_products(w, product_handles,
                (maxDescLength != null) ? maxDescLength.intValue() : 0);
        w.endList();
        
        // PERF: pdb.get (id) called twice
        
        String product_ids = products_to_id_list(product_handles);
        write_elm(w, "PROD-IDS", product_ids);
        
        List<Category> cat_list = products_to_cat_list(product_handles);
        String categories = cat_list_to_result_categories(cat_list);
        write_elm(w, "CATEGORIES", categories);
        String cat_names = cat_list_to_result_cat_names(cat_list);
        write_elm(w, "CAT-NAMES", cat_names);
        
        String targetedOSpecName = "";
        String targetedRealm = "";
        if (ospec != null) {
            targetedOSpecName = ospec.getName();
        }
		//@todo: The targeting and product requestProcessor should add the data to response object. So that piece of code
        //needs to be refactored across the request processors
        write_elm(w, "REALM", rqst.getTargetedRealm()); // FIXME: wip
        write_elm(w, "STRATEGY", targetedOSpecName); // FIXME: wip
        
        write_elm(w, "IS-PRIVATE-LABEL-P", (private_label_p ? "t" : "nil"));
        
        SexpList sexp_features = features.toSexpList(elapsed_time);
        write_elm(w, "SOZFEATURES", sexp_features.toStringValue());
        
        w.endList();
        
        w.endDocument();
    }
    
    // Write the list of selected products to {out}.
    // This is the biggest part of the result of get-ad-data, so we write
    // each product out individually instead of building an object describing
    // all of them and then write that out.
    // Things are complicated because the value that is written is a single
    // string containing all the products.
    
    private void write_products(SexpIFASLWriter w,
            ArrayList<Handle> product_handles, int maxDescLength)
            throws IOException {
        Iterator<Handle> iter = product_handles.iterator();
        StringBuilder b = new StringBuilder();
        
        // Ahh!!! The IFASL format requires a leading length of the
        // string. That means we pretty much have to build the entire string
        // of all products' data before we can send it.
        
        b.append("[");
        
        boolean done1 = false;
        while (iter.hasNext()) {
            if (done1)
                b.append(",");
            Handle h = iter.next();
            b.append(toAdDataResultString(h, maxDescLength));
            done1 = true;
        }
        
        b.append("]");
        
        String s = b.toString();
        
        // Don't construct huge string unnecessarily.
        if (log.isDebugEnabled())
            log.debug("Product string: " + s);
        
        w.writeString(s);
    }
    
    // Subroutine of {write_products} to simplify it.
    // Return a string suitable for passing back to the client.
    // See soz-product-selector.lisp:morph-product-list-into-sexpr-js-friendly.
    //
    // NOTE: If there is any RFC1630-like encoding that is needed, do it here.
    // Not everything needs to be encoded, and some things may need to be
    // encoded differently.
    
    public String toAdDataResultString(Handle h, int maxProdDescLength) {
        ProductDB pdb = ProductDB.getInstance();
        int id = h.getOid();
        IProduct p = pdb.get(id);
        
        StringBuilder b = new StringBuilder();
        
        b.append("{");
        b.append("id:\"");
        b.append(encode(p.getIdSymbol()));
        b.append("\",display_category_name:\"");
        Category cat = null;
        try {
        	cat = JOZTaxonomy.getInstance().getTaxonomy().getCategory(p.getCategory());
        } catch (NullPointerException npe) {
        	log.warn("The category specified for product is valid for the current taxonomy :" + p.getCategoryStr());
        }
        // Use the first parent as the category.
        if (cat != null) {
        	b.append(encode(cat.getName()));
        } 
        b.append("\",price:\"");
        b.append(encode_price(p.getPrice()));
        b.append("\",discount_price:\"");
        b.append(encode_price(p.getDiscountPrice()));
        b.append("\",brand:\"");
        b.append(encode(p.getBrandStr()));
        b.append("\",merchant_id:\"");
        b.append(encode(p.getSupplierStr()));
        b.append("\",provider:\"");
        b.append(encode(p.getSupplierStr()));
        MerchantData md = MerchantDB.getInstance().getMerchantData()
                .getMerchant(p.getSupplierStr());
        b.append("\",merchantlogo:\"");
        if (md != null) {
            b.append(encode(md.getLogoUrl()));
        }
        b.append("\",ship_promo:\"");
        if (md != null) {
            b.append(encode(md.getShippingPromotionText()));
        }
        b.append("\",description:\"");
        String desc = p.getDescription();
        if (maxProdDescLength > 0) {
            if (desc.length() < maxProdDescLength) {
                maxProdDescLength = desc.length();
            }
            desc = desc.substring(0, maxProdDescLength);
        }
        b.append(encode(desc));
        b.append("\",thumbnailraw:\"");
        b.append(encode(p.getThumbnail()));
        b.append("\",product_url:\"");
        b.append(encode(p.getPurchaseUrl()));
        b.append("\",picture_url:\"");
        b.append(encode(p.getImageUrl()));
        b.append("\",c_code:\"");
        b.append(encode(p.getCurrencyStr()));
        b.append("\",offer_type:\"");
        b.append(encode(p.getProductTypeStr()));
        b.append("\"");
        
        b.append("}");
        
        return b.toString();
    }
    
    private String encode(EString es) {
        // FIXME: wip
        if (es == null)
            return "";
        return es.toString();
    }
    
    /**
     * Escape the single and double quote chars
     * 
     * @param s
     * @return
     */
    private String encode(String s) {
        // FIXME: wip
        if (s == null)
            return "";
        StringBuffer sb = new StringBuffer();
        for (int i = 0, len = s.length(); i < len; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("&#34;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
    
    private String encode_price(Float f) {
        // FIXME: wip
        if (f == null)
            return "";
        return String.format("%.2f", f);
    }
    
    private String encode_price(Double d) {
        // FIXME: wip
        if (d == null)
            return "";
        return String.format("%.2f", d);
    }
    
    // Write an element of the result.
    
    private void write_elm(SexpIFASLWriter w, String name, Sexp sexp)
            throws IOException, Exception {
        // Don't construct string unnecessarily.
        if (log.isDebugEnabled())
            log.debug("Writing " + name + ": " + sexp.toString());
        
        w.startList(2);
        w.writeString8(name);
        w.visit(sexp);
        w.endList();
    }
    
    private void write_elm(SexpIFASLWriter w, String name, String s)
            throws IOException {
        // Don't construct string unnecessarily.
        if (log.isDebugEnabled())
            log.debug("Writing " + name + ": " + s);
        
        w.startList(2);
        w.writeString8(name);
        // FIXME: assumes ASCII
        w.writeString8(s);
        w.endList();
    }
    
    private static String products_to_id_list(ArrayList<Handle> product_handles) {
        ProductDB pdb = ProductDB.getInstance();
        StringBuilder b = new StringBuilder();
        boolean done_one = false;
        
        for (Handle h : product_handles) {
            if (done_one)
                b.append(",");
            int id = h.getOid();
            IProduct p = pdb.get(id);
            b.append(p.getGId());
            done_one = true;
        }
        
        return b.toString();
    }
    
    // Return uniqified list of all categories in {product_handles}.
    
    private static List<Category> products_to_cat_list(
            ArrayList<Handle> product_handles) {
        ProductDB pdb = ProductDB.getInstance();
        HashSet<Category> categories = new HashSet<Category>();
        
        for (Handle h : product_handles) {
            int id = h.getOid();
            IProduct p = pdb.get(id);
            // FIXME: Don't think this records _all_ parents.
            Category cat = JOZTaxonomy.getInstance().getTaxonomy().getCategory(
                    p.getCategory());
            if (cat != null) {
                categories.add(cat);
            }
        }
        
        List<Category> l = new ArrayList<Category>();
        
        for (Category c : categories)
            l.add(c);
        
        return l;
    }
    
    private String cat_list_to_result_categories(List<Category> cats) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[");
        boolean done_one = false;
        
        for (Category c : cats) {
            if (done_one)
                sb.append(",");
            sb.append("{categoryName:\"");
            sb.append(c.getGlassIdStr());
            sb.append("\",categoryDisplayName:\"");
            sb.append(encode(c.getName()));
            sb.append("\"}");
            done_one = true;
        }
        
        sb.append("]");
        
        return sb.toString();
    }
    
    private String cat_list_to_result_cat_names(List<Category> cats) {
        StringBuilder sb = new StringBuilder();
        
        boolean done_one = false;
        
        for (Category c : cats) {
            if (done_one)
                sb.append("||");
            // FIXME: See soz-taxonomy.lisp:print-name, what's this about?
            sb.append(encode(c.getName()));
            done_one = true;
        }
        
        return sb.toString();
    }
}
