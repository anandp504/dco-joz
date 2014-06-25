/**
 * 
 */
package com.tumri.joz.util;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tumri.joz.utils.AppProperties;

/**
 * @author omprakash
 * @date May 29, 2014
 * @time 4:01:37 PM
 */
public class TestAppProperties {
	private static Logger log = Logger.getLogger(TestAppProperties.class);
	private static AppProperties instance = null;
	
	@Test
	public void test0(){
		instance = AppProperties.getInstance();
		{
			Assert.assertEquals("10", instance.getProperty("tcpServer.poolSize", "20"));
			Assert.assertEquals("20", instance.getProperty("tcpServer.poolSize1", "20"));	
		}
		{
			Assert.assertEquals(10, instance.getMaxConcurrentContentLoading());
		}
		{
			Assert.assertEquals("flash", instance.getTargetingFlashEnv());
			Assert.assertEquals("edge", instance.getTargetingHTMLEnv());
			Assert.assertEquals("mraid", instance.getTargetingMRaidEnv());
		}
		{
			Assert.assertTrue(instance.isNioEnabled());
			Assert.assertTrue(instance.isIndexValidEnabled());
		}
	}
}
