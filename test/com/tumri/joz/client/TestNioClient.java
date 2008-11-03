/*
 * ListingsQueryHandler.java
 *
 * COPYRIGHT (C)  TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (@tumri.com)
 * @version 1.0     
 *
 */
package com.tumri.joz.client;

import com.tumri.utils.tcp.server.TCPServerException;
import com.tumri.utils.nio.client.NioClient;
import com.tumri.utils.nio.client.NioResponseHandler;
import com.tumri.joz.server.domain.*;

import java.util.HashMap;
import java.io.IOException;

/**
 * @author: nipun
 * Date: Sep 29, 2008
 * Time: 7:51:16 PM
 */
public class TestNioClient {

    public static void main(String[] args) {
        try {
            //Init the client
            NioClient.getInstance().init("localhost", 2544, 20, 3);

            for (int i=0;i<20;i++){
                try {
                    if (doRequest()) {
                        System.out.println("NIO request succeeded");
                    }
                } catch (TCPServerException e) {
                    System.out.println("NIO request failed");
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    System.out.println("NIO request failed");
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            //Stop the client
             NioClient.getInstance().destroy();
        } catch (TCPServerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println("Done ");

    }


    private static boolean doRequest() throws TCPServerException, IOException{
        NioResponseHandler handler = new NioResponseHandler();
        JozHCRequest request = new JozHCRequest();

        NioClient.getInstance().send(request, handler);
        JozHCResponse hcresponse  = (JozHCResponse) handler.waitForResponse();

        //Inspect response
        HashMap<String, String> resultMap = hcresponse.getResultMap();
        if (resultMap!=null && !resultMap.isEmpty()) {
            return true;
        } else {
            return false;
        }

    }

    private static boolean doAdDataRequest() throws TCPServerException, IOException{
        NioResponseHandler handler = new NioResponseHandler();
        JozAdRequest adRequest = new JozAdRequest();
        adRequest.setValue(JozAdRequest.KEY_RECIPE_ID, "10000");
        adRequest.setValue(JozAdRequest.KEY_AD_TYPE, "skyscraper");
        NioClient.getInstance().send(adRequest, handler);
        JozAdResponse hcresponse  = (JozAdResponse) handler.waitForResponse();

        //Inspect response
        HashMap<String, String> resultMap = hcresponse.getResultMap();
        if (resultMap!=null && !resultMap.isEmpty()) {
            System.out.println(resultMap.get(JozAdResponse.KEY_PRODUCTS));
            return true;
        } else {
            return false;
        }

    }

    private static boolean doGetAdvertiserRequest() throws TCPServerException, IOException{
        NioResponseHandler handler = new NioResponseHandler();
        JozICSCampaignRequest campaignQuery = new JozICSCampaignRequest();
        campaignQuery.setValue(JozICSCampaignRequest.KEY_COMMAND, JozICSCampaignRequest.COMMAND_GET_ALL_ADVERTISERS);
        NioClient.getInstance().send(campaignQuery, handler);
        JozICSCampaignResponse hcresponse  = (JozICSCampaignResponse) handler.waitForResponse();

        //Inspect response
        HashMap<String, String> resultMap = hcresponse.getResultMap();
        if (resultMap!=null && !resultMap.isEmpty()) {
            System.out.println(resultMap.get(JozICSCampaignResponse.KEY_CAMPAIGN));
            return true;
        } else {
            return false;
        }

    }


}
