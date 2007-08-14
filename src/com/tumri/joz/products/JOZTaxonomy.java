package com.tumri.joz.products;

import com.tumri.joz.index.DictionaryManager;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class JOZTaxonomy {
  private static JOZTaxonomy g_taxonomy;

  private TreeMap<Integer, TreeSet<Integer>> m_children = new TreeMap<Integer, TreeSet<Integer>>(); // parent -> children
  private TreeMap<Integer, Integer> m_parent = new TreeMap<Integer, Integer>(); // child -> parent

  public static JOZTaxonomy getInstance() {
    if (g_taxonomy == null) {
      synchronized(JOZTaxonomy.class) {
        if (g_taxonomy == null) {
          g_taxonomy = new JOZTaxonomy();
        }
      }
    }
    return g_taxonomy;
  }

  public Integer getRoot() {
    Iterator<Integer> iter = m_parent.keySet().iterator();
    Integer root = null;
    if (iter.hasNext()) {
      root = iter.next();
      while(m_parent.containsKey(root)) {
        root = m_parent.get(root);
      }
    }
    return root;
  }

  private JOZTaxonomy() {
  }

  public Iterator<Map.Entry<Integer, TreeSet<Integer>>> iterator() {
    return m_children.entrySet().iterator();
  }

  public void build(Taxonomy tax) {
    Taxonomy.Node node = tax.getRoot();
    build(tax,node);
  }

  private void build(Taxonomy tax,Taxonomy.Node parent) {
    TreeSet<Taxonomy.Node> children = tax.getChildren(parent);
    if (children != null) {
      Iterator<Taxonomy.Node> iter = children.iterator();
      while (iter.hasNext()) {
        Taxonomy.Node lNode = iter.next();
        addNodes(parent.getGlassName(),lNode.getGlassName());
        build(tax,lNode);
      }
    }
  }


  /**
   * For a given parent and child pair add it to taxonomy tree.
   * Note: this call is not synchronized
   * @param parent node
   * @param child node
   */
  public void addNodes(String parent, String child) {
    DictionaryManager dm = DictionaryManager.getInstance();
    Integer pid = dm.getId(IProduct.Attribute.kCategory,parent);
    Integer cid = dm.getId(IProduct.Attribute.kCategory,child);
    m_parent.put(cid,pid);
    TreeSet<Integer> children = m_children.get(pid);
    if (children == null) {
      children = new TreeSet<Integer>();
      m_children.put(pid,children);
    }
    children.add(cid);
  }

  public Integer getParent(Integer child) {
    return m_parent.get(child);
  }

  public TreeSet<Integer> getChildren(Integer node) {
    return m_children.get(node);
  }

  public void clear() {
    m_children.clear();
    m_parent.clear();
  }

}
