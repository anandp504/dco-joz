package com.tumri.joz.server.handlers;

import com.thoughtworks.xstream.XStream;
import com.tumri.cma.domain.*;
import com.tumri.joz.JoZException;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.Features;
import com.tumri.joz.products.Handle;
import com.tumri.joz.productselection.ProductSelectionRequest;
import com.tumri.joz.productselection.TSpecExecutor;
import com.tumri.joz.server.domain.*;
import com.tumri.utils.strings.StringTokenizer;
import com.tumri.utils.tcp.server.domain.QueryId;
import com.tumri.utils.tcp.server.domain.QueryInputData;
import com.tumri.utils.tcp.server.domain.QueryResponseData;
import com.tumri.utils.tcp.server.handlers.InvalidRequestException;
import com.tumri.utils.tcp.server.handlers.RequestHandler;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: scbraun
 * Date: Oct 10, 2008
 * Time: 11:27:15 AM
 * The purpose of JozQAReporting is to provide a simple API which allows an external entity to query Joz and recieve a
 * response which details successes and failures of the Recipes present within Joz. Joz will be sent a request containing
 * a list of Advertisers; if none are supplied, Joz will assume the query desires all advertisers to be tested. Joz will return information about which Recipes failed along with information about what caused the failure.
 */
public class JozQARequestHandler implements RequestHandler {


	private static Logger log = Logger.getLogger (JozQARequestHandler.class);

	public QueryResponseData query(QueryInputData input) throws InvalidRequestException {
		JozQAResponseWrapper responseWrapper = new JozQAResponseWrapper();
		doQuery((JozQARequest)input, responseWrapper);
		return responseWrapper;
	}

	public void query(QueryInputData input, QueryResponseData response) throws InvalidRequestException {
		try {
			doQuery((JozQARequest)input, (JozQAResponseWrapper)response);
		} catch (ClassCastException ex) {
			throw new InvalidRequestException("Only type JozQARequest & JozQAResponse supported.",ex);
		}
	}

	public QueryId[] getSupportedIds() {
		return  new QueryId[]{ QueryId.JOZ_QA };
	}

	public void doQuery(JozQARequest input, JozQAResponseWrapper responseWrapper) {
		try {
			processRequest(input, responseWrapper);
		} catch(JoZException e) {
			responseWrapper.addDetails(JozQAResponseWrapper.KEY_ERROR,"Joz exception on processing the request : " + e.getMessage());
			log.error("Jozexception caught",e);
		} catch (Throwable t) {
			responseWrapper.addDetails(JozQAResponseWrapper.KEY_ERROR,"Unexpected exception on processing the request : " + t.getMessage());
			log.error("Unexpected Exception caught",t);
		}
	}

	/**
	 *  For each advertisers, Joz will execute down through the campaign hierarchy (Campaign-->AdPod-->Recipe-->ListingQuery).
	 *  There are three basic states that a recipe can fall into: successful, failed, warned.
	 *  The success and failure of a Recipe is dependent upon the success and failure of their respective ListingQueries?.
	 *  Below is a description about the conditions in which Recipes and ListingQueries? will be failed and/or warned.
	 *
	 * Recipe Failures
	 * A whole Campaign can be failed, resulting in each of it's Recipes being failed, if it hasn't begun yet or is over (flightEnd and flightStart are used to determine Campaign duration).
	 * A Recipe is failed if any of its ListingQueries? are failed.
	 * Recipe Warned:
	 * A Recipe is warned if any of its ListingQueries? are warned and none of them are failed.
	 * ListingQuery Failure;
	 * A ListingQuery fails if the number of products returned by Joz is less than the number of products requested by the corresponding RecipeTSpecInfo of each Recipe.
	 * ListingQuery Warned:
	 * If its applyGeoFilter or geoEnabledFlagset is set to true.
	 * When a Recipe is warned a short description will be included within the corresponding QARecipeResponse detailing the reason for the warning. Warned TSpecs will not result in a Recipe failure and Warned Recipes will not result in the overall failure of an Advertiser.
	 * @param query JozQARequest
	 * @param responseWrapper JozQAResponseWrapper
	 * @throws com.tumri.joz.JoZException
	 */
	private void processRequest(JozQARequest query, JozQAResponseWrapper responseWrapper) throws JoZException{
		ArrayList<String> advertisers = new ArrayList<String>();
		boolean success = true;
		boolean zeroInitAdv = false;
		String advertiserNamesString = query.getValue(JozQARequest.KEY_ADVERTISERS);
		String details = "";
		HashSet<String> validAdvertisers = null;
		JozQAResponse resp = new JozQAResponse();

		//construct List of request Advertisers from comma seperated string of names
		if(advertiserNamesString != null && !"".equals(advertiserNamesString.trim())){
			StringTokenizer tokenizer = new StringTokenizer(advertiserNamesString, ',');
			advertisers = tokenizer.getTokens();
		} else {
			zeroInitAdv = true;
		}

		//do testing of recipes for request advertisers
		ArrayList<Campaign> camps = CampaignDB.getInstance().getCampaigns();
		for(Campaign camp:camps){ //for each campaign with a corrosponding clientName
			String clientName = camp.getClientName();
			if(zeroInitAdv){
				if(!advertisers.contains(clientName)){
					advertisers.add(clientName);
				}
			}
			if(advertisers.contains(clientName)){
				if(validAdvertisers == null){
					validAdvertisers = new HashSet<String>();
				}
				validAdvertisers.add(clientName);
				Date today = new Date();
				boolean failCampaign = false;
				if(camp.getFlightEnd() == null || camp.getFlightStart() == null|| camp.getFlightStart().after(today)
						|| camp.getFlightEnd().before(today)){
					failCampaign = true;
				}
				List<AdPod> adPods = camp.getAdpods();
				for(AdPod adPod: adPods){  //for each adPod within each camp
					List<Recipe> recipes = adPod.getRecipes();
					for(Recipe recipe : recipes){ //for each recipe
						success = executeRecipe(success, resp, camp, today, failCampaign, adPod, recipe);
					}
				}
			}
		}

		//construct List of invalid advertisers from diff between valid advertisers and all advertisers
		ArrayList<String> invalidAdvertisers = null;
		for(String adv: advertisers){
			if(validAdvertisers == null || !validAdvertisers.contains(adv)){
				if(invalidAdvertisers == null){
					invalidAdvertisers = new ArrayList<String>();
				}
				resp.addDetail("Invalid-Advertiser="+adv);
			}
		}

		resp.setSuccess(success);

		//construct response wrapper
		XStream xstream = new XStream();
		String xml = xstream.toXML(resp);
		responseWrapper.addDetails(JozQAResponseWrapper.KEY_QAREPORTDETAIL, xml);
	}

	/**
	 * Used by processRequest to execute the various TSpecs found within a specified Recipe.
	 * @param success boolean, overall success of all advertisers
	 * @param resp JozQAResponse, Primary interface class that returns all QA info
	 * @param camp Campaign, in which recipe is located
	 * @param today Date
	 * @param failCampaign boolean, if the entire Campaign and all sub-recipes should fail
	 * @param adPod AdPod, in which recipe is located
	 * @param recipe Recipe, all TSpec specified within <RecipeTSpecInfo> should be tested
	 * @return true for success
	 */
	private boolean executeRecipe(boolean success, JozQAResponse resp, Campaign camp, Date today, boolean failCampaign, AdPod adPod, Recipe recipe) {
		List<RecipeTSpecInfo> tSpecInfos = recipe.getTspecInfoList();
		boolean recipeFail = false;
		boolean recipeWarn = false;
		boolean allSpecs0 = true;
		boolean geoEnabled = false;
		boolean nullTSpec = false;
		String clientName = camp.getClientName();
		QARecipeResponse recipeResponse = new QARecipeResponse();
		recipeResponse.setRecipeId(recipe.getId());
		recipeResponse.setRecipeName(recipe.getName());
		//if campaign is invalid: ie not running
		if(failCampaign){
			if(camp.getFlightEnd() == null || camp.getFlightEnd().before(today)){
				recipeResponse.addDetail("Failed: Campaign Flight is over.");
			}
			if(camp.getFlightStart() == null || camp.getFlightStart().after(today)){
				recipeResponse.addDetail("Failed: Campaign Flight hasn't begun.");
			}
			resp.registerRecipeFailure(clientName, recipeResponse);
			success = false;
			return success;
		}

		if(recipe.getWeight() == 0){
			resp.registerRecipeSuccess(clientName);
			return success;
		}
		for(RecipeTSpecInfo tSpecInfo: tSpecInfos){ //for each listing query
			int numReqProducts = tSpecInfo.getNumProducts();
			int tSpecId = tSpecInfo.getTspecId();
			TSpec eTSpec = CampaignDB.getInstance().getTspec(tSpecId);
			if(eTSpec == null){
				eTSpec = new TSpec();
				eTSpec.setId(tSpecId);
				nullTSpec = true;
			}
			JozQAError error = new JozQAError(camp, adPod, recipe, eTSpec, numReqProducts);

			boolean geoEnabledTSpec = false;
			//Geo-Enabled check
			if(eTSpec.isGeoEnabledFlag() || eTSpec.isApplyGeoFilter()){
				geoEnabledTSpec = true;
				geoEnabled = true;
				error.addDetail("TSpec is geo enabled and was not processed.");
				recipeResponse.addWarnedTSpec(eTSpec);
				recipeResponse.addJozQAWarning(error);
			}
			//Check if Recipe requests 0 products for its TSpecs
			if(!(numReqProducts == 0)){
				allSpecs0 = false;
			}
			//conditions under which we do not want to execute the TSpec
			if(geoEnabledTSpec || allSpecs0 || nullTSpec){
				if(nullTSpec){
					recipeResponse.addFailedTSpec(eTSpec);
					recipeResponse.addJozQAError(error);
					error.addDetail("No TSpec with given Id found in campaignDB.");
				}
				continue;
			}
			//execute TSpec
			int numReturnedProds = getNumProducts(tSpecId, numReqProducts);
			if(numReturnedProds < numReqProducts){
				error.setNumRecieved(numReturnedProds);
				recipeResponse.addFailedTSpec(eTSpec);
				recipeResponse.addJozQAError(error);
				recipeFail = true;
			}
		}//end TSpec for
		if(allSpecs0){
			recipeWarn = true;
			recipeResponse.addDetail("Warn: Recipe has positive weight; however, all TSpecs request 0 products.");
		}
		if(geoEnabled){
			recipeWarn = true;
			recipeResponse.addDetail("Warn: Recipe has TSpecs which are Geo-Enabled and were not processed.");
		}
		if(nullTSpec){
			recipeFail = true;
			recipeResponse.addDetail("TSpec Failed: No TSpec with specified Id found.");
		}

		//add RecipeResponse according to status: failed, warned, successful.
		if(recipeFail){//set response with collected data for each recipe
			success = false;
			resp.registerRecipeFailure(clientName, recipeResponse);
		} else if(recipeWarn){
		    resp.registerRecipeWarning(clientName, recipeResponse);
		} else {
			resp.registerRecipeSuccess(clientName);
		}
		return success;
	}

	/**
	 * This method is used to test a particular TSpec Id.
	 * @param tSpecId TSpec Id that will be executed
	 * @param numProds int, The number of desired products
	 * @return
	 */
	private int getNumProducts(int tSpecId, int numProds){
		ProductSelectionRequest pr = new ProductSelectionRequest();

		pr.setPageSize(numProds);
		pr.setCurrPage(0);
		pr.setOfferType(AdDataRequest.AdOfferType.PRODUCT_LEADGEN);
		pr.setBPaginate(true);
		pr.setBRandomize(false);
		pr.setRequestKeyWords(null);
		pr.setBMineUrls(false);
		
		ArrayList<Handle> handles;
		int numReturnedProds = -1;

		try {
			handles = doProductSelection(tSpecId, pr, new Features() );
			if(handles != null){
				numReturnedProds = handles.size();
			}
		} catch(Exception ex) {
			log.error("Error getting number of prducts: "+ex.getMessage());
		}

		return numReturnedProds;
	}

	/**
	 * returns a List of Handles.  Used by getNumProducts to get products for tspecId
	 * @param tspecId int, Id of TSpec to be executed
	 * @param pr ProductSelectionRequest
	 * @param f Features
	 * @return an ArrayList of Handle Objects representing products return by executing tspecId
	 */
	private ArrayList<Handle> doProductSelection(int tspecId, ProductSelectionRequest pr, Features f) {
		TSpecExecutor qp = new TSpecExecutor(pr, f);
		return qp.processQuery(tspecId);
	}

}

