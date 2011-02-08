package com.tumri.joz.monitor;

import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.server.domain.JozAdResponse;
import com.tumri.joz.server.handlers.JozAdRequestHandler;
import com.tumri.utils.tcp.server.handlers.InvalidRequestException;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * JoZ Eval Monitor.
 *
 * @author Ramki
 */
public class EvalMonitor extends ComponentMonitor {
	private static Logger log = Logger.getLogger(EvalMonitor.class);
	private HashMap<String, String> keys = null;

	public EvalMonitor() {
		super("eval", new EvalMonitorStatus("eval"));
		keys = new HashMap<String, String>();
		keys.put(":" + JozAdRequest.KEY_THEME, JozAdRequest.KEY_THEME);
		keys.put(":" + JozAdRequest.KEY_AD_HEIGHT, JozAdRequest.KEY_AD_HEIGHT);
		keys.put(":" + JozAdRequest.KEY_AD_TYPE, JozAdRequest.KEY_AD_TYPE);
		keys.put(":" + JozAdRequest.KEY_AD_WIDTH, JozAdRequest.KEY_AD_WIDTH);
		keys.put(":" + JozAdRequest.KEY_AD_OFFER_TYPE, JozAdRequest.KEY_AD_OFFER_TYPE);
		keys.put(":" + JozAdRequest.KEY_ALLOW_TOO_FEW_PRODUCTS, JozAdRequest.KEY_ALLOW_TOO_FEW_PRODUCTS);
		keys.put(":" + JozAdRequest.KEY_AREACODE, JozAdRequest.KEY_AREACODE);
		keys.put(":" + JozAdRequest.KEY_CATEGORY, JozAdRequest.KEY_CATEGORY);
		keys.put(":" + JozAdRequest.KEY_CITY, JozAdRequest.KEY_CITY);
		keys.put(":" + JozAdRequest.KEY_COUNTRY, JozAdRequest.KEY_COUNTRY);
		keys.put(":" + JozAdRequest.KEY_DMACODE, JozAdRequest.KEY_DMACODE);
		keys.put(":" + JozAdRequest.KEY_KEYWORDS, JozAdRequest.KEY_KEYWORDS);
		keys.put(":" + JozAdRequest.KEY_LATITUDE, JozAdRequest.KEY_LATITUDE);
		keys.put(":" + JozAdRequest.KEY_LOCATION_ID, JozAdRequest.KEY_LOCATION_ID);
		keys.put(":" + JozAdRequest.KEY_LONGITUDE, JozAdRequest.KEY_LONGITUDE);
		keys.put(":" + JozAdRequest.KEY_MAX_PROD_DESC_LEN, JozAdRequest.KEY_MAX_PROD_DESC_LEN);
		keys.put(":" + JozAdRequest.KEY_MIN_NUM_LEADGENS, JozAdRequest.KEY_MIN_NUM_LEADGENS);
		keys.put(":f1", JozAdRequest.KEY_EXTERNAL_FILTER_FIELD1);
		keys.put(":f2", JozAdRequest.KEY_EXTERNAL_FILTER_FIELD2);
		keys.put(":f3", JozAdRequest.KEY_EXTERNAL_FILTER_FIELD3);
		keys.put(":f4", JozAdRequest.KEY_EXTERNAL_FILTER_FIELD4);
		keys.put(":f5", JozAdRequest.KEY_EXTERNAL_FILTER_FIELD5);
		keys.put(":t1", JozAdRequest.KEY_EXTERNAL_TARGET_FIELD1);
		keys.put(":t2", JozAdRequest.KEY_EXTERNAL_TARGET_FIELD2);
		keys.put(":t3", JozAdRequest.KEY_EXTERNAL_TARGET_FIELD3);
		keys.put(":t4", JozAdRequest.KEY_EXTERNAL_TARGET_FIELD4);
		keys.put(":t5", JozAdRequest.KEY_EXTERNAL_TARGET_FIELD5);
		keys.put(":" + JozAdRequest.KEY_NUM_PRODUCTS, JozAdRequest.KEY_NUM_PRODUCTS);
		keys.put(":" + JozAdRequest.KEY_RECIPE_ID, JozAdRequest.KEY_RECIPE_ID);
		keys.put(":" + JozAdRequest.KEY_REGION, JozAdRequest.KEY_REGION);
		keys.put(":" + JozAdRequest.KEY_REVERT_TO_DEFAULT_REALM, JozAdRequest.KEY_REVERT_TO_DEFAULT_REALM);
		keys.put(":" + JozAdRequest.KEY_ROW_SIZE, JozAdRequest.KEY_ROW_SIZE);
		keys.put(":" + JozAdRequest.KEY_SCRIPT_KEYWORDS, JozAdRequest.KEY_SCRIPT_KEYWORDS);
		keys.put(":" + JozAdRequest.KEY_STORE_ID, JozAdRequest.KEY_STORE_ID);
		keys.put(":" + JozAdRequest.KEY_T_SPEC, JozAdRequest.KEY_T_SPEC);
		keys.put(":" + JozAdRequest.KEY_URL, JozAdRequest.KEY_URL);
		keys.put(":" + JozAdRequest.KEY_WHICH_ROW, JozAdRequest.KEY_WHICH_ROW);
		keys.put(":" + JozAdRequest.KEY_ZIP_CODE, JozAdRequest.KEY_ZIP_CODE);
		keys.put(":" + JozAdRequest.KEY_EXTERNAL_PAGE_ID, JozAdRequest.KEY_EXTERNAL_PAGE_ID);
		keys.put(":" + JozAdRequest.KEY_USER_BUCKET, JozAdRequest.KEY_USER_BUCKET);
		keys.put(":" + JozAdRequest.KEY_AGE, JozAdRequest.KEY_AGE);
		keys.put(":" + JozAdRequest.KEY_BT, JozAdRequest.KEY_BT);
		keys.put(":" + JozAdRequest.KEY_GENDER, JozAdRequest.KEY_GENDER);
		keys.put(":" + JozAdRequest.KEY_RETARGETING_UT1, JozAdRequest.KEY_RETARGETING_UT1);
		keys.put(":" + JozAdRequest.KEY_RETARGETING_UT2, JozAdRequest.KEY_RETARGETING_UT2);
		keys.put(":" + JozAdRequest.KEY_RETARGETING_UT3, JozAdRequest.KEY_RETARGETING_UT3);
		keys.put(":" + JozAdRequest.KEY_RETARGETING_UT4, JozAdRequest.KEY_RETARGETING_UT4);
		keys.put(":" + JozAdRequest.KEY_RETARGETING_UT5, JozAdRequest.KEY_RETARGETING_UT5);
		keys.put(":" + JozAdRequest.KEY_CHILD_COUNT, JozAdRequest.KEY_CHILD_COUNT);
		keys.put(":" + JozAdRequest.KEY_HOUSEHOLD_INCOME, JozAdRequest.KEY_HOUSEHOLD_INCOME);
		keys.put(":" + JozAdRequest.KEY_TOPK, JozAdRequest.KEY_TOPK);
		keys.put(":" + JozAdRequest.KEY_USER_AGENT, JozAdRequest.KEY_USER_AGENT);
		keys.put(":" + JozAdRequest.KEY_USER_BUCKET, JozAdRequest.KEY_USER_BUCKET);
	}

	public JozAdRequest makeRequest(String requestString) {
		StringTokenizer reqTokenizer = new StringTokenizer(requestString);
		String key = "";
		String value = "";
		JozAdRequest req = new JozAdRequest();
		String cToken = "";
		while (reqTokenizer.hasMoreTokens()) {

			if (keys.containsKey(cToken)) {
				if (!"".equals(value)) {
					req.setValue(keys.get(key), value);
					value = "";
				}
				key = cToken;
				cToken = reqTokenizer.nextToken();
			} else {
				if (!"".equals(value)) {
					value += " ";
				}
				value += cToken;
				cToken = reqTokenizer.nextToken();
			}

		}
		if (!"".equals(value)) {
			value += " ";
		}
		req.setValue(keys.get(key), value + cToken);
		return req;
	}

	public JozAdResponse getResponse(JozAdRequest req) {
		JozAdRequestHandler handler = new JozAdRequestHandler();
		JozAdResponse resp = null;
		try {
			resp = handler.query(req);
		} catch (InvalidRequestException e) {
			log.error("Error caught during tspec evaluation", e);
		}
		return resp;
	}

	public MonitorStatus getStatus(String arg) {
		return null;
	}
}
