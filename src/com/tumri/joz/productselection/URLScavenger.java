package com.tumri.joz.productselection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.tumri.joz.jozMain.AdDataRequest;

/**
 * Class that holds the bizlogic for URL Scavenging 
 * Note that this code does not handle unicode characters well 
 * @author nipun
 *
 */
public class URLScavenger {

	private ArrayList<String> stopWordsAL = new ArrayList<String>();
	private static Logger log = Logger.getLogger (URLScavenger.class);
	
	public URLScavenger() {
		//Internal Stop words
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
	public static String mineKeywords(AdDataRequest request, ArrayList<String> reqStopWords, ArrayList<String> reqQueryNames) {
		URLScavenger tmpScavenger = new URLScavenger();
		if (reqStopWords!=null) {
			//Stopwords always have a + or - preceding them indicating whether to add or delete them from the global stop word list
			for (int i=0;i< reqStopWords.size();i++){
				String oprType = reqStopWords.get(i).substring(0, 1);
				String theStopWord = reqStopWords.get(i).substring(1);
				if (oprType.equals("+")) {
					tmpScavenger.stopWordsAL.add(theStopWord);
				} else if (oprType.equals("-")) {
					tmpScavenger.stopWordsAL.remove(theStopWord);
				} else {
					//By default add
					tmpScavenger.stopWordsAL.add(reqStopWords.get(i));
				}
			}
		}
		if ((reqQueryNames != null) && (!reqQueryNames.isEmpty())) {
			return tmpScavenger.buildKeywordsUsingQueryNames(request, reqQueryNames);
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
						if (!tmpScavenger.stopWordsAL.contains(word)) {
							builtUpKeywords.append(word);
							tmpScavenger.stopWordsAL.add(word); //avoid dups
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
						int num = (int)st.ttype;
						builtUpKeywords.append(num);
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
				log.debug("The keywords are : " + builtUpKeywords.toString());
			}
		} catch (IOException e) {
			//
		}
		return builtUpKeywords.toString();
	}
	
	/**
	 * returns a string of the keywords built by looking up the query name values in the string
	 * @param request
	 * @param requestQueryNames
	 * @return
	 */
	private String buildKeywordsUsingQueryNames(AdDataRequest request, ArrayList<String> reqQueryNames) {
		String publisherUrl = request.get_url();
		StringBuilder builtUpKeywords = new StringBuilder();
		if (reqQueryNames != null && publisherUrl !=null) {
			//Select the keywords using the querynames - we can ignore everything until the first '?'
			if (publisherUrl.indexOf("?") > -1) {
				publisherUrl = publisherUrl.substring(publisherUrl.indexOf("?"), publisherUrl.length());
			}
			try {
				if (publisherUrl!=null) {
					StreamTokenizer st = new StreamTokenizer(new BufferedReader(new StringReader(publisherUrl)));
					st.ordinaryChar('/');
					st.ordinaryChar('.');
					st.ordinaryChar(',');
					st.ordinaryChar('?');
					st.eolIsSignificant(true);
					st.lowerCaseMode(true);
					int token = st.nextToken();
					boolean bQueryNameValue = false;
					while (token != StreamTokenizer.TT_EOF) {
						token = st.nextToken();
						switch (token) {
						case StreamTokenizer.TT_WORD :
							// A word was found; the value is in sval
							if (bQueryNameValue) {
								String word = st.sval;
								bQueryNameValue = false;
								if (!stopWordsAL.contains(word)) {
									builtUpKeywords.append(word);
									stopWordsAL.add(word); //avoid dups
								}
							} else {
								//This might be a queryname
								String word = st.sval;
								if (reqQueryNames.contains(word)) {
									bQueryNameValue = true;				
								} else {
									bQueryNameValue = false;	
								}
							}
							break;
						case '=':
							builtUpKeywords.append(' ');
							break;    
						case '?':
							break;    
						case '&':
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
						case '+':
							builtUpKeywords.append(' ');
							break;  
						case '-':
							builtUpKeywords.append(' ');
							break;  
						case '_':
							builtUpKeywords.append(' ');
							break;  
						case '|':
							builtUpKeywords.append(' ');
							break;  
						case StreamTokenizer.TT_NUMBER:
							int num = (int)st.ttype;
							builtUpKeywords.append(num);
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
					log.debug("The keywords are : " + builtUpKeywords.toString());
				}
			} catch (IOException e) {
				//
			}
		}

		return builtUpKeywords.toString();
	}
	
	

}
