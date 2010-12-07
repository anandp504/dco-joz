package com.tumri.joz.jozMain;

import com.tumri.joz.utils.AppProperties;
import com.tumri.utils.strings.JStringBuilder;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Container for the feature list passed back in get-ad-data requests.
 * NOTE: There are no setter methods on purpose.
 * @author nipun
 *
 */
public class Features {
    public static final String FEATURE_WIDGET_SEARCH = "SEARCH-IN-WIDGET";
    public static final String FEATURE_MINE_URL_SEARCH = "SEARCH-MINE-URL";
    public static final String FEATURE_SCRIPT_SEARCH = "SEARCH-SCRIPT_KEYWORD";
    public static final String FEATURE_SEARCH_KEYWORDS = "SEARCH-KEYWORDS";
    public static final String FEATURE_TOPK = "TOPK";
    public static final String FEATURE_RANKSORT = "SORT_RANK";
    public static final String FEATURE_DISCOUNTSORT = "SOR_DISCOUNT";

    private static String _joz_version;
    private static String _host_name = null;
    private HashMap<String, String> jozFeaturesMap = null;

    private int campaignId = 0;
    private String campaignName = null;
    private int adPodId = 0;
    private String adpodName = null;
    private int recipeId = 0;
    private String recipeName = null;
    private int expId = 0;
    private String expName = null;
    private int campaignClientId = 0;
    private String campaignClientName = null;
    private boolean bGeoUsed = false;
    private String targetedLocationId = null;
    private String targetedLocationName = null;
    private int locationClientId = 0;
    private String locationClientName = null;

    static {
        try {
        	_host_name = java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            _host_name = "unknown";
        }
    }
    
    public Features() {
        if (_joz_version==null) {
            _joz_version = AppProperties.getInstance().getJozReleaseVersion();
        }
    }
    
    public void setJozFeaturesMap(HashMap<String, String> jozFeatures) {
    	this.jozFeaturesMap = jozFeatures;
    }
    
    public String toString(long elapsed_time) {
        JStringBuilder sbuild = new JStringBuilder(1000);
        sbuild.append("Time=");
        sbuild.append(new Long(elapsed_time).toString());
        sbuild.append(",Ver=");
        sbuild.append(_joz_version);
        sbuild.append(",Host=");
        sbuild.append(_host_name);
        if (jozFeaturesMap!=null && !jozFeaturesMap.isEmpty()) {
            sbuild.append(",Features=(");
        	Iterator<String> featureKeys = jozFeaturesMap.keySet().iterator();
        	String featureBuiltUpStr = "";
        	while (featureKeys.hasNext()) {
        		String featureKeyStr = featureKeys.next();
        		String featureValStr = jozFeaturesMap.get(featureKeyStr);
        		if (featureKeyStr!=null && !"".equals(featureKeyStr) && featureValStr!=null && !"".equals(featureValStr)) {
        			featureBuiltUpStr = featureBuiltUpStr + "," + featureKeyStr + "=" + featureValStr; 
		        }
        	}
	        sbuild.append(featureBuiltUpStr);
            sbuild.append(")");
        }
       return sbuild.toString();

    }
    

    public int getAdPodId() {
        return adPodId;
    }

    public void setAdPodId(int adPodId) {
        this.adPodId = adPodId;
    }

    public int getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(int campaignId) {
        this.campaignId = campaignId;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public void addFeatureDetail(String key, String val) {
        if (jozFeaturesMap == null) {
            jozFeaturesMap = new HashMap<String, String>();
        }
        jozFeaturesMap.put(key, val);
    }

    public String getFeaturesDetail(String key) {
        return jozFeaturesMap.get(key);
    }

    public String getAdpodName() {
        return adpodName;
    }

    public void setAdpodName(String adpodName) {
        this.adpodName = adpodName;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public boolean isGeoUsed() {
        return bGeoUsed;
    }

    public void setGeoUsed(boolean bGeoUsed) {
        this.bGeoUsed = bGeoUsed;
    }

    public String getTargetedLocationId() {
        return targetedLocationId;
    }

    public void setTargetedLocationId(String targetedLocationId) {
        this.targetedLocationId = targetedLocationId;
    }

    public String getTargetedLocationName() {
        return targetedLocationName;
    }

    public void setTargetedLocationName(String targetedLocationName) {
        this.targetedLocationName = targetedLocationName;
    }

    public int getCampaignClientId() {
        return campaignClientId;
    }

    public void setCampaignClientId(int campaignClientId) {
        this.campaignClientId = campaignClientId;
    }

    public String getCampaignClientName() {
        return campaignClientName;
    }

    public void setCampaignClientName(String campaignClientName) {
        this.campaignClientName = campaignClientName;
    }

    public int getLocationClientId() {
        return locationClientId;
    }

    public void setLocationClientId(int locationClientId) {
        this.locationClientId = locationClientId;
    }

    public String getLocationClientName() {
        return locationClientName;
    }

    public void setLocationClientName(String locationClientName) {
        this.locationClientName = locationClientName;
    }

    public int getExpId() {
        return expId;
    }

    public void setExpId(int expId) {
        this.expId = expId;
    }

    public String getExpName() {
        return expName;
    }

    public void setExpName(String expName) {
        this.expName = expName;
    }

    /*public String toString(){
         StringBuffer buff = new StringBuffer();

         buff.append("_joz_version = " + _joz_version);
         buff.append(" | ");
         buff.append("_host_name = " +  _host_name);
         buff.append(" | ");
         buff.append("jozFeaturesMap = " + jozFeaturesMap);
         buff.append(" | ");
         buff.append("campaignId = " + campaignId);
         buff.append(" | ");
         buff.append("campaignName = " + campaignName);
         buff.append(" | ");
         buff.append("adPodId = " + adPodId);
         buff.append(" | ");
         buff.append("adpodName = " + adpodName);
         buff.append(" | ");
         buff.append("recipeId = " + recipeId);
         buff.append(" | ");
         buff.append("recipeName = " + recipeName);
         buff.append(" | ");
         buff.append("campaignClientId = " + campaignClientId);
         buff.append(" | ");
         buff.append("campaignClientName = " + campaignClientName);
         buff.append(" | ");
         buff.append("bGeoUsed = " + bGeoUsed);
         buff.append(" | ");
         buff.append("targetedLocationId = " + targetedLocationId);
         buff.append(" | ");
         buff.append("targetedLocationName = " + targetedLocationName);
         buff.append(" | ");
         buff.append("locationClientId = " + locationClientId);
         buff.append(" | ");
         buff.append("locationClientName = " + locationClientName);
         return buff.toString();
     }*/
}
