package com.tumri.joz.index;

import com.tumri.joz.index.IRangeValue;

/**
 * Created by IntelliJ IDEA.
 * User: scbraun
 * Date: Feb 8, 2010
 * Time: 1:23:45 PM
 */
public class IntegerRangeValue implements IRangeValue<Integer>, Comparable {
	private Integer value;

	public IntegerRangeValue(int val) {
		value = val;
	}

	public IRangeValue<Integer> getNext() {
		IRangeValue<Integer> retVal = new IntegerRangeValue(value + 1);
		return retVal;
	}

	public IRangeValue<Integer> getPrevious() {
		IRangeValue<Integer> retVal = new IntegerRangeValue(value - 1);
		return retVal;
	}

	public boolean lessThanEqualTo(IRangeValue k) {
		Integer compInt = (Integer) k.getValue();
		if (compInt != null) {
			return (value <= compInt);
		}

		return false;
	}

	public boolean equals(Object k) {
		IRangeValue val = (IRangeValue) k;
		Integer compInt = (Integer) val.getValue();
		return compInt.equals(value);
	}

	public boolean greaterThanEqualTo(IRangeValue k) {
		Integer compInt = (Integer) k.getValue();
		if (compInt != null) {
			return (value >= compInt);
		}
		return false;
	}

	public Integer getValue() {
		return value;
	}

	public int compareTo(Object o) {
		if (this == o) return 0;
		if (o == null || getClass() != o.getClass()) return 1;
		IntegerRangeValue v = (IntegerRangeValue) o;
		if (value < (Integer) v.getValue()) {
			return -1;
		} else if (value > (Integer) v.getValue()) {
			return 1;
		}
		return 0;
	}

	public String toString() {
		return value.toString();
	}

}
