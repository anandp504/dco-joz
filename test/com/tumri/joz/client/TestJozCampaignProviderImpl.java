package com.tumri.joz.client;

import com.tumri.cma.domain.*;
import com.tumri.cma.persistence.xml.CampaignXMLDateConverter;
import com.tumri.joz.campaign.CMAContentPoller;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.client.impl.JozDataProviderImpl;
import com.tumri.joz.jozMain.JozData;
import com.tumri.joz.jozMain.ListingProviderFactory;
import com.tumri.joz.server.JozServer;
import com.tumri.joz.server.domain.JozResponse;
import com.tumri.joz.utils.AppProperties;
import com.tumri.lls.server.domain.listing.ListingsHelper;
import com.tumri.lls.server.domain.listingformat.ListingFormatHelper;
import com.tumri.lls.server.main.LLSServerException;
import com.tumri.lls.server.main.LlsServer;
import com.tumri.lls.server.utils.LlsAppProperties;
import com.tumri.utils.Polling;
import com.tumri.utils.tcp.client.TcpSocketConnectionPool;
import com.thoughtworks.xstream.XStream;
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
			int numInitCamps = CampaignDB.getInstance().getCampaigns().size();
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
		    int numFinalCamps = CampaignDB.getInstance().getCampaigns().size();
		    Assert.assertEquals(numInitCamps + 1, numFinalCamps);
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			fail("test campaignAdd failed");
			e.printStackTrace();
		}
    }
    @Test
    public void testCampaignDelete() {
    	try {
		    int numInitCamps = CampaignDB.getInstance().getCampaigns().size();
			Campaign campaign = new Campaign();
			campaign.setName("TestCampaign");
		    campaign.setId(12345);

			JozResponse  response = provider.deleteCampaign(campaign);
			Assert.assertNotNull(response);
		    int numFinalCamps = CampaignDB.getInstance().getCampaigns().size();
		    Assert.assertEquals(numInitCamps - 1, numFinalCamps);
		    if(!"success".equalsIgnoreCase(response.getStatus())){
			    fail("test campaignDelete failed");
		    }

			System.out.println("Response status is "+response.getStatus());
		} catch (JoZClientException e) {
			fail("test campaignDelete failed");
			e.printStackTrace();
		}
    }

	@Test
    public void testCampaignDeleteFail() {
		JozResponse  response = null;
	    try {
		    int numInitCamps = CampaignDB.getInstance().getCampaigns().size();
			Campaign campaign = new Campaign();
			campaign.setName("TestCampaign");
		    campaign.setId(12345);

			 response = provider.deleteCampaign(campaign);
		    int numFinalCamps = CampaignDB.getInstance().getCampaigns().size();
		    Assert.assertEquals(numInitCamps, numFinalCamps);
			Assert.assertNull(response);

		} catch (JoZClientException e) {
		    if(response != null){
		        System.out.println("Response status is "+response.getStatus());
		    }
			fail("test campaignDelete failed");
			e.printStackTrace();
		}
    }

	@Test
    public void testCampaignAddNumerous() {
    	try {
		    int numInitCamps = CampaignDB.getInstance().getCampaigns().size();
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


		    Campaign campaign2 = new Campaign();
			campaign2.setName("TestCampaign");
		    campaign2.setId(12345);
		    campaign2.setClientId(98765);


		    AdPod adPod2 = new AdPod();
		    adPod2.setId(98765);
		    adPod2.setName("scott_adpod");

		    ArrayList<Location> locs2 = new ArrayList<Location>();
		    Location loc2 = new Location();
		    loc2.setClientId(98765);
		    loc2.setId(98765);
		    loc2.setName("scott_loc");


		    locs2.add(loc2);
		    adPod2.setLocations(locs2);

		    Recipe recipe2 = new Recipe();
		    recipe2.setAdpodId(98765);

		    RecipeTSpecInfo info2 = new RecipeTSpecInfo();
		    info2.setId(98765);
		    info2.setTspecId(98765);

		    recipe2.addTSpecInfo(info2);
		    recipe2.setName("scott_recipe");

		    adPod2.addRecipe(recipe2);

		    OSpec ospec2 = new OSpec();
		    ospec2.setId(98765);
		    ospec2.setName("scott_ospec");

		    TSpec tspec2 = new TSpec();
		    tspec2.setId(98765);
		    tspec2.setName("scott_tspec");

		    ospec2.addTSpec(tspec2);
		    adPod2.setOspec(ospec2);

		    campaign2.addAdpod(adPod2);

			JozResponse  response = provider.addCampaign(campaign);
			Assert.assertNotNull(response);
		    int numCamps2 = CampaignDB.getInstance().getCampaigns().size();
		    Assert.assertEquals(numInitCamps + 1, numCamps2);

		    JozResponse response2 = provider.addCampaign(campaign2);
		    Assert.assertNotNull(response2);
		    int numCamps3 = CampaignDB.getInstance().getCampaigns().size();
		    Assert.assertEquals(numInitCamps + 1, numCamps3);
			//verify that second set of information is present
		    Campaign testCamp = CampaignDB.getInstance().getCampaign(12345);
		    XStream xstream = new XStream();
			xstream.processAnnotations(java.util.List.class);
			xstream.processAnnotations(Campaign.class);
			xstream.registerConverter(new CampaignXMLDateConverter());
		    String xml1 = xstream.toXML(campaign2);
		    String xml2 = xstream.toXML(testCamp);
		    Assert.assertEquals(xml1, xml2);

		    JozResponse  response3 = provider.deleteCampaign(campaign);
			Assert.assertNotNull(response3);
		    int numCamps4 = CampaignDB.getInstance().getCampaigns().size();
		    Assert.assertEquals(numInitCamps, numCamps4);

		    JozResponse  response4 = provider.deleteCampaign(campaign2);
		    int numCamps5 = CampaignDB.getInstance().getCampaigns().size();
		    Assert.assertEquals(numInitCamps, numCamps5);
			Assert.assertNull(response4);

		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			fail("test campaignAddNumerous failed");
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
