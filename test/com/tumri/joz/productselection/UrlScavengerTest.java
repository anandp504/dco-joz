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
		String queryStr = "(get-ad-data :url \"http://www.photography.com/camera/nikon\")";
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
	public void testRequestQueryNames() {
		String queryStr = "(get-ad-data :url \"http://www.photography.com/camera/canon/nikon/test?nipun=test,camera=nikon&testquery=blah\")";
		try {
			AdDataRequest rqst = createRequestFromCommandString(queryStr);
			ArrayList<String> requestQueryNamesAl = new ArrayList<String>();
			requestQueryNamesAl.add("camera");
			requestQueryNamesAl.add("testquery");
			requestQueryNamesAl.add("nipun");
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