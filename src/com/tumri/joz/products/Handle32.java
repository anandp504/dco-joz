package com.tumri.joz.products;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class Handle32 implements Handle<Handle32> {
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

  public int compareTo(Handle32 lHandle) {
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
    return g_random.nextInt(0x100);
  }
}
