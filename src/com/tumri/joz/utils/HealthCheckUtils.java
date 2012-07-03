/*
 * ListingsQueryHandler.java
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
package com.tumri.joz.utils;

import com.tumri.content.data.impl.AdvertiserMerchantDataMapperImpl;
import com.tumri.content.data.impl.AdvertiserTaxonomyMapperImpl;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.jozMain.ListingProviderFactory;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.lls.client.main.ListingProvider;
import org.apache.log4j.Logger;

/**
 * @author: nipun
 * Date: Aug 15, 2008
 * Time: 4:18:06 PM
 */
public class HealthCheckUtils {

    private static Logger log = Logger.getLogger(HealthCheckUtils.class);
    /**
     * Does the Health check for Joz
     * 1) CampaignDB not empty
     * 2) ProductDB not empty
     * 3) Provider Index not empty
     * 3) Valid connection to LLS
     * @return  true for success, false for failure
     */
    public static boolean doHealthCheck() {
        boolean bStatus;
        try {
            // Atleast one campaign must be loaded
            if(CampaignDB.getInstance().isEmpty()){
                bStatus = false;
                return bStatus;
            }

            // atleast one product must be loaded
            if (ProductDB.getInstance().isEmpty()){
                bStatus = false;
                return bStatus;
            }

             // The joz Provider index must not be empty
             ProductAttributeIndex provIndex=ProductDB.getInstance().getIndex(IProduct.Attribute.kProvider);
             if (provIndex==null || provIndex.getKeys().isEmpty()) {
                 bStatus = false;
                 return bStatus;
             }

            ListingProvider  _prov = ListingProviderFactory.getProviderInstance(AdvertiserTaxonomyMapperImpl.getInstance(),
                        AdvertiserMerchantDataMapperImpl.getInstance());

            //Check for LLS health
            if (!_prov.doHealthCheck()) {
                bStatus = false;
                return bStatus;
            }

             //All good
             bStatus = true;
         } catch (Exception ex) {
             // In case of any exception
             log.error("Error in checking health status of the Joz system",ex);
             bStatus = false;
         }
        return bStatus;
    }
}
