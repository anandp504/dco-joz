package com.tumri.joz.products;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

/**
 * User: scbraun
 * Date: 10/2/13
 *
 * very similar to WMContentProviderStatus.  even though initially identical, we want separate classes since
 * they do fundamentally represent different things.
 */
public class ListingOptContentProviderStatus {

	protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-d");
	protected static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

	/**
	 * Last time refresh occurred.
	 */
	public long lastRefreshTime=-1;

	/**
	 * True if last run success and false if not.
	 */
	public boolean lastRunStatus=false;

	/**
	 * Last time successful refresh occurred.
	 */
	public long lastSuccessfulRefreshTime = -1;

	/**
	 * Last error that occurred.
	 */
	public Throwable lastError = null;

	/**
	 * Last error runtime
	 */
	public long lastErrorRunTime = -1;

	/**
	 * How often the refresh occurs.
	 */
	public long refreshInterval=-1;

	public LinkedList<ListingOptContentProviderStatusHistory> runHistory = new LinkedList<ListingOptContentProviderStatusHistory>();

	private static int MAX_HISTORY = 24;

	private static ListingOptContentProviderStatus g_instance = null;

	private ListingOptContentProviderStatus() {
		super();
	}

	/**
	 * Returns an static reference to the WMContentProviderStatus
	 * @return
	 */
	public static ListingOptContentProviderStatus getInstance() {
		if (g_instance == null) {
			g_instance =  new ListingOptContentProviderStatus();
		}
		return g_instance;
	}

	public void addRunHistory(long refreshTime, boolean runStatus, String runDetail) {
		ListingOptContentProviderStatusHistory hist = new ListingOptContentProviderStatusHistory();
		hist.refreshTime = refreshTime;
		hist.runStatus = runStatus;
		hist.runDetailMessage = runDetail;

		if (runHistory.size() == MAX_HISTORY) {
			runHistory.removeFirst();
		}
		runHistory.add(hist);
	}

	@Override
	public String toString() {
		StringWriter sb = new StringWriter();
		sb.append(super.toString());
		sb.append("RefreshInterval(minutes): " + refreshInterval + "\n");
		sb.append("Last Run Succeeded: " + ((lastRunStatus)?"Yes":"No") + "\n");

		if (lastRefreshTime != -1) {
			sb.append("Last Refresh Time:" + TIME_FORMAT.format(lastRefreshTime) + "\n");
		}
		if (lastSuccessfulRefreshTime != -1) {
			sb.append("Last Successful Refresh Time:" + TIME_FORMAT.format(lastSuccessfulRefreshTime) + "\n");
		}
		if (lastErrorRunTime != -1) {
			sb.append("Last Error Time: " + TIME_FORMAT.format(lastErrorRunTime) + "\n");
			if (lastError != null) {
				StringWriter sw = new StringWriter();
				lastError.printStackTrace(new PrintWriter(sw));
				sb.append("Last Error: " + sw.toString() + "\n");
			}
		}
		if (runHistory != null && runHistory.size() > 0) {
			for (ListingOptContentProviderStatusHistory hist : runHistory) {
				sb.append(hist.toString());
			}
		}
		sb.append("\n");
		return sb.toString();
	}

	public class ListingOptContentProviderStatusHistory {
		public long refreshTime=-1;
		public boolean runStatus=false;
		public String runDetailMessage = "";
		public String toString() {
			StringWriter sb = new StringWriter();
			sb.append(super.toString());
			if (refreshTime != -1) {
				sb.append("Refresh Time:" + TIME_FORMAT.format(refreshTime) + "\n");
			}
			sb.append("Run Suceeded: " + ((runStatus)?"Yes":"No") + "\n");
			if (runDetailMessage != null && !"".equals(runDetailMessage)) {
				sb.append("Details:" + runDetailMessage + "\n");
			}
			return sb.toString();
		}
	}

}
