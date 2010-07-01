/*
 * Bug2549Test.java
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

import com.tumri.cma.domain.Recipe;
import com.tumri.joz.campaign.CampaignDBDataLoader;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.targeting.TargetingRequestProcessor;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author: nipun
 * Date: Apr 20, 2009
 * Time: 4:44:36 PM
 */
public class Bug2549Test {

    @BeforeClass
    public static void initialize() throws Exception {
        CampaignDBDataLoader loader = CampaignDBDataLoader.getInstance();
        loader.loadData();
    }


    @Test
    public void testMultipleMappingAdpod() {
        TargetingRequestProcessor trp = TargetingRequestProcessor.getInstance();
        JozAdRequest jozRequest = new JozAdRequest();
        jozRequest.setValue(JozAdRequest.KEY_LOCATION_ID, "106414");
        jozRequest.setValue(JozAdRequest.KEY_AD_TYPE, "mediumrectangle");
        jozRequest.setValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD1, "passflipLA");
        jozRequest.setValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD4, "passflipDAL");

        AdDataRequest request = new AdDataRequest(jozRequest);

        Recipe r = null; //trp.processRequest(request, new Features());
        Assert.assertTrue(r!=null);
        System.out.println(r.getName() + " " + r.getId() + " " + r.getAdpodId());
        Assert.assertTrue(r.getAdpodId()==4824);

    }

    @Test
    public void testFindFallBackAdpodForMissingMapping() {
        TargetingRequestProcessor trp = TargetingRequestProcessor.getInstance();
        JozAdRequest jozRequest = new JozAdRequest();
        jozRequest.setValue(JozAdRequest.KEY_LOCATION_ID, "106414");
        jozRequest.setValue(JozAdRequest.KEY_AD_TYPE, "mediumrectangle");
        jozRequest.setValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD1, "passflipLA");

        AdDataRequest request = new AdDataRequest(jozRequest);

        Recipe r = null; //trp.processRequest(request, new Features());
        Assert.assertTrue(r!=null);
        System.out.println(r.getName() + " " + r.getId() + " " + r.getAdpodId());
        Assert.assertTrue(r.getAdpodId()==4856);

    }

    @Test
    public void testFindFallBackAdpodForBadMapping() {
        TargetingRequestProcessor trp = TargetingRequestProcessor.getInstance();
        JozAdRequest jozRequest = new JozAdRequest();
        jozRequest.setValue(JozAdRequest.KEY_LOCATION_ID, "106414");
        jozRequest.setValue(JozAdRequest.KEY_AD_TYPE, "mediumrectangle");
        jozRequest.setValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD1, "XXX");

        AdDataRequest request = new AdDataRequest(jozRequest);

        Recipe r = null; //trp.processRequest(request, new Features());
        Assert.assertTrue(r!=null);
        System.out.println(r.getName() + " " + r.getId() + " " + r.getAdpodId());
        Assert.assertTrue(r.getAdpodId()==4856);

    }

    public void testSingleMappingAdpod(){
        TargetingRequestProcessor trp = TargetingRequestProcessor.getInstance();
        JozAdRequest jozRequest = new JozAdRequest();
        jozRequest.setValue(JozAdRequest.KEY_LOCATION_ID, "106414");
        jozRequest.setValue(JozAdRequest.KEY_AD_TYPE, "mediumrectangle");
        jozRequest.setValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD1, "fourcorners");

        AdDataRequest request = new AdDataRequest(jozRequest);

        Recipe r = null; //trp.processRequest(request, new Features());
        Assert.assertTrue(r!=null);
        System.out.println(r.getName() + " " + r.getId() + " " + r.getAdpodId());
        Assert.assertTrue(r.getAdpodId()==5041);

    }
}
