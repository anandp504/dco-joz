/*
 * TestRecipeWeightTargeting.java
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
package com.tumri.joz.targeting;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import com.tumri.joz.campaign.CampaignDBDataLoader;
import com.tumri.joz.campaign.wm.loader.WMXMLParserV1;
import com.tumri.joz.campaign.wm.loader.WMXMLParser;
import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.cma.domain.Recipe;

/**
 * @author: nipun
 * Date: Aug 12, 2009
 * Time: 11:02:49 AM
 */
public class TestRecipeWeightTargeting {

    @BeforeClass
    public static void initialize() throws Exception {
        CampaignDBDataLoader loader = CampaignDBDataLoader.getInstance();
        loader.loadData();

        WMXMLParser parser = new WMXMLParserV1();
        parser.process("/Users/nipun/ws/work-nbp/depot/Tumri/tas/joz/test/data/csl/wm.xml");

//        //Load the weights
//        Map<WMIndex.Attribute, Integer> contextMap = new HashMap<WMIndex.Attribute, Integer>();
//        contextMap.put(WMIndex.Attribute.kState, WMUtils.getDictId(WMIndex.Attribute.kState,"CA"));
//        contextMap.put(WMIndex.Attribute.kLineId, WMUtils.getDictId(WMIndex.Attribute.kLineId,"CA"));
//        contextMap.put(WMIndex.Attribute.kCountry, WMUtils.getDictId(WMIndex.Attribute.kCountry,"CA"));
//        contextMap.put(WMIndex.Attribute.kZip, WMUtils.getDictId(WMIndex.Attribute.kZip,"CA"));
//        //contextMap.put(WMIndex.Attribute.kLineId, "5555");
//
//        List<RecipeWeight> rwList = new ArrayList<RecipeWeight>();
//        rwList.add(new RecipeWeight(1974, 100));
//        rwList.add(new RecipeWeight(4219, 0));
//        rwList.add(new RecipeWeight(4220, 0));
//        WMHandle h = WMHandleFactory.getInstance().getHandle(100, contextMap, rwList);
//
//        WMDBLoader.updateDb(4445, contextMap, h);
//
//        Map<WMIndex.Attribute, Integer> contextMap2 = new HashMap<WMIndex.Attribute, Integer>();
//        contextMap2.put(WMIndex.Attribute.kState, WMUtils.getDictId(WMIndex.Attribute.kState,"CA"));
//        contextMap2.put(WMIndex.Attribute.kLineId, WMUtils.getDictId(WMIndex.Attribute.kLineId,"12345A"));
//        //contextMap2.put(WMIndex.Attribute.kLineId, "9888");
//        //contextMap2.put(WMIndex.Attribute.kZip, "78978");
//
//        List<RecipeWeight> rwList2 = new ArrayList<RecipeWeight>();
//        rwList2.add(new RecipeWeight(4220, 100));
//        rwList2.add(new RecipeWeight(4219, 0));
//        rwList2.add(new RecipeWeight(1974, 0));
//        WMHandle h2 = WMHandleFactory.getInstance().getHandle(101, contextMap2, rwList2);
//
//        WMDBLoader.updateDb(4445, contextMap2, h2);
    }

    @Test
    public void testCase0() {
        for (int i=0;i<10;i++) {
            TargetingRequestProcessor trp = TargetingRequestProcessor.getInstance();
            JozAdRequest jozRequest = new JozAdRequest();
            jozRequest.setValue(JozAdRequest.KEY_LOCATION_ID, "108173");
            jozRequest.setValue(JozAdRequest.KEY_EXTERNAL_PAGE_ID, "13477514");
            jozRequest.setValue(JozAdRequest.KEY_REGION, "TX");
            jozRequest.setValue(JozAdRequest.KEY_DMACODE, "123567");
            jozRequest.setValue(JozAdRequest.KEY_AREACODE, "333");
            jozRequest.setValue(JozAdRequest.KEY_AD_TYPE, "mediumrectangle");
            jozRequest.setValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD2, "d,a");
            jozRequest.setValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD4, "g");
            jozRequest.setValue(JozAdRequest.KEY_USER_BUCKET, "99");

            AdDataRequest request = new AdDataRequest(jozRequest);
            Features f = new Features();
            Recipe r = trp.processRequest(request, f);
            Assert.assertTrue(r!=null);
            System.out.println("Try " + i + " ===>" + r.getName() + " " + r.getId() + " " + r.getAdpodId());
            System.out.println("Try " + i + " ===>" + f.getAdpodName());
            try {
                System.out.println("Selected Context ID ===>" + f.getFeaturesDetail("RWM-ID"));
            } catch (Exception e) {
                //
            }
            System.out.println("---------------------");
            //Assert.assertTrue(f.getAdpodName().equals("admin_custom"));
        }
    }



}
