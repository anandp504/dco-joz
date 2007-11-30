package com.tumri.joz.productselection;

import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.utils.AppProperties;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;

/**
 * Class that holds the bizlogic for URL Scavenging 
 * Note that this code does not handle unicode characters well 
 * @author nipun
 *
 */
public class URLScavenger {

    private ArrayList<String> stopWordsAL = new ArrayList<String>();
    private static Logger log = Logger.getLogger (URLScavenger.class);
    private static final String GLOBAL_DEFAULT_STOP_WORDS = "%,^,.com .org .gov,.net,.biz,.uk,.fr,.in,.php,.html,.htm,.css,"
                                                            +  ".js,www http://,cache://,rss://,feed:// http";
    private static final String CONFIG_GLOBAL_DEFAULT_STOP_WORDS = "com.tumri.productselection.global.stopwords";
    
    
    public URLScavenger() {
        String globalStopWords = AppProperties.getInstance().getProperty(CONFIG_GLOBAL_DEFAULT_STOP_WORDS);
        if (globalStopWords == null) {
            globalStopWords = GLOBAL_DEFAULT_STOP_WORDS;
        }
        stopWordsAL = parseString(globalStopWords);
    }

    /**
     * Returns the list of keywords for a given urls
     * @param request - the Ad data request object
     * @param reqStopWordsStr - the list of request Stop words as retrived from the oSpec/tSpec
     * @param reqQueryNamesStr - the list of query names from the tSpec/oSpec
     * @return minedKeywords as a space delimited string
     */
    public static String mineKeywords(AdDataRequest request, String reqStopWordsStr, String reqQueryNamesStr) {
        URLScavenger tmpScavenger = new URLScavenger();
        boolean bQueryNames = false;
        ArrayList<String> reqStopWords = parseString(reqStopWordsStr);
        ArrayList<String> reqQueryNames = parseString(reqQueryNamesStr);

        if ((reqQueryNames != null) && (!reqQueryNames.isEmpty())) {
            bQueryNames = true;
        }
        StringBuilder builtUpKeywords = new StringBuilder();
        try {
            String publisherUrl = request.get_url();
            publisherUrl = tmpScavenger.cleanseURL(publisherUrl, reqStopWords);

            if (bQueryNames) {
                //Select the keywords using the querynames - we can ignore everything until the first '?'
                if (publisherUrl.indexOf("?") > -1) {
                    publisherUrl = publisherUrl.substring(publisherUrl.indexOf("?"), publisherUrl.length());
                }
            }
            if (publisherUrl!=null) {
                StreamTokenizer st = new StreamTokenizer(new BufferedReader(new StringReader(publisherUrl)));
                st.ordinaryChar('/');
                st.ordinaryChar('.');
                st.ordinaryChar('-');
                st.ordinaryChar(' ');
                st.ordinaryChar(',');
                st.ordinaryChar('?');
                st.eolIsSignificant(true);
                st.lowerCaseMode(true);
                boolean bQueryNameValue = false;
                boolean bSpaceAdded = false;
                int token = st.nextToken();
                while (token != StreamTokenizer.TT_EOF) {
                    token = st.nextToken();
                    switch (token) {
                        case StreamTokenizer.TT_WORD:
                            // A word was found; the value is in sval
                            if (!bQueryNames) {
                                String word = st.sval;
                                if (!tmpScavenger.stopWordsAL.contains(word)) {
                                    builtUpKeywords.append(word);
                                    bSpaceAdded = false;
                                    tmpScavenger.stopWordsAL.add(word); //avoid dups
                                }
                            } else {
                                if (bQueryNameValue) {
                                    String word = st.sval;
                                    if (!tmpScavenger.stopWordsAL.contains(word)) {
                                        builtUpKeywords.append(word);
                                        bSpaceAdded = false;
                                        tmpScavenger.stopWordsAL.add(word); //avoid dups
                                    }
                                } else {
                                    //This might be a queryname
                                    String word = st.sval;
                                    bQueryNameValue = reqQueryNames.contains(word);
                                }
                            }
                            break;
                        case '?':
                            if (bQueryNameValue) {
                                bQueryNameValue = false;
                            }
                            break;
                        case '&':
                            if (bQueryNameValue) {
                                bQueryNameValue = false;
                            }
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
                            if (!Character.isLetterOrDigit(ch)) {
                                //skip any character that is non ascii
                                if (!bSpaceAdded) {
                                    builtUpKeywords.append(' ');
                                    bSpaceAdded = true;
                                }
                            } else {
                                if (!bQueryNames) {
                                    builtUpKeywords.append(ch);
                                    bSpaceAdded = false;
                                } else {
                                    if (bQueryNameValue) {
                                        String chStr = Character.toString(ch);
                                        if (!tmpScavenger.stopWordsAL.contains(chStr)) {
                                            builtUpKeywords.append(ch);
                                            bSpaceAdded = false;
                                            tmpScavenger.stopWordsAL.add(chStr); //avoid dups
                                        }
                                    }
                                }
                            }

                            break;
                    }
                }
                log.debug("The keywords are : " + builtUpKeywords.toString());
            }
        } catch (IOException e) {
            //
        }
        return builtUpKeywords.toString().trim();
    }

    /**
     * Splits the string using comma or space
     * @param requestStr  - input string that contains the stopwords or querynames ( lowercased )
     * @return Parsed arraylist of string
     */
    private static ArrayList<String> parseString(String requestStr) {
        ArrayList<String> reqstopWordAL = new ArrayList<String>();
        if (requestStr != null&& !"".equals(requestStr))  {
            String[] splitStrArr = requestStr.split("[ ,]");
            if (splitStrArr != null) {
                for (String tmp : splitStrArr) {
                    if (tmp.length() > 0) {
                        reqstopWordAL.add(tmp.toLowerCase());
                    }
                }
            }
        }

        return reqstopWordAL;
    }

    /**
     * Cleanse the url by decoding it, and then removing all the stop words from it.
     * @param url - The request url that needs to be cleansed.
     * @return - the cleansed url
     */
    private String cleanseURL(String url, ArrayList<String> reqStopWords) {
        if (url == null || "".equals(url)) {
            return "";
        }

        if (reqStopWords!=null) {
            //Stopwords always have a + or - preceding them indicating whether to add or delete them from the global stop word list
            for (String stopWrd : reqStopWords) {
                String oprType = stopWrd.substring(0, 1);
                String theStopWord = stopWrd.substring(1);
                if (oprType.equals("+")) {
                    this.stopWordsAL.add(theStopWord);
                } else if (oprType.equals("-")) {
                    this.stopWordsAL.remove(theStopWord);
                } else {
                    //By default add
                    this.stopWordsAL.add(stopWrd);
                }
            }
        }
        try {
            url = URLDecoder.decode(url, "utf-8");

            //Sort the stopwords AL by descending order of length
            String[] stopWordsArr = this.stopWordsAL.toArray(new String[0]);
            String temp;
            for(int i=0; i<stopWordsArr.length; i++) {
                for(int j=0; j<stopWordsArr.length-1-i; j++) {
                    if(stopWordsArr[j].length() < stopWordsArr[j+1].length()) {
                        temp = stopWordsArr[j];
                        stopWordsArr[j] = stopWordsArr[j+1];
                        stopWordsArr[j+1] = temp;
                    }
                }
            }

            for (String stpWord : stopWordsArr) {
                url = replace(url.toLowerCase(),stpWord, " ");
            }

        } catch (UnsupportedEncodingException e) {
           log.error("Could not decode the url to mine the keywords", e);
           url = "";
        }
        return url.trim();
    }

    /**
     * Helper method to replace a portion of a string with another string.
     * Did not use Regex because of the escaping special chars issue
     * @param target is the original string
     * @param from  is the string to be replaced
     * @param to  is the string which will used to replace
     * @return String with the replaced values
     */
    private static String replace (String target, String from, String to) {
        int start = target.indexOf(from);
        if (start == -1) return target;
        int lf = from.length();
        char [] targetChars = target.toCharArray();
        StringBuffer buffer = new StringBuffer();
        int copyFrom = 0;
        while (start != -1) {
            buffer.append (targetChars, copyFrom, start-copyFrom);
            buffer.append (to);
            copyFrom = start + lf;
            start = target.indexOf (from, copyFrom);
        }
        buffer.append (targetChars, copyFrom, targetChars.length - copyFrom);
        return buffer.toString();
    }

}
