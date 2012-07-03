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
package com.tumri.joz.server.handlers;

import com.tumri.cma.domain.OSpec;
import com.tumri.cma.domain.TSpec;
import com.tumri.content.TaxonomyProvider;
import com.tumri.content.data.Category;
import com.tumri.content.data.dictionary.DictionaryManager;
import com.tumri.content.data.impl.AdvertiserTaxonomyMapperImpl;
import com.tumri.joz.JoZException;
import com.tumri.joz.Query.*;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.OSpecHelper;
import com.tumri.joz.campaign.TSpecHelper;
import com.tumri.joz.campaign.TSpecQueryCache;
import com.tumri.joz.campaign.TSpecQueryCacheHelper;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.ranks.AttributeWeights;
import com.tumri.utils.data.SortedArraySet;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Helper class to house the logic of the counts
 * @author: nipun
 * Date: Jun 18, 2008
 * Time: 11:05:13 AM
 */
public class CountsHelper {

	private static Logger log = Logger.getLogger (CountsHelper.class);

	@SuppressWarnings("unchecked")
	public static HashMap<String, Counter>[] getCounters(String tspecName) throws JoZException {

		HashMap<String, Counter>[] retVal = new HashMap[3];
		HashMap<String, Counter> category_counts = new HashMap<String, Counter>();
		HashMap<String, Counter> brand_counts = new HashMap<String, Counter>();
		HashMap<String, Counter> provider_counts = new HashMap<String, Counter>();
		retVal[0] = category_counts;
		retVal[1] = brand_counts;
		retVal[2] = provider_counts;

		if (tspecName != null) {
			OSpec oSpec = CampaignDB.getInstance().getOspec(tspecName);
			List<TSpec> tspecList = oSpec.getTspecs();
			if (tspecList == null || tspecList.isEmpty()) {
				log.error("t-spec " + tspecName + " not found.");
				throw new JoZException("t-spec " + tspecName + " not found.");
			}

			for (TSpec tspec: tspecList) {
				int tspecId = tspec.getId();
				CNFQuery query = TSpecQueryCache.getInstance().getCNFQuery(tspecId);
				if (query == null) {
					log.error("t-spec " + tspecName + " not found.");
					throw new JoZException("t-spec " + tspecName + " not found.");
				}
				getOSpecAttributeCount(provider_counts,query, IProduct.Attribute.kProvider);
				getOSpecAttributeCount(brand_counts, query,IProduct.Attribute.kBrand);
				getOSpecAttributeCount(category_counts, query,IProduct.Attribute.kCategory);
			}
			OSpec ospec = CampaignDB.getInstance().getOspec(tspecName);
			ArrayList<Handle> includedProducts = null;
			if (ospec !=null) {
				includedProducts = OSpecHelper.getIncludedProducts(ospec);
			}
			if (includedProducts!=null && includedProducts.size() > 0) {
				SortedSet<Handle> sortedInclProds = new SortedArraySet<Handle>();
				sortedInclProds.addAll(includedProducts);

				getIncludedProductAttributeCount(provider_counts, sortedInclProds, IProduct.Attribute.kProvider);
				getIncludedProductAttributeCount(brand_counts, sortedInclProds,IProduct.Attribute.kBrand);
				getIncludedProductAttributeCount(category_counts, sortedInclProds,IProduct.Attribute.kCategory);
			}
		} else {
			//Provider Counts
			getGlobalAttributeCount(provider_counts, IProduct.Attribute.kProvider);
			//Brand Counts
			getGlobalAttributeCount(brand_counts, IProduct.Attribute.kBrand);
			//Category Counts
			getGlobalAttributeCount(category_counts, IProduct.Attribute.kCategory);
		}

		return retVal;

	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, Counter>[] getCounters(TSpec tSpec) throws JoZException {
		HashMap<String, Counter>[] retVal = new HashMap[3];
		HashMap<String, Counter> category_counts = new HashMap<String, Counter>();
		HashMap<String, Counter> brand_counts = new HashMap<String, Counter>();
		HashMap<String, Counter> provider_counts = new HashMap<String, Counter>();

		retVal[0] = category_counts;
		retVal[1] = brand_counts;
		retVal[2] = provider_counts;
		CNFQuery cnfQuery = TSpecQueryCacheHelper.getQuery(tSpec);

		getOSpecAttributeCount(provider_counts, cnfQuery, IProduct.Attribute.kProvider);
		getOSpecAttributeCount(brand_counts, cnfQuery,IProduct.Attribute.kBrand);
		getOSpecAttributeCount(category_counts, cnfQuery,IProduct.Attribute.kCategory);

		ArrayList<Handle> includedProducts = TSpecHelper.getIncludedProducts(tSpec);

		if (includedProducts!=null && includedProducts.size() > 0) {
			SortedSet<Handle> sortedInclProds = new SortedArraySet<Handle>();
			sortedInclProds.addAll(includedProducts);
			getIncludedProductAttributeCount(provider_counts, sortedInclProds, IProduct.Attribute.kProvider);
			getIncludedProductAttributeCount(brand_counts, sortedInclProds,IProduct.Attribute.kBrand);
			getIncludedProductAttributeCount(category_counts, sortedInclProds,IProduct.Attribute.kCategory);
		}
		return retVal;
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, Counter>[] getCounters(int tSpecId) throws JoZException {
		HashMap<String, Counter>[] retVal = new HashMap[3];
		HashMap<String, Counter> category_counts = new HashMap<String, Counter>();
		HashMap<String, Counter> brand_counts = new HashMap<String, Counter>();
		HashMap<String, Counter> provider_counts = new HashMap<String, Counter>();

		retVal[0] = category_counts;
		retVal[1] = brand_counts;
		retVal[2] = provider_counts;
		CNFQuery cnfQuery = TSpecQueryCache.getInstance().getCNFQuery(tSpecId);

		getOSpecAttributeCount(provider_counts, cnfQuery, IProduct.Attribute.kProvider);
		getOSpecAttributeCount(brand_counts, cnfQuery,IProduct.Attribute.kBrand);
		getOSpecAttributeCount(category_counts, cnfQuery,IProduct.Attribute.kCategory);
		TSpec tSpec = CampaignDB.getInstance().getTspec(tSpecId);
		ArrayList<Handle> includedProducts = TSpecHelper.getIncludedProducts(tSpec);
		if (includedProducts!=null && includedProducts.size() > 0) {
			SortedSet<Handle> sortedInclProds = new SortedArraySet<Handle>();
			sortedInclProds.addAll(includedProducts);

			getIncludedProductAttributeCount(provider_counts, sortedInclProds, IProduct.Attribute.kProvider);
			getIncludedProductAttributeCount(brand_counts, sortedInclProds,IProduct.Attribute.kBrand);
			getIncludedProductAttributeCount(category_counts, sortedInclProds,IProduct.Attribute.kCategory);

		}
		return retVal;

	}

	/**
	 * Compute the global attribute counts for the given attribute
	 * @param attrCounts Hashmap to which the counts needs to be added
	 * @param kAttr  the current product attribute
	 * @return HashMap<String, Counter>
	 */
	@SuppressWarnings("unchecked")
	private static HashMap<String, Counter> getGlobalAttributeCount(HashMap<String, Counter> attrCounts,
	                                                                IProduct.Attribute kAttr) {
		ProductAttributeIndex<Integer,Handle> ai = ProductDB.getInstance().getIndex(kAttr);
		if (ai != null) {
			Set<Integer> keySet = ai.getKeys();
			for (Integer theKey: keySet) {
				String keyStrVal = DictionaryManager.getValue(kAttr, theKey);
				incrementCounter(ai.getCount(theKey),attrCounts, keyStrVal, kAttr);
			}
		}
		return attrCounts;
	}

	/**
	 * Get the OSpec attribute count for the given OSpec
	 * @param attrCounts Hashmap to which the counts needs to be added
	 * @param query CNFQuery
	 * @param kAttr  the current product attribute
	 * @return HashMap<String, Counter>
	 * @throws JoZException - from any called statements
	 */
	@SuppressWarnings("unchecked")
	private static HashMap<String, Counter> getOSpecAttributeCount(HashMap<String, Counter> attrCounts,
	                                                               CNFQuery query, IProduct.Attribute kAttr) throws JoZException{
		ProductAttributeIndex<Integer,Handle> ai = ProductDB.getInstance().getIndex(kAttr);

		ArrayList<ConjunctQuery> conjQueries = query.getQueries();
		if (ai != null) {
			for(ConjunctQuery cq: conjQueries) {
				//Check if there are any queries in this
				if (cq.getQueries().size()==0) {
					continue;
				}
				Set<Integer> keySet = ai.getKeys();

				for (Integer theKey: keySet) {
					String keyStrVal = DictionaryManager.getValue(kAttr, theKey);
					SimpleQuery sq;
					if (kAttr == IProduct.Attribute.kCategory) {
						sq = new CategoryQuery(theKey);
					} else {
						sq = new AttributeQuery(kAttr, theKey);
					}
					CNFQuery tmpQuery = new CNFQuery();
					ConjunctQuery tmpCq = (ConjunctQuery)cq.clone();
					tmpCq.addQuery(sq);
					tmpQuery.addQuery(tmpCq);
					tmpQuery.setStrict(true);
					tmpQuery.setBounds(0, 0);
					SortedSet<Handle> results = tmpQuery.exec();
					if (results.size() > 0) {
						incrementCounter(results.size(), attrCounts, keyStrVal, kAttr);
					}
				}

			}
		}
		return attrCounts;
	}

	/**
	 * For the given set of included product, return the count of the attribute
	 * @param attrCounts  - map of counters
	 * @param sortedInclProds  - incl prods
	 * @param kAttr        - product attr
	 * @return map of counters
	 */
	@SuppressWarnings("unchecked")
	private static HashMap<String, Counter> getIncludedProductAttributeCount(HashMap<String, Counter> attrCounts,
	                                                                         SortedSet<Handle> sortedInclProds,
	                                                                         IProduct.Attribute kAttr) {
		ProductAttributeIndex<Integer,Handle> ai = ProductDB.getInstance().getIndex(kAttr);
		if (ai != null) {
			Set<Integer> keySet = ai.getKeys();
			for (Integer theKey: keySet) {
				String keyStrVal = DictionaryManager.getValue(kAttr, theKey);
				//Intersect between the 2 sets
				ProductSetIntersector aIntersector;
				aIntersector = new ProductSetIntersector(true);
				aIntersector.include(ai.get(theKey), AttributeWeights.getWeight(kAttr));
				aIntersector.include(sortedInclProds, AttributeWeights.getWeight(kAttr));
				aIntersector.setMax(0);
				SortedSet<Handle> results = aIntersector.intersect();
				//Walk thru the loop - to avoid the warning of size() on SetIntersector
				for (int i=0;i<results.size();i++) {
					incrementCounter(attrCounts, keyStrVal, kAttr);
				}
			}
		}
		return attrCounts;
	}

	/**
	 * Convinence method to increment the count by 1
	 * @param attrCounts  - map of counters
	 * @param keyStrVal  - key value
	 * @param kAttr        - product attr
	 */
	private static void incrementCounter(HashMap<String, Counter> attrCounts, String keyStrVal, IProduct.Attribute kAttr) {
		incrementCounter(1, attrCounts, keyStrVal, kAttr);
	}

	/**
	 * Increment the counter taking into consideration the special case for Category, where parent counts are also
	 * to be included.
	 * @param size  - size
	 * @param attrCounts  - map of counters
	 * @param keyStrVal  - key value
	 * @param kAttr        - product attr
	 */
	private static void incrementCounter(int size, HashMap<String, Counter> attrCounts, String keyStrVal, IProduct.Attribute kAttr) {
		if (keyStrVal != null) {
			if (kAttr == IProduct.Attribute.kCategory) {
				Collection<TaxonomyProvider> ctp = AdvertiserTaxonomyMapperImpl.getInstance().getAllTaxonomyProviders();
				for(TaxonomyProvider tp: ctp){
					Category cat = tp.getTaxonomy().getCategory(keyStrVal);
					if (cat != null) {
						incrementCategoryCount(size, keyStrVal, attrCounts);
					}
				}
			} else {
				Counter ctr = getCounter(attrCounts,keyStrVal);
				ctr.inc(size);
			}
		}
	}

	/**
	 * Increment the count for the category, as well as all its parents
	 * @param size  - size
	 * @param catIdStr  - id value
	 * @param attrCounts  - map of counters
	 */
	private static void incrementCategoryCount(int size, String catIdStr, HashMap<String, Counter> attrCounts) {
		List<String> categories = new ArrayList<String>();
		categories.add(catIdStr);
		categories = getAllCategories(categories);
		if (categories!=null) {
			for (String cat : categories) {
				Counter ctr = getCounter(attrCounts,cat);
				ctr.inc(size);
			}
		}

	}

	/**
	 * Return list of all categories in cats and their parents.
	 * @param cats input list
	 * @return list of cats
	 */
	private static List<String> getAllCategories(List<String> cats) {
		List<String> result = new ArrayList<String>();
		Collection<TaxonomyProvider> ctp = AdvertiserTaxonomyMapperImpl.getInstance().getAllTaxonomyProviders();
		for(TaxonomyProvider tp: ctp){
			for (String c : cats) {
				HashSet<Integer> idSet = new HashSet<Integer>();

				Category p = tp.getTaxonomy().getCategory(c);
				while (p != null && !idSet.contains(p.getGlassId())) {
					idSet.add(p.getGlassId());
					//result.add(p.getGlassIdStr());
					result.add(p.getName());
					p = p.getParent();
				}
			}
		}
		return result;
	}



	private static Counter getCounter(HashMap<String, Counter> counts, String key) {
		if (counts == null) {
			return null;
		}
		Counter ctr = counts.get(key);
		if (ctr == null) {
			ctr = new Counter(0);
			counts.put(key,ctr);
		}
		return ctr;
	}

	public static class Counter {

		int count;

		public Counter(int i) {
			count = i;
		}

		public void inc() {
			++count;
		}

		public void inc(int ctr) {
			count = count + ctr;
		}

		public void dec(int ctr) {
			count = count - ctr;
		}

		public int get() {
			return count;
		}
	}

}
