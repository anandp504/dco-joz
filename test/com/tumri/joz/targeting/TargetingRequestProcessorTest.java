package com.tumri.joz.targeting;

import com.tumri.cma.CMAFactory;
import com.tumri.cma.domain.*;
import com.tumri.cma.service.CampaignProvider;
import com.tumri.joz.campaign.CampaignDBDataLoader;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.utils.AppProperties;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpSymbol;
import org.junit.*;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JUnit Test for Targeting request processor
 * @author bpatel 
 */
@SuppressWarnings({})
public class TargetingRequestProcessorTest {
    private static CampaignProvider campaignProvider;
    private static boolean          consoleDebug = true;
    private static int sampleAdPodId1;
    private static int sampleAdPodId2;
    private static int sampleAdPodId3;
    private static int sampleAdPodId4;
    private static int sampleAdPodId5;
    private static int sampleAdPodId6;
    private static int sampleAdPodId7;
    private static int sampleAdPodId8;
    
    private static String baseUrlName;
    private static String base2UrlName;

    @BeforeClass
    public static void initialize() throws Exception {
        String factoryClass = null;
        try {
           factoryClass = AppProperties.getInstance().getProperty("cma.factory.impl");
        }
        catch(NullPointerException e) {
            //ignore the exception
        }
        catch(Exception e) {
            //ignore the exception
        }
        if(factoryClass == null || "".equals(factoryClass) || factoryClass.equals("com.tumri.cma.misc.CMAFactoryImpl")) {
            loadData();
            loadUrlData();
        }
        CampaignDBDataLoader loader = CampaignDBDataLoader.getInstance();
        loader.loadData();
    }

    private static void loadData() throws Exception {
        campaignProvider = CMAFactory.getInstance().getCampaignProvider();
        Campaign campaign1 = DomainTestDataProvider.getNewShallowCampaign();
        Campaign campaign2 = DomainTestDataProvider.getNewShallowCampaign();
        Campaign campaign3 = DomainTestDataProvider.getNewShallowCampaign();

        OSpec oSpec1      = DomainTestDataProvider.getNewOSpec();
        OSpec oSpec2      = DomainTestDataProvider.getNewOSpec();
        OSpec oSpec3      = DomainTestDataProvider.getNewOSpec();
        OSpec oSpec4      = DomainTestDataProvider.getNewOSpec();
        OSpec oSpec5      = DomainTestDataProvider.getNewOSpec();
        OSpec oSpec6      = DomainTestDataProvider.getNewOSpec();
        OSpec oSpec7      = DomainTestDataProvider.getNewOSpec();
        OSpec oSpec8      = DomainTestDataProvider.getNewOSpec();

        TSpec tSpec1       = DomainTestDataProvider.getNewTSpec();
        TSpec tSpec2       = DomainTestDataProvider.getNewTSpec();
        TSpec tSpec3       = DomainTestDataProvider.getNewTSpec();
        TSpec tSpec4       = DomainTestDataProvider.getNewTSpec();
        TSpec tSpec5       = DomainTestDataProvider.getNewTSpec();
        TSpec tSpec6       = DomainTestDataProvider.getNewTSpec();
        TSpec tSpec7       = DomainTestDataProvider.getNewTSpec();
        TSpec tSpec8       = DomainTestDataProvider.getNewTSpec();
        oSpec1.addTSpec(tSpec1);
        oSpec2.addTSpec(tSpec2);
        oSpec3.addTSpec(tSpec3);
        oSpec4.addTSpec(tSpec4);
        oSpec5.addTSpec(tSpec5);
        oSpec6.addTSpec(tSpec6);
        oSpec7.addTSpec(tSpec7);
        oSpec8.addTSpec(tSpec8);

        AdPod sampleAdPod1 = DomainTestDataProvider.getNewAdPod();
        AdPod sampleAdPod2 = DomainTestDataProvider.getNewAdPod();
        AdPod sampleAdPod3 = DomainTestDataProvider.getNewAdPod();
        AdPod sampleAdPod4 = DomainTestDataProvider.getNewAdPod();
        AdPod sampleAdPod5 = DomainTestDataProvider.getNewAdPod();
        AdPod sampleAdPod6 = DomainTestDataProvider.getNewAdPod();
        AdPod sampleAdPod7 = DomainTestDataProvider.getNewAdPod();
        AdPod sampleAdPod8 = DomainTestDataProvider.getNewAdPod();

        sampleAdPod1.setOspec(oSpec1);
        sampleAdPod2.setOspec(oSpec2);
        sampleAdPod3.setOspec(oSpec3);
        sampleAdPod4.setOspec(oSpec4);
        sampleAdPod5.setOspec(oSpec5);
        sampleAdPod6.setOspec(oSpec6);
        sampleAdPod7.setOspec(oSpec5);
        sampleAdPod8.setOspec(oSpec6);

//        campaign1.addAdPod(sampleAdPod1);
//        campaign1.addAdPod(sampleAdPod2);
//        campaign1.addAdPod(sampleAdPod3);
//        campaign2.addAdPod(sampleAdPod4);
//        campaign2.addAdPod(sampleAdPod5);
//        campaign2.addAdPod(sampleAdPod6);
//        campaign3.addAdPod(sampleAdPod7);
//        campaign3.addAdPod(sampleAdPod8);


        campaignProvider.createCampaign(campaign1, true);
        campaignProvider.createCampaign(campaign2, true);
        campaignProvider.createCampaign(campaign3, true);

        assertTrue(sampleAdPod1.getId() > 0);
        assertTrue(sampleAdPod2.getId() > 0);
        assertTrue(sampleAdPod5.getId() > 0);
        sampleAdPodId1 = sampleAdPod1.getId();
        sampleAdPodId2 = sampleAdPod2.getId();
        sampleAdPodId3 = sampleAdPod3.getId();
        sampleAdPodId4 = sampleAdPod4.getId();
        sampleAdPodId5 = sampleAdPod5.getId();
        sampleAdPodId6 = sampleAdPod6.getId();
        sampleAdPodId7 = sampleAdPod7.getId();
        sampleAdPodId8 = sampleAdPod8.getId();
    }

    private static void loadUrlData() throws Exception {
        long uniquenessPrefix = System.currentTimeMillis();
        baseUrlName = "http://www."+ uniquenessPrefix + ".yahoo.com";

        {
            Url url = DomainTestDataProvider.getNewShallowUrl();
            url.setName(baseUrlName);
            campaignProvider.createURL(url);

            List<UrlAdPodMapping> adPodIdMappings = new ArrayList<UrlAdPodMapping>();
            adPodIdMappings.add(new UrlAdPodMapping(url.getId(), sampleAdPodId1, 20));
            adPodIdMappings.add(new UrlAdPodMapping(url.getId(), sampleAdPodId2, 50));
            adPodIdMappings.add(new UrlAdPodMapping(url.getId(), sampleAdPodId3, 30));

            campaignProvider.createUrlAdPodMappings(adPodIdMappings);
        }
        {
            Url url = DomainTestDataProvider.getNewShallowUrl();
            url.setName(baseUrlName + "/sports");
            campaignProvider.createURL(url);

            List<UrlAdPodMapping> adPodIdMappings = new ArrayList<UrlAdPodMapping>();
            adPodIdMappings.add(new UrlAdPodMapping(url.getId(), sampleAdPodId1, 30));
            adPodIdMappings.add(new UrlAdPodMapping(url.getId(), sampleAdPodId6, 70));
            campaignProvider.createUrlAdPodMappings(adPodIdMappings);
        }

        {
            Url url = DomainTestDataProvider.getNewShallowUrl();
            url.setName(baseUrlName + "/sports/baseball");
            campaignProvider.createURL(url);

            List<UrlAdPodMapping> adPodIdMappings = new ArrayList<UrlAdPodMapping>();
            adPodIdMappings.add(new UrlAdPodMapping(url.getId(), sampleAdPodId5, 100));
            campaignProvider.createUrlAdPodMappings(adPodIdMappings);
        }

        base2UrlName = "www.sale.com" + "/" + uniquenessPrefix ;

        {
            Url url = DomainTestDataProvider.getNewShallowUrl();
            url.setName(base2UrlName);
            campaignProvider.createURL(url);
            List<UrlAdPodMapping> adPodIdMappings = new ArrayList<UrlAdPodMapping>();
            adPodIdMappings.add(new UrlAdPodMapping(url.getId(), sampleAdPodId7, 25));
            adPodIdMappings.add(new UrlAdPodMapping(url.getId(), sampleAdPodId8, 61));
            campaignProvider.createUrlAdPodMappings(adPodIdMappings);
        }
        {
            Url url = DomainTestDataProvider.getNewShallowUrl();
            url.setName(base2UrlName + "/" + "/toys");
            campaignProvider.createURL(url);

            List<UrlAdPodMapping> adPodIdMappings = new ArrayList<UrlAdPodMapping>();
            adPodIdMappings.add(new UrlAdPodMapping(url.getId(), sampleAdPodId1, 30));
            adPodIdMappings.add(new UrlAdPodMapping(url.getId(), sampleAdPodId6, 70));
            campaignProvider.createUrlAdPodMappings(adPodIdMappings);
        }

        {
            Url url = DomainTestDataProvider.getNewShallowUrl();
            url.setName(base2UrlName  + "/toys/electronics");
            campaignProvider.createURL(url);

            List<UrlAdPodMapping> adPodIdMappings = new ArrayList<UrlAdPodMapping>();
            adPodIdMappings.add(new UrlAdPodMapping(url.getId(), sampleAdPodId4, 100));
            campaignProvider.createUrlAdPodMappings(adPodIdMappings);
        }
    }

    @Test
    public void testUrlCountryGeoGetAdData() {
        try {
            String queryStr = "(get-ad-data :country-name \"USA\"" + ":url \"http://consumersearch.com/www/electronics/gps\"" + ")";
            Recipe oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
            System.out.println(oSpec.getId() + " : " +oSpec.getName());
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test
    public void testCountryGeoGetAdData() {
        try {
            String queryStr = "(get-ad-data :country-name \"USA\"" + ":url \"http://www.photo.net/bboard/\"" + ")";
            Recipe oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
            System.out.println(oSpec.getId() + " : " +oSpec.getName());
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test
    public void testRegionGeoGetAdData() {
        try {
            String queryStr = "(get-ad-data :region \"KS\"" + ":url \"http://products.howstuffworks.com/computers-buying-guides.htm\"" + ")";
            Recipe oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
            System.out.println(oSpec.getId() + " : " +oSpec.getName());
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test
    public void testCityGeoGetAdData() {
        try {
            String queryStr = "(get-ad-data :city \"Los Angeles\"" + ":url \"http://www.consumersearch.com/www/kitchen/toaster-ovens/\"" + ")";
            Recipe oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
            System.out.println(oSpec.getId() + " : " +oSpec.getName());
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test
    public void testZipcodeGeoGetAdData() {
        try {
            String queryStr = "(get-ad-data :zip-code \"94404\"" + ":url \"http://www.consumersearch.com/www/kitchen/toaster-ovens/\"" + ")";
            Recipe oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
            System.out.println(oSpec.getId() + " : " +oSpec.getName());
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test
    public void testAreacodeGeoGetAdData() {
        try {
            String queryStr = "(get-ad-data :area-code \"650\"" + ":url \"" +  base2UrlName + "/toys/educational/\"" + ")";
            Recipe oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
            System.out.println(oSpec.getId() + " : " +oSpec.getName());
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test
    public void testDmacodeGeoGetAdData() {
        try {
            String queryStr = "(get-ad-data :dma \"DMA1\"" + ")";
            Recipe oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
            System.out.println(oSpec.getId() + " : " +oSpec.getName());
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test
    public void testThemeGetAdData() {
        try {
            String queryStr = "(get-ad-data :theme \"ecpm-test1-laptops\")";
            Recipe oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
            System.out.println(oSpec.getId() + " : " +oSpec.getName());
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test
    public void testUrlGetAdData() {
        try {
            String queryStr = "(get-ad-data :url \" http://consumersearch.com/www/electronics/gps\")";
            Recipe oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
            System.out.println(oSpec.getId() + " : " +oSpec.getName());
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test   
    public void testStoreGetAdData() {
        try {
            String queryStr = "(get-ad-data :store-ID \"11364\")";
            Recipe oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
            System.out.println(oSpec.getId() + " : " +oSpec.getName());
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test
    public void testGetAdDataForTSpecName() {
        try {
            String queryStr = "(get-ad-data :t-spec \"|tspecname|\")";
            Recipe oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
            System.out.println(oSpec.getId() + " : " +oSpec.getName());
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }
    
    @Test
    public void testGetAdDataForTSpecNameNoDefaultRealm() {
        try {
            String queryStr = "(get-ad-data :t-spec \"|tspecname|\")";
            Recipe oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test
    public void testUrl2GetAdData() {
        try {
            String queryStr = "(get-ad-data :url \" http://products.howstuffworks.com/computers-buying-guides.htm\")";
            Recipe oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
            System.out.println(oSpec.getId() + " : " +oSpec.getName());
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test
    public void testUrl3GetAdData() {
        try {
            String queryStr = "(get-ad-data :url \" http://www.consumersearch.com/www/kitchen/toaster-ovens/\")";
            Recipe oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
            System.out.println(oSpec.getId() + " : " +oSpec.getName());
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test
    public void testThemeGetAdDataWithDefaultRealmSet() {
        try {
            String queryStr = "(get-ad-data :theme \" wnd-mens-apparel-shoes\" :revert-to-default-realm t)";
            Recipe oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
            System.out.println(oSpec.getId() + " : " +oSpec.getName());
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test
	public void testDefaultRealm() {
		try {
			String queryStr = "(get-ad-data :url \"http://default-realm/\")";
			Recipe oSpec = testProcessRequest(queryStr);
			assertNotNull(oSpec);
		} catch(Exception e){
			e.printStackTrace();
            fail("Error occured" + e.getMessage());
		}
	}

    private Recipe testProcessRequest(String getAdDataCommandStr) throws Exception {
        TargetingRequestProcessor processor = TargetingRequestProcessor.getInstance();
        Reader r = new StringReader(getAdDataCommandStr);
        SexpReader lr = new SexpReader (r);
        Recipe recipe = null;
        try {
            Sexp e = lr.read ();
            SexpList l = e.toSexpList ();
            Sexp cmd_expr = l.getFirst ();
            if (! cmd_expr.isSexpSymbol ()) {
                fail("command name not a symbol: " + cmd_expr.toString ());
            }
            SexpSymbol sym = cmd_expr.toSexpSymbol();
            String cmd_name = sym.toString ();
//            String lval = l.toStringValue();
//            List<String> keywords = Arrays.asList(lval.split(" "));
//            System.out.println(keywords.toString());
             // Return the; right Cmd* class to handle this request.

            if (cmd_name.equals ("get-ad-data")) {
//                AdDataRequest rqst = null; //new AdDataRequest (e);
//                TargetingResults trs = null; 
//                Recipe r1 = null; ///processor.processRequest(rqst, f);
                //Assert.assertNotNull(r1);
                JozAdRequest jozRequest = new JozAdRequest();
                jozRequest.setValue(JozAdRequest.KEY_LOCATION_ID, "109228");
                jozRequest.setValue(JozAdRequest.KEY_REGION, "CA");
                jozRequest.setValue(JozAdRequest.KEY_USER_BUCKET, "99");
                AdDataRequest request = new AdDataRequest(jozRequest);
                Features f = new Features();
                TargetingResults trs = processor.processRequest(request, f);
                recipe= trs.getRecipe();
                
            } else {
                fail("The request could not be parsed correctly");
            }
        } catch(Exception e) {
            throw e;
        }

        return recipe;

    }

    private void printStackTrace(Exception e) {
        if(consoleDebug) { e.printStackTrace(); }
    }
}
