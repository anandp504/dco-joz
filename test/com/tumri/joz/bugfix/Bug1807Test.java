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
package com.tumri.joz.bugfix;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import com.tumri.joz.campaign.CampaignDBDataLoader;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.UrlNormalizer;
import com.tumri.joz.campaign.OSpecHelper;
import com.tumri.joz.products.Handle;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.SortedSet;

/**
 * Unit Test case for Bug 1807
 * This bug has to do with testing delete for a given transient OSpec
 */
public class Bug1807Test {
    
    @BeforeClass
    public static void initialize() throws Exception {
        CampaignDBDataLoader loader = CampaignDBDataLoader.getInstance();
        loader.loadData();
    }


    @Test
    public void testBug1807() {

        //1. Create a new tspec to replace an existing tspec
        String tSpecName = "T-SPEC-http://default-realm/";
        TransientDataTestUtil.addNewTSpec(tSpecName);
        try {
            //2. Delete the tspec
            OSpecHelper.doTSpecDelete(tSpecName);
        } catch( Exception e) {
            fail("Exception caught during tspec delete");
            e.printStackTrace();
        }
    }


}
