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
import com.tumri.content.data.Category;
import com.tumri.content.data.Taxonomy;
import com.tumri.joz.JoZException;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.joz.server.domain.JozCategory;
import com.tumri.joz.server.domain.JozResponse;
import com.tumri.joz.server.domain.JozTaxonomy;
import com.tumri.joz.server.domain.JozTaxonomyRequest;
import com.tumri.joz.server.domain.JozTaxonomyResponse;
import com.tumri.utils.tcp.server.domain.QueryId;
import com.tumri.utils.tcp.server.domain.QueryInputData;
import com.tumri.utils.tcp.server.domain.QueryResponseData;
import com.tumri.utils.tcp.server.handlers.InvalidRequestException;
import com.tumri.utils.tcp.server.handlers.RequestHandler;
import org.apache.log4j.Logger;

import java.util.HashMap;

/**
 * @author: nipun
 * Date: Jun 9, 2008
 * Time: 2:37:55 PM
 */

public class JozTaxonomyRequestHandler implements RequestHandler {

    private static Logger log = Logger.getLogger (JozTaxonomyRequestHandler.class);

    public QueryResponseData query(QueryInputData input) throws InvalidRequestException {
        JozTaxonomyResponse response = new JozTaxonomyResponse();
        doQuery((JozTaxonomyRequest)input, response);
        return response;
    }

    public void query(QueryInputData input, QueryResponseData response) throws InvalidRequestException {
        try {
            doQuery((JozTaxonomyRequest)input,(JozTaxonomyResponse)response);
        } catch (ClassCastException ex) {
            throw new InvalidRequestException("Only type JozTaxonomyRequest & JozTaxonomyResponse supported.",ex);
        }
    }

    public QueryId[] getSupportedIds() {
        return  new QueryId[]{ QueryId.TAXONOMY };
    }

    public void doQuery(JozTaxonomyRequest input, JozTaxonomyResponse response) {
        try {
            processRequest(input, response);
        } catch(JoZException e) {
            response.addDetails(JozTaxonomyResponse.KEY_ERROR,"Joz exception on processing the request : " + e.getMessage());
            log.error("Jozexception caught",e);
        } catch (Throwable t) {
            response.addDetails(JozTaxonomyResponse.KEY_ERROR,"Unexpected exception on processing the request : " + t.getMessage());
            log.error("Unexpected Exception caught",t);
        } 
    }

    /**
     * Create the Taxonomy listing
     * @param query
     * @return
     * @throws Exception
     */
    private void processRequest(JozTaxonomyRequest query,JozTaxonomyResponse response) throws JoZException{
        try {
            String inputCatId = query.getValue(JozTaxonomyRequest.KEY_CATEGORY);
            boolean fetchCounts = true;
            try {
                "true".equals(query.getValue(JozTaxonomyRequest.KEY_FETCH_COUNT));
            } catch (Exception e) {
                fetchCounts = true;
            }
            int maxDepth;
            try {
                maxDepth = Integer.parseInt(query.getValue(JozTaxonomyRequest.KEY_MAX_DEPTH));
            } catch (Exception e) {
                maxDepth = -1;
            }

            try {
                JOZTaxonomy tax = JOZTaxonomy.getInstance();
                Taxonomy t = tax.getTaxonomy();
                HashMap<String, CountsHelper.Counter> categoryCounts = null;
                if (fetchCounts) {
                    HashMap<String, CountsHelper.Counter>[] counts = null;
                    String tSpecName = null;
                    counts = CountsHelper.getCounters(tSpecName);
                    if (counts != null) {
                        categoryCounts = counts[0];
                    }
                }
                Category baseCategory = null;
                if (inputCatId != null) {
                    //The Assumption is that the input category id is the actual cat id such as TUMRI_11141.
                   baseCategory = t.getCategory(inputCatId);
                }
                if (baseCategory == null) {
                    baseCategory = t.getRootCategory();
                }
                XStream xstream = new XStream();
                xstream.alias("jozresponse", JozResponse.class);
        		xstream.alias("gettaxonomy",JozTaxonomy.class);
        		xstream.alias("category", JozCategory.class);
        		xstream.useAttributeFor("id", String.class);
        		xstream.useAttributeFor("name", String.class);
        		xstream.useAttributeFor("count", String.class);
        		xstream.useAttributeFor("glassIdStr",String.class);
        		JozResponse jozResponse = new JozResponse();
        		JozTaxonomy jozTax = new JozTaxonomy();
        		JozCategory rootCategory =getTaxonomyTree(baseCategory, fetchCounts, categoryCounts, 0, maxDepth);
                
                jozTax.setRootCategory(rootCategory);
                jozResponse.setTaxonomy(jozTax);
                String xml = xstream.toXML(jozResponse);
                response.addDetails(JozTaxonomyResponse.KEY_TAXONOMY, xml);
                
            } catch (Exception ex) {
                log.error("Error while fetching list of taxonomy. Request:\"" + toString() + "\".",ex);
                response.addDetails(JozTaxonomyResponse.KEY_ERROR, "Exception on getting taxonomy");
            }
            
        } catch (Exception ex) {
            log.error("Error while fetching taxonomy tree. Request:\"" + toString() + "\".",ex);
            response.addDetails(JozTaxonomyResponse.KEY_ERROR, "Exception on building XML response");
        }
    }

    /**
     * Returns the Taxonomy Tree as an XML Element. 
     * @param category
     * @param countsReq
     * @param counts
     * @param depth
     * @param maxDepth
     * @return JozCategory
     */

    
    private JozCategory getTaxonomyTree(Category category, boolean countsReq, HashMap<String, CountsHelper.Counter> counts,
            int depth, int maxDepth) {

		String glassIdStr = category.getGlassIdStr();
		String name = category.getName();
		int count = 0;
		CountsHelper.Counter counter = ((counts==null)?null:counts.get(name));
		if (counter != null) {
		count = counter.get();
		}

		JozCategory currCategory = new JozCategory();		
		currCategory.setGlassIdStr(category.getGlassIdStr());
		currCategory.setId(category.getIdStr());
		currCategory.setName(category.getName());
		
		if (countsReq) {
			currCategory.setCount(""+count);
		}
		
		if ((maxDepth <= 0) || ((depth+1) < maxDepth)) {
			Category[] childrens = category.getChildren();
			
			if (childrens != null) {
			
				for (Category childCat : childrens) {
					currCategory.addChildren(getTaxonomyTree(childCat, countsReq, counts, depth+1, maxDepth));				
				}
			}
		}
		return currCategory;
		}
    
}