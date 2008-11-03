/*
 * ListingsQueryHandler.java
 *
 * COPYRIGHT (C) ${year} TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (${user}@tumri.com)
 * @version 1.0     ${date}
 *
 */
package com.tumri.joz.client;

import com.tumri.joz.campaign.CMAContentPoller;
import com.tumri.joz.client.impl.JozDataProviderImpl;
import com.tumri.joz.jozMain.JozData;
import com.tumri.joz.jozMain.ListingProviderFactory;
import com.tumri.joz.server.JozServer;
import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.server.domain.JozAdResponse;
import com.tumri.joz.utils.AppProperties;
import com.tumri.lls.server.domain.listing.ListingsHelper;
import com.tumri.lls.server.domain.listingformat.ListingFormatHelper;
import com.tumri.lls.server.main.LLSServerException;
import com.tumri.lls.server.main.LLSTcpServer;
import com.tumri.lls.server.utils.LlsAppProperties;
import com.tumri.utils.Polling;
import com.tumri.utils.tcp.client.TcpSocketConnectionPool;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * @author: nipun
 * Date: Mar 24, 2008
 * Time: 10:09:13 AM
 */
public class TestJozAdDataProviderImpl extends TestCase{

	private static JozDataProviderImpl _impl = null;
	private static String host = "localhost";
	private static int numRetries = 3;
	private static Thread jozServerThread = null;
	private static JozServer jozServer = null;
	private static Thread llsServerThread = null;
	private static LLSTcpServer llsServer = null;
	@BeforeClass
	public static void init()  {

		try {
			System.out.println("Starting Lls");
            int poolSize = Integer.parseInt(AppProperties.getInstance().getProperty("tcpServer.poolSize"));
            int port = Integer.parseInt(AppProperties.getInstance().getProperty("tcpServer.port"));
            int timeout= Integer.parseInt(AppProperties.getInstance().getProperty("tcpServer.timeout"));

			LlsAppProperties.getInstance("lls.properties");
			try {
				ListingsHelper.init();
				ListingFormatHelper.refreshListingHeaders(false);
			} catch (LLSServerException e) {
				System.out.println("Error initializing the LLS server");
				System.exit(1);
			}
			llsServer = new LLSTcpServer(poolSize, port, timeout);
			llsServerThread = new Thread("LLSServerThread") {
				public void run() {
					llsServer.runServer();
				}
			};
			llsServerThread.start();

			System.out.println("Starting Joz");

			JozData.init();

			String queryHandlers = AppProperties.getInstance().getProperty("tcpServer.queryHandlers");

			jozServer = new JozServer(poolSize,port,timeout,queryHandlers);
			jozServerThread = new Thread("JoZServerThread") {
				public void run() {
					jozServer.runServer();
				}
			};
			jozServerThread.start();

			_impl = new JozDataProviderImpl(host, port,poolSize,numRetries);

		} catch (Exception e) {
			System.out.println("exception in initialisation of JozServer");
			e.printStackTrace();
		}
	}
	@Test
	public void testStart(){
		init();
	}
	@Test
	public void testURLTargeting() {
		System.out.println();
		System.out.println("----------TESTING URL Targeting ------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_URL, "http://abc.com");
			///aquery.setValue(JozAdRequest.KEY_T_SPEC, "admin_custom3");
			aquery.setValue(JozAdRequest.KEY_RECIPE_ID, "1003");
			aquery.setValue(JozAdRequest.KEY_ZIP_CODE, "94041");

			JozAdResponse res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				if(errorValue != null){
					fail("testURLTargeting failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
			} else {
				fail("testURLTargeting failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetGeoData() {
		System.out.println();
		System.out.println("----------TESTING Recipe and Geo Targeting ------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_RECIPE_ID, "1001");
			aquery.setValue(JozAdRequest.KEY_ZIP_CODE, "94041");
			JozAdResponse res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				if(errorValue != null){
					fail("testGetGeoData failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
			} else {
				fail("testGetGeoData failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetGeoData2() {
		System.out.println();
		System.out.println("----------TESTING Recipe and Geo Targeting 2------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_RECIPE_ID, "1004");
			aquery.setValue(JozAdRequest.KEY_CITY, "aslkdfjasdf");
			JozAdResponse res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				if(errorValue != null){
					fail("testGetGeoData failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
			} else {
				fail("testGetGeoData failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testThemeZipLeadgenAdTypeTargeting() {
		System.out.println();
		System.out.println("----------TESTING THEME, ADTYPE, OFFER-TYPE and GEO ------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_THEME, "CustomTestLoc1");
			aquery.setValue(JozAdRequest.KEY_AD_TYPE, "custom300x600");
			aquery.setValue(JozAdRequest.KEY_ZIP_CODE, "94041");
			aquery.setValue(JozAdRequest.KEY_AD_OFFER_TYPE, "LEADGEN");

			JozAdResponse res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				if(errorValue != null){
					fail("testThemeZipLeadgenAdTypeTargeting failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
				String productIds = res.getResultMap().get(JozAdResponse.KEY_PRODIDS);
				StringTokenizer idTokenizer = new StringTokenizer(productIds, ",");
				while(idTokenizer.hasMoreTokens()){
					String id = idTokenizer.nextToken();
					if(!"_1593.US6607168".equals(id)){
						fail("testThemeZipLeadgenAdTypeTargeting failed: wrong productId returned:" + id);
						System.out.println("testThemeZipLeadgenAdTypeTargeting failed: wrong productId returned:" + id);
					}
				}
			} else {
				fail("testThemeZipLeadgenAdTypeTargeting failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void testThemeZipLeadgenAdTypeTargeting2() {
		System.out.println();
		System.out.println("----------TESTING THEME, ADTYPE, OFFER-TYPE and GEO 2------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_THEME, "CustomTestLoc1");
			aquery.setValue(JozAdRequest.KEY_AD_TYPE, "custom300x600");
			aquery.setValue(JozAdRequest.KEY_ZIP_CODE, "94041");

			JozAdResponse res = _impl.getAdData(aquery);


			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				if(errorValue != null){
					fail("testRequestThemes failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
				String productIds = res.getResultMap().get(JozAdResponse.KEY_PRODIDS);
				StringTokenizer idTokenizer = new StringTokenizer(productIds, ",");
				ArrayList<String> idList = new ArrayList<String>();
				while(idTokenizer.hasMoreTokens()){
					String id = idTokenizer.nextToken();
					idList.add(id);
				}
				if(idList.size() > 0){
					if(!"_1593.US6607168".equals(idList.get(0))){
						fail("testThemeZipLeadgenAdTypeTargeting2 failed: wrong productId returned:" + idList.get(0));
						System.out.println("testThemeZipLeadgenAdTypeTargeting2 failed: wrong productId returned:" + idList.get(0));
					}
				} else {
					fail("testThemeZipLeadgenAdTypeTargeting2 failed: incorrect number of ids returned");
					System.out.println("testThemeZipLeadgenAdTypeTargeting2 failed: incorrect number of ids returned");
				}
			} else {
				fail("testThemeZipLeadgenAdTypeTargeting2 failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testThemeZipLeadgenAdTypeTargetingFail1() {
		System.out.println();
		System.out.println("----------TESTING THEME, ADTYPE, OFFER-TYPE and GEO Fail 1------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_THEME, "CustomTestLoc1");
			aquery.setValue(JozAdRequest.KEY_AD_TYPE, "custom300x600");

			aquery.setValue(JozAdRequest.KEY_NUM_PRODUCTS, "2");
			JozAdResponse res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				//res should contain ERROR because no geo is supplied
				if(errorValue == null){
					fail("testThemeZipLeadgenAdTypeTargeting3 failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
			} else {
				fail("testThemeZipLeadgenAdTypeTargeting3 failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testThemeZipLeadgenAdTypeTargetingFail2() {
		System.out.println();
		System.out.println("----------TESTING THEME, ADTYPE, OFFER-TYPE and GEO Fail 2------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_THEME, "CustomTestLoc1");
			aquery.setValue(JozAdRequest.KEY_AD_TYPE, "custom300x600");
			aquery.setValue(JozAdRequest.KEY_ZIP_CODE, "98765");
			aquery.setValue(JozAdRequest.KEY_NUM_PRODUCTS, "2");
			JozAdResponse res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				//res should contain ERROR because incorrect geo is supplied
				if(errorValue == null){
					fail("testThemeZipLeadgenAdTypeTargeting3 failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
			} else {
				fail("testThemeZipLeadgenAdTypeTargeting3 failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	public void testThemeZipLeadgenAdTypeTargeting3() {
		System.out.println();
		System.out.println("----------TESTING THEME, ADTYPE, OFFER-TYPE and GEO 3------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_THEME, "CustomTestLoc4");
			aquery.setValue(JozAdRequest.KEY_AD_TYPE, "custom300x600");
			aquery.setValue(JozAdRequest.KEY_URL, "http://abc.com");
			aquery.setValue(JozAdRequest.KEY_CITY, "placentia");
			aquery.setValue(JozAdRequest.KEY_NUM_PRODUCTS, "2");
			JozAdResponse res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				if(errorValue != null){
					fail("testRequestThemes failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
				String productIds = res.getResultMap().get(JozAdResponse.KEY_PRODIDS);
				StringTokenizer idTokenizer = new StringTokenizer(productIds, ",");
				ArrayList<String> idList = new ArrayList<String>();
				while(idTokenizer.hasMoreTokens()){
					String id = idTokenizer.nextToken();
					idList.add(id);
				}
				if(idList.size() > 0){
					if(!"_1593.US6607168".equals(idList.get(0))){
						fail("testThemeZipLeadgenAdTypeTargeting2 failed: wrong productId returned:" + idList.get(0));
						System.out.println("testThemeZipLeadgenAdTypeTargeting2 failed: wrong productId returned:" + idList.get(0));
					}
				} else {
					fail("testThemeZipLeadgenAdTypeTargeting2 failed: incorrect number of ids returned");
					System.out.println("testThemeZipLeadgenAdTypeTargeting2 failed: incorrect number of ids returned");
				}
			} else {
				fail("testThemeZipLeadgenAdTypeTargeting2 failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testThemeZipLeadgenAdTypeTargeting4() {
		System.out.println();
		System.out.println("----------TESTING THEME, ADTYPE, OFFER-TYPE and GEO 4------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_THEME, "CustomTestLoc4");
			aquery.setValue(JozAdRequest.KEY_AD_TYPE, "custom300x600");
			aquery.setValue(JozAdRequest.KEY_URL, "http://abc.com");
			aquery.setValue(JozAdRequest.KEY_REGION, "CA");
			aquery.setValue(JozAdRequest.KEY_NUM_PRODUCTS, "2");
			JozAdResponse res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				if(errorValue != null){
					fail("testRequestThemes failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
				String productIds = res.getResultMap().get(JozAdResponse.KEY_PRODIDS);
				StringTokenizer idTokenizer = new StringTokenizer(productIds, ",");
				ArrayList<String> idList = new ArrayList<String>();
				while(idTokenizer.hasMoreTokens()){
					String id = idTokenizer.nextToken();
					idList.add(id);
				}
				if(idList.size() > 0){
					if(!"_1593.US6607168".equals(idList.get(0))){
						fail("testThemeZipLeadgenAdTypeTargeting2 failed: wrong productId returned:" + idList.get(0));
						System.out.println("testThemeZipLeadgenAdTypeTargeting2 failed: wrong productId returned:" + idList.get(0));
					}
				} else {
					fail("testThemeZipLeadgenAdTypeTargeting2 failed: incorrect number of ids returned");
					System.out.println("testThemeZipLeadgenAdTypeTargeting2 failed: incorrect number of ids returned");
				}
			} else {
				fail("testThemeZipLeadgenAdTypeTargeting2 failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testThemeZipLeadgenAdTypeTargetingFail3() {
		System.out.println();
		System.out.println("----------TESTING THEME, ADTYPE, OFFER-TYPE and GEO Fail 3------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_THEME, "CustomTestLoc4");
			aquery.setValue(JozAdRequest.KEY_AD_TYPE, "custom300x600");
			aquery.setValue(JozAdRequest.KEY_URL, "http://abc.com");
			aquery.setValue(JozAdRequest.KEY_REGION, "WA");
			aquery.setValue(JozAdRequest.KEY_NUM_PRODUCTS, "2");
			JozAdResponse res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				//res should contain ERROR because incorrect geo is supplied
				if(errorValue == null){
					fail("testThemeZipLeadgenAdTypeTargeting3 failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
			} else {
				fail("testThemeZipLeadgenAdTypeTargeting3 failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testThemeZipLeadgenAdTypeTargeting5() {
		System.out.println();
		System.out.println("----------TESTING THEME, ADTYPE, OFFER-TYPE and GEO 5------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_THEME, "CustomTestLoc4");
			aquery.setValue(JozAdRequest.KEY_AD_TYPE, "custom300x600");
			aquery.setValue(JozAdRequest.KEY_URL, "http://abc.com");
			aquery.setValue(JozAdRequest.KEY_COUNTRY, "US");
			aquery.setValue(JozAdRequest.KEY_NUM_PRODUCTS, "2");
			JozAdResponse res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				if(errorValue != null){
					fail("testRequestThemes failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
				String productIds = res.getResultMap().get(JozAdResponse.KEY_PRODIDS);
				StringTokenizer idTokenizer = new StringTokenizer(productIds, ",");
				ArrayList<String> idList = new ArrayList<String>();
				while(idTokenizer.hasMoreTokens()){
					String id = idTokenizer.nextToken();
					idList.add(id);
				}
				if(idList.size() > 0){
					if(!"_1593.US6607168".equals(idList.get(0))){
						fail("testThemeZipLeadgenAdTypeTargeting2 failed: wrong productId returned:" + idList.get(0));
						System.out.println("testThemeZipLeadgenAdTypeTargeting2 failed: wrong productId returned:" + idList.get(0));
					}
				} else {
					fail("testThemeZipLeadgenAdTypeTargeting2 failed: incorrect number of ids returned");
					System.out.println("testThemeZipLeadgenAdTypeTargeting2 failed: incorrect number of ids returned");
				}
			} else {
				fail("testThemeZipLeadgenAdTypeTargeting2 failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testThemeZipLeadgenAdTypeTargetingFail4() {
		System.out.println();
		System.out.println("----------TESTING THEME, ADTYPE, OFFER-TYPE and GEO Fail 4------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_THEME, "CustomTestLoc4");
			aquery.setValue(JozAdRequest.KEY_AD_TYPE, "custom300x600");
			aquery.setValue(JozAdRequest.KEY_URL, "http://abc.com");
			aquery.setValue(JozAdRequest.KEY_COUNTRY, "INDIA");
			aquery.setValue(JozAdRequest.KEY_NUM_PRODUCTS, "2");
			JozAdResponse res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				//res should contain ERROR because incorrect geo is supplied
				if(errorValue == null){
					fail("testThemeZipLeadgenAdTypeTargeting3 failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
			} else {
				fail("testThemeZipLeadgenAdTypeTargeting3 failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testKeywordTargeting() {
		System.out.println();
		System.out.println("----------TESTING KEYWORD BASED TARGETING ------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_LOCATION_ID, "67547202");
			aquery.setValue(JozAdRequest.KEY_AD_TYPE, "custom300x600");
			aquery.setValue(JozAdRequest.KEY_ZIP_CODE, "94040");
			aquery.setValue(JozAdRequest.KEY_KEYWORDS, "Criminal");
			JozAdResponse res = null;

			res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				if(errorValue != null){
					fail("testKeywordTargeting failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
				String productIds = res.getResultMap().get(JozAdResponse.KEY_PRODIDS);
				StringTokenizer idTokenizer = new StringTokenizer(productIds, ",");
				ArrayList<String> idList = new ArrayList<String>();
				while(idTokenizer.hasMoreTokens()){
					String id = idTokenizer.nextToken();
					idList.add(id);
				}
				if(idList.size() > 0){
					//_1593.US6607166 is the only product with the word Criminal within Adteractive so it should be returned first
					if(!"_1593.US6607166".equals(idList.get(0))){
						fail("testKeywordTargeting failed: wrong productId returned:" + idList.get(0));
						System.out.println("testKeywordTargeting failed: wrong productId returned:" + idList.get(0));
					}
				} else {
					fail("testKeywordTargeting failed: incorrect number of ids returned");
					System.out.println("testKeywordTargeting failed: incorrect number of ids returned");
				}
			} else {
				fail("testKeywordTargeting failed: response == null");
				System.out.println("testKeywordTargeting Lookup faild. No result");
			}

		} catch (JoZClientException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

	}

	@Test
	public void testLocationBasedTargeting() {
		System.out.println();
		System.out.println("----------TESTING LOCATION BASED TARGETING 1------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_LOCATION_ID, "67547200");
			aquery.setValue(JozAdRequest.KEY_AD_TYPE, "custom300x600");

			JozAdResponse res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				if(errorValue != null){
					fail("testLocationBasedTargeting failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
			} else {
				fail("testLocationBasedTargeting failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void testLocationBasedTargeting3() {
		System.out.println();
		System.out.println("----------TESTING LOCATION BASED TARGETING 3------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_LOCATION_ID, "12345678");
			aquery.setValue(JozAdRequest.KEY_AD_TYPE, "custom300x600");

			JozAdResponse res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				//res should contain an ERROR because incorrect Location ID has been included
				if(errorValue == null){
					fail("testLocationBasedTargeting3 failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
			} else {
				fail("testLocationBasedTargeting3 failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testLocationBasedTargeting2() {
		System.out.println();
		System.out.println("----------TESTING LOCATION BASED TARGETING 2------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_LOCATION_ID, "67547200");
			aquery.setValue(JozAdRequest.KEY_AD_TYPE, "mediumrectangle");

			JozAdResponse res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				//res should contain an ERROR because incorrect adType has been included
				if(errorValue == null){
					fail("testLocationBasedTargeting2 failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
			} else {
				fail("testLocationBasedTargeting2 failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGeoBasedTargeting() {
		System.out.println();
		System.out.println("----------TESTING GEO BASED TARGETING ------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_ZIP_CODE,"94041");
			aquery.setValue(JozAdRequest.KEY_THEME, "CustomTestLoc1");
			aquery.setValue(JozAdRequest.KEY_AD_TYPE, "custom300x600");

			JozAdResponse res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				if(errorValue != null){
					fail("testGeoBasedTargeting failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
			} else {
				fail("testGeoBasedTargeting failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testRecipePriceTargeting() {
		System.out.println();
		System.out.println("----------TESTING RECIPE PRICE BASED TARGETING ------------");
		System.out.println();
		try {
			JozAdRequest aquery = new JozAdRequest();
			aquery.setValue(JozAdRequest.KEY_RECIPE_ID, "1005");
			aquery.setValue(JozAdRequest.KEY_NUM_PRODUCTS, "5");

			JozAdResponse res = _impl.getAdData(aquery);

			if(res != null){
				printResponseData(res);
				String errorValue = res.getResultMap().get(JozAdResponse.KEY_ERROR);
				if(errorValue != null){
					fail("testRecipePriceTargeting failed error:" + errorValue);
					System.out.println("Lookup failed. ResultMap has Error: " + errorValue);
				}
				String productIds = res.getResultMap().get(JozAdResponse.KEY_PRODIDS);
				StringTokenizer idTokenizer = new StringTokenizer(productIds, ",");
				ArrayList<String> idList = new ArrayList<String>();
				while(idTokenizer.hasMoreTokens()){
					String id = idTokenizer.nextToken();
					idList.add(id);
				}
				if(idList.size() > 0){
					if(!idList.contains("_1594.US7220128") || !idList.contains("_1594.US7207300")){
						fail("testRecipePriceTargeting failed: wrong productId returned:" + idList.get(0));
						System.out.println("testRecipePriceTargeting failed: wrong productId returned:" + idList.get(0));
					}
				} else {
					fail("testRecipePriceTargeting failed: incorrect number of ids returned");
					System.out.println("testRecipePriceTargeting failed: incorrect number of ids returned");
				}
			} else {
				fail("testRecipePriceTargeting failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void teardown() {
		TcpSocketConnectionPool.getInstance().tearDown();
		Polling.getInstance().shutdown();
		CMAContentPoller.getInstance().shutdown();
		System.out.println("Stopping Joz");
		ListingProviderFactory.shutdown();
		if  (jozServerThread!=null) {
			jozServerThread.interrupt();
		}
		jozServer.shutdown();

		System.out.println("Stopping lls");
		if  (llsServerThread!=null) {
			llsServerThread.interrupt();
		}
		llsServer.shutdown();
	}

	private void printResponseData(JozAdResponse response) {
		HashMap<String, String> resultMap = response.getResultMap();
		Iterator<String> resultIter = resultMap.keySet().iterator();
		while(resultIter.hasNext()){
			String resultKey = resultIter.next();
			String resultVal = resultMap.get(resultKey);
			System.out.print(resultKey + "= ");
			System.out.print(resultVal);
			System.out.println("\n");
		}
	}
}
