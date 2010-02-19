package com.tumri.joz.index;

import com.tumri.utils.data.RWLockedSortedArraySet;
import com.tumri.utils.data.RWLockedSortedSet;
import com.tumri.utils.data.SortedArraySet;

import java.util.Comparator;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: scbraun
 * Date: Feb 9, 2010
 * Time: 12:53:45 PM
 */
public class RangeDomainMapping<Key, Range> implements Comparable, Comparator {
	private Key key;
	private RWLockedSortedSet<Range> ranges;

	public RangeDomainMapping(Key k, SortedSet<Range> vals) {
		key = k;
		ranges = new RWLockedSortedArraySet();
		if (vals != null) {
			ranges.writerLock();
			try {
				ranges.addAll(vals);
			} finally {
				ranges.writerUnlock();
			}
		}
	}

	/**
	 * This is used for comparisons--since key is only required for equals() --specifically, used in set lookups.
	 *
	 * @param k
	 */
	public RangeDomainMapping(Key k) {
		key = k;
	}

	public RangeDomainMapping(Key k, Range Range) {
		key = k;
		ranges = new RWLockedSortedArraySet();
		ranges.writerLock();
		try {
			ranges.add(Range);
		} finally {
			ranges.writerUnlock();
		}
	}

	public void addRange(Range Range) {
		if (ranges == null) {
			ranges = new RWLockedSortedArraySet();
		}
		ranges.writerLock();
		try {
			ranges.add(Range);
		} finally {
			ranges.writerUnlock();
		}
	}

	public void removeRange(Range Range) {
		if (ranges != null) {
			ranges.writerLock();
			try {
				ranges.remove(Range);
			} finally {
				ranges.writerUnlock();
			}
		}
	}

	public Key getKey() {
		return key;
	}

	/**
	 * whoever gets this should use rwlocks on set
	 *
	 * @return
	 */
	public RWLockedSortedSet<Range> getRanges() {
		return ranges;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RangeDomainMapping<Key, Range> rdm = (RangeDomainMapping<Key, Range>) o;
		return key.equals(rdm.getKey());
	}

	public int compare(Object o, Object o1) {
		if (o == o1) return 0;
		if (o == null || getClass() != o.getClass()) return -1;
		if (o1 == null || getClass() != o1.getClass()) return 1;
		RangeDomainMapping m1 = (RangeDomainMapping) o;
		RangeDomainMapping m2 = (RangeDomainMapping) o1;
		return ((Comparable) m1.getKey()).compareTo((Comparable) m2.getKey());
	}

	public int compareTo(Object o) {
		if (o == this) return 0;
		if (o == null || getClass() != o.getClass()) return -1;
		RangeDomainMapping m1 = (RangeDomainMapping) o;
		return ((Comparable) key).compareTo((Comparable) (m1.getKey()));
	}
}
