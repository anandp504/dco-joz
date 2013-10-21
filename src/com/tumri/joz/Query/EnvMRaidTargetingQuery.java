package com.tumri.joz.Query;

import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.ranks.IWeight;
import com.tumri.joz.utils.AppProperties;
import com.tumri.utils.data.SortedArraySet;

import java.util.SortedSet;

/**
 * User: scbraun
 * Date: 10/17/13
 */
public class EnvMRaidTargetingQuery extends TargetingQuery {

	public EnvMRaidTargetingQuery() {
	}

	public Type getType() {
		return Type.kEnvMRaid;
	}

	public SortedSet<Handle> exec() {
		SortedSet<Handle> envFlashResults      = execEnvMRaidQuery();

		SortedSet<Handle> results = new SortedArraySet<Handle>();
		if(envFlashResults != null) {
			results = envFlashResults;
		}
		return results;
	}

	@SuppressWarnings({"unchecked"})
	private SortedSet<Handle> execEnvMRaidQuery() {
		AtomicAdpodIndex index = CampaignDB.getInstance().getAdpodENVIndex();
		SortedSet<Handle> results = index.get(AppProperties.getInstance().getTargetingMRaidEnv());
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
