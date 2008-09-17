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
package com.tumri.joz.server.handlers;

import com.tumri.joz.JoZException;
import com.tumri.joz.server.domain.JozHCRequest;
import com.tumri.joz.server.domain.JozHCResponse;
import com.tumri.joz.utils.HealthCheckUtils;
import com.tumri.utils.tcp.server.domain.QueryId;
import com.tumri.utils.tcp.server.domain.QueryInputData;
import com.tumri.utils.tcp.server.domain.QueryResponseData;
import com.tumri.utils.tcp.server.handlers.InvalidRequestException;
import com.tumri.utils.tcp.server.handlers.RequestHandler;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.io.UnsupportedEncodingException;

/**
 * @author: nipun
 * Date: Jun 9, 2008
 * Time: 2:37:55 PM
 */

public class JozHealthCheckRequestHandler implements RequestHandler {

    private static Logger log = Logger.getLogger (JozHealthCheckRequestHandler.class);

    public QueryResponseData query(QueryInputData input) throws InvalidRequestException {
        return doQuery();
    }

    public void query(QueryInputData input, QueryResponseData response) throws InvalidRequestException {
        throw new UnsupportedOperationException();
    }

    public QueryId[] getSupportedIds() {
        return  new QueryId[]{ QueryId.HC };
    }

    private JozHCResponse doQuery() {
        JozHCResponse result = new JozHCResponse();
        try {
            processRequest(result);
        } catch(JoZException e) {
            result.addDetails(JozHCResponse.KEY_ERROR,"Joz exception on processing the request : " + e.getMessage());
            log.error("Jozexception caught",e);
        } catch (Throwable t) {
            result.addDetails(JozHCResponse.KEY_ERROR,"Unexpected exception on processing the request : " + t.getMessage());
            log.error("Unexpected Exception caught",t);
        }
        return result;
    }

    /**
     * Create the response
     * @return
     * @throws Exception
     */
    private void processRequest(JozHCResponse result) throws JoZException{
        try {
            log.debug("Going to process health check request");
            result.addDetails(JozHCResponse.KEY_STATUS,(HealthCheckUtils.doHealthCheck()? JozHCResponse.SUCCESS:JozHCResponse.FAILED));
            log.debug("Finished health check request");
        } catch (Exception ex) {
            log.error("Error while  doing joz health check.",ex);
            result.addDetails(JozHCResponse.KEY_ERROR,"Exception on building XML response");
        }
    }



}