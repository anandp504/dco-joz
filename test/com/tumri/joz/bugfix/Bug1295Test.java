package com.tumri.joz.bugfix;

import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import com.tumri.cma.domain.OSpec;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpSymbol;
import com.tumri.joz.campaign.OSpecHelper;
import com.tumri.joz.campaign.TransientDataException;
import com.tumri.joz.campaign.CampaignDBDataLoader;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.targeting.TargetingRequestProcessor;
import com.tumri.joz.jozMain.AdDataRequest;

import java.io.Reader;
import java.io.StringReader;

/**
 * Unit Test case for Bug 1295
 */
public class Bug1295Test {
    @BeforeClass
    public static void initialize() throws Exception {
        CampaignDBDataLoader loader = CampaignDBDataLoader.getInstance();
        loader.loadData();
    }


    @Test
    public void testBug1295() {
        //1. Create a new tspec TSPEC-joz-test2
        String tSpecName = "TSPEC-joz-test2";
        addNewTSpec(tSpecName);

        //2. Create a url tspec mapping
        String urlMappingStr = "(incorp-mapping-deltas '((:add (:realm \"http://test-joz.com\")  nil  nil |TSPEC-joz-test2| 1.0f0 30763388)))";
        addNewMapping(urlMappingStr);

        //3. Create a theme tspec mapping
        String themeMappingStr = "(incorp-mapping-deltas '((:add (:theme \"theme-test-joz\")  nil  nil |TSPEC-joz-test2| 1.0f0 30786374))) ";
        addNewMapping(themeMappingStr);

        //4. make a get-ad-data request for above url http://test-joz.com
        String tSpecGetStr = "(get-ad-data :url \"http://test-joz.com\")";
        validateOSpecReturnedforGetAdDataRequest(tSpecGetStr, tSpecName);

        //5. Delete mapping for url http://test-joz.com
        String urlIncorpMappingStr = "(incorp-mapping-deltas '((:delete (:realm \"http://test-joz.com\")  ()  nil |TSPEC-joz-test2| 1.0f0 30941991)))";
        deleteIncorpDeltaMapping(urlIncorpMappingStr);

        //6. Add new mapping with geo location
        String mappingWithGeoStr = "(incorp-mapping-deltas '((:add (:realm \"http://test-joz.com\")  ((:zip \"99005\"))  nil |TSPEC-joz-test2| 1.0f0 30942626)))";
        addNewMapping(mappingWithGeoStr);

        //7. Make a get-ad-data request for url using geo
        String tSpecGetGeoStr = "(get-ad-data :zip-code \"99005\" :url \"http://test-joz.com\")";
        validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, tSpecName);

        //8. Make a get-ad-data request for url without geo
        String tSpecGetUrlMappingStr = "(get-ad-data :url \"http://test-joz.com\")";
        validateOSpecReturnedforGetAdDataRequest(tSpecGetUrlMappingStr, null);

        //8. Make a get-ad-data request for theme theme-test-joz
        String tSpecGetThemeStr = "(get-ad-data :theme \"theme-test-joz\")";
        validateOSpecReturnedforGetAdDataRequest(tSpecGetThemeStr, tSpecName);

        //9. Delete the url mapping
        String urlDeleteIncorpMappingStr = "(incorp-mapping-deltas '((:delete (:realm \"http://test-joz.com\")  ((:zip \"99005\"))  nil |TSPEC-joz-test2| 1.0f0 31230170)))";
        deleteIncorpDeltaMapping(urlDeleteIncorpMappingStr);

        //10. make a get-ad-data request for url http://test-joz.com
        validateOSpecReturnedforGetAdDataRequest(tSpecGetStr, null);

        //11. make a get-ad-data request for theme theme-test-joz
        validateOSpecReturnedforGetAdDataRequest(tSpecGetThemeStr, tSpecName);

        //12. make a get-ad-data request for theme theme-test-joz
        String tSpecGetGeoThemeStr = "(get-ad-data :zip-code \"99005\" :theme \"theme-test-joz\")";
        validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoThemeStr, tSpecName);

        //13. Delete the theme mapping
        String tSpecDeleteGeoThemeStr = "(incorp-mapping-deltas '((:delete (:theme \"theme-test-joz\") nil  nil |TSPEC-joz-test2| 1.0f0 30786374)))";
        deleteIncorpDeltaMapping(tSpecDeleteGeoThemeStr);

        //14. make a get-ad-data request for theme theme-test-joz
        validateOSpecReturnedforGetAdDataRequest(tSpecGetThemeStr, null);

    }

    private void validateOSpecReturnedforGetAdDataRequest(String getStr, String expectedOSpecName) {
        OSpec oSpec = null;
        try {
            oSpec = testTargetingRequest(getStr);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error occured while making get-ad-data request");
        }

        if(expectedOSpecName == null) {
            if(oSpec != null) {
                if(!oSpec.equals(CampaignDB.getInstance().getDefaultOSpec())) {
                    fail("Invalid OSpec returned for given get-ad-data request");                                    
                }
            }
        }
        else {
            assertNotNull(oSpec);
            assertEquals(oSpec.getName(), expectedOSpecName);
        }
    }

    private void addNewTSpec(String name) {
        //1. Create a new tspec TSPEC-joz-test2
        String tSpecAddStr = "(t-spec-add :name ' |" + name + "| :version -1 :include-categories '(|GLASSVIEW.TUMRI_14215| |GLASSVIEW.TUMRI_14209| |GLASSVIEW.TUMRI_14208| |GLASSVIEW.TUMRI_14214| |GLASSVIEW.TUMRI_14217| |GLASSVIEW.TUMRI_14227| ) :ref-price-constraints '(10.0 146.0) )";
        Sexp tspecMappingSexp = testProcessRequest(tSpecAddStr);
        SexpList tspecAddSpecList = tspecMappingSexp.toSexpList();
        String rName = null;
        try {
            rName = OSpecHelper.doTSpecAdd(tspecAddSpecList);
        } catch (TransientDataException e) {
            e.printStackTrace();
            fail("Error occured while adding new tspec");
        }
        if(rName == null || !rName.equals(name)) {
            fail("Invalid return value for name");
        }
    }

    private void addNewMapping(String mappingStr) {
        Sexp mappingCmdExp = testProcessRequest(mappingStr);
        SexpList l = mappingCmdExp.toSexpList ();
        Sexp mappingCmdDetails = l.get(1);
        try {
            OSpecHelper.doUpdateTSpecMapping(mappingCmdDetails.toSexpList());
        } catch (TransientDataException e) {
            e.printStackTrace();
            fail("Error occured while adding new mapping using incorp-mapping-delta");
        }
    }

    private void deleteIncorpDeltaMapping(String mappingStr) {
        Sexp mappingCmdExp = testProcessRequest(mappingStr);
        SexpList l = mappingCmdExp.toSexpList ();
        Sexp mappingCmdDetails = l.get(1);
        try {
            OSpecHelper.doUpdateTSpecMapping(mappingCmdDetails.toSexpList());
        } catch (TransientDataException e) {
            e.printStackTrace();
            fail("Error occured while adding new mapping using incorp-mapping-delta");
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
