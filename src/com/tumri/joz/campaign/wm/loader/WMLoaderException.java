/*
 * WMLoaderException.java
 *
 * COPYRIGHT (C)  TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (@tumri.com)
 * @version 1.0     
 *
 */
package com.tumri.joz.campaign.wm.loader;

/**
 * @author: nipun
 * Date: Aug 13, 2009
 * Time: 1:35:27 PM
 */
public class WMLoaderException extends Exception {

    public WMLoaderException() {
    }

    public WMLoaderException(String string) {
        super(string);
    }

    public WMLoaderException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public WMLoaderException(Throwable throwable) {
        super(throwable);
    }
}
