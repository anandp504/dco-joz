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
import com.tumri.joz.client.helper.JozTaxonomyDataProvider;
import com.tumri.joz.client.impl.JozDataProviderImpl;
import com.tumri.joz.jozMain.ListingProviderFactory;
import com.tumri.joz.jozMain.JozData;
import com.tumri.joz.server.JozServer;
import com.tumri.joz.server.domain.JozCategory;
import com.tumri.joz.server.domain.JozTaxonomy;
import com.tumri.joz.server.domain.JozTaxonomyRequest;
import com.tumri.joz.server.domain.JozTaxonomyResponse;
import com.tumri.joz.utils.AppProperties;

import com.tumri.lls.server.main.LLSServerException;
import com.tumri.lls.server.main.LLSTcpServer;
import com.tumri.lls.server.utils.LlsAppProperties;
import com.tumri.lls.server.domain.listing.ListingsHelper;
import com.tumri.lls.server.domain.listingformat.ListingFormatHelper;

import com.tumri.utils.Polling;
import com.tumri.utils.tcp.client.TcpSocketConnectionPool;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;


/**
 * @author: nipun
 * Date: Mar 24, 2008
 * Time: 10:09:13 AM
 */
public class TestJozTaxonomyDataProviderImpl extends TestCase {

    private static JozTaxonomyDataProvider _impl = null;
    private static String host = "localhost";
    private static int port = 2544;
    private static int poolSize = 10;
    private static int numRetries = 3;
    private static Thread jozServerThread = null;
    private static JozServer jozServer = null;
    private static JozDataProvider provider= null;
    
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
            provider = new JozDataProviderImpl("localhost", 2544,10,3);
        	           
        } catch (Exception e) {
            System.out.println("exception in initialisation of JozServer");
        }
    }
    @Test
    public void testStart(){
    	init();
    }
    
    @Test
    public void testGetTaxonomyData() {
    	
    	try {
    		
			JozTaxonomyRequest aquery = new JozTaxonomyRequest();
			//aquery.setValue(JozTaxonomyRequest.KEY_CATEGORY, "GLASSVIEW.TUMRI_14111");

			JozTaxonomy tax = provider.getTaxonomy(aquery).getTaxonomy();
			Assert.assertNotNull(tax);
			System.out.println("GlassIdStr "+tax.getRootCategory().getGlassIdStr());
			System.out.println("IdStr "+tax.getRootCategory().getId());
			System.out.println("GlassId "+tax.getRootCategory().getName());
			JozCategory rootCat = tax.getRootCategory();
			List<JozCategory>  childCatList = rootCat.getChildren();
			Iterator itr = childCatList.iterator();
			while(itr.hasNext()){
				inspectCategory((JozCategory)itr.next());
			}
			
		} catch (JoZClientException e) {
			e.printStackTrace();
			fail("Error in testGetTaxonomyData");
		}
		
      
    }
    @Test
    public void testGetCategories() throws JoZClientException {

        try {
        
			JozTaxonomyRequest aquery = new JozTaxonomyRequest();
			aquery.setValue(JozTaxonomyRequest.KEY_CATEGORY, "GLASSVIEW.TUMRI_14111");

            JozTaxonomyRequest taxRequest = new JozTaxonomyRequest();
            taxRequest.setValue(JozTaxonomyRequest.KEY_CATEGORY, "GLASSVIEW.TUMRI_14111");

            JozTaxonomy tax = provider.getTaxonomy(taxRequest).getTaxonomy();
            Assert.assertNotNull(tax);
            JozCategory rootCat = tax.getRootCategory();
            if (rootCat == null) {
                    System.out.println("Error getting Root category");
            }

            int topCatSize = rootCat.getChildren().size();
            System.out.println("GlassIdStr "+ rootCat.getGlassIdStr());
            System.out.println("IdStr "+rootCat.getId());
            System.out.println("GlassId "+rootCat.getName());
            System.out.println("Top level category children: " + topCatSize);
            System.out.println("Root Category prod count: " + rootCat.getCount());

        } catch (Throwable e) {
                System.out.println("Exception occurred in testTaxonomy.");
                fail("Exception occurred in testTaxonomy.");
                e.printStackTrace();
        }

}
    private void inspectCategory(JozCategory childCat){
    	System.out.println("GlassIdStr "+childCat.getGlassIdStr());
    	System.out.println("IdStr "+childCat.getId());
    	System.out.println("GlassId "+childCat.getId());
    	List<JozCategory>  childCatList = childCat.getChildren();
		Iterator itr = childCatList.iterator();
		while(itr.hasNext()){
			inspectCategory((JozCategory)itr.next());
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

    private void inspectResponseData(JozTaxonomyResponse response) {
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