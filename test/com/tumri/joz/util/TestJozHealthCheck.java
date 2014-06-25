/**
 * 
 */
package com.tumri.joz.util;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tumri.content.data.Product;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.CampaignDBCompleteRefreshImpl;
import com.tumri.joz.campaign.CampaignDBDataLoader;
import com.tumri.joz.campaign.CampaignDataLoadingException;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.jozMain.JozData;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.utils.HealthCheckUtils;
import com.tumri.joz.utils.IndexUtils;

/**
 * @author omprakash
 * @date May 30, 2014
 * @time 3:36:44 PM
 */
public class TestJozHealthCheck {

	@BeforeClass
	public static void init() throws Exception{
		CampaignDBDataLoader loader = CampaignDBDataLoader.getInstance();
        try {
            loader.loadData();
            System.out.println("campaign loaded !");
        } catch (CampaignDataLoadingException e) {
            throw new RuntimeException(e);
        }
        if(ProductDB.hasProductInfo() || (!(ProductDB.getInstance().isEmpty())));
        ProductDB.getInstance().clearProductDB();
       
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
		TreeMap<Long, ArrayList<Handle>> mindex = new TreeMap<Long, ArrayList<Handle>>();
		mindex.put(key, handlesForLong);
		// this updates the m_map of AttributeIndex and ProductDB
		ProductDB.getInstance().updateLongIndex(Product.Attribute.kMultiValueTextField, mindex);
	}
	@AfterClass
	public static void cleanup() throws Exception{
		 if(ProductDB.hasProductInfo() || (!(ProductDB.getInstance().isEmpty())));
	        ProductDB.getInstance().clearProductDB();
	}
	
	@Test
	public void test(){
		// Atleast one campaign must be loaded
		Assert.assertFalse(CampaignDB.getInstance().isEmpty());
		// atleast one product must be loaded
		Assert.assertNotNull(ProductDB.getInstance().getAll());
		
		boolean bStatus = true;
		 ProductAttributeIndex provIndex=ProductDB.getInstance().getIndex(IProduct.Attribute.kMultiValueTextField);
         if (provIndex==null || provIndex.getKeys().isEmpty()) {
             bStatus = false;
         }
         // The joz Provider index must not be empty
         Assert.assertTrue(bStatus);
         Assert.assertNotNull(ProductDB.getInstance().getAll());
         Assert.assertFalse(HealthCheckUtils.doHealthCheck());    
	}
}
