package com.tumri.joz.keywordServer;

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
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * Maintains the lucene index of all products, provides offline means of indexing the products
 */
public class ProductIndex {
  static Logger log = Logger.getLogger(ProductIndex.class);
  private static final String LUCENEDIR = "lucene";
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
  private AtomicReference<IndexSearcher> m_searcher = new AtomicReference<IndexSearcher>();

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

  /**
   * The public method for constructor should not be used
   * Use getInstance() to get an instance of the class
   */
  public ProductIndex() {
  }

  public static void main(String[] args) {
    new ProductIndex().index(args);
  }

  public static void init() {
    File f = findProductIndex();
    if (f != null) {
      ProductIndex pi = new ProductIndex(f);
      g_Instance.set(pi);
    }
  }

  private ProductIndex(File f) {
    try {
      if (f.exists() && f.isDirectory()) {
        m_index_dir = f;
        log.info("Loading keyword index from " + f.getAbsolutePath());
        m_searcher.set(new IndexSearcher(new RAMDirectory(f)));
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
    try {
      Query q = createQuery(query_string);
      if (q != null) {
        Hits hits = m_searcher.get().search(q);
        int len = (hits.length() < max_docs ? hits.length() : max_docs);
        db.readerLock();
        try {
          for(int i=0;i<len ; i++) {
            Document doc = hits.doc(i);
            String id = doc.get("id");
            double score = hits.score(i);
            int oid = Integer.parseInt(id);
            IProduct p = db.getInt(oid); // avoids too many calls to lock/unlock
            alist.add(new ProductHandle(p,score));
            if (score < min_score) break;
          }
        } finally {
          db.readerUnlock();
        }
      }
    } catch (IOException e) {
      log.error("Exception",e);
    } finally {
    }
    System.out.println("returned "+alist.size() + " products");
    return alist;
  }

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
      m_searcher.get().close();
      m_searcher.set(new IndexSearcher(new RAMDirectory(m_index_dir)));
    } catch (IOException e) {
      log.error("Exception while adding products",e);
    } finally {
      try {
        if (index_modifier != null) index_modifier.close();
      } catch (IOException e) {
      }
    }
  }

  public void deleteProducts(ArrayList<IProduct> products) {
    IndexModifier index_modifier = null;
    try {
      index_modifier = new IndexModifier(m_index_dir,getAnalyzer(false),false);
      for (int i = 0; i < products.size(); i++) {
        IProduct p = products.get(i);
        index_modifier.deleteDocuments(new Term("id",p.getGId()));
      }
      index_modifier.close();
      m_searcher.get().close();
      m_searcher.set(new IndexSearcher(new RAMDirectory(m_index_dir)));
    } catch (IOException e) {
      log.error("Exception while deleting products",e);
    } finally {
      try {
        if (index_modifier != null) index_modifier.close();
      } catch (IOException e) {
      }
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
   * Index all text files under a directory.
   */
  private void index(String[] args) {
    boolean dumpTokens = false;
    String usage = "java -jar ProductIndex.jar [-h] [-debug] [-dumpTokens] [-deboostCategoryFile XXX] [-docDir XXX] [-indexDir XXX] [-mergeFactor nnn] [-boostField name value]";

    // boost these by default
    fieldBoosts.put("name", new Float(2.0));
    fieldBoosts.put("parents", new Float(2.0));
    fieldBoosts.put("superclasses", new Float(1.5));

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];

      // NOTE: We let array index checking catch missing args to
      // -docDir, etc.

      if (arg.equals("-h")) {
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

  private ArrayList<IProduct> getAllProducts(File file) throws IOException {
    return new MUPLoader(file).getAll();
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

  private Document getDocument(IProduct p) {
    Taxonomy tax = Taxonomy.getInstance();
    Document doc = new Document();
    Taxonomy.Node n = tax.getNode(p.getCategoryStr());
    String cat = (n == null ? "" : n.getName());
    StringBuilder sb = new StringBuilder();
    sb.append(cat).append(" ").append(p.getBrandStr()).append(" ").append(p.getProductName()).append(" ");
    sb.append(p.getDescription());
    add_field(doc, "category", cat , Field.Store.YES, Field.Index.UN_TOKENIZED);
    add_field(doc, "brand", p.getBrandStr() , Field.Store.YES, Field.Index.TOKENIZED);
    add_field(doc, "name", p.getProductName() , Field.Store.YES, Field.Index.TOKENIZED);
    add_field(doc, "description", sb.toString() , Field.Store.NO, Field.Index.TOKENIZED);
    add_field(doc, "id", p.getGId() , Field.Store.YES, Field.Index.UN_TOKENIZED);
    return doc;
  }

  private static File findProductIndex() {
    String l = AppProperties.getInstance().getProperty(LUCENEDIR);
    if (l == null) l = LUCENEDIR;
    File f = new File(l);
    if (!f.exists()) {
      log.error("Lucene DB dir doesn't exist: "+l);
      return null;
    }
    return f;
  }

  private Query createQuery(String str) {
    QueryParser qp = new QueryParser("description", getAnalyzer(false));
    try {
      return qp.parse(str);
    } catch (ParseException e) {
      return null;
    }
  }


  @Test
  public void test() {
    ProductIndex pi = ProductIndex.getInstance();
    long start = System.currentTimeMillis();
    for (int i=0;i<1;i++) {
      try {
        pi.m_searcher.get().search(createQuery("canon eos 400D"));
      } catch (IOException e) {
      }
    }
    System.out.println("Time is "+(System.currentTimeMillis()-start));
  }
}