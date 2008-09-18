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
import com.tumri.joz.campaign.CampaignDBDataLoader;
import com.tumri.joz.campaign.CampaignDataLoadingException;
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
//
//	@BeforeClass
//	public static void initialize() {
//		JozData.init ();
//        CampaignDBDataLoader loader = CampaignDBDataLoader.getInstance();
//        try {
//            loader.loadData();
//        } catch (CampaignDataLoadingException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//    }
//
////	@Test
////	public void testDefaultRealmTSpec() {
////		try {
////			String queryStr = "(get-ad-data :url \"http://www.letsgodigital.org/en/2007/epson/review1.html/\" :num-products 12)";
////			ArrayList<Handle> result = testProcessRequest(queryStr);
////			assertNotNull(result);
////		} catch(Exception e){
////			System.out.println("Exception caught during test run");
////			e.printStackTrace();
////			assert(false);
////		}
////	}
////
////
////	@Test
////	public void testTSpecAdPod() {
////		try {
////			//String queryStr = "(get-ad-data :t-spec 'media-leadgen-offers :num-products 12 :ad-offer-type :product-leadgen)";
////            String queryStr = "(get-ad-data  :t-spec '|TSPEC-geckoseiya-1192593613232|  :ad-offer-type :product-leadgen " +
////                    " :revert-to-default-realm nil :ad-width nil  :ad-height nil  " +
////                    ":output-format :js-friendly)";
////            ArrayList<Handle> result = testProcessRequest(queryStr);
////			assertNotNull(result);
////		} catch(Exception e){
////			System.out.println("Exception caught during test run");
////			e.printStackTrace();
////			assert(false);
////		}
////	}
////	@Test
////	public void testDefaultRealm() {
////		try {
////			String queryStr = "(get-ad-data :url \"http://default-realm/\")";
////			ArrayList<Handle> result = testProcessRequest(queryStr);
////			assertNotNull(result);
////		} catch(Exception e){
////			System.out.println("Exception caught during test run");
////			e.printStackTrace();
////			assert(false);
////		}
////	}
////
////	@Test
////	public void testVanillaGetAdData() {
////		try {
////			String queryStr = "(get-ad-data :url \"http://www.photography.com/\")";
////			ArrayList<Handle> result = testProcessRequest(queryStr);
////			assertNotNull(result);
////		} catch(Exception e){
////			System.out.println("Exception caught during test run");
////			e.printStackTrace();
////			assert(false);
////		}
////	}
////
////	@Test
////	public void testNumProducts() {
////		try {
////			String queryStr = "(get-ad-data :url \"http://www.photography.com/\" :num-products 30)";
////			ArrayList<Handle> result = testProcessRequest(queryStr);
////			assertNotNull(result);
////		} catch(Exception e){
////			System.out.println("Exception caught during test run");
////			e.printStackTrace();
////			assert(false);
////		}
////	}
////
////	@Test
////	public void testBug1430() {
////		try {
////			String queryStr = "(get-ad-data :url \"http://www.testbug1430.com/\" :ad-offer-type :product-leadgen :which-row 1 :row-size 12)";
////			ArrayList<Handle> result = testProcessRequest(queryStr);
////			assertNotNull(result);
////		} catch(Exception e){
////			System.out.println("Exception caught during test run");
////			e.printStackTrace();
////			assert(false);
////		}
////	}
////
////	@Test
////	public void testAllowTooFewProducts() {
////		try {
////			String queryStr = "(get-ad-data :url \"http://www.photography.com/\" :ad-offer-type :leadgen-only :allow-too-few-products t)";
////			ArrayList<Handle> result = testProcessRequest(queryStr);
////			assertNotNull(result);
////		} catch(Exception e){
////			System.out.println("Exception caught during test run");
////			e.printStackTrace();
////			assert(false);
////		}
////	}
////
////	@Test
////	public void testNilRevertToDefaultRealm() {
////		try {
////			String queryStr = "(get-ad-data :url \"http://www.photography.com/\" :revert-to-default-realm nil)";
////			ArrayList<Handle> result = testProcessRequest(queryStr);
////			assertNotNull(result);
////		} catch(Exception e){
////			System.out.println("Exception caught during test run");
////			e.printStackTrace();
////			assert(false);
////		}
////	}
////
////	@Test
////	public void testLeadgenOnly() {
////		try {
////			String queryStr = "(get-ad-data :t-spec '|test-lenovo| :ad-offer-type :leadgen-only :num-products 50 :revert-to-default-realm t)";
////			ArrayList<Handle> result = testProcessRequest(queryStr);
////			assertNotNull(result);
////		} catch(Exception e){
////			System.out.println("Exception caught during test run");
////			e.printStackTrace();
////			assert(false);
////		}
////	}
////
////	@Test
////	public void testProductOnly() {
////		try {
////			String queryStr =  "(get-ad-data :url \"http://www.photography.com/\" :ad-offer-type :product-only :revert-to-default-realm t)";
////			ArrayList<Handle> result = testProcessRequest(queryStr);
////			assertNotNull(result);
////		} catch(Exception e){
////			System.out.println("Exception caught during test run");
////			e.printStackTrace();
////			assert(false);
////		}
////	}
////
////
////	@Test
////	public void testHybrid() {
////		try {
////			String queryStr =  "(get-ad-data :t-spec '|lenovo-t1| :ad-offer-type :product-leadgen :which-row 1 :row-size 12)";
////			ArrayList<Handle> result = testProcessRequest(queryStr);
////			assertNotNull(result);
////		} catch(Exception e){
////			System.out.println("Exception caught during test run");
////			e.printStackTrace();
////			assert(false);
////		}
////	}
////
////    	@Test
////	public void testIncludedProducts() {
////		try {
////			String queryStr =  "(get-ad-data :t-spec '|lenovo-t1| :ad-offer-type :product-leadgen)";
////			ArrayList<Handle> result = testProcessRequest(queryStr);
////			assertNotNull(result);
////		} catch(Exception e){
////			System.out.println("Exception caught during test run");
////			e.printStackTrace();
////			assert(false);
////		}
////	}
////
////
//////	@Test
//////	public void testKeywordSearch() {
//////		try {
//////			String queryStr = "(get-ad-data :url \"http://www.photography.com/\" :script-keywords \"\"Firefox Mozilla Netscape browser Internet Thunderbird CSS HTML wysiwyg editor Linux Mac Rich Text Editor http://forums.delphiforums.com/n/nav/start.asp?webtag=gofirefox\" :revert-to-default-realm t)";
//////			ArrayList<Handle> result = testProcessRequest(queryStr);
//////			assertNotNull(result);
//////		} catch(Exception e){
//////			System.out.println("Exception caught during test run");
//////			e.printStackTrace();
//////			assert(false);
//////		}
//////	}
////
////	@Test
////	public void testScriptKeywordSearch() {
////		try {
////			String queryStr = "(get-ad-data :url \"http://www.photography.com/\" :script-keywords \"nikon\" :revert-to-default-realm nil)";
////			ArrayList<Handle> result = testProcessRequest(queryStr);
////			assertNotNull(result);
////		} catch(Exception e){
////			System.out.println("Exception caught during test run");
////			e.printStackTrace();
////			assert(false);
////		}
////	}
////
////	@Test
////	public void testIncludedCategories() {
////		try {
////			String queryStr = "(get-ad-data :url \"http://www.photography.com/\" :category \"GLASSVIEW.TUMRI_14172\" :revert-to-default-realm nil)";
////			ArrayList<Handle> result = testProcessRequest(queryStr);
////			assertNotNull(result);
////		} catch(Exception e){
////			System.out.println("Exception caught during test run");
////			e.printStackTrace();
////			assert(false);
////		}
////	}
////
////	@Test
////	public void testTestBug1451() {
////		try {
////			//String queryStr = "(get-ad-data :url \"http://www.photography.com/\" :category \"GLASSVIEW.TUMRI_14172\" :revert-to-default-realm nil)";
////            String queryStr = "(get-ad-data :url \"http://www.letsgodigital.org/en/2007/epson/review1.html\" :t-spec |'letsgodigital-keyword-targeting| :num-products 12 :revert-to-default-realm nil :ad-width 300 :ad-height 250 :ad-offer-type :product-leadgen :min-num-leadgens 1 :output-format :js-friendly :max-prod-desc-len 32)";
////            ArrayList<Handle> result = testProcessRequest(queryStr);
////			assertNotNull(result);
////		} catch(Exception e){
////			System.out.println("Exception caught during test run");
////			e.printStackTrace();
////			assert(false);
////		}
////	}
////
////
////    public void testTestBug1451Url() {
////        try {
////            //String queryStr = "(get-ad-data :url \"http://www.photography.com/\" :category \"GLASSVIEW.TUMRI_14172\" :revert-to-default-realm nil)";
////            String queryStr = "(get-ad-data :url http://www.letsgodigital.org/en/2007/epson/review1.html :theme null :store-id null :category null :referrer null :zip-code null :num-products 12 :row-size null :which-row null :revert-to-default-realm false :keywords null :script-keywords null :include-cat-counts false :seed null :psychographics-p null :mine-pub-url-p null :allow-too-few-products null :ad-width 300 :ad-height 250 :ad-offer-type PRODUCT_LEADGEN :min-num-leadgens 1 :output-format JS_FRIENDLY :output-order null :output-order-noise-stddev 0.1 :max-prod-desc-len 32 :country-name null :region null :city null :dma null :area-code null)";
////            ArrayList<Handle> result = testProcessRequest(queryStr);
////            assertNotNull(result);
////        } catch(Exception e){
////            System.out.println("Exception caught during test run");
////            e.printStackTrace();
////            assert(false);
////        }
////    }
//    @Test
//    public void testCatAttrQueryText() {
//        try {
//            String queryStr = "(get-ad-data :t-spec |TestCatAttrText| :revert-to-default-realm nil)";
//            ArrayList<Handle> result = testProcessRequest(queryStr);
//            assertNotNull(result);
//        } catch(Exception e){
//            System.out.println("Exception caught during test run");
//            e.printStackTrace();
//            assert(false);
//        }
//    }
//
//    @Test
//    public void testCatAttrNumeric() {
//        try {
//            String queryStr = "(get-ad-data :t-spec |TestCatAttrNumeric| :revert-to-default-realm nil)";
//            ArrayList<Handle> result = testProcessRequest(queryStr);
//            assertNotNull(result);
//        } catch(Exception e){
//            System.out.println("Exception caught during test run");
//            e.printStackTrace();
//            assert(false);
//        }
//    }
//
//    @Test
//    public void testGeoCountry() {
//        try {
//            String queryStr = "(get-ad-data :t-spec |TestGeoCountry| :revert-to-default-realm nil)";
//            //String queryStr = "(get-ad-data :t-spec |TSPEC-geckoseiya-1192593613232| :revert-to-default-realm nil)";
//            ArrayList<Handle> result = testProcessRequest(queryStr);
//            assertNotNull(result);
//        } catch(Exception e){
//            System.out.println("Exception caught during test run");
//            e.printStackTrace();
//            assert(false);
//        }
//    }
//
//    @Test
//    public void testGeoFlag() {
//        try {
//            String queryStr = "(get-ad-data :t-spec |TestGeoFlag| :country-name \"US\" :city \"Redwood\" :revert-to-default-realm nil)";
//            //String queryStr = "(get-ad-data :t-spec |TSPEC-geckoseiya-1192593613232| :revert-to-default-realm nil)";
//            ArrayList<Handle> result = testProcessRequest(queryStr);
//            assertNotNull(result);
//        } catch(Exception e){
//            System.out.println("Exception caught during test run");
//            e.printStackTrace();
//            assert(false);
//        }
//    }
//
//    private ArrayList<Handle> testProcessRequest(String getAdDataCommandStr) throws Exception {
//		ProductRequestProcessor prodRequest = new ProductRequestProcessor();
//		Reader r = new StringReader (getAdDataCommandStr);
//		ArrayList<Handle> results = null;
//		SexpReader lr = new SexpReader (r);
//		try {
//			Sexp e = lr.read ();
//			SexpList l = e.toSexpList ();
//			Sexp cmd_expr = l.getFirst ();
//			if (! cmd_expr.isSexpSymbol ())
//				System.out.println("command name not a symbol: " + cmd_expr.toString ());
//
//			SexpSymbol sym = cmd_expr.toSexpSymbol ();
//			String cmd_name = sym.toString ();
//
//			// Return the right Cmd* class to handle this request.
//
//			if (cmd_name.equals ("get-ad-data")) {
//				AdDataRequest rqst = new AdDataRequest (e);
//				ProductSelectionResults presults = prodRequest.processRequest(rqst);
//				//Inspect the results
//				results = presults.getResults();
//				System.out.println("Number of results returned are : " + results.size());
//				Assert.assertTrue(presults!=null);
//
//				ProductDB pdb = ProductDB.getInstance ();
//				results = presults.getResults();
//				for (Handle res : results)
//				{
//					long id = res.getOid ();
//					IProduct ip = pdb.get (id);
//                    String name= "", desc = "", productType = "";
//                    if (ip !=null) {
//                        name = ip.getProductName ();
//                        desc = ip.getDescription ();
//                        productType =  ip.getProductTypeStr();
//                    }
//					System.out.println(id + " Type = " + productType + " " + name + "    " + desc);
//				}
//
//			} else {
//				System.out.println("The request could not be parsed correctly");
//				Assert.assertTrue(false);
//			}
//		} catch(Exception e) {
//			throw e;
//		}
//
//		return results;
//
//	}
}
