package com.tumri.joz.targeting;

import com.tumri.joz.utils.AppProperties;
import org.apache.log4j.Logger;

/**
 * Gets the score fro  site and geo elements from the properties file.
 *
 * @author bpatel
 */
public class TargetingScoreHelper {
    private double locationScore = 1.0;
    private double themeScore    = 0.9;
    private double urlScore      = 0.8;


    private double defaultScore  = 0.1;
    private double countryScore  = 0.75;
    private double regionScore   = 0.80;
    private double cityScore     = 0.85;
    private double zipcodeScore  = 1.0;
    private double dmacodeScore  = 0.95;
    private double areacodeScore = 0.90;

    private double runOfNetworkScore = 0.2;
    private int runOfNetworkWeight   = 1;
    private double geoNoneScore      = 0.2;
    private int geoNoneWeight        = 1;

    private static Logger log = Logger.getLogger (TargetingScoreHelper.class);

    private static TargetingScoreHelper scoreHelper = new TargetingScoreHelper();

    private TargetingScoreHelper() {
        initialize();
    }

    public static TargetingScoreHelper getInstance() {
        return scoreHelper;
    }

    public double getLocationScore() {
        return locationScore;
    }

    public double getThemeScore() {
        return themeScore;
    }

    public double getUrlScore() {
        return urlScore;
    }

    public double getDefaultScore() {
        return defaultScore;
    }

    public double getCountryScore() {
        return countryScore;
    }

    public double getRegionScore() {
        return regionScore;
    }

    public double getCityScore() {
        return cityScore;
    }

    public double getZipcodeScore() {
        return zipcodeScore;
    }

    public double getDmacodeScore() {
        return dmacodeScore;
    }

    public double getAreacodeScore() {
        return areacodeScore;
    }

    public double getRunOfNetworkScore() {
        return runOfNetworkScore;
    }

    public int getRunOfNetworkWeight() {
        return runOfNetworkWeight;
    }

    public double getGeoNoneScore() {
        return geoNoneScore;
    }

    public int getGeoNoneWeight() {
        return geoNoneWeight;
    }

    private void initialize() {
        double score;
        String scoreStr;
        try {
            scoreStr = AppProperties.getInstance().getProperty("com.tumri.targeting.locationScore");
            if(scoreStr != null && !"".equals(scoreStr)) {
                score = Double.parseDouble(scoreStr);
                locationScore = score;
            }
            scoreStr = AppProperties.getInstance().getProperty("com.tumri.targeting.themeScore");
            if(scoreStr != null && !"".equals(scoreStr)) {
                score = Double.parseDouble(scoreStr);
                themeScore = score;
            }
            scoreStr = AppProperties.getInstance().getProperty("com.tumri.targeting.urlScore");
            if(scoreStr != null && !"".equals(scoreStr)) {
                score = Double.parseDouble(scoreStr);
                urlScore = score;
            }
            scoreStr = AppProperties.getInstance().getProperty("com.tumri.targeting.defaultScore");
            if(scoreStr != null && !"".equals(scoreStr)) {
                score = Double.parseDouble(scoreStr);
                defaultScore = score;
            }
            scoreStr = AppProperties.getInstance().getProperty("com.tumri.targeting.countryScore");
            if(scoreStr != null && !"".equals(scoreStr)) {
                score = Double.parseDouble(scoreStr);
                countryScore = score;
            }
            scoreStr = AppProperties.getInstance().getProperty("com.tumri.targeting.regionScore");
            if(scoreStr != null && !"".equals(scoreStr)) {
                score = Double.parseDouble(scoreStr);
                regionScore = score;
            }
            scoreStr = AppProperties.getInstance().getProperty("com.tumri.targeting.cityScore");
            if(scoreStr != null && !"".equals(scoreStr)) {
                score = Double.parseDouble(scoreStr);
                cityScore = score;
            }
            scoreStr = AppProperties.getInstance().getProperty("com.tumri.targeting.zipcodeScore");
            if(scoreStr != null && !"".equals(scoreStr)) {
                score = Double.parseDouble(scoreStr);
                zipcodeScore = score;
            }
            scoreStr = AppProperties.getInstance().getProperty("com.tumri.targeting.dmacodeScore");
            if(scoreStr != null && !"".equals(scoreStr)) {
                score = Double.parseDouble(scoreStr);
                dmacodeScore = score;
            }
            scoreStr = AppProperties.getInstance().getProperty("com.tumri.targeting.areacodeScore");
            if(scoreStr != null && !"".equals(scoreStr)) {
                score = Double.parseDouble(scoreStr);
                areacodeScore = score;
            }
            scoreStr = AppProperties.getInstance().getProperty("com.tumri.targeting.runOfNetworkScore");
            if(scoreStr != null && !"".equals(scoreStr)) {
                score = Double.parseDouble(scoreStr);
                runOfNetworkScore = score;
            }
            scoreStr = AppProperties.getInstance().getProperty("com.tumri.targeting.geoNoneScore");
            if(scoreStr != null && !"".equals(scoreStr)) {
                score = Double.parseDouble(scoreStr);
                geoNoneScore = score;
            }
            int weight;
            String weightStr;

            weightStr = AppProperties.getInstance().getProperty("com.tumri.targeting.runOfNetworkWeight");
            if(weightStr != null && !"".equals(weightStr)) {
                weight = Integer.parseInt(weightStr);
                runOfNetworkWeight = weight;
            }
            weightStr = AppProperties.getInstance().getProperty("com.tumri.targeting.geoNoneWeight");
            if(weightStr != null && !"".equals(weightStr)) {
                weight = Integer.parseInt(weightStr);
                geoNoneWeight = weight;
            }
        }
        catch(NullPointerException e) {
            log.error("Invalid entry for targeting elements score specified in properties file");
        }
        catch(Exception e) {
            log.error("Invalid entry for targeting elements score specified in properties file");
        }
    }
}