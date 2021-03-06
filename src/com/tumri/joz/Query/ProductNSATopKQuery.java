package com.tumri.joz.Query;

import com.tumri.joz.filter.IFilter;
import com.tumri.joz.products.Handle;
import com.tumri.joz.ranks.IWeight;
import com.tumri.utils.topk.NSATopKQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * User: scbraun
 * Date: Nov 24, 2010
 */
public class ProductNSATopKQuery extends NSATopKQuery<Handle> {
	private ArrayList<IFilter<Handle>> m_filters;
	private ArrayList<IWeight<Handle>> m_filtersWeight;
	private ArrayList<SortedSet<Handle>> m_excludes;
	private ArrayList<IWeight<Handle>> m_excludesWeight;

	ProductNSATopKQuery(List<SortedSet<Handle>> baseSets, int numReqs, boolean strict, double alpha) {
		super(baseSets, numReqs, strict, alpha);
	}

	ProductNSATopKQuery(ProductNSATopKQuery set) {
		super(set);
	}

	@Override
	protected double modifyScore(Handle h, double minWeight) {
		double wt = 1.0;
		int i = 0;
		for (IFilter<Handle> lFilter : m_filters) {
			if (lFilter.accept(h)) {
				wt *= m_filtersWeight.get(i).getWeight(h, minWeight);
			} else if (m_filtersWeight.get(i).mustMatch()) {
				return 0.0;
			}
			i++;
		}
		i = 0;
		for (SortedSet<Handle> lExclude : m_excludes) {
			if (lExclude.contains(h)) {
				if (m_excludesWeight.get(i).mustMatch()) {
					return 0.0;
				} else {
					wt *= m_excludesWeight.get(i).getWeight(h, minWeight);
				}
			}
			i++;
		}
		return wt;
	}

	@Override
	protected double getMaxScoreModification() {
		double retWeight = 1.0;
		int i = 0;
		for (IFilter<Handle> lFilter : m_filters) {
			double tmpWeight = retWeight * m_filtersWeight.get(i).getMaxWeight();
			if (tmpWeight > retWeight) {
				retWeight = tmpWeight;
			}
			i++;
		}
		i = 0;
		for (SortedSet<Handle> lExclude : m_excludes) {
			double tmpWeight = retWeight * m_excludesWeight.get(i).getMaxWeight();
			if (tmpWeight > retWeight) {
				retWeight = tmpWeight;
			}
			i++;
		}
		return retWeight;
	}

	@Override
	protected Handle associateScore(Handle h, Double score) {
		return h.createHandle(score);
	}

	@Override
	public NSATopKQuery<Handle> clone() throws CloneNotSupportedException {
		return new ProductNSATopKQuery(this);
	}

	void setM_filters(ArrayList<IFilter<Handle>> m_filters) {
		this.m_filters = m_filters;
	}

	void setM_filtersWeight(ArrayList<IWeight<Handle>> m_filtersWeight) {
		this.m_filtersWeight = m_filtersWeight;
	}

	void setM_excludes(ArrayList<SortedSet<Handle>> m_excludes) {
		this.m_excludes = m_excludes;
	}

	void setM_excludesWeight(ArrayList<IWeight<Handle>> m_excludesWeight) {
		this.m_excludesWeight = m_excludesWeight;
	}
}
