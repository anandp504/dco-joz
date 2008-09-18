package com.tumri.joz.monitor;

import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.CmdGetAdData;
import com.tumri.joz.jozMain.Command;
import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.server.domain.JozAdResponse;
import com.tumri.joz.server.handlers.JozAdRequestHandler;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpIFASLReader;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.tcp.server.handlers.InvalidRequestException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
  * JoZ Eval Monitor.
  * @author Ramki
  */
public class EvalMonitor extends ComponentMonitor
{
	private static Logger log = Logger.getLogger(EvalMonitor.class);
	private Long totalProductMatch=null;
	private String strategy=null;
	private HashMap<String, String> keys = null;

	public EvalMonitor() {
	   super("eval", new EvalMonitorStatus("eval"));
		keys = new HashMap<String, String>();
		keys.put(":"+ JozAdRequest.KEY_THEME, JozAdRequest.KEY_THEME);
		keys.put(":"+JozAdRequest.KEY_AD_HEIGHT, JozAdRequest.KEY_AD_HEIGHT);
		keys.put(":"+JozAdRequest.KEY_AD_TYPE,JozAdRequest.KEY_AD_TYPE);
		keys.put(":"+JozAdRequest.KEY_AD_WIDTH, JozAdRequest.KEY_AD_WIDTH);
		keys.put(":"+JozAdRequest.KEY_AD_OFFER_TYPE, JozAdRequest.KEY_AD_OFFER_TYPE);
		keys.put(":"+JozAdRequest.KEY_ALLOW_TOO_FEW_PRODUCTS, JozAdRequest.KEY_ALLOW_TOO_FEW_PRODUCTS);
		keys.put(":"+JozAdRequest.KEY_AREACODE, JozAdRequest.KEY_AREACODE);
		keys.put(":"+JozAdRequest.KEY_CATEGORY, JozAdRequest.KEY_CATEGORY);
		keys.put(":"+JozAdRequest.KEY_CITY, JozAdRequest.KEY_CITY);
		keys.put(":"+JozAdRequest.KEY_COUNTRY, JozAdRequest.KEY_COUNTRY);
		keys.put(":"+JozAdRequest.KEY_DMACODE, JozAdRequest.KEY_DMACODE);
		keys.put(":"+JozAdRequest.KEY_KEYWORDS, JozAdRequest.KEY_KEYWORDS);
		keys.put(":"+JozAdRequest.KEY_LATITUDE, JozAdRequest.KEY_LATITUDE);
		keys.put(":"+JozAdRequest.KEY_LOCATION_ID, JozAdRequest.KEY_LOCATION_ID);
		keys.put(":"+JozAdRequest.KEY_LONGITUDE, JozAdRequest.KEY_LONGITUDE);
		keys.put(":"+JozAdRequest.KEY_MAX_PROD_DESC_LEN, JozAdRequest.KEY_MAX_PROD_DESC_LEN);
		keys.put(":"+JozAdRequest.KEY_MIN_NUM_LEADGENS, JozAdRequest.KEY_MIN_NUM_LEADGENS);
		keys.put(":"+JozAdRequest.KEY_MULTI_VALUE_FIELD1, JozAdRequest.KEY_MULTI_VALUE_FIELD1);
		keys.put(":"+JozAdRequest.KEY_MULTI_VALUE_FIELD2, JozAdRequest.KEY_MULTI_VALUE_FIELD2);
		keys.put(":"+JozAdRequest.KEY_MULTI_VALUE_FIELD3, JozAdRequest.KEY_MULTI_VALUE_FIELD3);
		keys.put(":"+JozAdRequest.KEY_MULTI_VALUE_FIELD4, JozAdRequest.KEY_MULTI_VALUE_FIELD4);
		keys.put(":"+JozAdRequest.KEY_MULTI_VALUE_FIELD5, JozAdRequest.KEY_MULTI_VALUE_FIELD5);
		keys.put(":"+JozAdRequest.KEY_NUM_PRODUCTS, JozAdRequest.KEY_NUM_PRODUCTS);
		keys.put(":"+JozAdRequest.KEY_RECIPE_ID, JozAdRequest.KEY_RECIPE_ID);
		keys.put(":"+JozAdRequest.KEY_REGION, JozAdRequest.KEY_REGION);
		keys.put(":"+JozAdRequest.KEY_REVERT_TO_DEFAULT_REALM, JozAdRequest.KEY_REVERT_TO_DEFAULT_REALM);
		keys.put(":"+ JozAdRequest.KEY_ROW_SIZE, JozAdRequest.KEY_ROW_SIZE);
		keys.put(":"+JozAdRequest.KEY_SCRIPT_KEYWORDS, JozAdRequest.KEY_SCRIPT_KEYWORDS);
		keys.put(":"+JozAdRequest.KEY_STORE_ID, JozAdRequest.KEY_STORE_ID);
		keys.put(":"+JozAdRequest.KEY_T_SPEC, JozAdRequest.KEY_T_SPEC);
		keys.put(":"+JozAdRequest.KEY_URL, JozAdRequest.KEY_URL);
		keys.put(":"+JozAdRequest.KEY_WHICH_ROW, JozAdRequest.KEY_WHICH_ROW);
		keys.put(":"+JozAdRequest.KEY_ZIP_CODE, JozAdRequest.KEY_ZIP_CODE);
	}

	public JozAdRequest makeRequest(String requestString){
		StringTokenizer reqTokenizer = new StringTokenizer(requestString);
		String key = "";
		String value = "";
		JozAdRequest req = new JozAdRequest();
	   	String cToken = "";
		while(reqTokenizer.hasMoreTokens()){

			if(keys.containsKey(cToken)){
				if(!"".equals(value)){
					req.setValue(keys.get(key), value);
					value = "";
				}
				key = cToken;
				cToken = reqTokenizer.nextToken();
			} else {
				if(!"".equals(value)){
					value += " ";
				}
				value += cToken;
				cToken = reqTokenizer.nextToken();
			}

		}
		req.setValue(keys.get(key), value + cToken);
		return req;
	}

	public JozAdResponse getResponse(JozAdRequest req){
		JozAdRequestHandler handler = new JozAdRequestHandler();
		JozAdResponse resp = null;
		try {
			resp = handler.query(req);
		} catch (InvalidRequestException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return resp;
	}

	public MonitorStatus getStatus(String getAdDataExpr) {
		Command cmd = null;
	    try {
			cmd = Command.parse(getAdDataExpr);
		}
		catch (Exception ex) {
			((EvalMonitorStatus)status).setFailed(true);
			((EvalMonitorStatus)status).setFailedMessage("Error - "+ ex.getMessage());
			return status;
		}
		if (cmd instanceof CmdGetAdData) {
		  	AdDataRequest adDataReq= null;
			try {
		  		adDataReq=new AdDataRequest(getSexp(getAdDataExpr));
			}
			catch (Exception ex) {
				((EvalMonitorStatus)status).setFailed(true);
				((EvalMonitorStatus)status).setFailedMessage("Error - "+ ex.getMessage());
				return status;
			}
			Sexp sexp = executeGetAdData(cmd);
			Map<String,Long> productMatchMap=getNumberOfProducts(sexp);
			((EvalMonitorStatus)status).setAdDataRequest(adDataReq);
			((EvalMonitorStatus)status).setTotalProductMatch(totalProductMatch);
			((EvalMonitorStatus)status).setProductMatch(productMatchMap);
			((EvalMonitorStatus)status).setStrategy(this.strategy);
			((EvalMonitorStatus)status).setFailed(false);
		}
		else {
			((EvalMonitorStatus)status).setFailed(true);
			((EvalMonitorStatus)status).setFailedMessage("Error: command is NOT supported.");
		}
	
		return status;
	}

	private Sexp getSexp(String getAdDataExpr) {
		SexpReader sExpReader=new SexpReader(new StringReader(getAdDataExpr));
		Sexp sExp=null;
		try {
		  sExp=sExpReader.read();
	  	}
		catch(Exception ex) {
			; //Dont do anything now. Null returned.
		}
		return sExp;
	}

	private Sexp executeGetAdData(Command cmd) {
		byte[] bytes;
		Sexp productSexp=null;
		try {
			ByteArrayOutputStream out=new ByteArrayOutputStream();
			//CmdGetAdData cmd=new CmdGetAdData(sExp);
			((CmdGetAdData)cmd).process_and_write(out);
			bytes=out.toByteArray();
			ByteArrayInputStream in=new ByteArrayInputStream(bytes);
			SexpIFASLReader ifaslreader=new SexpIFASLReader(in);
			productSexp=ifaslreader.read();
		}
		catch(Exception ex) {
		  ; //Dont do anything now. Null returned.
		}
		return productSexp;
	}

	private Map<String,Long> getNumberOfProducts(Sexp sExp) {
		Integer numProducts=null;
		Map<String,Long> productMatchMap=new HashMap<String,Long>();
		if (sExp.isSexpList()) {
			Sexp productData=((SexpList)sExp).get(1); //get Product Data
			Sexp catDispNames=(((SexpList)((SexpList)sExp).get(4))).get(1);
			Sexp strategy=(((SexpList)((SexpList)sExp).get(6))).get(1);
			if (null!=productData) {
				if (productData.isSexpList()) {
					productData=((SexpList)productData).get(1);
					if (productData.isSexpString()) {
						//strip off the quotes around the entire JSON array string.
						String jsonStr=productData.toString();
						if (jsonStr.startsWith("\""))
							jsonStr=jsonStr.substring(1, jsonStr.length()-1);
						String catDispNameStr=catDispNames.toString();
						if (catDispNameStr.startsWith("\""))
							catDispNameStr=catDispNameStr.substring(1, catDispNameStr.length()-1);

						StringTokenizer st=new StringTokenizer(catDispNameStr,"||");
						while(st.hasMoreTokens())
							productMatchMap.put(st.nextToken(), new Long(0L));

						//Replace "\"" by escaping it. Each character preceded by two(2) forward slashes.
						jsonStr=jsonStr.replaceAll("\\\\\"","\"");
						System.out.println("Product List : "+jsonStr);
						System.out.println("Product Cat Names : "+catDispNames);
						System.out.println("Strategy : "+strategy);
						this.strategy=strategy.toString();
						try {
							JSONArray jsonArray=new JSONArray(jsonStr);
							this.totalProductMatch=new Long(jsonArray.length());
							for (int i=0; i<jsonArray.length(); i++) {
								JSONObject jsonObj=(JSONObject)jsonArray.get(i);
								Iterator it=jsonObj.keys();
								String key=null;
								String value=null;
								while (it.hasNext()) {
									key=(String)it.next();
									value=(String)jsonObj.get(key);
									if (key != null) {
										if ("display_category_name".equals(key)) {
											Long match=(Long)productMatchMap.get(value);
											productMatchMap.put(value,new Long(match.longValue()+1));
										}
									}
								}
							}
						}
						catch(Exception e) {
							; //Dont do anything now. Null returned.
						}
					}
				}
			}
		}
		return productMatchMap;
	}
}
