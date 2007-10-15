package com.tumri.joz.monitor;

import java.util.Map;
import java.util.HashMap;
import com.tumri.joz.jozMain.AdDataRequest;

/**
 * Status description Class for Eval monitor
 * @author ramki
 */
public class EvalMonitorStatus extends MonitorStatus
{
	private AdDataRequest adDataReq=null;
	private Map<String, Long> productMatch=null;
	private Long totalProductMatch=null;
	private String strategy=null;

    public  EvalMonitorStatus(String name) {
        super(name);
		productMatch = new HashMap<String, Long>();
    }

    public void setAdDataRequest(AdDataRequest adDataReq) {
       this.adDataReq=adDataReq;
    }

    public void setTotalProductMatch(Long totalProductMatch) {
		this.totalProductMatch=totalProductMatch;
	}

    public void setProductMatch(Map<String, Long> productMatch) {
       this.productMatch.putAll(productMatch);
    }

    public void setStrategy(String strategy) {
		this.strategy=strategy;
	}

    public AdDataRequest getAdDataRequest() {
		return this.adDataReq;
	}

	public Long getTotalProductMatch() {
		return this.totalProductMatch;
	}

    public Map<String, Long> getProductMatch() {
		return this.productMatch;
	}

	public String getStrategy() {
		return this.strategy;
	}

    public String toHTML() {
		StringBuffer sb = new StringBuffer();
		sb.append("To be impelemented");
        return new String(sb);
    }
}
