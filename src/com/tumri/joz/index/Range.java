package com.tumri.joz.index;

import com.tumri.joz.index.IntegerRangeValue;
import com.tumri.utils.strings.StringTokenizer;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

/**
 * Indexable object that is used to hold a range. This can be used to support any data type that can be represented in
 * a series.
 * <p/>
 * Note that the index creation logic should make sure that we dont have overlap of ranges. Else this will break the
 * comparison logic. We got to make sure that a range will be repeated twice in the index
 */
public class Range<Value> implements Comparable, Comparator {

	private IRangeValue<Value> min;

	private IRangeValue<Value> max;

	public Range(IRangeValue<Value> min, IRangeValue<Value> max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Range range = (Range) o;
		return min.equals(range.min) && max.equals(range.max);
	}

	@Override
	public int hashCode() {
		int result = min != null ? min.hashCode() : 0;
		result = 31 * result + (max != null ? max.hashCode() : 0);
		return result;
	}

	public int compare(Object o, Object o1) {
		Range<Value> r = (Range<Value>) o;
		Range<Value> r1 = (Range<Value>) o1;
		if (r.equals(r1)) {
			return 0;
		} else if (r.min.equals(r1.min)) {
			if (r.max.greaterThanEqualTo(r1.max)) {
				return 1;
			} else {
				return -1;
			}
		} else if (r.min.greaterThanEqualTo(r1.min)) {
			return 1;
		} else {
			return -1;
		}
	}

	public boolean contains(IRangeValue<Value> valToCompare) {
		if (min == null || max == null || valToCompare == null) {
			return false;
		}
		if (min.lessThanEqualTo(valToCompare) && max.greaterThanEqualTo(valToCompare)) {
			return true;
		}
		return false;
	}


	public int compareTo(Object o) {
		Range<Value> r1 = (Range<Value>) o;
		if (this.equals(r1)) {
			return 0;
		} else if (this.min.equals(r1.min)) {
			if (this.max.greaterThanEqualTo(r1.max)) {
				return 1;
			} else {
				return -1;
			}
		} else if (this.min.greaterThanEqualTo(r1.min)) {
			return 1;
		} else {
			return -1;
		}
	}

	public IRangeValue<Value> getMin() {
		return min;
	}

	public IRangeValue<Value> getMax() {
		return max;
	}

	public Range() {

	}

	@Test
	public void test() {
		IRangeValue a1 = new IntegerRangeValue(3);
		IRangeValue a2 = new IntegerRangeValue(6);
		Range r1 = new Range(a1, a2);

		IRangeValue b1 = new IntegerRangeValue(1);
		IRangeValue b2 = new IntegerRangeValue(2);
		Range r2 = new Range(b1, b2);

		IRangeValue c1 = new IntegerRangeValue(7);
		IRangeValue c2 = new IntegerRangeValue(7);
		Range r3 = new Range(c1, c2);

		IRangeValue d1 = new IntegerRangeValue(8);
		IRangeValue d2 = new IntegerRangeValue(10);
		Range r4 = new Range(d1, d2);

		IRangeValue e1 = new IntegerRangeValue(4);
		IRangeValue e2 = new IntegerRangeValue(5);
		Range r5 = new Range(e1, e2);

		TreeMap<Range, Integer> map = new TreeMap<Range, Integer>();
		map.put(r3, 3);
		map.put(r2, 2);
		map.put(r1, 1);
		map.put(r4, 4);

		Assert.assertTrue(map.size() == 4);

		Integer t = map.get(r5);

		System.out.println(t);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("min: ");
		sb.append(min);
		sb.append(", max: ");
		sb.append(max);
		return sb.toString();
	}

}
