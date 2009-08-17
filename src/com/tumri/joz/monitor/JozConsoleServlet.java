package com.tumri.joz.monitor;

import com.thoughtworks.xstream.XStream;
import com.tumri.cma.domain.AdPod;
import com.tumri.cma.domain.Campaign;
import com.tumri.cma.domain.Recipe;
import com.tumri.cma.domain.TSpec;
import com.tumri.cma.persistence.xml.CampaignXMLDateConverter;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.server.domain.JozQARequest;
import com.tumri.joz.server.domain.JozQAResponse;
import com.tumri.joz.server.domain.JozQAResponseWrapper;
import com.tumri.joz.server.handlers.JozQARequestHandler;
import com.tumri.joz.utils.LogUtils;
import com.tumri.utils.strings.StringTokenizer;
import com.tumri.content.InvalidConfigException;
import com.tumri.content.ContentProviderFactory;
import com.tumri.content.data.ContentProviderStatus;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet class to control the refresh of data
 */
public class JozConsoleServlet extends HttpServlet {
	private static Logger log = Logger.getLogger (JozConsoleServlet.class);

	public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		doService (request, response);
	}

	public void doPost (HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		doService (request, response);
	}

	protected void doService (HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String mode = request.getParameter("mode");
		String option = request.getParameter("option");
		String id = request.getParameter("id");
		String responseJSP = "";
		if ("ad".equalsIgnoreCase(mode)) {
			responseJSP = "/jsp/get-ad-data.jsp";
		} else if ("caa".equalsIgnoreCase(mode)) {
			responseJSP = "/jsp/caa-content-status.jsp";
		} else if ("cma".equalsIgnoreCase(mode)) {
			responseJSP = "/jsp/cma-content-status.jsp";
		} else if ("wmstatus".equalsIgnoreCase(mode)) {
			responseJSP = "/jsp/wm-content-status.jsp";
		} else if ("perf".equalsIgnoreCase(mode)) {
			responseJSP = "/jsp/performance-stat.jsp";
		} else if ("eval".equalsIgnoreCase(mode)) {
			responseJSP = "/jsp/eval.jsp";
		} else if ("sm".equalsIgnoreCase(mode)) {
			responseJSP = "/jsp/serviceMultiplexer.jsp";
		} else if ("wm".equalsIgnoreCase(mode)) {
			responseJSP = "/jsp/wm.jsp";
		} else if ("llc".equalsIgnoreCase(mode)) {
			responseJSP = "/jsp/llc-status.jsp";
		} else if ("indexdebug".equalsIgnoreCase(mode)) {
			responseJSP = "/jsp/indexDebug.jsp?indexDebug=console";
		} else if ("productdb".equalsIgnoreCase(mode)) {
			responseJSP = "/jsp/productDb.jsp";
		} else if ("view".equalsIgnoreCase(mode)) {
			if("latest".equalsIgnoreCase(option)) {
				if(AdRequestMonitor.getInstance().getReqResp()!=null){
					request.setAttribute("adReq", AdRequestMonitor.getInstance().getReqResp().getFirst());
					request.setAttribute("adResp", AdRequestMonitor.getInstance().getReqResp().getSecond());
				}
				responseJSP = "/jsp/adRequest.jsp?mode=console";
			} else if("eval".equalsIgnoreCase(option)){
				EvalMonitor mon = new EvalMonitor();
				JozAdRequest req = mon.makeRequest(request.getParameter("text_eval_expr"));
				request.setAttribute("adReq", req);
				request.setAttribute("adResp", mon.getResponse(req));
				responseJSP = "/jsp/adRequest.jsp?mode=console";
			}
		} else if ("dl".equalsIgnoreCase(mode)) {
			ServletOutputStream output = response.getOutputStream();
			XStream xstream = new XStream();
			xstream.processAnnotations(java.util.List.class);
			xstream.processAnnotations(Campaign.class);
			xstream.registerConverter(new CampaignXMLDateConverter());
			response.setContentType("application/download");
			if("list".equalsIgnoreCase(option)) {
				response.setHeader("Content-Disposition", "attachment; filename=campaigns.xml");
				ArrayList<Campaign> camps = CampaignDB.getInstance().getCampaigns();
				output.write("<list xmlns='http://www.tumri.com/campaign' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.tumri.com/campaign campaign.xsd'>".getBytes("UTF-8"));
				for (int i = 0; i < camps.size(); i++){
					Campaign c = camps.get(i);
					output.write(xstream.toXML(c).toString().getBytes("UTF-8"));
				}
				output.write("</list>".getBytes("UTF-8"));
			} else if("camp".equalsIgnoreCase(option)){
				if(id != null && !"".equals(id.trim())){
					int campId = Integer.parseInt(id);
					Campaign camp = CampaignDB.getInstance().getCampaign(campId);
					response.setHeader("Content-Disposition", "attachment; filename=campaign"+id.trim()+".xml");
					output.write("<list xmlns='http://www.tumri.com/campaign' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.tumri.com/campaign campaign.xsd'>".getBytes("UTF-8"));
					output.write(xstream.toXML(camp).toString().getBytes("UTF-8"));
					output.write("</list>".getBytes("UTF-8"));
				}
			} else if("adpod".equalsIgnoreCase(option)){
				if(id != null && !"".equals(id.trim())){
					int adPodId = Integer.parseInt(id);
					AdPod adPod = CampaignDB.getInstance().getAdPod(adPodId);
					response.setHeader("Content-Disposition", "attachment; filename=adpod"+id.trim()+".xml");
					output.write(xstream.toXML(adPod).toString().getBytes("UTF-8"));
				}
			} else if("recipe".equalsIgnoreCase(option)){
				if(id != null && !"".equals(id.trim())){
					int recipeId = Integer.parseInt(id);
					Recipe recipe = CampaignDB.getInstance().getRecipe(recipeId);
					response.setHeader("Content-Disposition", "attachment; filename=recipe"+id.trim()+".xml");
					output.write(xstream.toXML(recipe).toString().getBytes("UTF-8"));
				}
			} else if("tspec".equalsIgnoreCase(option)){
				if(id != null && !"".equals(id.trim())){
					int tSpecId = Integer.parseInt(id);
					TSpec tSpec = CampaignDB.getInstance().getTspec(tSpecId);
					response.setHeader("Content-Disposition", "attachment; filename=tspec"+id.trim()+".xml");
					output.write(xstream.toXML(tSpec).toString().getBytes("UTF-8"));
				}
			} else {
				response.setHeader("Content-Disposition", "attachment; filename=campaigns.xml");
				ArrayList<Campaign> camps = CampaignDB.getInstance().getCampaigns();
				output.write("<list xmlns='http://www.tumri.com/campaign' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.tumri.com/campaign campaign.xsd'>".getBytes("UTF-8"));
				for (int i = 0; i < camps.size(); i++){
					Campaign c = camps.get(i);
					output.write(xstream.toXML(c).toString().getBytes("UTF-8"));
				}
				output.write("</list>".getBytes("UTF-8"));
			}
			output.flush();
			output.close();
			responseJSP = "/console";
		} else if("log".equalsIgnoreCase(mode)){
			if(id != null && option != null && !"".equals(id) && !"".equals(option)){
				LogUtils.setLogLevel(id, option);
			}
			responseJSP = "/jsp/log.jsp";
		} else if("qareport".equalsIgnoreCase(mode)){
			JozQARequestHandler qaHandler = new JozQARequestHandler();
			JozQARequest req = new JozQARequest();
			JozQAResponseWrapper wrapper = new JozQAResponseWrapper();
			JozQAResponse qaResponse = new JozQAResponse();
			ArrayList<String> advertiserNames = new ArrayList<String>();

			String advertisersString = request.getParameter("advertisers");

			if(advertisersString != null){
				StringTokenizer tokenizer = new StringTokenizer(advertisersString, ',');
				advertiserNames = tokenizer.getTokens();
			}
			req.setAdvertisers(advertiserNames);

			qaHandler.doQuery(req, wrapper);
			String xml = wrapper.getResultMap().get(JozQAResponseWrapper.KEY_QAREPORTDETAIL);
			XStream xstream = new XStream();
			if(xml != null){
				qaResponse = (JozQAResponse)xstream.fromXML(xml);
			}
			request.setAttribute("jozQAResp", qaResponse);
			request.setAttribute("jozQAReq", req);
			responseJSP = "/jsp/jozQAReport.jsp?mode=console";

		} else if ("execute".equalsIgnoreCase(mode)) {
			if("tspec".equalsIgnoreCase(option)){

				String reqPSR = request.getParameter("text_eval_expr");
				String reqTSpec = request.getParameter("text_eval_expr2");
				ProductQueryMonitor pqm=new ProductQueryMonitor();
				ProductQueryMonitorStatus pqmstat=(ProductQueryMonitorStatus)pqm.getStatus(reqPSR, reqTSpec);
				String rawData = null;
				if(pqmstat != null){
					rawData = pqmstat.getProductRawData();
				}
				request.setAttribute("reqPSR", reqPSR);
				request.setAttribute("reqTSpec", reqTSpec);
				request.setAttribute("resp", rawData);
			}
			responseJSP = "/jsp/evalTSpec.jsp?mode=console";
		} else {
			//Default send to console
			responseJSP = "/jsp/console.jsp";
		}
		//Forward to JSP page
		getServletConfig().getServletContext().getRequestDispatcher(responseJSP).forward(request, response);
	}


}
