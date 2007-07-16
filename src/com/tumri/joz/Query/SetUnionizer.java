package com.tumri.joz.Query;

import com.tumri.joz.utils.Pair;
import com.tumri.joz.index.MultiSortedSet;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class SetUnionizer<Value extends Comparable> {
  private ArrayList<SortedSet<Pair<Value,Double>>> m_includes;
  private SortedSet<Value> m_results;
  private int m_maxSetSize = 120;

  public SetUnionizer() {
    m_includes = new ArrayList<SortedSet<Pair<Value,Double>>>();
  }

  public void include(SortedSet<Pair<Value,Double>> set) {
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
    MultiSortedSet<Pair<Value,Double>> msorted = new MultiSortedSet<Pair<Value, Double>>();
    m_results = new TreeSet<Value>();

    for(int i=0;i<m_includes.size();i++) {
      msorted.add(m_includes.get(i),true);
    }
    int count = 0;
    Iterator<Pair<Value, Double>> iter = msorted.iterator();
    while (iter.hasNext()) {
      Pair<Value, Double> entry = iter.next();
      m_results.add(entry.getFirst());
      if (++count >= max)
        break;
    }
    return m_results;
  }
}