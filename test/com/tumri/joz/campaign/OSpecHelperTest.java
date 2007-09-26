package com.tumri.joz.campaign;

import java.io.Reader;
import java.io.StringReader;

import org.junit.BeforeClass;
import org.junit.Test;

import com.tumri.cma.domain.OSpec;
import com.tumri.cma.misc.SexpOSpecHelper;
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
    
    @Test
    public void testTspecAdd() {
    	String tSpecAddStr = "(t-spec-add :name '|TPP A 1| :version -1 :include-categories '(|GLASSVIEW.TUMRI_14215| |GLASSVIEW.TUMRI_14209| |GLASSVIEW.TUMRI_14208| |GLASSVIEW.TUMRI_14214| |GLASSVIEW.TUMRI_14217| |GLASSVIEW.TUMRI_14227| ) :ref-price-constraints '(10.0 146.0) )";
    	Sexp tspecMappingSexp = testProcessRequest(tSpecAddStr);
    	if (tspecMappingSexp!=null){
			SexpList tspecAddSpecList = tspecMappingSexp.toSexpList();
			Sexp cmd_expr = tspecAddSpecList.getFirst ();
			if (! cmd_expr.isSexpSymbol ()) {
				System.out.println("command name not a symbol: " + cmd_expr.toString ());
			}

			SexpSymbol sym = cmd_expr.toSexpSymbol ();
			String cmd_name = sym.toString ();
			if (cmd_name.equalsIgnoreCase("t-spec-add")) {
				String name = OSpecHelper.doTSpecAdd(tspecAddSpecList);
				if (name != null){
					assert(true);
				} else {
					assert(false);
				}
			} else {
				System.out.println("Unexpected command received : " + cmd_expr);
			}
    	}
    }

    @Test
    public void testTSpecDelete() {
    	
    }
    
    @Test
    public void testGetTSpecObject() {
    	String tSpecAddStr = "(t-spec-add :name '|TPP A 1| :version -1 :include-categories '(|GLASSVIEW.TUMRI_14215| |GLASSVIEW.TUMRI_14209| |GLASSVIEW.TUMRI_14208| |GLASSVIEW.TUMRI_14214| |GLASSVIEW.TUMRI_14217| |GLASSVIEW.TUMRI_14227| ) :ref-price-constraints '(10.0 146.0) )";
    	OSpec ospec = null;
    	try {
    		ospec = SexpOSpecHelper.getOSpec(tSpecAddStr);
    	} catch(Exception e) {
    		
    	}
    	assert(ospec!=null);
    	
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
