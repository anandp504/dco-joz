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

import com.thoughtworks.xstream.XStream;
import com.tumri.cma.domain.AdPod;
import com.tumri.cma.domain.Campaign;
import com.tumri.cma.domain.GeoAdPodMapping;
import com.tumri.cma.domain.Location;
import com.tumri.cma.domain.UrlAdPodMapping;
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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import junit.framework.TestCase;

/**
 * @author: raghu
 * Date: Aug 19, 2008
 * Time: 05:22:22 PM
 */
public class TestJozAdDataFromCampaignXML extends TestCase{

    private static JozDataProviderImpl _impl = null;
    private static String host = "localhost";
    private static int numRetries = 3;
    private static Thread jozServerThread = null;
    private static JozServer jozServer = null;
    private static Thread llsServerThread = null;
    private static LLSTcpServer llsServer = null;
    private List<HashMap> testCaseList = null;
    private HashMap adpodMap = null;
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
    	testCaseList = new ArrayList<HashMap>();
    	adpodMap = new HashMap();
    	loadCampaignXML();    	
    	for(HashMap testMap:testCaseList){
    		runTest(testMap);
    	}
    }
    public void runTest(HashMap testMap) {
        try {       	
			JozAdRequest aquery = new JozAdRequest();
			String type = (String)testMap.get("type");
			
			Set keys = testMap.keySet();
			Iterator kItr = keys.iterator();
			while(kItr.hasNext()){
				String key = (String)kItr.next();
				String value = (String)testMap.get(key);
				if(!key.equalsIgnoreCase("type")){
					aquery.setValue(key,value);
				}			
			}
			System.out.println("Running test "+type+" for data "+testMap.toString());
			JozAdResponse res = _impl.getAdData(aquery);
			assert(res != null);
			AdPod adpodFromJoz = inspectResponseData(res);
			if(adpodFromJoz != null){
				if(compareQueryAndResult(adpodFromJoz,testMap)){
					System.out.println("SUCCESS - Match found in result ####");
				}else{
					System.out.println("ERROR - Wrong adpods for given query set !!!!");
				}
			}else{
				System.out.println(" ERROR - No Adpod chosen !!!!");
			}
			
		} catch (JoZClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    private boolean compareQueryAndResult(AdPod adpodFromJoz,HashMap testMap){
    	boolean isValid = false;
    	Set keys = testMap.keySet();
		Iterator kItr = keys.iterator();
		while(kItr.hasNext()){
			String key = (String)kItr.next();
			String value = (String)testMap.get(key);
						
			if(key.equalsIgnoreCase("type") || key.equalsIgnoreCase(JozAdRequest.KEY_NUM_PRODUCTS) ){
                //String valueFromAdpod = checkAdPod(adpodFromJoz,value);
                
			}else{
				System.out.println("Value to check -> "+value);
				String valueFromAdpod = checkAdPod(adpodFromJoz,value);
				if(valueFromAdpod != null && value.equalsIgnoreCase(valueFromAdpod)){
					System.out.println("Matching value found - " +value+" in - "+valueFromAdpod);
					isValid = true;
					break;
				}
			}			
		}		
    	return isValid;
    }
    private String checkAdPod(AdPod adpod, String value){
    	String valueFound = null;
    	List<Location> locations = adpod.getLocations();
		if(locations != null){   				
			for(Location location:locations){	    					
                String locationTarget = ""+location.getExternalId();
                if(value.equalsIgnoreCase(locationTarget))
					return locationTarget;
                
                List<UrlAdPodMapping> urladpodMaps = adpod.getAdpodUrls();
    			if(urladpodMaps != null){
    				for(UrlAdPodMapping urladpodMap:urladpodMaps){
                        //add URL location test case
    					String url = urladpodMap.getName();
    					if(value.equalsIgnoreCase(url))
    						return url;
                        // add URL theme test case
                        String locationName =location.getName();
                        if(value.equalsIgnoreCase(locationName))
    						return locationName;
    				}
    			}
    			List<GeoAdPodMapping> geoAdpods = adpod.getGeoAdPodMappings();
    			if(geoAdpods != null){
    				for(GeoAdPodMapping geoAdPodMapping:geoAdpods){
    					
    					String codeType = geoAdPodMapping.getType();
    					if(codeType.equalsIgnoreCase("Zipcode")){
    						List<String> values = geoAdPodMapping.getGeoValue();
    						for(String value1:values){
    							StringTokenizer tokenizer = new StringTokenizer(value1,",");
    							while(tokenizer.hasMoreTokens()){
    								String token = tokenizer.nextToken();
    								if(value.equalsIgnoreCase(token))
    		    						return token;
                					// geo location test case 
    							}
    						}
    					}
    				}			
    			}   
            }
		}
		return value;

    }
    public void loadCampaignXML(){
    	String campaignFileName = "./test/data/cma/storebuilder/campaigns.xml";
    	File campaignFile = new File(campaignFileName);
    	List<Campaign> campaigns = loadCampaignsFromFile(campaignFile.getAbsolutePath());
    	if(campaigns.size() <= 0){
    		fail("No campaigns present, hence aborting");
    	}
    	for(Campaign campaign:campaigns){
    		HashMap locationMap = null;
    		HashMap url_LocationMap = null; 
    		HashMap url_ThemeMap= null; 
    		
    		HashMap geo_LocationMap= null; 
    		/*HashMap Geo_ThemeMap= null;
    		*/
    		HashMap URL_Geo_LocationMap= null;

    		HashMap map = null;
    		List<AdPod> adpodList = campaign.getAdpods();
    		for(AdPod adpod:adpodList){
    			System.out.println("AdPod  - "+adpod.getId());
    			adpodMap.put(adpod.getId(), adpod);
    			//map = new HashMap();
    			String adType = adpod.getAdType();
    			//
    			List<Location> locations = adpod.getLocations();
    			if(locations != null){   				
    				for(Location location:locations){	    					
                        String locationTarget = ""+location.getExternalId();
                        List<UrlAdPodMapping> urladpodMaps = adpod.getAdpodUrls();
            			if(urladpodMaps != null){
            				for(UrlAdPodMapping urladpodMap:urladpodMaps){
                                //add URL location test case
            					String url = urladpodMap.getName();
            					url_LocationMap = new HashMap();
            					url_LocationMap.put("type","URL_Location_Targeting");
            					url_LocationMap.put(JozAdRequest.KEY_URL, url);
                                url_LocationMap.put(JozAdRequest.KEY_AD_TYPE, adType);
                                url_LocationMap.put(JozAdRequest.KEY_STORE_ID, locationTarget);
                                url_LocationMap.put(JozAdRequest.KEY_NUM_PRODUCTS, "12");

                                testCaseList.add(url_LocationMap);
                                
                                // add URL theme test case
                                String locationName =location.getName();
                                url_ThemeMap = new HashMap();
                                url_ThemeMap.put("type","URL_Theme_Targeting");
                                url_ThemeMap.put(JozAdRequest.KEY_URL, url);
                                url_ThemeMap.put(JozAdRequest.KEY_AD_TYPE, adType);
                                url_ThemeMap.put(JozAdRequest.KEY_THEME, locationName);
                                url_ThemeMap.put(JozAdRequest.KEY_NUM_PRODUCTS, "12");                               
                                testCaseList.add(url_ThemeMap);
            				}
            			}
            			List<GeoAdPodMapping> geoAdpods = adpod.getGeoAdPodMappings();
            			if(geoAdpods != null){
            				for(GeoAdPodMapping geoAdPodMapping:geoAdpods){
            					
            					String codeType = geoAdPodMapping.getType();
            					if(codeType.equalsIgnoreCase("Zipcode")){
            						List<String> values = geoAdPodMapping.getGeoValue();
            						for(String value:values){
            							StringTokenizer tokenizer = new StringTokenizer(value,",");
            							while(tokenizer.hasMoreTokens()){
            								String token = tokenizer.nextToken();;
                        					// geo location test case
                        					geo_LocationMap = new HashMap();
                        					geo_LocationMap.put("type","Geo_Location_Targeting");
                        					geo_LocationMap.put(JozAdRequest.KEY_STORE_ID, locationTarget);
            								geo_LocationMap.put(JozAdRequest.KEY_ZIP_CODE,token);
            								geo_LocationMap.put(JozAdRequest.KEY_NUM_PRODUCTS, "12");
            		                        testCaseList.add(geo_LocationMap); 
            							}
            						}
            					}
            				}			
            			}
                        // add location test case
            			locationMap = new HashMap();
                        locationMap.put("type","Location_Targeting");
                        locationMap.put(JozAdRequest.KEY_AD_TYPE, adType);
                        locationMap.put(JozAdRequest.KEY_STORE_ID, locationTarget);
                        locationMap.put(JozAdRequest.KEY_NUM_PRODUCTS, "12");
                        testCaseList.add(locationMap);   
                    }
    			}
    		}
    	}
    }
    private List<Campaign> loadCampaignsFromFile(String campaignFileName){
		FileReader fr = null;
		List<Campaign> campaigns=null;
		StringBuffer strBuf = new StringBuffer();
		try {
			fr = new FileReader(campaignFileName);
			BufferedReader br =new BufferedReader(fr);
			String line = br.readLine();
		      while (line != null) {
		          strBuf.append(line);
		          line = br.readLine();			         
		      }
		      br.close();
			  
			  //unmarshall
		      XStream xstream = new XStream();			      
			  xstream.processAnnotations(java.util.List.class);
		      xstream.processAnnotations(Campaign.class);
			  campaigns =(List)xstream.fromXML(strBuf.toString());				  
		}
		catch (FileNotFoundException e) {
			System.out.println("Exception caught when processing tspec file : " + campaignFileName);
		}
		catch (IOException e) {
			System.out.println("Exception caught when processing tspec file : " + campaignFileName);
		}
		
		finally {
			try {
				fr.close();
			}
			catch (IOException e) {
				System.out.println("Exception caught when closing the file");
			}
		}
		return campaigns;
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

    private AdPod inspectResponseData(JozAdResponse response) {
        HashMap<String, String> resultMap = response.getResultMap();
        Iterator<String> resultIter = resultMap.keySet().iterator();
        int adpodId = -1;
        while(resultIter.hasNext()){
            String resultKey = resultIter.next();
            String resultVal = resultMap.get(resultKey);
            if(resultKey.equalsIgnoreCase("ADPOD-ID"))
            	adpodId = Integer.parseInt(resultVal);
            
            System.out.print(resultKey + "= ");
            System.out.print(resultVal);
            System.out.println("\n");
        }
        System.out.print("######## AdPod id chosen is "+adpodId);
        AdPod chosenAdpod = (AdPod)adpodMap.get(adpodId);
        return chosenAdpod;
    }
}
