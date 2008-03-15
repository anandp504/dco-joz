package com.tumri.joz.campaign;

import com.tumri.cma.domain.*;
import com.tumri.content.data.CategoryAttributeDetails;
import com.tumri.content.data.Product;
import com.tumri.joz.Query.*;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.utils.IndexUtils;
import com.tumri.joz.index.DictionaryManager;
import com.tumri.utils.strings.StringTokenizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        if(tSpecList != null) {
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

                    //Category Field Attributes
                    for (CategoryInfo ci: cinList) {
                        String catId = ci.getName();
                        CategoryInfoAttributes cia = ci.getAttribs();
                        if (cia != null) {
                            //There are attribs for this query
                            List<CategoryAttribute> attribs = cia.getAttribs();
                            for (CategoryAttribute attr : attribs) {
                                String categoryFieldName = attr.getName();
                                String categoryFieldValue = attr.getTextValue();
                                double lowRangeValue = attr.getLowRangeValue();
                                double highRangeValue = attr.getHighRangeValue();

                                DictionaryManager dm = DictionaryManager.getInstance ();
                                Integer cId = dm.getId (IProduct.Attribute.kCategory, catId);
                                CategoryAttributeDetails cad = IndexUtils.getDetailsForCategoryFieldName(cId, categoryFieldName);
                                if (cad != null) {
                                    CategoryAttributeDetails.DataType dt = cad.getFieldtype();
                                    Product.Attribute fieldPos = cad.getFieldPos();
                                    if (dt == CategoryAttributeDetails.DataType.kInteger) {
                                        //range query - we multiply by 100 to support upto 2 decimal places
                                        int lowRangeValId = new Double(lowRangeValue).intValue()*100;
                                        int highRangeValId = new Double(highRangeValue).intValue()*100;
                                        long lowRangekey = IndexUtils.createIndexKeyForCategory(cId, fieldPos, lowRangeValId);
                                        long highRangekey = IndexUtils.createIndexKeyForCategory(cId, fieldPos, highRangeValId);
                                        SimpleQuery csq = new LongRangeQuery (IProduct.Attribute.kCategoryNumericField,lowRangekey, highRangekey);
                                        _cjquery.addQuery(csq);
                                    } else if (dt == CategoryAttributeDetails.DataType.kText) {
                                        //simple query
                                        int fieldValId = IndexUtils.getIndexIdFromDictionary(fieldPos, categoryFieldValue);
                                        long key = IndexUtils.createIndexKeyForCategory(cId, fieldPos, fieldValId);
                                        SimpleQuery csq = new CategoryAttributeQuery (IProduct.Attribute.kCategoryTextField, key);
                                       _cjquery.addQuery(csq);
                                    }
                                }

                            }
                        }
                    }
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
                double highPrice = theTSpec.getHighPrice();
                double lowPrice = theTSpec.getLowPrice();
                if ((highPrice > 0) || ( lowPrice > 0)) {
                    SimpleQuery sq = new RangeQuery (IProduct.Attribute.kPrice,lowPrice, highPrice);
                    _cjquery.addQuery(sq);
                }

                //Country Filter
                String countryFilter = theTSpec.getCountryFilter();
                if (countryFilter!= null && !"".equals(countryFilter)) {
                    SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kCountry, countryFilter, false);
                     _cjquery.addQuery(sq);
                }

                //State Filter
                String stateFilter = theTSpec.getStateFilter();
                if (stateFilter!= null && !"".equals(stateFilter)) {
                    SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kState, stateFilter, false);
                     _cjquery.addQuery(sq);
                }

                //City Filter
                String cityFilter = theTSpec.getCityFilter();
                if (cityFilter!= null && !"".equals(cityFilter)) {
                    SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kCity, cityFilter, false);
                     _cjquery.addQuery(sq);
                }

                //Zip Code Filter
                String zipCodeFilter = theTSpec.getZipCodeFilter();
                if (zipCodeFilter!= null && !"".equals(zipCodeFilter)) {
                    SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kZip, zipCodeFilter, false);
                     _cjquery.addQuery(sq);
                }

                //DMA Code Filter
                String dmaCodeFilter = theTSpec.getDmaCodeFilter();
                if (dmaCodeFilter!= null && !"".equals(dmaCodeFilter)) {
                    SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kDMA, dmaCodeFilter, false);
                     _cjquery.addQuery(sq);
                }

                //Area Code Filter
                String areaCodeFilter = theTSpec.getAreaCodeFilter();
                if (areaCodeFilter!= null && !"".equals(areaCodeFilter)) {
                    SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kArea, areaCodeFilter, false);
                     _cjquery.addQuery(sq);
                }

                //Global ID - list of globals
                String globalID = theTSpec.getGlobalId();
                if (globalID != null && !"".equals(globalID)) {
                    SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kGlobalId, globalID, false);
                     _cjquery.addQuery(sq);
                }

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

    /**
     * Helper method to build attribiute query for a comma separated list of string values
     * @param type
     * @param values
     * @param bNegation
     * @return
     */
    private static SimpleQuery buildAttributeQuery(IProduct.Attribute type, String values, boolean bNegation) {
        ArrayList<Integer> valueIdList = new ArrayList<Integer>();
        if (values != null) {
            StringTokenizer st = new StringTokenizer(values, ',');
            ArrayList<String> valueStrList = st.getTokens();
            for (int i=0;i<valueStrList.size();i++){
                String valueStr = valueStrList.get(i);
                DictionaryManager dm = DictionaryManager.getInstance ();
                Integer id = dm.getId (type, valueStr);
                valueIdList.add(id);
            }
        }
        SimpleQuery sq = new AttributeQuery (type, valueIdList);
        sq.setNegation(bNegation);
        return sq;

    }

}
