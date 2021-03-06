package com.tumri.joz.bugfix;

import com.tumri.cma.domain.Campaign;
import com.tumri.cma.domain.OSpec;
import com.tumri.cma.domain.Recipe;
import com.tumri.cma.domain.TSpec;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.OSpecHelper;
import com.tumri.joz.campaign.TransientDataException;
import com.tumri.joz.campaign.TransientDataManager;
import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.targeting.TargetingRequestProcessor;
import com.tumri.joz.targeting.TargetingResults;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpSymbol;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

/**
 * Provides utility method for Transient Data creation, validation, etc. for unit tests.
 *
 * @author bpatel
 */
public class TransientDataTestUtil {

    public static void validateOSpecReturnedforGetAdDataRequest(String getStr, String expectedOSpecName) {
        Recipe recipe = null;
        try {
            recipe = testTargetingRequest(getStr);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error occured while making get-ad-data request");
        }
        assertNotNull(recipe);
        assertEquals(recipe.getName(), expectedOSpecName);
    }

    public static void validateOSpecResultforGetAdDataRequest(String getStr, String[] expectedOSpecNameArray) {
        Recipe recipe = null;
        try {
            recipe = testTargetingRequest(getStr);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error occured while making get-ad-data request");
        }
        assertNotNull(recipe);
        boolean success = false;
        for(int i=0; i<expectedOSpecNameArray.length; i++) {
            if(recipe.getName().equals(expectedOSpecNameArray[i])) {
                success = true;
                break;
            }
        }
        assertEquals(success, true);
    }

    public static void addNewTSpec(String name) {
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

    public static void addNewMapping(String mappingStr) {
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

    public static void deleteIncorpDeltaMapping(String mappingStr) {
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

    public static Sexp testProcessRequest(String sexpCommandStr) {
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

    public static Recipe testTargetingRequest(String getAdDataCommandStr) throws Exception {
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
            AdDataRequest rqst = null; //new AdDataRequest (e);
            recipe = null; //processor.processRequest(rqst, new Features());

            JozAdRequest jozRequest = new JozAdRequest();
            jozRequest.setValue(JozAdRequest.KEY_AD_TYPE, "skyscraper");
            jozRequest.setValue(JozAdRequest.KEY_LOCATION_ID, "115856");
           // String tspecName = jozRequest.getValue(JozAdRequest.KEY_T_SPEC);
            //System.out.println("tspecName: " + tspecName);
            if(CampaignDB.getInstance().getCampaign(101) != null){
            	if(CampaignDB.getInstance().getCampaign(101).getName().equals("testCampaign"))
            	jozRequest.setValue(JozAdRequest.KEY_T_SPEC, "testTSpec");
            }	
            else
            jozRequest.setValue(JozAdRequest.KEY_T_SPEC, "sampleTestTSpec");
            	
            AdDataRequest request = new AdDataRequest(jozRequest);
            Features f = new Features();
            TargetingResults trs = processor.processRequest(request, f);
            if(trs != null)
            recipe = trs.getRecipe(); //trp.processRequest(request, f);
        } else {
            fail("The request could not be parsed correctly");
        }

        return recipe;
    }
}
