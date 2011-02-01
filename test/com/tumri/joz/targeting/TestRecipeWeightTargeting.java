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

import com.tumri.joz.campaign.wm.*;
import com.tumri.joz.campaign.wm.loader.WMLoaderException;
import com.tumri.joz.products.Handle;
import com.tumri.utils.data.SortedArraySet;
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

import java.util.*;

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

        try {
            WMXMLParser parser = new WMXMLParserV1();
            parser.process("/Users/nipun/ws/work-nbp/depot/Tumri/tas/joz/test/data/csl/wm-test.xml");
        } catch (WMLoaderException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        //Load the weights
//        List details = new ArrayList<Integer>();
//        details.add(WMUtils.getDictId(WMAttribute.kT1, "12345"));
//        Map<WMAttribute, List<Integer>> map = new HashMap<WMAttribute, List<Integer>>();
//        map.put(WMAttribute.kT1, details);
//        VectorHandle h1 = new VectorHandle(12, 10, 1, map, false );
//        SortedBag<Pair<CreativeSet, Double>> optRules = null;
//
//        WMDBLoader.updateDb(optRules, map, h1);
    }

    @Test
    public void test0() {
        VectorTargetingProcessor proc = VectorTargetingProcessor.getInstance();
        JozAdRequest jozRequest = new JozAdRequest();
        jozRequest.setValue(JozAdRequest.KEY_EXTERNAL_PAGE_ID, "12345");
        jozRequest.setValue(JozAdRequest.KEY_REGION, "CA");
//        jozRequest.setValue(JozAdRequest.KEY_ZIP_CODE, "94065");
        AdDataRequest request = new AdDataRequest(jozRequest);
        Map<VectorAttribute, List<Integer>> requestMap = VectorUtils.getContextMap(7476, -1, request);
        SortedSet<Handle> results = proc.getMatchingVectors(requestMap);
        Assert.assertTrue(results!=null);
        for (Handle h : results) {
            VectorHandle vh = (VectorHandle)h;
            int[] dets = VectorHandleImpl.getIdDetails(vh.getOid());
            System.out.println("Experience/Adpod id = " + dets[1] + ". Vector id = " + dets[0] + ". Score = " + vh.getScore());
        }
    }

    @Test
    public void test1() {
        VectorTargetingProcessor proc = VectorTargetingProcessor.getInstance();
        JozAdRequest jozRequest = new JozAdRequest();
        jozRequest.setValue(JozAdRequest.KEY_EXTERNAL_PAGE_ID, "4444");
        jozRequest.setValue(JozAdRequest.KEY_REGION, "WY");
        jozRequest.setValue(JozAdRequest.KEY_ZIP_CODE, "94065");
        AdDataRequest request = new AdDataRequest(jozRequest);
        Map<VectorAttribute, List<Integer>> requestMap = VectorUtils.getContextMap(-1, 4445, request);
        SortedSet<Handle> results = null; //proc.getMatchingVectors(requestMap);
        Assert.assertTrue(results!=null);
        for (Handle h : results) {
            VectorHandle vh = (VectorHandle)h;
            int[] dets = VectorUtils.getIdDetails(vh.getOid());
            System.out.println("Experience id = " + dets[1] + ". Vector id = " + dets[0] + ". Score = " + vh.getScore());
        }

        SortedSet<Handle> results1 = new SortedArraySet<Handle>(results, new VectorHandleImpl(0L));
        for (Handle h: results1) {
            VectorHandle vh = (VectorHandle)h;
            int[] dets = VectorUtils.getIdDetails(vh.getOid());
            System.out.println("After Sort Experience id = " + dets[1] + ". Vector id = " + dets[0] + ". Score = " + vh.getScore());
        }
    }

    @Test
    public void testCase0() {
        System.out.println("Beginning test");
        for (int i=0;i<10;i++) {
            TargetingRequestProcessor trp = TargetingRequestProcessor.getInstance();
            JozAdRequest jozRequest = new JozAdRequest();
            jozRequest.setValue(JozAdRequest.KEY_LOCATION_ID, "105002");
            jozRequest.setValue(JozAdRequest.KEY_EXTERNAL_PAGE_ID, "12345");
            jozRequest.setValue(JozAdRequest.KEY_REGION, "CA");
            jozRequest.setValue(JozAdRequest.KEY_DMACODE, "123567");
            jozRequest.setValue(JozAdRequest.KEY_AREACODE, "333");
            jozRequest.setValue(JozAdRequest.KEY_ZIP_CODE, "94065");
            jozRequest.setValue(JozAdRequest.KEY_AD_TYPE, "mediumrectangle");
            jozRequest.setValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD2, "d,a");
            jozRequest.setValue(JozAdRequest.KEY_EXTERNAL_TARGET_FIELD4, "g");
            jozRequest.setValue(JozAdRequest.KEY_USER_BUCKET, "99");
            VectorDB db = VectorDB.getInstance();
            AdDataRequest request = new AdDataRequest(jozRequest);
            Features f = new Features();
            TargetingResults trs = trp.processRequest(request, f);
            Recipe r = trs.getRecipe();
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
