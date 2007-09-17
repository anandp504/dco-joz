package com.tumri.joz.campaign;

import static org.junit.Assert.*;
import org.junit.*;
import com.tumri.cma.CMAFactory;
import com.tumri.cma.CMAException;
import com.tumri.cma.util.DeepCopy;
import com.tumri.cma.domain.OSpec;
import com.tumri.cma.service.CampaignDeltaProvider;

import java.util.Iterator;

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
            CampaignDB campaignDB = CampaignDB.getInstance();
            assertNotNull(campaignDB);
        }
        catch (CampaignDataLoadingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail("Exception occured");
        }

    }

    
    public void testIteratorCloning() {
        try {
           CampaignDeltaProvider provider = CMAFactory.getInstance().getCampaignDeltaProvider();
            Iterator<OSpec> oSpecIterator = provider.getOspecs("USA");
            Iterator<OSpec> oSpecIterator2 = (Iterator<OSpec>)DeepCopy.copy(oSpecIterator);
            System.out.println("Iterator 1");
            if(oSpecIterator != null) {
                while(oSpecIterator.hasNext()) {
                    OSpec oSpec = oSpecIterator.next();
                    System.out.print(oSpec.getId() + " ,");
                }
                System.out.println("/n");
            }
            System.out.println("Iterator 1");
            if(oSpecIterator2 != null) {
                while(oSpecIterator2.hasNext()) {
                    OSpec oSpec2 = oSpecIterator2.next();
                    System.out.print(oSpec2.getId() + " ,");
                }

            }

        }
        catch (CMAException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail("Exception occured");
        }

    }
}
