package com.tumri.joz.monitor;

import com.tumri.cma.domain.*;
import com.tumri.content.data.impl.AdvertiserMerchantDataMapperImpl;
import com.tumri.content.data.impl.AdvertiserTaxonomyMapperImpl;
import com.tumri.joz.JoZException;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.jozMain.ListingProviderFactory;
import com.tumri.joz.products.Handle;
import com.tumri.joz.productselection.ProductSelectionRequest;
import com.tumri.joz.productselection.TSpecExecutor;
import com.tumri.lls.client.main.ListingProvider;
import com.tumri.lls.client.response.ListingResponse;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
//import org.json.JSONArray;
//import org.json.JSONObject;

import java.util.*;

/**
 * Monitor for JoZ ProductQuery component
 *
 * @author vijay
 */
public class ProductQueryMonitor extends ComponentMonitor {

	private static Logger log = Logger.getLogger(ProductQueryMonitor.class);

	public ProductQueryMonitor() {
		super("getaddata", new ProductQueryMonitorStatus("getaddata"));
	}

	/**
	 * Method not supported
	 *
	 * @param tSpecId
	 * @return
	 */
	public MonitorStatus getStatus(String tSpecId) {
		throw new UnsupportedOperationException("Method not supported");
	}

	public MonitorStatus getStatus(String prS, String tSpecS, String advertiser) {
		ProductSelectionRequest pr = generateProductSelectionRequest(prS);
		TSpec tSpec = generateTSpec(tSpecS);
		if (advertiser == null) {
			advertiser = pr.getAdvertiser();
		}

		if (advertiser == null) {
			List<ProviderInfo> info = tSpec.getIncludedProviders();
			if (info != null && !info.isEmpty()) {
				advertiser = info.get(0).getName();
			}
		}
		List<Map<String, String>> results;

		try {
			ArrayList<Handle> handles = doProductSelection(tSpec, pr, new Features());
			results = getProductData(advertiser, handles);
		}
		catch (Exception ex) {
			log.error("Error reading sexpression:  ", ex);
			ex.printStackTrace();
			results = null;
		}

		((ProductQueryMonitorStatus) status.getStatus()).setProducts(results);
		((ProductQueryMonitorStatus) status.getStatus()).setProductQuery(pr.toString());

		return status;

	}


	/**
	 * Method to get the product information for a tspec
	 *
	 * @param tSpecId
	 * @return
	 */
	public MonitorStatus getStatus(int tSpecId, String advertiser) {

		ProductSelectionRequest pr = new ProductSelectionRequest();
		pr.setPageSize(100);
		pr.setCurrPage(0);
		pr.setOfferType(AdDataRequest.AdOfferType.PRODUCT_LEADGEN);
		pr.setBPaginate(true);
		pr.setBRandomize(false);
		pr.setRequestKeyWords(null);
		pr.setBMineUrls(false);


		List<Map<String, String>> results;

		try {
			ArrayList<Handle> handles = doProductSelection(tSpecId, pr, new Features());
			results = getProductData(advertiser, handles);
		}
		catch (Exception ex) {
			log.error("Error reading sexpression:  " + ex.getMessage());
			results = null;
		}

		((ProductQueryMonitorStatus) status.getStatus()).setProducts(results);
		((ProductQueryMonitorStatus) status.getStatus()).setProductQuery(pr.toString());
		return status;
	}

	/**
	 * Execute the current tspec and add to the results map
	 *
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
	 *
	 * @param handles
	 * @return
	 * @throws JoZException
	 */
	private List<Map<String, String>> getProductData(String advertiser, ArrayList<Handle> handles) throws JoZException {
		Integer maxDescLength = 100;// default
		List<Map<String, String>> products = new ArrayList<Map<String, String>>();

		if (handles == null) {
			throw new JoZException("No products returned by the product selection");
		}
		if (advertiser == null) {
			throw new JoZException("No advertiser specified in request or tspec to get the listing data");
		}

		String jsonStr = null;
		ListingResponse response = null;

		if (handles.size() > 0) {
			long[] pids = new long[handles.size()];

			for (int i = 0; i < handles.size(); i++) {
				pids[i] = handles.get(i).getOid();
			}

			ListingProvider _prov = ListingProviderFactory.getProviderInstance(AdvertiserTaxonomyMapperImpl.getInstance(),
					AdvertiserMerchantDataMapperImpl.getInstance());
			response = _prov.getListing(advertiser, pids, (maxDescLength != null) ? maxDescLength.intValue() : 0, null);
			if (response == null) {
				throw new JoZException("Invalid response from Listing Provider");
			}

			jsonStr = response.getListingDetails();

		}
		if (jsonStr == null || "".equals(jsonStr)) {
			throw new JozMonitorException("Products not found.");
		}
		StringBuffer rawData = new StringBuffer();
		rawData.append("[PRODUCTS = " + jsonStr + "] ");
		rawData.append("[PROD-IDS = " + response.getProductIdList() + "] ");
		rawData.append("[CATEGORIES = " + response.getCatDetails() + "] ");
		rawData.append("[CAT-NAMES = " + response.getCatIdList() + "] ");

		((ProductQueryMonitorStatus) status.getStatus()).setProductRawData(rawData.toString());

		//jsonStr = jsonStr.replaceAll("\\\\\\\\\"","\\\\\\\\\\\\\"");
		try {
			JSONArray jsonArray = new JSONArray(jsonStr);
			for (int i = 0; i < jsonArray.length(); i++) {
				Map<String, String> attributes = new HashMap<String, String>();
				JSONObject jsonObj = (JSONObject) jsonArray.get(i);
				Iterator it = jsonObj.keys();
				String key = null;
				String value = null;
				while (it.hasNext()) {
					key = (String) it.next();
					value = (String) jsonObj.get(key);
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

	private static ProductSelectionRequest generateProductSelectionRequest(String req) {
		ProductSelectionRequest pr = new ProductSelectionRequest();
		HashSet<String> keys = new HashSet<String>();
		keys.add(":num_products");
		keys.add(":city");
		keys.add(":state");
		keys.add(":country");
		keys.add(":zipcode");
		keys.add(":dmacode");
		keys.add(":areacode");
		keys.add(":brandomize");
		keys.add(":bmineurls");
		keys.add(":bpaginate");
		keys.add(":requestkeywords");
		keys.add(":requestcategory");
		keys.add(":f1");
		keys.add(":f2");
		keys.add(":f3");
		keys.add(":f4");
		keys.add(":f5");
		keys.add(":ut1");
		keys.add(":ut2");
		keys.add(":ut3");
		keys.add(":ut4");
		keys.add(":ut5");
		keys.add(":age");
		keys.add(":gender");
		keys.add(":hhi");
		keys.add(":ms");
		keys.add(":bt");
		keys.add(":advertiser");
		keys.add(":topk");

		if (req == null || "".equals(req.trim())) {
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
			for (int i = 0; i < args.size(); i++) {
				if (i == (args.size() - 1)) {
					if (!"".equals(value)) {
						value += " ";
					}
					value += cToken;
					cToken = key;
					if (cToken != null) {
						cToken = cToken.trim();
					}
				}
				if (keys.contains(cToken)) {
					if (":num_products".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setPageSize(Integer.parseInt(value.trim()));
						}
					} else if (":city".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setCityCode(value.trim().toLowerCase());
						}
					} else if (":state".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setStateCode(value.trim().toLowerCase());
						}
					} else if (":country".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setCountryCode(value.trim().toLowerCase());
						}
					} else if (":zipcode".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setZipCode(value.trim().toLowerCase());
						}
					} else if (":dmacode".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setDmaCode(value.trim().toLowerCase());
						}
					} else if (":areacode".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setAreaCode(value.trim().toLowerCase());
						}
					} else if (":brandomize".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim().trim())) {
								pr.setBRandomize(false);
							} else {
								pr.setBRandomize(true);
							}
						}
					} else if (":mineurls".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim().trim())) {
								pr.setBMineUrls(false);
							} else {
								pr.setBMineUrls(true);
							}
						}
					} else if (":bpaginate".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim().trim())) {
								pr.setBPaginate(false);
							} else {
								pr.setBPaginate(true);
							}
						}
					} else if (":requestkeywords".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setRequestKeyWords(value.trim());
						}
					} else if (":requestcategory".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setRequestCategory(value.trim());
						}
					} else if (":f1".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setExternalFilterQuery1(value.trim());
						}
					} else if (":f2".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setExternalFilterQuery2(value.trim());
						}
					} else if (":f3".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setExternalFilterQuery3(value.trim());
						}
					} else if (":f4".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setExternalFilterQuery4(value.trim());
						}
					} else if (":f5".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setExternalFilterQuery5(value.trim());
						}
					} else if (":ut1".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setUt1(value.trim());
						}
					} else if (":ut2".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setUt2(value.trim());
						}
					} else if (":ut3".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setUt3(value.trim());
						}
					} else if (":ut4".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setUt4(value.trim());
						}
					} else if (":ut5".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setUt5(value.trim());
						}
					} else if (":age".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setAge(value.trim());
						}
					} else if (":gender".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setGender(value.trim());
						}
					} else if (":hhi".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setHhi(value.trim());
						}
					} else if (":ms".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setCC(value.trim());
						}
					} else if (":bt".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setBt(value.trim());
						}
					} else if (":advertiser".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							pr.setAdvertiser(value.trim());
						}
					} else if (":topk".equalsIgnoreCase(key)) {
						if ("true".equals(value.trim())) {
							pr.setUseTopK(true);
						} else {
							pr.setUseTopK(false);
						}
					}
				} else {
					if (!"".equals(value)) {
						value += " ";
					}
					value += cToken;
					cToken = args.get(i);
					if (cToken != null) {
						cToken = cToken.trim();
					}
					continue;
				}
				value = "";
				key = cToken;
				cToken = args.get(i);
				if (cToken != null) {
					cToken = cToken.trim();
				}
			}
		}
		if (pr.getPageSize() <= 0) {
			pr.setPageSize(100);
		}
		pr.setCurrPage(0);

		return pr;
	}

	private static TSpec generateTSpec(String req) {
		TSpec tSpec = new TSpec();
		HashSet<String> keys = new HashSet<String>();
		keys.add(":usef1");
		keys.add(":usef2");
		keys.add(":usef3");
		keys.add(":usef4");
		keys.add(":usef5");
		keys.add(":useut1");
		keys.add(":useut2");
		keys.add(":useut3");
		keys.add(":useut4");
		keys.add(":useut5");
		keys.add(":useage");
		keys.add(":usegender");
		keys.add(":usehhi");
		keys.add(":usems");
		keys.add(":usebt");
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
		keys.add(":LTKExpression");
		keys.add(":producttype");
		keys.add(":includedglobalids");
		keys.add(":excludedglobalids");
		keys.add(":enablebackfill");
		keys.add(":keywordSrc");
		keys.add(":sortrank");
		keys.add(":sortdiscount");
		keys.add(":f1Score");
		keys.add(":f2Score");
		keys.add(":f3Score");
		keys.add(":f4Score");
		keys.add(":f5Score");
		keys.add(":ut1Score");
		keys.add(":ut2Score");
		keys.add(":ut3Score");
		keys.add(":ut4Score");
		keys.add(":ut5Score");
		keys.add(":prodSelFuzz");

		if (req != null && !"".equals(req.trim())) {
			com.tumri.utils.strings.StringTokenizer reqTokenizer = new com.tumri.utils.strings.StringTokenizer(req, ' ');
			String key = "";
			String value = "";
			String cToken = "";
			ArrayList<String> args = reqTokenizer.getTokens();
			args.add(null);
			for (int i = 0; i < args.size(); i++) {
				if (i == (args.size() - 1)) {
					if (!"".equals(value)) {
						value += " ";
					}
					value += cToken;
					cToken = key;
					if (cToken != null) {
						cToken = cToken.trim();
					}
				}
				if (keys.contains(cToken)) {
					if (":usef1".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setUseListingFilter1(false);
							} else {
								tSpec.setUseListingFilter1(true);
							}
						}
					} else if (":usef2".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setUseListingFilter2(false);
							} else {
								tSpec.setUseListingFilter2(true);
							}
						}
					} else if (":usef3".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setUseListingFilter3(false);
							} else {
								tSpec.setUseListingFilter3(true);
							}
						}
					} else if (":usef4".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setUseListingFilter4(false);
							} else {
								tSpec.setUseListingFilter4(true);
							}
						}
					} else if (":usef5".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setUseListingFilter5(false);
							} else {
								tSpec.setUseListingFilter5(true);
							}
						}
					} else if (":useut1".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setUseUT1(false);
							} else {
								tSpec.setUseUT1(true);
							}
						}
					} else if (":useut2".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setUseUT2(false);
							} else {
								tSpec.setUseUT2(true);
							}
						}
					} else if (":useut3".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setUseUT3(false);
							} else {
								tSpec.setUseUT3(true);
							}
						}
					} else if (":useut4".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setUseUT4(false);
							} else {
								tSpec.setUseUT4(true);
							}
						}
					} else if (":useut5".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setUseUT5(false);
							} else {
								tSpec.setUseUT5(true);
							}
						}
					} else if (":useage".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setUseAgeFilter(false);
							} else {
								tSpec.setUseAgeFilter(true);
							}
						}
					} else if (":usegender".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setUseGenderFilter(false);
							} else {
								tSpec.setUseGenderFilter(true);
							}
						}
					} else if (":usehhi".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setUseHHIFilter(false);
							} else {
								tSpec.setUseHHIFilter(true);
							}
						}
					} else if (":usems".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setUseMSFilter(false);
							} else {
								tSpec.setUseMSFilter(true);
							}
						}
					} else if (":usebt".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setUseBTFilter(false);
							} else {
								tSpec.setUseBTFilter(true);
							}
						}
					} else if (":enablebackfill".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setEnableBackFill(false);
							} else {
								tSpec.setEnableBackFill(true);
							}
						}
					} else if (":useradiusquery".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setUseRadiusQuery(false);
							} else {
								tSpec.setUseRadiusQuery(true);
							}
						}
					} else if (":radius".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							tSpec.setRadius(Integer.parseInt(value.trim()));
						}
					} else if (":minepuburl".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setMinePubUrl(false);
							} else {
								tSpec.setMinePubUrl(true);
							}
						}
					} else if (":lowprice".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							tSpec.setLowPrice(Integer.parseInt(value.trim()));
						}
					} else if (":highprice".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							tSpec.setHighPrice(Integer.parseInt(value.trim()));
						}
					} else if (":applygeofilter".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setApplyGeoFilter(false);
							} else {
								tSpec.setApplyGeoFilter(true);
							}
						}
					} else if (":applyurlfilter".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setApplyUrlFilter(false);
							} else {
								tSpec.setApplyUrlFilter(true);
							}
						}
					} else if (":applykeywordfilter".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("false".equalsIgnoreCase(value.trim())) {
								tSpec.setApplyKeywordFilter(false);
							} else {
								tSpec.setApplyKeywordFilter(true);
							}
						}
					} else if (":includedproviders".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value.trim(), ',');
							ArrayList<String> incProvs = st.getTokens();
							for (String prov : incProvs) {
								ProviderInfo provInfo = new ProviderInfo(prov.trim());
								tSpec.addIncludedProviders(provInfo);
							}
						}
					} else if (":excludedproviders".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value.trim(), ',');
							ArrayList<String> exProvs = st.getTokens();
							for (String prov : exProvs) {
								ProviderInfo provInfo = new ProviderInfo(prov.trim());
								tSpec.addExcludedProviders(provInfo);
							}
						}
					} else if (":excludedcategories".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value.trim(), ',');
							ArrayList<String> exCats = st.getTokens();
							for (String cat : exCats) {
								CategoryInfo catInfo = new CategoryInfo(cat.trim(), cat.trim());
								tSpec.addExcludedCategories(catInfo);
							}
						}
					} else if (":includedcategories".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value.trim(), ',');
							ArrayList<String> incCats = st.getTokens();
							for (String cat : incCats) {
								CategoryInfo catInfo = new CategoryInfo(cat.trim(), cat.trim());
								tSpec.addIncludedCategories(catInfo);
							}
						}
					} else if (":includedbrands".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value.trim(), ',');
							ArrayList<String> incbrands = st.getTokens();
							for (String brand : incbrands) {
								BrandInfo brandInfo = new BrandInfo(brand.trim(), brand.trim());
								tSpec.addIncludedBrand(brandInfo);
							}
						}
					} else if (":excludedbrands".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value.trim(), ',');
							ArrayList<String> exbrands = st.getTokens();
							for (String brand : exbrands) {
								BrandInfo brandInfo = new BrandInfo(brand.trim(), brand.trim());
								tSpec.addExcludedBrand(brandInfo);
							}
						}
					} else if (":excludedmerchants".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value.trim(), ',');
							ArrayList<String> exMerchs = st.getTokens();
							for (String merch : exMerchs) {
								MerchantInfo merchInfo = new MerchantInfo(merch.trim());
								tSpec.addExcludedMerchant(merchInfo);
							}
						}
					} else if (":includedmerchants".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value.trim(), ',');
							ArrayList<String> incMerchs = st.getTokens();
							for (String merch : incMerchs) {
								MerchantInfo merchInfo = new MerchantInfo(merch.trim());
								tSpec.addIncludedMerchant(merchInfo);
							}
						}
					} else if (":includedproducts".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value.trim(), ',');
							ArrayList<String> incProds = st.getTokens();
							for (String prod : incProds) {
								ProductInfo prodInfo = new ProductInfo(prod.trim());
								tSpec.addIncludedProducts(prodInfo);
							}
						}
					} else if (":excludedproducts".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value.trim(), ',');
							ArrayList<String> exProds = st.getTokens();
							for (String prod : exProds) {
								ProductInfo prodInfo = new ProductInfo(prod.trim());
								tSpec.addExcludedProducts(prodInfo);
							}
						}
					} else if (":includedglobalids".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value.trim(), ',');
							ArrayList<String> inclGids = st.getTokens();
							for (String gid : inclGids) {
								GlobalIdInfo ginfo = new GlobalIdInfo(gid.trim());
								tSpec.addIncludedGlobalIds(ginfo);
							}
						}
					} else if (":excludedglobalids".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							com.tumri.utils.strings.StringTokenizer st = new com.tumri.utils.strings.StringTokenizer(value.trim(), ',');
							ArrayList<String> inclGids = st.getTokens();
							for (String gid : inclGids) {
								GlobalIdInfo ginfo = new GlobalIdInfo(gid.trim());
								tSpec.addExcludedGlobalIds(ginfo);
							}
						}
					} else if (":LTKExpression".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							tSpec.setLoadTimeKeywordExpression(value.trim());
						}
					} else if (":producttype".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							tSpec.setSpecType(value.trim());
						}
					} else if (":f1Score".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							Map<String, Double> map = tSpec.getProdSelWeights();
							if (map == null) {
								map = new HashMap<String, Double>();
							}
							map.put(TSpec.ProdAttribute.f1.name(), Double.valueOf(value.trim()));
							tSpec.setProdSelWeights(map);
						}
					} else if (":f2Score".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							Map<String, Double> map = tSpec.getProdSelWeights();
							if (map == null) {
								map = new HashMap<String, Double>();
							}
							map.put(TSpec.ProdAttribute.f2.name(), Double.valueOf(value.trim()));
							tSpec.setProdSelWeights(map);
						}
					} else if (":f3Score".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							Map<String, Double> map = tSpec.getProdSelWeights();
							if (map == null) {
								map = new HashMap<String, Double>();
							}
							map.put(TSpec.ProdAttribute.f3.name(), Double.valueOf(value.trim()));
							tSpec.setProdSelWeights(map);
						}
					} else if (":f4Score".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							Map<String, Double> map = tSpec.getProdSelWeights();
							if (map == null) {
								map = new HashMap<String, Double>();
							}
							map.put(TSpec.ProdAttribute.f4.name(), Double.valueOf(value.trim()));
							tSpec.setProdSelWeights(map);
						}
					} else if (":f5Score".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							Map<String, Double> map = tSpec.getProdSelWeights();
							if (map == null) {
								map = new HashMap<String, Double>();
							}
							map.put(TSpec.ProdAttribute.f5.name(), Double.valueOf(value.trim()));
							tSpec.setProdSelWeights(map);
						}
					} else if (":ut1Score".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							Map<String, Double> map = tSpec.getProdSelWeights();
							if (map == null) {
								map = new HashMap<String, Double>();
							}
							map.put(TSpec.ProdAttribute.ut1.name(), Double.valueOf(value.trim()));
							tSpec.setProdSelWeights(map);
						}
					} else if (":ut2Score".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							Map<String, Double> map = tSpec.getProdSelWeights();
							if (map == null) {
								map = new HashMap<String, Double>();
							}
							map.put(TSpec.ProdAttribute.ut2.name(), Double.valueOf(value.trim()));
							tSpec.setProdSelWeights(map);
						}
					} else if (":ut3Score".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							Map<String, Double> map = tSpec.getProdSelWeights();
							if (map == null) {
								map = new HashMap<String, Double>();
							}
							map.put(TSpec.ProdAttribute.ut3.name(), Double.valueOf(value.trim()));
							tSpec.setProdSelWeights(map);
						}
					} else if (":ut4Score".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							Map<String, Double> map = tSpec.getProdSelWeights();
							if (map == null) {
								map = new HashMap<String, Double>();
							}
							map.put(TSpec.ProdAttribute.ut4.name(), Double.valueOf(value.trim()));
							tSpec.setProdSelWeights(map);
						}
					} else if (":ut5Score".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							Map<String, Double> map = tSpec.getProdSelWeights();
							if (map == null) {
								map = new HashMap<String, Double>();
							}
							map.put(TSpec.ProdAttribute.ut5.name(), Double.valueOf(value.trim()));
							tSpec.setProdSelWeights(map);
						}
					} else if (":keywordSrc".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							tSpec.setKeywordSource(value.trim());
						}
					} else if (":prodSelFuzz".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							tSpec.setProdSelFuzzFactor(Double.valueOf(value.trim()));
						}
					} else if (":sortrank".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("true".equalsIgnoreCase(value.trim())) {
								tSpec.setSortByRank(true);
							} else {
								tSpec.setSortByRank(false);
							}
						}
					} else if (":sortdiscount".equalsIgnoreCase(key)) {
						if (!"".equals(value.trim())) {
							if ("true".equalsIgnoreCase(value.trim())) {
								tSpec.setSortByDiscount(true);
							} else {
								tSpec.setSortByDiscount(false);
							}
						}
					}
				} else {
					if (!"".equals(value)) {
						value += " ";
					}
					value += cToken;
					cToken = args.get(i);
					if (cToken != null) {
						cToken = cToken.trim();
					}
					continue;
				}
				value = "";
				key = cToken;
				cToken = args.get(i);
				if (cToken != null) {
					cToken = cToken.trim();
				}
			}
		}

		return tSpec;
	}

}
