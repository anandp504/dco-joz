package com.tumri.joz.Query;

import com.tumri.joz.filter.IFilter;
import com.tumri.joz.products.Handle;
import com.tumri.joz.ranks.IWeight;

import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public abstract class TargetingQuery extends SimpleQuery implements IFilter<Handle>, IWeight<Handle> {

	public SortedSet<Handle> getAll() {
		//return CampaignDB.getInstance().getAll();
		return null; // @todo
	}

	public IFilter<Handle> getFilter() {
		return this;
	}

	public IWeight<Handle> getWeight() {
		return this;
	}

	public double getWeight(Handle v, double minWeight) {
		return v.getScore();
	}

	//this is max in TargetingScoreHelper

	public double getMaxWeight() {
		return 1.0;
	}

	public double getMinWeight() {
		return 1.0;
	}

	public int match(Handle v) {
		return 1;
	}

	public int getCount() {
		return exec().size();
	}

	public double getCost() {
		return getCount();
	}

	public boolean hasIndex() {
		return true;
	}


	public boolean mustMatch() {
		return false;
	}

	public void setWeight(IWeight<Handle> wt) {
		throw new UnsupportedOperationException("This is not supported");
	}
}
