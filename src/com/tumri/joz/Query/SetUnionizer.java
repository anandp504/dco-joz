package com.tumri.joz.Query;

import com.tumri.joz.index.MultiSortedSet;
import com.tumri.joz.index.SortedArraySet;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class SetUnionizer<Value extends Comparable> {
  private ArrayList<SortedSet<Value>> m_includes;
  private SortedSet<Value> m_results;
  private int m_maxSetSize = 120;

  public SetUnionizer() {
    m_includes = new ArrayList<SortedSet<Value>>();
  }

  public void include(SortedSet<Value> set) {
    m_includes.add(set);
  }

  public void setMax(int size) {
    m_maxSetSize = size;
  }


  public SortedSet<Value> union() {
    if (this.m_results != null) {
      return this.m_results;
    }
    int max = m_maxSetSize * m_includes.size();
    MultiSortedSet<Value> msorted = new MultiSortedSet<Value>();
    ArrayList<Value> list = new ArrayList<Value>();

    for(int i=0;i<m_includes.size();i++) {
      msorted.add(m_includes.get(i),true);
    }
    int count = 0;
    Iterator<Value> iter = msorted.iterator();
    while (iter.hasNext()) {
      Value entry = iter.next();
      list.add(entry);
      if (++count >= max)
        break;
    }
    m_results = new SortedArraySet<Value>(list,true); // presorted set
    return m_results;
  }
}