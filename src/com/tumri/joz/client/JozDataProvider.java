/*
 * ListingsQueryHandler.java
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
package com.tumri.joz.client;

import java.util.ArrayList;
import java.util.List;

import com.tumri.cma.domain.AdPod;
import com.tumri.cma.domain.Campaign;
import com.tumri.cma.domain.GeoAdPodMapping;
import com.tumri.cma.domain.Geocode;
import com.tumri.cma.domain.Location;
import com.tumri.cma.domain.LocationAdPodMapping;
import com.tumri.cma.domain.OSpec;
import com.tumri.cma.domain.Recipe;
import com.tumri.cma.domain.TSpec;
import com.tumri.cma.domain.ThemeAdPodMapping;
import com.tumri.cma.domain.Url;
import com.tumri.cma.domain.UrlAdPodMapping;
import com.tumri.joz.campaign.TransientDataException;
import com.tumri.joz.server.domain.*;

/**
 * Defines all the external APIs that are supported by the Joz Client
 * @author: nipun
 * Date: Jun 16, 2008
 * Time: 11:33:21 AM
 */

public interface JozDataProvider {

    public void shutdown();

    /**
     * Perform a health check with Joz
     * @return
     * @throws JoZClientException
     */
    public boolean doHealthCheck() throws JoZClientException;

    /**
     * Get all the providers loaded into Joz. We are not calling these "Advertisers" - since this is not the
     * terminology followed within Joz.
     * 
     * @param provRequest the JozProviderRequest object
     * @return JozResponse that encapsulates the provider data
     * @throws JoZClientException 
     */
    public JozResponse getProviders(JozProviderRequest provRequest)throws JoZClientException;

    /**
     * Process the Ad Request and give back a response. This call is invoked from the iCornerStore web application
     * to get the ad results. This API replaces the get-ad-data call.
     *
     * The response will contain information about the products as a JSON String, similar to what iCS gets
     * which will contain the meta information embedded.
     *
     * @param adRequest the JozAdRequest object 
     * @return JozAdResponse that encapsulates the ad data
     * @throws JoZClientException
     */
    public JozAdResponse getAdData(JozAdRequest adRequest) throws JoZClientException;

    /**
     * Get all the merchants loaded into Joz. At present Joz does not handle merchants that are specific
     * to providers.
     * @param merchRequest the JozMerchantRequest object
     * @return JozResponse that encapsulates the merchant data
     * @throws JoZClientException 
     */
    public JozResponse getMerchants(JozMerchantRequest merchRequest) throws JoZClientException;


    /**
     * Get the global taxonomy that is loaded into Joz. This includes the Tumri and the provider taxonomies.
     * The request can contain additional details of which node of Taxonomy that will be returned, but at this point
     * the Taxonomy request will return the entire tree.
     * The response is an JozResponse that encapsulates the taxonomy data
     * @param taxonomyRequest the JozTaxonomyRequest object
     * @return JozResponse that encapsulates the taxonomy data
     * @throws JoZClientException 
     */
    public JozResponse getTaxonomy(JozTaxonomyRequest taxonomyRequest) throws JoZClientException;
    
    /**
     * Get the category, brand and merchant counts information for a given listing query
     * The response is an JozResponse that encapsulates the counts data
     * @param countRequest the JozCountRequest object 
     * @return JozResponse that encapsulates the counts data
     * @throws JoZClientException 
     */
    public JozResponse getCountData(JozCountRequest countRequest) throws JoZClientException;
    /**
     * Adds campaign to JoZ
     * @param campaign the CMA Campaign object
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    public JozResponse addCampaign(Campaign campaign) throws JoZClientException;
    
    /**
     * Deletes the campaign from JoZ
     * @param campaign the CMA Campaign object
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    public JozResponse deleteCampaign(Campaign campaign)throws JoZClientException;

    /**
     * Gets tspec results. This API is used during the listing query authoring process within TCM.
     * @param  tSpec  the TSpec for which the query is run
     * @param  pageNum the page number of the result set
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */

    public JozResponse getTSpecDetails(TSpec tSpec,int pageSize,int pageNum)throws JoZClientException;
    
    /**
     * Gets tspec results. This API is used during the listing query authoring process within TCM.
     * @param  tSpecId  the TSpec id for which the query is run
     * @param  pageNum the page number of the result set
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    public JozResponse getTSpecDetails(int tSpecId,int pageSize,int pageNum)throws JoZClientException;
    
    /**
     * Gets tspec results and counts. This API is used during the listing query authoring process within TCM.
     * @param  tSpec  the TSpec for which the query is run
     * @param  pageNum the page number of the result set
     * @return JozResponse which contains the details and count data
     * @throws JoZClientException 
     */

    public JozResponse getTSpecDetailsAndCounts(TSpec tSpec,int pageSize,int pageNum)throws JoZClientException;
    
    /**
     * Gets tspec results and counts. This API is used during the listing query authoring process within TCM.
     * @param  tSpecId  the TSpec id for which the query is run
     * @param  pageNum the page number of the result set
     * @return JozResponse which contains the details and count data
     * @throws JoZClientException 
     */
    public JozResponse getTSpecDetailsAndCounts(int tSpecId,int pageSize,int pageNum)throws JoZClientException;

    
    /**
     * Gets tspec counts. This API is used during the listing query authoring process within TCM.
     * @param  tSpec the TSpec object
     * @return JozResponse which provides the status message
     * @throws JoZClientException
     */
    public JozResponse getTSpecCounts(TSpec tSpec)throws JoZClientException;
    
    /**
     *  Gets tspec counts. This API is used during the listing query authoring process within TCM.
     * @param tSpecId
     * @return  JozResponse which provides the status message
     * @throws JoZClientException
     */
    public JozResponse getTSpecCounts(int tSpecId)throws JoZClientException;
    /**
     *  Gets all advertisers. 
     * @return  String xml response as string
     * @throws JoZClientException
     */
    public String getAllAdvertisers()throws JoZClientException;
    /**
     *  Gets campaign data for advertiser
     * @param advertiserId 
     * @return String xml response as string
     * @throws JoZClientException
     */
    public String getAdvertiserCampaignData(int advertiserId)throws JoZClientException;
    /**
     *  Gets all campaigns. 
     * @return  String xml response as string
     * @throws JoZClientException
     */
    public String getAllAdvertisersCampaignData()throws JoZClientException;
    
    /**
     * Adds adpod to JoZ
     * @param campaignId the parent campaign to the adpod
     * @param adPod  the adPod object
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    //public JozResponse addAdPod(int campaignId, AdPod adPod)throws JoZClientException;
    
    /**
     * Deletes  adpod from JoZ
     * @param  adpodId
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    //public JozResponse deleteAdPod(int adpodId)throws JoZClientException;

    /**
     * Adds tspec to JoZ
     * @param ospecId the tspec belongs to
     * @param tspec, the tSpec object
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    //public JozResponse addTSpec(int ospecId , TSpec tspec)throws JoZClientException;

    /**
     * Deletes  tspec from JoZ
     * @param  tspecId id of the tspec
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
   // public JozResponse deleteTSpec(int tspecId)throws JoZClientException;
    
    /**
     * Adds recipe to JoZ
     * @param adpodId - id of the adpod to which the recipe belongs
     * @param recipe, the recipe object to add
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    //public JozResponse addRecipe(int adpodId, Recipe recipe)throws JoZClientException;
    
    /**
     * Deletes  recipe from JoZ
     * @param  adpodid - id of the adpod
     * @param recipeId - id of the recipe
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    //public JozResponse deleteRecipe(int adpodid, int recipeId)throws JoZClientException;
  
    /**
     * Gets tspec results. This API is used during the listing query authoring process within TCM.
     * @param  
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */


    /**
     * Deletes geocode Mapping from JoZ
     * The response is an JozResponse 
     * @param adPodId id of the adpod id
     * @param geocode - geocode
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
   // public JozResponse deleteGeocodeMapping(Geocode geocode, int adPodId)throws JoZClientException;

    /**
     * Adds the url-adpod-mapping
     * Step 1: Check if OSpec exists, if not throw exception
     * Step 2: Check if url exist, if not create one.
     * Step 3: Lookup adpod id and url id for given request
     * Step 4: Add to Url-Adpod-Index within CampaignDB
     *  
     * @param urlMapping - the UrlAdPodMapping object
     * @return JozResponse which provides the status message
     * @throws JoZClientException - Gets thrown for invalid conditio
     */
    
   // public JozResponse addUrlMapping(UrlAdPodMapping urlMapping) throws JoZClientException;
    /**
     * Deletes the url-adpod-mapping
     * @param urlName - the name of the url to be deleted
     * @param adPodid - the id of the associated adPod
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    //public JozResponse deleteUrlMapping(int adPodid, String urlName) throws JoZClientException;
    /**
     * Adds the theme-adpod-mapping
     * @param themeMapping - the ThemeAdPodMapping object
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    
    //public JozResponse addThemeMapping(ThemeAdPodMapping themeMapping) throws JoZClientException;
    /**
     * Deletes the theme-adpod-mapping
     * @param themeName - the name of the theme to be deleted
     * @param adPodid - the id of the associated adPod
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    //public JozResponse deleteThemeMapping(int adPodid, String themeName) throws JoZClientException;
    /**
     * Adds the location-adpod-mapping
     * @param locMapping - the LocationAdPodMapping object
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    //public JozResponse addLocationMapping(LocationAdPodMapping locMapping)throws JoZClientException;
    /**
     * Delete the location-adpod-mapping
     * @param locationId - the id LocationAdPodMapping object
     * @param adPodid - the id of the associated adPod
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */

    //public JozResponse deleteTSpec(int tspecId)throws JoZClientException;
    
    /**
     * Adds recipe to JoZ
     * @param adpodId - id of the adpod to which the recipe belongs
     * @param recipe, the recipe object to add
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    //public JozResponse addRecipe(int adpodId, Recipe recipe)throws JoZClientException;
    
    /**
     * Deletes  recipe from JoZ
     * @param  adpodid - id of the adpod
     * @param recipeId - id of the recipe
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    //public JozResponse deleteRecipe(int adpodid, int recipeId)throws JoZClientException;


    /**
     * Deletes geocode Mapping from JoZ
     * The response is an JozResponse 
     * @param adPodId id of the adpod id
     * @param geocode - geocode
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    //public JozResponse deleteGeocodeMapping(Geocode geocode, int adPodId)throws JoZClientException;

    /**
     * Adds the url-adpod-mapping
     * Step 1: Check if OSpec exists, if not throw exception
     * Step 2: Check if url exist, if not create one.
     * Step 3: Lookup adpod id and url id for given request
     * Step 4: Add to Url-Adpod-Index within CampaignDB
     *  
     * @param urlMapping - the UrlAdPodMapping object
     * @return JozResponse which provides the status message
     * @throws JoZClientException - Gets thrown for invalid conditio
     */
    
    //public JozResponse addUrlMapping(UrlAdPodMapping urlMapping) throws JoZClientException;
    /**
     * Deletes the url-adpod-mapping
     * @param urlName - the name of the url to be deleted
     * @param adPodid - the id of the associated adPod
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
   // public JozResponse deleteUrlMapping(int adPodid, String urlName) throws JoZClientException;
    /**
     * Adds the theme-adpod-mapping
     * @param themeMapping - the ThemeAdPodMapping object
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    
    //public JozResponse addThemeMapping(ThemeAdPodMapping themeMapping) throws JoZClientException;
    /**
     * Deletes the theme-adpod-mapping
     * @param themeName - the name of the theme to be deleted
     * @param adPodid - the id of the associated adPod
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
    //public JozResponse deleteThemeMapping(int adPodid, String themeName) throws JoZClientException;
    /**
     * Adds the location-adpod-mapping
     * @param locMapping - the LocationAdPodMapping object
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
   // public JozResponse addLocationMapping(LocationAdPodMapping locMapping)throws JoZClientException;
    /**
     * Delete the location-adpod-mapping
     * @param locationId - the id LocationAdPodMapping object
     * @param adPodid - the id of the associated adPod
     * @return JozResponse which provides the status message
     * @throws JoZClientException 
     */
   // public JozResponse deleteThemeMapping(int adPodid, int locationId)throws JoZClientException;

    /**
     * Adds the geocode-adpod-mapping
     * @param geoObj- the Geocode object
     * @param adPodId adpod id associated with the geocode
     * @return JozResponse which provides the status message
     * @throws JoZClientException - Gets thrown for invalid condition
     */
    //public JozResponse addGeoMapping(Geocode geoObj, int adPodId) throws JoZClientException;
    
    /**
     * Delete the geocode-adpod-mapping
     * @param geoObj- the Geocode object
     * @param adPodId adpod id associated with the geocode
     * @return JozResponse which provides the status message
     * @throws JoZClientException - Gets thrown for invalid condition
     */
    //public JozResponse deleteGeocodeMapping(int adPodId, Geocode geoObj) throws JoZClientException;

    /**
     * Adds the geocode-adpod-mapping
     * @param geoObj- the Geocode object
     * @param adPodId adpod id associated with the geocode
     * @return JozResponse which provides the status message
     * @throws JoZClientException - Gets thrown for invalid condition
     */
   // public JozResponse addGeoMapping(Geocode geoObj, int adPodId) throws JoZClientException;
    
    /**
     * Delete the geocode-adpod-mapping
     * @param geoObj- the Geocode object
     * @param adPodId adpod id associated with the geocode
     * @return JozResponse which provides the status message
     * @throws JoZClientException - Gets thrown for invalid condition
     */
   // public JozResponse deleteGeocodeMapping(int adPodId, Geocode geoObj) throws JoZClientException;

}
