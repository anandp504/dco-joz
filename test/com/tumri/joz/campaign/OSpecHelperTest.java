package com.tumri.joz.campaign;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.productselection.ProductRequestProcessor;
import com.tumri.joz.productselection.ProductSelectionResults;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpSymbol;

/**
 * Unit test class for the OSpecHelper
 * @author nipun
 *
 */
public class OSpecHelperTest {
    private static CampaignDBDataLoader loader;
    
    @BeforeClass
    public static void initialize() throws Exception {
        loader = CampaignDBDataLoader.getInstance();
        loader.loadData();
    }

    @Test
    public void testTSpecIncorpMappingDeltas(){
    	String mappingStr = "(incorp-mapping-deltas '((:add (:realm \"http://www.pradeep.com\")  nil  nil |keyword-scavenge-morefocus| 1.0f0 1190503807055)))";
    	Sexp mappingCmdExp = testProcessRequest(mappingStr);
    	if (mappingCmdExp!=null){
			SexpList l = mappingCmdExp.toSexpList ();
			Sexp cmd_expr = l.getFirst ();
			if (! cmd_expr.isSexpSymbol ())
				System.out.println("command name not a symbol: " + cmd_expr.toString ());

			SexpSymbol sym = cmd_expr.toSexpSymbol ();
			String cmd_name = sym.toString ();

			if (cmd_name.equals ("incorp-mapping-deltas")) {
				Sexp mappingCmdDetails = l.get(1);
				System.out.println("issuing mapping command  :" + mappingCmdDetails); 
				OSpecHelper.doUpdateTSpecMapping(mappingCmdDetails.toSexpList());
				assert(true);
			} else {
				System.out.println("Invalid command passed into the test : " + cmd_name);
				assert(false);
			}

    	}
    	
    }
    
    
    /**
     * Helper method that will convert the string into a Sexp
     * @param getAdDataCommandStr
     * @return
     * @throws Exception
     */
	private Sexp testProcessRequest(String sexpCommandStr) {
		Reader r = new StringReader (sexpCommandStr);
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
}
