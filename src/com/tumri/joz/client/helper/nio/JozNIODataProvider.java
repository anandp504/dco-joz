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
package com.tumri.joz.client.helper.nio;

import com.tumri.utils.nio.client.NioClient;
import com.tumri.utils.nio.client.NioResponseHandler;
import com.tumri.utils.tcp.server.TCPServerException;
import com.tumri.utils.tcp.server.domain.QueryResponseData;
import com.tumri.utils.tcp.server.domain.QueryInputData;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * NIO request helper
 * @author: nipun
 * Date: Sep 21, 2008
 * Time: 7:43:21 PM
 */
public class JozNIODataProvider {

    private static Logger log = Logger.getLogger(JozNIODataProvider.class);

    /**
     * Generic method that will get the response from the NioClient
     * @param request
     * @return
     */
    public QueryResponseData processRequest(QueryInputData request) {
        QueryResponseData response = null;
        try {
            NioResponseHandler handler = new NioResponseHandler();
            NioClient.getInstance().send(request, handler);
            response = handler.waitForResponse();
        } catch (IOException e) {
            log.error("Exception caught during process request", e);
        } catch (TCPServerException e) {
            log.error("Exception caught during process request", e);
        } catch (Throwable t) {
            log.error("Exception caught during process request", t);
        }
        return response;
    }
}
