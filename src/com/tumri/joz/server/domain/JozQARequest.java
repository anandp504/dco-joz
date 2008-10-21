package com.tumri.joz.server.domain;

import com.tumri.utils.tcp.server.domain.QueryId;
import com.tumri.utils.tcp.server.domain.QueryInputData;

import java.util.List;

/**
 * Internal representation of an ArrayList of Advertisers for which Joz will test Recipes.
 * This will be set using the setAdvertisers(ArrayList<String>) method of JozQARequest. 
 * User: scbraun
 * Date: Oct 10, 2008
 * Time: 11:26:23 AM
 */
public class JozQARequest extends QueryInputData {
	public static final String KEY_ADVERTISERS="ADVERTISERS"; //comma separated list of advertisers, no advertisers implies all advertisers
	public JozQARequest() {
        super();
    }
	/**
	 * Add the advertisers for which the QA report should be generated.
	 * @param advertisers ArrayList<String> of advertiser/clientNames for which Joz generates a QA report
	 */
	public void setAdvertisers(List<String> advertisers) {
		String commaList = "";
		for(int i = 0; i < advertisers.size(); i++){
			String advertiser = advertisers.get(i);
			if(advertiser != null && !"".equals(advertiser.trim())){
				if(i == 0){
					commaList += advertiser.trim();
				} else {
					commaList += "," + advertiser.trim();
				}
			}
		}
		requestMap.put(KEY_ADVERTISERS, commaList);
	}

	public String getAdvertisers(){
		return requestMap.get(KEY_ADVERTISERS);
	}

	public QueryId getQueryId() {
		return QueryId.JOZ_QA;
	}
}
