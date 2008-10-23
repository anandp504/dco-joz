package com.tumri.joz.client;

import com.thoughtworks.xstream.XStream;
import com.tumri.cma.domain.*;
import com.tumri.cma.persistence.xml.CampaignXMLDateConverter;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: scbraun
 * Date: Aug 22, 2008
 * Time: 9:32:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestJozCampaignCreator {
	protected List<String> campaignInfo = null;
	protected String writeFile = "campaigns.xml";
	protected String writeDir = "/tmp";
	private ArrayList<String> providers = new ArrayList<String>();
	private ArrayList<String> brands = new ArrayList<String>();
	private ArrayList<String> merchants = new ArrayList<String>();
	private ArrayList<String> adTypes = new ArrayList<String>();
	private ArrayList<String> cities = new ArrayList<String>();
	private ArrayList<String> states = new ArrayList<String>();
	private ArrayList<String> zipcodes = new ArrayList<String>();
	private ArrayList<String> areacodes = new ArrayList<String>();
	private ArrayList<String> countries = new ArrayList<String>();
	private ArrayList<String> dmacodes = new ArrayList<String>();
	private ArrayList<String> categories = new ArrayList<String>();
	Random r = new Random();

	public void makeXML() throws IOException {
		boolean append = true;
		providers.add("OVERSTOCK.COM");
		providers.add("OVERSTOCK.COM");
		providers.add("INTEL");
		providers.add("BESTBUY");
		providers.add("LENOVO");
		providers.add("GAP");
		providers.add("SEARS.COM");
		providers.add("WALMART");
		providers.add("NASCAR");
		providers.add("TOYSRUS");
		providers.add("NIKE");
		providers.add("   ");
		providers.add("\t");
		providers.add("\n");

		adTypes.add("mediumrectangle");
		adTypes.add("skyscraper");
		adTypes.add("leaderboard");
		adTypes.add("custom300x600");

		cities.add("Berkeley(CA)");
		cities.add("New York(NY)");
		cities.add("Dallas(TX)");
		cities.add("Bismark(ND)");
		cities.add("Orlando(FL)");

		states.add("CA");
		states.add("TX");
		states.add("ND");
		states.add("FL");
		states.add("NY");

		zipcodes.add("92870");
		zipcodes.add("94041");
		zipcodes.add("94702");
		zipcodes.add("12345");
		zipcodes.add("98765");
		zipcodes.add("   ");
		zipcodes.add("\t");
		zipcodes.add("\n");

		countries.add("Mexico");
		countries.add("United States");
		countries.add("Japan");
		countries.add("Canada");
		countries.add("France");
		countries.add("   ");
		countries.add("\t");
		countries.add("\n");

		areacodes.add("714");
		areacodes.add("650");
		areacodes.add("408");
		areacodes.add("415");
		areacodes.add("323");
		areacodes.add("   ");
		areacodes.add("\t");
		areacodes.add("\n");

		dmacodes.add("803");
		dmacodes.add("807");
		dmacodes.add("839");
		dmacodes.add("501");
		dmacodes.add("602");
		dmacodes.add("   ");
		dmacodes.add("\t");
		dmacodes.add("\n");

		categories.add("   ");
		categories.add("\t");
		categories.add("\n");
		readCategoryFile();

		File outFile = null;
		File outDir = new File(writeDir);

		if (!outDir.exists()) {
			boolean success = outDir.mkdirs();
		}

		outFile = new File(writeDir + "/" + writeFile);

		if (outFile.exists()) {
			outFile.delete();
		}

		FileWriter fw = null;

		XStream xstream = new XStream();
		xstream.processAnnotations(java.util.List.class);
		xstream.processAnnotations(Campaign.class);
		xstream.registerConverter(new CampaignXMLDateConverter());
		StringBuffer campaignBuff = new StringBuffer();

		//make 10 Campaigns that have themes and select products of given providers
		int i;


		StringBuffer xmlBuff = new StringBuffer();
		xmlBuff.append("<list xmlns='http://www.tumri.com/campaign' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.tumri.com/campaign campaign.xsd'>");

		xmlBuff.append(campaignBuff);
		xmlBuff.append("</list>");
		String finalXML = xmlBuff.toString();
		try{
			fw = new FileWriter(outFile, append);
			fw.write("<list xmlns='http://www.tumri.com/campaign' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.tumri.com/campaign campaign.xsd'>");
			for(i = 1; i < 11; i++){
				Campaign camp = makeCampaign("testCamp"+i, i, 1, 1, i-1, 1, false);
				String xml = xstream.toXML(camp);
				//campaignBuff.append(xml);
				fw.write(xml);
				fw.write("\n");
			}



			for(int j = i; j < 100; j++){
				int numTSpecs = r.nextInt(2) + 1;
				int numRecipes = r.nextInt(2) + 1;
				int numAdPods = r.nextInt(2) + 1;
				Campaign camp = makeCampaign("randomCamp"+j, j, numAdPods, numRecipes, j, numTSpecs, true);
				String xml = xstream.toXML(camp);
				fw.write(xml);
				fw.write("\n");
				//campaignBuff.append(xml);

			}

			fw.write("</list>");
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		} finally {
			try {
				fw.close();
			} catch(Exception e) {
				System.err.println("Error: " + e.getMessage());
			}
		}
		assert(new File(writeDir + writeFile).exists());

	}

	public Campaign makeCampaign(String cName, int cId, int numAdPods, int numRecipes, int chooseSpec, int numTSpecs, boolean random){
		Campaign newCamp = new Campaign();
		List<AdPod> adPods = new ArrayList<AdPod>();
		newCamp.setId(cId);
		newCamp.setName(cName);
		newCamp.setClientId((cId%20) + 1);
		newCamp.setClientName("TumriDesigners"+cId%20);
		long x = System.currentTimeMillis()+ 2000000000;
		x+=2000000000;
		x+=2000000000;

		Date endDate = new Date(x);
		newCamp.setFlightEnd(endDate);
		newCamp.setFlightStart(new Date(System.currentTimeMillis()));
		for(int i = 0; i < numAdPods; i++){
			if(random == true){
				numTSpecs  = numTSpecs>0?numTSpecs:1;
				int rand = r.nextInt(numTSpecs+1);
				numTSpecs = rand + 1;
				numRecipes = numRecipes>0?numRecipes:1;
				rand = r.nextInt(numRecipes);
				numRecipes = rand + 1;
			}
			AdPod adPod = makeAdPod(newCamp, newCamp.getId()*10+i, "AdPod"+cId*10+i, numRecipes, numTSpecs, chooseSpec, random);
			adPods.add(adPod);
		}
		newCamp.setAdpods(adPods);
		return newCamp;
	}

	public AdPod makeAdPod(Campaign camp, int id, String name, int numRecipes, int numTSpecs, int chooseSpec, boolean random){
		AdPod newAdPod = new AdPod();

		List<UrlAdPodMapping> adPodURLs = new ArrayList<UrlAdPodMapping>();
		List<Location> adPodLocations = new ArrayList<Location>();

		newAdPod.setId(id);
		newAdPod.setName(name);
		String adtype = getRandomAdType();
		int index = adTypes.indexOf(adtype);
		newAdPod.setAdType(adtype);
		newAdPod.setAdTypeId(index);
		OSpec ospec = makeOSpec(id*10, "OSpec"+(id*10), numTSpecs, chooseSpec, random);
		newAdPod.setOspec(ospec);
		for(int i = 0; i < numRecipes; i++){
			Recipe recipe = makeRecipe(id*10+i, ospec, adtype);
			recipe.setAdpodId(id);
			newAdPod.addRecipe(recipe);
		}
		Location loc = makeLocation("Theme"+id*10+numRecipes+1, (id*10+numRecipes+1), adtype);
//		ArrayList<Integer> adPodIds = new ArrayList<Integer>();
//		adPodIds.add(new Integer(id));
//		loc.setAdPodIds(adPodIds);
		loc.setClientId(camp.getId()%2 + 1);
		adPodLocations.add(loc);
		newAdPod.setLocations(adPodLocations);

		List<GeoAdPodMapping> geoList = new ArrayList<GeoAdPodMapping>();

		if(r.nextInt(100) < 50){
			GeoAdPodMapping geoMapping = makeGeoMapping(id*10+1, id);
			geoList.add(geoMapping);
			newAdPod.setGeoAdPodMappings(geoList);
		}
		return newAdPod;
	}

	protected GeoAdPodMapping makeGeoMapping(int id, int adPodId){
		GeoAdPodMapping tempMapping = new GeoAdPodMapping();
		int type = r.nextInt(6);
		ArrayList<String> values = new ArrayList<String>();
		if(type == 0){
			tempMapping.setAdPodId(adPodId);
			tempMapping.setType(GeoAdPodMapping.TYPE_ZIP);
			values.add(getRandomZip());
			tempMapping.setGeoValue(values);
			tempMapping.setId(id);
		} else if (type == 1){
			tempMapping.setAdPodId(adPodId);
			tempMapping.setType(GeoAdPodMapping.TYPE_CITY);
			values.add(getRandomCity());
			tempMapping.setGeoValue(values);
			tempMapping.setId(id);
		} else if (type == 2){
			tempMapping.setAdPodId(adPodId);
			values.add(getRandomState());
			tempMapping.setType(GeoAdPodMapping.TYPE_STATE);
			tempMapping.setGeoValue(values);
			tempMapping.setId(id);
		} else if (type == 3){
			tempMapping.setAdPodId(adPodId);
			values.add(getRandomCountry());
			tempMapping.setType(GeoAdPodMapping.TYPE_COUNTRY);
			tempMapping.setGeoValue(values);
			tempMapping.setId(id);
		} else if (type == 4){
			tempMapping.setAdPodId(adPodId);
			values.add(getRandomDMACode());
			tempMapping.setType(GeoAdPodMapping.TYPE_DMA);
			tempMapping.setGeoValue(values);
			tempMapping.setId(id);
		} else if (type == 5){
			tempMapping.setAdPodId(adPodId);
			values.add(getRandomAreaCode());
			tempMapping.setType(GeoAdPodMapping.TYPE_AREA);
			tempMapping.setGeoValue(values);
			tempMapping.setId(id);
		}
		return tempMapping;

	}

	protected String getRandomCity(){
		return cities.get(r.nextInt(cities.size()));
	}

	protected String getRandomState(){
		return states.get(r.nextInt(states.size()));
	}

	protected String getRandomZip(){
		return zipcodes.get(r.nextInt(zipcodes.size()));
	}

	protected String getRandomCountry(){
		return countries.get(r.nextInt(countries.size()));
	}

	protected String getRandomDMACode(){
		return dmacodes.get(r.nextInt(dmacodes.size()));
	}

	protected String getRandomAreaCode(){
		return areacodes.get(r.nextInt(areacodes.size()));
	}

	protected Location makeLocation(String name, int id, String adtype){

		File themeOutFile = null;
		String themeDir = "/tmp";
		String themeFile = "requests.txt";
		File themeOutDir = new File(themeDir);

		if (!themeOutDir.exists()) {
			boolean success = themeOutDir.mkdirs();
		}

		themeOutFile = new File(themeDir + "/" + themeFile);

		try {
			FileWriter themeFW = new FileWriter(themeOutFile, true);
			themeFW.append("theme " + name + " " + "adtype " + adtype + "\n");
			themeFW.close();
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		Location loc = new Location();
		loc.setRegion("USA");
		loc.setName(name);
		loc.setId(id);
		loc.setExternalId(id);

		return loc;
	}

	protected Recipe makeRecipe(int recipeId, OSpec ospec, String adType){
		Recipe newRecipe = new Recipe();
		newRecipe.setName("recipe"+recipeId);
		newRecipe.setId(recipeId);
		for(int i = 0; i < ospec.getTspecs().size(); i++){
			int id = ospec.getTspecs().get(i).getId();
			RecipeTSpecInfo info = makeRecipeTSpecInfo(id);
			info.setRecipeId(recipeId);
			newRecipe.addTSpecInfo(info);
		}
		UIProperty prop = makeUIProperty(recipeId*10+5, "Property"+(recipeId*10+5), "val");
		prop.setRecipeId(recipeId);
		newRecipe.addProperty(prop);
		String designName = getRandomDesign(adType);
		newRecipe.setDesign(designName);

		UIProperty design = makeUIProperty(recipeId*10+7, "design", designName);
		design.setRecipeId(recipeId);
		newRecipe.addProperty(design);

		newRecipe.setWeight(r.nextInt(100) + 1);

		return newRecipe;
	}

	protected UIProperty makeUIProperty(int id, String name, String val){
		UIProperty prop = new UIProperty();
		prop.setId(id);
		prop.setName(name);
		prop.setValue(val);
		return prop;
	}

	protected OSpec makeOSpec(int id, String name, int numTSpecs, int chooseSpec, boolean random){
		OSpec newOSpec = new OSpec();
		newOSpec.setId(id);
		newOSpec.setName(name);
		newOSpec.setTspecs(new ArrayList<TSpec>());
		for(int i = 0; i < numTSpecs; i++){
			TSpec tspec;
			if(!random){
				tspec = makeTSpec1(id*10+i, "TSpec"+(id*10+i), chooseSpec, random);
			} else {
				tspec = makeRandomTSpec(id*10+i, "TSpec"+(id*10+i), chooseSpec, random);
			}
			tspec.setOspecId(id);
			newOSpec.addTSpec(tspec);
		}
		return newOSpec;
	}

	public RecipeTSpecInfo makeRecipeTSpecInfo(int id){
		RecipeTSpecInfo newRecipeTSpecInfo = new RecipeTSpecInfo();
		newRecipeTSpecInfo.setTspecId(id);
		newRecipeTSpecInfo.setNumProducts(13);
		return newRecipeTSpecInfo;
	}

	public TSpec makeTSpec(int id, String name, int chooseSpec, boolean random){
		TSpec newTSpec = new TSpec();
		newTSpec.setId(id);
		newTSpec.setName(name);
		newTSpec.setLowPrice(0);
		newTSpec.setHighPrice(10000000);
		newTSpec.addIncludedProviders(makeProviderInfo(id, getProvider(chooseSpec % providers.size())));

		return newTSpec;
	}
	public TSpec makeTSpec0(int id, String name, int chooseSpec, boolean random){
		TSpec newTSpec = new TSpec();
		newTSpec.setId(id);
		newTSpec.setName(name);
		newTSpec.setLowPrice(0);
		newTSpec.setHighPrice(0);
		newTSpec.addIncludedProviders(makeProviderInfo(id, getProvider(chooseSpec % providers.size())));

		return newTSpec;
	}
	public TSpec makeTSpec1(int id, String name, int chooseSpec, boolean random){
		TSpec newTSpec = new TSpec();
		newTSpec.setId(id);
		newTSpec.setName(name);
		newTSpec.setLowPrice(-1);
		newTSpec.setHighPrice(-1);
		newTSpec.addIncludedProviders(makeProviderInfo(id, getProvider(chooseSpec % providers.size())));

		return newTSpec;
	}
	private TSpec makeRandomTSpec(int id, String name, int chooseSpec, boolean random){
		TSpec newTSpec = new TSpec();
		newTSpec.setId(id);
		newTSpec.setName(name);
		if(r.nextInt(1000) > 9750){
			setRandomLowPrice(newTSpec);
			setRandomHighPrice(newTSpec);
		}

		int numIProviders;
		int numEProviders;

		//numIProviders = r.nextInt(providers.size());
		numIProviders = r.nextInt(2);
		//numEProviders = r.nextInt(providers.size() - numIProviders);
		int numICategories = r.nextInt(5);

		numEProviders = 0;
		if(numIProviders > 0){

		}

		for(int i = 0; i < numIProviders; i++){
			addRandomIncludedProvider(newTSpec);
		}
		for(int i = 0; i < numEProviders; i++){
			addRandomExcludedProvider(newTSpec);
		}

		for(int i = 0; i < numICategories; i++){
			addRandomIncludedCategory(newTSpec);
		}

		return newTSpec;
	}

	private TSpec setRandomLowPrice(TSpec tspec){
		int low = r.nextInt(100);
		while(low < 20){
			low = r.nextInt(100);
		}
		tspec.setLowPrice(low);
		return tspec;
	}

	private TSpec setRandomHighPrice(TSpec tspec){
		int high = r.nextInt(2000);
		while(high<20){
			high = r.nextInt(2000);
		}
		tspec.setHighPrice(high);

		return tspec;
	}

	private TSpec addRandomIncludeMerchant(TSpec tspec){
		ArrayList<String> myMerchants = merchants;
		while(myMerchants.size()>0){
			int i = r.nextInt(myMerchants.size());
			String newMerchant = myMerchants.get(i);
			List<MerchantInfo> includedMerchants = tspec.getIncludedMerchants();
			List<MerchantInfo> excludedMerchants = tspec.getExcludedMerchants();
			if(!includedMerchants.contains(newMerchant) && !excludedMerchants.contains(newMerchant)){
				tspec.addIncludedMerchant(makeMerchantInfo(tspec.getId(),newMerchant));
				return tspec;
			} else {
				myMerchants.remove(i);
			}
		}
		return tspec;
	}

	private TSpec addRandomExcludeMerchant(TSpec tspec){
		ArrayList<String> myMerchants = merchants;
		while(myMerchants.size()>0){
			int i = r.nextInt(myMerchants.size());
			String newMerchant = myMerchants.get(i);
			List<MerchantInfo> includedMerchants = tspec.getIncludedMerchants();
			List<MerchantInfo> excludedMerchants = tspec.getExcludedMerchants();
			if(!includedMerchants.contains(newMerchant) && !excludedMerchants.contains(newMerchant)){
				tspec.addExcludedMerchant(makeMerchantInfo(tspec.getId(),newMerchant));
				return tspec;
			} else {
				myMerchants.remove(i);
			}
		}
		return tspec;
	}

	private TSpec addRandomIncludedCategory(TSpec tspec){
		ArrayList<String> myCategories = (ArrayList<String>)categories.clone();
		while(myCategories.size()>0){
			int i = r.nextInt(myCategories.size());
			String newCategory = myCategories.get(i);
			List<CategoryInfo> includedCategoryInfo = tspec.getIncludedCategories();
			ArrayList<String> includedCategories = new ArrayList<String>();
			if(includedCategoryInfo != null){
				for(int j = 0; j < includedCategoryInfo.size(); j++){
					includedCategories.add(includedCategoryInfo.get(j).getName());
				}
			}
			List<CategoryInfo> excludedCategoryInfo = tspec.getIncludedCategories();
			ArrayList<String> excludedCategories = new ArrayList<String>();
			if(excludedCategoryInfo != null){
				for(int j = 0; j < excludedCategoryInfo.size(); j++){
					excludedCategories.add(excludedCategoryInfo.get(j).getName());
				}
			}
			if(!excludedCategories.contains(newCategory) && !excludedCategories.contains(newCategory)){
				tspec.addIncludedCategories(makeCategoryInfo(tspec.getId(),newCategory));
				return tspec;
			} else {
				myCategories.remove(i);
			}
		}
		return tspec;
	}

	private TSpec addRandomIncludedProvider(TSpec tspec){
		ArrayList<String> myProviders = (ArrayList<String>)providers.clone();
		while(myProviders.size()>0){
			int i = r.nextInt(myProviders.size());
			String newProvider = myProviders.get(i);
			List<ProviderInfo> includedProvidersInfo = tspec.getIncludedProviders();
			ArrayList<String> includedProviders = new ArrayList<String>();
			if(includedProvidersInfo != null){
				for(int j = 0; j < includedProvidersInfo.size(); j++){
					includedProviders.add(includedProvidersInfo.get(j).getName());
				}
			}
			List<ProviderInfo> excludedProvidersInfo = tspec.getExcludedProviders();
			ArrayList<String> excludedProviders = new ArrayList<String>();
			if(excludedProvidersInfo != null){
				for(int j = 0; j < excludedProvidersInfo.size(); j++){
					excludedProviders.add(excludedProvidersInfo.get(j).getName());
				}
			}
			if(!includedProviders.contains(newProvider) && !excludedProviders.contains(newProvider)){
				tspec.addIncludedProviders(makeProviderInfo(tspec.getId(),newProvider));
				return tspec;
			} else {
				myProviders.remove(i);
			}
		}
		return tspec;
	}

	private TSpec addRandomExcludedProvider(TSpec tspec){
		ArrayList<String> myProviders = (ArrayList<String>)providers.clone();
		while(myProviders.size()>0){
			int i = r.nextInt(myProviders.size());
			String newProvider = myProviders.get(i);
			List<ProviderInfo> includedProvidersInfo = tspec.getIncludedProviders();
			ArrayList<String> includedProviders = new ArrayList<String>();
			if(includedProvidersInfo != null){
				for(int j = 0; j < includedProvidersInfo.size(); j++){
					includedProviders.add(includedProvidersInfo.get(j).getName());
				}
			}
			List<ProviderInfo> excludedProvidersInfo = tspec.getExcludedProviders();
			ArrayList<String> excludedProviders = new ArrayList<String>();
			if(excludedProvidersInfo != null){
				for(int j = 0; j < excludedProvidersInfo.size(); j++){
					excludedProviders.add(excludedProvidersInfo.get(j).getName());
				}
			}
			if(!includedProviders.contains(newProvider) && !excludedProviders.contains(newProvider)){
				tspec.addExcludedProviders(makeProviderInfo(tspec.getId(),newProvider));
				return tspec;
			} else {
				myProviders.remove(i);
			}
		}
		return tspec;
	}

	private TSpec addRandomIncludedBrand(TSpec tspec){
		tspec.addIncludedBrand(makeBrandInfo(tspec.getId(), brands.get(r.nextInt(brands.size()))));
		return tspec;
	}

	private TSpec addRandomExcludedBrand(TSpec tspec){
		tspec.addExcludedBrand(makeBrandInfo(tspec.getId(), brands.get(r.nextInt(brands.size()))));
		return tspec;
	}
	protected void readCategoryFile() {
		String file = "/opt/Tumri/joz/data/caa/current/data/US_Tumri-Taxonomy_4.7_Taxonomy_1.0_.utf8";
		ArrayList<String> requests;
		try {
			requests = new ArrayList<String>();
			FileInputStream inFile = new FileInputStream(file);
			InputStreamReader ir = new InputStreamReader(inFile,"UTF-8");
			BufferedReader br = new BufferedReader(ir);

			String line = null;
			boolean eof = false;
			while (!eof) {
				line = br.readLine();
				if (line == null) {
					eof = true;
					continue;
				}
				requests.add(line);
			}
			br.close();
			ir.close();
			inFile.close();

			for(int i = 0; i < requests.size(); i++){
				StringTokenizer tokenizer =  new StringTokenizer(requests.get(i));
				if(tokenizer.hasMoreTokens()){
					String category = tokenizer.nextToken();
					categories.add("GLASSVIEW." + category);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			System.exit(1);
		}


	}
	private String getProvider(int i){
		return providers.get(i % providers.size());
	}

	private String getMerchant(int i){
		return merchants.get(i % merchants.size());
	}

	private String getBrand(int i){
		return brands.get(i % brands.size());
	}

	private MerchantInfo makeMerchantInfo(int id, String name){
		MerchantInfo newInfo = new MerchantInfo();
		newInfo.setName(name);
		newInfo.setTspecId(id);
		return newInfo;
	}

	private BrandInfo makeBrandInfo(int id, String name){
		BrandInfo newInfo = new BrandInfo();
		newInfo.setName(name);
		newInfo.setTspecId(id);
		return newInfo;
	}

	private ProviderInfo makeProviderInfo(int id, String name){
		ProviderInfo newInfo = new ProviderInfo();
		newInfo.setName(name);
		newInfo.setTspecId(id);
		return newInfo;
	}

	private CategoryInfo makeCategoryInfo(int id, String name){
		CategoryInfo newInfo = new CategoryInfo();
		newInfo.setName(name);
		newInfo.setTspecId(id);
		newInfo.setDisplayName("DisplayName" + name);
		return newInfo;
	}

	private String getRandomAdType(){
		return adTypes.get(r.nextInt(adTypes.size()));
	}

	private String getRandomDesign(String adType){
		ArrayList<String> designs = new ArrayList<String>();
		if("mediumrectangle".equals(adType)){
			designs.add("brand1");
			designs.add("brand2");
			designs.add("everest2");
			designs.add("everestflash");
			designs.add("flashcontainer");
			designs.add("flashcontainer");
		} else if ("skyscraper".equals(adType)){
			designs.add("brand1");
			designs.add("brand2");
			designs.add("flashcontainer");
		} else if ("leaderboard".equals(adType)){
			designs.add("brand1");
			designs.add("brand2");
			designs.add("flashcontainer");
		} else if ("custom300x600".equals(adType)){
			designs.add("brand2");
			designs.add("flashcontainer");
		} else {
			designs.add("brand1");
			designs.add("brand2");
			designs.add("everest2");
		}
		return designs.get(r.nextInt(designs.size()));
	}

	public static void main(String[] args) {
		try {
			(new TestJozCampaignCreator()).makeXML();
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

}
