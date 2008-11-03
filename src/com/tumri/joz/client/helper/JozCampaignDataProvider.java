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
package com.tumri.joz.client.helper;

import com.tumri.joz.server.domain.JozCampaignRequest;
import com.tumri.joz.server.domain.JozCampaignResponse;
import com.tumri.utils.tcp.client.TcpSocketConnectionPool;
import org.apache.log4j.Logger;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JozCampaignDataProvider {
	private static Logger log = Logger.getLogger(JozCampaignDataProvider.class);

    public JozCampaignDataProvider() {
    }

    public JozCampaignResponse processRequest(JozCampaignRequest request) {
    	JozCampaignResponse response=null;
    	boolean bDone = false;
        while (!bDone) {
            //Get connection from Pool
            TcpSocketConnectionPool.SocketStream s = TcpSocketConnectionPool.getInstance().getConnection();
            if (s==null) {
                log.error("Cannot connect to TCP server, aborting request");
                bDone = true;
                break;
            }
            log.debug("Socket being used is : " + s.s.getLocalPort());

            boolean bError = false;
            try {
                //Open the outputstream and flush out the request
                ObjectOutputStream oos = s.getOos();
                oos.writeUnshared(request);
                oos.flush();
                oos.reset();

                //Next read the response
                ObjectInputStream ois = s.getOis();
                Object o = null;
                try {
                    o = ois.readUnshared();
                } catch(ClassNotFoundException cfe) {
                    log.error("Class not found exception caught");
                }
                if (o!=null && o instanceof JozCampaignResponse) {
                    response = (JozCampaignResponse)o;
                } else {
                    log.error("Error in response.");
                }
                bDone = true;

            } catch (Throwable t) {
                log.error("Process request failed", t);
                bError = true;
            } finally {
                s.setToBeClosed(bError);
                //Release connection to pool
                TcpSocketConnectionPool.getInstance().freeConnection(s);
            }

        }
        return response;
    }
}