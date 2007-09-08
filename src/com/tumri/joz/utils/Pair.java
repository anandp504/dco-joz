package com.tumri.joz.utils;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class Pair<A, B> implements Map.Entry<A,B> {
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

  public A getKey() {
    return getFirst();
  }

  public B getValue() {
    return getSecond();
  }

  public B setValue(B aB) {
    B old = getSecond();
    setSecond(aB);
    return old;
  }
}