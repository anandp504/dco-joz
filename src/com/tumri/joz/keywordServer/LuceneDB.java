// Main class to implement lucene server for joz.

package com.tumri.joz.keywordServer;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LuceneDB
{
    public LuceneDB (String index_dir)
    {
	_index_dir = index_dir;

	try
	{
	    File f = new File (_index_dir);
	    if (f.exists () && f.isDirectory ())
	    {
		log.info ("Loading keyword index from " + _index_dir);
		_reader = IndexReader.open (_index_dir);
		_searcher = new IndexSearcher (_reader);
    _analyzer = IndexProducts.getAnalyzer (false /*no-dump*/);
		return;
	    }
	}
	catch (IOException ex)
	{
	    // error message logged below
	}

	log.error ("Bad index directory: " + _index_dir);
	log.error ("Keyword searching disbled.");
    }

    public static class LuceneResult
    {
	public LuceneResult (String id, double score)
	{
	    _id = id;
	    _score = score;
	}

	public String get_id () { return _id; }
	public double get_score () { return _score; }

	private String _id;
	private double _score;
    }

    // max_docs = 0 -> no limit

    public List<LuceneResult>
    search (String query_string, double min_score, int max_docs)
    {
	// NOTE: not thread safe!
	QueryParser parser = new QueryParser (_field, _analyzer);
	long start = System.nanoTime ();

	try
	{
	    Query query = parser.parse (query_string);
	    log.info ("Searching for: " + query.toString (_field));
	    Hits hits = _searcher.search (query);

	    long stop = System.nanoTime ();
	    log.info (hits.length () + " matching documents in "
		      + (stop - start) * 1E-9 );

	    List<LuceneResult> results = new ArrayList<LuceneResult> ();
	    int n = hits.length ();
	    if (max_docs > 0 && max_docs < n)
		n = max_docs;

	    for (int i = 0; i < n; ++i)
	    {
		Document doc = hits.doc (i);
		double score = hits.score (i);
		if (score < min_score)
		    break;
		if (log.isDebugEnabled () || _explain)
		{
		    Explanation expl = _searcher.explain (query, hits.id (i));
		    if (_explain)
		    {
			// If the user specifies -explain, don't print the
			// output with debug.  Enabling debug-level output
			// turns on too many other messages.
			log.info ("\n\n" + doc.get ("id") + " "
				  + doc.get("name") + ":");
			log.info (expl.toString ());
		    }
		    else
		    {
			log.debug ("\n\n" + doc.get("id") + " "
				   + doc.get ("name") + ":");
			log.debug (expl.toString ());
		    }
		}
		String id = doc.get ("id");
		results.add (new LuceneResult (id, score));
	    }

	    return results;
	}
	catch (Exception ex)
	{
	    log.error ("Problem handling query \"" + query_string + "\".");
	    log.error (ex);
	    return null;
	}
    }

    // implementation details -------------------------------------------------

    // directory containing index files
    String _index_dir = null;

    // misc objects to read, analyze, search the index
    IndexReader _reader = null;
    Searcher _searcher = null;
    Analyzer _analyzer = null;

    // true -> add result explanation to logging output
    private boolean _explain = false;

    // the default field to search on
    private String _field = DEFAULT_SEARCH_FIELD;

    private static Logger log = Logger.getLogger (LuceneDB.class);

    private static final String DEFAULT_SEARCH_FIELD = "alltext";

    /** Use the norms from one field for all fields.  Norms are read into memory,
     * using a byte of memory per document per searched field.  This can cause
     * search of large collections with a large number of fields to run out of
     * memory.  If all of the fields contain only a single token, then the norms
     * are all identical, then single norm vector may be shared.
     */
    private static class OneNormsReader extends FilterIndexReader
    {
	private String _field;

	public OneNormsReader (IndexReader in, String field)
	{
	    super (in);
	    _field = field;
	}

	public byte[]
	norms (String field)
	    throws IOException
	{
	    return in.norms (_field);
	}
    }
}
