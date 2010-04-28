package com.tumri.joz.keywordServer;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.PorterStemFilter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * PorterStemAnalyzer processes input
 * text by stemming English words to their roots.
 * This Analyzer also converts the input to lower case
 * and removes stop words.  A small set of default stop
 * words is defined in the STOP_WORDS
 * array, but a caller can specify an alternative set
 * of stop words by calling non-default constructor.
 */
public class PorterStemAnalyzer extends Analyzer
{
    private static Set _stopTable;
    static Logger log = Logger.getLogger(ProductIndex.class);

    /**
     * An array containing some common English words
     * that are usually not useful for searching.
     */
    public static final String[] STOP_WORDS =
    {
        "0", "1", "2", "3", "4", "5", "6", "7", "8",
        "9", "000", "$",
        "about", "after", "all", "also", "an", "and",
        "another", "any", "are", "as", "at", "be",
        "because", "been", "before", "being", "between",
        "both", "but", "by", "came", "can", "come",
        "could", "did", "do", "does", "each", "else",
        "for", "from", "get", "got", "has", "had",
        "he", "have", "her", "here", "him", "himself",
        "his", "how","if", "in", "into", "is", "it",
        "its", "just", "like", "make", "many", "me",
        "might", "more", "most", "much", "must", "my",
        "never", "now", "of", "on", "only", "or",
        "other", "our", "out", "over", "re", "said",
        "same", "see", "should", "since", "so", "some",
        "still", "such", "take", "than", "that", "the",
        "their", "them", "then", "there", "these",
        "they", "this", "those", "through", "to", "too",
        "under", "up", "use", "very", "want", "was",
        "way", "we", "well", "were", "what", "when",
        "where", "which", "while", "who", "will",
        "with", "would", "you", "your",
        "a", "b", "c", "d", "e", "f", "g", "h", "i",
        "j", "k", "l", "m", "n", "o", "p", "q", "r",
        "s", "t", "u", "v", "w", "x", "y", "z"
    };

    /**
     * Builds an analyzer.
     */
    public PorterStemAnalyzer()
    {
        this(STOP_WORDS);
    }

    /**
     * Builds an analyzer with the given stop words.
     *
     * @param stopWords a String array of stop words
     */
    public PorterStemAnalyzer(String[] stopWords)
    {
        _stopTable = StopFilter.makeStopSet(stopWords);
    }

    /**
     * Processes the input by first converting it to
     * lower case, then by eliminating stop words, and
     * finally by performing Porter stemming on it.
     *
     * @param reader the Reader that provides access to the input text
     * @return an instance of TokenStream
     */
    public final TokenStream tokenStream(Reader reader)
    {
	// This was LowerCaseTokenizer (reader) but that strips numbers which
	// we need for model numbers.
	Tokenizer t = new LowerAlnumTokenizer (reader);
	return new PorterStemFilter (new StopFilter (t, _stopTable));
    }

    /** 
     * Creates a TokenStream which tokenizes all the text in the provided
     * Reader.  Default implementation forwards to tokenStream(Reader) for 
     * compatibility with older version.  Override to allow Analyzer to choose 
     * strategy based on document and/or field.  Must be able to handle null
     * field name for backward compatibility.
     */
    public TokenStream tokenStream(String fieldName, Reader reader)
    {
	return tokenStream(reader);
    }

    /**
     * Utility method to test the stemming logic
     * @param testStr
     * @return
     */
    public static List<String> stemPhrase(String testStr) {
        List<String> result = new ArrayList<String>();
        PorterStemAnalyzer stemmer = new PorterStemAnalyzer(STOP_WORDS);
        StringReader reader = new StringReader(testStr);
        TokenStream stream = stemmer.tokenStream(reader);

        try {
            org.apache.lucene.analysis.Token  t = stream.next();
            while (t != null) {
               result.add(t.termText());
               t = stream.next();
            }
        } catch (IOException e) {
            log.error("Exception caught on stemming" , e);
        } finally {
            try {
                stream.close();
            } catch (IOException e1) {
                log.error("Exception caught on closing the stream" , e1);
            }
        }
        return result;
    }

    //Test code for the analyzer
    public static void main(String[] args) {
        //String testStr = "abingdon,vji,va,us,galesburg municipal airport,6141,illinois,abingdon,Destabingdon,Destvji,Destva,Destus,Destgalesburg municipal airport,Dest6141,Destillinois,Destabingdon,";
        String testStr = "Portaland, OR";
        List<String> termList = PorterStemAnalyzer.stemPhrase(testStr);
        for (String s: termList) {
            System.out.println("Term: " + s);
        }
    }
}