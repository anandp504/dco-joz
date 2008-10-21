package com.tumri.joz.server.domain;

import com.tumri.utils.tcp.server.domain.QueryResponseData;

/**
 * Wrapper designed to send info over the wire during JozQARequst/Response communication.
 * User: scbraun
 * Date: Oct 10, 2008
 * Time: 11:26:47 AM
 */
public class JozQAResponseWrapper extends QueryResponseData {
	public static final String KEY_ERROR = "ERROR"; //if an exception occors
	public static final String KEY_QAREPORTDETAIL = "QAREPORT"; //hashmap advertiser-->num tspecs run

}
