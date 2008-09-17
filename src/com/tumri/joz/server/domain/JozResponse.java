package com.tumri.joz.server.domain;

import java.util.ArrayList;

/**
 * JozResponse is the default response object returned by all the Joz client api calls.
 * Depending upon the type of call different attributes of the class are populated.
 * <p>
 *
 * <ul>
 * <li>Taxonomy results are populated into the taxonomy attribute
 * <li>Merchant results are populated into the merchants attribute
 * <li>Provider results are populated into the providers attribute
 * <li>Count results are populated into the counts attribute
 * <li>All error messages and status updates are detailed in the status attribute
 * </ul>
 *
 * @author nipun
 * @author raghu
 * @version 1.0
 * @since 4.0
 *
 */
public class JozResponse {

    public static String JOZ_OPERATION_SUCCESS="Success";
    public static String JOZ_OPERATION_FAILURE="Failed";
    public static String KEY_ERROR="Failed";

    private JozTaxonomy taxonomy=null;
    private JozCounts counts=null;
    private ArrayList<JozMerchant> merchants = null;
    private ArrayList<JozProvider> providers = null;
    private ArrayList<JozAdvertiser> advertisers = null;
    private String status = null;
    private JozAdResponse adResponse = null;
    private ArrayList<JozCampaign> campaigns = null;

	public JozAdResponse getAdResponse() {
		return adResponse;
	}

	public void setAdResponse(JozAdResponse adResponse) {
		this.adResponse = adResponse;
	}

    public void setCounts(JozCounts counts) {
        this.counts = counts;
    }

    public void setMerchants(ArrayList<JozMerchant> merchants) {
        this.merchants = merchants;
    }

    public void setProviders(ArrayList<JozProvider> providers) {
        this.providers = providers;
    }

    public void setTaxonomy(JozTaxonomy taxonomy) {
        this.taxonomy = taxonomy;
    }

    public JozCounts getCounts() {
        return counts;
    }

    public ArrayList<JozMerchant> getMerchants() {
        return merchants;
    }

    public ArrayList<JozProvider> getProviders() {
        return providers;
    }

    public JozTaxonomy getTaxonomy() {
        return taxonomy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

	public ArrayList<JozCampaign> getCampaigns() {
		return campaigns;
	}

	public void setCampaigns(ArrayList<JozCampaign> campaigns) {
		this.campaigns = campaigns;
	}

	public ArrayList<JozAdvertiser> getAdvertisers() {
		return advertisers;
	}

	public void setAdvertisers(ArrayList<JozAdvertiser> advertisers) {
		this.advertisers = advertisers;
	}

}
