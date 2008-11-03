package com.tumri.joz.client;

import com.tumri.joz.campaign.CMAContentPoller;
import com.tumri.joz.client.impl.JozDataProviderImpl;
import com.tumri.joz.jozMain.JozData;
import com.tumri.joz.jozMain.ListingProviderFactory;
import com.tumri.joz.server.JozServer;
import com.tumri.joz.server.domain.*;
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
import java.util.HashSet;

/**
 * Unit Tests designed for testing JozQARequest/Handler/Response.
 * User: scbraun
 * Date: Oct 15, 2008
 * Time: 2:53:57 PM
 */
public class TestJozQAProviderImpl extends TestCase{

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
            llsServer = new LLSTcpServer(poolSize,port,timeout);
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
	public void testGetAllAdvertisers() {
		System.out.println();
		System.out.println("----------TESTING Get-All-Advertisers ------------");
		System.out.println();
		try {
			JozQARequest jozQARequest = new JozQARequest();
			JozQAResponse res = _impl.getQAReport(jozQARequest);

			if(res != null){
				printResponseData(res);
				if(res.getTotalNumFailedRecipes() != 3){
					fail("Get-All-Advertisers Failed: incorrect num of Failed Recipes");
				}else if (res.getTotalNumSuccessRecieps() != 6){
					fail("Get-All-Advertisers Failed: incorrect num of Successful Recipes");	
				}
			} else {
				fail("Get-All-Advertisers failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetOneAdvertisers() {
		System.out.println();
		System.out.println("----------TESTING Get-One-Advertisers ------------");
		System.out.println();
		try {
			JozQARequest jozQARequest = new JozQARequest();
			ArrayList<String> advertisers = new ArrayList<String>();
			advertisers.add("Admin_Adv0");
			jozQARequest.setAdvertisers(advertisers);
			JozQAResponse res = _impl.getQAReport(jozQARequest);

			if(res != null){
				printResponseData(res);
				if(res.getTotalNumFailedRecipes() != 0){
					fail("Get-One-Advertisers Failed: incorrect num of Failed Recipes");
				}else if (res.getTotalNumSuccessRecieps() != 1){
					fail("Get-One-Advertisers Failed: incorrect num of Successful Recipes");
				}
			} else {
				fail("Get-One-Advertisers failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetTwoAdvertisers() {
		System.out.println();
		System.out.println("----------TESTING Get-Two-Advertisers ------------");
		System.out.println();
		try {
			JozQARequest jozQARequest = new JozQARequest();
			ArrayList<String> advertisers = new ArrayList<String>();
			advertisers.add("Admin_Adv0");
			advertisers.add("Admin_Adv1");
			jozQARequest.setAdvertisers(advertisers);
			JozQAResponse res = _impl.getQAReport(jozQARequest);

			if(res != null){
				printResponseData(res);
				if(res.getTotalNumFailedRecipes() != 0){
					fail("Get-Two-Advertisers Failed: incorrect num of Failed Recipes");
				}else if (res.getTotalNumSuccessRecieps() != 2){
					fail("Get-Two-Advertisers Failed: incorrect num of Successful Recipes");
				}
			} else {
				fail("Get-Two-Advertisers failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetOneInvalidAdvertisers() {
		System.out.println();
		System.out.println("----------TESTING Get-One-Invalid-Advertisers ------------");
		System.out.println();
		try {
			JozQARequest jozQARequest = new JozQARequest();
			ArrayList<String> advertisers = new ArrayList<String>();
			advertisers.add("DNE_Advertiser");
			jozQARequest.setAdvertisers(advertisers);
			JozQAResponse res = _impl.getQAReport(jozQARequest);

			if(res != null){
				printResponseData(res);
				if(res.getTotalNumFailedRecipes() != 0){
					fail("Get-All-Advertisers Failed: incorrect num of Failed Recipes");
				}else if (res.getTotalNumSuccessRecieps() != 0){
					fail("Get-All-Advertisers Failed: incorrect num of Successful Recipes");
				}
				ArrayList<String> details = res.getDetails();
				if(details.size()!=1){
					fail("Get-One-Invalid-Advertiser Failed: wrong number of invalid advertisers");
				}
			} else {
				fail("Get-One-Invalid-Advertisers failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetOneWarningAdvertisers() {
		System.out.println();
		System.out.println("----------TESTING Get-One-Warning-Advertiser------------");
		System.out.println();
		try {
			JozQARequest jozQARequest = new JozQARequest();
			ArrayList<String> advertisers = new ArrayList<String>();
			advertisers.add("Admin_Adv7");
			jozQARequest.setAdvertisers(advertisers);
			JozQAResponse res = _impl.getQAReport(jozQARequest);

			if(res != null){
				printResponseData(res);
				if(res.getTotalNumFailedRecipes() != 0){
					fail("Get-One-Warning-Advertiser Failed: incorrect num of Failed Recipes");
				}else if (res.getTotalNumSuccessRecieps() != 0){
					fail("Get-One-Warning-Advertiser Failed: incorrect num of Successful Recipes");
				}
			} else {
				fail("Get-One-Warning-Advertiser failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetOneAdvertisersTwoTSpecs() {
		System.out.println();
		System.out.println("----------TESTING Get-One-Advertiser-Two-TSpecs------------");
		System.out.println();
		try {
			JozQARequest jozQARequest = new JozQARequest();
			ArrayList<String> advertisers = new ArrayList<String>();
			advertisers.add("Admin_Adv8");
			jozQARequest.setAdvertisers(advertisers);
			JozQAResponse res = _impl.getQAReport(jozQARequest);

			if(res != null){
				printResponseData(res);
				if(res.getTotalNumFailedRecipes() != 1){
					fail("Get-One-Warning-Advertiser Failed: incorrect num of Failed Recipes");
				}else if (res.getTotalNumSuccessRecieps() != 0){
					fail("Get-One-Warning-Advertiser Failed: incorrect num of Successful Recipes");
				}
			} else {
				fail("Get-One-Warning-Advertiser failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetOneAdvertiserNotInFlight() {
		System.out.println();
		System.out.println("----------TESTING Get-One-Advertiser-Not-In-Flight------------");
		System.out.println();
		try {
			JozQARequest jozQARequest = new JozQARequest();
			ArrayList<String> advertisers = new ArrayList<String>();
			advertisers.add("Admin_Adv9");
			jozQARequest.setAdvertisers(advertisers);
			JozQAResponse res = _impl.getQAReport(jozQARequest);

			if(res != null){
				printResponseData(res);
				if(res.getTotalNumFailedRecipes() != 1){
					fail("Get-One-Warning-Advertiser Failed: incorrect num of Failed Recipes");
				}else if (res.getTotalNumSuccessRecieps() != 0){
					fail("Get-One-Warning-Advertiser Failed: incorrect num of Successful Recipes");
				}
			} else {
				fail("Get-One-Warning-Advertiser failed: response == null");
				System.out.println("Lookup faild. No result");
			}
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	private void printResponseData(JozQAResponse response) {
		ArrayList<QAAdvertiserResponse> advInfos= response.getAdvertiserInfos();
		System.out.println("All Recipes for All Advertisers Passed = " + response.isSuccess());
		System.out.println("Total Number of Failed Recipes = " + response.getTotalNumFailedRecipes());
		System.out.println("Total Number of Successful Recipes = " + response.getTotalNumSuccessRecieps());
		System.out.println("Details: " + response.getDetails());
		for(QAAdvertiserResponse resp: advInfos){
			System.out.println(" Advertiser Name = " + resp.getAdvertiserName());
			System.out.println(" Complete Success = " + resp.isCompleteSuccess());
			System.out.println(" Num Failed Recipes = " + resp.getNumFailedRecipes());
			System.out.println(" Num Successful Recipes = " + resp.getNumSuccessfulRecipes());
			HashSet<QARecipeResponse> failedRecipeResponses = resp.getFailedRecipeResponses();
			for(QARecipeResponse fRecResp: failedRecipeResponses){
				System.out.println("  Recipe Name = "+ fRecResp.getRecipeName());
				System.out.println("  Recipe Id = "+ fRecResp.getRecipeId());
				System.out.println("  Description = " + fRecResp.getDetailsString());
				HashSet<JozQAError> errors = fRecResp.getJozQAErrors();
				for(JozQAError error: errors){
					System.out.println("   Num Products Recieved = " + error.getNumRecieved());
					System.out.println("   Num Products Requested = " + error.getNumRequested());
					System.out.println("   Camp Name = " + error.getCampName());
					System.out.println("   Camp Id = " + error.getCampId());
					System.out.println("   AdPod Name = " + error.getAdPodName());
					System.out.println("   AdPod Id = " + error.getAdPodId());
					System.out.println("   Recipe Name = " + error.getRecipeName());
					System.out.println("   Recipe Id = " + error.getRecipeId());
					System.out.println("   TSpec Name = " + error.getTSpecName());
					System.out.println("   TSpec Id = " + error.getTSpecId());
					System.out.println("   is Allow External Query = " + error.isAllowExternalQuery());
					System.out.println("   is geo-enabled = " + error.isGoeEnabled());
					System.out.println("   are there included products = " + error.isIncludedProducts());
				}
			}
			System.out.println();
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

}
