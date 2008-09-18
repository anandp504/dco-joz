package com.tumri.joz.monitor;

import com.tumri.joz.server.domain.JozAdRequest;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        } else {
            //Default send to console
            responseJSP = "/jsp/console.jsp";
        }
        //Forward to JSP page
        getServletConfig().getServletContext().getRequestDispatcher(responseJSP).forward(request, response);
    }
}