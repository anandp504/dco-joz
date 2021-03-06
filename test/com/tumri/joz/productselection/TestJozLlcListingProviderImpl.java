package com.tumri.joz.productselection;

import com.tumri.content.data.AdvertiserTaxonomyMapper;
import com.tumri.content.ContentProviderFactory;
import com.tumri.content.InvalidConfigException;
import com.tumri.content.impl.file.FileContentConfigValues;
import com.tumri.lls.client.main.ListingProvider;
import com.tumri.lls.client.response.ListingResponse;
import com.tumri.joz.jozMain.ListingProviderFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.Assert;

import com.tumri.content.data.AdvertiserMerchantDataMapper;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author: nipun
 * Date: Mar 31, 2008
 * Time: 10:59:59 AM
 */
public class TestJozLlcListingProviderImpl {

    private static ListingProvider _prov = null;
    private static final String dataDir = "./test/data/caa/current/data";
    private static AdvertiserTaxonomyMapper t = null;
    private static AdvertiserMerchantDataMapper m = null  ;
    private static String advertiser = "BESTBUY"; 
    private int maxDescLen = 50;

    @BeforeClass
    public static void init() {
        try {
            initContent(new File(dataDir));
        } catch(IOException e){
            System.out.println("INIT FAILED  : Could not load the merchant and taxonomy information");
            throw new RuntimeException("Test setTaxonomyAndMerchantData failed");
        }
        initLlc();
        System.out.println("Done setTaxonomyAndMerchantData..");
    }

    @Test
    public void testNormalListingResponse() {
       System.out.println("Starting..");
       long[] requestPids = {57408341L, 57408365L};

       ListingResponse response = _prov.getListing(advertiser, requestPids, maxDescLen, null);

       Assert.assertNotNull(response);
       Assert.assertNotNull(response.getListingDetails());
       Assert.assertNotNull(response.getProductIdList());
       Assert.assertNotNull(response.getCatDetails());

       System.out.println("LISTING DETAILS : " + response.getListingDetails());
       System.out.println("CATEGORY DETAILS : " + response.getCatDetails());
       System.out.println("LISTING IDS : " + response.getProductIdList());
       System.out.println("CAT NAME DETAILS  : " + response.getCatIdList());
    }

    @Test
    public void testBadProdIdsListingResponse() {
       System.out.println("Starting..");
       long[] requestPids = {1L, 57408365L};

       ListingResponse response = _prov.getListing(advertiser, requestPids, maxDescLen, null);
       
       Assert.assertNotNull(response);
       Assert.assertNotNull(response.getListingDetails());
       Assert.assertNotNull(response.getProductIdList());
       Assert.assertNotNull(response.getCatDetails());

       System.out.println("LISTING DETAILS : " + response.getListingDetails());
       System.out.println("CATEGORY DETAILS : " + response.getCatDetails());
       System.out.println("LISTING IDS : " + response.getProductIdList());
       System.out.println("CAT NAME DETAILS  : " + response.getCatIdList());

    }
    private static void initContent(File file) throws IOException {
        ContentProviderFactory f;
        Properties props = new Properties();
        //Load just the taxonomy and merchant information
        props.setProperty(FileContentConfigValues.CONFIG_TAXONOMY_DIR, file.getCanonicalPath());
        props.setProperty(FileContentConfigValues.CONFIG_MERCHANT_DATA_DIR, file.getCanonicalPath());
        props.setProperty(FileContentConfigValues.CONFIG_DISABLE_MUP,"true");
        props.setProperty(FileContentConfigValues.CONFIG_SOURCE_DIR, dataDir);
        try {
            ContentProviderFactory.initialized = false;
            f = ContentProviderFactory.getInstance();
            f.init(props);
            m= f.getContentProvider().getContent().getMerchantData();
            t= f.getContentProvider().getContent().getTaxonomy();
        } catch (InvalidConfigException e) {
            throw new IOException("Unable to get products:" + ((e.getCause() == null)?e.getMessage():e.getCause().getMessage()));
        }
    }

    private static void initLlc() {
        _prov = ListingProviderFactory.getProviderInstance(t,m);
    }

    @AfterClass
    public static void teardown(){
        _prov.shutdown();
    } 
}
