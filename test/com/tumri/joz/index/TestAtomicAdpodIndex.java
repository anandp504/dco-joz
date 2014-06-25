/**
 * 
 */
package com.tumri.joz.index;

import junit.framework.Assert;
import org.junit.Test;
import java.util.*;

import com.tumri.utils.data.MultiSortedSet;

/**
 * @author omprakash
 * @date May 21, 2014
 * @time 2:54:49 PM
 */
public class TestAtomicAdpodIndex {

	@Test
	public void test0() {
		AdpodIndex<Integer, String> aindex_t = new AdpodIndex<Integer, String>(AdpodIndex.Attribute.kAdType);
		AdpodIndex<Integer, String> aindex_g = new AdpodIndex<Integer, String>(AdpodIndex.Attribute.kGender);
		AtomicAdpodIndex<Integer, String> atomicIndex = new AtomicAdpodIndex<Integer, String>(aindex_t);
		System.out.println(atomicIndex.getType());
		System.out.println(aindex_g.getType());
		
		atomicIndex.put(100, "hundred0");
		atomicIndex.put(100, "hundred1");
		atomicIndex.put(100, "hundred2");
		atomicIndex.put(100, "hundred0");
		atomicIndex.put(200, "TwoHundred");

		SortedSet<String> strings = atomicIndex.get(100);
		for (String str : strings)
			System.out.println(str);
		ArrayList<Integer> keys = new ArrayList<Integer>();
		{
			keys.add(100);
			keys.add(200);
			int count = atomicIndex.getCount(keys);
			Assert.assertEquals(4, count);
		}
		{
			SortedSet<String> results = atomicIndex.get(keys);
			System.out.println("results:");
			for (String result : results)
				System.out.println(result);
		}
		int rangeCount = atomicIndex.getCount(100, 201);
		{
			Assert.assertEquals(4, rangeCount);
			Assert.assertEquals(3, atomicIndex.getCount(50, 101));
			Assert.assertEquals(3, atomicIndex.getCount(100, 101));
			Assert.assertEquals(1, atomicIndex.getCount(101, 201));
		}
		{
			Map<Integer, List<String>> vmap = new HashMap<Integer, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("hundred00");
			values.add("hundred11");
			values.add("hundred22");
			vmap.put(111, values);
			atomicIndex.put(vmap);
		}
		{
			TreeSet<String> set1 = new TreeSet<String>();
			set1.add("one");
			set1.add("five");
			set1.add("nine");
			set1.add("twothree");
			set1.add("twofour");
			TreeSet<String> set2 = new TreeSet<String>();
			set2.add("one");
			set2.add("four");
			set2.add("eight");
			set2.add("twothree");
			set2.add("twofour");
			MultiSortedSet<String> set = new MultiSortedSet<String>();
			set.add(set1);
			set.add(set2);
			atomicIndex.put(222, set);
		}
		SortedSet<String> strings100 = atomicIndex.get(100);
		for (String str : strings100)
			System.out.println(str);
		SortedSet<String> strings111 = atomicIndex.get(111);
		for (String str : strings111)
			System.out.println(str);
		SortedSet<String> strings222 = atomicIndex.get(222);
		for (String str : strings222)
			System.out.println(str);
		{
			Assert.assertEquals(6, atomicIndex.getCount(100, 112));
			Assert.assertEquals(7, atomicIndex.getCount(100, 201));
			Assert.assertEquals(17, atomicIndex.getCount(100, 223));
			Assert.assertEquals(0, atomicIndex.getCount(50, 100));
		}
		
		{
			Assert.assertEquals(4, atomicIndex.getKeys().size());
			atomicIndex.remove(100);
			Assert.assertEquals(3, atomicIndex.getKeys().size());
		}
		
		{ 
			atomicIndex.set(aindex_g);
			int newrangeCount = atomicIndex.getCount(100, 201);
			Assert.assertNull(atomicIndex.get(100));
			System.out.println(newrangeCount);
			Assert.assertEquals(0, newrangeCount);
			Assert.assertEquals(0, atomicIndex.getCount(50, 101));
			Assert.assertEquals(0, atomicIndex.getCount(100, 101));
			Assert.assertEquals(0, atomicIndex.getCount(101, 201));
		}
		atomicIndex.clear();
		Assert.assertEquals(0, atomicIndex.getKeys().size());

	}
}
