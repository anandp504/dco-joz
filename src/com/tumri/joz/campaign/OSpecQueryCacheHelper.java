package com.tumri.joz.campaign;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.tumri.cma.domain.BrandInfo;
import com.tumri.cma.domain.CategoryInfo;
import com.tumri.cma.domain.MerchantInfo;
import com.tumri.cma.domain.OSpec;
import com.tumri.cma.domain.ProviderInfo;
import com.tumri.cma.domain.TSpec;
import com.tumri.joz.Query.AttributeQuery;
import com.tumri.joz.Query.CNFQuery;
import com.tumri.joz.Query.ConjunctQuery;
import com.tumri.joz.Query.KeywordQuery;
import com.tumri.joz.Query.ProductQueryProcessor;
import com.tumri.joz.Query.RangeQuery;
import com.tumri.joz.Query.SimpleQuery;
import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.products.IProduct;

public class OSpecQueryCacheHelper {

	/**
	 * Walk thru the OSpec details and create the Query
	 * This method should ideally be private to this class, if not it should be moved to a utils class.
	 * @param oSpec
	 * @return
	 */
	public static CNFQuery getQuery(OSpec oSpec) {
		CNFQuery _query = new CNFQuery ();
		ConjunctQuery _cjquery = new ConjunctQuery (new ProductQueryProcessor());
		List<TSpec> tSpecList = oSpec.getTspecs();
		Iterator<TSpec> tSpecIter = tSpecList.iterator();
		while (tSpecIter.hasNext()){
			TSpec theTSpec = tSpecIter.next();
			//Keyword
			String keywordExp = theTSpec.getLoadTimeKeywordExpression();
			if (keywordExp != null){
				KeywordQuery kwQuery = new KeywordQuery(keywordExp,true);
				_cjquery.addQuery(kwQuery);
			}

			//Excluded Brand
			List<BrandInfo> bixList = theTSpec.getExcludedBrands();
			if (bixList != null) {
				SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kBrand, bixList, true);
				_cjquery.addQuery(sq);
			}

			//Included brand
			List<BrandInfo> binList = theTSpec.getIncludedBrands();
			if (binList != null) {
				SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kBrand, binList, false);
				_cjquery.addQuery(sq);
			}			  

			//Include cats
			List<CategoryInfo> cinList = theTSpec.getIncludedCategories();
			if (cinList != null) {
				SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kCategory, cinList, false);
				_cjquery.addQuery(sq);
			}

			//Excluded cats
			List<CategoryInfo> cexList = theTSpec.getExcludedCategories();
			if (cexList != null) {
				SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kCategory, cexList, true);
				_cjquery.addQuery(sq);
			}

			//Included merchants
			List<MerchantInfo> inMerchants = theTSpec.getIncludedMerchants();
			if (inMerchants != null) {
				SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kSupplier, inMerchants, false);
				_cjquery.addQuery(sq);				  
			}

			//Excluded merchants
			List<MerchantInfo> exMerchants = theTSpec.getExcludedMerchants();
			if (exMerchants != null) {
				SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kSupplier, exMerchants, true);
				_cjquery.addQuery(sq);				  
			}

			//Included Providers
			List<ProviderInfo> inProviders = theTSpec.getIncludedProviders();
			if (inProviders != null) {
				SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kProvider, inProviders, false);
				_cjquery.addQuery(sq);				  
			}

			//Excluded Providers
			List<ProviderInfo> exProviders = theTSpec.getExcludedProviders();
			if (exProviders != null) {
				SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kProvider, exProviders, true);
				_cjquery.addQuery(sq);				  
			}	

			//CPC Range
			double highCPC = theTSpec.getHighCPC();
			double lowCPC = theTSpec.getLowCPC();
			if ((highCPC > 0) || ( lowCPC > 0)) {
				SimpleQuery sq = new RangeQuery (IProduct.Attribute.kCPC,lowCPC, highCPC);
				_cjquery.addQuery(sq);
			}

			//CPO Range
			double highCPO = theTSpec.getHighCPO();
			double lowCPO = theTSpec.getLowCPO();
			if ((highCPO > 0) || ( lowCPO > 0)) {
				SimpleQuery sq = new RangeQuery (IProduct.Attribute.kCPO,lowCPO, highCPO);
				_cjquery.addQuery(sq);
			}


			//Price Range
			double highPrice = theTSpec.getHighCPC();
			double lowPrice = theTSpec.getLowCPC();
			if ((highPrice > 0) || ( lowPrice > 0)) {
				SimpleQuery sq = new RangeQuery (IProduct.Attribute.kPrice,lowPrice, highPrice);
				_cjquery.addQuery(sq);
			}

		}

		_query.addQuery (_cjquery);
		return _query;
	}


	/**
	 * Helper method that Builds a Simplequery based on the values passed in
	 * @param type
	 * @param values
	 * @return
	 */
	private static SimpleQuery buildAttributeQuery(IProduct.Attribute type, List values, boolean bNegation) {
		ArrayList<Integer> valueIdList = new ArrayList<Integer>(values.size()); 
		ArrayList<String> valueStrList = new ArrayList<String>(values.size()); 
		if (values != null) {
			for (int i=0;i<values.size();i++){
				if (type.equals(IProduct.Attribute.kCategory)) {
					CategoryInfo cInfo = (CategoryInfo)values.get(i);
					valueStrList.add(cInfo.getName());
				} else if (type.equals(IProduct.Attribute.kBrand)) {
					BrandInfo bInfo = (BrandInfo)values.get(i);
					valueStrList.add(bInfo.getName());
				} else if (type.equals(IProduct.Attribute.kProvider)) {
					ProviderInfo pInfo = (ProviderInfo)values.get(i);
					valueStrList.add(pInfo.getName());
				} else if (type.equals(IProduct.Attribute.kSupplier)) {
					MerchantInfo mInfo = (MerchantInfo)values.get(i);
					valueStrList.add(mInfo.getName());
				}
			}
			for (int i=0;i<valueStrList.size();i++){
				String valueStr = valueStrList.get(i);
				DictionaryManager dm = DictionaryManager.getInstance ();
				Integer brandId = dm.getId (type, valueStr);
				valueIdList.add(brandId);
			}				
		}
		SimpleQuery sq = new AttributeQuery (type, valueIdList);
		sq.setNegation(bNegation);
		return sq;
	}

}
