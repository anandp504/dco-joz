package com.tumri.joz.keywordServer;

import com.tumri.content.ContentProvider;
import com.tumri.content.ContentProviderFactory;
import com.tumri.content.InvalidConfigException;
import com.tumri.content.data.Category;
import com.tumri.content.data.Content;
import com.tumri.content.data.ContentProviderStatus;
import com.tumri.content.impl.file.FileContentConfigValues;
import com.tumri.joz.index.creator.JozIndexCreator;
import com.tumri.joz.products.*;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.FSUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexModifier;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.memory.AnalyzerUtil;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: nipun
 * Maintains the lucene index of all products, provides offline means of indexing the products for lucence as well as the Joz Indexes
 */
public class ProductIndex {
    static Logger log = Logger.getLogger(ProductIndex.class);
    private static final String LUCENEDIR = "com.tumri.content.luceneDir";
    private static final String LUCENETMPDIR = "com.tumri.content.luceneTmpDir";

    private static AtomicReference<ProductIndex> g_Instance = new AtomicReference<ProductIndex>();
    private static String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "lucene";

    private boolean debug = false;
    private String indexDir = "lucene";
    private String currentDocDir = ".";
    private String prevDocDir = null;
    protected static final String MUP_FILE_FORMAT = ".*_provider-content_.*.utf8";
    static boolean dumpTokens = false;
    private static Properties mupConfig = null;
    private static String mupConfigFileName = "luceneindex.properties";



    // Lines beginning with this character in {deboostCategoryFile}
    // are ignored.
    private static final char FILE_COMMENT_CHAR = '#';
    private static int mergeFactor = 256;
    private Hashtable<String, Float> fieldBoosts = new Hashtable<String, Float>();

    // NOTE: categories are stored in lowercase
    private Set<String> deboostCategories = new HashSet<String>();

    private String deboostCategoryFile = null;
    private File m_index_dir;

    private AtomicReference<IndexSearcherCache> m_searcherCache = new AtomicReference<IndexSearcherCache>();

    /**
     * @return singleton instance of LuceneDB
     */
    public static final ProductIndex getInstance() {
        ProductIndex pi = g_Instance.get();
        if (pi == null) {
            synchronized(ProductIndex.class) {
                if (g_Instance.get() == null) {
                    init(null);
                    pi = g_Instance.get();
                }
            }
        }
        return pi;
    }

    public static void init() {
        init(AppProperties.getInstance().getProperty(FileContentConfigValues.CONFIG_SOURCE_DIR));
    }

    public static void init(String dir) {
        if (dir == null || "".equals(dir.trim())) {
            dir = "./";
        }
        File f = findProductIndex(dir);
        if (f != null) {
            File[] luceneFiles = f.listFiles();
            ArrayList<String> luceneFileNames = new ArrayList<String>();
            for (File lf : luceneFiles) {
                luceneFileNames.add(lf.getName());

            }
            ContentProviderStatus status = null;
            try {
                status = ContentProviderFactory.getInstance().getContentProvider().getStatus();
                status.luceneFileNames = luceneFileNames;
            } catch (Exception ex) {
                status = null;
            }

            ProductIndex pi = new ProductIndex(f);
            ProductIndex oldIndex = g_Instance.get();
            g_Instance.set(pi);
            if (oldIndex != null)
                oldIndex.m_searcherCache.get().close();
        }
    }

    private static File findProductIndex(String dir) {
        File f = new File(dir);
        if (!f.exists()) {
            log.error("Directory doesn't exist: " + dir);
            return null;
        }
        String cannonicalPath = null;
        try {
            cannonicalPath = f.getCanonicalPath();
        } catch (IOException e) {
            // Shouldn't happen.
            log.error("Directory doesn't exist: " + dir);
            return null;
        }
        String luceneSubDir = AppProperties.getInstance().getProperty(LUCENEDIR);
        String luceneDir = cannonicalPath + (cannonicalPath.endsWith("/")?"":"/") + ((luceneSubDir == null)?"":luceneSubDir);
        f = new File(luceneDir);
        if (!f.exists()) {
            log.error("Lucene index dir doesn't exist: "+ luceneDir);
            return null;
        }
        return f;
    }


    /**
     * The public method for constructor should not be used
     * Use getInstance() to get an instance of the class
     */
    public ProductIndex() {
        super();
    }

    private ProductIndex(File f) {
        try {
            if (f.exists() && f.isDirectory()) {
                m_index_dir = f;
                File tmpDir = FSUtils.createTMPDir(getTmpDir(),f.getName());
                try {
                    FSUtils.copyDir(f,tmpDir);
                    m_index_dir = tmpDir;
                } catch (IOException e) {
                    log.fatal("Failed to create a temporary directory for lucene.");
                }
                log.info("Loading keyword index from " + m_index_dir.getAbsolutePath());
                m_searcherCache.set(new IndexSearcherCache(m_index_dir));
            } else {
                log.error("Bad index directory: " + f.getAbsolutePath());
                log.error("Keyword searching disbled.");
            }
        }
        catch (IOException ex) {
            log.error("Caught an exception while opening the index",ex);
        }
    }

    public ArrayList<Handle> search(String query_string, double min_score, int max_docs) {
        ArrayList<Handle> alist = new ArrayList<Handle>();
        ProductDB db = ProductDB.getInstance();
        IndexSearcherCache searcherCache = m_searcherCache.get();
        IndexSearcher searcher = searcherCache.get();
        if (searcher != null) {
            try {
                Query q = createQuery(query_string);
                if (q != null) {
                    TopDocCollector tdc = new TopDocCollector(max_docs);
                    searcher.search(q, tdc);
                    db.readerLock();
                    try {
                        TopDocs topdocs = tdc.topDocs();
                        if (topdocs != null) {
                            int len = topdocs.scoreDocs.length;
                            for (int i = 0; i < len; i++) {
                                Document doc = searcher.doc(topdocs.scoreDocs[i].doc);
                                String id = doc.get("id");
                                double score = topdocs.scoreDocs[i].score;
                                long oid = Long.parseLong(id);
                                Handle ph = db.getProdHandle(oid); // avoids too many calls to lock/unlock
                                if (ph != null) {
                                    alist.add(new ProductHandle(score,ph.getOid()));
                                }
                                if (score < min_score) break;
                            }
                        }
                    } finally {
                        db.readerUnlock();
                    }
                }
            } catch (IOException e) {
                searcherCache.close(searcher);
                searcher = null; // Important to avoid the finally put clause
                log.error("Exception", e);
            } finally {
                searcherCache.put(searcher);
            }
        }
        //System.out.println("returned "+alist.size() + " products");
        return alist;
    }

    private Query createQuery(String str) {
        QueryParser qp = new QueryParser("description", getAnalyzer(false));
        try {
            str = cleanseQueryString(str);
            return qp.parse(str);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Replace are whitespace control chars or Control-M with spaces
     * @param queryStr
     * @return
     */
    private String cleanseQueryString(String queryStr){
        if (queryStr!=null) {
            queryStr = queryStr.replaceAll("[\\cM\\s]", " ");
        }
        return queryStr;
    }

    /*
    * The following code is used to generate lucene index.
    */

    private Set<String> loadDeboostCategories(String file) throws IOException {
        Set<String> deboostSet = new HashSet<String>();
        List<String> lines = readTxtFile(file);

        for (int i = 0; i < lines.size(); ++i) {
            // Find the first non-blank char.
            int j = 0;
            // ??? This is slow for lists in general, but we
            // assume we have an ArrayList.
            String l = lines.get(i);
            while (j < l.length()
                    && Character.isWhitespace(l.charAt(j)))
                ++j;

            // Skip blank lines.
            if (j == l.length())
                continue;

            if (l.charAt(j) == FILE_COMMENT_CHAR)
                continue;

            // If there's whitespace at the end of the line the category
            // will be ignored and it's really hard to debug.  So remove it.
            int k = l.length() - 1;
            while (k > j
                    && Character.isWhitespace(l.charAt(k)))
                --k;

            String category = l.substring(j, k + 1);
            if (debug)
                System.out.println("Deboosting " + category);
            deboostSet.add(category.toLowerCase());
        }

        return deboostSet;
    }

    // Read in a text file and return a string list, one entry per line.
    private List<String> readTxtFile(String path) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();

        InputStreamReader isr = new InputStreamReader(new FileInputStream(path), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String line = null;

        while ((line = br.readLine()) != null) {
            lines.add(line);
        }

        return lines;
    }

    private void add_field(Document doc, String name, String value, Field.Store store, Field.Index index) {
        Field f = new Field(name, value, store, index);
        Float boost = fieldBoosts.get(name);
        if (boost != null)
            f.setBoost(boost);
        doc.add(f);
    }

    /**
     * This method is not used anymore. It has been left here because this is invoked by the add_document api
     * See the getDocument(HashMap) method for current implementation
     * @param p
     * @return
     */
    private Document getDocument(IProduct p) {
        Document doc = new Document();
        String cat = "";
        com.tumri.content.data.Taxonomy t = JOZTaxonomy.getInstance().getTaxonomy();
        if (t != null) {
            Category c  = t.getCategory(p.getCategoryStr());
            if (c != null) {
                cat = c.getName();
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(cat).append(" ").append(p.getBrandStr()).append(" ").append(p.getProductName()).append(" ");
        sb.append(p.getDescription());
        add_field(doc, "category", cat , Field.Store.YES, Field.Index.UN_TOKENIZED);
        add_field(doc, "brand", p.getBrandStr() , Field.Store.YES, Field.Index.TOKENIZED);
        add_field(doc, "name", p.getProductName() , Field.Store.YES, Field.Index.TOKENIZED);
        add_field(doc, "description", sb.toString() , Field.Store.NO, Field.Index.TOKENIZED);
        add_field(doc, "id", Long.toString(p.getId()) , Field.Store.YES, Field.Index.UN_TOKENIZED);

        return doc;
    }


    /**
     * This should be called by both the indexer & the searcher so that we
     * can be sure that they're using the same analyzer.
     * @param dump boolean
     * @return Analyzer object
     */
    private Analyzer getAnalyzer(boolean dump) {
        Analyzer a = new PorterStemAnalyzer(LargeStopWordList.SMART_STOP_WORDS);
        if (dump)
            return AnalyzerUtil.getLoggingAnalyzer(a, System.err, "dump");
        else
            return a;
    }

    private void initLuceneIndexProperties() {
        InputStream is = null;
        try {
            mupConfig = new Properties();
            is = ProductIndex.class.getClassLoader().getResourceAsStream(mupConfigFileName);
            mupConfig.load(is);
        } catch (IOException e) {
            log.error("Exception caught during the init of the ProductIndex", e);
        } catch (Exception e) {
            log.error("Exception caught during the init of the ProductIndex", e);
        } finally {
            try {
                is.close();
            } catch(Exception e) {
                log.error("Exception caught", e);
            }
        }
    }

    /**
     * Read the mup files in the given dir and index the docs
     * @param writer
     * @param dir
     * @throws IOException
     */
    private void indexDocs(IndexWriter writer, File dir) throws IOException {

        File[] files = dir.listFiles();
        List<File> newMupFiles = new ArrayList<File>();
        for (File f: files) {
            if (f.getName().matches(MUP_FILE_FORMAT)) {
                newMupFiles.add(f);
            }
        }

        if (newMupFiles.size() == 0) {
            throw new IOException("No new provider content data found in directory: " + dir.getAbsolutePath());
        }
        for (File mup: newMupFiles) {
            FileInputStream fir1=null;
            InputStreamReader isr1=null;
            BufferedReader br1=null;

            initLuceneIndexProperties();
            if (mupConfig == null) {
                throw new RuntimeException("Could not load the luceneindex.properties. Aborting indexing");
            }
            try {
                fir1 = new FileInputStream(mup);
                isr1 = new InputStreamReader(fir1, "utf8");
                br1 = new BufferedReader(isr1);
                boolean eof1 = false;
                String line1;
                HashMap<String, String> prodDetailsMap1;
                int i=0;
                while(!eof1) {
                    line1 = br1.readLine();
                    if (line1 == null) {
                        eof1 = true;
                        continue;
                    } else {
                        i++;
                        prodDetailsMap1 = convertLine(line1);
                        prodDetailsMap1.put("id", getProductID(line1));
                        writer.addDocument(getDocument(prodDetailsMap1));
                        if (i%1000000==0){
                            log.info("Processing count = " + i);
                        }
                    }
                }
                log.info("Total number documents added to lucene = " + i);
            } finally {
                if (br1 != null) {
                    br1.close();
                }
                if (isr1!=null) {
                    isr1.close();
                }
                if (fir1 != null) {
                    fir1.close();
                }
            }

        }
    }

    /**
     * Get the Product ID from the given line. The PID is assumed to be the first column, and we drop any chracters that
     * it starts with and convert the rest into a Long.
     * @param line
     * @return
     */
    private String getProductID(String line) {
        //Find the first column
        String pidStr =line.substring(0,line.indexOf('\t'));
        char[] pidCharArr = pidStr.toCharArray();
        //Drop any non digit characters
        StringBuffer spid = new StringBuffer();
        for (char ch: pidCharArr) {
            if (Character.isDigit(ch)) {
                spid.append(ch);
            }
        }
        return spid.toString();
    }


    private Document getDocument(HashMap<String, String> pDetails) {
        Document doc = new Document();
        String cat = "";
        com.tumri.content.data.Taxonomy t = JOZTaxonomy.getInstance().getTaxonomy();
        if (t != null) {
            Category c  = t.getCategory(pDetails.get("category"));
            if (c != null) {
                cat = c.getName();
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(cat).append(" ").append(pDetails.get("brand")).append(" ").append(pDetails.get("name")).append(" ");
        sb.append(pDetails.get("description"));
        add_field(doc, "category", cat , Field.Store.YES, Field.Index.UN_TOKENIZED);
        add_field(doc, "brand", pDetails.get("brand") , Field.Store.YES, Field.Index.TOKENIZED);
        add_field(doc, "name", pDetails.get("name") , Field.Store.YES, Field.Index.TOKENIZED);
        add_field(doc, "description", sb.toString() , Field.Store.NO, Field.Index.TOKENIZED);
        add_field(doc, "id", pDetails.get("id") , Field.Store.YES, Field.Index.UN_TOKENIZED);

        return doc;
    }

    /**
     * Read the line and build the list of values based on the pos that is being indexed.
     * @param line
     * @return
     */
    protected HashMap<String,String> convertLine(String line) {
        HashMap<String,String> retVal = null;
        if (mupConfig==null) {
            return null;
        }
        Set indexPosSet = mupConfig.keySet();
        if (line != null && !"".equals(line.trim())) {
            com.tumri.utils.strings.StringTokenizer str = new com.tumri.utils.strings.StringTokenizer(line,'\t');
            ArrayList<String> strings = str.getTokens();
            retVal = new HashMap<String,String>();
            for (int i=0;i<strings.size();i++) {
                if (indexPosSet.contains(new Integer(i).toString())) {
                    String val = strings.get(i);
                    String key = mupConfig.getProperty(new Integer(i).toString());
                    retVal.put(key,val);
                }
            }
        }
        return (retVal);
    }

    private void initJozTaxonomy(File file) throws IOException {
        ContentProviderFactory f;
        Properties props = new Properties();
        props.setProperty(FileContentConfigValues.CONFIG_PRODUCTS_DIR, file.getCanonicalPath());
        props.setProperty(FileContentConfigValues.CONFIG_TAXONOMY_DIR, file.getCanonicalPath());
        props.setProperty(FileContentConfigValues.CONFIG_DISABLE_MERCHANT_DATA,"true");
        props.setProperty(FileContentConfigValues.CONFIG_DISABLE_MUP,"true");
        ContentProviderFactory.initialized = false;
        try {
            f = ContentProviderFactory.getInstance();
            f.init(props);
            ContentProvider p = f.getContentProvider();
            Content data = p.getContent();
            JOZTaxonomy tax = JOZTaxonomy.getInstance();
            tax.setTaxonomy(data.getTaxonomy().getTaxonomy());
        } catch(InvalidConfigException e) {
            log.error("Taxnomy load failed. ", e);
        }

    }


    /**
     * Index all text files under a directory.
     */
    public void index(String[] args) {
        String usage = "java -jar joz.jar [-h] [-debug] [-dumpTokens] [-deboostCategoryFile XXX] [-docDir XXX] [-previousDocDir XXX] [-indexDir XXX] [-jozindexDir XXX] [-maxLinesPerChunk XX] [-mergeFactor nnn] [-boostField name value]";
        String jozIndexDir = null;

        File f = new File(getTmpDir());
        f.mkdir();

        // boost these by default
        fieldBoosts.put("name", new Float(2.0));
        fieldBoosts.put("parents", new Float(2.0));
        fieldBoosts.put("superclasses", new Float(1.5));
        int maxLinesPerChunk = 50000;
        boolean bDisableLucene = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            // NOTE: We let array index checking catch missing args to
            // -currentDocDir, etc.

            if (arg.equals("-h")) {
                System.out.println("Usage: " + usage);
                System.exit(0);
            } else if (arg.equals("-debug")) {
                debug = true;
            } else if (arg.equals("-dumpTokens")) {
                dumpTokens = true;
            } else if (arg.equals("-deboostCategoryFile")) {
                deboostCategoryFile = args[++i];
            } else if (arg.equals("-docDir")) {
                currentDocDir = args[++i];
            } else if (arg.equals("-previousDocDir")) {
                prevDocDir = args[++i];
            } else if (arg.equals("-indexDir")) {
                indexDir = args[++i];
            } else if (arg.equals("-jozindexDir")) {
                jozIndexDir = args[++i];
            } else if (arg.equals("-mergeFactor")) {
                mergeFactor = Integer.parseInt(args[++i]);
            } else if (arg.equals("-boostField")) {
                String field = args[++i];
                Float boost = Float.parseFloat(args[++i]);
                log.info("Boosting field '" + field + "' by " + boost);
                fieldBoosts.put(field, boost);
            } else if (arg.equals ("-maxLinesPerChunk")) {
                maxLinesPerChunk = Integer.parseInt(args[++i]);
            } else if (arg.equals ("-disableLucene")) {
                bDisableLucene = true;
            } else {
                log.info("Usage: " + usage);
                System.exit(1);
            }
        }

        String boosts = "All field boosts:";
        for (Enumeration<String> fields = fieldBoosts.keys(); fields.hasMoreElements();) {
            String field = fields.nextElement();
            boosts += (" " + field + ": " + fieldBoosts.get(field));
        }
        log.info(boosts);

        if (deboostCategoryFile != null) {
            log.info("Using deboost categories file " + deboostCategoryFile);
            try {
                deboostCategories = loadDeboostCategories(deboostCategoryFile);
            }
            catch (IOException e) {
                log.fatal("Error loading " + deboostCategoryFile);
                System.exit(1);
            }
        }

        boolean bBuildJozIndex = false;
        File jozindexDirF = null;
        File oldDataDocDirF = null;
        File luceneIdxDirF = null;

        if (!bDisableLucene) {
            luceneIdxDirF = new File(indexDir);
            if (luceneIdxDirF.exists()) {
                log.fatal("Cannot save lucene index to '" + indexDir + "' directory, please delete it first");
                System.exit(1);
            }
        }

        final File currdocDirF = new File(currentDocDir);
        if (!currdocDirF.exists() || !currdocDirF.canRead()) {
            log.fatal("Document directory '" + currdocDirF.getAbsolutePath() + " does not exist or is not readable, please check the path");
            System.exit(1);
        }


        if (jozIndexDir!=null) {
            //Building the Joz Indexes
            bBuildJozIndex = true;
            jozindexDirF = new File(jozIndexDir);
            if (!jozindexDirF.exists())
            {
                log.info("Creating dir '" +jozindexDirF+ "'");
                jozindexDirF.mkdirs();
            } else {
                //Clear the directory
                FSUtils.removeFiles(jozindexDirF, true);
            }
            if (prevDocDir!=null) {
                oldDataDocDirF = new File(prevDocDir);
                if (!oldDataDocDirF.exists() || !oldDataDocDirF.canRead())
                {
                    log.info("Previous Document directory '" +prevDocDir+ "' does not exist, indexing will not use previous content data");
                    oldDataDocDirF = null;
                }
            }
        }

        try {
            Date start = new Date();
            if (!bDisableLucene) {
                log.info("Creating lucene indexes");
                createLuceneIndexes(currdocDirF, oldDataDocDirF, luceneIdxDirF , dumpTokens);
            }
            if (bBuildJozIndex) {
                log.info("Creating joz indexes");
                createJozIndexes(currentDocDir, prevDocDir, jozIndexDir, maxLinesPerChunk);
            }
            log.info("Completed in : " + ((new Date()).getTime() - start.getTime()) * 1E-3 / 60.0 + " total minutes");
        }
        catch (Throwable e) {
            log.error("something screwed up: ", e);
            // If we fail we must exit with a non-zero error code.
            System.exit(-1);
        }

    }

    private static void createJozIndexes(String newDataDir, String oldDataDir, String indexDir, int maxLinesPerChunk) {
        JozIndexCreator jic = new JozIndexCreator(newDataDir, oldDataDir, indexDir,maxLinesPerChunk);
        jic.createJozIndexes();
    }

    /**
     * Create lucene indexes for each provider, if needed - and then merge them all together
     */
    private void createLuceneIndexes(File currDoc, File prevDoc, File indexDir, boolean dumpTokens) throws IOException {
        //1. Look at each dir, find out each provider files
        File currDataDir = new File(currDoc.getAbsolutePath() + "/data");
        if (!currDataDir.exists() || !currDataDir.isDirectory()) {
            throw new IOException("Directory " + currDataDir.getAbsolutePath() + " doesn't exist or is not a directory");
        }
        File provBaseIndexDir = new File(currDoc.getAbsolutePath() + "/provLucene");
        provBaseIndexDir.mkdir();

        File[] files = currDataDir.listFiles();
        List<File> newMupFiles = new ArrayList<File>();
        for (File f: files) {
            if (f.getName().matches(MUP_FILE_FORMAT)) {
                newMupFiles.add(f);
            }
        }
        if (newMupFiles.size() == 0) {
            throw new IOException("No new provider content data found in directory: " + currDataDir.getAbsolutePath());
        }

        List<File> oldMupFiles = null;

        if (prevDoc != null && !"".equals(prevDoc)) {
            File prevDataDir = new File(prevDoc.getAbsolutePath() + "/data");
            if (!prevDataDir.exists() || !prevDataDir.isDirectory()) {
                throw new IOException("Directory " + prevDataDir.getAbsolutePath() + " doesn't exist or is not a directory");
            }
            File[] oldFiles = prevDataDir.listFiles();
            oldMupFiles = new ArrayList<File>();
            for (File f: oldFiles) {
                if (f.getName().matches(MUP_FILE_FORMAT)) {
                    oldMupFiles.add(f);
                }
            }
            if (oldMupFiles.size() == 0) {
                throw new IOException("No old provider content data found in directory: " + prevDataDir.getAbsolutePath());
            }
        }

        File[] sortedFiles = newMupFiles.toArray(new File[0]);
        ArrayList<RAMDirectory> provIndexes = new ArrayList<RAMDirectory>();
        Arrays.sort(sortedFiles);
        initJozTaxonomy(currDataDir);

        for (File f: sortedFiles) {
            String providerName = getProviderFromFileName(f.getName());
            if (prevDoc!=null) {
                File oldFile = findOldMupFile(f, oldMupFiles);
                if (oldFile!=null && oldFile.getName().equals(f.getName())) {
                    File prevLuceneDir = new File(prevDoc.getAbsolutePath() + "/provLucene/");
                    File provLuceneIndex = null;
                    if (prevLuceneDir.exists()) {
                        provLuceneIndex = findOldLuceneIndex(providerName, prevLuceneDir);
                    }
                    if (provLuceneIndex!=null) {
                        log.info("Going to use the previous lucene index for : " + providerName);
                        File newProvLuceneIndex = new File(provBaseIndexDir.getAbsolutePath() + "/" + providerName);
                        newProvLuceneIndex.mkdir();
                        //Delete any existing indexes there
                        FSUtils.removeFiles(newProvLuceneIndex, false);
                        FSUtils.copyDir(provLuceneIndex, newProvLuceneIndex);
                        provIndexes.add(new RAMDirectory(provBaseIndexDir.getAbsolutePath() + "/" + providerName));
                        continue;
                    }
                }
            }
            //Create the index for this provider
            File tmpDocDir = FSUtils.createTMPDir(getTmpDir(),providerName);
            tmpDocDir.mkdir();
            FSUtils.copyFile(f, new File(tmpDocDir.getAbsolutePath()+ "/" + f.getName()));
            //Create the lucene index for provider
            File provLuceneIndex = createProvLuceneIndex(provBaseIndexDir, tmpDocDir,providerName, dumpTokens);
            provIndexes.add(new RAMDirectory(provLuceneIndex.getAbsolutePath()));
            FSUtils.removeDir(tmpDocDir);
        }

        //Merge all the indexes
        mergeIndexes(indexDir,provIndexes.toArray(new RAMDirectory[0]) , dumpTokens);
    }

    private File createProvLuceneIndex(File indexDir, File docDir, String providerName, boolean dumpTokens) throws IOException {
        File newProviderIndexDir = new File(indexDir.getAbsolutePath() + "/" + providerName);
        newProviderIndexDir.mkdirs();
        Analyzer analyzer = getAnalyzer(dumpTokens);
        IndexWriter writer = new IndexWriter(newProviderIndexDir, analyzer, true);
        writer.setMergeFactor(mergeFactor);
        log.info("Indexing '" + docDir + "' into '" + indexDir + "' ...");
        indexDocs(writer, docDir);
        log.info("Optimizing ...");
        writer.optimize();
        writer.close();
        return newProviderIndexDir;
    }

    /**
     * Helper method to find the old MUP from the given list of files.
     * @param currentFile
     * @param oldFiles
     * @return
     */
    private File findOldMupFile(File currentFile,List<File> oldFiles) {
        String fileNamePrefix = getProviderFromFileName(currentFile.getName());
        File result = null;
        for(File f: oldFiles) {
            String oldProvName = getProviderFromFileName(f.getName());
            if (oldProvName.equals(fileNamePrefix)) {
                result = f;
                break;
            }
        }
        return result;
    }

    /**
     * Gets the provider name from a given mup file by tokenizing by _ char
     * It is assumed that the provider name will be between the first _ char and the 5th _ char from the end
     * Returns an empty string if the file name was not of correct syntax
     * @param fileName - mup file name
     * @return - provider name
     */
    private static String getProviderFromFileName(String fileName) {
        String providerName = "";
        if (fileName!=null) {
            String[] parts = fileName.split("_");
            if (parts.length<7) {
                return "";
            }
            for (int i=1; i<parts.length-5; i++) {
                String delim = "";
                if (i>1) {
                    delim = "_";
                }
                providerName = providerName + delim + parts[i];
            }
        }
        return providerName;
    }

    /**
     * Locate the prev lucene index for the given provider
     * @param providerName
     */
    private static File findOldLuceneIndex(String providerName, File prevDocDir) {
        File[] oldLuceneIdxArr = prevDocDir.listFiles();
        File idxDir = null;
        for (int i=0;i<oldLuceneIdxArr.length;i++) {
            File currDocDir = oldLuceneIdxArr[i];
            if (currDocDir.isDirectory()) {
                if (currDocDir.getName().equals(providerName)) {
                    idxDir = currDocDir;
                    break;
                }
            }
        }
        return idxDir;
    }


    /**
     * Method to merge the multiple index files into one index
     * @param arrIndexes
     */
    private void mergeIndexes(File indexDirF , RAMDirectory[] arrIndexes, boolean dumpTokens) {
        try {
            log.info("Creating Merged index...");
            Date start = new Date();
            Analyzer analyzer = getAnalyzer(dumpTokens);
            IndexWriter writer = new IndexWriter(indexDirF, analyzer, true);
            writer.setMergeFactor(mergeFactor);
            writer.addIndexes(arrIndexes);
            log.info("Optimizing Merged index...");
            writer.optimize();
            writer.close();
            log.info("Successfully Merged lucene index... Time Taken : " + ((new Date()).getTime() - start.getTime()) * 1E-3 / 60.0 + " total minutes");
        } catch (IOException e) {
            log.error("Exception caught when merging the indexes",e );
	        System.exit(-1);
        }
    }

    public static void main(String[] args) {
        new ProductIndex().index(args);
    }

    @Test
    public void test() {
        long start = System.currentTimeMillis();
        for (int i=0;i<10;i++) {
            init();
            ProductIndex pi = getInstance();
            try {
                IndexSearcher searcher = pi.m_searcherCache.get().get();
                Query q = createQuery("HPMICROBIZ1");
                TopDocCollector tdc = new TopDocCollector(2000);
                searcher.search(q, tdc);
                TopDocs topdocs = tdc.topDocs();
                if (topdocs != null) {
                    int len = topdocs.scoreDocs.length;
                    for (int j = 0; j < len; j++) {
                        Document doc = searcher.doc(topdocs.scoreDocs[j].doc);
                        String id = doc.get("id");
                        double score = topdocs.scoreDocs[j].score;
                        int oid = Integer.parseInt(id);
                    }
                }
                pi.m_searcherCache.get().put(searcher);
            } catch (IOException e) {
            }
        }
        System.out.println("Time is "+(System.currentTimeMillis()-start));
        Runtime.getRuntime().gc();
        Runtime.getRuntime().gc();
        try {
            Thread.sleep(360);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * Currently this code is not used, the intention is to allow dynamic addition of products
     * @param products
     */
    public void addProducts(ArrayList<IProduct> products) {
        IndexModifier index_modifier = null;
        try {
            index_modifier = new IndexModifier(m_index_dir,getAnalyzer(false),false);
            for (int i = 0; i < products.size(); i++) {
                IProduct p = products.get(i);
                Document doc = getDocument(p);
                index_modifier.addDocument(doc,getAnalyzer(false));
            }
            index_modifier.close();
            IndexSearcherCache isc = m_searcherCache.get();
            m_searcherCache.set(new IndexSearcherCache(m_index_dir));
            isc.close();
        } catch (IOException e) {
            log.error("Exception while adding products",e);
        } finally {
            try {
                if (index_modifier != null) index_modifier.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Currently this code is not used, the intention is to allow dynamic addition of products
     * @param products
     */
    public void deleteProducts(ArrayList<IProduct> products) {
        IndexModifier index_modifier = null;
        try {
            index_modifier = new IndexModifier(m_index_dir,getAnalyzer(false),false);
            for (int i = 0; i < products.size(); i++) {
                IProduct p = products.get(i);
                index_modifier.deleteDocuments(new Term("id",p.getGId()));
            }
            index_modifier.close();
            IndexSearcherCache isc = m_searcherCache.get();
            m_searcherCache.set(new IndexSearcherCache(m_index_dir));
            isc.close();
        } catch (IOException e) {
            log.error("Exception while deleting products",e);
        } finally {
            try {
                if (index_modifier != null) index_modifier.close();
            } catch (IOException e) {
            }
        }
    }

    private static String getTmpDir() {
        return tmpDir;
    }

    /**
     * Recursively delete all the files in the lucene tmp directory
     */
    static {
        String str = null;
        try {
            str = AppProperties.getInstance().getProperty(LUCENETMPDIR);
        } catch (Exception e) {
            //Ignore
        }
        if (str != null && "".equals(str.trim())) {
            tmpDir = str;
        }
        File f = new File(getTmpDir());
        FSUtils.removeFiles(f,true);  // rm -r /tmp/lucene/*
    }

}

class IndexSearcherCache {
    static Logger log = Logger.getLogger(IndexSearcherCache.class);
    LinkedList<IndexSearcher> m_list = new LinkedList<IndexSearcher>();
    AtomicInteger m_references = new AtomicInteger(0); // number of referers to m_ramDirectory
    File m_dir;
    boolean m_close = false;
    RAMDirectory m_ramDir;

    IndexSearcherCache(File dir) throws IOException {
        m_dir = dir;
        m_ramDir = new RAMDirectory(m_dir);
        // m_ramDir.setLockFactory(new NoLockFactory()); @todo this needs to be tested
        IndexSearcher searcher = createSearcher();
        if (searcher != null)
            m_list.add(searcher);
    }


    /**
     * @return IndexSearcher object or NULL
     */
    IndexSearcher get() {
        IndexSearcher searcher = null;
        synchronized (this) {
            searcher =  (m_list.isEmpty() ? createSearcher() : m_list.removeFirst());
        }
        return searcher;
    }

    void put(IndexSearcher searcher) {
        if (searcher != null) {
            synchronized (this) {
                if (!m_close) {
                    m_list.add(searcher);
                } else {
                    close(searcher);
                }
            }
        }
    }

    void close() {
        synchronized (this) {
            m_close = true;
            IndexSearcher searcher;
            while((searcher = get()) != null) {
                close(searcher);
            }
        }
    }

    void close(IndexSearcher searcher) {
        try {
            if (searcher != null)
                searcher.close();
        } catch (IOException e) {
            log.error("IO exception while closing IndexSearcher",e);
        }
        if (searcher != null && m_references.decrementAndGet() <= 0) {
            try {
                m_ramDir.close();
            } catch (Exception e) {
            }
        }
    }

    IndexSearcher createSearcher() {
        if (!m_close) {
            try {
                IndexSearcher searcher = new IndexSearcher(m_ramDir);
                m_references.incrementAndGet();
                return searcher;
            } catch (IOException e) {
                log.error("IO exception while creating IndexSearcher",e);
            }
        }
        return null;
    }
}