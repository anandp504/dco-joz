/**
 * 
 */
package com.tumri.joz.filter;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeMap;

import junit.framework.Assert;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tumri.content.data.Product;
import com.tumri.content.data.dictionary.DictionaryManager;
import com.tumri.content.data.impl.ProductImpl;
import com.tumri.joz.Query.AttributeQuery;
import com.tumri.joz.Query.CNFQuery;
import com.tumri.joz.Query.ConjunctQuery;
import com.tumri.joz.Query.LongTextQuery;
import com.tumri.joz.Query.ProductQueryProcessor;
import com.tumri.joz.Query.QueryProcessor;
import com.tumri.joz.Query.RangeQuery;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.products.ProductWrapper;
import com.tumri.joz.utils.IndexUtils;

/**
 * @author omprakash
 * @date Jun 5, 2014
 * @time 6:24:04 PM
 */
public class TestFilter {
	
	@Test
	public void test() {
		
		ProductHandle ph0 = new ProductHandle(1.0, 37319666L);
		ProductHandle ph1 = new ProductHandle(1.0, 37319999L);
		ProductHandle ph2 = new ProductHandle(1.0, 37319444L);
		// update productDB
		ArrayList<Handle> handlesForInt = new ArrayList<Handle>();
		handlesForInt.add(ph0);
		handlesForInt.add(ph1);
		handlesForInt.add(ph2);
		TreeMap<Integer, ArrayList<Handle>> mindex = new TreeMap<Integer, ArrayList<Handle>>();
		Integer val = IndexUtils.getIndexIdFromDictionary(Product.Attribute.kCategory, "Electronics");
		mindex.put(val, handlesForInt);
		ProductDB.getInstance().updateIntegerIndex(Product.Attribute.kCategory,mindex);
		ProductAttributeIndex theIndex = ProductDB.getInstance().getIndex(Product.Attribute.kCategory);
		SortedSet<Handle> results = theIndex.get(val);
		for(Handle h: results){
			System.out.println("handle id: "+ h.getOid());
		}	
		
//		QueryProcessor qp = new ProductQueryProcessor();
//	    DictionaryManager.getInstance();
//	    CNFQuery q = new CNFQuery();
//	    ConjunctQuery cq = new ConjunctQuery(qp);
	        
	    AttributeQuery aq = new AttributeQuery(IProduct.Attribute.kProvider,DictionaryManager.getId(IProduct.Attribute.kProvider, "BESTBUY"));
	    LongTextQuery ltq = new LongTextQuery(IProduct.Attribute.kMultiValueTextField,DictionaryManager.getId(IProduct.Attribute.kUT1, "101010").longValue());
	    RangeQuery rq = new RangeQuery(Product.Attribute.kPrice, 4.0, 9.0);
//	    cq.addQuery(aq);
//	    cq.addQuery(ltq);
//	    cq.addQuery(rq);
		
		{
			Filter<Handle> catFilter = ProductDB.getInstance().getFilter(Product.Attribute.kCategory);
			catFilter.setQuery(aq);
		    catFilter.setNegation(true);
		    Assert.assertTrue(catFilter.accept(ph0));
		}
		{
			LongFilter<Handle> multiFilter = ProductDB.getInstance().getLongFilter(Product.Attribute.kMultiValueTextField);
			multiFilter.setQuery(ltq);
			multiFilter.setNegation(false);
		    Assert.assertFalse(multiFilter.accept(ph0));
		}
		{
			
			Filter<Handle> priceFilter = (PriceRangeFilter) ProductDB.getInstance().getFilter(Product.Attribute.kPrice);
			priceFilter.getMin();
			priceFilter.getMax();
			priceFilter.setQuery(rq);
			priceFilter.setNegation(true);
		    Assert.assertTrue(priceFilter.accept(ph0));
		}
		{
			Filter<Handle> supplierFilter = ProductDB.getInstance().getFilter(Product.Attribute.kSupplier);
			supplierFilter.setQuery(rq);
			supplierFilter.setNegation(true);
		    Assert.assertTrue(supplierFilter.accept(ph0));
		}
		{
			Filter<Handle> provFilter = ProductDB.getInstance().getFilter(Product.Attribute.kProvider);
			provFilter.setQuery(rq);
			provFilter.setNegation(true);
		    Assert.assertTrue(provFilter.accept(ph0));
		}
		{
			Filter<Handle> prodTypeFilter = ProductDB.getInstance().getFilter(Product.Attribute.kProductType);
			prodTypeFilter.setQuery(rq);
			prodTypeFilter.setNegation(true);
		    Assert.assertTrue(prodTypeFilter.accept(ph0));
		}
		{
			Filter<Handle> globIdFilter = ProductDB.getInstance().getFilter(Product.Attribute.kGlobalId);
			globIdFilter.setQuery(rq);
			globIdFilter.setNegation(true);
		    Assert.assertTrue(globIdFilter.accept(ph0));
		}
		{
			Filter<Handle> brandFilter = ProductDB.getInstance().getFilter(Product.Attribute.kBrand);
			brandFilter.setQuery(rq);
			brandFilter.setNegation(true);
		    Assert.assertTrue(brandFilter.accept(ph0));
		}
		{
			Filter<Handle> zipFilter = ProductDB.getInstance().getFilter(Product.Attribute.kZip);
			zipFilter.setQuery(rq);
			zipFilter.setNegation(true);
		    Assert.assertTrue(zipFilter.accept(ph0));
		}
		{
			Filter<Handle> stateFilter = ProductDB.getInstance().getFilter(Product.Attribute.kState);
			stateFilter.setQuery(rq);
			stateFilter.setNegation(true);
		    Assert.assertTrue(stateFilter.accept(ph0));
		}
		{
			Filter<Handle> longFilter = ProductDB.getInstance().getFilter(Product.Attribute.kLongitude);
			longFilter.setQuery(rq);
			longFilter.setNegation(true);
		    Assert.assertTrue(longFilter.accept(ph0));
		}
		{
			Filter<Handle> latFilter = ProductDB.getInstance().getFilter(Product.Attribute.kLatitude);
			latFilter.setQuery(rq);
			latFilter.setNegation(true);
		    Assert.assertTrue(latFilter.accept(ph0));
		}
		{
			Filter<Handle> hhiFilter = ProductDB.getInstance().getFilter(Product.Attribute.kHHI);
			hhiFilter.setQuery(rq);
			hhiFilter.setNegation(true);
		    Assert.assertTrue(hhiFilter.accept(ph0));
		}
		{
			Filter<Handle> geoEnabledFilter = ProductDB.getInstance().getFilter(Product.Attribute.kGeoEnabledFlag);
			geoEnabledFilter.setQuery(rq);
			geoEnabledFilter.setNegation(true);
		    Assert.assertTrue(geoEnabledFilter.accept(ph0));
		}
		{
			Filter<Handle> genderFilter = ProductDB.getInstance().getFilter(Product.Attribute.kGender);
			genderFilter.setQuery(rq);
			genderFilter.setNegation(true);
		    Assert.assertTrue(genderFilter.accept(ph0));
		}
		{
			Filter<Handle> dmaFilter = ProductDB.getInstance().getFilter(Product.Attribute.kDMA);
			dmaFilter.setQuery(rq);
			dmaFilter.setNegation(true);
		    Assert.assertTrue(dmaFilter.accept(ph0));
		}
//		{
//			Filter<Handle> discFilter = ProductDB.getInstance().getFilter(Product.Attribute.kDiscount);
//			discFilter.setQuery(rq);
//			discFilter.setNegation(true);
//		    Assert.assertTrue(discFilter.accept(ph0));
//		}
		{
			Filter<Handle> cpoFilter = ProductDB.getInstance().getFilter(Product.Attribute.kCPO);
			cpoFilter.setQuery(rq);
			cpoFilter.setNegation(true);
		    Assert.assertTrue(cpoFilter.accept(ph0));
		}
		{
			Filter<Handle> cpcFilter = ProductDB.getInstance().getFilter(Product.Attribute.kCPC);
			cpcFilter.setQuery(rq);
			cpcFilter.setNegation(true);
		    Assert.assertTrue(cpcFilter.accept(ph0));
		}
		{
			Filter<Handle> countryFilter = ProductDB.getInstance().getFilter(Product.Attribute.kCountry);
			countryFilter.setQuery(rq);
			countryFilter.setNegation(true);
		    Assert.assertTrue(countryFilter.accept(ph0));
		}
		{
			Filter<Handle> cityFilter = ProductDB.getInstance().getFilter(Product.Attribute.kCity);
			cityFilter.setQuery(rq);
			cityFilter.setNegation(true);
		    Assert.assertTrue(cityFilter.accept(ph0));
		}
		{
			Filter<Handle> ccFilter = ProductDB.getInstance().getFilter(Product.Attribute.kCC);
			ccFilter.setQuery(rq);
			ccFilter.setNegation(true);
		    Assert.assertTrue(ccFilter.accept(ph0));
		}
		{
			Filter<Handle> btFilter = ProductDB.getInstance().getFilter(Product.Attribute.kBT);
			btFilter.setQuery(rq);
			btFilter.setNegation(true);
		    Assert.assertTrue(btFilter.accept(ph0));
		}
		{
			Filter<Handle> areaFilter = ProductDB.getInstance().getFilter(Product.Attribute.kArea);
			areaFilter.setQuery(rq);
			areaFilter.setNegation(true);
		    Assert.assertTrue(areaFilter.accept(ph0));
		}
		{
			Filter<Handle> ageFilter = ProductDB.getInstance().getFilter(Product.Attribute.kAge);
			ageFilter.setQuery(rq);
			ageFilter.setNegation(true);
		    Assert.assertTrue(ageFilter.accept(ph0));
		}
	}
	@After
	public void clear(){
		ProductDB.getInstance().clearProductDB();
		DictionaryManager.dictionaries.clear();
	}
}
