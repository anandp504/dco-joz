package com.tumri.joz.productselection;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpSymbol;

public class UrlScavengerTest {

	@Test
	public void testURLScavenging() {
		String queryStr = "(get-ad-data :url \"http://www.photography.com/ni11,pun/camera/nikon?q=xyz\")";
		try {
			AdDataRequest rqst = createRequestFromCommandString(queryStr);
			String keywords = URLScavenger.mineKeywords(rqst, null, null);
			Assert.assertTrue(keywords!=null);
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
	}

    @Test
	public void testnullURL() {
		String queryStr = "(get-ad-data :url \"\")";
		try {
			AdDataRequest rqst = createRequestFromCommandString(queryStr);
			String keywords = URLScavenger.mineKeywords(rqst, null, null);
			Assert.assertTrue(keywords.equals(""));
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
	}

    @Test
	public void testRequestStopWords() {
		String queryStr = "(get-ad-data :url \"http://www.photography.com/camera/canon/nikon/test\")";
		try {
			AdDataRequest rqst = createRequestFromCommandString(queryStr);
			ArrayList<String> requestStopWordsAl = new ArrayList<String>();
			requestStopWordsAl.add("+canon");
			String keywords = URLScavenger.mineKeywords(rqst, requestStopWordsAl, null);
			Assert.assertTrue((keywords!=null)&&(keywords.indexOf("canon")==-1));
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
	}

    @Test
	public void testExciteUKIssue() {
		String queryStr = "(get-ad-data :url \"http://www.excite.co.uk/shopping/categories/briefcases_and_attache_cases?cid=8533&mid=&bid=&\")";
		try {
			AdDataRequest rqst = createRequestFromCommandString(queryStr);
			ArrayList<String> requestStopWordsAl = new ArrayList<String>();
			requestStopWordsAl.add("excite");
			requestStopWordsAl.add("co");
            requestStopWordsAl.add("uk");
            requestStopWordsAl.add("shopping");
            requestStopWordsAl.add("cid");
            requestStopWordsAl.add("categories");
            requestStopWordsAl.add("bid");
            requestStopWordsAl.add("mid");
            String keywords = URLScavenger.mineKeywords(rqst, requestStopWordsAl, null);
			Assert.assertTrue((keywords!=null)&&(keywords.indexOf("excite")==-1));
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
	}

    @Test
    public void testShopInternet() {
		//String queryStr = "(get-ad-data :url \"http://shop.internet.com/index.php?/SF-6/BEFID-9006/keyword-Flat+Panel+LCD\")";

        //String queryStr = "(get-ad-data :url \"http://shop.internet.com/index.php?/BEFID-96252/SF-6/keyword=plasma\")";

        //String queryStr = "(get-ad-data :url \"http://shop.internet.com/index.php?IC_ic=1&IC_query_search=1&IC_QueryText=Sharp+Aguus&SUBMIT.x=0&SUBMIT.y=0&SUBMIT=Find\")";

        //String queryStr = "(get-ad-data :url \"http://shop.internet.com/index.php?/SF-7/BEFID-96252/keyword-Sharp%20Aquos/dnatrs-price_range_1300_1650\")";

        //String queryStr = "(get-ad-data :url \"http://alatest.com/Digital_SLR_Cameras/248/?v1=brand%7EPentax%7EL\")";

        String queryStr = "(get-ad-data :url \"http://alatest.com/Global_Positioning_Systems_GPS/15/?v1=brand%7EMagellan+Navigation%7EL\")";


        try {
			AdDataRequest rqst = createRequestFromCommandString(queryStr);
			ArrayList<String> requestStopWordsAl = new ArrayList<String>();
            requestStopWordsAl.add("~");
            requestStopWordsAl.add("-");
            requestStopWordsAl.add("//");
            requestStopWordsAl.add("shop");
			requestStopWordsAl.add("internet");
            requestStopWordsAl.add("SF-6");
            requestStopWordsAl.add("SF-7");
            requestStopWordsAl.add("index.php");
            requestStopWordsAl.add("keyword");
            requestStopWordsAl.add("dnatrs");
            requestStopWordsAl.add("IC_ic");
            requestStopWordsAl.add("IC_query_search");
            requestStopWordsAl.add("IC_Query_text");
            requestStopWordsAl.add("SUBMIT");
            requestStopWordsAl.add("BEFID");
            requestStopWordsAl.add("price_range");
            requestStopWordsAl.add("brand");
            requestStopWordsAl.add("sf-6");
            requestStopWordsAl.add("sf-7");
            requestStopWordsAl.add("index");
            requestStopWordsAl.add("php");
            String keywords = URLScavenger.mineKeywords(rqst, requestStopWordsAl, null);
			Assert.assertTrue((keywords!=null));
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}        
    }
    @Test
	public void testRequestExciteQueryNames() {
		String queryStr = "(get-ad-data :url \"http://excite.co.uk/search?q=camera\")";
		try {
			AdDataRequest rqst = createRequestFromCommandString(queryStr);
			ArrayList<String> requestQueryNamesAl = new ArrayList<String>();
			requestQueryNamesAl.add("q");
			//requestQueryNamesAl.add("testquery");
			//requestQueryNamesAl.add("nipun");
			String keywords = URLScavenger.mineKeywords(rqst, null, requestQueryNamesAl);
			System.out.println("The mined keywords using query names are : " + keywords);
			Assert.assertTrue((keywords!=null)&&(keywords.indexOf("camera")!=-1));
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
	}
    @Test
	public void testRequestQueryNames() {
		String queryStr = "(get-ad-data :url \"http://www.photography.com/camera/canon/nikon/test?nipun=test,camera=nikon&testquery=blah\")";
		try {
			AdDataRequest rqst = createRequestFromCommandString(queryStr);
			ArrayList<String> requestQueryNamesAl = new ArrayList<String>();
			requestQueryNamesAl.add("camera");
			//requestQueryNamesAl.add("testquery");
			//requestQueryNamesAl.add("nipun");
			String keywords = URLScavenger.mineKeywords(rqst, null, requestQueryNamesAl);
			System.out.println("The mined keywords using query names are : " + keywords);
			Assert.assertTrue((keywords!=null)&&(keywords.indexOf("nikon")!=-1));
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
	}
	/**
	 * Returns the constructed Ad Data request - used for test methods
	 * @param adDataCmdStr
	 * @return
	 */
	private AdDataRequest createRequestFromCommandString(String adDataCmdStr) {
		Reader r = new StringReader (adDataCmdStr);
		SexpReader lr = new SexpReader (r);
		AdDataRequest rqst = null;
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
				rqst = new AdDataRequest (e);
			} else {
				System.out.println("The request could not be parsed correctly");
				Assert.assertTrue(false);
			}
		} catch(Exception e) {
			System.out.println("Could not parse the request");
			e.printStackTrace();
		}
		return rqst;
	}
}
