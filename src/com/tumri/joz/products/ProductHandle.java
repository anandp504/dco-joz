package com.tumri.joz.products;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class ProductHandle implements Comparable<ProductHandle> {
  private static Random g_random = new Random();
  private static int k_RANK =52;
  private static int k_POS =32;
  private static long k_POSMASK =  0x000fffff00000000L;
  private static long k_RANKMASK = 0xfff0000000000000L;
  private static long k_IDMASK =   0x00000000ffffffffL;
  // The handle is a 64 bit number composed as follows:
  // -----------------------------------------
  // | 12-bit   |  20-bit   |   32-bit        |
  // | rank     | position  |  product id     |
  // -----------------------------------------
  private long m_data;
  // @todo this needs additional score keeping and a comparator to go with it
  
  public ProductHandle(long l) {
    m_data = l;
  }

  public ProductHandle(String s) throws NumberFormatException {
    m_data = new Long(s).longValue();
  }

  public ProductHandle(int id, int rank) {
    setValue(rank,0,id);
  }
  public void update() {
     int pos = g_random.nextInt(0xfffff);
     setValue(getRank(),pos,getId());
   }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ProductHandle lHandle = (ProductHandle) o;

    if (m_data != lHandle.m_data) return false;

    return true;
  }

  public int hashCode() {
    return (int) (m_data ^ (m_data >>> 32));
  }

  public int compareTo(ProductHandle lHandle) {
    return (m_data < lHandle.m_data ? -1 :
            m_data == lHandle.m_data ? 0 : 1);
  }

  public int getId() {
    return (int)(m_data & k_IDMASK);
  }

  private int getRank() {
    return (int)((m_data & k_RANKMASK) >> k_RANK);
  }

  private int getPos() {
    return (int)((m_data & k_POSMASK) >> k_POS);
  }

  private void setValue(int rank,int pos,int id) {
    m_data = ((((long)rank) << k_RANK) & k_RANKMASK) |
             ((((long)pos) << k_POS) & k_POSMASK) |
             ((long)id);
  }
  public String toString() {
    return Long.toString(m_data);
  }
}