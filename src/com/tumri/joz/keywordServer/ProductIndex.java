package com.tumri.joz.keywordServer;

import com.tumri.content.ContentProviderFactory;
import com.tumri.content.InvalidConfigException;
import com.tumri.content.data.Category;
import com.tumri.content.data.Product;
import com.tumri.content.impl.file.FileContentConfigValues;
import com.tumri.joz.products.*;
import com.tumri.joz.utils.AppProperties;
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
 * User: snawathe
 * Maintains the lucene index of all products, provides offline means of indexing the products
 */
public class ProductIndex {
  static Logger log = Logger.getLogger(ProductIndex.class);
  private static final String LUCENEDIR = "com.tumri.content.luceneDir";
  private static AtomicReference<ProductIndex> g_Instance = new AtomicReference<ProductIndex>();

  private boolean debug = false;
  private String indexDir = "lucene";
  private String docDir = ".";
  // Lines beginning with this character in {deboostCategoryFile}
  // are ignored.
  private static final char FILE_COMMENT_CHAR = '#';
  private int mergeFactor = 256;
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
  private ProductIndex() {
      super();
  }

  private ProductIndex(File f) {
    try {
      if (f.exists() && f.isDirectory()) {
        m_index_dir = f;
        log.info("Loading keyword index from " + f.getAbsolutePath());
        m_searcherCache.set(new IndexSearcherCache(f));
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
                int oid = Integer.parseInt(id);
                IProduct p = db.getInt(oid); // avoids too many calls to lock/unlock
                if (p != null) {
                  alist.add(new ProductHandle(p, score));
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
  		queryStr = queryStr.replaceAll("[\\^M\\s]", " ");
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
      add_field(doc, "id", Integer.toString(p.getId()) , Field.Store.YES, Field.Index.UN_TOKENIZED);

      return doc;
    }

  private ArrayList<IProduct> getAllProducts(File file) throws IOException {
      ContentProviderFactory f;
      Properties props = new Properties();
      props.setProperty(FileContentConfigValues.CONFIG_PRODUCTS_DIR, file.getCanonicalPath());
      props.setProperty(FileContentConfigValues.CONFIG_TAXONOMY_DIR, file.getCanonicalPath());
      props.setProperty(FileContentConfigValues.CONFIG_DISABLE_MERCHANT_DATA,"true");
      try {
          f = ContentProviderFactory.getInstance();
          f.init(props);
          List<Product> prods = f.getContentProvider().getContent().getProducts().getAll();
          ArrayList<IProduct> retVal = new ArrayList<IProduct>(prods.size());
          for (Product p: prods) {
              retVal.add(new ProductWrapper(p));
          }
          return retVal;
      } catch (InvalidConfigException e) {
          throw new IOException("Unable to get products:" + ((e.getCause() == null)?e.getMessage():e.getCause().getMessage()));
      }
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
   * Index each of the products from the MUP file. MUP file is read using the MUP Loader
   * Each of the IProduct is treated as a document and indexed
   * @param writer
   * @param file
   * @throws IOException
   */
   private void indexDocs(IndexWriter writer, File file) throws IOException {
    ArrayList<IProduct> products = getAllProducts(file);
    for (int i = 0; i < products.size(); i++) {
      IProduct p = products.get(i);
      Document doc = getDocument(p);
      writer.addDocument(doc);
    }
  }

  /**
   * Index all text files under a directory.
   */
  private void index(String[] args) {
    boolean dumpTokens = false;
    String usage = "java -jar joz.jar [-h] [-debug] [-dumpTokens] [-deboostCategoryFile XXX] [-docDir XXX] [-indexDir XXX] [-mergeFactor nnn] [-boostField name value]";

    // boost these by default
    fieldBoosts.put("name", new Float(2.0));
    fieldBoosts.put("parents", new Float(2.0));
    fieldBoosts.put("superclasses", new Float(1.5));

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];

      // NOTE: We let array index checking catch missing args to
      // -docDir, etc.

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
        docDir = args[++i];
      } else if (arg.equals("-indexDir")) {
        indexDir = args[++i];
      } else if (arg.equals("-mergeFactor")) {
        mergeFactor = Integer.parseInt(args[++i]);
      } else if (arg.equals("-boostField")) {
        String field = args[++i];
        Float boost = Float.parseFloat(args[++i]);
        log.info("Boosting field '" + field + "' by " + boost);
        fieldBoosts.put(field, boost);
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

    File indexDirF = new File(indexDir);
    if (indexDirF.exists()) {
      log.fatal("Cannot save index to '" + indexDir + "' directory, please delete it first");
      System.exit(1);
    }
    final File docDirF = new File(docDir);
    if (!docDirF.exists() || !docDirF.canRead()) {
      log.fatal("Document directory '" + docDirF.getAbsolutePath() + "' does not exist or is not readable, please check the path");
      System.exit(1);
    }

    try {
      Date start = new Date();
      Analyzer analyzer = getAnalyzer(dumpTokens);
      IndexWriter writer = new IndexWriter(indexDirF, analyzer, true);
      writer.setMergeFactor(mergeFactor);
      log.info("Indexing '" + docDir + "' into '" + indexDir + "' ...");
      indexDocs(writer, docDirF);
      log.info("Optimizing ...");
      writer.optimize();
      writer.close();
      log.info(((new Date()).getTime() - start.getTime()) * 1E-3 / 60.0 + " total minutes");
    }
    catch (IOException e) {
      log.error("something screwed up: ", e);
      // If we fail we must exit with a non-zero error code.
      System.exit(1);
    }
  }

  public static void main(String[] args) {
      new ProductIndex().index(args);
    }

  @Test
  public void test() {
    long start = System.currentTimeMillis();
    for (int i=0;i<10;i++) {
      ProductIndex.init();
      ProductIndex pi = ProductIndex.getInstance();
      try {
        IndexSearcher searcher = pi.m_searcherCache.get().get();
        Query q = createQuery("canon eos 400D");
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