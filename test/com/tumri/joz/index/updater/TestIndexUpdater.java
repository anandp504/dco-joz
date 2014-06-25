/**
 * 
 */
package com.tumri.joz.index.updater;

import java.util.ArrayList;
import java.util.SortedSet;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Test;

import com.tumri.content.data.Product;
import com.tumri.content.data.dictionary.DictionaryManager;
import com.tumri.jic.joz.PersistantIndexLine;
import com.tumri.jic.joz.PersistantIndexLine.IndexOperation;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.jozMain.JozData;
import com.tumri.joz.products.ContentHelper;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.IndexUtils;

/**
 * @author omprakash
 * @date Jun 4, 2014
 * @time 4:44:31 PM
 */
public class TestIndexUpdater {
	@AfterClass
	public static void cleanUp(){
		ProductDB.getInstance().clearProductDB();
		DictionaryManager.dictionaries.clear();
	}

	@Test
	public void test(){
		JozIndexUpdater updater = new JozIndexUpdater();
		ArrayList<Object> pids = new ArrayList<Object>();
		ProductHandle ph0 = new ProductHandle(1.0, 37319266L);
		ProductHandle ph1 = (ProductHandle) updater.createNewHandle(37319279L, false);//new ProductHandle(1.0, 37319279L);
		ProductHandle ph2 = (ProductHandle) updater.createNewHandle(37319284L, true);//new ProductHandle(1.0, 37319284L);
		
		pids.add(ph0);
		pids.add(ph1);
		pids.add(ph2);
		ArrayList<Long> prods = new ArrayList<Long>();
		prods.add(37319266L);
		prods.add(37319279L);
		prods.add(37319284L);
		
		updater.setProdIds(prods);
		updater.handleLine("supplier", "-456504", pids, PersistantIndexLine.IndexOperation.kAdd, false, false);
		
		Integer key = IndexUtils.getIndexIdFromDictionary(Product.Attribute.kSupplier, "-456504");
		ProductAttributeIndex theIndex=ProductDB.getInstance().getIndex(Product.Attribute.kSupplier);
		SortedSet<Handle> results = theIndex.get(key);
		Assert.assertNotNull(theIndex.get(key));
		Assert.assertEquals(37319266L, results.first().getOid());
		Assert.assertEquals(37319284L, results.last().getOid());
		
		JozIndexUpdater reUpdater = new JozIndexUpdater(true);
		reUpdater.handleLine("supplier", "-456504", pids, PersistantIndexLine.IndexOperation.kDelModified, false, true);
		Integer rekey = IndexUtils.getIndexIdFromDictionary(Product.Attribute.kSupplier, "-456504");
		ProductAttributeIndex theReIndex=ProductDB.getInstance().getIndex(Product.Attribute.kSupplier);
		
		Assert.assertNull(theReIndex.get(rekey));
		Assert.assertEquals(3, updater.productIds.size());
		updater.reset();
		Assert.assertEquals(0, updater.productIds.size());		
	}
	
	@Test
	public void test0(){
		OptJozIndexUpdater optUpdater = new OptJozIndexUpdater();
		ArrayList<Object> pids = new ArrayList<Object>();
		ProductHandle ph0 = new ProductHandle(1.0, 37319666L);
		ProductHandle ph1 = (ProductHandle) optUpdater.createNewHandle(37319999L, false);
		ProductHandle ph2 = (ProductHandle) optUpdater.createNewHandle(37319444L, true);
		pids.add(ph0);
		pids.add(ph1);
		pids.add(ph2);
		ArrayList<Long> prods = new ArrayList<Long>();
		prods.add(37319666L);
		prods.add(37319999L);
		prods.add(37319444L);
		optUpdater.setProdIds(prods);
		Integer experienceId = 9339;
		optUpdater.setExperienceId(experienceId);
		
		optUpdater.handleLine("optExp", experienceId.toString(), pids, PersistantIndexLine.IndexOperation.kAdd, false, false);
		Integer key = IndexUtils.getIndexIdFromDictionary(Product.Attribute.kExperienceId, experienceId.toString());
		ProductAttributeIndex index=ProductDB.getInstance().getOptIndex(Product.Attribute.kExperienceId, experienceId);
		SortedSet<Handle> results = index.get(key);
		
		for(Handle h:results){
			System.out.println(h.getOid());
		}
		Assert.assertNotNull(ProductDB.getInstance().getOptIndex(Product.Attribute.kExperienceId, experienceId));
		ProductDB.getInstance().deleteAllOptIndexesForExperience(experienceId);
		Assert.assertNull(ProductDB.getInstance().getOptIndex(Product.Attribute.kExperienceId, experienceId));	
	}
}
