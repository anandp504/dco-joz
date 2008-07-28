/*
 * ListingsQueryHandler.java
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

import com.tumri.content.data.dictionary.DictionaryManager;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.JoZException;
import com.tumri.utils.strings.StringTokenizer;
import com.tumri.utils.PropertyUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author: nipun
 * Date: Jul 26, 2008
 * Time: 5:24:01 PM
 */
public class ZipCodeDB {

    private static ZipCodeDB g_DB;
    private static Logger log = Logger.getLogger(ZipCodeDB.class);
    private static final String CONFIG_ZIPCODE_DB_FILE = "com.tumri.joz.zipcode.file";

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
    
    private HashMap<Integer, List<Integer>> zip10MileIndex = new HashMap<Integer, List<Integer>>();
    private HashMap<Integer, List<Integer>> zip25MileIndex = new HashMap<Integer, List<Integer>>();
    private HashMap<Integer, List<Integer>> zip40MileIndex = new HashMap<Integer, List<Integer>>();
    private HashMap<Integer, List<Integer>> zip50MileIndex = new HashMap<Integer, List<Integer>>();
    private HashMap<Integer, List<Integer>> zip100MileIndex = new HashMap<Integer, List<Integer>>();

    /**
     * Adds the ZipCode and nearby zipcode into the Zip database
     * @param zipCode
     * @param nearbyZipCode
     * @param distance
     */
    public void addEntry(String zipCode, String nearbyZipCode, int distance) {
        if (!nearbyZipCode.equals(zipCode)) {
            addEntry(DictionaryManager.getId(IProduct.Attribute.kZip, zipCode),
                    DictionaryManager.getId(IProduct.Attribute.kZip, nearbyZipCode), distance);
        }
    }

    /**
     * Adds a arraylist of nearby zips to the index
     * @param zipCode
     * @param nearbyZipAL
     * @param distance
     */
    public void addEntry(String zipCode, ArrayList<String> nearbyZipAL, int distance) {
        for (String zip: nearbyZipAL) {
            if (!zip.equals(zipCode)) {
                addEntry(DictionaryManager.getId(IProduct.Attribute.kZip, zipCode),
                        DictionaryManager.getId(IProduct.Attribute.kZip, zip), distance);
            }
        }
    }

    /**
     * Adds the
     * @param key
     * @param value
     * @param distance
     */
    private void addEntry(Integer key, Integer value, int distance) {
        List<Integer> result10, result25, result40, result50 ;
        switch (distance) {
            case 10:
                List<Integer> values = zip10MileIndex.get(key);
                if (values == null) {
                    values = new ArrayList<Integer>();
                }
                values.add(value);

                zip10MileIndex.put(key, values);
                break;
            case 25:
                result10 = zip10MileIndex.get(key);
                if (result10==null || !result10.contains(value)) {
                    values = zip25MileIndex.get(key);
                    if (values == null) {
                        values = new ArrayList<Integer>();
                    }
                    values.add(value);
                    zip25MileIndex.put(key, values);
                }
                break;
            case 40:
                result10 = zip10MileIndex.get(key);
                result25 = zip25MileIndex.get(key);
                if ((result10==null || !result10.contains(value)) &&
                        (result25==null || !result25.contains(value))){
                    values = zip40MileIndex.get(key);
                    if (values == null) {
                        values = new ArrayList<Integer>();
                    }
                    values.add(value);
                    zip40MileIndex.put(key, values);
                }
                break;
            case 50:
                result10 = zip10MileIndex.get(key);
                result25 = zip25MileIndex.get(key);
                result40 = zip40MileIndex.get(key);
                if ((result10==null || !result10.contains(value)) &&
                        (result25==null || !result25.contains(value)) &&
                        (result40==null || !result40.contains(value))){
                    values = zip50MileIndex.get(key);
                    if (values == null) {
                        values = new ArrayList<Integer>();
                    }
                    values.add(value);
                    zip50MileIndex.put(key, values);
                }
                break;
            case 100:
                result10 = zip10MileIndex.get(key);
                result25 = zip25MileIndex.get(key);
                result40 = zip40MileIndex.get(key);
                result50 = zip50MileIndex.get(key);
                if ((result10==null || !result10.contains(value)) &&
                        (result25==null || !result25.contains(value)) &&
                        (result40==null || !result40.contains(value)) &&
                        (result50==null || !result50.contains(value))){
                    values = zip100MileIndex.get(key);
                    if (values == null) {
                        values = new ArrayList<Integer>();
                    }
                    values.add(value);
                    zip100MileIndex.put(key, values);
                }
                break;
            default:
                throw new UnsupportedOperationException("Distance not supported");
        }
    }

    /**
     * Gets the list of nearby zip codes
     * @param zipId
     * @param dist
     * @return
     */
    public List<Integer> getNearbyZips(Integer zipId, int dist) {
        List<Integer> result, result10, result25, result40, result50, result100;
        result = new ArrayList<Integer>();
        switch (dist) {
            case 10:
                result = zip10MileIndex.get(zipId);
                break;
            case 25:
                result10 = zip10MileIndex.get(zipId);
                result25 = zip25MileIndex.get(zipId);
                if (result10!=null) {
                    result.addAll(result10);
                }
                if (result25!=null) {
                    result.addAll(result25);
                }
                break;
            case 40:
                result10 = zip10MileIndex.get(zipId);
                result25 = zip25MileIndex.get(zipId);
                result40 = zip40MileIndex.get(zipId);
                if (result10!=null) {
                    result.addAll(result10);
                }
                if (result25!=null) {
                    result.addAll(result25);
                }
                break;
            case 50:
                result10 = zip10MileIndex.get(zipId);
                result25 = zip25MileIndex.get(zipId);
                result40 = zip40MileIndex.get(zipId);
                result50 = zip50MileIndex.get(zipId);
                if (result10!=null) {
                    result.addAll(result10);
                }
                if (result25!=null) {
                    result.addAll(result25);
                }
                if (result40!=null) {
                    result.addAll(result40);
                }
                if (result50!=null) {
                    result.addAll(result50);
                }
                break;
            case 100:
                result10 = zip10MileIndex.get(zipId);
                result25 = zip25MileIndex.get(zipId);
                result40 = zip40MileIndex.get(zipId);
                result50 = zip50MileIndex.get(zipId);
                result100 = zip100MileIndex.get(zipId);
                if (result10!=null) {
                    result.addAll(result10);
                }
                if (result25!=null) {
                    result.addAll(result25);
                }
                if (result40!=null) {
                    result.addAll(result40);
                }
                if (result50!=null) {
                    result.addAll(result50);
                }
                if (result100!=null) {
                    result.addAll(result100);
                }
                break;
            default:
                throw new UnsupportedOperationException("Distance not supported");
        }
        return result;
    }

    /**
     * Read the zip file, and instantiate the database
     */
    public void init() throws JoZException {
        String zipDbFile = AppProperties.getInstance().getProperty(CONFIG_ZIPCODE_DB_FILE);
        InputStream is =  PropertyUtils.class.getClassLoader().getResourceAsStream(zipDbFile);
        if (is == null) {
            throw new JoZException("Zipcode db file not found in classpath : " + zipDbFile);
        }
        log.info("Going to load the zipcode index from file.");
        long starttime = System.currentTimeMillis();

        try {
            InputStreamReader isr = new InputStreamReader(is, "utf8");
            BufferedReader br = new BufferedReader(isr);

            boolean eof = false;
            String line = null;
            while (!eof) {
                line = br.readLine();
                if (line == null) {
                    eof = true;
                    continue;
                }
                StringTokenizer str = new StringTokenizer(line,'\t');
                ArrayList<String> strings = str.getTokens();
                if (strings.size() != 3) {
                    throw new RuntimeException("Zip file should have 2 columns");
                }

                String zipCode = strings.get(0);
                int dist = Integer.parseInt(strings.get(1));
                String nearByZips = strings.get(2);
                StringTokenizer zipsStr = new StringTokenizer(nearByZips,',');
                ArrayList<String> zipsAL = zipsStr.getTokens();
                this.addEntry(zipCode, zipsAL, dist);
            }
            br.close();
            isr.close();
            log.info("Finished loading the zip index. Time taken = " + (System.currentTimeMillis() - starttime) + " millisecs.");
        } catch (IOException ex) {
            log.error("Could not load zip code file.");
            throw new JoZException(ex);
        } catch (Throwable t){
            log.error("Zip code Index load failed.",t );
            throw new JoZException(t);
        } finally {
            try {
                is.close();
            } catch(Throwable t) {
                log.error("Error in closing the file input stream", t);
            }
        }

    }

    public static void main(String[] args) {
        try {
            ZipCodeDB.getInstance().init();
            List<Integer> nearbyZips = ZipCodeDB.getInstance().getNearbyZips(DictionaryManager.getId(IProduct.Attribute.kZip, "95119"), 100);
            for (Integer zip: nearbyZips) {
                System.out.println(DictionaryManager.getValue(IProduct.Attribute.kZip, zip));
            }
        } catch (JoZException e) {
            System.out.println("Zip code test failed");
            e.printStackTrace();

        }
    }
    
}
