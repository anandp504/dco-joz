package com.tumri.joz.utils;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class Pair<A extends Comparable, B extends Comparable> implements Comparable<Pair <A, B>>, Comparator<Pair<A, B>> {
  private A m_first;
  private B m_second;

  public Pair() {
  }

  public Pair(A aFirst, B aSecond) {
    m_first = aFirst;
    m_second = aSecond;
  }

  public A getFirst() {
    return m_first;
  }

  public void setFirst(A aFirst) {
    m_first = aFirst;
  }

  public B getSecond() {
    return m_second;
  }

  public void setSecond(B aSecond) {
    m_second = aSecond;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Pair lPair = (Pair) o;

    if (m_first != null ? !m_first.equals(lPair.m_first) : lPair.m_first != null) return false;
    if (m_second != null ? !m_second.equals(lPair.m_second) : lPair.m_second != null) return false;

    return true;
  }

  public int hashCode() {
    int lresult;
    lresult = (m_first != null ? m_first.hashCode() : 0);
    lresult = 31 * lresult + (m_second != null ? m_second.hashCode() : 0);
    return lresult;
  }

  public int compareTo(Pair<A, B> aPair) {
    if (this == aPair) return 0;
    if (aPair == null) return 1;
    if (m_first == null && aPair.m_first != null) return -1;
    if (m_first != null && aPair.m_first == null) return 1;
    if (m_first != null) {
      int cmp = m_first.compareTo(aPair.m_first);
      if (cmp != 0) return cmp;
    }
    if (m_second == null && aPair.m_second != null) return -1;
    if (m_second != null && aPair.m_second == null) return 1;
    return (m_second != null ? m_second.compareTo(aPair.m_second) : 0);
  }

  public int compare(Pair<A, B> aPair, Pair<A, B> aPair1) {
    if (aPair == aPair1) return 0;
    if (aPair1 == null) return 1;
    if (aPair.m_second == null && aPair1.m_second != null) return -1;
    if (aPair.m_second != null && aPair1.m_second == null) return 1;
    if (aPair.m_second != null) {
      int cmp = aPair.m_second.compareTo(aPair1.m_second);
      if (cmp != 0) return cmp;
    }
    if (aPair.m_first == null && aPair1.m_first != null) return -1;
    if (aPair.m_first != null && aPair1.m_first == null) return 1;
    return (aPair.m_first != null ? aPair.m_first.compareTo(aPair1.m_first) : 0);
  }
}