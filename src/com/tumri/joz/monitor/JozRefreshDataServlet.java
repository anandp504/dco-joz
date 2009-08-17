package com.tumri.joz.monitor;

import com.tumri.content.ContentProvider;
import com.tumri.content.ContentProviderFactory;
import com.tumri.content.InvalidConfigException;
import com.tumri.content.data.ContentProviderStatus;
import com.tumri.joz.campaign.CMAContentProviderStatus;
import com.tumri.joz.campaign.CMAContentRefreshMonitor;
import com.tumri.joz.campaign.CampaignDBDataLoader;
import com.tumri.joz.campaign.CampaignDataLoadingException;
import com.tumri.joz.campaign.wm.loader.WMDBLoader;
import com.tumri.joz.campaign.wm.loader.WMLoaderException;
import com.tumri.joz.campaign.wm.loader.WMContentProviderStatus;
import com.tumri.joz.jozMain.ListingProviderFactory;
import com.tumri.joz.jozMain.MerchantDB;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.LogUtils;
import com.tumri.utils.nio.NioSocketChannelPool;
import com.tumri.utils.tcp.client.TcpSocketConnectionPool;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.StringWriter;

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
        String jspMode = request.getParameter("jspMode");
        String responseJSP = "";
        String dataType = request.getParameter("type");
        if ("listing".equalsIgnoreCase(dataType)) {
            String clearListing = request.getParameter("clear-cache");
            if (clearListing!=null) {
                result = doClearListingCache();
                jspMode = null;
            } else {
                String revertMode = request.getParameter("full-load");
                try {
                    result = doRefreshListingData(revertMode);
                } catch (Exception e) {
                    result = "failed";
                }
                responseJSP = "/jsp/caa-content-status.jsp";
            }
        } else if ("campaign".equalsIgnoreCase(dataType)) {
            try {
                result = doRefreshCampaignData();
            } catch (Exception e) {
                result = "failed";
            }
            responseJSP = "/jsp/cma-content-status.jsp";
        } else if ("wm".equalsIgnoreCase(dataType)) {
            try {
                result = doRefreshWMData();
            } catch (Exception e) {
                result = "failed";
            }
            responseJSP = "/jsp/wm-content-status.jsp";
        } else if ("socket".equalsIgnoreCase(dataType)) {
            try {
                result = doResetSocketPool();
            } catch (Exception e) {
                result = "failed";
            }
            responseJSP = "/jsp/llc-status.jsp";
        } else if ("caadetails".equalsIgnoreCase(dataType)) {
            try {
                result = getManifestInfo();
                jspMode = null;
            } catch (Exception e) {
                result = "failed";
            }
        } else {
            //Default send to console
            jspMode = "true";
            responseJSP = "/jsp/console.jsp";
        }
        //By default send non verbose output
        if (jspMode!=null) {
            //Forward to JSP page
            getServletConfig().getServletContext().getRequestDispatcher(responseJSP).forward(request, response);
        } else {
            response.setContentType ("text/plain");
            PrintWriter out = response.getWriter();
            out.print(result);
        }
    }

    /**
     * Helper method to refresh listing data
     */
    private synchronized String doRefreshListingData(String revertMode) throws InvalidConfigException  {
        if (revertMode != null) {
            ProductDB.getInstance().clearProductDB();
        }
        ContentProviderFactory f = ContentProviderFactory.getDefaultInitializedInstance();
        ContentProvider cp = f.getContentProvider();
        cp.refresh();
        //Invoke the content refresh on Listings Data client
        ListingProviderFactory.refreshData(JOZTaxonomy.getInstance().getTaxonomy(),
                MerchantDB.getInstance().getMerchantData());
        ContentProviderStatus status = cp.getStatus();
        String success = (status.lastRunStatus == true ? "success" : "failed");
        return success;
    }

    /**
     * Clear the listing cache
     */
    private String doClearListingCache() {
        try {
            ListingProviderFactory.clearListingCache();
            return "success";
        } catch(Throwable t) {
            return "failed";
        }
    }

    /**
     * Helper method to refresh campaign data
     */
    private synchronized String doRefreshCampaignData() {
        CMAContentRefreshMonitor.getInstance().loadCampaignData();
        CMAContentProviderStatus status = CMAContentProviderStatus.getInstance();
        String success = (status.lastRunStatus == true? "success" : "failed");
        return success;
    }

    /**
     * Helper method to refresh weight matrix data
     */
    private synchronized String doRefreshWMData() {
        WMDBLoader.forceLoadData();
        WMContentProviderStatus status = WMContentProviderStatus.getInstance();
        String success = (status.lastRunStatus == true? "success" : "failed");
        return success;
    }

    /**
     * Helper method to refresh socket data
     */
    private String doResetSocketPool() {
        if (AppProperties.getInstance().isNioEnabled()) {
            NioSocketChannelPool.getInstance().initConnections(true);
        } else {
            TcpSocketConnectionPool.getInstance().reset();
        }
        //Wait for 5 secs for the connections to be restored
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            //
        }
        return "success";
    }

    /**
     * Gets the current set of content details loaded into Joz
     * @return
     */
    private String getManifestInfo() throws InvalidConfigException {
        ContentProviderStatus status = ContentProviderFactory.getInstance().getContentProvider().getStatus();
        return status.getManifestInfo();
    }

}
