package com.tumri.joz.monitor;

import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpIFASLReader;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.joz.jozMain.CmdGetAdData;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.jozMain.AdDataRequest;

import org.apache.log4j.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

/**
  * JoZ Eval Monitor.
  * @author Ramki
  */
public class EvalMonitor extends ComponentMonitor
{
	private static Logger log = Logger.getLogger(EvalMonitor.class);
	private Long totalProductMatch=null;
	private String strategy=null;

	public EvalMonitor() {
	   super("eval", new EvalMonitorStatus("eval"));
	}

	public MonitorStatus getStatus(String getAdDataExpr) {
		AdDataRequest adDataReq=this.getAdDataRequest(getAdDataExpr);
		Map<String,Long> productMatchMap=this.getNumberOfProducts(getAdDataExpr);
		((EvalMonitorStatus)status).setAdDataRequest(adDataReq);
		((EvalMonitorStatus)status).setTotalProductMatch(totalProductMatch);
		((EvalMonitorStatus)status).setProductMatch(productMatchMap);
		((EvalMonitorStatus)status).setStrategy(this.strategy);
		return status;
	}

	private AdDataRequest getAdDataRequest(String getAdDataExpr) {
		Sexp sExp=this.getSexp(getAdDataExpr);
		AdDataRequest adDataReq=null;
		try {
		  adDataReq=new AdDataRequest(sExp);
		}
		catch(Exception ex) {
			; //Dont do anything now. Null returned.
		}
		return adDataReq;
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

	private Sexp executeGetAdData(Sexp sExp) {
		byte[] bytes;
		Sexp productSexp=null;
		try {
			ByteArrayOutputStream out=new ByteArrayOutputStream();
			CmdGetAdData cmd=new CmdGetAdData(sExp);
			cmd.process_and_write(out);
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

	private Map<String,Long> getNumberOfProducts(String getAdDataExpr) {
		Integer numProducts=null;
		Map<String,Long> productMatchMap=new HashMap<String,Long>();
		Sexp sExp=this.getSexp(getAdDataExpr);
		sExp=this.executeGetAdData(sExp);
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
