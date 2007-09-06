package com.tumri.joz.campaign;

import com.tumri.joz.products.Handle;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class AdPodHandle implements Handle {
  private JAdPod m_adpod;
  private double m_score;
  private int m_rank = 1;

  public AdPodHandle(JAdPod aAdpod, double aScore) {
    m_adpod = aAdpod;
    m_score = aScore;
  }

  public AdPodHandle(JAdPod aAdpod, double aScore, int rank) {
    m_adpod = aAdpod;
    m_score = aScore;
    m_rank = rank;
  }

  public JAdPod getAdpod() {
    return m_adpod;
  }

  public int getOid() {
    return m_adpod.getId();
  }

  public double getScore() {
    return m_score;
  }

  public int getRank() {
    return m_rank;
  }

  public Handle createHandle(double score) {
    return (score != m_score ? new AdPodHandle(m_adpod,score) : this);
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