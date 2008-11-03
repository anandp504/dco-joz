package com.tumri.joz.client;

import java.util.HashMap;
import java.util.Iterator;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import com.tumri.cma.domain.*;
import com.tumri.joz.campaign.CMAContentPoller;
import com.tumri.joz.client.impl.JozDataProviderImpl;
import com.tumri.joz.jozMain.JozData;
import com.tumri.joz.jozMain.ListingProviderFactory;
import com.tumri.joz.server.JozServer;
import com.tumri.joz.server.domain.JozAdResponse;
import com.tumri.joz.server.domain.JozCounts;
import com.tumri.joz.server.domain.JozResponse;
import com.tumri.joz.utils.AppProperties;
import com.tumri.lls.server.domain.listing.ListingsHelper;
import com.tumri.lls.server.domain.listingformat.ListingFormatHelper;
import com.tumri.lls.server.main.LLSServerException;
import com.tumri.lls.server.main.LLSTcpServer;
import com.tumri.lls.server.utils.LlsAppProperties;
import com.tumri.utils.Polling;
import com.tumri.utils.tcp.client.TcpSocketConnectionPool;


public class TestJozTSpecProviderImpl extends TestCase {
    private static String host = "localhost";
    private static int numRetries = 3;
    private static Thread jozServerThread = null;
    private static JozServer jozServer = null;
    private static JozDataProvider provider = null;
    private static Thread llsServerThread = null;
    private static LLSTcpServer llsServer = null;
    //@BeforeClass
    public static void init() {
        
        try {
//      	
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
            provider = new JozDataProviderImpl(host, port,poolSize,numRetries);
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
    public void testTSpecGetDetailsByTSpec() {
    	try {
			TSpec tSpec = new TSpec();
			tSpec.setName("TestTSpec");
			tSpec.addIncludedCategories(new CategoryInfo("GLASSVIEW.TUMRI_14240","Women\'s Tops"));
			//tSpec.addIncludedCategories(new CategoryInfo("GLASSVIEW.TUMRI_14198",""));
			//tSpec.addIncludedKeywords(new KeywordInfo("Motorola Nokia Blackberry Apple LG Samsung"));
			//tSpec.addIncludedCategories(new CategoryInfo("GLASSVIEW.TUMRI_14361",""));
			int pageSize = 12;
			int pageNum = 2;
			JozResponse  response = provider.getTSpecDetails(tSpec,pageSize,pageNum);
			JozAdResponse adResponse = response.getAdResponse();
            inspectResponseData(adResponse);
		} catch (JoZClientException e) {
			fail("testTSpecGetDetailsByTSpec FAILED");
			e.printStackTrace();
		}
    }@Test
    public void testTSpecIncludedProducts() {
    	try {
			TSpec tSpec = new TSpec();
			tSpec.setName("TestTSpec");
			//tSpec.addIncludedCategories(new CategoryInfo("GLASSVIEW.TUMRI_14240","Women\'s Tops"));
			//tSpec.addIncludedCategories(new CategoryInfo("GLASSVIEW.TUMRI_14198",""));
			//tSpec.addIncludedKeywords(new KeywordInfo("Motorola Nokia Blackberry Apple LG Samsung"));
			//tSpec.addIncludedCategories(new CategoryInfo("GLASSVIEW.TUMRI_14361",""));		
			tSpec.addIncludedProducts(new ProductInfo("_1305.US3781596"));
			int pageSize = 12;
			int pageNum = 0;
			JozResponse  response = provider.getTSpecDetails(tSpec,pageSize,pageNum);
			JozAdResponse adResponse = response.getAdResponse();
            inspectResponseData(adResponse);
		} catch (JoZClientException e) {
			fail("testTSpecGetDetailsByTSpec FAILED");
			e.printStackTrace();
		}
    }
    
    @Test
    public void testTSpecGetDetailsByTSpecWithPagination() {
    	try {
			TSpec tSpec = new TSpec();
			tSpec.setName("TestTSpec");
			tSpec.addIncludedCategories(new CategoryInfo("GLASSVIEW.TUMRI_14240","Women\'s Tops"));
			//tSpec.addIncludedCategories(new CategoryInfo("GLASSVIEW.TUMRI_14198",""));
			//tSpec.addIncludedKeywords(new KeywordInfo("Motorola Nokia Blackberry Apple LG Samsung"));
			//tSpec.addIncludedCategories(new CategoryInfo("GLASSVIEW.TUMRI_14361",""));
			int pageSize = 12;
			int pageNum = 0;
			JozResponse  response = provider.getTSpecDetails(tSpec,pageSize,pageNum);
			Assert.assertNotNull(response);
			JozAdResponse adResponse = response.getAdResponse();
            inspectResponseData(adResponse);
            System.out.println("Get the next page - with getTSpecDetails");
    		response = provider.getTSpecDetails(tSpec, 12, 1);
    		System.out.println("Next page Response status = " + response.getStatus());
    		adResponse = response.getAdResponse();
    		inspectResponseData(adResponse);
		} catch (JoZClientException e) {
			fail("testTSpecGetDetailsByTSpec FAILED");
			e.printStackTrace();
		}
    }
    @Test
    public void testTSpecGetDetailsCountsByTSpec() {
    	try {
			TSpec tSpec = new TSpec();
			tSpec.setName("TestTSpec");
			tSpec.addIncludedCategories(new CategoryInfo("GLASSVIEW.TUMRI_14240","Women\'s Tops"));
			//tSpec.addIncludedCategories(new CategoryInfo("GLASSVIEW.TUMRI_14198",""));
			//tSpec.addIncludedKeywords(new KeywordInfo("Motorola Nokia Blackberry Apple LG Samsung"));
			//tSpec.addIncludedCategories(new CategoryInfo("GLASSVIEW.TUMRI_14361",""));
			int pageSize = 12;
			int pageNum = 3;
			JozResponse  response = provider.getTSpecDetailsAndCounts(tSpec,pageSize,pageNum);
			Assert.assertNotNull(response);
			JozAdResponse adResponse = response.getAdResponse();
            inspectResponseData(adResponse);
            JozCounts counts = response.getCounts();
			System.out.println("Num Categories : "+counts.getCategory().size());
			System.out.println("Num Brands : "+counts.getBrand().size());
			System.out.println("Num Providers : "+counts.getProvider().size());
		} catch (JoZClientException e) {
			fail("testTSpecGetDetailsCountsByTSpec FAILED");
			e.printStackTrace();
		}
    }
    @Test
    public void testTSpecExcludedCategoriesGetDetailsCountsByTSpec() {
    	try {
			TSpec tSpec = new TSpec();
			tSpec.setName("TestTSpec");
			tSpec.addIncludedCategories(new CategoryInfo("GLASSVIEW.TUMRI_14206","GLASSVIEW.TUMRI_14206"));
			tSpec.addExcludedCategories(new CategoryInfo("GLASSVIEW.TUMRI_14148","GLASSVIEW.TUMRI_14148"));
            //Note: If the name of the excluded category is set to NULL, then we will get an exception.
            tSpec.addIncludedMerchant(new MerchantInfo("GAP"));
			tSpec.addExcludedMerchant(new MerchantInfo("Lenovo"));
			tSpec.addIncludedProviders(new ProviderInfo("GAP"));
			int pageSize = 12;
			int pageNum = 3;
			JozResponse  response = provider.getTSpecDetailsAndCounts(tSpec,pageSize,pageNum);
			Assert.assertNotNull(response);
			JozAdResponse adResponse = response.getAdResponse();
            inspectResponseData(adResponse);
            JozCounts counts = response.getCounts();
			System.out.println("Num Categories : "+counts.getCategory().size());
			System.out.println("Num Brands : "+counts.getBrand().size());
			System.out.println("Num Providers : "+counts.getProvider().size());
		} catch (JoZClientException e) {
			fail("testTSpecExcludedCategoriesGetDetailsCountsByTSpec FAILED");
			e.printStackTrace();
		}
    }
    private void inspectResponseData(JozAdResponse response) {
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
    
    //@Test
    public void XtestTSpecGetDetailsByTSpecById() {
    	try {
			int tSpecId = 12286;
			int pageSize = 12;
			int pageNum = 0;
			JozResponse  response = provider.getTSpecDetails(tSpecId,pageSize,pageNum);
			Assert.assertNotNull(response);
			JozAdResponse adResponse = response.getAdResponse();
            inspectResponseData(adResponse);
		} catch (JoZClientException e) {
			fail("testTSpecGetDetailsByTSpecById FAILED");
			e.printStackTrace();
		}
    }
    
    @Test
    public void XtestTSpecGetCountsByTSpec() {
    	try {
			TSpec tSpec = new TSpec();
			tSpec.setName("TestTSpec");
			tSpec.addIncludedCategories(new CategoryInfo("GLASSVIEW.TUMRI_14240","Women\'s Tops"));
			JozResponse  response = provider.getTSpecCounts(tSpec);
			Assert.assertNotNull(response);
			JozCounts counts = response.getCounts();
			System.out.println("Num Categories : "+counts.getCategory().size());
			System.out.println("Num Brands : "+counts.getBrand().size());
			System.out.println("Num Providers : "+counts.getProvider().size());
		} catch (JoZClientException e) {
			fail("testTSpecGetCountsByTSpec FAILED");
			e.printStackTrace();
		}
    }
    
    //@Test
    public void XtestTSpecGetCountsByTSpecId() {
    	try {
			int tSpecId = 0;
			JozResponse  response = provider.getTSpecCounts(tSpecId);
			Assert.assertNotNull(response);
			JozCounts counts = response.getCounts();
			System.out.println("Num Categories : "+counts.getCategory().size());
			System.out.println("Num Brands : "+counts.getBrand().size());
			System.out.println("Num Providers : "+counts.getProvider().size());
		} catch (JoZClientException e) {
			fail("testTSpecGetCountsByTSpecId FAILED");
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
