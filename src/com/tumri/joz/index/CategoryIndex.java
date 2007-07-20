package com.tumri.joz.index;

import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.Taxonomy;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CategoryIndex extends Index<Integer, ProductHandle> {
  public CategoryIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kCategory;
  }

  public Integer getKey(IProduct p) {
    return p.getCategory();
  }

  public ProductHandle getValue(IProduct p) {
    return p.getHandle();
  }

  /**
   * Updates the index with the taxonomy object
   * Internally for each of the parent child relationship index entries are created
   * Any old entries are updated
   * @param taxonomy
   */
  public void update(Taxonomy taxonomy) {
    try {
      m_map.writerLock();
      Iterator<Map.Entry<Integer, TreeSet<Integer>>> iter = taxonomy.iterator();
      while (iter.hasNext()) {
        Map.Entry<Integer, TreeSet<Integer>> lEntry = iter.next();
        update(lEntry.getKey(),lEntry.getValue());
      }
    } finally {
      m_map.writerUnlock();
    }
  }

  private SortedSet<ProductHandle> getChildSet(Integer childId) {
    SortedSet<ProductHandle> childSet = get(childId);
    if (childSet == null) {
      RWLockedTreeSet<ProductHandle> set = new RWLockedTreeSet<ProductHandle>();
      childSet = set;
      m_map.put(childId,set);
    }
    return childSet;
  }

  /**
   * find the parent set from the index
   * If the parent is not a MultiSortedSet then change the class to be so
   * The parent category Node should have MultiSortedSet<ProductHandle> as its set, where constituents are as follows
   * MultiSortedSet = { Native Products to Category, child0, child1, child2 ... }
   * Note: MultiSortedSet.add(Key,Value) adds the native products to first set in the row
   * @return
   */
  private void update(Integer pid, TreeSet<Integer> children) {
    SortedSet<ProductHandle> parentSet = get(pid);
    if (parentSet == null) parentSet = new RWLockedTreeSet<ProductHandle>();
    List<SortedSet<ProductHandle>> nlist = new ArrayList<SortedSet<ProductHandle>>();
    nlist.add(parentSet instanceof MultiSortedSet ? ((MultiSortedSet<ProductHandle>)parentSet).getList().get(0) : parentSet);
    Iterator<Integer> iter = children.iterator();
    while (iter.hasNext()) {
      SortedSet<ProductHandle> childSet = getChildSet(iter.next());
      if (childSet instanceof MultiSortedSet) {
        nlist.addAll(((MultiSortedSet)childSet).getList());
      } else {
        nlist.add(childSet);
      }
    }
    put(pid,new MultiSortedSet<ProductHandle>(nlist));
  }
}