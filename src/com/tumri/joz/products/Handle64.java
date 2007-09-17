package com.tumri.joz.products;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class Handle64 implements Handle {
  private static Random g_random = new Random();
  private static int k_RANK =32;
  private static long k_RANKMASK = 0x000000ff00000000L;
  private static long k_IDMASK =   0x00000000ffffffffL;
  // The handle is a 64 bit number composed as follows:
  // ----------------------------------
  // |       20-bit |   32-bit        |
  // | wighted rank |  product id     |
  // ----------------------------------
  private long m_data;

  public Handle64(int id, int rank) {
    setValue(rank,id);
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Handle64 lHandle = (Handle64) o;

    if (m_data != lHandle.m_data) return false;

    return true;
  }

  public int hashCode() {
    return (int) (m_data ^ (m_data >>> 32));
  }

  public int compareTo(Object handle) {
    Handle64 lHandle = (Handle64)handle;
    return (m_data < lHandle.m_data ? -1 :
            m_data == lHandle.m_data ? 0 : 1);
  }

  public int getOid() {
    return (int)(m_data & k_IDMASK);
  }

  private int getRank() {
    return (int)((m_data & k_RANKMASK) >> k_RANK);
  }

  private void setValue(int rank, int id) {
    m_data = ((((long)weightedRank(rank)) << k_RANK) & k_RANKMASK) |
             ((long)id);
  }

  /**
   * This should be reimplemented when input ranks are more reliable.
   * One way to create weighted randomized rank is to oscillate the weighted rank around the RANK
   * as a centroid in a Standard Normal distribution. Adding clipping effects as necessary.
   *
   * @param rank
   * @return weighted rank number
   */
  private int weightedRank(int rank) {
    return g_random.nextInt(0x100);
  }

  public String toString() {
    return Long.toString(m_data);
  }


  public double getScore() {
    return 1.0; // @todo
  }


  public Handle createHandle(double score) {
    return null;  //@Todo change body of implemented methods use File | Settings | File Templates.
  }

  public int compare(Object h1, Object h2) {
    Handle64 handle1 = (Handle64)h1;
    Handle64 handle2 = (Handle64)h2;

    return handle1.compareTo(handle2); // @todo
  }

}