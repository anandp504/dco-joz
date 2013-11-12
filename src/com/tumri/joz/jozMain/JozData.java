// Repository of various Joz data.

package com.tumri.joz.jozMain;

import java.util.Properties;

import com.tumri.content.data.impl.AdvertiserMerchantDataMapperImpl;
import com.tumri.content.data.impl.AdvertiserTaxonomyMapperImpl;
import com.tumri.joz.products.ListingOptContentPoller;
import org.apache.log4j.Logger;

import com.tumri.joz.campaign.CMAContentPoller;
import com.tumri.joz.campaign.CMAContentRefreshMonitor;
import com.tumri.joz.campaign.wm.loader.WMContentPoller;
import com.tumri.joz.products.ContentHelper;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.LogUtils;
import com.tumri.joz.utils.ZipCodeDB;
import com.tumri.joz.JoZException;

public class JozData {

    private static Logger log = Logger.getLogger(JozData.class);
    
    public static void init() {
        // Read the datapath from the joz.properties file
        AppProperties props = AppProperties.getInstance();
        //Load Zip Data
        loadZipData();
        //Init the jozindex
        loadContent(props.getProperties());
        //Init the CMA data
        loadCampaignData();
        //Init LLC
        ListingProviderFactory.getProviderInstance(AdvertiserTaxonomyMapperImpl.getInstance(),
                        AdvertiserMerchantDataMapperImpl.getInstance());

	    ListingOptContentPoller.getInstance().init(); //add poller for opt prod indexes

    }

    private static void loadContent(Properties props) {
        ContentHelper.init(props);
    }
    
    /**
     * Load the campaign data
     * 
     */
    private static void loadCampaignData() {
        // Initialize the campaign content poller, which will also take care
        // loading the campaign db.
        try {
        	CMAContentRefreshMonitor.getInstance().init();
            CMAContentPoller.getInstance().init();
            WMContentPoller.getInstance().init();
        } catch (Exception e) {
            LogUtils.getFatalLog().fatal("Exception caught during campaign data load", e);
        } catch (Throwable t) {
            LogUtils.getFatalLog().fatal("Unexpected runtime exception during the campaign data load", t);
        }
    }

    private static void loadZipData() {
        //Load Zip db
        try {
            ZipCodeDB.getInstance().init();
        } catch (JoZException e) {
            LogUtils.getFatalLog().fatal("Zip code database could not be initialized", e);
        } catch (Throwable t) {
            LogUtils.getFatalLog().fatal("Unexpected runtime exception during the zip data load", t);
        }

    }
}
