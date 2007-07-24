package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.Taxonomy;
import com.tumri.joz.products.Handle;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CategoryIndex extends Index<Integer, Handle> {
  public CategoryIndex() {
  }

  public IProduct.Attribute getType() {
    return IProduct.Attribute.kCategory;
  }

  public Integer getKey(IProduct p) {
    return p.getCategory();
  }

  public Handle getValue(IProduct p) {
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

  private SortedSet<Handle> getChildSet(Integer childId) {
    SortedSet<Handle> childSet = get(childId);
    if (childSet == null) {
      RWLockedTreeSet<Handle> set = new RWLockedTreeSet<Handle>();
      childSet = set;
      m_map.put(childId,set);
    }
    return childSet;
  }

  /**
   * find the parent set from the index
   * If the parent is not a MultiSortedSet then change the class to be so
   * The parent category Node should have MultiSortedSet<Handle> as its set, where constituents are as follows
   * MultiSortedSet = { Native Products to Category, child0, child1, child2 ... }
   * Note: MultiSortedSet.add(Key,Value) adds the native products to first set in the row
   * @return
   */
  private void update(Integer pid, TreeSet<Integer> children) {
    SortedSet<Handle> parentSet = get(pid);
    if (parentSet == null) parentSet = new RWLockedTreeSet<Handle>();
    List<SortedSet<Handle>> nlist = new ArrayList<SortedSet<Handle>>();
    nlist.add(parentSet instanceof MultiSortedSet ? ((MultiSortedSet<Handle>)parentSet).getList().get(0) : parentSet);
    Iterator<Integer> iter = children.iterator();
    while (iter.hasNext()) {
      SortedSet<Handle> childSet = getChildSet(iter.next());
      if (childSet instanceof MultiSortedSet) {
        nlist.addAll(((MultiSortedSet)childSet).getList());
      } else {
        nlist.add(childSet);
      }
    }
    put(pid,new MultiSortedSet<Handle>(nlist));
  }
}