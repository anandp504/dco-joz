package com.tumri.joz.jozMain;

import com.tumri.lls.client.main.ListingProvider;
import com.tumri.lls.client.main.LLCClientException;
import com.tumri.lls.client.response.ListingResponse;
import com.tumri.content.data.Taxonomy;
import com.tumri.content.data.Category;
import com.tumri.content.MerchantDataProvider;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;

import java.util.Properties;
import java.util.List;
import java.util.HashMap;

/**
 * Implementation of the Listing Provider for Joz. This implementation depends upon the Product information to be
 * loaded into the Joz's memory ( in the Product DB ) class.
 * 
 * @author: nipun
 * Date: Mar 29, 2008
 * Time: 6:50:29 AM
 */
public class JozListingProviderImpl implements ListingProvider {

    private static JozListingProviderImpl _inst = null;

    public static JozListingProviderImpl getInstance() {
        if (_inst == null) {
            _inst = new JozListingProviderImpl();
        }
        return _inst;
    }

    /**
     * Initialize the Data Provider
     * @param configProps
     * @param t
     * @param m
     */
    public void init(Properties configProps, Taxonomy t, MerchantDataProvider m) {
        //do nothing
        return;
    }

    /**
     * Entry point to lookup the Product Information from the ProductDB and get the response
     * @param pids
     * @param maxDescLength
     * @return
     */
    public ListingResponse getListing(String advertiser, long[] pids, int maxDescLength, String[] slotData) {

        StringBuilder listingBuffr = new StringBuilder();

        listingBuffr.append("[");
        boolean done1 = false;
        for (int i=0;i<pids.length;i++) {
            if (done1)
                listingBuffr.append(",");
            Handle h = ProductDB.getInstance().get(pids[i]).getHandle();
            listingBuffr.append(JozJSONResponseBuilder.getListingDetails(h, maxDescLength));
            done1 = true;
        }
        listingBuffr.append("]");

        String product_ids = JozJSONResponseBuilder.constructListingIdList(pids);

        List<Category> cat_list = JozJSONResponseBuilder.constructCategoryList(pids);
        String categories = JozJSONResponseBuilder.getCategoryDetails(cat_list);
        String cat_names = JozJSONResponseBuilder.getCategoryNameList(cat_list);

        ListingResponse result = new ListingResponse();
        result.setCatDetails(categories);
        result.setCatIdList(cat_names);
        result.setListingDetails(listingBuffr.toString());
        result.setProductIdList(product_ids);
        return result;
    }

    /**
     * Clear out any resources that were used
     */
    public void shutdown() {
        //do nothing
        return;
    }


    /**
     * Handle the content refresh
     * @param t --> Taxonomy that has been loaded
     * @param m --> Merchant information that has been loaded
     */
    public boolean doContentRefresh(Taxonomy t, MerchantDataProvider m) {
        //do nothing
        return true;
    }

    public boolean doHealthCheck() throws LLCClientException {
        return true;
    }

    public void clearCache() throws LLCClientException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
