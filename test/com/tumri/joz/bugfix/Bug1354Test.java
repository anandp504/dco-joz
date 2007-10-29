package com.tumri.joz.bugfix;

import org.junit.BeforeClass;
import org.junit.Test;
import com.tumri.joz.campaign.CampaignDBDataLoader;

/**
 * Test case for regression testing of bug 1354
 *
 * @author bpatel
 */
public class Bug1354Test {
    @BeforeClass
    public static void initialize() throws Exception {
        CampaignDBDataLoader loader = CampaignDBDataLoader.getInstance();
        loader.loadData();
    }


    @Test
    public void testBug1354() {
        //1. Create a new tspec TSPEC-joz-test2
        String tSpecName = "TSPEC-joz-test2";
        TransientDataTestUtil.addNewTSpec(tSpecName);

        //2. Add new theme mapping with geo location
        String mappingWithGeoStr = "(incorp-mapping-deltas '((:add (:theme \"theme-test-joz\")  ((:zip \"99005\"))  nil |TSPEC-joz-test2| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(mappingWithGeoStr);

        //3. Make a get-ad-data request for theme using geo
        String tSpecGetGeoStr = "(get-ad-data :zip-code \"99005\" :theme \"theme-test-joz\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, tSpecName);

        //4. Make a get-ad-data request for theme without geo
        String tSpecGetUrlMappingStr = "(get-ad-data :theme \"theme-test-joz\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetUrlMappingStr, null);

        //4. Delete the theme mapping
        String themeDeleteIncorpMappingStr = "(incorp-mapping-deltas '((:delete (:theme \"theme-test-joz\")  ((:zip \"99005\")) nil |TSPEC-joz-test2| 1.0f0 31230170)))";
        TransientDataTestUtil.deleteIncorpDeltaMapping(themeDeleteIncorpMappingStr);

    }

    @Test
    public void test2Bug1354() {
        //1. Create a new tspec TSPEC-joz-test2
        String tSpecName = "TSPEC-joz-test2";
        TransientDataTestUtil.addNewTSpec(tSpecName);

        //2. Create a theme tspec mapping
        String themeMappingStr = "(incorp-mapping-deltas '((:add (:theme \"theme-test-joz\")  nil  nil |TSPEC-joz-test2| 1.0f0 30786374))) ";
        TransientDataTestUtil.addNewMapping(themeMappingStr);

        //3. make a get-ad-data request for above theme
        String tSpecGetStr = "(get-ad-data :theme \"theme-test-joz\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetStr, tSpecName);

        //4. Delete the theme mapping
        String themeDeleteIncorpMappingStr = "(incorp-mapping-deltas '((:delete (:theme \"theme-test-joz\")  nil nil |TSPEC-joz-test2| 1.0f0 31230170)))";
        TransientDataTestUtil.deleteIncorpDeltaMapping(themeDeleteIncorpMappingStr);

        //5. make a get-ad-data request for theme
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetStr, null);

        //6. Add new theme mapping with geo location
        String mappingWithGeoStr = "(incorp-mapping-deltas '((:add (:theme \"theme-test-joz\")  ((:zip \"99005\"))  nil |TSPEC-joz-test2| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(mappingWithGeoStr);

        //7. Make a get-ad-data request for theme using geo
        String tSpecGetGeoStr = "(get-ad-data :zip-code \"99005\" :theme \"theme-test-joz\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, tSpecName);

        //8. Make a get-ad-data request for theme without geo
        String tSpecGetUrlMappingStr = "(get-ad-data :theme \"theme-test-joz\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetUrlMappingStr, null);
    }

}
