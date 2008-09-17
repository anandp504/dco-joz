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

import com.thoughtworks.xstream.XStream;
import com.tumri.cma.domain.Campaign;
import com.tumri.joz.server.domain.JozCampaignRequest;
import com.tumri.joz.server.domain.JozCampaignResponse;

import java.io.*;
import java.net.Socket;

/**
 * @author: nipun
 * Date: Aug 22, 2008
 * Time: 10:36:37 AM
 */
public class SimpleSocketClient {

    public static void main(String[] args) {
        Socket myClient=null;
        try {
            myClient = new Socket("eit-demo02.dev.tumri.net", 2544);
        }
        catch (IOException e) {
            System.out.println(e);
        }

        if (myClient!=null) {
            System.out.println("Socket created");
        }

        JozCampaignRequest campaignQuery = new JozCampaignRequest();

        XStream xstream = new XStream();
        String xmlCampaign = xstream.toXML(new Campaign());

        campaignQuery.setValue(JozCampaignRequest.KEY_CAMPAIGN, xmlCampaign);
        campaignQuery.setValue(JozCampaignRequest.KEY_COMMAND, JozCampaignRequest.COMMAND_ADD);

        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            oos = new ObjectOutputStream(new BufferedOutputStream(myClient.getOutputStream()));
            oos.writeUnshared(campaignQuery);
            oos.flush();
            oos.reset();
        }
        catch (IOException e) {
            System.out.println(e);
        }

        try {
            ois =  new ObjectInputStream(new BufferedInputStream(myClient.getInputStream()));
            Object o = null;
            try {
                o = ois.readUnshared();
            } catch(ClassNotFoundException cfe) {
                System.err.println("Class not found exception caught");
            }
            if (o!=null && o instanceof JozCampaignResponse) {
                JozCampaignResponse response = (JozCampaignResponse)o;
                if (response!=null){
                    System.out.println("Valid response got");
                }
            } else {
                System.out.println("Error in response.");
            }

        }
        catch (IOException e) {
            System.out.println(e);
        }


        try {
            oos.close();
            ois.close();
            myClient.close();
            System.out.println("Socket closed");
        }
        catch (IOException e) {
            System.out.println(e);
        }

    }
}
