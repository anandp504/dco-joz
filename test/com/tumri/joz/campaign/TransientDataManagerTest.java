package com.tumri.joz.campaign;

import org.junit.*;
import org.junit.Test;
import static org.junit.Assert.*;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpSymbol;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.cma.domain.*;
import com.tumri.joz.targeting.TargetingRequestProcessor;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.bugfix.TransientDataTestUtil;
import com.tumri.joz.JoZException;

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

    //@Test
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

    //@Test
    /**
     * This test will return null for results since we have no mappings added yet.
     */
    public void testTargetingWithInvalidMapping(){
        String[] urlArray = {"http://www.incorp-mapping-delta.com/test", "http://www.incorp-mapping-delta.com/test", "http://www.imd.com/sample", "http://www.sample4.com/sample"};

        for (String anUrlArray : urlArray) {
            String tSpecGetStr = "(get-ad-data :url \" " + anUrlArray + "\")";
            Recipe recipe = null;
            try {
                recipe = testTargetingRequest(tSpecGetStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            assertNull(recipe);
        }
    }

    //@Test
    /**
     * This test should not return any targeted Recipes since URL only targeting is not supported in Joz
     */
    public void testUrlBasedTargeting(){
    	String mappingStr = "(incorp-mapping-deltas '((:add (:realm \"http://www.incorp-mapping-delta.com/test\")  nil  nil |" + sampleTSpecName + "| 1.0f0 1190503807055)))";
        String mappingStr2 = "(incorp-mapping-deltas '((:add (:realm \"http://www.incorp-mapping-delta.com/test\")  nil  nil |" + sampleTSpecName2 + "| 2.0f0 1190503807055)))";
        String[] strArray = {mappingStr, mappingStr2};
        String[] urlArray = {"http://www.incorp-mapping-delta.com/test", "http://www.incorp-mapping-delta.com/test"};

        for(int i=0; i<strArray.length; i++) {
            TransientDataTestUtil.addNewMapping(strArray[i]);
            String tSpecGetStr = "(get-ad-data :url \" " + urlArray[i]+ "\")";
            //TransientDataTestUtil.validateOSpecResultforGetAdDataRequest(tSpecGetStr, tspecNameArray);
            Recipe recipe = null;
            try {
                recipe = testTargetingRequest(tSpecGetStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            assertNull(recipe);
        }
    }

    //@Test
    public void testThemeBasedTargeting(){
    	String mappingStr = "(incorp-mapping-deltas '((:add (:theme \"test-theme1\")  nil  nil |" + sampleTSpecName + "| 1.0f0 1190503807055)))";
        String[] strArray = {mappingStr};
        String[] themeArray = {"test-theme1"};
        String[] tspecNameArray = {sampleTSpecName};

        for(int i=0; i<strArray.length; i++) {
            TransientDataTestUtil.addNewMapping(strArray[i]);
            String tSpecGetStr = "(get-ad-data :theme \"" + themeArray[i]+ "\")";
            TransientDataTestUtil.validateOSpecResultforGetAdDataRequest(tSpecGetStr, tspecNameArray);
        }
    }

    //@Test
    public void testLocationBasedTargeting(){
        String mappingStr = "(incorp-mapping-deltas '((:add (:store-id \"123456\")  nil  nil |" + sampleTSpecName + "| 1.0f0 1190503807055)))";
        String[] strArray = {mappingStr};
        String[] storeIdArray = {"123456"};
        String[] tspecNameArray = {sampleTSpecName};

        for(int i=0; i<strArray.length; i++) {
            TransientDataTestUtil.addNewMapping(strArray[i]);
            String tSpecGetStr = "(get-ad-data :store-ID \"" + storeIdArray[i]+ "\")";
            TransientDataTestUtil.validateOSpecResultforGetAdDataRequest(tSpecGetStr, tspecNameArray);
        }
    }

    //@Test
    public void testLocationUrlBasedTargeting(){
        String mappingStr = "(incorp-mapping-deltas '((:add (:realm \"http://www.testurl.com\")  nil  nil |" + sampleTSpecName + "| 1.0f0 1190503807055)))";
        String[] strArray = {mappingStr};
        String[] urlArray = {"http://www.testurl.com"};
        String[] tspecNameArray = {sampleTSpecName};

        for(int i=0; i<strArray.length; i++) {
            TransientDataTestUtil.addNewMapping(strArray[i]);
            String tSpecGetStr = "(get-ad-data :store-ID \"123456\" :url \"" + urlArray[i]+ "\")";
            TransientDataTestUtil.validateOSpecResultforGetAdDataRequest(tSpecGetStr, tspecNameArray);
        }
    }

    //@Test
    public void testThemeLocationBasedTargetingWithGeo(){
    	String mappingStr = "(incorp-mapping-deltas '((:add (:theme \"geotheme1\") ((:zip \"91234\")) nil |" + sampleGeoTSpecName + "| 1.0f0 1190503807055)))";
        String mappingStr2 = "(incorp-mapping-deltas '((:add (:store-id \"22222\") ((:zip \"94065\")) nil |" + sampleTSpecName2 + "| 2.0f0 1190503807055)))";
        String[] strArray = {mappingStr, mappingStr2};
        String[] tspecNameArray = {sampleGeoTSpecName, sampleTSpecName2};
        for (String aStrArray : strArray) {
            TransientDataTestUtil.addNewMapping(aStrArray);
        }
        try {
            String tSpecGetStr = "(get-ad-data :zip-code \"91234\" :theme \"geotheme1\")";
            String tSpecGetStr2 = "(get-ad-data :zip-code \"94065\" :store-ID \"22222\")";
            TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetStr, tspecNameArray[0]);
            TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetStr2, tspecNameArray[1]);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

    //@Test
    public void testDeleteTSpecIncorpMappingDeltas(){
        TransientDataTestUtil.deleteIncorpDeltaMapping(sampleDeleteIncorpMappingDeltaStr);
        String tSpecGetStr = "(get-ad-data :url \" http://www.imd.com/sample\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetStr, null);
    }


    //@Test
    public void testTSpecDelete() {
        TransientDataManager.getInstance().deleteOSpec(sampleTSpecName4);
        try {
            String tSpecGetStr = "(get-ad-data :t-spec '|" + sampleTSpecName4 + "|)";
            Recipe r = testTargetingRequest(tSpecGetStr);
            assertNull(r);
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
    public void testFullTransientData() {
        //1. Create new T-Spec
        String tSpecName = "sampleTestTSpec";
        TransientDataTestUtil.addNewTSpec(tSpecName);

        //2. Check if the tspec is successfully created
        String tSpecGetStr = "(get-ad-data :t-spec '|" + tSpecName + "| :num-products 12 :ad-offer-type :product-leadgen)";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetStr, tSpecName);

        //3. Add new mapping with geo location
        String mappingWithGeoStr = "(incorp-mapping-deltas '((:add (:theme \"test-url.com\")  ((:zip \"99005\"))  nil |" + tSpecName + "| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(mappingWithGeoStr);

        //4. Make a get-ad-data request for url using geo
        String tSpecGetGeoStr = "(get-ad-data :zip-code \"99005\" :theme \"test-url.com\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, tSpecName);

        //5. Make a get-ad-data request for url without geo
        String tSpecGetNonGeoStr = "(get-ad-data :theme \"test-url.com\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetNonGeoStr, null);

        //6. Add new mapping without geo location
        String mappingWithoutGeoStr = "(incorp-mapping-deltas '((:add (:theme \"test-url.com\")  nil  nil |" + tSpecName + "| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(mappingWithoutGeoStr);

        //7. Make a get-ad-data request for url without geo
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetNonGeoStr, tSpecName);

        //7a. Validate that the tspec is selected without Geo also
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, tSpecName);

        //8. Delete the url mapping
        String themeDeleteIncorpMappingStr = "(incorp-mapping-deltas '((:delete (:theme \"test-url.com\")  nil nil |" + tSpecName + "| 1.0f0 31230170)))";
        TransientDataTestUtil.deleteIncorpDeltaMapping(themeDeleteIncorpMappingStr);

        //9. Make a get-ad-data request for url without geo
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetNonGeoStr, null);

        //10. Make a get-ad-data request for url using geo
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, tSpecName);

        //11. Delete the url mapping with geo
        String themeDeleteIncorpMappingWithGeoStr = "(incorp-mapping-deltas '((:delete (:theme \"test-url.com\")  ((:zip \"99005\")) nil |" + tSpecName + "| 1.0f0 31230170)))";
        TransientDataTestUtil.deleteIncorpDeltaMapping(themeDeleteIncorpMappingWithGeoStr);

        //12. Make a get-ad-data request for url using geo
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, null);

        //13. Add new mapping with geo location
        String mappingWithNewGeoStr = "(incorp-mapping-deltas '((:add (:theme \"test-url.com\")  ((:zip \"11011\"))  nil |" + tSpecName + "| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(mappingWithNewGeoStr);

        //14. Make a get-ad-data request for url using geo
        String tSpecGetNewGeoStr = "(get-ad-data :zip-code \"11011\" :theme \"test-url.com\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetNewGeoStr, tSpecName);
    }

    @Test
    public void testAddCampaign() {
        Campaign testCampaign = new Campaign();
        testCampaign.setId(101);
        testCampaign.setName("testCampaign");
        AdGroup testGroup = new AdGroup();
        testGroup.setId(102);
        AdPod testAdPod = new AdPod();
        Recipe testRecipe = new Recipe();
        testRecipe.setId(103);
        testRecipe.setName("testRecipe");
        TSpec testTSpec = new TSpec();
        testTSpec.setId(106);
        OSpec testOSpec = new OSpec();
        testOSpec.setId(105);
        testOSpec.setName("testOSpec");
        testOSpec.addTSpec(testTSpec);
        testAdPod.setOspec(testOSpec);
        RecipeTSpecInfo tInfo = new RecipeTSpecInfo();
        tInfo.setTspecId(106);
        tInfo.setSlotId("1");
        tInfo.setNumProducts(12);
        testRecipe.addTSpecInfo(tInfo);
        UIProperty testProperty = new UIProperty("cta","testtest","id");
        testProperty.setId(104);
        testRecipe.addProperty(testProperty);
        testAdPod.addRecipe(testRecipe);
        testGroup.addAdPod(testAdPod);
        testCampaign.addAdGroup(testGroup);
        try {
            TransientDataManager.getInstance().addCampaign(testCampaign);
        } catch (JoZException e) {
            fail("Exception caught during campaign add");
        }

        String tSpecRecipeAdDataStr = "(get-ad-data :recipe-id 103)";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecRecipeAdDataStr, "testRecipe");


        //Now delete the campaign and check if the recipe id returned or not

	    try {
		    TransientDataManager.getInstance().deleteCampaign(101);
		    TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecRecipeAdDataStr, null);
	    } catch (JoZException e) {
		    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
	    }


    }
    
    private Recipe testTargetingRequest(String getAdDataCommandStr) throws Exception {
        TargetingRequestProcessor processor = TargetingRequestProcessor.getInstance();
        Reader r = new StringReader(getAdDataCommandStr);
        SexpReader lr = new SexpReader (r);
        Recipe recipe = null;
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

            recipe = processor.processRequest(rqst, new Features());

        } else {
            fail("The request could not be parsed correctly");
        }

        return recipe;

    }

    
}
