package com.tumri.joz.keywordServer;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.memory.AnalyzerUtil; // for -dumpTokens

import java.io.StringReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/** Index all text files under a directory. */

public class IndexProducts
{
    static Logger log = Logger.getLogger(IndexProducts.class);

    private IndexProducts() {}

    static boolean debug = false;
    static String indexDir = "c:/lucene-2.0.0/build/index";
    static String docDir = "c:/products/all-products";
    static int mergeFactor = 10000;
    static Hashtable<String,Float> fieldBoosts = new Hashtable<String,Float>();

    // NOTE: categories are stored in lowercase
    static Set<String> deboostCategories = new HashSet<String> ();

    private static String deboostCategoryFile = null;
    // Lines beginning with this character in {deboostCategoryFile}
    // are ignored.
    private static final char FILE_COMMENT_CHAR = '#';

    /**
     * This should be called by both the indexer & the searcher so that we 
     * can be sure that they're using the same analyzer.
     */
    public static Analyzer getAnalyzer(boolean dump)
    {
	Analyzer a = new PorterStemAnalyzer(LargeStopWordList.SMART_STOP_WORDS);
	if (dump)
	    return AnalyzerUtil.getLoggingAnalyzer (a, System.err, "dump");
	else
	    return a;
    }

    /** Index all text files under a directory. */
    public static void main(String[] args)
    {
	boolean dumpTokens = false;
	String usage = "java -jar IndexProducts.jar [-h] [-debug] [-dumpTokens] [-deboostCategoryFile XXX] [-docDir XXX] [-indexDir XXX] [-mergeFactor nnn] [-boostField name value]";

	// boost these by default
	fieldBoosts.put("name",new Float(2.0));
	fieldBoosts.put("parents",new Float(2.0));
	fieldBoosts.put("superclasses",new Float(1.5));

	for (int i = 0; i < args.length; i++)
	{
	    String arg = args[i];

	    // NOTE: We let array index checking catch missing args to
	    // -docDir, etc.

	    if (arg.equals ("-h"))
	    {
	    }
	    else if (arg.equals ("-debug"))
	    {
		debug = true;
	    }
	    else if (arg.equals ("-dumpTokens"))
	    {
		dumpTokens = true;
	    }
	    else if (arg.equals ("-deboostCategoryFile"))
	    {
		deboostCategoryFile = args[++i];
	    }
	    else if (arg.equals ("-docDir"))
	    {
		docDir = args[++i];
	    }
	    else if (arg.equals ("-indexDir"))
	    {
		indexDir = args[++i];
	    }
	    else if (arg.equals ("-mergeFactor"))
	    {
		mergeFactor = Integer.parseInt(args[++i]);
	    }
	    else if (arg.equals ("-boostField"))
	    {
		String field = args[++i];
		Float boost = Float.parseFloat(args[++i]);
		log.info( "Boosting field '" + field + "' by " + boost );
		fieldBoosts.put(field,boost);
	    }
	    else
	    {
		log.info("Usage: " + usage);
		System.exit(1);
	    }
	}

	String boosts = "All field boosts:";
	for( Enumeration<String> fields = fieldBoosts.keys(); fields.hasMoreElements(); )
	{
	    String field = fields.nextElement();
	    boosts += (" " + field + ": " + fieldBoosts.get(field));
	}
	log.info( boosts );

	if (deboostCategoryFile != null)
	{
	    log.info( "Using deboost categories file " + deboostCategoryFile);
	    try
	    {
		deboostCategories = loadDeboostCategories (deboostCategoryFile);
	    }
	    catch (IOException e)
	    {
		log.fatal ("Error loading " + deboostCategoryFile);
		System.exit (1);
	    }
	}

	File indexDirF = new File(indexDir);
	if (indexDirF.exists())
	{
	    log.fatal("Cannot save index to '" +indexDir+ "' directory, please delete it first");
	    System.exit(1);
	}		
	final File docDirF = new File(docDir);
	if (!docDirF.exists() || !docDirF.canRead())
	{
	    log.fatal("Document directory '" +docDirF.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
	    System.exit(1);
	}

	try
	{
	    Date start = new Date();
	    Analyzer analyzer = getAnalyzer(dumpTokens);
	    IndexWriter writer = new IndexWriter(indexDirF, analyzer, true);
	    writer.setMergeFactor(mergeFactor);
	    log.info( "Indexing '" + docDir + "' into '" + indexDir + "' ..." );
	    indexDocs(writer, docDirF);
	    log.info( "Optimizing ..." );
	    writer.optimize();
	    writer.close();
	    log.info( ((new Date()).getTime() - start.getTime()) * 1E-3 / 60.0 + " total minutes" );
	}
	catch (IOException e)
	{
	    log.error("something screwed up: ", e );
	    // If we fail we must exit with a non-zero error code.
	    System.exit(1);
	}
    }

    private static Set<String>
    loadDeboostCategories (String file) throws IOException
    {
	Set<String> deboostSet = new HashSet<String> ();
	List<String> lines = readTxtFile (file);

	for (int i = 0; i < lines.size (); ++i)
	{
	    // Find the first non-blank char.
	    int j = 0;
	    // ??? This is slow for lists in general, but we
	    // assume we have an ArrayList.
	    String l = lines.get (i);
	    while (j < l.length ()
		   && Character.isWhitespace (l.charAt (j)))
		++j;

	    // Skip blank lines.
	    if (j == l.length ())
		continue;

	    if (l.charAt (j) == FILE_COMMENT_CHAR)
		continue;

	    // If there's whitespace at the end of the line the category
	    // will be ignored and it's really hard to debug.  So remove it.
	    int k = l.length () - 1;
	    while (k > j
		   && Character.isWhitespace (l.charAt (k)))
		--k;

	    String category = l.substring (j, k + 1);
	    if (debug)
		System.out.println ("Deboosting " + category);
	    deboostSet.add (category.toLowerCase ());
	}

	return deboostSet;
    }

    // Read in a text file and return a string list, one entry per line.

    public static List<String>
    readTxtFile (String path) throws IOException
    {
	ArrayList<String> lines = new ArrayList<String> ();

	InputStreamReader isr = new InputStreamReader (new FileInputStream (path), "utf-8");
	BufferedReader br = new BufferedReader (isr);
	String line = null;

	while ((line = br.readLine ()) != null)
	{
	    lines.add (line);
	}

	return lines;
    }

    // Remove duplicate words from {s}, while maintaining word order.
    // The order of words can be important when searching, e.g. with
    // phrase searches, so we preserve it.

    private static String
    uniqify (String s)
    {
	StringReader reader = new StringReader (s);
	Tokenizer tokenizer = new LowerAlnumTokenizer (reader);
	List<String> tokens = new ArrayList<String> ();
	Set<String> token_set = new HashSet<String> ();

	// Shouldn't get an IOException, but need to watch for it since we
	// don't throw the exception.

	try
	{
	    Token t;

	    while ((t = tokenizer.next ()) != null)
	    {
		String word = t.termText ();
		if (! token_set.contains (word))
		{
		    token_set.add (word);
		    tokens.add (word);
		}
	    }
	}
	catch (Exception e)
	{
	    // shouldn't happen
	    e.printStackTrace ();
	}

	StringBuffer sb = new StringBuffer ();
	for (int i = 0; i < tokens.size (); ++i)
	{
	    if (i > 0)
		sb.append (" ");
	    sb.append (tokens.get (i));
	}

	return sb.toString ();
    }

    // Subroutine of {indexDocs} to simplify it.

    private static void
    add_field (Document doc, String name, String value,
	       Store store, Index index)
    {
	Field f = new Field (name, value, store, index);
	Float boost = fieldBoosts.get (name);
	if (boost != null)
	    f.setBoost (boost);
	doc.add (f);
    }

    // Main routine for indexing a file.
    //
    // Files consist of multiple documents, with each document consisting of
    // lines of "attr: value".  Each document begins with "id: mumble".
    //
    // See ./README for docs on the things we try to get more usable scoring.

    static void indexDocs(IndexWriter writer, File file) throws IOException
    {
	// do not try to index files that cannot be read
	if (! file.canRead())
	    return;

	if (file.isDirectory()) 
	{
	    String[] files = file.list();
	    // NOTE: an IO error could occur
	    // ??? Does file.list() return null for empty directories
	    // or an empty array?
	    if( files != null )
		for( int i = 0; i < files.length; i++ )
		    indexDocs(writer, new File(file, files[i]));
	}
	else
	{
	    int numDocs = 0, blockSize = 1000, counter = blockSize;
	    long blockStartTime = System.nanoTime();
	    log.info("adding " + file);

	    try
	    {
		InputStreamReader isr = new InputStreamReader(new FileInputStream(file),"utf-8");
		BufferedReader br = new BufferedReader(isr);

		String line = null;
		Document doc = null;
		String alltext = null;
		String name = null;
		String brand = null;
		String parents = null;

		while( (line = br.readLine()) != null )
		{
		    int colon = line.indexOf(':');
		    //log.info("["+line+"]");
		    String attr = line.substring(0,colon);
		    attr = attr.toLowerCase ();
		    ++colon;
		    while( colon < line.length()
			   && Character.isWhitespace(line.charAt(colon)) )
			++colon;
		    String value = line.substring(colon);
		    Store store = Store.NO;
		    Index index = Index.TOKENIZED;

		    // If the next field is "id" we are starting a new
		    // "document".

		    if( attr.equals("id") )
		    {
			++numDocs;
			if( --counter == 0 )
			{
			    counter = blockSize;
			    log.info( numDocs + " docs; "
				      + blockSize / ((System.nanoTime() - blockStartTime) * 1E-9)
				      + " per/sec" );
			    blockStartTime = System.nanoTime();
			}
			//log.info("id " + value);

			if( doc != null )
			{
			    add_field (doc, "canonicalname",
				       parents + " " + brand + " " + name,
				       Store.NO, Index.TOKENIZED);
			    writer.addDocument(doc);
			}

			doc = new Document();
			alltext = null;
			name = null;
			brand = null;
			parents = null;
			store = Store.YES;
			// No point in indexing the "id" field.
			index = Index.NO;
		    }

		    if (attr.equals ("parents"))
		    {
			// The parents field ends in '.', blech.
			int dot = value.lastIndexOf ('.');
			if (dot > 0)
			    value = value.substring (0, dot);

			if (deboostCategories.contains (value.toLowerCase ()))
			{
			    // ??? The value to use here must be empirically
			    // determined.
			    doc.setBoost (0.6f);
			    if (debug)
				System.out.println ("Deboosting doc.");
			}
		    }

		    // Defer adding alltext so we can append the name field
		    // to it.
		    // WARNING: We assume "alltext" appears before "name".

		    if (attr.equals ("alltext"))
		    {
			// See ./README.
			alltext = uniqify (value);
			if (debug)
			{
			    System.out.println ("  before uniqify: " + value);
			    System.out.println ("  after uniqify:  " + alltext);
			}
			continue;
		    }

		    // Append the name field to alltext to boost searches
		    // looking for words in the name.

		    if (attr.equals ("name"))
		    {
			// While we could simplify the code a bit and add
			// multiple "alltext" fields to achieve the same thing,
			// only the first instantiation of the field is
			// retrievable.  So for debugging purposes we keep all
			// the text together.
			if (alltext != null)
			    alltext = alltext + " " + value;
			else
			    alltext = value;

			// Can't add alltext until now because we don't have
			// all of it until we see the name field.
			add_field (doc, "alltext", alltext,
				   Store.NO, Index.TOKENIZED);
		    }

		    // Collect together to fields for "canonicalname".
		    // See ./README.
		    // We duplicate tests for parents,name from above to keep
		    // the "canonicalname" code together.
		    if (attr.equals ("parents"))
			parents = value;
		    else if (attr.equals ("brand"))
			brand = value;
		    else if (attr.equals ("name"))
			name = value;

		    add_field (doc, attr, value, store, index);
		}

		if( doc != null )
		{
		    add_field (doc, "canonicalname",
			       parents + " " + brand + " " + name,
			       Store.NO, Index.TOKENIZED);
		    writer.addDocument(doc);
		}

		br.close();
	    }

	    // at least on windows, some temporary files raise this
	    // exception with an "access denied" message checking if the
	    // file can be read doesn't help
	    catch( FileNotFoundException ex )
	    {
		log.error( "this shouldn't be happening: ", ex );
	    }
	}
    }
}
