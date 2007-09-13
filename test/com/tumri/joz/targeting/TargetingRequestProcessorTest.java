package com.tumri.joz.targeting;

import static org.junit.Assert.*;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.productselection.ProductSelectionResults;
import com.tumri.joz.campaign.CampaignDBDataLoader;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpSymbol;
import com.tumri.cma.domain.*;
import com.tumri.cma.service.CampaignProvider;
import com.tumri.cma.CMAFactory;
import com.tumri.cma.DomainTestDataProvider;

import java.util.SortedSet;
import java.util.List;
import java.util.ArrayList;
import java.io.Reader;
import java.io.StringReader;

import org.junit.*;

/**
 * Created by IntelliJ IDEA.
 * User: bpatel
 * Date: Sep 8, 2007
 * Time: 10:40:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class TargetingRequestProcessorTest {
    private static CampaignProvider campaignProvider;
    private static boolean          consoleDebug = true;
    private static int sampleAdPodId1;
    private static int sampleAdPodId2;
    private static int sampleAdPodId3;
    private static int sampleAdPodId4;
    private static int sampleAdPodId5;
    private static int sampleAdPodId6;

    private static int sampleThemeId;
    private static String sampleThemeName;
    private static long uniquenessPrefix;
    private static String baseUrlName;

    private static CampaignDBDataLoader loader;
    @BeforeClass
    public static void initialize() throws Exception {
        loadData();
        loadThemeData();
        loadUrlData();
        loader = CampaignDBDataLoader.getInstance();
        loader.loadData();

    }

    private static void loadThemeData() throws Exception {
        Theme theme = DomainTestDataProvider.getNewShallowTheme();
        campaignProvider.createTheme(theme);
        Theme retrievedTheme = campaignProvider.getTheme(theme.getId());
        assertNotNull(retrievedTheme);
        sampleThemeId = theme.getId();
        sampleThemeName = theme.getName();
        List<ThemeAdPodMapping> adPodIdMappings = new ArrayList<ThemeAdPodMapping>();
        adPodIdMappings.add(new ThemeAdPodMapping(sampleThemeId, sampleAdPodId4, 50));
        adPodIdMappings.add(new ThemeAdPodMapping(sampleThemeId, sampleAdPodId5, 50));
        campaignProvider.createThemeAdPodMappings(adPodIdMappings);

    }

    private static void loadData() throws Exception {
        campaignProvider = CMAFactory.getInstance().getCampaignProvider();
        Campaign campaign1 = DomainTestDataProvider.getNewShallowCampaign();
        Campaign campaign2 = DomainTestDataProvider.getNewShallowCampaign();
        OSpec oSpec1       = DomainTestDataProvider.getNewOSpec();
        OSpec oSpec2      = DomainTestDataProvider.getNewOSpec();
        OSpec oSpec3      = DomainTestDataProvider.getNewOSpec();
        OSpec oSpec4      = DomainTestDataProvider.getNewOSpec();
        OSpec oSpec5      = DomainTestDataProvider.getNewOSpec();
        OSpec oSpec6      = DomainTestDataProvider.getNewOSpec();
        TSpec tSpec1       = DomainTestDataProvider.getNewTSpec();
        TSpec tSpec2       = DomainTestDataProvider.getNewTSpec();
        TSpec tSpec3       = DomainTestDataProvider.getNewTSpec();
        TSpec tSpec4       = DomainTestDataProvider.getNewTSpec();
        TSpec tSpec5       = DomainTestDataProvider.getNewTSpec();
        TSpec tSpec6       = DomainTestDataProvider.getNewTSpec();
        oSpec1.addTSpec(tSpec1);
        oSpec2.addTSpec(tSpec2);
        oSpec3.addTSpec(tSpec3);
        oSpec4.addTSpec(tSpec4);
        oSpec5.addTSpec(tSpec5);
        oSpec6.addTSpec(tSpec6);
        AdPod sampleAdPod1 = DomainTestDataProvider.getNewAdPod();
        AdPod sampleAdPod2 = DomainTestDataProvider.getNewAdPod();
        AdPod sampleAdPod3 = DomainTestDataProvider.getNewAdPod();
        AdPod sampleAdPod4 = DomainTestDataProvider.getNewAdPod();
        AdPod sampleAdPod5 = DomainTestDataProvider.getNewAdPod();
        AdPod sampleAdPod6 = DomainTestDataProvider.getNewAdPod();
        sampleAdPod1.setOspec(oSpec1);
        sampleAdPod2.setOspec(oSpec2);
        sampleAdPod3.setOspec(oSpec3);
        sampleAdPod4.setOspec(oSpec4);
        sampleAdPod5.setOspec(oSpec5);
        sampleAdPod6.setOspec(oSpec6);
        campaign1.addAdPod(sampleAdPod1);
        campaign1.addAdPod(sampleAdPod2);
        campaign1.addAdPod(sampleAdPod3);
        campaign2.addAdPod(sampleAdPod4);
        campaign2.addAdPod(sampleAdPod5);
        campaign2.addAdPod(sampleAdPod6);

        campaignProvider.createCampaign(campaign1, true);
        campaignProvider.createCampaign(campaign2, true);
        assertTrue(sampleAdPod1.getId() > 0);
        assertTrue(sampleAdPod2.getId() > 0);
        assertTrue(sampleAdPod5.getId() > 0);
        sampleAdPodId1 = sampleAdPod1.getId();
        sampleAdPodId2 = sampleAdPod2.getId();
        sampleAdPodId3 = sampleAdPod3.getId();
        sampleAdPodId4 = sampleAdPod4.getId();
        sampleAdPodId5 = sampleAdPod5.getId();
        sampleAdPodId6 = sampleAdPod6.getId();

    }

    private static void loadUrlData() throws Exception {
        uniquenessPrefix = System.currentTimeMillis();
        baseUrlName = "http://www.yahoo.com" + uniquenessPrefix;
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

    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testUrlCountryGeoGetAdData() {
        try {
            String queryStr = "(get-ad-data :country-name \"USA\" \"UK\"" + ":url \"" +  baseUrlName + "/sports/cricket\"" + ")";
            OSpec oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test
    public void testCountryGeoGetAdData() {
        try {
            String queryStr = "(get-ad-data :country-name \"USA\" \"UK\"" + ":url \"" +  baseUrlName + "/sports/cricket\"" + ")";
            OSpec oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test
    public void testThemeGetAdData() {
        try {
            String queryStr = "(get-ad-data :theme \"" + sampleThemeName + "\")";
            OSpec oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test
    public void testUrlGetAdData() {
        try {
            String queryStr = "(get-ad-data :url \"" +  baseUrlName + "/sports/cricket\")";
            OSpec oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    @Test   
    public void testStoreGetAdData() {
        try {
            String queryStr = "(get-ad-data :store-ID \"11\")";
            OSpec oSpec = testProcessRequest(queryStr);
            Assert.assertNotNull(oSpec);
        } catch(Exception e){
            printStackTrace(e);
            fail("Exception caught during test run");
        }
    }

    private OSpec testProcessRequest(String getAdDataCommandStr) throws Exception {
        TargetingRequestProcessor processor = TargetingRequestProcessor.getInstance();
        Reader r = new StringReader(getAdDataCommandStr);
        SortedSet<Handle> results = null;
        SexpReader lr = new SexpReader (r);
        OSpec oSpec = null;
        try {
            Sexp e = lr.read ();
            SexpList l = e.toSexpList ();
            Sexp cmd_expr = l.getFirst ();
            if (! cmd_expr.isSexpSymbol ()) {
                fail("command name not a symbol: " + cmd_expr.toString ());
            }
            SexpSymbol sym = cmd_expr.toSexpSymbol ();
            String cmd_name = sym.toString ();

            // Return the right Cmd* class to handle this request.

            if (cmd_name.equals ("get-ad-data")) {
                AdDataRequest rqst = new AdDataRequest (e);
                
                oSpec = processor.processRequest(rqst);
                Assert.assertNotNull(oSpec);

            } else {
                fail("The request could not be parsed correctly");
            }
        } catch(Exception e) {
            throw e;
        }

        return oSpec;

    }

    private void printStackTrace(Exception e) {
        if(consoleDebug) { e.printStackTrace(); }
    }
}
