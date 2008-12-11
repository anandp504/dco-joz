/*
 * ZipCodeHandle.java
 *
 * COPYRIGHT (C)  TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (@tumri.com)
 * @version 1.0     
 *
 */
package com.tumri.joz.utils;

import com.tumri.joz.products.Handle;

/**
 * @author: nipun
 * Date: Dec 9, 2008
 * Time: 12:33:28 PM
 */
public class ZipCodeHandle implements Handle {
  private double score;
  private int    weight = 1;
  private int    oid;

  public ZipCodeHandle(int oid, double aScore) {
    this.score = aScore;
    this.oid = oid;
  }

  public ZipCodeHandle(int oid, double aScore, int weight) {
      this.score = aScore;
      this.weight = weight;
      this.oid = oid;
  }

  public long getOid() {
    return oid;
  }

  public double getScore() {
    return score;
  }

  public int getWeight() {
    return weight;
  }

  public Handle createHandle(double score) {
    return (score != this.score ? new ZipCodeHandle(oid, score, weight) : this);
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ZipCodeHandle that = (ZipCodeHandle) o;

    return (this.getOid() == that.getOid());
  }

  public int hashCode() {
    return (int)getOid();
  }


  public int compareTo(Object handle) {
    int loid = (int)((ZipCodeHandle)handle).getOid();
    return (oid < loid ? -1 :
            oid == loid ? 0 : 1);
  }


  public int compare(Object h1, Object h2) {
    ZipCodeHandle handle1 = (ZipCodeHandle)h1;
    ZipCodeHandle handle2 = (ZipCodeHandle)h2;
    if (handle1.score > handle2.score) return 1;
    if (handle1.score < handle2.score) return -1;
    if (handle1.oid < handle2.oid) return -1;
    if (handle1.oid > handle2.oid) return 1;
    return 0;
  }
}
