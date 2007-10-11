package com.tumri.joz.campaign;

import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpSymbol;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.cma.domain.OSpec;
import com.tumri.joz.targeting.TargetingRequestProcessor;
import com.tumri.joz.jozMain.AdDataRequest;

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

    private static String sample4AddIncorpMappingDeltaStr = "(incorp-mapping-deltas '((:add (:realm \"http://www.sample4.com/sample\")  nil  nil |" + sampleTSpecName4 + "| 2.0f0 1190503807055)))";
    private static String sampleAddIncorpMappingDeltaStr = "(incorp-mapping-deltas '((:add (:realm \"http://www.imd.com/sample\")  nil  nil |" + sampleTSpecName3 + "| 2.0f0 1190503807055)))";
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
            Sexp tspecMappingSexp = testProcessRequest(strArray[i]);
            if (tspecMappingSexp!=null){
                SexpList tspecAddSpecList = tspecMappingSexp.toSexpList();
                Sexp cmd_expr = tspecAddSpecList.getFirst ();
                if (! cmd_expr.isSexpSymbol ()) {
                    fail("command name not a symbol: " + cmd_expr.toString ());
                }

                SexpSymbol sym = cmd_expr.toSexpSymbol ();
                String cmd_name = sym.toString ();
                try {
                    if (cmd_name.equalsIgnoreCase("t-spec-add")) {
                        String name = OSpecHelper.doTSpecAdd(tspecAddSpecList);
                        if(!name.equals(tspecNameArray[i])) {
                            fail("Invalid return value for name");
                        }
                    }
                    else {
                        fail("Unexpected command received : " + cmd_expr);
                    }

                    String tSpecGetStr = "(get-ad-data :t-spec '|" + tspecNameArray[i] + "| :num-products 12 :ad-offer-type :product-leadgen)";
                    OSpec oSpec = testTargetingRequest(tSpecGetStr);
                    assertNotNull(oSpec);
                }
                catch(TransientDataException e) {
                    e.printStackTrace();
                    fail("Error occured while adding tspec" + e.getMessage());
                }
                catch(Exception e) {
                    e.printStackTrace();
                    fail("Error occured while retrieving tspec" + e.getMessage());
                }
            }
        }
    }


   
    public void testgetTSpecIncorpMappingDeltas1(){
        String[] urlArray = {"http://www.incorp-mapping-delta.com/test", "http://www.incorp-mapping-delta.com/test", "http://www.imd.com/sample", "http://www.sample4.com/sample"};

        for(int i=0; i<urlArray.length; i++) {
            String tSpecGetStr = "(get-ad-data :url \" " + urlArray[i]+ "\")";
            OSpec oSpec = null;
            try {
                oSpec = testTargetingRequest(tSpecGetStr);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            assertNotNull(oSpec);
        }
    }


    public void testAddTSpecIncorpMappingDeltas1(){
    	String mappingStr = "(incorp-mapping-deltas '((:add (:realm \"http://www.incorp-mapping-delta.com/test\")  nil  nil |" + sampleTSpecName + "| 1.0f0 1190503807055)))";
        String mappingStr2 = "(incorp-mapping-deltas '((:add (:realm \"http://www.incorp-mapping-delta.com/test\")  nil  nil |" + sampleTSpecName2 + "| 2.0f0 1190503807055)))";
        String[] strArray = {mappingStr, mappingStr2, sampleAddIncorpMappingDeltaStr, sample4AddIncorpMappingDeltaStr};
        String[] urlArray = {"http://www.incorp-mapping-delta.com/test", "http://www.incorp-mapping-delta.com/test", "http://www.imd.com/sample", "http://www.sample4.com/sample"};
        for(int i=0; i<strArray.length; i++) {
            Sexp mappingCmdExp = testProcessRequest(strArray[i]);
            if (mappingCmdExp!=null){
                SexpList l = mappingCmdExp.toSexpList ();
                Sexp cmd_expr = l.getFirst ();
                if (! cmd_expr.isSexpSymbol ())
                    System.out.println("command name not a symbol: " + cmd_expr.toString ());

                SexpSymbol sym = cmd_expr.toSexpSymbol ();
                String cmd_name = sym.toString ();
                try {
                    if (cmd_name.equals ("incorp-mapping-deltas")) {
                        Sexp mappingCmdDetails = l.get(1);
                        OSpecHelper.doUpdateTSpecMapping(mappingCmdDetails.toSexpList());
                    }
                    else {
                        fail("Invalid command passed into the test : " + cmd_name);
                    }
                    String tSpecGetStr = "(get-ad-data :url \" " + urlArray[i]+ "\")";
                    OSpec oSpec = testTargetingRequest(tSpecGetStr);
                    assertNotNull(oSpec);
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
        }
    }

    @Test
    public void testAddTSpecIncorpMappingDeltasWithGeocode(){
    	String mappingStr = "(incorp-mapping-deltas '((:add (:realm \"http://www.geo-incorp-test-url.com/test\") ((:zip \"91234\")) nil |" + sampleGeoTSpecName + "| 1.0f0 1190503807055)))";
        String mappingStr2 = "(incorp-mapping-deltas '((:add (:realm \"http://www.geo-incorp-test-url.com/test\")  nil  nil |" + sampleTSpecName2 + "| 2.0f0 1190503807055)))";
        String[] strArray = {mappingStr, mappingStr2};
        for(int i=0; i<strArray.length; i++) {
            Sexp mappingCmdExp = testProcessRequest(strArray[i]);
            if (mappingCmdExp!=null){
                SexpList l = mappingCmdExp.toSexpList ();
                Sexp cmd_expr = l.getFirst ();
                if (! cmd_expr.isSexpSymbol ())
                    System.out.println("command name not a symbol: " + cmd_expr.toString ());

                SexpSymbol sym = cmd_expr.toSexpSymbol ();
                String cmd_name = sym.toString ();
                try {
                    if (cmd_name.equals ("incorp-mapping-deltas")) {
                        Sexp mappingCmdDetails = l.get(1);
                        OSpecHelper.doUpdateTSpecMapping(mappingCmdDetails.toSexpList());
                    }
                    else {
                        fail("Invalid command passed into the test : " + cmd_name);
                    }
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
        }
        try {
            String tSpecGetStr = "(get-ad-data :zip-code \"91234\" :url \"http://www.geo-incorp-test-url.com/test\")";
            String tSpecGetStr2 = "(get-ad-data :url \"http://www.geo-incorp-test-url.com/test\")";
            OSpec oSpec = testTargetingRequest(tSpecGetStr);
            assertNotNull(oSpec);
            oSpec = testTargetingRequest(tSpecGetStr2);
            assertNotNull(oSpec);

        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testDeleteTSpecIncorpMappingDeltas(){
    	Sexp mappingCmdExp = testProcessRequest(sampleDeleteIncorpMappingDeltaStr);
    	if (mappingCmdExp!=null){
			SexpList l = mappingCmdExp.toSexpList ();
			Sexp cmd_expr = l.getFirst ();
			if (! cmd_expr.isSexpSymbol ())
				System.out.println("command name not a symbol: " + cmd_expr.toString ());

			SexpSymbol sym = cmd_expr.toSexpSymbol ();
			String cmd_name = sym.toString ();
            try {
                if (cmd_name.equals ("incorp-mapping-deltas")) {
                    Sexp mappingCmdDetails = l.get(1);
                    OSpecHelper.doUpdateTSpecMapping(mappingCmdDetails.toSexpList());
                }
                else {
                    fail("Invalid command passed into the test : " + cmd_name);
                }
                String tSpecGetStr = "(get-ad-data :url \" http://www.imd.com/sample\")";
                OSpec oSpec = testTargetingRequest(tSpecGetStr);
                assertNull(oSpec);
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
    }


    @Test
    public void testTSpecDelete() {
        TransientDataManager.getInstance().deleteOSpec(sampleTSpecName4);
        try {
            String tSpecGetStr = "(get-ad-data :url \" http://www.sample4.com/sample\")";
            OSpec oSpec = testTargetingRequest(tSpecGetStr);
            assertNull(oSpec);
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

    private Sexp testProcessRequest(String sexpCommandStr) {
		Reader r = new StringReader(sexpCommandStr);
		SexpReader lr = new SexpReader (r);
		Sexp e = null;
		try {
			e = lr.read ();
		} catch(Exception exp) {
			System.out.println("Exception caught when parsing the request string ");
			exp.printStackTrace();
		}

		return e;

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
