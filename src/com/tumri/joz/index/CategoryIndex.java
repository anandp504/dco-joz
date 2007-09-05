package com.tumri.joz.index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import com.tumri.content.data.Category;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.utils.data.MultiSortedSet;
import com.tumri.utils.data.RWLockedSortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CategoryIndex extends ProductAttributeIndex<Integer, Handle> {
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
   * Given a List of Key objects keys, returns the SortedSet of values. The sort order is decided by the
   * natural order of the Value. Key and Value both should implement Comparable
   * @param keys a List of key objects
   */
  public SortedSet<Handle> get(List<Integer> keys) {
    if (keys.size() == 1) {
      return get(keys.get(0));
    } else {
      MultiSortedSet<Handle> set = new MultiSortedSet<Handle>();
      for (int i = 0; i < keys.size(); i++) {
        SortedSet<Handle> s = get(keys.get(i));
        if (s instanceof MultiSortedSet) {
          List<SortedSet<Handle>> slist = ((MultiSortedSet<Handle>)s).getList();
          for (int j = 0; j < slist.size(); j++) {
            SortedSet<Handle> lHandles = slist.get(j);
            set.add(lHandles);
          }
        } else {
          set.add(s);
        }
      }
      return set;
    }

  }
  /**
   * Updates the index with the taxonomy object
   * Internally for each of the parent child relationship index entries are created
   * Any old entries are updated
   * @param taxonomy
   */
  public void update(JOZTaxonomy taxonomy) {
    try {
      m_map.writerLock();
      Integer root = taxonomy.getTaxonomy().getRootCategory().getId();
      if (root != null) {
        update(taxonomy,root);
      }
    } finally {
      m_map.writerUnlock();
    }
  }

  private SortedSet<Handle> getChildSet(Integer childId) {
    SortedSet<Handle> childSet = get(childId);
    if (childSet == null) {
      RWLockedSortedSet<Handle> set = createSet();
      childSet = set;
      m_map.put(childId,set);
    }
    return childSet;
  }

  /**
   * Update all the child nodes before updating the parent node
   * find the parent set from the index
   * If the parent is not a MultiSortedSet then change the class to be so
   * The parent category Node should have MultiSortedSet<Handle> as its set, where constituents are as follows
   * MultiSortedSet = { Native Products to Category, child0, child1, child2 ... }
   * Note: MultiSortedSet.add(Key,Value) adds the native products to first set in the row, while updating
   * @param tax
   * @param pid
   */
  private void update(JOZTaxonomy tax, Integer pid) {
    Category[] children = tax.getTaxonomy().getCategory(pid).getChildren();
    if (children != null) {
      SortedSet<Handle> parentSet = get(pid);
      if (parentSet == null) parentSet = createSet();
      List<SortedSet<Handle>> nlist = new ArrayList<SortedSet<Handle>>();
      nlist.add(parentSet instanceof MultiSortedSet ? ((MultiSortedSet<Handle>)parentSet).getList().get(0) : parentSet);
      for (Category c: children) {
        Integer child = c.getId();
        update(tax,child);
        SortedSet<Handle> childSet = getChildSet(child);
        if (childSet instanceof MultiSortedSet) {
          nlist.addAll(((MultiSortedSet<Handle>)childSet).getList());
        } else {
          nlist.add(childSet);
        }
      }
      put(pid,new MultiSortedSet<Handle>(nlist));
    }
  }
}