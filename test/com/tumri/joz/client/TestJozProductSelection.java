package com.tumri.joz.client;


import com.tumri.joz.campaign.CampaignDBDataLoader;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.products.ContentHelper;
import com.tumri.joz.productselection.ProductSelectionProcessor;
import com.tumri.joz.productselection.ProductSelectionResults;
import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.utils.AppProperties;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: scbraun
 * Date: Aug 21, 2008
 * Time: 11:43:10 AM
 * Loads a txt file of the format:
 *  theme theme1
 *  theme theme2 recipeid recipeid1
 *  theme theme2
 *  locationname name1
 *  locationname name2  recipeid recipeid2
 *
 *  and sends multiple requests to joz built off of this txt file: each line representing a request
 */
public class TestJozProductSelection {

	//protected static String host = "localhost";
	//protected static int port = 2544;
	protected String file = "/tmp/requests.txt";
	//protected String file = "/opt/joz/data/caa/pids.txt";
	private static Logger log = Logger.getLogger(TestJozProductSelection.class);
	//private int socketPoolSize = 100;
	private AtomicInteger numFailedRequests = new AtomicInteger();

	protected List<String> requests = null;

	protected ArrayList<Thread> threads = new ArrayList<Thread>();
	protected long[][] timings = null;
	private static ProductSelectionProcessor _impl = null;

	/**
	 * @param numThreads: the number of threads that should be run
	 * @param runs: the number of requests each thread should perform
	 * @param numProducts the number of products each request should ask for
	 * The main purpose of this method is to:
	 *  1) create new Threads that will independently make requests
	 *  2) collect and analyze timing information returned from each thread
	 */
	public void runTest(final int numThreads, final int runs, final int numProducts) {
		_impl = new ProductSelectionProcessor();

		readFile();
		long startTime = System.nanoTime();
		timings = new long[numThreads][];
		for (int k = 0; k < numThreads; k++) {
			timings[k] = new long[runs];
			final int index = k;

			Thread t = new Thread("Test " + k) {
				public void run() {
					timings[index] = runTest(runs, numProducts);
				}
			};

			threads.add(t);
			t.start();
		}

		for (int k = 0; k < numThreads; k++) {
			try {
				threads.get(k).join();
			} catch (InterruptedException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}

		long stopTime = System.nanoTime();

		long minTime = Long.MAX_VALUE;
		long maxTime = -1;
		long totalTime = 0;

		/*
		for (int i = 0; i < numThreads; i++) {
			for (int j = 0; j < runs; j++) {
				if (minTime > timings[i][j] && timings[i][j] > 0) {
					minTime = timings[i][j];
				}
				if (maxTime < timings[i][j] && timings[i][j] > 0) {
					maxTime = timings[i][j];
				}
				totalTime += timings[i][j];
			}
		}*/
		for(int i = 0; i < numThreads; i++){
			totalTime += timings[i][0];
		}

		//System.out.println("Minimum Time for a request: " + minTime);
		//System.out.println("Maximum Time for a request: " + maxTime);
		System.out.println("Average Time for a request: " + (totalTime/(numThreads * runs)));
		System.out.println("Total time: " + (stopTime - startTime));
		System.out.println("Total Requests " + numThreads * runs);
		System.out.println("Total FAILED Requests " + numFailedRequests.get());
	}

	/**
	 * @param runs: the number of runs to be performed by each thread
	 * @param numProducts: the number of products each request should request
	 * @return an array of run times
	 * The main purpose of this method is to run each thread so many times and collect timing information
	 */
	public long[] runTest(int runs, int numProducts) {
		long[] retVal = new long[runs];
		r = new Random(Calendar.getInstance().getTimeInMillis());

		long startTime = -1;
		long endTime = -1;

		//First open the outputstream and flush out a request
		//JozAdResponse res= sendRequest(numProducts);
		ProductSelectionResults res = null;
		//Now we run multiple requests in the same connection
		startTime = System.nanoTime();
		for (int i = 0; i < runs; i++) {
			AdDataRequest req = makeRequest(numProducts);
			//startTime = System.currentTimeMillis();
			res = sendRequest(req);
			//endTime = System.currentTimeMillis();
			//retVal[i] = endTime - startTime;
		}
		endTime = System.nanoTime();
		retVal[0] = endTime - startTime;
		return retVal;
	}

	Random r = null;
	int totalAvailableRequests = 0;

	protected ProductSelectionResults sendRequest(AdDataRequest req){
		Features features = new Features();
		ProductSelectionResults res = _impl.processRequest(req, features);
		inspectResponseData(res);
		return res;
	}

	protected AdDataRequest makeRequest(int numProducts){
		String requestString = null;
		int index = Math.abs(r.nextInt()%totalAvailableRequests);
		requestString = requests.get(index);
		//parse input String request into a JozAdRequest
		StringTokenizer reqTokenizer = new StringTokenizer(requestString);
		String key = "";
		String value = "";
		JozAdRequest jozAdRequest = new JozAdRequest();
		jozAdRequest.setValue(JozAdRequest.KEY_NUM_PRODUCTS, String.valueOf(numProducts));
		while(reqTokenizer.hasMoreTokens()){
			key = reqTokenizer.nextToken();
			value = reqTokenizer.nextToken();

			if("theme".equalsIgnoreCase(key) || "locationname".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_THEME, value);
			} else if("adheight".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_AD_HEIGHT, value);
			} else if("adtype".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_AD_TYPE, value);
			} else if("adwidth".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_AD_WIDTH, value);
			} else if("allowtoofewproducts".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_ALLOW_TOO_FEW_PRODUCTS, value);
			} else if("areacode".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_AREACODE, value);
			} else if("category".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_CATEGORY, value);
			} else if("city".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_CITY, value);
			} else if("country".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_COUNTRY, value);
			} else if("dmacode".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_DMACODE, value);
			} else if("keywords".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_KEYWORDS, value);
			} else if("lattitude".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_LATITUDE, value);
			} else if("longitude".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_LONGITUDE, value);
			} else if("maxproddesclen".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_MAX_PROD_DESC_LEN, value);
			} else if("minnumleadgens".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_MIN_NUM_LEADGENS, value);
			} else if("multivaluefield1".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_MULTI_VALUE_FIELD1, value);
			} else if("multivaluefield2".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_MULTI_VALUE_FIELD2, value);
			} else if("multivaluefield3".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_MULTI_VALUE_FIELD3, value);
			} else if("multivaluefield4".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_MULTI_VALUE_FIELD4, value);
			} else if("multivaluefield5".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_MULTI_VALUE_FIELD5, value);
			} else if("numproducts".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_NUM_PRODUCTS, value);
			} else if("recipeid".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_RECIPE_ID, value);
			} else if("region".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_REGION, value);
			} else if("reverttodefaultrealm".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_REVERT_TO_DEFAULT_REALM, value);
			} else if("rowsize".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_ROW_SIZE, value);
			} else if("scriptkeywords".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_SCRIPT_KEYWORDS, value);
			} else if("storeid".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_STORE_ID, value);
			} else if("tspec".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_T_SPEC, value);
			} else if("url".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_URL, value);
			} else if("whichrow".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_WHICH_ROW, value);
			} else if("zipcode".equalsIgnoreCase(key)){
				jozAdRequest.setValue(JozAdRequest.KEY_ZIP_CODE, value);
			}
		}
		AdDataRequest adDataRequest = new AdDataRequest(jozAdRequest);
		return adDataRequest;
	}


	/**
	 * @param selectionResults
	 * this method inspects the response to see if it is first null and then if it has an ERROR
	 */
	private void inspectResponseData(ProductSelectionResults selectionResults) {
		//if (log.isInfoEnabled()) {
			String resultValue;
			if(selectionResults != null){
				resultValue = selectionResults.getTargetedTSpecName();
				if(resultValue == null){
					log.info("Lookup failed. ResultMap has Error: " + resultValue);
					numFailedRequests.incrementAndGet();
				}
			} else {
				numFailedRequests.incrementAndGet();
				log.info("Lookup faild. No result");
			}
		//}
	}

	protected void readFile() {
		try {
			requests = new ArrayList<String>();
			FileInputStream inFile = new FileInputStream(file);
			InputStreamReader ir = new InputStreamReader(inFile,"UTF-8");
			BufferedReader br = new BufferedReader(ir);

			String line = null;
			boolean eof = false;
			while (!eof) {
				line = br.readLine();
				if (line == null) {
					eof = true;
					continue;
				}
				requests.add(line);
				totalAvailableRequests++;
			}
			br.close();
			ir.close();
			inFile.close();

		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			System.exit(1);
		}
	}

	public static void main(String[] args) {

		int numThreads = 1;
		int numRuns = 10;
		int numProducts = 10;

		switch(args.length) {
			case 3: {
				numProducts = Integer.parseInt(args[2]);
			}
			case 2: {
				numRuns = Integer.parseInt(args[1]);
			}
			case 1: {
				numThreads = Integer.parseInt(args[0]);
			}
			default: {

			}
		}

       Properties props = AppProperties.getInstance().getProperties();
		ContentHelper.init(props);
		try {
			CampaignDBDataLoader.getInstance().loadData();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Test: " + numThreads + " threads, " + numRuns + " runs/thread, " + numProducts
				+ " products/request");
		(new TestJozProductSelection()).runTest(numThreads, numRuns, numProducts);

	}

}