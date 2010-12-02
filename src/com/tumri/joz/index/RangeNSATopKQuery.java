package com.tumri.joz.index;

import com.tumri.joz.filter.IFilter;
import com.tumri.joz.products.Handle;
import com.tumri.joz.ranks.IWeight;
import com.tumri.utils.topk.NSATopKQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: scbraun
 * Date: Nov 29, 2010
 */
public class RangeNSATopKQuery<Value> extends NSATopKQuery<Range<Value>> {
	private ArrayList<IFilter<Range<Value>>> m_filters;
	private ArrayList<IWeight<Range<Value>>> m_filtersWeight;
	private ArrayList<SortedSet<Range<Value>>> m_excludes;
	private ArrayList<IWeight<Range<Value>>> m_excludesWeight;

	RangeNSATopKQuery(List<SortedSet<Range<Value>>> baseSets, int numReqs, boolean strict) {
		super(baseSets, numReqs, strict);
	}

	RangeNSATopKQuery(RangeNSATopKQuery set) {
		super(set);
	}

	@Override
	protected double modifyScore(Range<Value> h) {
		double wt = 1.0;
		int i = 0;
		for (IFilter<Range<Value>> lFilter : m_filters) {
			if (lFilter.accept(h)) {
				wt *= m_filtersWeight.get(i).getWeight(h);
			} else if (m_filtersWeight.get(i).mustMatch()) {
				return 0.0;
			}
			i++;
		}
		i = 0;
		for (SortedSet<Range<Value>> lExclude : m_excludes) {
			if (lExclude.contains(h)) {
				if (m_excludesWeight.get(i).mustMatch()) {
					return 0.0;
				} else {
					wt *= m_excludesWeight.get(i).getWeight(h);
				}
			}
			i++;
		}
		return wt;
	}

	@Override
	protected double getMaxScoreModification() {
		return 100;
	}

	@Override
	protected Range<Value> associateScore(Range<Value> h, Double score) {
		return h;
	}

	@Override
	public NSATopKQuery<Range<Value>> clone() throws CloneNotSupportedException {
		return new RangeNSATopKQuery(this);
	}

	void setM_filters(ArrayList<IFilter<Range<Value>>> m_filters) {
		this.m_filters = m_filters;
	}

	void setM_filtersWeight(ArrayList<IWeight<Range<Value>>> m_filtersWeight) {
		this.m_filtersWeight = m_filtersWeight;
	}

	void setM_excludes(ArrayList<SortedSet<Range<Value>>> m_excludes) {
		this.m_excludes = m_excludes;
	}

	void setM_excludesWeight(ArrayList<IWeight<Range<Value>>> m_excludesWeight) {
		this.m_excludesWeight = m_excludesWeight;
	}
}
