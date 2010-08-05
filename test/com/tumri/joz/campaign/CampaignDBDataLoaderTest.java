package com.tumri.joz.campaign;

import static org.junit.Assert.*;

import com.tumri.cma.domain.CAMDimensionType;
import com.tumri.cma.domain.Experience;
import com.tumri.cma.domain.UIProperty;
import com.tumri.joz.campaign.wm.VectorDB;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.productselection.ProductSelectionResults;
import com.tumri.joz.server.domain.JozAdRequest;
import com.tumri.joz.targeting.TargetingRequestProcessor;
import com.tumri.joz.targeting.TargetingResults;
import org.junit.*;
import com.tumri.cma.CMAFactory;
import com.tumri.cma.CMAException;
import com.tumri.cma.util.DeepCopy;
import com.tumri.cma.domain.OSpec;
import com.tumri.cma.service.CampaignDeltaProvider;

import java.util.Iterator;
import java.util.List;

/**
 * Test case for campaign data loader
 */
public class
        CampaignDBDataLoaderTest {
    private static CampaignDBDataLoader loader;
    @BeforeClass
    public static void initialize() throws Exception {
        loader = CampaignDBDataLoader.getInstance();
    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testLoadData() {
        try {
            loader.loadData();
            CampaignDB campaignDB = CampaignDB.getInstance();
            assertNotNull(campaignDB);
        }
        catch (CampaignDataLoadingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail("Exception occured");
        }

    }
    
    @Test
    public void testTargetingExperience() {
        TargetingRequestProcessor trp = TargetingRequestProcessor.getInstance();
        JozAdRequest jozRequest = new JozAdRequest();
        jozRequest.setValue(JozAdRequest.KEY_EXPERIENCE_ID, "100");
        AdDataRequest request = new AdDataRequest(jozRequest);
        Features f = new Features();
        TargetingResults trs = trp.processRequest(request, f);
        ProductSelectionResults pResults = new ProductSelectionResults();
        pResults.setAttributePositions(trs.getAttributePositions());
        pResults.setAttributeValues(trs.getAttributeValues());
        pResults.setCamDimensionNames(trs.getCamDimensionNames());
        pResults.setCamDimensionTypes(trs.getCamDimensionTypes());
        pResults.setTargetedExperience(trs.getExperience());
        System.out.println(getExpData(pResults));
        assertNotNull(trs.getExperience());
    }

    @Test
    public void testTargetingAdpod() {
        TargetingRequestProcessor trp = TargetingRequestProcessor.getInstance();
        JozAdRequest jozRequest = new JozAdRequest();
        jozRequest.setValue(JozAdRequest.KEY_LOCATION_ID, "105146");
        jozRequest.setValue(JozAdRequest.KEY_AD_TYPE, "mediumrectangle");
        AdDataRequest request = new AdDataRequest(jozRequest);
        Features f = new Features();
        TargetingResults trs = trp.processRequest(request, f);
        ProductSelectionResults pResults = new ProductSelectionResults();
        pResults.setAttributePositions(trs.getAttributePositions());
        pResults.setAttributeValues(trs.getAttributeValues());
        pResults.setCamDimensionNames(trs.getCamDimensionNames());
        pResults.setCamDimensionTypes(trs.getCamDimensionTypes());
        pResults.setTargetedExperience(trs.getExperience());
        System.out.println(getExpData(pResults));
        assertNotNull(trs.getExperience());
    }

    private String getExpData(ProductSelectionResults prs) {
        StringBuilder sbuild = new StringBuilder();
        Experience exp = prs.getTargetedExperience();
        List<UIProperty> props = exp.getProperties();
        if (props != null) {
            int count = props.size();
            int i = 0;
            String design = exp.getDesign();
            if (design != null) {
                sbuild.append("design===" + design + "&&&");
            }
            for (UIProperty prop : props) {
                String name = prop.getName();
                String value = prop.getValue();

                if (name != null && !name.equals("") && value != null && !value.equals("")) {
                    sbuild.append(name + "===" + value + "&&&");
                }
            }
        }

        CAMDimensionType[] dims = prs.getCamDimensionTypes();
        int[] attrPosArr = prs.getAttributePositions();
        String[] dimNamesArr = prs.getCamDimensionNames();
        String[] dimValuesArr = prs.getAttributeValues();
        if ( dims!=null && attrPosArr!=null && dimNamesArr!=null && dimValuesArr!=null ) {
            for (int i =0;i<dims.length;i++) {
                CAMDimensionType dim = dims[i];
                switch(dim) {
                    case D1:
                        sbuild.append("CA1NAME===" + dimNamesArr[i] + "&&&");
                        sbuild.append("CA1ID===" + attrPosArr[i] + "&&&");
                        sbuild.append("CA1VALUE===" + dimValuesArr[i] + "&&&");
                        break;
                    case D2:
                        sbuild.append("CA2NAME===" + dimNamesArr[i] + "&&&");
                        sbuild.append("CA2ID===" + attrPosArr[i] + "&&&");
                        sbuild.append("CA2VALUE===" + dimValuesArr[i] + "&&&");
                        break;
                    case D3:
                        sbuild.append("CA3NAME===" + dimNamesArr[i] + "&&&");
                        sbuild.append("CA3ID===" + attrPosArr[i] + "&&&");
                        sbuild.append("CA3VALUE===" + dimValuesArr[i] + "&&&");
                        break;
                    case D4:
                        sbuild.append("CA4NAME===" + dimNamesArr[i] + "&&&");
                        sbuild.append("CA4ID===" + attrPosArr[i] + "&&&");
                        sbuild.append("CA4VALUE===" + dimValuesArr[i] + "&&&");
                        break;
                    case D5:
                        sbuild.append("CA5NAME===" + dimNamesArr[i] + "&&&");
                        sbuild.append("CA5ID===" + attrPosArr[i] + "&&&");
                        sbuild.append("CA5VALUE===" + dimValuesArr[i] + "&&&");
                        break;
                    default:
                }
            }
        }
        String res = sbuild.toString();
        if (res.endsWith("&&&")) {
            res = res.substring(0,res.length()-3);
        }

        return res;

    }


}
