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

import com.thoughtworks.xstream.XStream;
import com.tumri.joz.JoZException;
import com.tumri.joz.server.domain.JozBrandCount;
import com.tumri.joz.server.domain.JozCategoryCount;
import com.tumri.joz.server.domain.JozCountRequest;
import com.tumri.joz.server.domain.JozCountResponse;
import com.tumri.joz.server.domain.JozCounts;
import com.tumri.joz.server.domain.JozProviderCount;
import com.tumri.joz.server.domain.JozResponse;
import com.tumri.utils.tcp.server.domain.QueryId;
import com.tumri.utils.tcp.server.domain.QueryInputData;
import com.tumri.utils.tcp.server.domain.QueryResponseData;
import com.tumri.utils.tcp.server.handlers.InvalidRequestException;
import com.tumri.utils.tcp.server.handlers.RequestHandler;
import org.apache.log4j.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author: nipun
 * Date: Jun 9, 2008
 * Time: 2:37:55 PM
 */

public class JozCountRequestHandler implements RequestHandler {

    private static Logger log = Logger.getLogger (JozCountRequestHandler.class);

    public QueryResponseData query(QueryInputData input) throws InvalidRequestException {
        JozCountResponse response = new JozCountResponse();
        query(input, response);
        return response;
    }

    public void query(QueryInputData input, QueryResponseData response) throws InvalidRequestException {
        try {
            query((JozCountRequest)input,(JozCountResponse)response);
        } catch (ClassCastException ex) {
            throw new InvalidRequestException("Only type JozCountRequest & JozCountResponse supported.",ex);
        }
    }

    public QueryId[] getSupportedIds() {
        return  new QueryId[]{ QueryId.COUNTS };
    }

    public void query(JozCountRequest input, JozCountResponse response) {
        try {
            processRequest(input, response);
        } catch(JoZException e) {
            response.addDetails(JozCountResponse.KEY_ERROR,"Joz exception on processing the request : " + e.getMessage());
            log.error("Jozexception caught",e);
        } catch (Throwable t) {
            response.addDetails(JozCountResponse.KEY_ERROR,"Unexpected exception on processing the request : " + t.getMessage());
            log.error("Unexpected Exception caught",t);
        }
    }

    /**
     * Create the Count listing
     * @param query
     * @return
     * @throws Exception
     */
    private void processRequest(JozCountRequest query, JozCountResponse response) throws JoZException{
        try {
            log.info("Going to process get count request");
            String tspecName = query.getValue(JozCountRequest.KEY_TSPEC);
            if (tspecName == null || "".equals(tspecName)) {
                throw new JoZException("TSpec name not provider for getCounts");
            }
            XStream xstream = new XStream();
            // set the alises
            xstream.alias("jozresponse", JozResponse.class);
    		xstream.alias("getcounts",JozCounts.class);  		
    		xstream.alias("category",JozCategoryCount.class);
    		xstream.alias("brand",JozBrandCount.class);
    		xstream.alias("provider",JozProviderCount.class);
    		xstream.useAttributeFor("name", String.class);
    		xstream.useAttributeFor("count", String.class);
    		JozCounts counts = new JozCounts();
    		try {
                HashMap<String, CountsHelper.Counter>[] counters = CountsHelper.getCounters(tspecName);

                HashMap<String, CountsHelper.Counter> category_counts = counters[0];

                Set<Map.Entry<String, CountsHelper.Counter>> cat_counts = category_counts.entrySet();
                for (Map.Entry<String, CountsHelper.Counter> count : cat_counts) {
                    JozCategoryCount categoryCount = new JozCategoryCount(count.getKey(), new Integer(count.getValue().get()).toString());
                    counts.addCategoryCount(categoryCount);
                }
                HashMap<String, CountsHelper.Counter> brand_counts = counters[1];
                Set<Map.Entry<String, CountsHelper.Counter>> brnd_counts = brand_counts.entrySet();
                for (Map.Entry<String, CountsHelper.Counter> count : brnd_counts) {
                    JozBrandCount brandCount = new JozBrandCount(count.getKey(), new Integer(count.getValue().get()).toString());
                    counts.addBrandCount(brandCount);
                }

                HashMap<String, CountsHelper.Counter> provider_counts = counters[2];
                Set<Map.Entry<String, CountsHelper.Counter>> prov_counts = provider_counts.entrySet();
                for (Map.Entry<String, CountsHelper.Counter> count : prov_counts) {
                    JozProviderCount providerCount = new JozProviderCount(count.getKey(), new Integer(count.getValue().get()).toString());
                    counts.addProviderCount(providerCount);
                }               
                JozResponse jresponse = new JozResponse();
                jresponse.setCounts(counts);
                String xml = xstream.toXML(jresponse);
                response.addDetails(JozCountResponse.KEY_COUNTS, xml);

            } catch (Exception ex) {
                log.error("Error while fetching count. Request:\"" + toString() + "\".",ex);
                response.addDetails(JozCountResponse.KEY_ERROR, "Exception on getting merchants");
            }
        } catch (Exception ex) {
            log.error("Error while fetching Counts. Request:\"" + toString() + "\".",ex);
            response.addDetails(JozCountResponse.KEY_ERROR, "Exception on building XML response");
        }
    }


    
}