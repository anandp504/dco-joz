/**
 * 
 */
package com.tumri.joz.index;

import java.io.File;
import java.io.IOException;
import java.util.*;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tumri.content.data.Product;
import com.tumri.content.data.impl.ProductImpl;
import com.tumri.joz.JoZException;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.products.ProductWrapper;
import com.tumri.joz.utils.IndexUtils;
import com.tumri.joz.utils.ZipCodeDB;
import com.tumri.content.data.dictionary.DictionaryManager;

/**
 * @author omprakash
 * @date May 23, 2014
 * @time 1:15:34 PM
 */
public class TestCommonIndexFunctionality {
	AreaCodeIndex acIndex = new AreaCodeIndex();
	BrandIndex brandIndex = new BrandIndex();
	BTIndex btIndex = new BTIndex();
	CCIndex ccIndex = new CCIndex();
	CategoryIndex catIndex = new CategoryIndex();
	CityIndex cityIndex = new CityIndex();
	CountryIndex countryIndex = new CountryIndex();
	CPCIndex cpcIndex = new CPCIndex();
	CPOIndex cpoIndex = new CPOIndex();
	PriceIndex priceIndex = new PriceIndex();
	DiscountIndex discIndex = new DiscountIndex();
	DmaCodeIndex dmaCodIndex = new DmaCodeIndex();
	GeoEnabledIndex geoEnableIndex = new GeoEnabledIndex();
	GlobalIdIndex globalIndex = new GlobalIdIndex();
	HHIIndex hhiIndex = new HHIIndex();
	ProviderIndex provIndex = new ProviderIndex();
	RankIndex rankIndex = new RankIndex();
	StateIndex stateIndex = new StateIndex();
	SupplierIndex supIndex = new SupplierIndex();
	TextIndexImpl txtIndex = new TextIndexImpl(Product.Attribute.kUT1);
	ZipCodeIndex zipCodIndex = new ZipCodeIndex();
	ProductTypeIndex prodTypIndex = new ProductTypeIndex();
	LongitudeIndex longitudeIndex = new LongitudeIndex();
	LatitudeIndex latIndex = new LatitudeIndex();
	LatLongIndex<String, Handle> llIndex = new LatLongIndex<String, Handle>(LatLongIndex.Attribute.kLatitude);
	OptTextIndexImpl optTxtIndex = new OptTextIndexImpl(Product.Attribute.kExperienceIdUT1);
	private static ZipCodeDB zipDB = null;
	
	@BeforeClass
    public static void init() {
		ProductDB.getInstance().clearProductDB();
		//clean dictionaries
		DictionaryManager.dictionaries.clear();
		zipDB = ZipCodeDB.getInstance();
	    try {
			zipDB.init();
		} catch (JoZException e) {
			e.printStackTrace();
		}
        System.out.println("Done zipcode DB initialization...");
    }
	@AfterClass
	public static void clear(){
		ProductDB.getInstance().clearProductDB();
		DictionaryManager.dictionaries.clear();
		if(zipDB != null){
			zipDB = null;
		}
	}
		 
	@Test
	public void test0() {
	
		ProductImpl pimpl = new ProductImpl();
		pimpl.setAreaCodeStr("areaCodeString");
		pimpl.setBrandStr("BB_Uber_HomeTheater");
		pimpl.setBtStr("1111");
		pimpl.setCcStr("cc string");
		pimpl.setCategoryStr("GLASSVIEW.TUMRI_403359");
		pimpl.setCityStr("cityString");
		pimpl.setCountryStr("countryString");
		pimpl.setCPC(11.11);
		pimpl.setCPCStr("11.11");
		pimpl.setCPO(22.22);
		pimpl.setCPOStr("22.22");
		pimpl.setDiscountPrice(0.00);
		pimpl.setDiscountPriceCurrencyStr("discount currency string");
		pimpl.setDiscountPriceStr("0.00");
		pimpl.setPrice(1.00);
		pimpl.setProviderStr("BESTBUY");
		pimpl.setRank(40);
		pimpl.setRankStr("40");
		pimpl.setStateStr("StateString");
		pimpl.setSupplierStr("-456504");
		pimpl.setZipStr("00501");			//00501,40.8151,-73.0455
        pimpl.setGId("US38238481");			// set id of pimpl to 38238481, which is Oid of productHandle
        pimpl.setProductTypeStr("Electronics");
        pimpl.setPassThroughField11Str("Audio Cable");
        pimpl.setPassThroughField12Str("4.7");
        pimpl.setPassThroughField13Str("GC36563");
        pimpl.setPassThroughField14Str("true");
        pimpl.setPassThroughField15Str("");
        
        
		ArrayList<ProductImpl> pimpls = new ArrayList<ProductImpl>();
		pimpls.add(pimpl);
		pimpls.add(null);
		for( ProductImpl impl: pimpls){
			try{
				test(impl);
			}catch(Exception e){
				System.out.println(e.getMessage());
			}	
		}
	}

	private void test(ProductImpl impl) {
		IProduct pw = new ProductWrapper(impl);
		
		{
			Assert.assertEquals("kArea", acIndex.getType().toString());
			Assert.assertEquals(0, acIndex.getKey(pw).intValue());
			Assert.assertEquals(38238481, acIndex.getValue(pw).getOid());
			Assert.assertEquals("areaCodeString", pw.getAreaCodeStr());
		}
		{
			Assert.assertEquals("kBT", btIndex.getType().toString());
			Assert.assertNull(btIndex.getKey(pw));
			Assert.assertEquals(38238481, btIndex.getValue(pw).getOid());
		}
		{
			Assert.assertEquals("kCC", ccIndex.getType().toString());
			Assert.assertNull(ccIndex.getKey(pw));
			Assert.assertEquals(38238481, ccIndex.getValue(pw).getOid());
		}
		{
			Assert.assertEquals("kCity", cityIndex.getType().toString());
			Assert.assertEquals(0, cityIndex.getKey(pw).intValue());
			Assert.assertEquals(38238481, cityIndex.getValue(pw).getOid());
			Assert.assertEquals("cityString", pw.getCityStr());
		}
		{
			Assert.assertEquals("kCountry", countryIndex.getType().toString());
			Assert.assertEquals(0, countryIndex.getKey(pw).intValue());
			Assert.assertEquals(38238481, countryIndex.getValue(pw).getOid());
			Assert.assertEquals("countryString", pw.getCountryStr());
		}
		{
			Assert.assertEquals("kCPC", cpcIndex.getType().toString());
			Assert.assertEquals(11.11, cpcIndex.getKey(pw).doubleValue());
			Assert.assertEquals(1.0, cpcIndex.getValue(pw).getScore());
		}
		{
			Assert.assertEquals("kCPO", cpoIndex.getType().toString());
			Assert.assertEquals(22.22, cpoIndex.getKey(pw).doubleValue());
			Assert.assertEquals(1.0, cpoIndex.getValue(pw).getScore());
		}
		{
			Assert.assertEquals("kDiscount", discIndex.getType().toString());
			Assert.assertNull(discIndex.getKey(pw));
			Assert.assertEquals(1.0, discIndex.getValue(pw).getScore());
			Assert.assertEquals("discount currency string", pw.getDiscountPriceCurrencyStr());
			Assert.assertEquals(0.00, pw.getDiscountPrice().doubleValue());
		}
		{
			Assert.assertEquals("kDMA", dmaCodIndex.getType().toString());
			Assert.assertNull(dmaCodIndex.getKey(pw));
			Assert.assertEquals(1.0, dmaCodIndex.getValue(pw).getScore());
		}
		{
			Assert.assertEquals("kGeoEnabledFlag", geoEnableIndex.getType().toString());
			Assert.assertNull(geoEnableIndex.getKey(pw));
			Assert.assertEquals(1.0, geoEnableIndex.getValue(pw).getScore());
		}
		{
			Assert.assertEquals("kGlobalId", globalIndex.getType().toString());
			Assert.assertNull(globalIndex.getKey(pw));
			Assert.assertEquals(38238481, globalIndex.getValue(pw).getOid());
		}
		{
			Assert.assertEquals("kHHI", hhiIndex.getType().toString());
			Assert.assertNull(hhiIndex.getKey(pw));
			Assert.assertEquals(38238481, hhiIndex.getValue(pw).getOid());
		}
		{
			Assert.assertEquals("kPrice", priceIndex.getType().toString());
			Assert.assertEquals(1.00, priceIndex.getKey(pw).doubleValue());
			Assert.assertEquals(38238481, priceIndex.getValue(pw).getOid());
		}
		
		{
			Assert.assertEquals("kRank", rankIndex.getType().toString());
			Assert.assertEquals(40, rankIndex.getKey(pw).intValue());								
			Assert.assertNotNull(rankIndex.getValue(pw));
		}
		{
			Assert.assertEquals("kState", stateIndex.getType().toString());
			Assert.assertEquals(0, stateIndex.getKey(pw).intValue());				// index of the state as per dictionary index
			Assert.assertNotNull(stateIndex.getValue(pw));
			Assert.assertEquals("StateString", pw.getStateStr());
		}
		
		{
			Assert.assertEquals("kUT1", txtIndex.getType().toString());
			Assert.assertNull(txtIndex.getKey(pw));									// null only, seems no way to set test data
			Assert.assertNotNull(txtIndex.getValue(pw));
		}
		{
			Assert.assertEquals("kZip", zipCodIndex.getType().toString());
			Assert.assertEquals(0, zipCodIndex.getKey(pw).intValue());
			Assert.assertNotNull(zipCodIndex.getValue(pw));
		}
		{
			Assert.assertEquals("kBrand", brandIndex.getType().toString());
			Assert.assertEquals(0, brandIndex.getKey(pw).intValue());				
			Assert.assertEquals(38238481, brandIndex.getValue(pw).getOid());
		}
		{
			Assert.assertEquals("kCategory", catIndex.getType().toString());
			Assert.assertEquals(0, catIndex.getKey(pw).intValue());					
			Assert.assertEquals(38238481, catIndex.getValue(pw).getOid());
			Assert.assertNotNull(catIndex.getEntries(pw));
		}
		{
			Assert.assertEquals("kProvider", provIndex.getType().toString());
			Assert.assertEquals(0, provIndex.getKey(pw).intValue());				
			Assert.assertEquals(38238481, provIndex.getValue(pw).getOid());
			Assert.assertEquals("BESTBUY", pw.getProviderStr());
		}
		{
			Assert.assertEquals("kSupplier", supIndex.getType().toString());
			Assert.assertEquals(0, supIndex.getKey(pw).intValue());							
			Assert.assertNotNull(supIndex.getValue(pw));
			Assert.assertEquals("-456504", pw.getSupplierStr());
		}
		{
			Assert.assertEquals("kProductType", prodTypIndex.getType().toString());
			Assert.assertEquals(0, prodTypIndex.getKey(pw).intValue());					
			Assert.assertEquals(38238481, prodTypIndex.getValue(pw).getOid());
		}
		{
			Assert.assertEquals("kLongitude", longitudeIndex.getType().toString());
			Assert.assertEquals(-73, longitudeIndex.getKey(pw).intValue());
			Assert.assertEquals(38238481, longitudeIndex.getValue(pw).getOid());
		}
		{
			Assert.assertEquals("kLatitude", latIndex.getType().toString());
			Assert.assertEquals(40, latIndex.getKey(pw).intValue());
			Assert.assertEquals(38238481, latIndex.getValue(pw).getOid());
		}
		{
			Assert.assertEquals("kExperienceIdUT1", optTxtIndex.getType().toString());
			Assert.assertNull(optTxtIndex.getKey(pw));						// null only, seems no way to set data
			Assert.assertNotNull(optTxtIndex.getValue(pw));
		}
		{
			Assert.assertEquals("Audio Cable", pw.getPassThrough1Str());
			Assert.assertEquals("4.7", pw.getPassThrough2Str());
			Assert.assertEquals("GC36563", pw.getPassThrough3Str());
			Assert.assertEquals("true", pw.getPassThrough4Str());
			Assert.assertEquals("", pw.getPassThrough5Str());
		}
		{
			String message = "This method is not supported by this index. Use put(Map) method instead";
			Assert.assertEquals("kLatitude", llIndex.getType().toString());
			try{
				llIndex.getEntries(40.8151);
			}catch(Exception e){
			Assert.assertEquals(message, e.getMessage());
			}
		}
		{
		// update productDB 
		ArrayList<Handle> handlesForInt = new ArrayList<Handle>();
		ProductHandle ph0 = new ProductHandle(1.0, 38238481L);
		handlesForInt.add(ph0);
		TreeMap<Integer, ArrayList<Handle>> mindex = new TreeMap<Integer, ArrayList<Handle>>();
		Integer val = IndexUtils.getIndexIdFromDictionary(Product.Attribute.kCategory, "Electronics");
		mindex.put(val, handlesForInt);
		ProductDB.getInstance().updateIntegerIndex(Product.Attribute.kCategory, mindex);
		ProductAttributeIndex theIndex=ProductDB.getInstance().getIndex(Product.Attribute.kCategory);
		 
		 List<Integer> keys = new ArrayList<Integer>();
		 keys.add(val);
		 SortedSet<ProductHandle> handles = theIndex.get(keys);
		 long actual = 0;
		 if(handles != null){
			 if(handles.size() != 1){
				 System.out.println("more products");
			 }else{
				 for(ProductHandle ph: handles){
					 actual = ph.getOid();
				 }
			 }
		 }
		 Assert.assertEquals(38238481, actual);
		 }	
	}
}
