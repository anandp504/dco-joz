package com.tumri.joz.index;

import com.tumri.utils.Pair;
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
 * Ranges are equal only if min=min and max=max (first=first and last=last)
 * range r1 > r2 if r1.min > r2.min || (r1.min=r2.min && r1.max>r2.max)
 */
public class Range<Value> extends Pair<Value, Value> implements Comparator<Range<Value>> {

	Value min = null;
	Value max = null;

	public Range(Value min, Value max) {
		super(min, max);
		this.min = min;
		this.max = max;
	}

	public boolean contains(Value valToCompare) {
		if (max == null || max == null || valToCompare == null) {
			return false;
		}
		if (((Comparable) min).compareTo(valToCompare) > 0 || ((Comparable) max).compareTo(valToCompare) < 0) {
			return false;
		}
		return true;
	}

	public int compare(Range<Value> r, Range<Value> r1) {
		if (r.equals(r1)) {
			return 0;
		} else if (r.min.equals(r1.min)) {
			if (((Comparable) r.max).compareTo(r1.max) < 0) {
				return -1;
			} else {
				return 1;
			}
		} else if (((Comparable) r.min).compareTo(r1.min) < 0) {
			return -1;
		} else {
			return 1;
		}
	}


	@Override
	public int hashCode() {
		int result = min != null ? min.hashCode() : 0;
		result = 31 * result + (max != null ? max.hashCode() : 0);
		return result;
	}

	@Override
	public int compareTo(Pair<Value, Value> o) {
		Range<Value> r1 = (Range<Value>) o;
		if (this.equals(r1)) {
			return 0;
		} else if (this.min.equals(r1.min)) {
			if (((Comparable) this.max).compareTo(r1.max) < 0) {
				return -1;
			} else {
				return 1;
			}
		} else if (((Comparable) this.min).compareTo(r1.min) < 0) {
			return -1;
		} else {
			return 1;
		}
	}

	public Value getMin() {
		return min;
	}

	public Value getMax() {
		return max;
	}

	public Range() {
	}

	@Test
	public void test() {
		Range<Integer> r1 = new Range<Integer>(3, 6);
		Range<Integer> r2 = new Range<Integer>(1, 2);
		Range<Integer> r3 = new Range<Integer>(7, 7);
		Range<Integer> r4 = new Range<Integer>(8, 10);
		Range<Integer> r5 = new Range<Integer>(4, 5);

		TreeMap<Range<Integer>, Integer> map = new TreeMap<Range<Integer>, Integer>();
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
