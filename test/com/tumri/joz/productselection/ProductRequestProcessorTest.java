package com.tumri.joz.productselection;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.JozData;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpSymbol;

import static org.junit.Assert.*;
/**
 * Unit test class for Product Selection
 * @author nipun
 *
 */
public class ProductRequestProcessorTest {

	@BeforeClass
	public static void initialize() {
		JozData.init ();
	}

	@Test
	public void testHybridAdPod() {
		try {
			String queryStr = "(get-ad-data :url \"http://www.consumersearch.com/www/house_and_home/\" :num-products 12 :ad-offer-type :product-leadgen)";
			ArrayList<Handle> result = testProcessRequest(queryStr);
			assertNotNull(result);
		} catch(Exception e){
			System.out.println("Exception caught during test run");
			e.printStackTrace();
			assert(false);
		}
	}
	
	
	@Test
	public void testTSpecAdPod() {
		try {
			String queryStr = "(get-ad-data :t-spec \"TSPEC-zappos.com-1189622851218\" :num-products 12 :ad-offer-type :product-leadgen :allow-too-few-products t)";
			ArrayList<Handle> result = testProcessRequest(queryStr);
			assertNotNull(result);
		} catch(Exception e){
			System.out.println("Exception caught during test run");
			e.printStackTrace();
			assert(false);
		}
	}
	@Test
	public void testDefaultRealm() {
		try {
			String queryStr = "(get-ad-data :url \"http://default-realm/\")";
			ArrayList<Handle> result = testProcessRequest(queryStr);
			assertNotNull(result);
		} catch(Exception e){
			System.out.println("Exception caught during test run");
			e.printStackTrace();
			assert(false);
		}
	}

	@Test
	public void testVanillaGetAdData() {
		try {
			String queryStr = "(get-ad-data :url \"http://www.photography.com/\")";
			ArrayList<Handle> result = testProcessRequest(queryStr);
			assertNotNull(result);
		} catch(Exception e){
			System.out.println("Exception caught during test run");
			e.printStackTrace();
			assert(false);
		}
	}

	@Test
	public void testNumProducts() {
		try {
			String queryStr = "(get-ad-data :url \"http://www.photography.com/\" :num-products 30)";
			ArrayList<Handle> result = testProcessRequest(queryStr);
			assertNotNull(result);
		} catch(Exception e){
			System.out.println("Exception caught during test run");
			e.printStackTrace();
			assert(false);
		}
	}

	@Test
	public void testPagination() {
		try {
			String queryStr = "(get-ad-data :url \"http://www.photography.com/\" :which-row 2 :row-size 12)";
			ArrayList<Handle> result = testProcessRequest(queryStr);
			assertNotNull(result);
		} catch(Exception e){
			System.out.println("Exception caught during test run");
			e.printStackTrace();
			assert(false);
		}
	}

	@Test
	public void testAllowTooFewProducts() {
		try {
			String queryStr = "(get-ad-data :url \"http://www.photography.com/\" :ad-offer-type :leadgen-only :allow-too-few-products t)";
			ArrayList<Handle> result = testProcessRequest(queryStr);
			assertNotNull(result);
		} catch(Exception e){
			System.out.println("Exception caught during test run");
			e.printStackTrace();
			assert(false);
		}
	}

	@Test
	public void testRevertToDefaultRealm() {
		try {
			String queryStr = "(get-ad-data :url \"http://www.photography.com/\" :revert-to-default-realm nil)";
			ArrayList<Handle> result = testProcessRequest(queryStr);
			assertNotNull(result);
		} catch(Exception e){
			System.out.println("Exception caught during test run");
			e.printStackTrace();
			assert(false);
		}
	}

	@Test
	public void testLeadgenOnly() {
		try {
			String queryStr = "(get-ad-data :t-spec \"T-SPEC-NBC Lead Gen\" :ad-offer-type :leadgen-only :ad-height 600 :ad-width 120)";
			ArrayList<Handle> result = testProcessRequest(queryStr);
			assertNotNull(result);
		} catch(Exception e){
			System.out.println("Exception caught during test run");
			e.printStackTrace();
			assert(false);
		}
	}

	@Test
	public void testProductOnly() {
		try {
			String queryStr =  "(get-ad-data :url \"http://www.photography.com/\" :ad-offer-type :product-only :revert-to-default-realm nil)";
			ArrayList<Handle> result = testProcessRequest(queryStr);
			assertNotNull(result);
		} catch(Exception e){
			System.out.println("Exception caught during test run");
			e.printStackTrace();
			assert(false);
		}
	}


	@Test
	public void testHybrid() {
		try {
			String queryStr =  "(get-ad-data :url \"http://www.photography.com/\" :ad-offer-type :product-leadgen :revert-to-default-realm nil)";
			ArrayList<Handle> result = testProcessRequest(queryStr);
			assertNotNull(result);
		} catch(Exception e){
			System.out.println("Exception caught during test run");
			e.printStackTrace();
			assert(false);
		}
	}

	@Test
	public void testKeywordSearch() {
		try {
			String queryStr = "(get-ad-data :url \"http://www.photography.com/\" :keywords \"nikon\" :revert-to-default-realm nil)";
			ArrayList<Handle> result = testProcessRequest(queryStr);
			assertNotNull(result);
		} catch(Exception e){
			System.out.println("Exception caught during test run");
			e.printStackTrace();
			assert(false);
		}
	}

	@Test
	public void testScriptKeywordSearch() {
		try {
			String queryStr = "(get-ad-data :url \"http://www.photography.com/\" :script-keywords \"nikon\" :revert-to-default-realm nil)";
			ArrayList<Handle> result = testProcessRequest(queryStr);
			assertNotNull(result);
		} catch(Exception e){
			System.out.println("Exception caught during test run");
			e.printStackTrace();
			assert(false);
		}
	}

	@Test
	public void testIncludedCategories() {
		try {
			String queryStr = "(get-ad-data :url \"http://www.photography.com/\" :category \"GLASSVIEW.TUMRI_14172\" :revert-to-default-realm nil)";
			ArrayList<Handle> result = testProcessRequest(queryStr);
			assertNotNull(result);
		} catch(Exception e){
			System.out.println("Exception caught during test run");
			e.printStackTrace();
			assert(false);
		}
	}

	private ArrayList<Handle> testProcessRequest(String getAdDataCommandStr) throws Exception {
		ProductRequestProcessor prodRequest = new ProductRequestProcessor();
		Reader r = new StringReader (getAdDataCommandStr);
		ArrayList<Handle> results = null;
		SexpReader lr = new SexpReader (r);
		try {
			Sexp e = lr.read ();
			SexpList l = e.toSexpList ();
			Sexp cmd_expr = l.getFirst ();
			if (! cmd_expr.isSexpSymbol ())
				System.out.println("command name not a symbol: " + cmd_expr.toString ());

			SexpSymbol sym = cmd_expr.toSexpSymbol ();
			String cmd_name = sym.toString ();

			// Return the right Cmd* class to handle this request.

			if (cmd_name.equals ("get-ad-data")) {
				AdDataRequest rqst = new AdDataRequest (e);
				ProductSelectionResults presults = prodRequest.processRequest(rqst);
				//Inspect the results
				results = presults.getResults();
				System.out.println("Number of results returned are : " + results.size());
				Assert.assertTrue(presults!=null);

				ProductDB pdb = ProductDB.getInstance ();
				results = presults.getResults();
				for (Handle res : results)
				{
					int id = res.getOid ();
					IProduct ip = pdb.get (id);
					String name = ip.getProductName ();
					String desc = ip.getDescription ();
					System.out.println(id + " Type = " + ip.getProductTypeStr() + " " + name + "    " + desc);
				}

			} else {
				System.out.println("The request could not be parsed correctly");
				Assert.assertTrue(false);
			}
		} catch(Exception e) {
			throw e;
		}

		return results;

	}}
