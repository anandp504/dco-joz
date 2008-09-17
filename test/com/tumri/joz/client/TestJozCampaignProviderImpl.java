package com.tumri.joz.client;

import com.tumri.cma.domain.*;
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
import com.tumri.lls.server.main.LlsServer;
import com.tumri.lls.server.utils.LlsAppProperties;
import com.tumri.utils.Polling;
import com.tumri.utils.tcp.client.TcpSocketConnectionPool;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;


public class TestJozCampaignProviderImpl extends TestCase {
    private static String host = "localhost";
    private static int numRetries = 3;
    private static Thread jozServerThread = null;
    private static JozServer jozServer = null;
    private static  JozDataProvider provider = null;
    private static Thread llsServerThread = null;
    private static LlsServer llsServer = null;
   // @BeforeClass
    public static void init() {
        
        try {
             System.out.println("Starting Lls");
        	
        	LlsAppProperties.getInstance("lls.properties");
            try {
                ListingsHelper.init();
                ListingFormatHelper.refreshListingHeaders(false);
            } catch (LLSServerException e) {
                System.out.println("Error initializing the LLS server");
                System.exit(1);
            }
            llsServer = new LlsServer();
            llsServerThread = new Thread("LLSServerThread") {
                public void run() {
                	llsServer.runServer();
                }
            };
            llsServerThread.start();

        	System.out.println("Starting Joz");
            JozData.init();

            int poolSize = Integer.parseInt(AppProperties.getInstance().getProperty("tcpServer.poolSize"));
            int port = Integer.parseInt(AppProperties.getInstance().getProperty("tcpServer.port"));
            int timeout= Integer.parseInt(AppProperties.getInstance().getProperty("tcpServer.timeout"));
            String queryHandlers = AppProperties.getInstance().getProperty("tcpServer.queryHandlers");

            jozServer = new JozServer(poolSize,port,timeout,queryHandlers);
            jozServerThread = new Thread("JoZServerThread") {
                public void run() {
                	jozServer.runServer();
                }
            };
            jozServerThread.start();  
           provider = new JozDataProviderImpl(host, port,poolSize,numRetries);
        } catch (Exception e) {
            System.out.println("exception in initialisation of JozServer");
        }
    }
    @Test
    public void testStart(){
    	init();
    }
    @Test
    public void testCampaignAdd() {
    	try {
			
			Campaign campaign = new Campaign();
			campaign.setName("TestCampaign");
		    campaign.setId(12345);
		    campaign.setClientId(54321);


		    AdPod adPod = new AdPod();
		    adPod.setId(23456);
		    adPod.setName("scott_adpod");

		    ArrayList<Location> locs = new ArrayList<Location>();
		    Location loc = new Location();
		    loc.setClientId(54321);
		    loc.setId(98765);
		    loc.setName("scott_loc");


		    locs.add(loc);
		    adPod.setLocations(locs);

		    Recipe recipe = new Recipe();
		    recipe.setAdpodId(23456);

		    RecipeTSpecInfo info = new RecipeTSpecInfo();
		    info.setId(87654);
		    info.setTspecId(77777);

		    recipe.addTSpecInfo(info);
		    recipe.setName("scott_recipe");

		    adPod.addRecipe(recipe);

		    OSpec oSpec = new OSpec();
		    oSpec.setId(55555);
		    oSpec.setName("scott_ospec");

		    TSpec tSpec = new TSpec();
		    tSpec.setId(77777);
		    tSpec.setName("scott_tspec");

		    oSpec.addTSpec(tSpec);
		    adPod.setOspec(oSpec);

		    campaign.addAdpod(adPod);


			JozResponse  response = provider.addCampaign(campaign);
			Assert.assertNotNull(response);
			System.out.println("Response status is "+response.getStatus());
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			fail("test campaignAdd failed");
			e.printStackTrace();
		}
    }
    @Test
    public void testCampaignDelete() {
    	try {
			Campaign campaign = new Campaign();
			campaign.setName("TestCampaign");
		    campaign.setId(12345);
		    campaign.setClientId(54321);


		    AdPod adPod = new AdPod();
		    adPod.setId(23456);
		    adPod.setName("scott_adpod");

		    ArrayList<Location> locs = new ArrayList<Location>();
		    Location loc = new Location();
		    loc.setClientId(54321);
		    loc.setId(98765);
		    loc.setName("scott_loc");


		    locs.add(loc);
		    adPod.setLocations(locs);

		    Recipe recipe = new Recipe();
		    recipe.setAdpodId(23456);

		    RecipeTSpecInfo info = new RecipeTSpecInfo();
		    info.setId(87654);
		    info.setTspecId(77777);

		    recipe.addTSpecInfo(info);
		    recipe.setName("scott_recipe");

		    adPod.addRecipe(recipe);

		    OSpec oSpec = new OSpec();
		    oSpec.setId(55555);
		    oSpec.setName("scott_ospec");

		    TSpec tSpec = new TSpec();
		    tSpec.setId(77777);
		    tSpec.setName("scott_tspec");

		    oSpec.addTSpec(tSpec);
		    adPod.setOspec(oSpec);

		    campaign.addAdpod(adPod);
			JozResponse  response = provider.deleteCampaign(campaign);
			Assert.assertNotNull(response);
			System.out.println("Response status is "+response.getStatus());
		} catch (JoZClientException e) {
			fail("test campaignDelete failed");
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
}
