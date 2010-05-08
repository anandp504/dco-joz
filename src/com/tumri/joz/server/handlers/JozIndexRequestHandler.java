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
import com.tumri.content.data.Product;
import com.tumri.content.data.dictionary.DictionaryManager;
import com.tumri.joz.JoZException;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.server.domain.JozIndexRequest;
import com.tumri.joz.server.domain.JozIndexResponse;
import com.tumri.joz.server.domain.JozResponse;
import com.tumri.joz.utils.IndexUtils;
import com.tumri.utils.tcp.server.domain.QueryId;
import com.tumri.utils.tcp.server.domain.QueryInputData;
import com.tumri.utils.tcp.server.domain.QueryResponseData;
import com.tumri.utils.tcp.server.handlers.InvalidRequestException;
import com.tumri.utils.tcp.server.handlers.RequestHandler;
import com.tumri.utils.data.SortedArraySet;
import com.tumri.utils.dictionary.Dictionary;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.SortedSet;

/**
 * Request handler that will return the values from the given index
 * @author: nipun
 * Date: Jun 9, 2008
 * Time: 2:37:55 PM
 */

public class JozIndexRequestHandler implements RequestHandler {

    private static Logger log = Logger.getLogger (JozIndexRequestHandler.class);

    public QueryResponseData query(QueryInputData input) throws InvalidRequestException {
        JozIndexResponse response = new JozIndexResponse();
        doQuery((JozIndexRequest)input, response);
        return response;
    }

    public void query(QueryInputData input, QueryResponseData response) throws InvalidRequestException {
        try {
            doQuery((JozIndexRequest)input,(JozIndexResponse)response);
        } catch (ClassCastException ex) {
            throw new InvalidRequestException("Only type JozIndexRequest & JozIndexResponse supported.",ex);
        }
    }

    public QueryId[] getSupportedIds() {
        return  new QueryId[]{ QueryId.INDEX };
    }

    public void doQuery(JozIndexRequest input, JozIndexResponse response) {
        try {
            processRequest(input, response);
        } catch(JoZException e) {
            response.addDetails(JozIndexResponse.KEY_ERROR,"Joz exception on processing the request : " + e.getMessage());
            log.error("Jozexception caught",e);
        } catch (Throwable t) {
            response.addDetails(JozIndexResponse.KEY_ERROR,"Unexpected exception on processing the request : " + t.getMessage());
            log.error("Unexpected Exception caught",t);
        }
    }

    /**
     * Create the Index Val listing
     * @param query - query
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private void processRequest(JozIndexRequest query,  JozIndexResponse response) throws JoZException{
        String indexKey = query.getValue(JozIndexRequest.KEY_INDEX);
        String prov = query.getValue(JozIndexRequest.KEY_PROVIDER);
        if (indexKey == null) {
            response.addDetails(JozIndexResponse.KEY_ERROR, "Empty request");
            return;
        }

        Product.Attribute kAttr = IndexUtils.getAttribute(indexKey);
        if (kAttr == null) {
            response.addDetails(JozIndexResponse.KEY_ERROR, "Could not find the attribute for the given index type : " + indexKey);
            return;
        }
        
        //Build XML String
        log.debug("Received Index request for : " + kAttr.name() + " and provider = " + prov);
        try {
            XStream xstream = new XStream();
            // set the aliases
            xstream.alias("jozresponse", JozResponse.class);
            xstream.alias("getindexvals",ArrayList.class);
            SortedSet<String> indexVals = null;
            if (prov!=null && !prov.isEmpty()) {
                indexVals = getProviderIndexVals(kAttr, prov);
            } else {
                indexVals = getIndexVals(kAttr);
            }
            ArrayList<String> resultAL = new ArrayList<String>();
            if (indexVals!=null && !indexVals.isEmpty()) {
                resultAL.addAll(indexVals);
            }
            JozResponse resp = new JozResponse();
            resp.setIndexVals(resultAL);
            String xml = xstream.toXML(resp);
            response.addDetails(JozIndexResponse.KEY_INDEXVALS, xml);
        } catch (Throwable ex) {
            log.error("Error while fetching list of indexvals. Request:\"" + indexKey + "\".",ex);
            response.addDetails(JozIndexResponse.KEY_ERROR, "Exception on getting index vals");
        }

    }

    /**
     * Get all index vals  by looking up the dictionary
     * @param kAttr
     * @return
     */
    private SortedSet<String> getIndexVals(Product.Attribute kAttr) {
        SortedSet<String> indexVals = new SortedArraySet<String>();
        Dictionary<String> d = DictionaryManager.getDictionary(kAttr);
        if (d!=null) {
            indexVals.addAll(d.getValues());
        }
        return indexVals;
    }

    /**
     * Get the provider specific index vals
     * @param kAttr
     * @param providerId
     * @return
     */
    @SuppressWarnings("unchecked")
    private SortedSet<String> getProviderIndexVals(Product.Attribute kAttr, String providerId) {
        SortedSet<String> indexVals = new SortedArraySet<String>();
        ProductAttributeIndex<Integer,Handle> providerIndex = ProductDB.getInstance().getIndex(Product.Attribute.kProvider);
        Integer provid = DictionaryManager.getId(Product.Attribute.kProvider, providerId);
        SortedSet<Handle> provHandles = providerIndex.get(provid);

        if (kAttr == Product.Attribute.kProductType) {
            //For product type - get the values from the product handle
            if (provHandles!=null) {
                for (Handle h: provHandles) {
                    ProductHandle ph = (ProductHandle)h;
                    indexVals.add(DictionaryManager.getValue(Product.Attribute.kProductType, ph.getProductType()));
                }
            }

        } else {
            ProductAttributeIndex<Integer,Handle> pai = ProductDB.getInstance().getIndex(kAttr);
            if (pai != null) {
                Set<Integer> keys = pai.getKeys();
                for (Integer id: keys) {
                    SortedSet<Handle> attrHandles = pai.get(id);
                    for (Handle h: attrHandles) {
                        if (provHandles.contains(h)) {
                            String valStr = DictionaryManager.getValue(kAttr, id);
                            indexVals.add(valStr);
                        }
                    }
                }
            }
        }
        return indexVals;
    }
}