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
package com.tumri.joz.server;


import com.tumri.utils.tcp.server.TCPServerException;
import com.tumri.utils.nio.server.NioServer;
import com.tumri.joz.jozMain.JozData;

import java.io.IOException;

import org.apache.log4j.Logger;


/**
 * @author: nipun
 * Date: Jun 9, 2008
 * Time: 2:01:37 PM
 */
public class JozNioServer implements JozBaseServer {
    private NioServer server = null;
    private static Logger log = Logger.getLogger(NioServer.class);

    public JozNioServer(int p, int poolsize, String qh) throws TCPServerException {
        try {
            server = new NioServer(null, p, poolsize, qh);
        } catch (IOException e) {
            log.fatal("Could not initialize the NIOServer", e);
            throw new TCPServerException(e);
        }
    }

    /**
     * Start the server
     */
    public void start() {
        if (server!=null) server.startServer();
    }

    /**
     * Stop the server
     */
    public void stop() {
        if (server!=null) server.stopServer();
    }

    public static void main(String[] args){
        //Intialize Joz
        JozData.init ();
        String queryHandlers = new String("com.tumri.joz.server.handlers.JozAdRequestHandler,com.tumri.joz.server.handlers.JozProviderRequestHandler,com.tumri.joz.server.handlers.JozMerchantRequestHandler,com.tumri.joz.server.handlers.JozTaxonomyRequestHandler,com.tumri.joz.server.handlers.JozCountRequestHandler,com.tumri.joz.server.handlers.JozCampaignRequestHandler,com.tumri.joz.server.handlers.JozTSpecRequestHandler,com.tumri.joz.server.handlers.JozHealthCheckRequestHandler,com.tumri.joz.server.handlers.JozICSCampaignRequestHandler");
        try {
            JozNioServer server = new JozNioServer(2544,10, queryHandlers);
            server.start();
        } catch (Exception e) {
           System.out.println("Server excption on start");
            e.printStackTrace();
        }
    }
}