package com.tumri.joz.targeting;

import com.tumri.joz.campaign.CampaignDBDataLoader;
import com.tumri.joz.campaign.CampaignDataLoadingException;
import com.tumri.joz.campaign.wm.VectorDB;
import com.tumri.joz.campaign.wm.VectorHandle;
import com.tumri.joz.campaign.wm.loader.WMLoaderException;
import com.tumri.joz.campaign.wm.loader.WMXMLParser;
import com.tumri.joz.campaign.wm.loader.WMXMLParserV1;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: nipun
 * Date: Jun 17, 2010
 * Time: 3:10:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestVectorDBLoading {

    @BeforeClass
    public static void init() {
        CampaignDBDataLoader loader = CampaignDBDataLoader.getInstance();
        try {
            loader.loadData();
        } catch (CampaignDataLoadingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testload0() {
        try {
            WMXMLParser parser = new WMXMLParserV1();
            parser.process("/Users/nipun/ws/work-nbp/depot/dev/branch/tc/tas/joz/test/data/csl/wm-test.xml");
        } catch (WMLoaderException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        VectorHandle h = VectorDB.getInstance().getVectorHandle(4445,101,1);
        Assert.assertTrue(h!=null);

        try {
            WMXMLParser parser = new WMXMLParserV1();
            parser.process("/Users/nipun/ws/work-nbp/depot/dev/branch/tc/tas/joz/test/data/csl/wm-test2.xml");
        } catch (WMLoaderException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        VectorHandle h2 = VectorDB.getInstance().getVectorHandle(4445,101,1);
        Assert.assertTrue(h2==null);


    }
}
