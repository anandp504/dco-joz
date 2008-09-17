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


import com.tumri.utils.tcp.server.TcpServer;
import com.tumri.joz.jozMain.JozData;


/**
 * @author: nipun
 * Date: Jun 9, 2008
 * Time: 2:01:37 PM
 */
public class JozServer extends TcpServer {

    public JozServer(int pSize, int port, int timeout,String queryHandlers) {
        super(pSize, port, timeout,queryHandlers);
    }

    public static void main(String[] args){
        //Intialize Joz
        JozData.init ();
        String queryHandlers = new String("com.tumri.joz.server.handlers.JozAdRequestHandler,com.tumri.joz.server.handlers.JozProviderRequestHandler,com.tumri.joz.server.handlers.JozMerchantRequestHandler,com.tumri.joz.server.handlers.JozTaxonomyRequestHandler,com.tumri.joz.server.handlers.JozCountRequestHandler,com.tumri.joz.server.handlers.JozCampaignRequestHandler,com.tumri.joz.server.handlers.JozTSpecRequestHandler,com.tumri.joz.server.handlers.JozHealthCheckRequestHandler,com.tumri.joz.server.handlers.JozICSCampaignRequestHandler");
        (new JozServer(10,25444,5000,queryHandlers)).runServer();
    }
}
