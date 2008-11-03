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
package com.tumri.joz.server;

import com.tumri.utils.nio.server.NioServer;

/**
 * Test Server implementation
 * @author: nipun
 * Date: Sep 29, 2008
 * Time: 7:29:29 PM
 */
public class TestNioServer {

    public static void main(String[] args){
        //Intialize Joz
        String queryHandlers = new String("com.tumri.joz.server.handlers.JozAdRequestHandler,com.tumri.joz.server.handlers.JozProviderRequestHandler,com.tumri.joz.server.handlers.JozMerchantRequestHandler,com.tumri.joz.server.handlers.JozTaxonomyRequestHandler,com.tumri.joz.server.handlers.JozCountRequestHandler,com.tumri.joz.server.handlers.JozCampaignRequestHandler,com.tumri.joz.server.handlers.JozTSpecRequestHandler,com.tumri.joz.server.handlers.JozHealthCheckRequestHandler,com.tumri.joz.server.handlers.JozICSCampaignRequestHandler");
        try {
            NioServer server = new NioServer(null, 2544,10, queryHandlers);
            server.startServer();
        } catch (Exception e) {
           System.out.println("Server exception on start");
            e.printStackTrace();
        }
    }

}
