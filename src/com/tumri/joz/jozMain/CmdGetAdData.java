/**
 * get-ad-data API implementation class
 */
package com.tumri.joz.jozMain;

import com.tumri.cma.domain.OSpec;
import com.tumri.joz.monitor.PerformanceMonitor;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.joz.productselection.ProductRequestProcessor;
import com.tumri.joz.productselection.ProductSelectionResults;
import com.tumri.joz.productselection.ProductSelectionProcessor;
import com.tumri.joz.JoZException;
import com.tumri.lls.client.main.ListingProvider;
import com.tumri.lls.client.response.ListingResponse;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpIFASLWriter;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpString;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CmdGetAdData extends CommandOwnWriting {

    public CmdGetAdData(Sexp e) {
        super(e);
    }

    public void process_and_write(OutputStream out) {
        boolean bError = false;
        try {
            AdDataRequest rqst = new AdDataRequest(expr);
            choose_and_write_products(rqst, out);
            bError = false;
        } catch (IOException e) {
        	log.error("IOException : Connection lost with the client - cannot write to outputstream", e);
            bError = true;
        } catch (Throwable t) {
            log.error("Unexpected Exception caught",t);
            bError = true;
        } finally {
            if (bError) {
                try {
                    writeErrorMessage("Unexpected exception caught during processing ad request", out);
                } catch (Exception ioe) {
                    log.error("Could not write the error to the output stream", ioe);
                }
            }
        }
    }

    // implementation details -------------------------------------------------

    private static Logger log = Logger.getLogger(CmdGetAdData.class);


    // Main entry point to product selection once the request has been read
    // and parsed.

    private void choose_and_write_products(AdDataRequest rqst, OutputStream out)
            throws Exception {
        boolean private_label_p = false; // FIXME: t_spec.private_label_p ();

		//Reverse order of precedence.
		String reqParams=rqst.get_t_spec();
		if (null == reqParams) {
			reqParams=rqst.get_url();
		}
		if (null == reqParams) {
			reqParams=rqst.get_theme();
		}
		if (null == reqParams) {
			reqParams=rqst.get_store_id();
		}
		if (null == reqParams) {
			Integer rId =rqst.getRecipeId();
            if (rId!=null) {
                reqParams = rId.toString();
            }
        }

        // This does the real work of selecting a set of products.
        long start_time = System.nanoTime();
        ProductSelectionProcessor prp = new ProductSelectionProcessor();
        Features features = new Features();
        ProductSelectionResults prs = prp.processRequest(rqst, features);
        if (prs!=null) {
            HashMap<Integer, ArrayList<Handle>> resultsMap = prs.getTspecResultsMap();
            ArrayList<Handle> product_handles = new ArrayList<Handle>();
            if (resultsMap!= null && !resultsMap.isEmpty()) {
                Iterator<Integer> tspecIdList = resultsMap.keySet().iterator();
                while (tspecIdList.hasNext()) {
                    Integer id = tspecIdList.next();
                    product_handles.addAll(resultsMap.get(id));
                }
            }
            String targetedOSpec = prs.getTargetedTSpecName();
            long end_time = System.nanoTime();
            long elapsed_time = end_time - start_time;

            PerformanceMonitor.getInstance().registerSuccess(reqParams, elapsed_time);

            // Send the result back to the client.
            write_result(rqst, targetedOSpec, 
                    private_label_p, features, elapsed_time, product_handles, out);
        } else {
			if (reqParams!=null) {
                PerformanceMonitor.getInstance().registerFailure(reqParams);
            }
        	log.error("No results were returned during product selecion");
        	writeErrorMessage("null t-spec, most likely no default realm", out);
        }

    }

    /**
     * Helper method to write the error string into the output stream.
     * @param errorString
     */
    private void writeErrorMessage(String errorString,OutputStream out) throws Exception {
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

    private void write_result(AdDataRequest rqst, String oSpecName,
            boolean private_label_p, Features features, long elapsed_time,
            ArrayList<Handle> product_handles, OutputStream out)
            throws Exception {
        SexpIFASLWriter w = new SexpIFASLWriter(out);
        Integer maxDescLength = rqst.get_max_prod_desc_len();

        w.writeVersion();

        w.startDocument();

        // See above, 9 elements in result list.
        w.startList(9);

        write_elm(w, "VERSION", new SexpString("1.0"));

        // This is a big part of the result, write directly.
        w.startList(2);
        long[] pids = new long[product_handles.size()];

        for (int i=0;i<product_handles.size();i++){
            pids[i] = product_handles.get(i).getOid();
        }

        ListingProvider _prov = ListingProviderFactory.getProviderInstance(JOZTaxonomy.getInstance().getTaxonomy(),
                        MerchantDB.getInstance().getMerchantData());
        ListingResponse response = _prov.getListing(pids, (maxDescLength != null) ? maxDescLength.intValue() : 0,null);
        if (response==null) {
            throw new JoZException("Invalid response from Listing Provider");
        }
        w.writeString8("PRODUCTS");
        w.writeString(response.getListingDetails());
        write_elm(w, "PROD-IDS", response.getProductIdList());
        write_elm(w, "CATEGORIES", response.getCatDetails());
        write_elm(w, "CAT-NAMES", response.getCatIdList());

        write_elm(w, "REALM", rqst.getTargetedRealm()); // FIXME: wip
        write_elm(w, "STRATEGY", oSpecName); // FIXME: wip

        write_elm(w, "IS-PRIVATE-LABEL-P", (private_label_p ? "t" : "nil"));

        write_elm(w, "SOZFEATURES", features.toString(elapsed_time));

        w.endList();

        w.endDocument();

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

}
