package com.tumri.joz.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.tumri.cma.domain.Campaign;
import com.tumri.joz.campaign.CMAContentPoller;
import com.tumri.joz.client.impl.JozDataProviderImpl;
import com.tumri.joz.jozMain.JozData;
import com.tumri.joz.jozMain.ListingProviderFactory;
import com.tumri.joz.keywordServer.ProductIndex;
import com.tumri.joz.server.JozServer;
import com.tumri.joz.server.domain.JozAdPod;
import com.tumri.joz.server.domain.JozAdvertiser;
import com.tumri.joz.server.domain.JozCampaign;
import com.tumri.joz.server.domain.JozLocation;
import com.tumri.joz.server.domain.JozProvider;
import com.tumri.joz.server.domain.JozRecipe;
import com.tumri.joz.server.domain.JozResponse;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.FSUtils;
import com.tumri.lls.server.domain.listing.ListingsHelper;
import com.tumri.lls.server.domain.listingformat.ListingFormatHelper;
import com.tumri.lls.server.main.LLSServerException;
import com.tumri.lls.server.main.LlsServer;
import com.tumri.lls.server.utils.LlsAppProperties;
import com.tumri.utils.Polling;
import com.tumri.utils.tcp.client.TcpSocketConnectionPool;


public class TestJozICSCampaignProviderImpl extends TestCase {
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
             System.out.println(new Date().getTime());
        	
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
    public void testCampaignGetAllAdvertisers() {
    	try {
			String  response = provider.getAllAdvertisers();
			Assert.assertNotNull(response);
			System.out.println("Response is "+response);
			/*
			System.out.println("Response status is "+response.getStatus());
			ArrayList<JozAdvertiser> advertisers =response.getAdvertisers();
			if(advertisers != null){
				for(JozAdvertiser advertiser:advertisers){
					System.out.println(advertiser.getId());
					System.out.println(advertiser.getName());
					
					XStream xstream = new XStream(new JettisonMappedXmlDriver());
			        System.out.println(" JSON advertiser ---\n"+xstream.toXML(advertiser));
				}
			}
			*/
		} catch (JoZClientException e) {
			fail("testCampaignGetAllAdvertisers failed");
			e.printStackTrace();
		}
    }
    
    @Test
    public void testAdvertiserCampaignData() {
    	try {
    		int advertiserId = -1;
			String  response = provider.getAdvertiserCampaignData(advertiserId);
			Assert.assertNotNull(response);
			System.out.println("Response is "+response);
			/*
			 System.out.println("Response status is "+response.getStatus());
			
	        
			ArrayList<JozCampaign> campaigns =response.getCampaigns();
			if(campaigns != null){
				for(JozCampaign campaign:campaigns){
					System.out.println(campaign.getId());
					System.out.println(campaign.getName());
					System.out.println(campaign.getClientName());
					System.out.println(campaign.getClientId());
					ArrayList<JozAdPod> adpods = campaign.getAdpods();
					if(adpods != null){
						for(JozAdPod adpod:adpods){
							System.out.println(" - "+adpod.getId());
							System.out.println(" - "+adpod.getName());
							System.out.println(" - "+adpod.getAdType());
							ArrayList<JozLocation> locations = adpod.getLocations();
							if(locations != null){
								for(JozLocation location:locations){
									System.out.println(" - - "+location.getClientId());
									System.out.println(" - - "+location.getName());
									System.out.println(" - - "+location.getExternalId());
									System.out.println(" - - "+location.getName());
								}
							}
							ArrayList<JozRecipe> recipes= adpod.getRecipes();
							if(recipes !=  null){
								for(JozRecipe recipe:recipes){
									System.out.println(" - - "+recipe.getId());
									System.out.println(" - - "+recipe.getName());
								}
							}
						}
					}
					XStream xstream = new XStream(new JettisonMappedXmlDriver());
			        System.out.println(" JSON campaign ---\n"+xstream.toXML(campaign));
				}
				
			}
			
			*/
	        
			
		} catch (JoZClientException e) {
			fail("testAdvertiserCampaignData failed");
			e.printStackTrace();
		}
    }
    
    @Test
    public void testAllAdvertisersCampaignData() {
    	try {
			String  response = provider.getAllAdvertisersCampaignData();
			Assert.assertNotNull(response);
			System.out.println("Response is "+response);
			/*
			System.out.println("Response status is "+response.getStatus());
			ArrayList<JozCampaign> campaigns =response.getCampaigns();
			if(campaigns != null){
				for(JozCampaign campaign:campaigns){
					System.out.println(campaign.getId());
					System.out.println(campaign.getName());
					System.out.println(campaign.getClientName());
					System.out.println(campaign.getClientId());
					ArrayList<JozAdPod> adpods = campaign.getAdpods();
					if(adpods != null){
						for(JozAdPod adpod:adpods){
							System.out.println(" - "+adpod.getId());
							System.out.println(" - "+adpod.getName());
							System.out.println(" - "+adpod.getAdType());
							ArrayList<JozLocation> locations = adpod.getLocations();
							if(locations != null){
								for(JozLocation location:locations){
									System.out.println(" - - "+location.getClientId());
									System.out.println(" - - "+location.getName());
									System.out.println(" - - "+location.getExternalId());
									System.out.println(" - - "+location.getName());
								}
							}
							ArrayList<JozRecipe> recipes= adpod.getRecipes();
							if(recipes !=  null){
								for(JozRecipe recipe:recipes){
									System.out.println(" - - "+recipe.getId());
									System.out.println(" - - "+recipe.getName());
								}
							}
						}
					}
					XStream xstream = new XStream(new JettisonMappedXmlDriver());
			        System.out.println(" JSON campaign ---\n"+xstream.toXML(campaign));
				}
			}
			*/
			
		} catch (JoZClientException e) {
			fail("testAdvertiserCampaignData failed");
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
