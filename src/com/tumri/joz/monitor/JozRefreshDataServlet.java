package com.tumri.joz.monitor;

import com.tumri.content.ContentProvider;
import com.tumri.content.ContentProviderFactory;
import com.tumri.content.InvalidConfigException;
import com.tumri.content.data.ContentProviderStatus;
import com.tumri.content.data.impl.AdvertiserMerchantDataMapperImpl;
import com.tumri.content.data.impl.AdvertiserTaxonomyMapperImpl;
import com.tumri.joz.campaign.CMAContentProviderStatus;
import com.tumri.joz.campaign.CMAContentRefreshMonitor;
import com.tumri.joz.campaign.wm.loader.WMContentProviderStatus;
import com.tumri.joz.campaign.wm.loader.WMDBLoader;
import com.tumri.joz.jozMain.ListingProviderFactory;
import com.tumri.joz.products.JozIndexHelper;
import com.tumri.joz.products.ListingOptContentPoller;
import com.tumri.joz.products.ListingOptContentProviderStatus;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.IndexLoadingComparator;
import com.tumri.utils.nio.NioSocketChannelPool;
import com.tumri.utils.tcp.client.TcpSocketConnectionPool;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Servlet class to control the refresh of data
 */
public class JozRefreshDataServlet extends HttpServlet {
	private static Logger log = Logger.getLogger (JozRefreshDataServlet.class);
	private static ConcurrentHashMap<String, Semaphore> advSemaphores = new ConcurrentHashMap<String, Semaphore>(1000);
	private static final int MAX_SIMULTANEOUS_ADVERTISERS;
	private static Semaphore allAdvSemaphore = null;

	static {
		int maxConcurrentContentLoadingFromProperties = AppProperties.getInstance().getMaxConcurrentContentLoading();
		if(maxConcurrentContentLoadingFromProperties > 0){
			MAX_SIMULTANEOUS_ADVERTISERS = maxConcurrentContentLoadingFromProperties;
		} else {
			MAX_SIMULTANEOUS_ADVERTISERS = 100;
		}
		allAdvSemaphore = new Semaphore(MAX_SIMULTANEOUS_ADVERTISERS, true);
	}

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
		boolean isContentRefresh = false;
		String adv = null;
		if ("listing".equalsIgnoreCase(dataType)) {
			isContentRefresh = true;
			String clearListing = request.getParameter("clear-cache");
			String advertiser = request.getParameter("adv");
			adv = advertiser;
			if (clearListing!=null) {
				result = doClearListingCache();
				jspMode = null;
			} else {
				String revertMode = request.getParameter("full-load");
				try {
					result = doRefreshListingData(advertiser, revertMode);
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
		}  else if ("validateIndex".equalsIgnoreCase(dataType)) {
			responseJSP = "/jsp/MupIndexComparison.jsp";
			try {
				IndexLoadingComparator validator = new IndexLoadingComparator();
				List<String> infos = validator.validate(null);
				if (jspMode!=null) {
					request.setAttribute("infos", infos);
				} else {
					StringBuilder sb = new StringBuilder();
					if (infos!=null) {
						for (String s:infos) {
							sb.append(s);
							sb.append("\n");
						}
					}
					result = sb.toString();
				}
			} catch (Exception e) {
				result = "failed";
			}
		} else if("listingOpt".equalsIgnoreCase(dataType)){
            try {
                result = doRefreshListingOptData();
            } catch (Exception e) {
                result = "failed";
            }
            responseJSP = "/jsp/opt-content-status.jsp";

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
			out.flush();
			out.close();
		}
		if(isContentRefresh){
			log.warn("Finished all of content load for " + adv);
		}
	}

	/**
	 * Helper method to refresh listing data
	 */
	private String doRefreshListingData(String advertiser, String revertMode) throws InvalidConfigException  {
		String revertModeS = revertMode != null ? "enabled" : "disabled";
		log.warn("Attempting refresh for advertiser: " + advertiser + " with revertMode " + revertModeS);
		String advStr = null;
		Semaphore sAdv = null;
		String success = "failed";

		try {
			if(advertiser == null){
				allAdvSemaphore.acquire(MAX_SIMULTANEOUS_ADVERTISERS);
				advStr = "all advertisers";
			} else {
				allAdvSemaphore.acquire(1);
				advSemaphores.putIfAbsent(advertiser, new Semaphore(1, true));
				sAdv = advSemaphores.get(advertiser);
				sAdv.acquire();
				advStr = advertiser;
			}
			log.warn("Beginning refresh for advertiser: " + advertiser + " with revertMode " + revertModeS);
			if (revertMode != null) {
				log.warn("Content full load starting for " + advStr);
				if (advertiser!=null) {
					JozIndexHelper.getInstance().deleteJozIndex(advertiser);
				} else {
					ProductDB.getInstance().clearProductDB();
				}
			}  else {
				log.warn("Content load starting for " + advStr);
			}
			ContentProviderFactory f = ContentProviderFactory.getDefaultInitializedInstance();
			ContentProvider cp = f.getContentProvider();
			cp.refresh(advertiser);
			//Invoke the content refresh on Listings Data client
			ListingProviderFactory.refreshData(AdvertiserTaxonomyMapperImpl.getInstance(),
					AdvertiserMerchantDataMapperImpl.getInstance());
			ContentProviderStatus status = cp.getStatus();
			success = (status.lastRunStatus == true ? "success" : "failed");
			if (revertMode!=null) {
				log.warn("Content full load finished for " + advStr + ". Status = " + success);
			} else {
				log.warn("Content load finished for " + advStr + ". Status = " + success);
			}

		} catch (InterruptedException e) {
			success = "failed";
			if (revertMode!=null) {
				log.warn("Content full load finished for " + advStr + ". Status = " + success);
			} else {
				log.warn("Content load finished for " + advStr + ". Status = " + success);
			}
			log.error(e);
		} finally {
			//todo: possibly improve handling of error when 'releasing'
			int numToRelease = 1;
			if(advertiser==null){
				numToRelease = MAX_SIMULTANEOUS_ADVERTISERS;
			}
			allAdvSemaphore.release(numToRelease);
			if(sAdv!=null && advStr!=null){
				sAdv.release();
			}
		}
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
     * Helper method to refresh Listing Optimization data
     */
    private synchronized String doRefreshListingOptData(){
        ListingOptContentPoller.getInstance().performTask();
        ListingOptContentProviderStatus status = ListingOptContentProviderStatus.getInstance();
        String result = (status.lastRunStatus == true? "success" : "failed");
        return result;
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
