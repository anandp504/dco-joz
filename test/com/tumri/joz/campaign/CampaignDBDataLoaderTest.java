package com.tumri.joz.campaign;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * Test case for campaign data loader
 */
public class CampaignDBDataLoaderTest {
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
        }
        catch (CampaignDataLoadingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail("Exception occured");
        }

    }
}
