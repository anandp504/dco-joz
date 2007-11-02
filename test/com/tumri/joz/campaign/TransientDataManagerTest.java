package com.tumri.joz.campaign;

import org.junit.*;
import static org.junit.Assert.*;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpSymbol;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.cma.domain.OSpec;
import com.tumri.joz.targeting.TargetingRequestProcessor;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.bugfix.TransientDataTestUtil;

import java.io.Reader;
import java.io.StringReader;

/**
 * Unit test case for TransientDataManager
 *
 * @author bpatel
 */
public class TransientDataManagerTest {
    private static String sampleTSpecName  = "TPP A1 " + System.currentTimeMillis();
    private static String sampleTSpecName2 = "TPP A2 " + System.currentTimeMillis();
    private static String sampleTSpecName3 = "TPP A3 " + System.currentTimeMillis();
    private static String sampleTSpecName4 = "TPP A4 " + System.currentTimeMillis();
    private static String sampleGeoTSpecName = "Geo-TSPEC 1 " + System.currentTimeMillis();

    private static String sampleDeleteIncorpMappingDeltaStr = "(incorp-mapping-deltas '((:delete (:realm \"http://www.imd.com/sample\")  nil  nil |" + sampleTSpecName3 + "| 2.0f0 1190503807055)))";

    @BeforeClass
    public static void initialize() throws Exception {
        CampaignDBDataLoader loader = CampaignDBDataLoader.getInstance();
        loader.loadData();
    }

    @Test
    public void testTspecAdd() {
    	String tSpecAddStr = "(t-spec-add :name ' |" + sampleTSpecName + "| :version -1 :include-categories '(|GLASSVIEW.TUMRI_14215| |GLASSVIEW.TUMRI_14209| |GLASSVIEW.TUMRI_14208| |GLASSVIEW.TUMRI_14214| |GLASSVIEW.TUMRI_14217| |GLASSVIEW.TUMRI_14227| ) :ref-price-constraints '(10.0 146.0) )";
        String tSpecAddStr2 = "(t-spec-add :name ' |" + sampleTSpecName2 + "| :version -1 :include-categories '(|GLASSVIEW.TUMRI_14215| |GLASSVIEW.TUMRI_14209| |GLASSVIEW.TUMRI_14208| |GLASSVIEW.TUMRI_14214| |GLASSVIEW.TUMRI_14217| |GLASSVIEW.TUMRI_14227| ) :ref-price-constraints '(10.0 146.0) )";
        String tSpecAddStr3 = "(t-spec-add :name ' |" + sampleTSpecName + "| :version -1 :include-categories '(|GLASSVIEW.TUMRI_99999| |GLASSVIEW.TUMRI_14209| |GLASSVIEW.TUMRI_14208| |GLASSVIEW.TUMRI_14214| |GLASSVIEW.TUMRI_14217| |GLASSVIEW.TUMRI_14227| ) :ref-price-constraints '(10.0 146.0) )";
        String tSpecAddStr4 = "(t-spec-add :name ' |" + sampleTSpecName4 + "| :version -1 :include-categories '(|GLASSVIEW.TUMRI_14215| |GLASSVIEW.TUMRI_14209| |GLASSVIEW.TUMRI_14208| |GLASSVIEW.TUMRI_14214| |GLASSVIEW.TUMRI_14217| |GLASSVIEW.TUMRI_14227| ) :ref-price-constraints '(10.0 146.0) )";
        String tSpecAddStr5 = "(t-spec-add :name ' |" + sampleGeoTSpecName + "| :version -1 :include-categories '(|GLASSVIEW.TUMRI_14215| |GLASSVIEW.TUMRI_14209| |GLASSVIEW.TUMRI_14208| |GLASSVIEW.TUMRI_14214| |GLASSVIEW.TUMRI_14217| |GLASSVIEW.TUMRI_14227| ) :ref-price-constraints '(10.0 146.0) )";

        String[] tspecNameArray = {sampleTSpecName, sampleTSpecName2, sampleTSpecName, sampleTSpecName4, sampleGeoTSpecName};
        String[] strArray = {tSpecAddStr, tSpecAddStr2, tSpecAddStr3, tSpecAddStr4, tSpecAddStr5};

        for(int i=0; i<strArray.length; i++) {
            TransientDataTestUtil.addNewTSpec(tspecNameArray[i]);
            String tSpecGetStr = "(get-ad-data :t-spec '|" + tspecNameArray[i] + "| :num-products 12 :ad-offer-type :product-leadgen)";
            TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetStr, tspecNameArray[i]);
        }
    }

    @Test
    public void testgetTSpecIncorpMappingDeltas1(){
        String[] urlArray = {"http://www.incorp-mapping-delta.com/test", "http://www.incorp-mapping-delta.com/test", "http://www.imd.com/sample", "http://www.sample4.com/sample"};

        for (String anUrlArray : urlArray) {
            String tSpecGetStr = "(get-ad-data :url \" " + anUrlArray + "\")";
            OSpec oSpec = null;
            try {
                oSpec = testTargetingRequest(tSpecGetStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            assertNotNull(oSpec);
        }
    }

    @Test
    public void testAddTSpecIncorpMappingDeltas1(){
    	String mappingStr = "(incorp-mapping-deltas '((:add (:realm \"http://www.incorp-mapping-delta.com/test\")  nil  nil |" + sampleTSpecName + "| 1.0f0 1190503807055)))";
        String mappingStr2 = "(incorp-mapping-deltas '((:add (:realm \"http://www.incorp-mapping-delta.com/test\")  nil  nil |" + sampleTSpecName2 + "| 2.0f0 1190503807055)))";
        String[] strArray = {mappingStr, mappingStr2};
        String[] urlArray = {"http://www.incorp-mapping-delta.com/test", "http://www.incorp-mapping-delta.com/test"};
        String[] tspecNameArray = {sampleTSpecName, sampleTSpecName2};

        for(int i=0; i<strArray.length; i++) {
            TransientDataTestUtil.addNewMapping(strArray[i]);
            String tSpecGetStr = "(get-ad-data :url \" " + urlArray[i]+ "\")";
            TransientDataTestUtil.validateOSpecResultforGetAdDataRequest(tSpecGetStr, tspecNameArray);
        }
    }

    @Test
    public void testAddTSpecIncorpMappingDeltasWithGeocode(){
    	String mappingStr = "(incorp-mapping-deltas '((:add (:realm \"http://www.geo-incorp-test-url.com/test\") ((:zip \"91234\")) nil |" + sampleGeoTSpecName + "| 1.0f0 1190503807055)))";
        String mappingStr2 = "(incorp-mapping-deltas '((:add (:realm \"http://www.geo-incorp-test-url.com/test\")  nil  nil |" + sampleTSpecName2 + "| 2.0f0 1190503807055)))";
        String[] strArray = {mappingStr, mappingStr2};
        String[] tspecNameArray = {sampleGeoTSpecName, sampleTSpecName2};
        for (String aStrArray : strArray) {
            TransientDataTestUtil.addNewMapping(aStrArray);
        }
        try {
            String tSpecGetStr = "(get-ad-data :zip-code \"91234\" :url \"http://www.geo-incorp-test-url.com/test\")";
            String tSpecGetStr2 = "(get-ad-data :url \"http://www.geo-incorp-test-url.com/test\")";
            TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetStr, tspecNameArray[0]);
            TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetStr2, tspecNameArray[1]);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testDeleteTSpecIncorpMappingDeltas(){
        TransientDataTestUtil.deleteIncorpDeltaMapping(sampleDeleteIncorpMappingDeltaStr);
        String tSpecGetStr = "(get-ad-data :url \" http://www.imd.com/sample\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetStr, null);
    }


    @Test
    public void testTSpecDelete() {
        TransientDataManager.getInstance().deleteOSpec(sampleTSpecName4);
        try {
            String tSpecGetStr = "(get-ad-data :url \" http://www.sample4.com/sample\")";
            OSpec oSpec = testTargetingRequest(tSpecGetStr);
            assertNotNull(oSpec);
            assertFalse(oSpec.getName().equals(sampleTSpecName4));
        }
        catch(TransientDataException e) {
            e.printStackTrace();
            fail("Error occured while adding incorp-mapping-delta" + e.getMessage());
        }
        catch(Exception e) {
            e.printStackTrace();
            fail("Error occured while retrieving tspec" + e.getMessage());
        }
    }

    @Test
    public void testUrlMappingDelete() {
        //1. Create new T-Spec
        String tSpecName = "sampleTestTSpec";
        TransientDataTestUtil.addNewTSpec(tSpecName);

        //2. Check if the tspec is successfully created
        String tSpecGetStr = "(get-ad-data :t-spec '|" + tSpecName + "| :num-products 12 :ad-offer-type :product-leadgen)";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetStr, tSpecName);

        //3. Add new mapping with geo location
        String mappingWithGeoStr = "(incorp-mapping-deltas '((:add (:realm \"http://test-url.com\")  ((:zip \"99005\"))  nil |" + tSpecName + "| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(mappingWithGeoStr);

        //4. Make a get-ad-data request for url using geo
        String tSpecGetGeoStr = "(get-ad-data :zip-code \"99005\" :url \"http://test-url.com\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, tSpecName);

        //5. Make a get-ad-data request for url without geo
        String tSpecGetNonGeoStr = "(get-ad-data :url \"http://test-url.com\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetNonGeoStr, null);

        //6. Add new mapping without geo location
        String mappingWithoutGeoStr = "(incorp-mapping-deltas '((:add (:realm \"http://test-url.com\")  nil  nil |" + tSpecName + "| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(mappingWithoutGeoStr);

        //7. Make a get-ad-data request for url without geo
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetNonGeoStr, tSpecName);

        //8. Delete the url mapping
        String themeDeleteIncorpMappingStr = "(incorp-mapping-deltas '((:delete (:realm \"http://test-url.com\")  nil nil |" + tSpecName + "| 1.0f0 31230170)))";
        TransientDataTestUtil.deleteIncorpDeltaMapping(themeDeleteIncorpMappingStr);

        //9. Make a get-ad-data request for url without geo
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetNonGeoStr, null);

        //10. Make a get-ad-data request for url using geo
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, tSpecName);

        //11. Delete the url mapping with geo
        String themeDeleteIncorpMappingWithGeoStr = "(incorp-mapping-deltas '((:delete (:realm \"http://test-url.com\")  ((:zip \"99005\")) nil |" + tSpecName + "| 1.0f0 31230170)))";
        TransientDataTestUtil.deleteIncorpDeltaMapping(themeDeleteIncorpMappingWithGeoStr);

        //12. Make a get-ad-data request for url using geo
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, null);

        //13. Add new mapping with geo location
        String mappingWithNewGeoStr = "(incorp-mapping-deltas '((:add (:realm \"http://test-url.com\")  ((:zip \"11011\"))  nil |" + tSpecName + "| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(mappingWithNewGeoStr);

        //14. Make a get-ad-data request for url using geo
        String tSpecGetNewGeoStr = "(get-ad-data :zip-code \"11011\" :url \"http://test-url.com\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetNewGeoStr, tSpecName);
    }

    private OSpec testTargetingRequest(String getAdDataCommandStr) throws Exception {
        TargetingRequestProcessor processor = TargetingRequestProcessor.getInstance();
        Reader r = new StringReader(getAdDataCommandStr);
        SexpReader lr = new SexpReader (r);
        OSpec oSpec = null;
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

        } else {
            fail("The request could not be parsed correctly");
        }

        return oSpec;

    }

    
}
