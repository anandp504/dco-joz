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

import org.apache.log4j.Logger;
import org.junit.Assert;
import com.tumri.joz.client.impl.JozDataProviderImpl;
import com.tumri.joz.server.domain.JozResponse;
import com.tumri.cma.domain.Campaign;
import com.tumri.utils.tcp.client.TcpSocketConnectionPool;
import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;

/**
 * @author: nipun
 * Date: Aug 22, 2008
 * Time: 10:13:38 AM
 */
public class TestJozCampaignAdd {

    private static Logger log = Logger.getLogger(TestJozCampaignAdd.class);
    private static JozDataProvider jozProvider = null;

    private static void init() {
        try {
            if (jozProvider == null) {
                String host = "eit-demo02.dev.tumri.net";
                int port = 2544;
                int poolSize = 10;
                int numRetries = 3;
                jozProvider = new JozDataProviderImpl(host,port,poolSize,numRetries);
            }
        } catch (Throwable e) {
            System.out.println("Exception caught in connecting to Joz");
            e.printStackTrace();
        }
    }

    private static void shutdown() {
        TcpSocketConnectionPool.getInstance().tearDown();
    }

    public static void main(String[] args) {
        init();
        StringBuilder sb = new StringBuilder();
        try {
            File file = new File("/Users/nipun/Desktop/campaign-d.xml");
            //File file = new File("/Users/nipun/Desktop/testCampaign.xml");
            FileInputStream fis = new FileInputStream(file);

            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);

            while (dis.available() != 0) {
                sb.append(dis.readLine());
            }

            fis.close();
            bis.close();
            dis.close();
            String xmlCampaign = sb.toString();
            XStream xstream = new XStream();
            xstream.processAnnotations(java.util.List.class);
            xstream.processAnnotations(Campaign.class);

            ArrayList<Campaign> campaigns = (ArrayList<Campaign>)xstream.fromXML(xmlCampaign);
            for (Campaign c: campaigns) {
                JozResponse  response = jozProvider.addCampaign(c);
                Assert.assertNotNull(campaigns);
                System.out.println("Campaign added ");
            }

        } catch (Exception e) {
            System.out.println("Exception caught");
            e.printStackTrace();
        }
        shutdown();
    }

}
