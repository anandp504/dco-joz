
package com.tumri.joz.server.handlers;

import com.thoughtworks.xstream.XStream;
import com.tumri.cma.domain.Recipe;
import com.tumri.cma.domain.TSpec;
import com.tumri.cma.domain.UIProperty;
import com.tumri.joz.JoZException;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.ListingProviderFactory;
import com.tumri.joz.jozMain.MerchantDB;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.joz.productselection.ProductSelectionRequest;
import com.tumri.joz.productselection.TSpecExecutor;
import com.tumri.joz.server.domain.*;
import com.tumri.lls.client.main.ListingProvider;
import com.tumri.lls.client.response.ListingResponse;
import com.tumri.utils.tcp.server.domain.QueryId;
import com.tumri.utils.tcp.server.domain.QueryInputData;
import com.tumri.utils.tcp.server.domain.QueryResponseData;
import com.tumri.utils.tcp.server.handlers.InvalidRequestException;
import com.tumri.utils.tcp.server.handlers.RequestHandler;
import org.apache.log4j.Logger;

import java.util.*;


public class JozTSpecRequestHandler implements RequestHandler {

    private static Logger log = Logger.getLogger (JozTSpecRequestHandler.class);


    public QueryResponseData query(QueryInputData input) throws InvalidRequestException {
    	JozTSpecResponse response = new JozTSpecResponse();
        doQuery((JozTSpecRequest)input, response);
        return response;
    }

    public void query(QueryInputData input, QueryResponseData response) throws InvalidRequestException {
        try {
            doQuery((JozTSpecRequest)input,(JozTSpecResponse)response);
        } catch (ClassCastException ex) {
            throw new InvalidRequestException("Only type JozTSpecRequest & JozTSpecResponse supported.",ex);
        }
    }

    public QueryId[] getSupportedIds() {
        return  new QueryId[]{ QueryId.TSPEC };
    }

    public void doQuery(JozTSpecRequest input, JozTSpecResponse response) {
        try {
            processRequest(input, response);
        } catch(JoZException e) {
            response.addDetails(JozTSpecResponse.KEY_ERROR,"Joz exception on processing the request : " + e.getMessage());
            log.error("Jozexception caught",e);
        } catch (Throwable t) {
            response.addDetails(JozTSpecResponse.KEY_ERROR,"Unexpected exception on processing the request : " + t.getMessage());
            log.error("Unexpected Exception caught",t);
        }
    }

    /**
     * Create the Provider listing
     * @param query
     * @return
     * @throws Exception
     */
    private void processRequest(JozTSpecRequest query, JozTSpecResponse response) throws JoZException{
        log.debug("Received TSpec eval request");
        try {
            JozResponse resp = new JozResponse();

            XStream xstream = new XStream();
            // get the Campaign data object from the XML using XStream apis
            String commandType = query.getValue(JozTSpecRequest.KEY_COMMAND);
            if(commandType.equalsIgnoreCase(JozTSpecRequest.KEY_GET_PRODUCTS_TSPEC)){
                String xmlTSpec = query.getValue(JozTSpecRequest.KEY_TSPEC);
                String pageNumStr = query.getValue(JozTSpecRequest.KEY_PAGE_NUM);
                String pageSizeStr = query.getValue(JozTSpecRequest.KEY_PAGE_SIZE);
                log.debug("TSpec XML = " + xmlTSpec);
                TSpec tSpec = (TSpec)xstream.fromXML(xmlTSpec);
                int pageNum = Integer.parseInt(pageNumStr);
                int pageSize = Integer.parseInt(pageSizeStr);
            	JozAdResponse adResponse = new JozAdResponse();
            	getTSpecDetails(tSpec, pageSize,pageNum,adResponse);
                resp.setAdResponse(adResponse);
            }else if(commandType.equalsIgnoreCase(JozTSpecRequest.KEY_GET_PRODUCTS_TSPEC_ID)){
                String tSpecIdStr = query.getValue(JozTSpecRequest.KEY_TSPEC_ID);
                String pageNumStr = query.getValue(JozTSpecRequest.KEY_PAGE_NUM);
                String pageSizeStr = query.getValue(JozTSpecRequest.KEY_PAGE_SIZE);
                int pageNum = Integer.parseInt(pageNumStr);
                int pageSize = Integer.parseInt(pageSizeStr);
                int tSpecId = Integer.parseInt(tSpecIdStr);
                JozAdResponse adResponse = new JozAdResponse();
                getTSpecDetails(tSpecId, pageSize,pageNum,adResponse);
                resp.setAdResponse(adResponse);
            }else if(commandType.equalsIgnoreCase(JozTSpecRequest.KEY_GET_COUNTS_TSPEC)){
                String xmlTSpec = query.getValue(JozTSpecRequest.KEY_TSPEC);
                //
                xstream.alias("jozresponse", JozResponse.class);
                xstream.alias("getcounts",JozCounts.class);
                xstream.alias("category",JozCategoryCount.class);
                xstream.alias("brand",JozBrandCount.class);
                xstream.alias("provider",JozProviderCount.class);
                xstream.useAttributeFor("name", String.class);
                xstream.useAttributeFor("count", String.class);
                //
                TSpec tSpec = (TSpec)xstream.fromXML(xmlTSpec);
                JozCounts counts = getTSpecCounts(tSpec);
                resp.setCounts(counts);
            }else if(commandType.equalsIgnoreCase(JozTSpecRequest.KEY_GET_COUNTS_TSPEC_ID)){
                String tSpecIdStr = query.getValue(JozTSpecRequest.KEY_TSPEC_ID);
                int tSpecId = Integer.parseInt(tSpecIdStr);
                JozCounts counts = getTSpecCounts(tSpecId);
                resp.setCounts(counts);
            }else if(commandType.equalsIgnoreCase(JozTSpecRequest.KEY_GET_PRODUCTS_COUNTS_TSPEC)){

                String xmlTSpec = query.getValue(JozTSpecRequest.KEY_TSPEC);
                log.debug("TSpec XML = " + xmlTSpec);
                
                String pageNumStr = query.getValue(JozTSpecRequest.KEY_PAGE_NUM);
                String pageSizeStr = query.getValue(JozTSpecRequest.KEY_PAGE_SIZE);
                TSpec tSpec = (TSpec)xstream.fromXML(xmlTSpec);
                int pageNum = Integer.parseInt(pageNumStr);
                int pageSize = Integer.parseInt(pageSizeStr);
                JozAdResponse adResponse = new JozAdResponse();;
                getTSpecDetails(tSpec, pageSize,pageNum,adResponse);
                resp.setAdResponse(adResponse);
                xstream.alias("jozresponse", JozResponse.class);
                xstream.alias("getcounts",JozCounts.class);
                xstream.alias("category",JozCategoryCount.class);
                xstream.alias("brand",JozBrandCount.class);
                xstream.alias("provider",JozProviderCount.class);
                xstream.useAttributeFor("name", String.class);
                xstream.useAttributeFor("count", String.class);
                //
                JozCounts counts = getTSpecCounts(tSpec);
                resp.setCounts(counts);
            }else if(commandType.equalsIgnoreCase(JozTSpecRequest.KEY_GET_PRODUCTS_COUNTS_TSPEC_ID)){

                String tSpecIdStr = query.getValue(JozTSpecRequest.KEY_TSPEC_ID);
                String pageNumStr = query.getValue(JozTSpecRequest.KEY_PAGE_NUM);
                String pageSizeStr = query.getValue(JozTSpecRequest.KEY_PAGE_SIZE);
                int pageNum = Integer.parseInt(pageNumStr);
                int pageSize = Integer.parseInt(pageSizeStr);
                int tSpecId = Integer.parseInt(tSpecIdStr);
            	JozAdResponse adResponse = new JozAdResponse();
            	getTSpecDetails(tSpecId, pageSize,pageNum,adResponse);
                resp.setAdResponse(adResponse);
                xstream.alias("jozresponse", JozResponse.class);
                xstream.alias("getcounts",JozCounts.class);
                xstream.alias("category",JozCategoryCount.class);
                xstream.alias("brand",JozBrandCount.class);
                xstream.alias("provider",JozProviderCount.class);
                xstream.useAttributeFor("name", String.class);
                xstream.useAttributeFor("count", String.class);
                //
                JozCounts counts = getTSpecCounts(tSpecId);
                resp.setCounts(counts);
            }
            resp.setStatus(JozResponse.JOZ_OPERATION_SUCCESS);
            String xml = xstream.toXML(resp);
            response.addDetails(JozTSpecResponse.KEY_RESPONSE, xml);

        } catch (Throwable ex) {
            log.error("Error while processing tspec request. Request:\"" + toString() + "\".",ex);
            response.addDetails(JozResponse.KEY_ERROR, "Exception on processing tspec request");
        }
    }
    
    private void getTSpecDetails(TSpec tSpec,int pageSize,int pageNum,JozAdResponse adResponse) throws JoZException{
    	log.debug("TSpec "+tSpec);
    	log.debug("pageNum "+pageNum);

    	ProductSelectionRequest pr = createProductSelectionRequest(pageSize,pageNum);
    	TSpecExecutor queryExecutor = new TSpecExecutor(pr);
    	//tSpec.setMinePubUrl(false);
    	ArrayList<Handle> prodResults = queryExecutor.processQuery(tSpec);        
    	write_result(prodResults, adResponse);
    }
	private String getRecipeData(Recipe r) {
        StringBuilder sbuild = new StringBuilder();
        List<UIProperty> props = r.getProperties();
        if (props!= null) {
            int count = props.size();
            int i = 0;
            for (UIProperty prop: props) {
                String name = prop.getName();
                String value = prop.getValue();
                if (name != null && value != null) {
                    sbuild.append(name + "=" + value);
                    if (i+1 != count) {
                        sbuild.append("&");
                    }
                }
            }
        }
        return sbuild.toString();

    }
	private void write_result( ArrayList<Handle> product_handles, JozAdResponse resp) throws JoZException {
		Integer maxDescLength = 100;// default
		if (product_handles==null) {
			throw new JoZException("No products returned by the product selection");
		}
		int phSize = product_handles.size();
		if((phSize == 0)){
			return;
		}
		long[] pids = new long[phSize];

		for (int i=0;i<phSize;i++){
			pids[i] = product_handles.get(i).getOid();
		}

		ListingProvider _prov = ListingProviderFactory.getProviderInstance(JOZTaxonomy.getInstance().getTaxonomy(),
				MerchantDB.getInstance().getMerchantData());
		ListingResponse response = _prov.getListing(pids, (maxDescLength != null) ? maxDescLength.intValue() : 0,null);
		if (response==null) {
			throw new JoZException("Invalid response from Listing Provider");
		}
		resp.addDetails(JozAdResponse.KEY_PRODUCTS,response.getListingDetails());
		resp.addDetails(JozAdResponse.KEY_PRODIDS, response.getProductIdList());
		resp.addDetails(JozAdResponse.KEY_CATEGORIES, response.getCatDetails());
		resp.addDetails(JozAdResponse.KEY_CATNAMES, response.getCatIdList());
	}
    	
    private ProductSelectionRequest createProductSelectionRequest(int pageSize,int pageNum){
    	ProductSelectionRequest pr = new ProductSelectionRequest();

    	pr.setPageSize(pageSize);
        pr.setCurrPage(pageNum);
        pr.setBPaginate(true);
        pr.setOfferType(AdDataRequest.AdOfferType.PRODUCT_ONLY);
        pr.setBRandomize(false);
        pr.setRequestKeyWords(null);
        pr.setBMineUrls(false);
        return pr;
    }

    private void getTSpecDetails(int tSpecId,int pageSize,int pageNum,JozAdResponse adResponse) throws JoZException{
    	log.debug("TSpecId "+tSpecId);
    	log.debug("pageNum "+pageNum);
    	ProductSelectionRequest pr = createProductSelectionRequest(pageSize,pageNum);
    	TSpecExecutor queryExecutor = new TSpecExecutor(pr);
    	ArrayList<Handle> prodResults = queryExecutor.processQuery(tSpecId);
        write_result(prodResults, adResponse);
    }
    
   private JozCounts getTSpecCounts(TSpec tSpec) throws JoZException{
	   log.debug("TSpec "+tSpec);
	   JozCounts counts = new JozCounts();
       HashMap<String, CountsHelper.Counter>[] counters = CountsHelper.getCounters(tSpec);

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
       return counts;
    }

    private JozCounts getTSpecCounts(int tSpecId) throws JoZException{
    	log.debug("TSpecId "+tSpecId);

 	   JozCounts counts = new JozCounts();
        HashMap<String, CountsHelper.Counter>[] counters = CountsHelper.getCounters(tSpecId);

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
        return counts;
    }
    

}