package com.tumri.joz.campaign;

import com.tumri.joz.products.Handle;

/**
 * AdPodHandle for use by Indices
 * 
 * @author snawathe, bpatel
 */
public class AdPodHandle implements Handle {
  private double score;
  private int    weight = 1;
  private int    oid;

  public AdPodHandle(int oid, double aScore) {
    this.score = aScore;
    this.oid = oid;
  }

  public AdPodHandle(int oid, double aScore, int weight) {
      this.score = aScore;
      this.weight = weight;
      this.oid = oid;
  }

  public int getOid() {
    return oid;
  }

  public double getScore() {
    return score;
  }

  public int getWeight() {
    return weight;
  }

  public Handle createHandle(double score) {
    return (score != this.score ? new AdPodHandle(oid, score, weight) : this);
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AdPodHandle that = (AdPodHandle) o;

    return (this.getOid() == that.getOid());
  }

  public int hashCode() {
    return getOid();
  }


  public int compareTo(Object handle) {
    int loid = ((AdPodHandle)handle).getOid();
    return (oid < loid ? -1 :
            oid == loid ? 0 : 1);
  }


  public int compare(Object h1, Object h2) {
    AdPodHandle handle1 = (AdPodHandle)h1;
    AdPodHandle handle2 = (AdPodHandle)h2;
    if (handle1.score > handle2.score) return -1;
    if (handle1.score < handle2.score) return 1;
    if (handle1.oid < handle2.oid) return -1;
    if (handle1.oid > handle2.oid) return 1;
    return 0;
  }
}