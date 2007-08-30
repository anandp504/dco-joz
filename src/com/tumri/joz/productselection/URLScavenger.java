package com.tumri.joz.productselection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpSymbol;

/**
 * Class that holds the bizlogic for URL Scavenging 
 * @author nipun
 *
 */
public class URLScavenger {

	private static ArrayList<String> stopWordsAL = new ArrayList<String>();
	private static Logger log = Logger.getLogger (URLScavenger.class);
	
	//Internal Stop words - todo make this configurable?
	static {
		stopWordsAL.add("http");
		stopWordsAL.add("www");
		stopWordsAL.add("com");
		stopWordsAL.add("org");
		stopWordsAL.add("net");
		stopWordsAL.add("html");
		stopWordsAL.add("js");
		stopWordsAL.add("jsp");
		stopWordsAL.add("https");
	}

	/**
	 * Returns the list of keywords for a given urls
	 * @param request - the Ad data request object
	 * @param reqStopWords - the list of request Stop words as retrived from the oSpec/tSpec
	 * @param req
	 * @return
	 */
	public static String mineKeywords(AdDataRequest request, ArrayList<String>reqStopWords) {
		if (reqStopWords!=null) {
			stopWordsAL.addAll(reqStopWords);
		}
		StringBuilder builtUpKeywords = new StringBuilder();
		try {
			String publisherUrl = request.get_url();
			if (publisherUrl!=null) {
				StreamTokenizer st = new StreamTokenizer(new BufferedReader(new StringReader(publisherUrl)));
				st.ordinaryChar('/');
				st.ordinaryChar('.');
				st.ordinaryChar(',');
				st.ordinaryChar('?');
				st.eolIsSignificant(true);
				st.lowerCaseMode(true);
				int token = st.nextToken();
				while (token != StreamTokenizer.TT_EOF) {
					token = st.nextToken();
					switch (token) {
					case StreamTokenizer.TT_WORD:
						// A word was found; the value is in sval
						String word = st.sval;
						if (!stopWordsAL.contains(word)) {
							builtUpKeywords.append(word);
							stopWordsAL.add(word); //avoid dups
						}
						break;
					case '=':
						builtUpKeywords.append(' ');
						break;    
					case '?':
						break;    
					case ',':
						builtUpKeywords.append(' ');
						break;    
					case '/':
						builtUpKeywords.append(' ');
						break;    
					case '.':
						builtUpKeywords.append(' ');
						break;    
					case StreamTokenizer.TT_NUMBER:
						break;
					case ':':
						break;
					case '"':
						break;
					case '\'':
						break;
					case '#':
						break;
					case StreamTokenizer.TT_EOL:
						// End of line character found
						break;
					case StreamTokenizer.TT_EOF:
						// End of file has been reached
						break;
					default:
						// A regular character was found; the value is the token itself
						char ch = (char)st.ttype;
						builtUpKeywords.append(ch);
						break;
					}
				}
				System.out.println("The keywords are : " + builtUpKeywords.toString());
			}
		} catch (IOException e) {
			//
		}
		return builtUpKeywords.toString();
	}
	
	@Test
	public void testURLScavenging() {
		String queryStr = "(get-ad-data :url \"http://www.photography.com/camera/nikon\")";
		try {
			AdDataRequest rqst = createRequestFromCommandString(queryStr);
			String keywords = mineKeywords(rqst, null);
			Assert.assertTrue(keywords!=null);
			log.info("The mined keywords are : " + keywords);
		} catch(Exception e) {
			log.error("Could not parse the request and mine the url");
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testRequestStopWords() {
		String queryStr = "(get-ad-data :url \"http://www.photography.com/camera/canon/nikon/test\")";
		try {
			AdDataRequest rqst = createRequestFromCommandString(queryStr);
			ArrayList<String> requestStopWordsAl = new ArrayList<String>();
			requestStopWordsAl.add("canon");
			String keywords = mineKeywords(rqst, requestStopWordsAl);
			Assert.assertTrue((keywords!=null)&&(keywords.indexOf("canon")==-1));
			log.info("The mined keywords are : " + keywords);
		} catch(Exception e) {
			log.error("Could not parse the request and mine the url");
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
				log.error("command name not a symbol: " + cmd_expr.toString ());

			SexpSymbol sym = cmd_expr.toSexpSymbol ();
			String cmd_name = sym.toString ();

			// Return the right Cmd* class to handle this request.

			if (cmd_name.equals ("get-ad-data")) {
				rqst = new AdDataRequest (e);
			} else {
				log.error("The request could not be parsed correctly");
				Assert.assertTrue(false);
			}
		} catch(Exception e) {
			log.error("Could not parse the request");
			e.printStackTrace();
		}
		return rqst;
	}
}
