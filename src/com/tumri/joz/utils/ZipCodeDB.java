/*
 * ZipCodeDB.java
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

import com.tumri.joz.JoZException;
import com.tumri.joz.index.LatLongIndex;
import com.tumri.joz.Query.SetIntersector;
import com.tumri.joz.ranks.IWeight;
import com.tumri.utils.Pair;
import com.tumri.utils.data.*;
import com.tumri.utils.strings.StringTokenizer;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * @author: nipun
 * Date: Jul 26, 2008
 * Time: 5:24:01 PM
 */
public class ZipCodeDB implements IWeight<Integer> {

    private static ZipCodeDB g_DB;
    private static Logger log = Logger.getLogger(ZipCodeDB.class);
    private static final String CONFIG_ZIPLATLONG_DB_FILE = "com.tumri.joz.ziplatlong.file";

    public TreeMap<Integer, Pair<Double, Double>> zipLatLongMap = new TreeMap<Integer, Pair<Double, Double>>();

    private LatLongIndex<Integer, Integer> latitudeIndex = new LatLongIndex<Integer, Integer>(LatLongIndex.Attribute.kLatitude);
    private LatLongIndex<Integer, Integer> longitudeIndex = new LatLongIndex<Integer, Integer>(LatLongIndex.Attribute.kLongitude);

    public static ZipCodeDB getInstance() {
        if (g_DB == null) {
            synchronized (ZipCodeDB.class) {
                if (g_DB == null) {
                    g_DB = new ZipCodeDB();
                }
            }
        }
        return g_DB;
    }

    public double getWeight(Integer v) {
        return 1;
    }

    public int match(Integer v) {
        return 1;
    }

    /**
     * @return true if the match is mandatory, false otherwise
     */
    public boolean mustMatch() {
        return false;
    }

    /**
     * Read the zip lat long file, and instantiate the database
     */
    public void init() throws JoZException {
        log.info("Going to load the zip lat long db.");
        long starttime = System.currentTimeMillis();
        FileInputStream is = null;
        try {
            String zipDbFile = AppProperties.getInstance().getProperty(CONFIG_ZIPLATLONG_DB_FILE);
            is = new FileInputStream(zipDbFile);

            InputStreamReader isr = new InputStreamReader(is, "utf8");
            BufferedReader br = new BufferedReader(isr);

            boolean eof = false;
            String line;
            while (!eof) {
                line = br.readLine();
                if (line == null) {
                    eof = true;
                    continue;
                }
                StringTokenizer str = new StringTokenizer(line,',');
                ArrayList<String> strings = str.getTokens();
                if (strings.size() != 3) {
                    throw new RuntimeException("Zip lat long file should have 3 columns");
                }

                int zipCode = Integer.parseInt(strings.get(0));
                double latitude = Double.parseDouble(strings.get(1));
                double longitude = Double.parseDouble(strings.get(2));
                //Add to indexes
                populateIndex(zipCode, latitude, longitude);
            }
            br.close();
            isr.close();
            log.info("Finished loading the zip lat long db. Time taken = " + (System.currentTimeMillis() - starttime) + " millisecs.");
        } catch (IOException ex) {
            log.fatal("Could not load zip lat long db file.");
            throw new JoZException(ex);
        } catch (Throwable t){
            log.fatal("Zip code lat long db failed.",t );
            throw new JoZException(t);
        } finally {
            try {
                is.close();
            } catch(Throwable t) {
                log.error("Error in closing the file input stream", t);
            }
        }

    }

    /**
     * returns a lat long value given teh zipcode
     * @param zipCode
     * @return
     */
    public Pair<Double, Double> getLatLong(Integer zipCode) {
       return zipLatLongMap.get(zipCode);
    }

    /**
     * Populate the indices using the following formula :
     * We will assume that all distances are stored from 0.0 Lat and 0.0 Long
     * DistLat = 69.1 * (Lat1-0)
     * DistLong = 69.1 * (Lg1-0) * cos(Lat1 / 57.3)
     * @param zipCodeId
     * @param latitude
     * @param longitude
     */
    private void populateIndex(Integer zipCodeId, double latitude, double longitude) {
        int latint = (int)latitude;
        int longint = (int)longitude;

        zipLatLongMap.put(zipCodeId,new Pair<Double, Double>(latitude, longitude));
        //Convert into distances in miles  and put into index
        latitudeIndex.put(latint,zipCodeId);
        longitudeIndex.put(longint,zipCodeId);
    }

    /**
     * Gets all the nearby zips given a specific zip given a radius
     * Results are sorted by their promixity to the input zip code
     * @param zipId
     * @param rad
     */
    public SortedSet<ZipCodeHandle> getNearbyZips(Integer zipId, double rad) {
        //1. Get the Latitude, long for this zip
        Pair<Double, Double> latLong = zipLatLongMap.get(zipId);
        if (latLong==null || rad ==0) {
            return null;
        }
        Double latObj = latLong.getFirst();
        Double longObj = latLong.getSecond();

        //2. Convert radius into Latitude and Logitude delta values
        double deltaLat = rad/ 69.1;
        double deltaLong = rad / (69.1 * Math.cos(latObj/57.3));

        //3. Get the nearby Zips for the lat and long
        SetUnion<Integer> latMultiZips = new SetUnion<Integer>();
        for (int i=(int)(latObj-deltaLat);i<=(int)(latObj+deltaLat);i++){
            latMultiZips.add(latitudeIndex.get(i));
        }

        SetUnion<Integer> longMultiZips = new SetUnion<Integer>();
        for (int i=(int)(longObj-deltaLong);i<=(int)(longObj+deltaLong);i++){
            longMultiZips.add(longitudeIndex.get(i));
        }
        SortedSet<Integer> sortedlatZips = new SortedArraySet<Integer>(latMultiZips);
        SortedSet<Integer> sortedlongZips = new SortedArraySet<Integer>(longMultiZips);

        //4. Intersect and present zipResults
        ZipSetIntersector intersector = new ZipSetIntersector();
        intersector.include(sortedlatZips, this);
        intersector.include(sortedlongZips, this);
        intersector.setStrict(true);
        intersector.setMax(0);
        SortedSet<Integer> zipResults = intersector.intersect();

        SortedSet<ZipCodeHandle> zipCodeHandles = new SortedArraySet<ZipCodeHandle>(new ZipCodeHandle(1, 1.0));

        //5. Filter out erroneous zips based on distance
        for (Integer zipCdId : zipResults) {
            if (zipCdId.equals(zipId)) {
                continue;
            }
            Pair<Double, Double> tmpLatLong = zipLatLongMap.get(zipCdId);
            Double tLatObj = tmpLatLong.getFirst();
            Double tLongObj = tmpLatLong.getSecond();
            double dist = getDistance(latObj, longObj, tLatObj, tLongObj);
            if (dist <= rad) {
                zipCodeHandles.add(new ZipCodeHandle(zipCdId, dist));
            }
        }
        return zipCodeHandles;

    }

    /**
     * Get the distance between 2 lat long points. The formula used is :
     * One degree of latitude is equal to 69.1 miles. One degree of longitude is equal to 69.1 miles at
     * the equator. North or south of the equator, one degree of longitude is a smaller distance. It's
     * reduced by the cosine of the latitude. Dividing the latitude number by 57.3 converts it to radians.
     * DistLat = 69.1 * (Lat2-Lat1)
     * DistLong = 69.1 * (Lg2-Lg1) * cos(Lat1 / 57.3)
     * Dist = (DlstLat2 (squared) + DlstLong2(squared)^ 0.5
     * @param lat1 - Latitude of the first point
     * @param long1 - Longitude of the first point
     * @param lat2 - Latitude of the second point
     * @param long2 - Longitude of the second point
     * @return distance in miles
     */
    private double getDistance(double lat1, double long1, double lat2, double long2) {
        double distLat = 69.1 * ( lat2 - lat1);
        double distLong = 69.1 * ( long2 - long1) * Math.cos(lat1/57.3);
        double dist = Math.sqrt(distLat*distLat + distLong * distLong);
        return dist;
    }

    public class ZipSetIntersector extends SetIntersector<Integer> {

        public Integer getResult(Integer z, Double score) {
            return z;
                }

        public ZipSetIntersector() {
            super();
            this.setStrict(true);
            this.setMax(0);
            }

        public ZipSetIntersector(ZipSetIntersector z) {
            super(z);
        }

        public SetIntersector<Integer> clone() throws CloneNotSupportedException {
            return new ZipSetIntersector(this);
            }
        }

        /**
     * Test method
     * @param args
     */
    public static void main(String[] args) {
        try {
            ZipCodeDB.getInstance().init();
            long start = System.currentTimeMillis();
            for (int i=0;i<=1;i++) {
                SortedSet<ZipCodeHandle> nearbyZips = ZipCodeDB.getInstance().getNearbyZips(22040, 40);
                System.out.println(nearbyZips.size());
            }
            System.out.println("test Finished. Time taken = " + (System.currentTimeMillis() - start) + " millis");
//
//            SortedSet<ZipCodeHandle> nearbyZips = ZipCodeDB.getInstance().getNearbyZips(95119, 100);
//
//            if (nearbyZips==null) {
//                System.out.println("No nearby zips found");
//                System.exit(0);
//            }
//            log.info("Number of nearby Zips found = " + nearbyZips.size());
//            for (ZipCodeHandle zip: nearbyZips) {
//                log.info(zip.getOid() + " , " + zip.getScore());
//            }


        } catch (JoZException e) {
            log.error("Zip code test failed",e);
        }
    }

}
