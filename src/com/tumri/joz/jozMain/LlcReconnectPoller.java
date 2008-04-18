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

import com.tumri.content.data.Taxonomy;
import com.tumri.content.MerchantDataProvider;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.LogUtils;
import com.tumri.lls.client.main.LLCClientException;
import com.tumri.lls.client.main.ListingProvider;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * Timer class that will take care of reconnecting to LLS. The thread kills itself once the connection is successful.
 * @author: nipun
* Date: Apr 18, 2008
* Time: 11:25:53 AM
*/
public class LlcReconnectPoller {
    private Taxonomy taxonomy;
    private MerchantDataProvider md;
    private ListingProvider lp;
    
    private static Logger log = Logger.getLogger(LlcReconnectPoller.class);

    protected Timer _timer = new Timer();
    protected int repeatIntervalSecs = 60; //every 1 min
    private static LlcReconnectPoller g_inst= null;

   /**
	 * Returns an static reference to the CMAContentPoller
	 * @return
	 */
	public static LlcReconnectPoller getInstance(ListingProvider lp,Taxonomy tax, MerchantDataProvider m) {
		if (g_inst == null) {
			g_inst =  new LlcReconnectPoller(lp,tax,m);
		}
		return g_inst;
	}

    private LlcReconnectPoller(ListingProvider tlp, Taxonomy tax, MerchantDataProvider m) {
        this.taxonomy = tax;
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
            lp.init(AppProperties.getInstance().getProperties(), taxonomy, md);
            bInit = true;
        } catch (LLCClientException e) {
           LogUtils.getFatalLog().fatal("Exception caught on initializing content provider");
           bInit = false;
        }

        if (bInit) {
            //Stop the polling
            log.info("Reconnect successful");
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
