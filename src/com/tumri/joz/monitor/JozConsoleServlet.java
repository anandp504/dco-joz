package com.tumri.joz.monitor;

import com.thoughtworks.xstream.XStream;
import com.tumri.cma.domain.AdPod;
import com.tumri.cma.domain.Campaign;
import com.tumri.cma.domain.Recipe;
import com.tumri.cma.domain.TSpec;
import com.tumri.cma.persistence.xml.CampaignXMLDateConverter;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.client.JoZClientException;
import com.tumri.joz.client.impl.JozDataProviderImpl;
import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.server.domain.JozQARequest;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.LogUtils;
import com.tumri.utils.strings.StringTokenizer;
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
            responseJSP = "/jsp/content-status.jsp";
        } else if ("cma".equalsIgnoreCase(mode)) {
            responseJSP = "/jsp/cma-content-status.jsp";
        } else if ("perf".equalsIgnoreCase(mode)) {
            responseJSP = "/jsp/performance-stat.jsp";
        } else if ("eval".equalsIgnoreCase(mode)) {
            responseJSP = "/jsp/eval.jsp";
        } else if ("sm".equalsIgnoreCase(mode)) {
            responseJSP = "/jsp/service-multiplexer.jsp";
        } else if ("llc".equalsIgnoreCase(mode)) {
            responseJSP = "/jsp/llc-status.jsp";
        } else if ("indexdebug".equalsIgnoreCase(mode)) {
            responseJSP = "/jsp/indexDebug.jsp?mode=console";
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
	        int poolSize = Integer.parseInt(AppProperties.getInstance().getProperty("tcpServer.poolSize"));
			int port = Integer.parseInt(AppProperties.getInstance().getProperty("tcpServer.port"));

	        JozDataProviderImpl impl = new JozDataProviderImpl("localhost", port,poolSize,3);
			JozQARequest req = new JozQARequest();
	        String advertisersString = request.getParameter("advertisers");

		    ArrayList<String> advertiserNames = new ArrayList<String>();

	        if(advertisersString != null){
				StringTokenizer tokenizer = new StringTokenizer(advertisersString, ',');
		        advertiserNames = tokenizer.getTokens();
			}
			req.setAdvertisers(advertiserNames);
	        
	        try {
		        request.setAttribute("jozQAResp", impl.getQAReport(req));
		        request.setAttribute("jozQAReq", req);
	        } catch (JoZClientException e) {
		        log.error("Error getting JozQAResponse: ",e);
	        }
	        responseJSP = "/jsp/jozQAReport.jsp?mode=console";
        } else {
            //Default send to console
            responseJSP = "/jsp/console.jsp";
        }
        //Forward to JSP page
        getServletConfig().getServletContext().getRequestDispatcher(responseJSP).forward(request, response);
    }
}