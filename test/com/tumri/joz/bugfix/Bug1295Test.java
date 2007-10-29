package com.tumri.joz.bugfix;

import org.junit.Test;
import org.junit.BeforeClass;

import com.tumri.joz.campaign.CampaignDBDataLoader;


/**
 * Unit Test case for Bug 1295
 */
public class Bug1295Test {
    @BeforeClass
    public static void initialize() throws Exception {
        CampaignDBDataLoader loader = CampaignDBDataLoader.getInstance();
        loader.loadData();
    }


    @Test
    public void testBug1295() {
        //1. Create a new tspec TSPEC-joz-test2
        String tSpecName = "TSPEC-joz-test2";
        TransientDataTestUtil.addNewTSpec(tSpecName);

        //2. Create a url tspec mapping
        String urlMappingStr = "(incorp-mapping-deltas '((:add (:realm \"http://test-joz.com\")  nil  nil |TSPEC-joz-test2| 1.0f0 30763388)))";
        TransientDataTestUtil.addNewMapping(urlMappingStr);

        //3. Create a theme tspec mapping
        String themeMappingStr = "(incorp-mapping-deltas '((:add (:theme \"theme-test-joz\")  nil  nil |TSPEC-joz-test2| 1.0f0 30786374))) ";
        TransientDataTestUtil.addNewMapping(themeMappingStr);

        //4. make a get-ad-data request for above url http://test-joz.com
        String tSpecGetStr = "(get-ad-data :url \"http://test-joz.com\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetStr, tSpecName);

        //5. Delete mapping for url http://test-joz.com
        String urlIncorpMappingStr = "(incorp-mapping-deltas '((:delete (:realm \"http://test-joz.com\")  ()  nil |TSPEC-joz-test2| 1.0f0 30941991)))";
        TransientDataTestUtil.deleteIncorpDeltaMapping(urlIncorpMappingStr);

        //6. Add new mapping with geo location
        String mappingWithGeoStr = "(incorp-mapping-deltas '((:add (:realm \"http://test-joz.com\")  ((:zip \"99005\"))  nil |TSPEC-joz-test2| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(mappingWithGeoStr);

        //7. Make a get-ad-data request for url using geo
        String tSpecGetGeoStr = "(get-ad-data :zip-code \"99005\" :url \"http://test-joz.com\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, tSpecName);

        //8. Make a get-ad-data request for url without geo
        String tSpecGetUrlMappingStr = "(get-ad-data :url \"http://test-joz.com\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetUrlMappingStr, null);

        //8. Make a get-ad-data request for theme theme-test-joz
        String tSpecGetThemeStr = "(get-ad-data :theme \"theme-test-joz\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetThemeStr, tSpecName);

        //9. Delete the url mapping
        String urlDeleteIncorpMappingStr = "(incorp-mapping-deltas '((:delete (:realm \"http://test-joz.com\")  ((:zip \"99005\"))  nil |TSPEC-joz-test2| 1.0f0 31230170)))";
        TransientDataTestUtil.deleteIncorpDeltaMapping(urlDeleteIncorpMappingStr);

        //10. make a get-ad-data request for url http://test-joz.com
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetStr, null);

        //11. make a get-ad-data request for theme theme-test-joz
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetThemeStr, tSpecName);

        //12. make a get-ad-data request for theme theme-test-joz
        String tSpecGetGeoThemeStr = "(get-ad-data :zip-code \"99005\" :theme \"theme-test-joz\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoThemeStr, tSpecName);

        //13. Delete the theme mapping
        String tSpecDeleteGeoThemeStr = "(incorp-mapping-deltas '((:delete (:theme \"theme-test-joz\") nil  nil |TSPEC-joz-test2| 1.0f0 30786374)))";
        TransientDataTestUtil.deleteIncorpDeltaMapping(tSpecDeleteGeoThemeStr);

        //14. make a get-ad-data request for theme theme-test-joz
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetThemeStr, null);

    }


}
