package com.tumri.joz.client;

import com.tumri.joz.client.helper.JozAdDataProvider;
import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.server.domain.JozAdResponse;
import com.tumri.utils.tcp.client.TcpSocketConnectionPool;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
public class TestJozClient {

	protected static String host = "localhost";
	protected static int port = 2544;
	protected String file = "/tmp/requests.txt";
	//protected String file = "/opt/joz/data/caa/pids.txt";
	private static Logger log = Logger.getLogger(TestJozClient.class);
	private int socketPoolSize = 100;
	private AtomicInteger numFailedRequests = new AtomicInteger();
    private AtomicLong totalSize = new AtomicLong();

    protected List<String> requests = null;

	protected ArrayList<Thread> threads = new ArrayList<Thread>();
	protected long[][] timings = null;
	private static JozAdDataProvider _impl = null;
	private static JozAdRequest singleReq;
	int totalAvailableRequests = 0;
	Random r = null;

	public void runTest(String iserver, int iport, int isocketPoolSize,
	                    final int numThreads, final int runs, final int numProducts) {
		port = iport;
		host = iserver;
		socketPoolSize = isocketPoolSize;
		runTest(numThreads,  runs, numProducts);
	}

	/**
	 * @param numThreads: the number of threads that should be run
	 * @param runs: the number of requests each thread should perform
	 * @param numProducts the number of products each request should ask for
	 * The main purpose of this method is to:
	 *  1) create new Threads that will independently make requests
	 *  2) collect and analyze timing information returned from each thread
	 */
	public void runTest(final int numThreads, final int runs, final int numProducts) {
		TcpSocketConnectionPool.getInstance().init(host, port, socketPoolSize, 3);
		_impl = new JozAdDataProvider();
		r = new Random(Calendar.getInstance().getTimeInMillis());
		readFile();
		singleReq = makeRequest(numProducts);
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
//
//		long minTime = Long.MAX_VALUE;
//		long maxTime = -1;
		long totalTime = stopTime - startTime;


//		for (int i = 0; i < numThreads; i++) {
//			for (int j = 0; j < runs; j++) {
//				if (minTime > timings[i][j] && timings[i][j] > 0) {
//					minTime = timings[i][j];
//				}
//				if (maxTime < timings[i][j] && timings[i][j] > 0) {
//					maxTime = timings[i][j];
//				}
//				totalTime += timings[i][j];
//			}
//		}

		long combinedTotalTime = 0;
		//long averages = 0;
		for(int i = 0; i < numThreads; i++){
			combinedTotalTime += timings[i][0];
			//averages += (timings[i][0] / runs);
		}
		//averages = averages/numThreads;

		//System.out.println("Minimum Time for a request: " + minTime);
		//System.out.println("Maximum Time for a request: " + maxTime);

		long averageTime = (combinedTotalTime/(numThreads * runs));
		double totalTimeSec = (double)totalTime / 1000000000;
		double rps =  ((double)(numThreads * runs) / totalTimeSec);
		System.out.println("Average Time for a request: " + averageTime + "ns (" + (double)averageTime/1000000 +"ms)");
		System.out.println("Combined Total Time: " + combinedTotalTime + "ns");
		System.out.println("Runs per second: " + rps);
		System.out.println("Total time: " + totalTime + "ns (" + totalTimeSec + "sec)");
		System.out.println("Total Requests " + numThreads * runs);
		System.out.println("Total FAILED Requests " + numFailedRequests.get());
		System.out.println("Total size transmitted " + totalSize.get());
		System.out.println("Avg size per response " + totalSize.get()/(numThreads * runs));
		TcpSocketConnectionPool.getInstance().tearDown();
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
		//Now we run multiple requests in the same connection
		startTime = System.nanoTime();
		for (int i = 0; i < runs; i++) {
			//JozAdRequest req = makeRequest(numProducts);
			//startTime = System.currentTimeMillis();
			//res = sendRequest(numProducts);
			sendRequest(singleReq);
			//endTime = System.currentTimeMillis();
			//retVal[i] = endTime - startTime;
		}
		endTime = System.nanoTime();
		retVal[0] = endTime - startTime;
		return retVal;
	}



	protected JozAdResponse sendRequest(JozAdRequest req){
		JozAdResponse res = _impl.processRequest(req);
		inspectResponseData(res);
		return res;
	}
	
	protected JozAdRequest makeRequest(int numProducts){
		String requestString = null;
		int index = Math.abs(r.nextInt()%totalAvailableRequests);
		requestString = requests.get(index);
		//parse input String request into a JozAdRequest
		StringTokenizer reqTokenizer = new StringTokenizer(requestString);
		String key = "";
		String value = "";
		JozAdRequest req = new JozAdRequest();
		req.setValue(JozAdRequest.KEY_NUM_PRODUCTS, String.valueOf(numProducts));
		while(reqTokenizer.hasMoreTokens()){
			key = reqTokenizer.nextToken();
			value = reqTokenizer.nextToken();

			if("theme".equalsIgnoreCase(key) || "locationname".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_THEME, value);
			} else if("adheight".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_AD_HEIGHT, value);
			} else if("adtype".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_AD_TYPE, value);
			} else if("adwidth".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_AD_WIDTH, value);
			} else if("allowtoofewproducts".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_ALLOW_TOO_FEW_PRODUCTS, value);
			} else if("areacode".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_AREACODE, value);
			} else if("category".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_CATEGORY, value);
			} else if("city".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_CITY, value);
			} else if("country".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_COUNTRY, value);
			} else if("dmacode".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_DMACODE, value);
			} else if("keywords".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_KEYWORDS, value);
			} else if("lattitude".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_LATITUDE, value);
			} else if("longitude".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_LONGITUDE, value);
			} else if("maxproddesclen".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_MAX_PROD_DESC_LEN, value);
			} else if("minnumleadgens".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_MIN_NUM_LEADGENS, value);
			} else if("multivaluefield1".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_MULTI_VALUE_FIELD1, value);
			} else if("multivaluefield2".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_MULTI_VALUE_FIELD2, value);
			} else if("multivaluefield3".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_MULTI_VALUE_FIELD3, value);
			} else if("multivaluefield4".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_MULTI_VALUE_FIELD4, value);
			} else if("multivaluefield5".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_MULTI_VALUE_FIELD5, value);
			} else if("numproducts".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_NUM_PRODUCTS, value);
			} else if("recipeid".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_RECIPE_ID, value);
			} else if("region".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_REGION, value);
			} else if("reverttodefaultrealm".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_REVERT_TO_DEFAULT_REALM, value);
			} else if("rowsize".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_ROW_SIZE, value);
			} else if("scriptkeywords".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_SCRIPT_KEYWORDS, value);
			} else if("storeid".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_STORE_ID, value);
			} else if("tspec".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_T_SPEC, value);
			} else if("url".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_URL, value);
			} else if("whichrow".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_WHICH_ROW, value);
			} else if("zipcode".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_ZIP_CODE, value);
			}
		}
		return req;
	}
	/**
	 * @param numProducts
	 * @return JozAdResponse resulting from a query to JozAdDataProvider
	 * @throws IOException
	 * The main purpose of this method is to:
	 * 1)randomly select a request from the list of possible requests
	 * 2)parse the selected request into a JozAdRequest
	 * 3)use JozAdDataProvider to process the request
	 */
	protected JozAdResponse sendRequest(int numProducts) throws IOException {
		String requestString = null;
		int index = Math.abs(r.nextInt()%totalAvailableRequests);
		requestString = requests.get(index);
		//parse input String request into a JozAdRequest
		StringTokenizer reqTokenizer = new StringTokenizer(requestString);
		String key = "";
		String value = "";
		JozAdRequest req = new JozAdRequest();
		req.setValue(JozAdRequest.KEY_NUM_PRODUCTS, String.valueOf(numProducts));
		while(reqTokenizer.hasMoreTokens()){
			key = reqTokenizer.nextToken();
			value = reqTokenizer.nextToken();

			if("theme".equalsIgnoreCase(key) || "locationname".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_THEME, value);
			} else if("adheight".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_AD_HEIGHT, value);
			} else if("adtype".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_AD_TYPE, value);
			} else if("adwidth".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_AD_WIDTH, value);
			} else if("allowtoofewproducts".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_ALLOW_TOO_FEW_PRODUCTS, value);
			} else if("areacode".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_AREACODE, value);
			} else if("category".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_CATEGORY, value);
			} else if("city".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_CITY, value);
			} else if("country".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_COUNTRY, value);
			} else if("dmacode".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_DMACODE, value);
			} else if("keywords".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_KEYWORDS, value);
			} else if("lattitude".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_LATITUDE, value);
			} else if("longitude".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_LONGITUDE, value);
			} else if("maxproddesclen".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_MAX_PROD_DESC_LEN, value);
			} else if("minnumleadgens".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_MIN_NUM_LEADGENS, value);
			} else if("multivaluefield1".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_MULTI_VALUE_FIELD1, value);
			} else if("multivaluefield2".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_MULTI_VALUE_FIELD2, value);
			} else if("multivaluefield3".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_MULTI_VALUE_FIELD3, value);
			} else if("multivaluefield4".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_MULTI_VALUE_FIELD4, value);
			} else if("multivaluefield5".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_MULTI_VALUE_FIELD5, value);
			} else if("numproducts".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_NUM_PRODUCTS, value);
			} else if("recipeid".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_RECIPE_ID, value);
			} else if("region".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_REGION, value);
			} else if("reverttodefaultrealm".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_REVERT_TO_DEFAULT_REALM, value);
			} else if("rowsize".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_ROW_SIZE, value);
			} else if("scriptkeywords".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_SCRIPT_KEYWORDS, value);
			} else if("storeid".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_STORE_ID, value);
			} else if("tspec".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_T_SPEC, value);
			} else if("url".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_URL, value);
			} else if("whichrow".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_WHICH_ROW, value);
			} else if("zipcode".equalsIgnoreCase(key)){
				req.setValue(JozAdRequest.KEY_ZIP_CODE, value);
			}
		}

		JozAdResponse res = _impl.processRequest(req);
		inspectResponseData(res);
		return res;
	}

	/**
	 * @param response
	 * this method inspects the response to see if it is first null and then if it has an ERROR
	 */
	private void inspectResponseData(JozAdResponse response) {
        totalSize.addAndGet(response.getResponse().length);
//        String str1 = new String(resp);
//        HashMap<String, String> resultMap = response.getResultMap();
//        log.info(str1);
        //if (log.isInfoEnabled()) {
//			String resultValue;
//			if(response != null){
//				resultValue = response.getResultMap().get(JozAdResponse.KEY_ERROR);
//				if(resultValue != null){
//					log.info("Lookup failed. ResultMap has Error: " + resultValue);
//					numFailedRequests.incrementAndGet();
//				}
//			} else {
//				numFailedRequests.incrementAndGet();
//				log.info("Lookup faild. No result");
//			}
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
		int numRuns = 1000;
		int numProducts = 10;
		String serverName = "localhost";
		int port = 2544;
		int poolSize = 10;
		switch(args.length) {
			case 6: {
				poolSize = Integer.parseInt(args[5]);
			}
			case 5: {
				serverName = args[4];
			}
			case 4: {
				port = Integer.parseInt(args[3]);
			}
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
		System.out.println("Test: " + numThreads + " threads, " + numRuns + " runs/thread, " + numProducts
				+ " products/request, Server = " + serverName + ", port : " + port + ", socketPoolSize : " + poolSize );
		(new TestJozClient()).runTest(serverName, port, poolSize, numThreads, numRuns, numProducts);

	}

}
