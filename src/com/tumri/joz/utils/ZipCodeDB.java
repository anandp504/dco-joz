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
import com.tumri.joz.products.IProduct;
import com.tumri.joz.index.LatLongIndex;
import com.tumri.joz.Query.SetIntersector;
import com.tumri.joz.ranks.AttributeWeights;
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
import java.util.HashSet;

/**
 * Zip to lat long DB implementation
 *
 * @author: nipun
 * Date: Jul 26, 2008
 * Time: 5:24:01 PM
 */
public class ZipCodeDB implements IWeight<Integer> {

	private static ZipCodeDB g_DB;
	private static Logger log = Logger.getLogger(ZipCodeDB.class);
	private static final String CONFIG_ZIPLATLONG_DB_FILE = "com.tumri.joz.ziplatlong.file";
	private static final double BUCKETING_FACTOR = 10.0;
	public TreeMap<Integer, Pair<Double, Double>> zipLatLongMap = new TreeMap<Integer, Pair<Double, Double>>();

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

	public double getWeight(Integer v, double minWeight) {
		return 1;
	}

	public double getMaxWeight() {
		return 1;
	}

	public double getMinWeight() {
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
				StringTokenizer str = new StringTokenizer(line, ',');
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
		} catch (Throwable t) {
			log.fatal("Zip code lat long db failed.", t);
			throw new JoZException(t);
		} finally {
			try {
				is.close();
			} catch (Throwable t) {
				log.error("Error in closing the file input stream", t);
			}
		}

	}

	/**
	 * Returns the normalized set of lat long for a given zip
	 *
	 * @param zipCodeStr
	 * @return
	 */
	public Pair<Integer, Integer> getNormalizedLatLong(String zipCodeStr) {
		if (zipCodeStr == null) {
			return null;
		}
		Integer zipCode;
		try {
			zipCode = Integer.parseInt(zipCodeStr);
		} catch (NumberFormatException e) {
			return null;
		}
		Pair<Double, Double> latLong = zipLatLongMap.get(zipCode);
		if (latLong != null) {
			return new Pair<Integer, Integer>(normalizeLatLong(latLong.getFirst()), normalizeLatLong(latLong.getSecond()));
		} else {
			return null;
		}
	}

	/**
	 * returns a lat long value given teh zipcode
	 *
	 * @param zipCode
	 * @return
	 */
	public Pair<Double, Double> getLatLong(Integer zipCode) {
		return zipLatLongMap.get(zipCode);
	}

	/**
	 * Normalize the lat/long value
	 *
	 * @param latLongObj
	 * @return
	 */
	public Integer normalizeLatLong(Double latLongObj) {
		return (int) (Math.round(latLongObj * BUCKETING_FACTOR));
	}

	/**
	 * returns the set of keys to be looked up in the index based on lat or long
	 * We will assume that all distances are stored from 0.0 Lat and 0.0 Long
	 * DistLat = 69.1 * (Lat1-0)
	 * DistLong = 69.1 * (Lg1-0) * cos(Lat1 / 57.3)
	 *
	 * @param zipCode
	 * @param isLat
	 * @param rad
	 * @return
	 */
	public ArrayList<Integer> getKeys(String zipCode, boolean isLat, int rad) {
		ArrayList<Integer> keys = new ArrayList<Integer>();
		if (zipCode == null) {
			return keys;
		}
		Integer zipInt;
		try {
			zipInt = Integer.parseInt(zipCode);
		} catch (NumberFormatException e) {
			return keys;
		}
		try {
			HashSet<Integer> keySet = new HashSet<Integer>();
			Pair<Double, Double> latLong = getLatLong(zipInt);
			if (latLong == null) {
				log.debug("Invalid zip code passed in for lookup : " + zipInt);
				return keys;
			}
			Double latObj = latLong.getFirst();
			if (isLat) {
				int latInt = normalizeLatLong(latObj);
				int deltaLat = normalizeLatLong(rad / 69.1);
				for (int i = (latInt - deltaLat); i <= (latInt + deltaLat); i++) {
					keySet.add(i);
				}
			} else {
				Double longObj = latLong.getSecond();
				int longInt = normalizeLatLong(longObj);
				int deltaLong = normalizeLatLong(rad / (69.1 * Math.cos(latObj / 57.3)));
				for (int i = (longInt - deltaLong); i <= (longInt + deltaLong); i++) {
					keySet.add(i);
				}
			}
			if (!keySet.isEmpty()) {
				keys.addAll(keySet);
			}
		} catch (Exception e) {
			log.error("Could not get the keys for the given zipcode", e);
		}
		return keys;
	}

	/**
	 * Populate the indices using the following formula :
	 *
	 * @param zipCodeId
	 * @param latitude
	 * @param longitude
	 */
	private void populateIndex(Integer zipCodeId, double latitude, double longitude) {
		zipLatLongMap.put(zipCodeId, new Pair<Double, Double>(latitude, longitude));
	}

	/**
	 * Test method
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ZipCodeDB.getInstance().init();
			ArrayList<Integer> keys = ZipCodeDB.getInstance().getKeys("95113", false, 40);
			for (Integer k : keys) {
				System.out.println(k);
			}
		} catch (JoZException e) {
			log.error("Zip code test failed", e);
		}
	}

}
