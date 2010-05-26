package com.tumri.joz.keywordServer;

import com.tumri.content.ContentProviderFactory;
import com.tumri.content.InvalidConfigException;
import com.tumri.content.data.ContentProviderStatus;
import com.tumri.content.impl.file.FileContentConfigValues;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.ShellCommand;
import com.tumri.utils.FSUtils;
import com.tumri.utils.data.RWLockedTreeMap;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.memory.AnalyzerUtil;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: nipun
 * Maintains the lucene index of all products, provides offline means of indexing the products for lucence as well as the Joz Indexes
 */
public class ProductIndex {
    static Logger log = Logger.getLogger(ProductIndex.class);
    private static final String PROVLUCENEDIR = "com.tumri.content.provLuceneDir";
    private static final String LUCENETMPDIR = "com.tumri.content.luceneTmpDir";
    private static final String LUCENEDIRPATTERN = "com.tumri.content.luceneDir";
    private static final String CONFIG_LUCENE_CACHE_SIZE = "com.tumri.content.lucene.cache.size";

    private static AtomicReference<ProductIndex> g_Instance = new AtomicReference<ProductIndex>();
    private static String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "lucene";

    private static int mergeFactor = 256;
    private File m_index_dir;

    // Map m_map maintains map from Advertisername -> SearchCache
    private RWLockedTreeMap<String, IndexSearcherCache> m_map = new RWLockedTreeMap<String, IndexSearcherCache>();
    private static final String DEFAULT = "DEFAULT";
    private static int MAX_SECONDARY_CACHE_SIZE = 3;

    /**
     * @return singleton instance of LuceneDB
     */
    public static final ProductIndex getInstance() {
        ProductIndex pi = g_Instance.get();
        if (pi == null) {
            synchronized(ProductIndex.class) {
                if (g_Instance.get() == null) {
                    init();
                    pi = g_Instance.get();
                }
            }
        }
        return pi;
    }

    public static void init() {
        init(AppProperties.getInstance().getProperty(FileContentConfigValues.CONFIG_SOURCE_DIR),
                tmpDir);
        try {
            MAX_SECONDARY_CACHE_SIZE = Integer.parseInt(AppProperties.getInstance().getProperty(CONFIG_LUCENE_CACHE_SIZE));
        } catch(Exception e) {
            MAX_SECONDARY_CACHE_SIZE = 3;
        }
    }

    /**
     * Initialize the product index, load all the provider indexes
     * @param dir
     */
    public static void init(String dir, String indexDir) {
        log.info("Going to load all lucene indexes within : " + dir);
        if (dir == null || "".equals(dir.trim())) {
            dir = "./";
        }

        //Find all the lucene directories under the base dir
        List<File> luceneDirs = findProductIndex(dir,null);

        if (!luceneDirs.isEmpty()) {
            ArrayList<String> luceneFileNames = new ArrayList<String>();
            for (File lf : luceneDirs) {
                luceneFileNames.add(lf.getName());
                ContentProviderStatus.getInstance().luceneFileNames.put(lf.getParentFile().getName(), lf.getAbsolutePath());
            }

            File fIndexDir = new File(indexDir);
            if (fIndexDir.exists()){
                FSUtils.removeFiles(fIndexDir, true);
            }
            ProductIndex pi = null;
            try {
                pi = new ProductIndex(luceneDirs);
            } catch(Exception e) {
                log.error("Could not create the product index after merge", e);
            }
            ProductIndex oldIndex = g_Instance.get();
            g_Instance.set(pi);
            if (oldIndex != null)  {
                RWLockedTreeMap<String, IndexSearcherCache> oldMap = oldIndex.m_map;
                oldMap.writerLock();
                try {
                    for(String k: oldMap.keySet()) {
                        oldMap.get(k).close();
                    }
                } finally {
                    oldMap.writerUnlock();
                }
            }
        } else {
            log.error("No lucene directories found to load");
        }
    }

    /**
     * API to load the index for a given advertiser
     * @param advertiser
     */
    public static void loadIndexForAdvertiser(String advertiser) {
        if (advertiser==null){
            init();
        } else {
            //Locate the lucene dir for that advertiser
            advertiser = advertiser.toUpperCase();
            log.info("Going to load the lucene index for " + advertiser);
            List<File> luceneDirs = findProductIndex(AppProperties.getInstance().getProperty(FileContentConfigValues.CONFIG_SOURCE_DIR), advertiser);
            if (luceneDirs!=null && !luceneDirs.isEmpty()) {
                for(File lf:luceneDirs) {
                    if (lf.getParentFile().getName().equals(advertiser)) {
                        try {
                            IndexSearcherCache cache = new IndexSearcherCache(lf);
                            getInstance().addCache(advertiser, cache);
                            ContentProviderStatus.getInstance().luceneFileNames.put(advertiser, lf.getAbsolutePath());
                        } catch (IOException e) {
                            log.error("Exception on loading the advertiser index",e);
                        }
                    }
                    break;
                }
            } else {
                log.error("Could not locate the lucene index for the advertiser : " + advertiser);
            }
        }


    }

    /**
     * Get the product Index dir for the new and old scheme and a given advertiser or all
     * Note that we first check for old scheme and then new
     * @param dir
     * @param advertiser
     * @return
     */
    private static List<File> findProductIndex(String dir, String advertiser) {
        ArrayList<File> luceneDirs = new ArrayList<File>();
        File indexRootDir = new File(dir);
        if (!indexRootDir.exists()) {
            log.error("Directory doesn't exist: " + dir);
            return null;
        }
        String cannonicalPath = null;
        try {
            cannonicalPath = indexRootDir.getCanonicalPath();
        } catch (IOException e) {
            // Shouldn't happen.
            log.error("Directory doesn't exist: " + dir);
            return null;
        }
        String luceneSubDir = AppProperties.getInstance().getProperty(PROVLUCENEDIR);
        String provLuceneDir = cannonicalPath + (cannonicalPath.endsWith("/")?"":"/") + ((luceneSubDir == null)?"":luceneSubDir);
        File provLuceneindexRootDir = new File(provLuceneDir);
        if (provLuceneindexRootDir.exists()) {
            if (advertiser!=null) {
                File advDir = new File(provLuceneDir + "/" + advertiser);
                if (advDir.exists()) {
                    luceneDirs.add(advDir);
                }
            } else {
                File[] advDirs = provLuceneindexRootDir.listFiles();
                for (File f:advDirs) {
                    if (f.isDirectory()) {
                        luceneDirs.add(f);
                    }
                }
            }
        }

        if (luceneDirs.isEmpty()) {
            //check for new scheme
            String pattern = AppProperties.getInstance().getProperty(LUCENEDIRPATTERN);
            if (advertiser==null) {
                FSUtils.findDirectories(luceneDirs, new File(dir), pattern);
            } else {
                String advBaseDir = dir + "/" + advertiser.toUpperCase();
                FSUtils.findDirectories(luceneDirs, new File(advBaseDir), pattern);
            }
        }
        return luceneDirs;
    }


    /**
     * The public method for constructor should not be used
     * Use getInstance() to get an instance of the class
     */
    public ProductIndex() {
        super();
    }

    private ProductIndex(List<File> luceneDirs) {
        m_index_dir = new File(tmpDir);
        if (!luceneDirs.isEmpty()) {
            try {
                mergeIndexesOffline();
            } catch (Exception e) {
                log.fatal("Failed to create a merge the prov indexes and create directory for lucene.");
            }
        } else {
            log.error("No provider lucene folders found, nothing to load into keyword index");
        }
    }

    private void addCache(String id, IndexSearcherCache cache) {
        //Merge the index if there is more than a certain number of advertisers
        if (m_map.size() > MAX_SECONDARY_CACHE_SIZE) {
            mergeIndexesOffline();
        } else {
            m_map.writerLock();
            try {
                m_map.put(id, cache);
            } finally {
                m_map.writerUnlock();
            }
        }


    }

    /**
     * Invoke the shell command to the merge the indexes offline
     */
    private synchronized void mergeIndexesOffline() {
        String luceneToolsDir = AppProperties.getInstance().getProperty("com.tumri.content.lucene.toolsdir");
        File cwd = new File(luceneToolsDir);

        String srcDir = AppProperties.getInstance().getProperty(FileContentConfigValues.CONFIG_SOURCE_DIR);
        String cmd = luceneToolsDir+"/merge.sh -srcDir " + srcDir + " -indexDir " + tmpDir;
        try {
            int retVal = ShellCommand.executeCommand(cmd, cwd);
            if (retVal != 0) {
                String err = "Create index failed";
                log.fatal(err);
            } else {
                m_index_dir = new File(tmpDir);
                if (m_index_dir.exists() && m_index_dir.listFiles().length>0) {
                    log.info("Loading keyword index from " + m_index_dir.getAbsolutePath());
                    addCache(DEFAULT, new IndexSearcherCache(m_index_dir));
                } else {
                    throw new RuntimeException("Merged lucene index not found, nothing to load into keyword index");
                }
            }
        } catch(Throwable e) {
            throw new RuntimeException("Error executing index merge process : "+e.getMessage());
        }


    }

    private IndexSearcherCache getCache(String id) {
        IndexSearcherCache cache = null;
        m_map.readerLock();
        try {
            cache = m_map.get(id);
            if (cache == null) {
                cache = m_map.get(DEFAULT);
            }
        } finally {
            m_map.readerUnlock();
        }
        return cache;
    }

    public ArrayList<Handle> search(String advertiser, String query_string, double min_score, int max_docs) {
        ArrayList<Handle> alist = new ArrayList<Handle>();
        ProductDB db = ProductDB.getInstance();
        IndexSearcherCache searcherCache = getCache(DEFAULT);
        IndexSearcher searcher = searcherCache.get();
        if (searcher != null) {
            try {
                Query q = createQuery(advertiser, query_string);
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
                                    ProductHandle newHandle = new ProductHandle(score,ph.getOid());
                                    newHandle.setProductType(((ProductHandle) ph).getProductType());
                                    alist.add(newHandle);
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

    private Query createQuery(String advertiser, String str) {
        QueryParser qp = new QueryParser("description", getAnalyzer(false));
        try {
            str = cleanseQueryString(str);
            str = "+provider:"+advertiser + " " + str;
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
            queryStr = queryStr.replaceAll("[\\\\cM\\s]", " ");
        }
        return queryStr;
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

    /**
     * Method to merge the multiple index files into one index
     * @param arrIndexes
     */
    private static void mergeIndexes(File indexDirF , RAMDirectory[] arrIndexes) {
        try {
            log.info("Creating Merged index...");
            Date start = new Date();
            Analyzer analyzer = new PorterStemAnalyzer(LargeStopWordList.SMART_STOP_WORDS);
            IndexWriter writer = new IndexWriter(indexDirF, analyzer, true);
            writer.setMergeFactor(mergeFactor);
            writer.addIndexes(arrIndexes);
            log.info("Optimizing Merged index...");
            writer.optimize();
            writer.close();
            log.info("Successfully Merged lucene index... Time Taken : " + ((new Date()).getTime() - start.getTime()) * 1E-3 / 60.0 + " total minutes");
        } catch (IOException e) {
            log.error("Exception caught when merging the indexes",e );
            throw new RuntimeException("Exception caught when merging the indexes");
        }
    }

    /**
     * Entry point that will generate the "merged index"
     * @param args
     */
    public static void main(String[] args) {
        String usage = "java -jar joz.jar [-h] [-srcDir XXX] [-indexDir XXX]";
        String srcDir = AppProperties.getInstance().getProperty(FileContentConfigValues.CONFIG_SOURCE_DIR);
        String outDir = tmpDir;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("-h")) {
                System.out.println("Usage: " + usage);
                System.exit(0);
            } else if (arg.equals("-srcDir")) {
                srcDir = args[++i];
            } else if (arg.equals("-indexDir")) {
                outDir = args[++i];
            } else {
                log.info("Usage: " + usage);
                System.exit(1);
            }
        }
        if (!new File(srcDir).exists()) {
            log.error("The Source directory does not exist : " + srcDir);
            System.exit(1);
        }
        File foutDir = new File(outDir);
        if (foutDir.exists()) {
            FSUtils.removeFiles(foutDir, true);
        } else {
            foutDir.mkdirs();
        }


        try {
            long start = System.currentTimeMillis();
            List<File> luceneDirs = findProductIndex(srcDir,null);

            ArrayList<RAMDirectory> provIndexes = new ArrayList<RAMDirectory>();
            for (File dir: luceneDirs) {
                if (dir.isDirectory()) {
                    log.debug("Found index : " + dir.getAbsolutePath());
                    provIndexes.add(new RAMDirectory(dir));
                }
            }
            try {
                mergeIndexes(foutDir, provIndexes.toArray(new RAMDirectory[0]));
            } catch (Exception e) {
                log.fatal("Failed to create a merge the prov indexes and create directory for lucene.",e);
            }

            log.info("Done. Time taken = " + (System.currentTimeMillis() - start)/1000 + " secs");
        } catch (IOException e) {
            log.info("Unexpected error on index merge", e);
        }
    }

    @Test
    public void test() {
        long start = System.currentTimeMillis();
        try {
            ContentProviderFactory f = null;
            // Filename or Properties ???
            f = ContentProviderFactory.getInstance();
            f.init(AppProperties.getInstance().getProperties());
        } catch (InvalidConfigException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        for (int i=0;i<1;i++) {
            init();
            ProductIndex pi = getInstance();
            try {
                IndexSearcher searcher = pi.getCache("ADIDAS").get();
                Query q = createQuery("ADIDAS", "women");
                TopDocCollector tdc = new TopDocCollector(2000);
                System.out.println("Querying for products...");
                searcher.search(q, tdc);
                TopDocs topdocs = tdc.topDocs();
                if (topdocs != null) {
                    int len = topdocs.scoreDocs.length;
                    for (int j = 0; j < len; j++) {
                        Document doc = searcher.doc(topdocs.scoreDocs[j].doc);
                        String id = doc.get("id");
                        double score = topdocs.scoreDocs[j].score;
                        int oid = Integer.parseInt(id);
                        System.out.println("Result = " + id);
                    }
                }
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