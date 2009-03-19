package com.tumri.joz.campaign;

import com.tumri.cma.domain.*;
import com.tumri.content.data.Product;
import com.tumri.content.data.ProductAttributeDetails;
import com.tumri.content.data.dictionary.DictionaryManager;
import com.tumri.joz.Query.*;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.utils.IndexUtils;
import com.tumri.utils.strings.StringTokenizer;

import java.util.ArrayList;
import java.util.List;

public class TSpecQueryCacheHelper {
    private static final String PRODUCT = "Product";

    /**
     * Walk thru the TSpec details and create the Query
     * @param theTSpec
     * @return
     */
    public static CNFQuery getQuery(TSpec theTSpec) {
        CNFQuery _query = new CNFQuery ();
        ConjunctQuery _cjquery = new ConjunctQuery (new ProductQueryProcessor());
        //Keyword
        String keywordExp = theTSpec.getLoadTimeKeywordExpression();
        if (keywordExp != null && !"".equals(keywordExp.trim())){
            KeywordQuery kwQuery = new KeywordQuery(keywordExp,true);
            _cjquery.addQuery(kwQuery);
        }

        //Excluded Brand
        List<BrandInfo> bixList = theTSpec.getExcludedBrands();
        if (bixList != null) {
            SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kBrand, bixList, true);
            if(sq!=null){
                _cjquery.addQuery(sq);
	        }
        }

        //Included brand
        List<BrandInfo> binList = theTSpec.getIncludedBrands();
        if (binList != null) {
            SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kBrand, binList, false);
            if(sq!=null){
                _cjquery.addQuery(sq);
	        }
        }

        //Include cats
        List<CategoryInfo> cinList = theTSpec.getIncludedCategories();
        if (cinList != null) {
            SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kCategory, cinList, false);
            if(sq!=null){
                _cjquery.addQuery(sq);
	        }

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
                        ProductAttributeDetails cad = IndexUtils.getDetailsForCategoryFieldName(cId, categoryFieldName);
                        if (cad != null) {
                            ProductAttributeDetails.DataType dt = cad.getFieldtype();
                            Product.Attribute fieldPos = cad.getFieldPos();
                            if (dt == ProductAttributeDetails.DataType.kInteger) {
                                //range query - we multiply by 100 to support upto 2 decimal places
	                            if(lowRangeValue >= 0 && highRangeValue > 0){
									int lowRangeValId = new Double(lowRangeValue).intValue()*100;
									int highRangeValId = new Double(highRangeValue).intValue()*100;
									long lowRangekey = IndexUtils.createIndexKeyForCategoryAttribute(cId, fieldPos, lowRangeValId);
									long highRangekey = IndexUtils.createIndexKeyForCategoryAttribute(cId, fieldPos, highRangeValId);
									SimpleQuery csq = new LongRangeQuery (IProduct.Attribute.kCategoryNumericField,lowRangekey, highRangekey);
									_cjquery.addQuery(csq);
	                            }
                            } else if (dt == ProductAttributeDetails.DataType.kText) {
                                //categoryFieldValue might comma separated list of values
                                if (categoryFieldValue != null && !"".equals(categoryFieldValue.trim())) {
                                    ArrayList<Long> valueIdList = new ArrayList<Long>();
                                    StringTokenizer st = new StringTokenizer(categoryFieldValue, ',');
                                    ArrayList<String> valueStrList = st.getTokens();
                                    for (int i=0;i<valueStrList.size();i++){
                                        String valueStr = valueStrList.get(i);
                                        int fieldValId = IndexUtils.getIndexIdFromDictionary(fieldPos, valueStr);
                                        long key = IndexUtils.createIndexKeyForCategoryAttribute(cId, fieldPos, fieldValId);
                                        valueIdList.add(key);
                                    }
                                    //simple query
                                    SimpleQuery csq = new LongTextQuery(IProduct.Attribute.kCategoryTextField, valueIdList);
                                    _cjquery.addQuery(csq);
                                }
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
	        if(sq!=null){
                _cjquery.addQuery(sq);
	        }
        }

        //Included merchants
        List<MerchantInfo> inMerchants = theTSpec.getIncludedMerchants();
        if (inMerchants != null) {
            SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kSupplier, inMerchants, false);
            if(sq!=null){
                _cjquery.addQuery(sq);
	        }
        }

        //Excluded merchants
        List<MerchantInfo> exMerchants = theTSpec.getExcludedMerchants();
        if (exMerchants != null) {
            SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kSupplier, exMerchants, true);
           if(sq!=null){
                _cjquery.addQuery(sq);
	        }
        }

        //Included Providers
        List<ProviderInfo> inProviders = theTSpec.getIncludedProviders();
        if (inProviders != null) {
            SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kProvider, inProviders, false);
            if(sq!=null){
                _cjquery.addQuery(sq);
	        }
        }

        //Excluded Providers
        List<ProviderInfo> exProviders = theTSpec.getExcludedProviders();
        if (exProviders != null) {
            SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kProvider, exProviders, true);
            if(sq!=null){
                _cjquery.addQuery(sq);
	        }
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
        if (countryFilter!= null && !"".equals(countryFilter.trim())) {
            SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kCountry, countryFilter, false);
            _cjquery.addQuery(sq);
        }

        //State Filter
        String stateFilter = theTSpec.getStateFilter();
        if (stateFilter!= null && !"".equals(stateFilter.trim())) {
            SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kState, stateFilter, false);
            _cjquery.addQuery(sq);
        }

        //City Filter
        String cityFilter = theTSpec.getCityFilter();
        if (cityFilter!= null && !"".equals(cityFilter.trim())) {
            SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kCity, cityFilter, false);
            _cjquery.addQuery(sq);
        }

        //Zip Code Filter
        String zipCodeFilter = theTSpec.getZipCodeFilter();
        if (zipCodeFilter!= null && !"".equals(zipCodeFilter.trim())) {
            SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kZip, zipCodeFilter, false);
            _cjquery.addQuery(sq);
        }

        //DMA Code Filter
        String dmaCodeFilter = theTSpec.getDmaCodeFilter();
        if (dmaCodeFilter!= null && !"".equals(dmaCodeFilter.trim())) {
            SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kDMA, dmaCodeFilter, false);
            _cjquery.addQuery(sq);
        }

        //Area Code Filter
        String areaCodeFilter = theTSpec.getAreaCodeFilter();
        if (areaCodeFilter!= null && !"".equals(areaCodeFilter.trim())) {
            SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kArea, areaCodeFilter, false);
            _cjquery.addQuery(sq);
        }

        //Global ID - list of globals
        List<GlobalIdInfo> inGlobals = theTSpec.getIncludedGlobalIds();
        if (inGlobals != null) {
            SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kGlobalId, inGlobals, false);
            if(sq!=null){
                _cjquery.addQuery(sq);
	        }
        }

        //Excluded GlobalsIds
        List<GlobalIdInfo> exGlobalIds = theTSpec.getExcludedGlobalIds();
        if (exProviders != null) {
            SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kGlobalId, exGlobalIds, true);
            if(sq!=null){
                _cjquery.addQuery(sq);
	        }
        }

        String productType = theTSpec.getProductType();
        //Default to product
        if (productType == null || "".equals(productType.trim())) {
           productType = PRODUCT;
        }
        SimpleQuery sq = buildAttributeQuery(IProduct.Attribute.kProductType, productType, false);
        _cjquery.addQuery(sq);

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
        ArrayList<Integer> valueIdList = new ArrayList<Integer>();
        ArrayList<String> valueStrList = new ArrayList<String>();
        if (values != null) {
            for (int i=0;i<values.size();i++){
                if (type.equals(IProduct.Attribute.kCategory)) {
                    CategoryInfo cInfo = (CategoryInfo)values.get(i);
	                String cName = cInfo.getName();
	                if(cName != null && !"".equals(cName.trim())){
                        valueStrList.add(cName);
	                }
                } else if (type.equals(IProduct.Attribute.kBrand)) {
                    BrandInfo bInfo = (BrandInfo)values.get(i);
	                String bName = bInfo.getName();
	                if(bName != null && !"".equals(bName.trim())){
                        valueStrList.add(bName);
	                }
                } else if (type.equals(IProduct.Attribute.kProvider)) {
                    ProviderInfo pInfo = (ProviderInfo)values.get(i);
	                String pName = pInfo.getName();
	                if(pName != null && !"".equals(pName.trim())){
                        valueStrList.add(pName);
	                }
                } else if (type.equals(IProduct.Attribute.kSupplier)) {
                    MerchantInfo mInfo = (MerchantInfo)values.get(i);
	                String mName = mInfo.getName();
	                if(mName != null && !"".equals(mName.trim())){
                        valueStrList.add(mInfo.getName());
	                }
                } else if (type.equals(IProduct.Attribute.kGlobalId)) {
                    GlobalIdInfo gInfo = (GlobalIdInfo)values.get(i);
	                String gName = gInfo.getName();
	                if(gName != null && !"".equals(gName.trim())){
                        valueStrList.add(gInfo.getName());
	                }
                } else if (type.equals(IProduct.Attribute.kProductType)) {
                    valueStrList.add((String)values.get(i));
                }
            }
            for (String valueStr :valueStrList){
                Integer brandId = DictionaryManager.getId (type, valueStr);
                valueIdList.add(brandId);
            }
        }
        SimpleQuery sq = null;
	    if(valueIdList.size() > 0){
	        sq = new AttributeQuery (type, valueIdList);
		    sq.setNegation(bNegation);
        }

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
