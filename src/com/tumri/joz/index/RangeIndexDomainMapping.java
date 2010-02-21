package com.tumri.joz.index;

import com.tumri.utils.data.RWLockedSortedArraySet;
import com.tumri.utils.data.RWLockedSortedSet;

import java.util.Comparator;
import java.util.SortedSet;

/**
 * It is required that this class is comparable on Key key and not on ranges.
 * User: scbraun
 * Date: Feb 9, 2010
 * Time: 12:53:45 PM
 */
public class RangeIndexDomainMapping<Key> implements Comparable, Comparator {
	private Key key;
	private RWLockedSortedSet<Range<Key>> ranges;

	public RangeIndexDomainMapping(Key k, SortedSet<Range<Key>> vals) {
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
	public RangeIndexDomainMapping(Key k) {
		key = k;
	}

	public RangeIndexDomainMapping(Key k, Range Range) {
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
	public RWLockedSortedSet<Range<Key>> getRanges() {
		return ranges;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RangeIndexDomainMapping<Key> rdm = (RangeIndexDomainMapping<Key>) o;
		return key.equals(rdm.getKey());
	}

	public int compare(Object o, Object o1) {
		if (o == o1) return 0;
		if (o == null || getClass() != o.getClass()) return -1;
		if (o1 == null || getClass() != o1.getClass()) return 1;
		RangeIndexDomainMapping m1 = (RangeIndexDomainMapping) o;
		RangeIndexDomainMapping m2 = (RangeIndexDomainMapping) o1;
		return ((Comparable) m1.getKey()).compareTo((Comparable) m2.getKey());
	}

	public int compareTo(Object o) {
		if (o == this) return 0;
		if (o == null || getClass() != o.getClass()) return -1;
		RangeIndexDomainMapping m1 = (RangeIndexDomainMapping) o;
		return ((Comparable) key).compareTo((Comparable) (m1.getKey()));
	}
}
