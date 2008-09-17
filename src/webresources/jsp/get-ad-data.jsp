<%@ page language="java" import="com.tumri.cma.domain.*" %>
<%@ page language="java" import="com.tumri.content.ContentProvider" %>
<%@ page language="java" import="com.tumri.content.ContentProviderFactory" %>
<%@ page import="com.tumri.content.data.Category" %>
<%@ page import="com.tumri.content.data.ContentProviderStatus" %>
<%@ page import="com.tumri.joz.campaign.AdPodHandle" %>
<%@ page import="com.tumri.joz.campaign.CampaignDB" %>
<%@ page import="com.tumri.joz.index.AtomicAdpodIndex" %>
<%@ page import="com.tumri.joz.jozMain.AdDataRequest" %>
<%@ page import="com.tumri.joz.jozMain.Features" %>
<%@ page import="com.tumri.joz.jozMain.ListingProviderFactory" %>
<%@ page import="com.tumri.joz.jozMain.MerchantDB" %>
<%@ page import="com.tumri.joz.monitor.*" %>
<%@ page import="com.tumri.joz.products.JOZTaxonomy" %>
<%@ page import="com.tumri.joz.products.ProductDB" %>
<%@ page import="com.tumri.joz.targeting.TargetingRequestProcessor" %>
<%@ page import="com.tumri.joz.utils.AppProperties" %>
<%@ page import="com.tumri.joz.utils.IndexDebugUtils" %>
<%@ page import="com.tumri.utils.sexp.Sexp" %>
<%@ page import="com.tumri.utils.sexp.SexpReader" %>
<%@ page import="java.io.Reader" %>
<%@ page import="java.io.StringReader" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Joz Console : Get-Ad-Data</title>
    <script type="text/javascript">
        function submitTSpecForm() {
            var obj = document.getElementById("TSpecList");
            var selTSpec = document.getElementById("selTSpec");
            selTSpec.value = obj.options[obj.selectedIndex].value;
            var form = document.getElementById("TSpecSelForm");
            form.submit();
        }
        ;
        function submitOSpecForm() {
            var obj = document.getElementById("OSpecList");
            var selTspec = document.getElementById("selOSpec");
            selTspec.value = obj.options[obj.selectedIndex].value;
            var form = document.getElementById("OSpecSelForm");
            form.submit();
        }
        ;
        function submitCampaignForm() {
            var obj = document.getElementById("CampaignList");
            var selTspec = document.getElementById("selCampaign");
            selTspec.value = obj.options[obj.selectedIndex].value;
            var form = document.getElementById("CampaignSelForm");
            form.submit();
        }
        ;
        function submitAdPodForm() {
            var obj = document.getElementById("AdPodList");
            var selTspec = document.getElementById("selAdPod");
            selTspec.value = obj.options[obj.selectedIndex].value;
            var form = document.getElementById("AdPodSelForm");
            form.submit();
        }
        ;
        function submitRecipeForm() {
            var obj = document.getElementById("RecipeList");
            var selTspec = document.getElementById("selRecipe");
            selTspec.value = obj.options[obj.selectedIndex].value;
            var form = document.getElementById("RecipeSelForm");
            form.submit();
        }
        ;
    </script>
</head>
<body>
<%
	/*
	Goal of this code is to get ArrayLists of Campaigns, TSpecs, AdPods and Recipes.
	We create a CMAFactory which in turn gives us a CampaignDeltaProvider.
	The CampaignDeltaProvider gives us the ability to obtain Iterators for:
	Campaigns, TSpecs, AdPods and Recipes.
	We then create and populate an ArrayList for each of these Iterators.

	For both AdPods and Tspecs it is necessary to create a HashMap because we wish to display
	the names of these in Alphabetical order but we need to know their respective Ids to forward
	to the desired *.jsp. 
	*/
	CampaignDB campaignDB=CampaignDB.getInstance();

	ArrayList<AdPod> myAdPods = campaignDB.getAdPods();
	ArrayList<Campaign> myCampaigns = campaignDB.getCampaigns();
	ArrayList<Recipe> myRecipes = campaignDB.getRecipes();
	ArrayList<TSpec> myTSpecs = campaignDB.getTSpecs();

	List<String> tSpecNames = new ArrayList<String>();
	HashMap tSpecHash = new HashMap();
	for(int i = 0; i < myTSpecs.size(); i++){
		TSpec tempTSpec = myTSpecs.get(i);
		if(tempTSpec != null){
			tSpecNames.add(tempTSpec.getName()!=null?tempTSpec.getName().trim():String.valueOf(tempTSpec.getId()));
			tSpecHash.put(tempTSpec.getName()!=null?tempTSpec.getName().trim():String.valueOf(tempTSpec.getId()), tempTSpec);
		}

	}

	List<String> adPodNames = new ArrayList<String>();
	HashMap adPodHash = new HashMap();
	for(int i=0;i<myAdPods.size();i++) {
        AdPod tempAdPod = myAdPods.get(i);
        if (tempAdPod!=null) {
			adPodNames.add(tempAdPod.getName()!=null?tempAdPod.getName().trim():String.valueOf(tempAdPod.getId()));
			adPodHash.put(tempAdPod.getName()!=null?tempAdPod.getName().trim():String.valueOf(tempAdPod.getId()), tempAdPod);
        }
    }

	List<String> campaignNames = new ArrayList<String>();
	HashMap campaignHash = new HashMap();
	for(int i=0;i<myCampaigns.size();i++) {
		Campaign tempCampaign = myCampaigns.get(i);
		if(tempCampaign!=null){
			campaignNames.add(tempCampaign.getName()!=null?tempCampaign.getName().trim():String.valueOf(tempCampaign.getId()));
			campaignHash.put(tempCampaign.getName()!=null?tempCampaign.getName().trim():String.valueOf(tempCampaign.getId()), tempCampaign);
		}
	}

	List<String> recipeNames = new ArrayList<String>();
	HashMap recipeHash = new HashMap();
	for(int i=0;i<myRecipes.size();i++) {
		Recipe tempRecipe = myRecipes.get(i);
		if(tempRecipe != null){
			recipeNames.add(tempRecipe.getName()!=null?tempRecipe.getName().trim():String.valueOf(tempRecipe.getId()));
			recipeHash.put(tempRecipe.getName()!=null?tempRecipe.getName().trim():String.valueOf(tempRecipe.getId()), tempRecipe);
		}
	}

	Collections.sort(tSpecNames);
	Collections.sort(adPodNames);
	Collections.sort(campaignNames);
	Collections.sort(recipeNames);

%>

<jsp:include page="header.jsp"/>
<div id="homelink" style="text-align: right">
	<a href="/joz/console">Home</a>
</div>
<div id="links">
	<strong>Get-Ad-Data</strong>
</div>
<br>
<div>
	<form id="CampaignSelForm" action="/joz/jsp/campaignSelection.jsp" method="post">
		<strong>Select Campaign:</strong> (<%=campaignNames.size()%> Campaigns <i>--sorted by Campaign Name or Id</i>)
		<br>
		&nbsp; &nbsp; &nbsp; &nbsp;
		<select id="CampaignList">
			<%
				for(int i=0;i<campaignNames.size();i++) {
					out.print("<option value=\""+((Campaign)campaignHash.get(campaignNames.get(i))).getId()+"\">"+campaignNames.get(i) + "</option>");
				}
			%>
		</select>
		<input type="hidden"  name="selCampaign"  id="selCampaign" value=""/>
		<input type="button" value="Get Campaign" onClick= "submitCampaignForm()"/>
	</form>
</div>
<br>
<div>
	<form id="AdPodSelForm" action="/joz/jsp/adPodSelection.jsp" method="post">
		<strong>Select AdPod:</strong> (<%=adPodNames.size()%> AdPods <i>--sorted by AdPod Name or Id</i>)
		<br>
		&nbsp; &nbsp; &nbsp; &nbsp;
		<select id="AdPodList">
			<%
				for(int i=0;i<adPodNames.size();i++) {
					out.print("<option value=\""+((AdPod)adPodHash.get(adPodNames.get(i))).getId()+"\">"+adPodNames.get(i)+"</option>");
				}
			%>
		</select>
		<input type="hidden"  name="selAdPod"  id="selAdPod" value=""/>
		<input type="button" value="Get AdPod" onClick= "submitAdPodForm()"/>
	</form>
</div>
<br>
<div>
	<form id="RecipeSelForm" action="/joz/jsp/recipeSelection.jsp" method="post">
		<strong>Select Recipe:</strong> (<%=recipeNames.size()%> Recipes <i>--sorted by Recipe Name or Id</i>)
		<br>
		&nbsp; &nbsp; &nbsp; &nbsp;
		<select id="RecipeList">
			<%
				for(int i=0;i<recipeNames.size();i++) {
					out.print("<option value=\""+((Recipe)recipeHash.get(recipeNames.get(i))).getId()+"\">"+recipeNames.get(i)+"</option>");
				}
			%>
		</select>
		<input type="hidden"  name="selRecipe"  id="selRecipe" value=""/>
		<input type="button" value="Get Recipe" onClick= "submitRecipeForm()"/>
	</form>
</div>
<br>
<div>
	<form id="TSpecSelForm" action="/joz/jsp/tSpecSelection.jsp" method="post">
		<strong>Select TSpec:</strong> (<%=tSpecNames.size()%> TSpecs <i>--sorted by TSpec Name or Id</i>)
		<br>
		&nbsp; &nbsp; &nbsp; &nbsp;
		<select id="TSpecList">
			<%
				for(int i=0;i<tSpecNames.size();i++) {
					out.print("<option value=\""+((TSpec)tSpecHash.get(tSpecNames.get(i))).getId()+"\">"+tSpecNames.get(i) + "</option>");
				}
			%>
		</select>
		<input type="hidden"  name="selTSpec"  id="selTSpec" value=""/>
		<input type="button" value="Get TSpec" onClick= "submitTSpecForm()"/>
	</form>
</div>

<br>
</body>
</html>
