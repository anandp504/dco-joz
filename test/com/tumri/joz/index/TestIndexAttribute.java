/**
 * 
 */
package com.tumri.joz.index;

import java.util.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.tumri.joz.JoZException;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.utils.IndexUtils;
import com.tumri.joz.utils.ZipCodeDB;
import com.tumri.content.data.Product;
import com.tumri.content.data.dictionary.DictionaryManager;

/**
 * @author omprakash
 * @date May 20, 2014
 * @time 10:52:42 AM
 */
public class TestIndexAttribute {
	
	@Before
    public void init() {
		ProductDB.getInstance().clearProductDB();
		//clean dictionaries
		DictionaryManager.dictionaries.clear();
    }
	@After
	public void clear(){
		ProductDB.getInstance().clearProductDB();
		DictionaryManager.dictionaries.clear();
	}

	@Test
	public void test0(){
		
		ArrayList<Handle> handlesForLong = new ArrayList<Handle>();
		// create map for kExternalFilterField2 "cases and straps" ==>Long
		ProductHandle ph0 = new ProductHandle(1.0, 37319266L);
		ProductHandle ph1 = new ProductHandle(1.0, 37319279L);
		ProductHandle ph2 = new ProductHandle(1.0, 37319284L);
		handlesForLong.add(ph0);
		handlesForLong.add(ph1);
		handlesForLong.add(ph2);
		
		String value = "cases & straps";
		long key = IndexUtils.createLongIndexKey(Product.Attribute.kExternalFilterField2,IndexUtils.getIndexIdFromDictionary(Product.Attribute.kExternalFilterField2, value));
		// Obtained key = 2305843009213693952
		TreeMap<Long, ArrayList<Handle>> mindex = new TreeMap<Long, ArrayList<Handle>>();
		mindex.put(key, handlesForLong);
		// this updates the m_map of AttributeIndex
		ProductDB.getInstance().updateLongIndex(Product.Attribute.kMultiValueTextField, mindex);
		//retrieval
		ProductAttributeIndex theIndex=ProductDB.getInstance().getIndex(Product.Attribute.kMultiValueTextField);
		SortedSet<Handle> results = theIndex.get(key);
		for(Handle h: results){
			System.out.println("handle id: "+ h.getOid());
		}	
	}
	
	@Test
	public void test1(){
		
		SortedSet<Handle> d_hs = new TreeSet<Handle>();
		ArrayList<Handle> handlesForDouble = new ArrayList<Handle>();
		// create map for kExternalFilterField2 "cases and straps" ==>Long
		ProductHandle ph0 = new ProductHandle(1.0, 37319266L);
		ProductHandle ph1 = new ProductHandle(1.0, 37319279L);
		ProductHandle ph2 = new ProductHandle(1.0, 37319284L);
		d_hs.add(ph0);handlesForDouble.add(ph0);
		d_hs.add(ph1);handlesForDouble.add(ph1);
		d_hs.add(ph2);handlesForDouble.add(ph2);
		
		String price = "100.50";
		Double dPrice = Double.parseDouble(price);
		// Obtained key = 2305843009213693952
		TreeMap<Double, ArrayList<Handle>> mindex = new TreeMap<Double, ArrayList<Handle>>();
		mindex.put(dPrice, handlesForDouble);
		// this updates the m_map of AttributeIndex
		ProductDB.getInstance().updateDoubleIndex(Product.Attribute.kPrice, mindex);
		
		//retrieval
		ProductAttributeIndex theIndex=ProductDB.getInstance().getIndex(Product.Attribute.kPrice);
		SortedSet<Handle> results = theIndex.get(dPrice);
		System.out.println("test 1:");
		for(Handle h: results){
			System.out.println("handle id: "+ h.getOid());
		}	
	}
	@Test
	public void test2(){
		
		SortedSet<Handle> i_hs = new TreeSet<Handle>();
		ArrayList<Handle> handlesForInteger = new ArrayList<Handle>();
		
		// create map for kExternalFilterField2 "cases and straps" ==>Long
		ProductHandle ph0 = new ProductHandle(1.0, 37319266L);
		ProductHandle ph1 = new ProductHandle(1.0, 37319279L);
		ProductHandle ph2 = new ProductHandle(1.0, 37319284L);
		i_hs.add(ph0);handlesForInteger.add(ph0);
		i_hs.add(ph1);handlesForInteger.add(ph1);
		i_hs.add(ph2);handlesForInteger.add(ph2);
		
		Integer val = IndexUtils.getIndexIdFromDictionary(Product.Attribute.kProvider, "BESTBUY");
		// Obtained key = 2305843009213693952
		TreeMap<Integer, ArrayList<Handle>> mindex = new TreeMap<Integer, ArrayList<Handle>>();
		mindex.put(val, handlesForInteger);
		// this updates the m_map of AttributeIndex
		ProductDB.getInstance().updateIntegerIndex(Product.Attribute.kProvider, mindex);
		//retrieval
		ProductAttributeIndex theIndex=ProductDB.getInstance().getIndex(Product.Attribute.kProvider);
		Integer key = DictionaryManager.getInstance().getId(Product.Attribute.kProvider, "BESTBUY");
		SortedSet<Handle> results = theIndex.get(key);
		System.out.println("test 2:");
		for(Handle h: results){
			System.out.println("handle id: "+ h.getOid());
		}	
	}	
}
