package com.tumri.joz.campaign;

import com.tumri.joz.products.Handle;
import com.tumri.cma.domain.AdPod;

/**
 * AdPodHandle for use by Indices
 * 
 * @author snawathe, bpatel
 */
public class AdPodHandle implements Handle {
  private AdPod  adpod;
  private double score;
  private int    weight = 1;
  private int    oid;

  public static final double locationScore = 1.0;
  public static final double urlScore = 0.9;
  public static final double themeScore = 0.8;



  public AdPodHandle(AdPod aAdpod, double aScore) {
    this.adpod = aAdpod;
    this.score = aScore;
  }

  public AdPodHandle(AdPod aAdpod, double aScore, int weight) {
      this.adpod = aAdpod;
      this.score = aScore;
      this.weight = weight;
  }

  public AdPod getAdpod() {
    return adpod;
  }

  public int getOid() {
    return adpod.getId();
  }

  public double getScore() {
    return score;
  }

  public int getWeight() {
    return weight;
  }

  public Handle createHandle(double score) {
    return (score != this.score ? new AdPodHandle(adpod, score) : this);
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


  public int compareTo(Handle handle) {
    return (getOid() < handle.getOid() ? -1 :
            getOid() == handle.getOid() ? 0 : 1);
  }


  public int compare(Handle handle1, Handle handle2) {
    if (handle1.getScore() > handle2.getScore()) return -1;
    if (handle1.getScore() < handle2.getScore()) return 1;
    if (handle1.getOid() < handle2.getOid()) return -1;
    if (handle1.getOid() > handle2.getOid()) return 1;
    return 0;
  }
}