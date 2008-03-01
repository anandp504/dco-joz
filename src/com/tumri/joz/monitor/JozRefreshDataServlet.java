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
public class JozRefreshDataServlet extends HttpServlet {
    private static Logger log = Logger.getLogger (JozRefreshDataServlet.class);

    public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doService (request, response);
    }

    public void doPost (HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        doService (request, response);
    }

    protected void doService (HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String result = "";
        String textMode = request.getParameter("text");
        String responseJSP = "";
        try {
            String dataType = request.getParameter("type");
            if ("listing".equalsIgnoreCase(dataType)) {
                String revertMode = request.getParameter("full-load");
                result = doRefreshListingData(revertMode);
                responseJSP = "/jsp/content-status.jsp";
            } else if ("campaign".equalsIgnoreCase(dataType)) {
                result = doRefreshCampaignData();
                responseJSP = "/jsp/cma-content-status.jsp";
            }
        }
        catch (Exception e) {
            result = "failed";
        }
        if (textMode!=null) {
            response.setContentType ("text/plain");
            PrintWriter out = response.getWriter();
            out.print(result);
        } else {
            //Forward to JSP page
            getServletConfig().getServletContext().getRequestDispatcher(responseJSP).forward(request, response);
        }
    }

    /**
     * Helper method to refresh listing data
     */
    private String doRefreshListingData(String revertMode) throws InvalidConfigException  {
        if (revertMode != null) {
            ProductDB.getInstance().clearProductDB();
        }
        ContentProviderFactory f = ContentProviderFactory.getDefaultInitializedInstance();
        ContentProvider cp = f.getContentProvider();
        if (((FileContentProviderImpl) cp).lst.isEmpty()) {
            ContentHelper h = new ContentHelper(cp);
            cp.addContentListener(h);
        }
        cp.refresh();
        ContentProviderStatus status = cp.getStatus();
        String success = (status.lastRunStatus == true ? "success" : "failed");
        return success;
    }

    /**
     * Helper method to refresh campaign data
     */
    private String doRefreshCampaignData() {
        CMAContentRefreshMonitor.getInstance().loadCampaignData();
        CMAContentProviderStatus status = CMAContentProviderStatus.getInstance();
        String success = (status.lastRunStatus == true? "success" : "failed");
        return success;      
    }

}
