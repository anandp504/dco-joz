package com.tumri.joz.campaign;

import com.tumri.cma.domain.*;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class JAdPod {
  private AdPod m_adpod;
  private AdPodHandle m_handle;

  public JAdPod(AdPod adpod) {
    m_adpod = adpod;
    m_handle = new AdPodHandle(this,1.0);
  }

  public AdPodHandle getHandle() {
    return m_handle;
  }

  public AdPodHandle getHandle(int rank) {
    return new AdPodHandle(this,1.0,rank);
  }


  public AdPod getAdpod() {
    return m_adpod;
  }

  public boolean isPublish() {
    return m_adpod.isPublish();
  }

  public int getRowVersion() {
    return m_adpod.getRowVersion();
  }

  public int getId() {
    return m_adpod.getId();
  }

  public int getCampaignId() {
    return m_adpod.getCampaignId();
  }

  public String getName() {
    return m_adpod.getName();
  }

  public boolean isFeatured() {
    return m_adpod.isFeatured();
  }

  public OSpec getOspec() {
    return m_adpod.getOspec();
  }

  public Date getCreationDate() {
    return m_adpod.getCreationDate();
  }

  public String getOwnerId() {
    return m_adpod.getOwnerId();
  }

  public Date getUpdateDate() {
    return m_adpod.getUpdateDate();
  }

  public String getSource() {
    return m_adpod.getSource();
  }

  public String getRegion() {
    return m_adpod.getRegion();
  }

  public String getDisplayName() {
    return m_adpod.getDisplayName();
  }

  public int getRank() {
    return m_adpod.getRank();
  }

  public List<Url> getUrls() {
    //return m_adpod.getUrls();
      return null;
  }

  public List<Theme> getThemes() {
    //return m_adpod.getThemes();
      return null;
  }

  public List<Location> getLocations() {
    //return m_adpod.getLocations();
      return null;
  }

  public Geocode getGeocode() {
    return m_adpod.getGeocode();
  }
}
