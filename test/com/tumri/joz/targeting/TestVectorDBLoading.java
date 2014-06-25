package com.tumri.joz.targeting;

import java.util.List;

import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.CampaignDBDataLoader;
import com.tumri.joz.campaign.CampaignDataLoadingException;
import com.tumri.joz.campaign.wm.VectorDB;
import com.tumri.joz.campaign.wm.VectorHandle;
import com.tumri.joz.campaign.wm.loader.WMDBLoader;
import com.tumri.joz.campaign.wm.loader.WMLoaderException;
import com.tumri.joz.campaign.wm.loader.WMXMLParser;
import com.tumri.joz.campaign.wm.loader.WMXMLParserV1;
import com.tumri.joz.utils.AppProperties;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA. User: nipun Date: Jun 17, 2010 Time: 3:10:03 PM To
 * change this template use File | Settings | File Templates.
 */
public class TestVectorDBLoading {

	@BeforeClass
	public static void init() {
		CampaignDBDataLoader loader = CampaignDBDataLoader.getInstance();
		try {
			loader.loadData();
			System.out.println("campaign loaded !");
		} catch (CampaignDataLoadingException e) {
			throw new RuntimeException(e);
		}
	}
	@Test
	public void testload0() {
		try {
			WMXMLParser parser = new WMXMLParserV1();
			parser.process("./test/data/csl/wm_1.xml");
		} catch (WMLoaderException e) {
			e.printStackTrace();
		}
		// VectorHandle h = VectorDB.getInstance().getVectorHandle(4445,101,1);
		VectorHandle h = VectorDB.getInstance().getVectorHandle(10935, 1, 0); // AdpodId 10935																		
		Assert.assertTrue(h != null);
		
		VectorHandle h0 = VectorDB.getInstance().getVectorHandle(10935, 9689, 0); // AdpodId 10935, vecId from wm.																		
		Assert.assertTrue(h0 == null);

		VectorHandle h2 = VectorDB.getInstance().getVectorHandle(14742, 9689, 2);
		Assert.assertTrue(h2 == null);
	}
}
