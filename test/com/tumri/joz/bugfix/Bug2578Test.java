/*
 * Bug2578Test.java
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
package com.tumri.joz.bugfix;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import com.tumri.joz.campaign.CampaignDBDataLoader;
import com.tumri.joz.targeting.TargetingRequestProcessor;
import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.cma.domain.Recipe;

/**
 * @author: nipun
 * Date: May 7, 2009
 * Time: 8:55:43 AM
 */
public class Bug2578Test {

    @BeforeClass
    public static void initialize() throws Exception {
        CampaignDBDataLoader loader = CampaignDBDataLoader.getInstance();
        loader.loadData();
    }

    @Test
    public void testSingleMatches() {
        TargetingRequestProcessor trp = TargetingRequestProcessor.getInstance();
        JozAdRequest jozRequest = new JozAdRequest();
        jozRequest.setValue(JozAdRequest.KEY_LOCATION_ID, "106562");
        jozRequest.setValue(JozAdRequest.KEY_AD_TYPE, "mediumrectangle");
        jozRequest.setValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD1, ",100");

        AdDataRequest request = new AdDataRequest(jozRequest);
        Features f = new Features();
        Recipe r = trp.processRequest(request, f);
        Assert.assertTrue(r!=null);
        System.out.println(r.getName() + " " + r.getId() + " " + r.getAdpodId());
        System.out.println(f.getAdpodName());
        Assert.assertTrue(f.getAdpodName().equals("NS-Shalane AdPod"));

    }

    @Test
    public void testMultiMatches() {
        TargetingRequestProcessor trp = TargetingRequestProcessor.getInstance();
        JozAdRequest jozRequest = new JozAdRequest();
        jozRequest.setValue(JozAdRequest.KEY_LOCATION_ID, "106562");
        jozRequest.setValue(JozAdRequest.KEY_AD_TYPE, "mediumrectangle");
        jozRequest.setValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD1, ",100,200");

        AdDataRequest request = new AdDataRequest(jozRequest);
        Features f = new Features();
        Recipe r = trp.processRequest(request, f);
        Assert.assertTrue(r!=null);
        System.out.println(r.getName() + " " + r.getId() + " " + r.getAdpodId());
        System.out.println(f.getAdpodName());
        Assert.assertTrue(f.getAdpodName().equals("NS-Shalane AdPod"));

    }

    @Test
    public void testMultiMatchesWithInvalids() {
        TargetingRequestProcessor trp = TargetingRequestProcessor.getInstance();
        JozAdRequest jozRequest = new JozAdRequest();
        jozRequest.setValue(JozAdRequest.KEY_LOCATION_ID, "106562");
        jozRequest.setValue(JozAdRequest.KEY_AD_TYPE, "mediumrectangle");
        jozRequest.setValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD1, ",100,blah");

        AdDataRequest request = new AdDataRequest(jozRequest);
        Features f = new Features();
        Recipe r = trp.processRequest(request, f);
        Assert.assertTrue(r!=null);
        System.out.println(r.getName() + " " + r.getId() + " " + r.getAdpodId());
        System.out.println(f.getAdpodName());
        Assert.assertTrue(f.getAdpodName().equals("NS-Shalane AdPod"));

    }
}