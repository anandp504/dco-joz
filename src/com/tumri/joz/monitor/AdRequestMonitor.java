package com.tumri.joz.monitor;

import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.server.domain.JozAdResponse;
import com.tumri.utils.Pair;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: scbraun
 * Date: Sep 17, 2008
 * Time: 12:18:33 PM
 * To change this template use File | Settings | File Templates.
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

	public void setReqResp(JozAdRequest req, JozAdResponse resp){
		 reqRespPair.set(new Pair<JozAdRequest, JozAdResponse>(req, resp));
	}

	public Pair<JozAdRequest, JozAdResponse> getReqResp(){
		Pair<JozAdRequest, JozAdResponse> pair = (Pair)reqRespPair.get();
		return pair;
	}
}
