package com.tumri.joz.monitor;

import com.tumri.joz.JoZException;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.jozMain.ListingProviderFactory;
import com.tumri.joz.jozMain.MerchantDB;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.joz.productselection.ProductSelectionRequest;
import com.tumri.joz.productselection.TSpecExecutor;
import com.tumri.lls.client.main.ListingProvider;
import com.tumri.lls.client.response.ListingResponse;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Monitor for JoZ ProductQuery component
 *
 * @author vijay
 */
public class ProductQueryMonitor extends ComponentMonitor
{

    private static Logger log = Logger.getLogger(ProductQueryMonitor.class);

    public ProductQueryMonitor()
    {
       super("getaddata", new ProductQueryMonitorStatus("getaddata"));
    }

    /**
     * Method not supported
     * @param tSpecId
     * @return
     */
    public MonitorStatus getStatus(String tSpecId) {
        throw new UnsupportedOperationException("Method not supported");
    }

    /**
     * Method to get the product information for a tspec
     * @param tSpecId
     * @return
     */
    public MonitorStatus getStatus(int tSpecId)
    {

        ProductSelectionRequest pr = new ProductSelectionRequest();
        pr.setPageSize(100);
        pr.setCurrPage(0);
        pr.setOfferType(AdDataRequest.AdOfferType.PRODUCT_LEADGEN);
        pr.setBPaginate(true);
        pr.setBRandomize(false);
        pr.setRequestKeyWords(null);
        pr.setBMineUrls(false);


        List<Map<String, String>>  results;

        try {
            ArrayList<Handle> handles = doProductSelection(tSpecId, pr, new Features() );
            results = getProductData(handles);
        }
        catch(Exception ex) {
            log.error("Error reading sexpression:  "+ex.getMessage());
            results = null;
        }

        ((ProductQueryMonitorStatus)status.getStatus()).setProducts(results);
	    ((ProductQueryMonitorStatus)status.getStatus()).setProductQuery(pr.toString());

        return status;
    }

    /**
     * Execute the current tspec and add to the results map
     * @param pr
     */
    private ArrayList<Handle> doProductSelection(int tspecId, ProductSelectionRequest pr, Features f) {
        TSpecExecutor qp = new TSpecExecutor(pr, f);
        return qp.processQuery(tspecId);
    }

    /**
     * Get the JSON Product listing response from LLC Client.
     * @param handles
     * @return
     * @throws JoZException
     */
    private List<Map<String, String>> getProductData( ArrayList<Handle> handles) throws JoZException {
        Integer maxDescLength = 100;// default
        List<Map<String, String>> products=new ArrayList<Map<String,String>>();

        if (handles==null) {
            throw new JoZException("No products returned by the product selection");
        }

        String jsonStr = null;
        ListingResponse response = null;

        if (handles.size()>0) {
            long[] pids = new long[handles.size()];

            for (int i=0;i<handles.size();i++){
                pids[i] = handles.get(i).getOid();
            }

            ListingProvider _prov = ListingProviderFactory.getProviderInstance(JOZTaxonomy.getInstance().getTaxonomy(),
                    MerchantDB.getInstance().getMerchantData());
            response = _prov.getListing(pids, (maxDescLength != null) ? maxDescLength.intValue() : 0,null);
            if (response==null) {
                throw new JoZException("Invalid response from Listing Provider");
            }

            jsonStr = response.getListingDetails();

        }
        if (jsonStr==null || "".equals(jsonStr)) {
            throw new JozMonitorException("Products not found.");
        }
        StringBuffer rawData = new StringBuffer();
        rawData.append("[PRODUCTS = " + jsonStr + "] ");
        rawData.append("[PROD-IDS = " + response.getProductIdList() + "] ");
        rawData.append("[CATEGORIES = " + response.getCatDetails() + "] ");
        rawData.append("[CAT-NAMES = " + response.getCatIdList() + "] ");

        ((ProductQueryMonitorStatus)status.getStatus()).setProductRawData(rawData.toString());

        //jsonStr = jsonStr.replaceAll("\\\\\\\\\"","\\\\\\\\\\\\\"");
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i=0; i<jsonArray.length(); i++) {
                Map<String, String> attributes = new HashMap<String, String>();
                JSONObject jsonObj  = (JSONObject)jsonArray.get(i);
                Iterator it = jsonObj.keys();
                String key = null;
                String value = null;
                while (it.hasNext()) {
                    key = (String)it.next();
                    value = (String)jsonObj.get(key);
                    if (key != null)
                        attributes.put(key, value);
                }
                products.add(attributes);
            }
        }
        catch (Exception ex) {
            log.info(jsonStr);
            log.error("Error in json parsing : " + ex);
            throw new JozMonitorException("Unexpected Json library error.");
        }

        return products;

    }

}
