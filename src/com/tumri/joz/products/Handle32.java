package com.tumri.joz.products;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class Handle32 implements Handle {
  private static Random g_random = new Random();
  private static int k_RANK =23;
  private static int k_RANKMASK = 0x7f800000;
  private static int k_IDMASK =   0x007fffff;
  // The handle is a 32 bit number composed as follows:
  // ---------------------------------------
  // |  8-bit            |   23-bit        |
  // | weighted rank     |  product id     |
  // ---------------------------------------
  private int m_data;
  
  public Handle32(int id, int rank) {
    setValue(rank, id);
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Handle32 lHandle = (Handle32) o;

    if (m_data != lHandle.m_data) return false;

    return true;
  }

  public int hashCode() {
    return m_data;
  }

  public int compareTo(Object handle) {
    Handle32 lHandle = (Handle32)handle;
    return (m_data < lHandle.m_data ? -1 :
            m_data == lHandle.m_data ? 0 : 1);
  }

  public int getOid() {
    return (m_data & k_IDMASK);
  }

  private int getRank() {
    return ((m_data & k_RANKMASK) >> k_RANK);
  }

  private void setValue(int rank, int id) {
    m_data = (((weightedRank(rank) << k_RANK) & k_RANKMASK) |
              (id & k_IDMASK));
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
    //return g_random.nextInt(0x100);
    return 0;
  }

  public String toString() {
    return Integer.toString(getOid());
  }

  public double getScore() {
    return 1.0; // @todo
  }


  public Handle createHandle(double score) {
    return null;  //@ToDo change body of implemented methods use File | Settings | File Templates.
  }


  public int compare(Object h1, Object h2) {
    Handle32 handle1 = (Handle32)h1;
    Handle32 handle2 = (Handle32)h2;

    return handle1.compareTo(handle2); // @todo
  }
}
