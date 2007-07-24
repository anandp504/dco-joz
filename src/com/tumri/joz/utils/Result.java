package com.tumri.joz.utils;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class Result implements Comparable<Result>, Comparator<Result> {
  int m_oid = 0;
  double m_score = 0.0;

  public Result() {
  }

  public Result(int aOid, double aScore) {
    m_oid = aOid;
    m_score = aScore;
  }

  public int getOid() {
    return m_oid;
  }

  public double getScore() {
    return m_score;
  }


  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Result lResult = (Result) o;

    if (m_oid != lResult.m_oid) return false;
    if (Double.compare(lResult.m_score, m_score) != 0) return false;

    return true;
  }

  public int hashCode() {
    int lresult;
    long temp;
    lresult = m_oid;
    temp = m_score != +0.0d ? Double.doubleToLongBits(m_score) : 0L;
    lresult = 31 * lresult + (int) (temp ^ (temp >>> 32));
    return lresult;
  }


  public int compareTo(Result aResult) {
    if (m_oid < aResult.m_oid) return -1;
    if (m_oid > aResult.m_oid) return 1;
    if (m_score > aResult.m_score) return -1;
    if (m_score < aResult.m_score) return 1;
    return 0;
  }


  public int compare(Result aResult, Result aResult1) {
    if (aResult.m_score > aResult1.m_score) return -1;
    if (aResult.m_score < aResult1.m_score) return 1;
    if (aResult.m_oid < aResult1.m_oid) return -1;
    if (aResult.m_oid > aResult1.m_oid) return 1;
    return 0;
  }
}