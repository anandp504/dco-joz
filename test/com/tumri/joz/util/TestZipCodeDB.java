/**
 * 
 */
package com.tumri.joz.util;

import java.util.ArrayList;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.tumri.joz.JoZException;
import com.tumri.joz.utils.ZipCodeDB;
import com.tumri.utils.Pair;

/**
 * @author omprakash
 * @date Jun 5, 2014
 * @time 4:22:15 PM
 */
public class TestZipCodeDB {
//	00501,40.8151,-73.0455
//	00544,40.8132,-73.0476
//	00601,18.1642,-66.7227
//	00602,18.3974,-67.1679
//	00603,18.4409,-67.1508
	private static Logger log = Logger.getLogger(TestZipCodeDB.class);
	@Test
	public void test() throws JoZException{
		try {
			ZipCodeDB.getInstance().init();
			ArrayList<Integer> keys = ZipCodeDB.getInstance().getKeys("95113", false, 40);
		} catch (JoZException e) {
			log.error("Zip code test failed", e);
		}
		{
			String zipCodeStr = "00501";
			Pair<Integer, Integer> latlong = ZipCodeDB.getInstance().getNormalizedLatLong(zipCodeStr);
			Assert.assertEquals(408, latlong.getFirst().intValue());
			Assert.assertEquals(-730, latlong.getSecond().intValue());	
		}
		{
			String zipCodeStr = "00601";
			Pair<Integer, Integer> latlong = ZipCodeDB.getInstance().getNormalizedLatLong(zipCodeStr);
			Assert.assertEquals(182, latlong.getFirst().intValue());
			Assert.assertEquals(-667, latlong.getSecond().intValue());	
		}
		{
			String zipCodeStr = "00602";
			Pair<Integer, Integer> latlong = ZipCodeDB.getInstance().getNormalizedLatLong(zipCodeStr);
			Assert.assertEquals(184, latlong.getFirst().intValue());
			Assert.assertEquals(-672, latlong.getSecond().intValue());	
		}
		{
			Integer zipCodeStr = Integer.parseInt("00602");
			Pair<Double, Double> latlong = ZipCodeDB.getInstance().getLatLong(zipCodeStr);
			Assert.assertEquals(18.3974, latlong.getFirst().doubleValue());
			Assert.assertEquals(-67.1679, latlong.getSecond().doubleValue());	
		}
		{
			Integer zipCodeStr = Integer.parseInt("00501");
			Pair<Double, Double> latlong = ZipCodeDB.getInstance().getLatLong(zipCodeStr);
			Assert.assertEquals(40.8151, latlong.getFirst().doubleValue());
			Assert.assertEquals(-73.0455, latlong.getSecond().doubleValue());	
		
			ArrayList<Integer> lists1 = ZipCodeDB.getInstance().getKeys("00501", true, 100);
			ArrayList<Integer> lists2 = ZipCodeDB.getInstance().getKeys("00501", false, 50);
			ArrayList<Integer> lists3 = ZipCodeDB.getInstance().getKeys("00501", true, 5);
			ArrayList<Integer> lists4 = ZipCodeDB.getInstance().getKeys("00501", false, 5);
			ArrayList<Integer> lists5 = ZipCodeDB.getInstance().getKeys("11111", false, 100);
			ArrayList<Integer> lists6 = ZipCodeDB.getInstance().getKeys("11111", true, 10);
			System.out.println(lists1);
			System.out.println(lists2);
			System.out.println(lists3);
			System.out.println(lists4);
			System.out.println(lists5);
			System.out.println(lists6);
		}
		{
			ZipCodeDB instance = ZipCodeDB.getInstance();
			Assert.assertFalse(instance.mustMatch());
			Assert.assertEquals(1.0, instance.getMinWeight());
			Assert.assertEquals(1.0, instance.getMaxWeight());
			Assert.assertEquals(1.0, instance.getWeight(new Integer(1234), 1.0));
		}
		ZipCodeDB instance = ZipCodeDB.getInstance();
		instance.zipLatLongMap.entrySet().clear();
	}	
}
