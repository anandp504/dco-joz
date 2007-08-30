package com.tumri.joz.productselection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;

import com.tumri.joz.jozMain.AdDataRequest;

/**
 * Class that holds the bizlogic for URL Scavenging 
 * @author nipun
 *
 */
public class URLScavenger {

	private static ArrayList<String> stopWordsAL = new ArrayList<String>();

	static {
		stopWordsAL.add("http");
		stopWordsAL.add("www");
		stopWordsAL.add("com");
		stopWordsAL.add("org");
		stopWordsAL.add("net");
		stopWordsAL.add("html");
		stopWordsAL.add("js");
		stopWordsAL.add("jsp");
	}

	public static void main(String[] args) {
		mineKeywords(null, null);
	}
	
	/**
	 * Returns the list of keywords for a given urls
	 * @param url
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
}
