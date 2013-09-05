package com.tumri.joz.Query;

import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.ranks.IWeight;
import com.tumri.joz.utils.AppProperties;
import com.tumri.utils.data.MultiSortedSet;
import com.tumri.utils.data.SortedArraySet;

import java.util.SortedSet;

/**
 * User: scbraun
 * Date: 8/27/13
 */
public class EnvFlashTargetingQuery extends TargetingQuery {

	public EnvFlashTargetingQuery() {
	}

	public Type getType() {
		return Type.kEnvFlash;
	}

	public SortedSet<Handle> exec() {
		SortedSet<Handle> envFlashResults      = execEnvFlashQuery();

		SortedSet<Handle> results = new SortedArraySet<Handle>();
		if(envFlashResults != null) {
			results = envFlashResults;
		}
		return results;
	}

	@SuppressWarnings({"unchecked"})
	private SortedSet<Handle> execEnvFlashQuery() {
		AtomicAdpodIndex index = CampaignDB.getInstance().getAdpodENVIndex();
		SortedSet<Handle> results = index.get(AppProperties.getInstance().getTargetingFlashEnv());
		return results;
	}

	public boolean accept(Handle v) {
		return false;
	}

	@Override
	public boolean mustMatch(){
		return false;
	}

	@Override
	public IWeight<Handle> getWeight() {
		return this;
	}

	@Override
	public double getWeight(Handle v, double minWeight) {
		return 1.6;
	}

	//this is max in TargetingScoreHelper

	@Override
	public double getMaxWeight() {
		return 1.6;
	}

	@Override
	public double getMinWeight() {
		return 1.6;
	}

}
