package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.utils.data.MergeSortedSets;
import com.tumri.utils.data.MultiSortedSet;

import java.util.List;
import java.util.SortedSet;

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
    SortedSet<Handle> s = super.get(keys);
    if (s instanceof MultiSortedSet) {
      MultiSortedSet<Handle> set = (MultiSortedSet<Handle>)s;
      if (set.getList().size() > 10) {
        try {
          set.readerLock();
          return MergeSortedSets.merge(set);
        } finally {
          set.readerUnlock();
        }
      }
    }
    return s;
  }
}