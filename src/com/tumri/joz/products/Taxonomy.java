package com.tumri.joz.products;

import com.tumri.joz.utils.Pair;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.Assert;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 */
public class Taxonomy {
  private static final String TAXONOMYDIR = ".";
  static Logger log = Logger.getLogger(Taxonomy.class);

  private static AtomicReference<Taxonomy> g_Taxonomy = new AtomicReference<Taxonomy>();
  private TreeMap<Node, TreeSet<Node>> m_children = new TreeMap<Node, TreeSet<Node>>(); // parent -> children
  private TreeMap<Node, Node> m_parent = new TreeMap<Node, Node>(); // child -> parent
  private TreeMap<String, Node> m_nodes = new TreeMap<String, Node>();

  /**
   * @return singleton instance of Taxonomy
   */
  public static Taxonomy getInstance() {
    Taxonomy tax = g_Taxonomy.get();
    if (tax == null) {
      synchronized (Taxonomy.class) {
        if (g_Taxonomy.get() == null) {
          try {
            init(TAXONOMYDIR);
            tax = g_Taxonomy.get();
          } catch (IOException e) {
          }
        }
      }
    }
    return tax;
  }

  /**
   * init() should be called from Tomcat servlet initialization sequence or content update
   */
  public static void init(String dir) throws IOException {
    Taxonomy tax = new Taxonomy();
    File f = tax.findTaxonomyFile(dir);
    ArrayList<Pair<Node,Node>> nodes = tax.getAll(f);
    for (int i = 0; i < nodes.size(); i++) {
      Pair<Node, Node> lPair =  nodes.get(i);
      tax.addNodes(lPair.getFirst(),lPair.getSecond());
    }
    g_Taxonomy.set(tax);
  }

  public Node getRoot() {
    Iterator<Node> iter = m_parent.keySet().iterator();
    Node root = null;
    if (iter.hasNext()) {
      root = iter.next();
      while (m_parent.containsKey(root)) {
        root = m_parent.get(root);
      }
    }
    return root;
  }

  /**
   * This constructor is not for public use, use getInstance() method instead
   * This exists for Junit testing only
   */
  public Taxonomy() {
  }

  /**
   * For a given parent and child pair add it to Tax tree.
   * Note: this call is not synchronized
   *
   * @param parent node
   * @param child  node
   */
  public void addNodes(Node parent, Node child) {
    m_nodes.put(parent.getGlassName(),parent);
    m_nodes.put(child.getGlassName(),child);
    m_parent.put(child, parent);
    TreeSet<Node> children = m_children.get(parent);
    if (children == null) {
      children = new TreeSet<Node>();
      m_children.put(parent, children);
    }
    children.add(child);
  }

  public Node getParent(Node child) {
    return m_parent.get(child);
  }

  public TreeSet<Node> getChildren(Node node) {
    return m_children.get(node);
  }

  public void clear() {
    m_children.clear();
    m_parent.clear();
  }

  public Node getNode(String glassName) {
    return m_nodes.get(glassName);
  }

  private ArrayList<Pair<Node, Node>> getAll(File file) throws IOException {
    if (file == null || !file.exists()) {
      log.fatal("File " + file + " doesn't exist");
      throw new RuntimeException("File " + file + " doesn't exist");
    }
    if (!file.canRead()) {
      log.fatal("File " + file + " cannot be read");
      throw new RuntimeException("File " + file + " cannot be read");
    }

    FileInputStream fir = new FileInputStream(file);
    try {
      InputStreamReader isr = new InputStreamReader(fir, "utf8");
      BufferedReader br = new BufferedReader(isr);

      boolean eof = false;
      String line = null;
      ArrayList<Pair<Node, Node>> nodes = new ArrayList<Pair<Node, Node>>();
      while (!eof) {
        line = br.readLine();
        if (line == null) {
          eof = true;
          continue;
        }
        Pair<Node, Node> p = convertLine(line);
        if (p != null) nodes.add(p);
      }
      return nodes;
    } finally {
      fir.close();
    }
  }

  private Pair<Node, Node> convertLine(String line) {
    if (line != null) {
      StringTokenizer str = new StringTokenizer(line, "\t", false);
      ArrayList<String> strings = new ArrayList<String>();
      String s1 = null;

      while (str.hasMoreTokens()) {
        strings.add(str.nextToken());
      }

      if (strings.size() != 4) {
        log.error("Invalid Entry found(Size=" + strings.size() + "): " + line);
        return null;
      }
      Node child = new Node(strings.get(1), strings.get(0));
      Node parent = new Node(strings.get(3), strings.get(2));
      return new Pair<Node, Node>(parent, child);
    }
    return null;
  }

  private File findTaxonomyFile(String dir) {
    File d = new File(dir);
    if (d.exists() && d.isDirectory()) {
      File taxfiles[] = d.listFiles(new TaxonomyFileFilter());
      if (taxfiles.length > 0)
        return taxfiles[0];
    }
    log.error("Could not locate taxonomy file in the directory: "+dir);
    return null;
  }

  class TaxonomyFileFilter implements FilenameFilter {
    private static final String TAXONOMY_SUFFIX = ".taxonomy";

    public TaxonomyFileFilter() {
    }


    public boolean accept(File aFile, String aString) {
      return (aString.endsWith(TAXONOMY_SUFFIX));
    }
  }

  public class Node implements Comparable<Node> {
    private static final String GLASSVIEW = "GLASSVIEW.";
    String m_name;
    String m_glassName;

    public Node(String aName, String aGlassName) {
      m_name = aName;
      m_glassName = (aGlassName.startsWith(GLASSVIEW) ? aGlassName : GLASSVIEW + aGlassName);
    }

    public String getName() {
      return m_name;
    }

    public String getGlassName() {
      return m_glassName;
    }

    public int compareTo(Node aNode) {
      return m_glassName.compareTo(aNode.m_glassName);
    }


    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Node that = (Node) o;

      if (!m_glassName.equals(that.m_glassName)) return false;

      return true;
    }

    public int hashCode() {
      return m_glassName.hashCode();
    }

    public String toString() {
      return m_name;
    }
  }

  @Test public void test() {
    Taxonomy tax = getInstance();
    Node root = tax.getRoot();
    Assert.assertTrue(root != null);
    TreeSet<Node> children = tax.getChildren(root);
    Assert.assertTrue(children.size() > 0);
  }
}

