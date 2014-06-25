/**
 * 
 */
package com.tumri.joz.rules;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

import junit.framework.Assert;

import org.junit.Test;

import com.tumri.content.data.Product;
import com.tumri.joz.jozMain.JozListingProviderImpl;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.ProductHandle;
import com.tumri.joz.utils.IndexUtils;
import com.tumri.lls.client.response.ListingResponse;
import com.tumri.utils.Pair;
import com.tumri.utils.data.SortedBag;
import com.tumri.utils.data.SortedListBag;

/**
 * @author omprakash
 * @date Jun 4, 2014
 * @time 11:31:52 AM
 */
public class ListingClauseTest {

	@Test
	public void test() {
		// considered attr for LC are kId,kBrand,kCategory,kSupplier,kGlobalId,kKeywords.
		ListingClause listClause1 = new ListingClause("kSupplier", "-456611");
		ListingClause listClause2 = new ListingClause("kSupplier", "-456613");	// invalid listing clause as no dict index created.
		ListingClause listClause3 = new ListingClause("kGender", "Male");
		ListingClause listClause5 = new ListingClause("kGlobalId", "US111111");
		
		{
			ListingClause listClause4 = new ListingClause(listClause1);
			Set<String> res1 = listClause1.getListingClause("kSupplier");
			Set<String> res4 = listClause4.getListingClause("kSupplier");
			Assert.assertEquals(res1, res4);
			System.out.println(listClause1.toString());
		}
		{
			try {
				ListingClause noListClause = new ListingClause("kProvider","BestBuy");
			} catch (UnsupportedOperationException exp) {
				System.out.println(exp.getMessage());
			}
		}

			SortedBag<Pair<ListingClause, Double>> Clauses = new SortedListBag<Pair<ListingClause, Double>>();
			Pair<ListingClause, Double> pair1 = new Pair<ListingClause, Double>(listClause1, 1.0);
			Pair<ListingClause, Double> pair2 = new Pair<ListingClause, Double>(listClause2, 2.0);
			Pair<ListingClause, Double> pair3 = new Pair<ListingClause, Double>(listClause3, 3.0);
			Pair<ListingClause, Double> pair5 = new Pair<ListingClause, Double>(listClause5, 5.0);
			
			Clauses.add(pair1);
			Clauses.add(pair2);
			Clauses.add(pair3);
			Clauses.add(pair5);
			
		{
			TreeMap<Integer, ArrayList<Handle>> mindex = new TreeMap<Integer, ArrayList<Handle>>();
			Integer val = IndexUtils.getIndexIdFromDictionary(Product.Attribute.kSupplier, "-456611");
			ArrayList<Handle> handlesForInt = new ArrayList<Handle>();
			ProductHandle ph0 = new ProductHandle(1.0, 37319266L);
			ProductHandle ph1 = new ProductHandle(1.0, 37319279L);
			ProductHandle ph2 = new ProductHandle(1.0, 37319284L);
			handlesForInt.add(ph0);
			handlesForInt.add(ph1);
			handlesForInt.add(ph2);
			mindex.put(val, handlesForInt);
			ProductDB.getInstance().updateIntegerIndex(Product.Attribute.kSupplier, mindex);

			TreeMap<Integer, ArrayList<Handle>> index = new TreeMap<Integer, ArrayList<Handle>>();
			Integer key = IndexUtils.getIndexIdFromDictionary(Product.Attribute.kGlobalId, "US111111");
			ArrayList<Handle> handlesForId = new ArrayList<Handle>();
			ProductHandle ph20 = new ProductHandle(2.0, 37319260L);
			ProductHandle ph21 = new ProductHandle(2.0, 37319270L);
			ProductHandle ph22 = new ProductHandle(2.0, 37319280L);
			handlesForId.add(ph20);
			handlesForId.add(ph21);
			handlesForId.add(ph22);
			index.put(key, handlesForId);
			ProductDB.getInstance().updateIntegerIndex(Product.Attribute.kGlobalId, index);
			Assert.assertNotNull(ListingClauseUtils.validateListingClauses(Clauses));
		}
	}
}
