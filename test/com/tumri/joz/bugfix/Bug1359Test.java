package com.tumri.joz.bugfix;

import org.junit.BeforeClass;
import org.junit.Test;
import com.tumri.joz.campaign.CampaignDBDataLoader;

/**
 * Unit Test case for Bug 1359
 */
public class Bug1359Test {
    @BeforeClass
    public static void initialize() throws Exception {
        CampaignDBDataLoader loader = CampaignDBDataLoader.getInstance();
        loader.loadData();
    }


    @Test
    public void testBug1359() {
        //1. Create a new tspec TSPEC-joz-test2
        String tSpecName = "TSPEC-joz-test2";
        TransientDataTestUtil.addNewTSpec(tSpecName);

        //2. Add new theme mapping with geo location
        String mappingWithGeoStr = "(incorp-mapping-deltas '((:add (:theme \"theme-test-joz\")  ((:zip \"99005\"))  nil |TSPEC-joz-test2| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(mappingWithGeoStr);

        //3. Add new theme mapping without geo location
        String mappingWithoutGeoStr = "(incorp-mapping-deltas '((:add (:theme \"theme-test-joz\")  nil  nil |TSPEC-joz-test2| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(mappingWithoutGeoStr);

        //4. Make a get-ad-data request for theme using geo
        String tSpecGetGeoStr = "(get-ad-data :zip-code \"99005\" :theme \"theme-test-joz\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, tSpecName);

        //5. Make a get-ad-data request for theme without geo
        String mappingStr = "(get-ad-data :theme \"theme-test-joz\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(mappingStr, tSpecName);

        //6. Delete the theme mapping
        String themeDeleteIncorpMappingStr = "(incorp-mapping-deltas '((:delete (:theme \"theme-test-joz\")  nil nil |TSPEC-joz-test2| 1.0f0 31230170)))";
        TransientDataTestUtil.deleteIncorpDeltaMapping(themeDeleteIncorpMappingStr);

        //7. Make a get-ad-data request for theme using geo
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, tSpecName);

        //8. Make a get-ad-data request for theme without geo
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(mappingStr, null);
    }

    @Test
    public void testDuplicateThemeMappingIssue() {
        //1. Create a new tspec TSPEC-joz-test2
        String tSpecName = "TSPEC-joz-test2";
        TransientDataTestUtil.addNewTSpec(tSpecName);

        //2. Add new theme mapping with geo location
        String mappingWithGeoStr = "(incorp-mapping-deltas '((:add (:theme \"theme-test-joz\")  ((:zip \"99005\"))  nil |TSPEC-joz-test2| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(mappingWithGeoStr);

        //3. Add new theme mapping with geo location
        String duplicateMappingWithGeoStr = "(incorp-mapping-deltas '((:add (:theme \"theme-test-joz\")  ((:zip \"99005\"))  nil |TSPEC-joz-test2| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(duplicateMappingWithGeoStr);

        //4. Add new theme mapping without geo location
        String mappingWithoutGeoStr = "(incorp-mapping-deltas '((:add (:theme \"theme-test-joz\")  nil  nil |TSPEC-joz-test2| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(mappingWithoutGeoStr);

        //5. Add new theme mapping without geo location
        String duplicateMappingWithoutGeoStr = "(incorp-mapping-deltas '((:add (:theme \"theme-test-joz\")  nil  nil |TSPEC-joz-test2| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(duplicateMappingWithoutGeoStr);

        //6. Make a get-ad-data request for theme using geo
        String tSpecGetGeoStr = "(get-ad-data :zip-code \"99005\" :theme \"theme-test-joz\")";        
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, tSpecName);

    }

    @Test
    public void testDuplicateUrlMappingIssue() {
        //1. Create a new tspec TSPEC-joz-test2
        String tSpecName = "TSPEC-joz-test2";
        TransientDataTestUtil.addNewTSpec(tSpecName);

        //2. Add new mapping with geo location
        String mappingWithGeoStr = "(incorp-mapping-deltas '((:add (:realm \"http://test-joz.com\")  ((:zip \"99005\"))  nil |TSPEC-joz-test2| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(mappingWithGeoStr);

        //3. Make a get-ad-data request for url using geo
        String tSpecGetGeoStr = "(get-ad-data :zip-code \"99005\" :url \"http://test-joz.com\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, tSpecName);

        //4. Make a get-ad-data request for url without geo
        String tSpecGetUrlMappingStr = "(get-ad-data :url \"http://test-joz.com\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetUrlMappingStr, null);

        //5. Add new mapping with geo location
        String duplicateMappingWithGeoStr = "(incorp-mapping-deltas '((:add (:realm \"http://test-joz.com\")  ((:zip \"99999\"))  nil |TSPEC-joz-test2| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(duplicateMappingWithGeoStr);
        
        //6. Make a get-ad-data request for url using geo
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, null);

        //7. Make a get-ad-data request for url using geo
        String tSpecGetGeoStr2 = "(get-ad-data :zip-code \"99999\" :url \"http://test-joz.com\")";        
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr2, tSpecName);

    }

    @Test
    public void testDuplicateLocationMappingIssue() {
        //1. Create a new tspec TSPEC-joz-test2
        String tSpecName = "TSPEC-joz-test2";
        TransientDataTestUtil.addNewTSpec(tSpecName);

        //2. Add new mapping with geo location
        String mappingWithGeoStr = "(incorp-mapping-deltas '((:add (:store-ID \"919191\")  ((:zip \"99005\"))  nil |TSPEC-joz-test2| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(mappingWithGeoStr);

        //3. Make a get-ad-data request for url using geo
        String tSpecGetGeoStr = "(get-ad-data :zip-code \"99005\" :store-ID \"919191\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, tSpecName);

        //4. Make a get-ad-data request for url without geo
        String tSpecGetUrlMappingStr = "(get-ad-data :store-ID \"919191\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetUrlMappingStr, null);

        //5. Add new mapping with geo location
        String duplicateMappingWithGeoStr = "(incorp-mapping-deltas '((:add (:store-ID \"919191\")  ((:zip \"99999\"))  nil |TSPEC-joz-test2| 1.0f0 30942626)))";
        TransientDataTestUtil.addNewMapping(duplicateMappingWithGeoStr);

        //6. Make a get-ad-data request for url using geo
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr, null);

        //7. Make a get-ad-data request for url using geo
        String tSpecGetGeoStr2 = "(get-ad-data :zip-code \"99999\" :store-ID \"919191\")";
        TransientDataTestUtil.validateOSpecReturnedforGetAdDataRequest(tSpecGetGeoStr2, tSpecName);

    }

}
