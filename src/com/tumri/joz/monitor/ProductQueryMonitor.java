package com.tumri.joz.monitor;

import com.tumri.cma.domain.*;
import com.tumri.joz.JoZException;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.jozMain.ListingProviderFactory;
import com.tumri.joz.jozMain.MerchantDB;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.joz.productselection.ProductSelectionRequest;
import com.tumri.joz.productselection.TSpecExecutor;
import com.tumri.lls.client.main.ListingProvider;
import com.tumri.lls.client.response.ListingResponse;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Monitor for JoZ ProductQuery component
 *
 * @author vijay
 */
public class ProductQueryMonitor extends ComponentMonitor
{

    private static Logger log = Logger.getLogger(ProductQueryMonitor.class);

    public ProductQueryMonitor()
    {
       super("getaddata", new ProductQueryMonitorStatus("getaddata"));
    }

    /**
     * Method not supported
     * @param tSpecId
     * @return
     */
    public MonitorStatus getStatus(String tSpecId) {
        throw new UnsupportedOperationException("Method not supported");
    }

	public MonitorStatus getStatus(String prS, String tSpecS){
		ProductSelectionRequest pr = generateProductSelectionRequest(prS);
		TSpec tSpec = generateTSpec(tSpecS);

		List<Map<String, String>>  results;

        try {
            ArrayList<Handle> handles = doProductSelection(tSpec, pr, new Features());
            results = getProductData(handles);
        }
        catch(Exception ex) {
            log.error("Error reading sexpression:  "+ex.getMessage());
            results = null;
        }

        ((ProductQueryMonitorStatus)status.getStatus()).setProducts(results);
	    ((ProductQueryMonitorStatus)status.getStatus()).setProductQuery(pr.toString());

        return status;

	}



    /**
     * Method to get the product information for a tspec
     * @param tSpecId
     * @return
     */
    public MonitorStatus getStatus(int tSpecId)
    {

        ProductSelectionRequest pr = new ProductSelectionRequest();
        pr.setPageSize(100);
        pr.setCurrPage(0);
        pr.setOfferType(AdDataRequest.AdOfferType.PRODUCT_LEADGEN);
        pr.setBPaginate(true);
        pr.setBRandomize(false);
        pr.setRequestKeyWords(null);
        pr.setBMineUrls(false);


        List<Map<String, String>>  results;

        try {
            ArrayList<Handle> handles = doProductSelection(tSpecId, pr, new Features() );
            results = getProductData(handles);
        }
        catch(Exception ex) {
            log.error("Error reading sexpression:  "+ex.getMessage());
            results = null;
        }

        ((ProductQueryMonitorStatus)status.getStatus()).setProducts(results);
	    ((ProductQueryMonitorStatus)status.getStatus()).setProductQuery(pr.toString());
        return status;
    }

    /**
     * Execute the current tspec and add to the results map
     * @param pr
     */
    private ArrayList<Handle> doProductSelection(int tspecId, ProductSelectionRequest pr, Features f) {
        TSpecExecutor qp = new TSpecExecutor(pr, f);
        return qp.processQuery(tspecId);
    }

	private ArrayList<Handle> doProductSelection(TSpec tSpec, ProductSelectionRequest pr, Features f) {
		TSpecExecutor qp = new TSpecExecutor(pr, f);
		return qp.processQuery(tSpec);
	}

    /**
     * Get the JSON Product listing response from LLC Client.
     * @param handles
     * @return
     * @throws JoZException
     */
    private List<Map<String, String>> getProductData( ArrayList<Handle> handles) throws JoZException {
        Integer maxDescLength = 100;// default
        List<Map<String, String>> products=new ArrayList<Map<String,String>>();

        if (handles==null) {
            throw new JoZException("No products returned by the product selection");
        }

        String jsonStr = null;
        ListingResponse response = null;

        if (handles.size()>0) {
            long[] pids = new long[handles.size()];

            for (int i=0;i<handles.size();i++){
                pids[i] = handles.get(i).getOid();
            }

            ListingProvider _prov = ListingProviderFactory.getProviderInstance(JOZTaxonomy.getInstance().getTaxonomy(),
                    MerchantDB.getInstance().getMerchantData());
            response = _prov.getListing(pids, (maxDescLength != null) ? maxDescLength.intValue() : 0,null);
            if (response==null) {
                throw new JoZException("Invalid response from Listing Provider");
            }

            jsonStr = response.getListingDetails();

        }
        if (jsonStr==null || "".equals(jsonStr)) {
            throw new JozMonitorException("Products not found.");
        }
        StringBuffer rawData = new StringBuffer();
        rawData.append("[PRODUCTS = " + jsonStr + "] ");
        rawData.append("[PROD-IDS = " + response.getProductIdList() + "] ");
        rawData.append("[CATEGORIES = " + response.getCatDetails() + "] ");
        rawData.append("[CAT-NAMES = " + response.getCatIdList() + "] ");

        ((ProductQueryMonitorStatus)status.getStatus()).setProductRawData(rawData.toString());

        //jsonStr = jsonStr.replaceAll("\\\\\\\\\"","\\\\\\\\\\\\\"");
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i=0; i<jsonArray.length(); i++) {
                Map<String, String> attributes = new HashMap<String, String>();
                JSONObject jsonObj  = (JSONObject)jsonArray.get(i);
                Iterator it = jsonObj.keys();
                String key = null;
                String value = null;
                while (it.hasNext()) {
                    key = (String)it.next();
                    value = (String)jsonObj.get(key);
                    if (key != null)
                        attributes.put(key, value);
                }
                products.add(attributes);
            }
        }
        catch (Exception ex) {
            log.info(jsonStr);
            log.error("Error in json parsing : " + ex);
            throw new JozMonitorException("Unexpected Json library error.");
        }

        return products;

    }

	private static ProductSelectionRequest generateProductSelectionRequest(String req){
		ProductSelectionRequest pr = new ProductSelectionRequest();
		HashSet<String> keys = new HashSet<String>();
		keys.add(":num_products");
		keys.add(":offertype");
		keys.add(":city");
		keys.add(":state");
		keys.add(":country");
		keys.add(":zipcode");
		keys.add(":dmacode");
		keys.add(":areacode");
		keys.add(":brandomize");
		keys.add(":bmineurls");
		keys.add(":bpaginate");
		keys.add(":bbackfill");
		keys.add(":requestkeywords");
		keys.add(":requestcategory");
		keys.add(":multivaluequery1");
		keys.add(":multivaluequery2");
		keys.add(":multivaluequery3");
		keys.add(":multivaluequery4");
		keys.add(":multivaluequery5");

		if(req == null || "".equals(req.trim())){
			pr.setCurrPage(0);
			pr.setOfferType(AdDataRequest.AdOfferType.PRODUCT_LEADGEN);
			pr.setBPaginate(true);
			pr.setBRandomize(false);
			pr.setRequestKeyWords(null);
			pr.setBMineUrls(false);
		} else {

			com.tumri.utils.strings.StringTokenizer reqTokenizer = new com.tumri.utils.strings.StringTokenizer(req, ' ');
			String key = "";
			String value = "";
			String cToken = "";
			ArrayList<String> args = reqTokenizer.getTokens();
			//while(reqTokenizer.hasMoreTokens()){
			args.add(null);
			for(int i = 0; i < args.size(); i++){
				if(i == (args.size() - 1)){
					if(!"".equals(value)){
						value += " ";
					}
					value += cToken;
					cToken = key;
				}
				if(keys.contains(cToken)){
					if(":offertype".equalsIgnoreCase(cToken)){
						if(!"".equals(value)){
							if("leadgen".equalsIgnoreCase(value)){
								pr.setOfferType(AdDataRequest.AdOfferType.LEADGEN_ONLY);
							} else if("product".equalsIgnoreCase(value)){
								pr.setOfferType(AdDataRequest.AdOfferType.PRODUCT_ONLY);
							} else {
								pr.setOfferType(AdDataRequest.AdOfferType.PRODUCT_LEADGEN);
							}
						}
					} else if(":num_products".equalsIgnoreCase(cToken)){
						if(!"".equals(value)){
							pr.setPageSize(Integer.parseInt(value));
						}
					} else if(":city".equalsIgnoreCase(cToken)){
						if(!"".equals(value)){
							pr.setCityCode(value);
						}
					} else if(":state".equalsIgnoreCase(cToken)){
						if(!"".equals(value)){
							pr.setStateCode(value);
						}
					} else if(":country".equalsIgnoreCase(cToken)){
						if(!"".equals(value)){
							pr.setCountryCode(value);
						}
					} else if(":zipcode".equalsIgnoreCase(cToken)){
						if(!"".equals(value)){
							pr.setZipCode(value);
						}
					} else if(":dmacode".equalsIgnoreCase(cToken)){
						if(!"".equals(value)){
							pr.setDmaCode(value);
						}
					} else if(":areacode".equalsIgnoreCase(cToken)){
						if(!"".equals(value)){
							pr.setAreaCode(value);
						}
					} else if(":brandomize".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							if("false".equalsIgnoreCase(value.trim())){
								pr.setBRandomize(false);
							} else {
								pr.setBRandomize(true);
							}
						}
					} else if(":mineurls".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							if("false".equalsIgnoreCase(value.trim())){
								pr.setBMineUrls(false);
							} else {
								pr.setBMineUrls(true);
							}
						}
					} else if(":bpaginate".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							if("false".equalsIgnoreCase(value.trim())){
								pr.setBPaginate(false);
							} else {
								pr.setBPaginate(true);
							}
						}
					} else if(":bbackfill".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							if("false".equalsIgnoreCase(value.trim())){
								pr.setBBackFill(false);
							} else {
								pr.setBBackFill(true);
							}
						}
					} else if(":requestkeywords".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							pr.setRequestKeyWords(value);
						}
					} else if(":requestcategory".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							pr.setRequestCategory(value);
						}
					} else if(":multivaluequery1".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							pr.setMultiValueQuery1(value);
						}
					} else if(":multivaluequery2".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							pr.setMultiValueQuery2(value);
						}
					} else if(":multivaluequery3".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							pr.setMultiValueQuery3(value);
						}
					} else if(":multivaluequery4".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							pr.setMultiValueQuery4(value);
						}
					} else if(":multivaluequery5".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							pr.setMultiValueQuery5(value);
						}
					}
				}else {
					if(!"".equals(value)){
						value += " ";
					}
					value += cToken;
					cToken = args.get(i);
					continue;
				}
				value = "";
				key = cToken;
				cToken = args.get(i);
			}
		}
		if(pr.getPageSize() <= 0){
			pr.setPageSize(100);
		}

		return pr;
	}

	private static TSpec generateTSpec(String req){
		TSpec tSpec = new TSpec();
		HashSet<String> keys = new HashSet<String>();
		keys.add(":allowexternalquery");
		keys.add(":useradiusquery");
		keys.add(":radius");
		keys.add(":minepuburl");
		keys.add(":lowprice");
		keys.add(":highprice");
		keys.add(":applygeofilter");
		keys.add(":applyurlfilter");
		keys.add(":applykeywordfilter");
		keys.add(":includedproviders");
		keys.add(":excludedproviders");
		keys.add(":excludedcategories");
		keys.add(":includedcategories");
		keys.add(":includedbrands");
		keys.add(":excludedbrands");
		keys.add(":excludedmerchants");
		keys.add(":includedmerchants");
		keys.add(":includedproducts");
		keys.add(":excludedproducts");
		keys.add(":includedkeywords");

		if(req != null && !"".equals(req.trim())){
			com.tumri.utils.strings.StringTokenizer reqTokenizer = new com.tumri.utils.strings.StringTokenizer(req, ' ');
			String key = "";
			String value = "";
			String cToken = "";
			ArrayList<String> args = reqTokenizer.getTokens();
			args.add(null);
			for(int i = 0; i < args.size(); i++){
				if(i == (args.size() - 1)){
					if(!"".equals(value)){
						value += " ";
					}
					value += cToken;
					cToken = key;
				}
				if(keys.contains(cToken)){
					if(":allowexternalquery".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							if("false".equalsIgnoreCase(value)){
								tSpec.setAllowExternalQuery(false);
							} else {
								tSpec.setAllowExternalQuery(true);
							}
						}
					} else if(":useradiusquery".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							if("false".equalsIgnoreCase(value)){
								tSpec.setUseRadiusQuery(false);
							} else {
								tSpec.setUseRadiusQuery(true);
							}
						}
					} else if(":radius".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							tSpec.setRadius(Integer.parseInt(value));
						}
					} else if(":minepuburl".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							if("false".equalsIgnoreCase(value)){
								tSpec.setMinePubUrl(false);
							} else {
								tSpec.setMinePubUrl(true);
							}
						}
					} else if(":lowprice".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							tSpec.setLowPrice(Integer.parseInt(value));
						}
					} else if(":highprice".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							tSpec.setHighPrice(Integer.parseInt(value));
						}
					} else if(":applygeofilter".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							if("false".equalsIgnoreCase(value)){
								tSpec.setApplyGeoFilter(false);
							}else{
								tSpec.setApplyGeoFilter(true);
							}
						}
					} else if(":applyurlfilter".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							if("false".equalsIgnoreCase(value)){
								tSpec.setApplyUrlFilter(false);
							} else {
								tSpec.setApplyUrlFilter(true);
							}
						}
					} else if(":applykeywordfilter".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							if("false".equalsIgnoreCase(value)){
								tSpec.setApplyKeywordFilter(false);
							} else {
								tSpec.setApplyKeywordFilter(true);
							}
						}
					} else if(":includedproviders".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value,',');
							ArrayList<String> incProvs = st.getTokens();
							for(String prov: incProvs){
								ProviderInfo provInfo = new ProviderInfo(prov.trim());
								tSpec.addIncludedProviders(provInfo);
							}
						}
					} else if(":excludedproviders".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value,',');
							ArrayList<String> exProvs = st.getTokens();
							for(String prov: exProvs){
								ProviderInfo provInfo = new ProviderInfo(prov.trim());
								tSpec.addExcludedProviders(provInfo);
							}
						}
					} else if(":excludedcategories".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value,',');
							ArrayList<String> exCats = st.getTokens();
							for(String cat: exCats){
								CategoryInfo catInfo = new CategoryInfo(cat.trim(), cat.trim());
								tSpec.addExcludedCategories(catInfo);
							}
						}
					} else if(":includedcategories".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value,',');
							ArrayList<String> incCats = st.getTokens();
							for(String cat: incCats){
								CategoryInfo catInfo = new CategoryInfo(cat.trim(), cat.trim());
								tSpec.addIncludedCategories(catInfo);
							}
						}
					} else if(":includedbrands".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value,',');
							ArrayList<String> incbrands = st.getTokens();
							for(String brand: incbrands){
								BrandInfo brandInfo = new BrandInfo(brand.trim(), brand.trim());
								tSpec.addIncludedBrand(brandInfo);
							}
						}
					} else if(":excludedbrands".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value,',');
							ArrayList<String> exbrands = st.getTokens();
							for(String brand: exbrands){
								BrandInfo brandInfo = new BrandInfo(brand.trim(), brand.trim());
								tSpec.addExcludedBrand(brandInfo);
							}
						}
					} else if(":excludedmerchants".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value,',');
							ArrayList<String> exMerchs = st.getTokens();
							for(String merch: exMerchs){
								MerchantInfo merchInfo = new MerchantInfo(merch.trim());
								tSpec.addExcludedMerchant(merchInfo);
							}
						}
					} else if(":includedmerchants".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value,',');
							ArrayList<String> incMerchs = st.getTokens();
							for(String merch: incMerchs){
								MerchantInfo merchInfo = new MerchantInfo(merch.trim());
								tSpec.addIncludedMerchant(merchInfo);
							}
						}
					} else if(":includedproducts".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value,',');
							ArrayList<String> incProds = st.getTokens();
							for(String prod: incProds){
								ProductInfo prodInfo = new ProductInfo(prod.trim());
								tSpec.addIncludedProducts(prodInfo);
							}
						}
					} else if(":excludedproducts".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value,',');
							ArrayList<String> exProds = st.getTokens();
							for(String prod: exProds){
								ProductInfo prodInfo = new ProductInfo(prod.trim());
								tSpec.addExcludedProducts(prodInfo);
							}
						}
					} else if(":includedkeywords".equalsIgnoreCase(key)){
						if(!"".equals(value)){
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value,',');
							ArrayList<String> incKeywords = st.getTokens();
							for(String keyword: incKeywords){
								KeywordInfo keywordInfo = new KeywordInfo(keyword.trim());
								tSpec.addIncludedKeywords(keywordInfo);
							}
						}
					}
				} else {
					if(!"".equals(value)){
						value += " ";
					}
					value += cToken;
					cToken = args.get(i);
					continue;
				}
				value = "";
				key = cToken;
				cToken = args.get(i);
			}
		}

		return tSpec;
	}

}
