package com.tumri.joz.campaign.wm;

import com.tumri.joz.campaign.wm.WMAttribute;
import com.tumri.joz.index.AbstractRangeIndex;
import com.tumri.joz.index.Range;
import org.junit.Test;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: scbraun
 * Date: Feb 9, 2010
 * Time: 11:47:06 AM
 */
public class WMRangeIndex<V, Value> extends AbstractRangeIndex<WMAttribute, V, Value> {

	private WMAttribute type;

	public WMRangeIndex() {
	}

	public WMRangeIndex(WMAttribute type) {
		this.type = type;
	}

	public WMAttribute getType() {
		return type;
	}

	@Override
	public List<Map.Entry<Range<V>, Value>> getEntries(Value p) {
		return null;
	}

	public static List<WMAttribute> getAllowdAttributes() {
		List<WMAttribute> retList = new ArrayList<WMAttribute>();
		retList.add(WMAttribute.ub);
		return retList;
	}

	@Test
	public void test() {
//		SortedSet<Integer> ss = new SortedArraySet<Integer>();
//		ss.add(1);
//		ss.add(2);
//		ss.add(3);
//		ss.add(4);
//		ss.add(7);
//		ss.add(10);
//
//		SortedSet<Integer> tailSet = ss.tailSet(5);
//		Integer tailFirst = tailSet.first();
//		Integer tailLast = tailSet.last();
//		SortedSet<Integer> headSet = ss.headSet(5);
//		Integer headFirst = headSet.first();
//		Integer headLast = headSet.last();
//
//		System.out.println("tailSet: " + printSet(tailSet));
//		System.out.println("tailFirst: " + tailFirst);
//		System.out.println("tailLast: " + tailLast);
//		System.out.println("headSet: " + printSet(headSet));
//		System.out.println("headFirst: " + headFirst);
//		System.out.println("headLast: " + headLast);


		Range<Integer> r1 = new Range<Integer>(1, 4);
		Range<Integer> r2 = new Range<Integer>(3, 4);

//		Range r3 = new Range(2, 2);
//
//		Range r4 = new Range(3, 7);
//
//		Range r5 = new Range(2, 2);

		Range<Integer> r6 = new Range<Integer>(1, 1);

		WMRangeIndex<Integer, String> rangeIndex = new WMRangeIndex<Integer, String>(WMAttribute.ub);

		rangeIndex.put(r1, "Range1");
		rangeIndex.put(r2, "Range2");
		rangeIndex.materialize();
//		rangeIndex.put(r3, "Range3");
//		rangeIndex.put(r4, "Range4");

		SortedSet<String> s = rangeIndex.get(r6);

		System.out.println(printSet(s));
	}

	private String printSet(SortedSet<?> ss) {
		StringBuilder sb = new StringBuilder();
		Iterator i = ss.iterator();
		int k = 0;
		while (i.hasNext()) {
			sb.append("element" + k + ": " + i.next() + "; ");
			k++;
		}
		return sb.toString();
	}


}
