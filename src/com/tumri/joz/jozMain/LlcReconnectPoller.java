/*
 * ListingsQueryHandler.java
 *
 * COPYRIGHT (C) ${year} TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (${user}@tumri.com)
 * @version 1.0     ${date}
 *
 */
package com.tumri.joz.jozMain;

import com.tumri.content.data.AdvertiserMerchantDataMapper;
import com.tumri.content.data.AdvertiserTaxonomyMapper;
import com.tumri.joz.utils.LogUtils;
import com.tumri.lls.client.main.LLCClientException;
import com.tumri.lls.client.main.ListingProvider;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * Timer class that will take care of doing the content refresh with LLS in case LLS is unavailable during startup.
 * The content refresh is needed for the listing formats to be available in Joz 
 * The thread kills itself once the connection is successful.
 * @author: nipun
* Date: Apr 18, 2008
* Time: 11:25:53 AM
*/
public class LlcReconnectPoller {
    private AdvertiserTaxonomyMapper advTaxProv;
    private AdvertiserMerchantDataMapper md;
    private ListingProvider lp;
    
    private static Logger log = Logger.getLogger(LlcReconnectPoller.class);

    protected Timer _timer = new Timer();
    protected int repeatIntervalSecs = 10; //every 10 secs
    private static LlcReconnectPoller g_inst= null;

   /**
	 * Returns an static reference to the CMAContentPoller
	 * @return
	 */
	public static LlcReconnectPoller getInstance(ListingProvider lp, AdvertiserTaxonomyMapper advTaxProv, AdvertiserMerchantDataMapper m) {
		if (g_inst == null) {
			g_inst =  new LlcReconnectPoller(lp, advTaxProv,m);
		}
		return g_inst;
	}

    private LlcReconnectPoller(ListingProvider tlp, AdvertiserTaxonomyMapper advTaxProv, AdvertiserMerchantDataMapper m) {
        this.advTaxProv = advTaxProv;
        this.md = m;
        this.lp = tlp;
    }

    public void init(){
        startTimer();
    }

    /**
     * Shutdown the timer task
     *
     */
    public void shutdown() {
        _timer.cancel();
    }


    public void performTask() {
        boolean bInit;

        try {
            log.info("Attempting to re-init the LLC listing provider");
            bInit = lp.doContentRefresh(advTaxProv,md);
        } catch (LLCClientException e) {
           LogUtils.getFatalLog().fatal("Exception caught on initializing content provider");
           bInit = false;
        } 

        if (bInit) {
            //Stop the polling
            log.info("Listing provider re-init successful");
            shutdown();
        }

    }

    /**
     * Start the timer.
     */
    private void startTimer()
    {
        _timer.schedule(new TimerTask() {
            public void run()
            {
              performTask();
            }
        }, 1, repeatIntervalSecs *1000);

    }

}
