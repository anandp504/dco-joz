package com.tumri.joz.monitor;

import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.server.domain.JozAdResponse;
import com.tumri.utils.Pair;

import java.util.concurrent.atomic.AtomicReference;

/**
 * AdRequestMonitor to monitor the last request and response processed by Jz
 * User: scbraun
 * Date: Sep 17, 2008
 * Time: 12:18:33 PM
 */
public class AdRequestMonitor {
	private AtomicReference reqRespPair=new AtomicReference();
	private static AdRequestMonitor instance=null;


	public static AdRequestMonitor getInstance() {
		if (null == instance) {
			synchronized(AdRequestMonitor.class) {
				if (null == instance) {
					instance=new AdRequestMonitor();
				}
			}
		}
		return instance;
	}

    @SuppressWarnings("unchecked")
    public void setReqResp(JozAdRequest req, JozAdResponse resp){
		 reqRespPair.set(new Pair<JozAdRequest, JozAdResponse>(req, resp));
	}

    @SuppressWarnings("unchecked")
    public Pair<JozAdRequest, JozAdResponse> getReqResp(){
		return (Pair)reqRespPair.get();
	}
}
