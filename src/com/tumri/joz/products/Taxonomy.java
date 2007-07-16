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
public class Taxonomy {
  private static Taxonomy g_taxonomy;

  private TreeMap<Integer, TreeSet<Integer>> m_children = new TreeMap<Integer, TreeSet<Integer>>(); // parent -> children
  private TreeMap<Integer, Integer> m_parent = new TreeMap<Integer, Integer>(); // child -> parent

  public static Taxonomy getInstance() {
    if (g_taxonomy == null) {
      synchronized(Taxonomy.class) {
        if (g_taxonomy == null) {
          g_taxonomy = new Taxonomy();
        }
      }
    }
    return g_taxonomy;
  }

  private Taxonomy() {
  }

  public Iterator<Map.Entry<Integer, TreeSet<Integer>>> iterator() {
    return m_children.entrySet().iterator();
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

  public void clear() {
    m_children.clear();
    m_parent.clear();
  }

}
