/* 
 * LogUtils.java
 * 
 * COPYRIGHT (C) 2007 TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE 
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY, 
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL 
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART 
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM 
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR 
 * WRITTEN PERMISSION OF TUMRI INC.
 * 
 * @author Bhavin Doshi (bdoshi@tumri.com)
 * @version 1.0     Oct 10, 2007
 * 
 */
package com.tumri.joz.utils;

import org.apache.log4j.Logger;


/**
 *
 * Created by Bhavin Doshi (bdoshi@tumri.com) on Oct 10, 2007
 * Company: Tumri Inc.
 */
public class LogUtils {
    protected static Logger fatallog = Logger.getLogger("fatal");
    protected static Logger timinglog = Logger.getLogger("timing");

    public static Logger getFatalLog() {
        return fatallog;
    }

    public static Logger getTimingLog() {
        return timinglog;
    }

}
