package com.tumri.joz.utils;

import com.tumri.joz.products.Handle;
import com.tumri.utils.data.SortedArraySet;

import java.util.SortedSet;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * Date: Sep 4, 2007
 * Time: 9:56:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class SetUtils {
  public static SortedSet<Handle> multiplyByScalar(SortedSet<Handle> set, double k) {
    if (set != null && !set.isEmpty()) {
      Iterator<Handle> iter = set.iterator();
      ArrayList<Handle> nlist = new ArrayList<Handle>();
      while (iter.hasNext()) {
        Handle h = iter.next();
        nlist.add(h.createHandle(h.getScore()*k));
      }
      return new SortedArraySet<Handle>(nlist,true);
    }
    return null;
  }
}
