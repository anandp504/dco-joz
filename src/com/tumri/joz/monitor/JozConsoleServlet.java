package com.tumri.joz.monitor;

import com.tumri.content.ContentProvider;
import com.tumri.content.ContentProviderFactory;
import com.tumri.content.InvalidConfigException;
import com.tumri.content.data.ContentProviderStatus;
import com.tumri.content.impl.file.FileContentProviderImpl;
import com.tumri.joz.campaign.CMAContentProviderStatus;
import com.tumri.joz.campaign.CMAContentRefreshMonitor;
import com.tumri.joz.products.ContentHelper;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.joz.jozMain.ListingProviderFactory;
import com.tumri.joz.jozMain.MerchantDB;
import com.tumri.lls.client.LlsSocketConnectionPool;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

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
        } else {
            //Default send to console
            responseJSP = "/jsp/console.jsp";
        }
        //Forward to JSP page
        getServletConfig().getServletContext().getRequestDispatcher(responseJSP).forward(request, response);
    }
}