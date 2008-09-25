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
import com.tumri.content.data.dictionary.DictionaryManager;
import com.tumri.joz.JoZException;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.server.domain.JozProvider;
import com.tumri.joz.server.domain.JozProviderRequest;
import com.tumri.joz.server.domain.JozProviderResponse;
import com.tumri.joz.server.domain.JozResponse;
import com.tumri.utils.tcp.server.domain.QueryId;
import com.tumri.utils.tcp.server.domain.QueryInputData;
import com.tumri.utils.tcp.server.domain.QueryResponseData;
import com.tumri.utils.tcp.server.handlers.InvalidRequestException;
import com.tumri.utils.tcp.server.handlers.RequestHandler;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * @author: nipun
 * Date: Jun 9, 2008
 * Time: 2:37:55 PM
 */

public class JozProviderRequestHandler implements RequestHandler {

    private static Logger log = Logger.getLogger (JozProviderRequestHandler.class);

    public QueryResponseData query(QueryInputData input) throws InvalidRequestException {
        JozProviderResponse response = new JozProviderResponse();
        doQuery((JozProviderRequest)input, response);
        return response;
    }

    public void query(QueryInputData input, QueryResponseData response) throws InvalidRequestException {
        try {
            doQuery((JozProviderRequest)input,(JozProviderResponse)response);
        } catch (ClassCastException ex) {
            throw new InvalidRequestException("Only type JozProviderRequest & JozProviderResponse supported.",ex);
        }
    }

    public QueryId[] getSupportedIds() {
        return  new QueryId[]{ QueryId.PROVIDER };
    }

    public void doQuery(JozProviderRequest input, JozProviderResponse response) {
        try {
            processRequest(input, response);
        } catch(JoZException e) {
            response.addDetails(JozProviderResponse.KEY_ERROR,"Joz exception on processing the request : " + e.getMessage());
            log.error("Jozexception caught",e);
        } catch (Throwable t) {
            response.addDetails(JozProviderResponse.KEY_ERROR,"Unexpected exception on processing the request : " + t.getMessage());
            log.error("Unexpected Exception caught",t);
        }
    }

    /**
     * Create the Provider listing
     * @param query - query
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private void processRequest(JozProviderRequest query,  JozProviderResponse response) throws JoZException{
        //GetProviders does not have any specific query parmaters right now
        //Build XML String
        log.info("Received Provider request");
        try {
            XStream xstream = new XStream();
            // set the aliases
            xstream.alias("jozresponse", JozResponse.class);
            xstream.alias("getprovider",ArrayList.class);
            xstream.alias("provider",JozProvider.class);
            xstream.useAttributeFor("id", String.class);
            xstream.useAttributeFor("name", String.class);
            xstream.useAttributeFor("logourl",String.class);
            ArrayList<JozProvider> jozProviders = new ArrayList<JozProvider>();
            ProductAttributeIndex<Integer,Handle> pai = ProductDB.getInstance().getIndex(IProduct.Attribute.kProvider);
            if (pai != null) {
                Set<Integer> providers = pai.getKeys();
                String providerStr;
                for (Integer provider: providers) {
                    providerStr = DictionaryManager.getInstance().getValue(IProduct.Attribute.kProvider, provider);
                    if (providerStr != null) {
                        JozProvider p = new JozProvider();
                        p.setId(providerStr);
                        p.setName(providerStr);
                        p.setLogourl("");
                        jozProviders.add(p);
                    }
                }
            }
            JozResponse resp = new JozResponse();
            resp.setProviders(jozProviders);
            String xml = xstream.toXML(resp);
            response.addDetails(JozProviderResponse.KEY_PROVIDERS, xml);
        } catch (Throwable ex) {
            log.error("Error while fetching list of providers. Request:\"" + toString() + "\".",ex);
            response.addDetails(JozProviderResponse.KEY_ERROR, "Exception on getting providers");
        }

    }
}