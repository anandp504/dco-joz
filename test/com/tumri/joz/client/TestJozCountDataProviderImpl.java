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
import com.tumri.joz.server.domain.JozCountRequest;
import com.tumri.joz.server.domain.JozCountResponse;
import com.tumri.joz.server.domain.JozCounts;
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
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;


/**
 * @author: nipun
 * Date: Mar 24, 2008
 * Time: 10:09:13 AM
 */
public class TestJozCountDataProviderImpl extends TestCase{

	private static JozDataProvider provider = null;
    private static String host = "localhost";
    private static int numRetries = 3;

    private static Thread jozServerThread = null;
    private static JozServer jozServer = null;
    private static Thread llsServerThread = null;
    private static LlsServer llsServer = null;
    @BeforeClass
    public static void init() {
        
        try {
        	//      	
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
    public void testGetCountsData() {
    	try {
    		init();
			JozCountRequest aquery = new JozCountRequest();
			
			//aquery.setValue(JozCountRequest.KEY_TSPEC, "T-SPEC-http://default-realm/");
			aquery.setValue(JozCountRequest.KEY_TSPEC, "admin_custom");
			JozCounts counts = provider.getCountData(aquery).getCounts();
			Assert.assertNotNull(counts);
			System.out.println("Num Categories : "+counts.getCategory().size());
			System.out.println("Num Brands : "+counts.getBrand().size());
			System.out.println("Num Providers : "+counts.getProvider().size());
		} catch (JoZClientException e) {
			fail("Error in testGetCountsData");
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

    private void inspectResponseData(JozCountResponse response) {
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