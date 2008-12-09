<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.tumri.joz.monitor.*" %>
<%@ page language="java" import="com.tumri.joz.campaign.*" %>
<%@ page language="java" import="com.tumri.joz.products.JOZTaxonomy" %>
<%@ page language="java" import="com.tumri.content.data.Category" %>
<%@ page language="java" import="com.tumri.cma.domain.*" %>
<%@ page language="java" import="com.tumri.joz.utils.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Joz Console : Adpod Information</title>

	<style>
		.floatingDiv { position:absolute; border: solid 1px blue; display:none; width:325px; height:300px; }
	</style>

	<script  type="text/javascript">
		function submitRecipeForm() {
			var obj=document.getElementById("RecipeList");
			var selTspec=document.getElementById("selRecipe");
			selTspec.value=obj.options[obj.selectedIndex].value;
			var form=document.getElementById("RecipeSelForm");
			form.submit();
		};
		function submitOSpecForm(oSpecId) {
			var selOSpec=document.getElementById("selOSpec");
			selOSpec.value = oSpecId;
			var form=document.getElementById("OSpecSelForm");
			form.submit();
		};

		function saveAdPod() {
			var perfForm=document.getElementById("SaveAdPod");
			perfForm.submit();
		}

	</script>
</head>
<body>
<jsp:include page="header.jsp"/>
<div id="links" style="text-align: right">
    <a href="/joz/console?mode=ad">get-ad-data</a>
    <a href="/joz/console">home</a>
</div>

<%
	//get AdPod from supplied adPodId
	CampaignDB campaignDB=CampaignDB.getInstance();
	String adPodId = request.getParameter("selAdPod");
	if (adPodId == null){
		adPodId = (String)session.getAttribute("selAdPod");
	} else {
		session.setAttribute("selAdPod", adPodId);
	}

	AdPod myAdPod = campaignDB.getAdPod(Integer.parseInt(adPodId));
	List<Recipe> recipes = myAdPod.getRecipes();
	if(recipes == null){
		recipes = new ArrayList<Recipe>();
	}

%>
<div>
	AdPod Information
</div>
<br>
<form id="SaveAdPod" action="/joz/console?mode=dl&option=adpod&id=<%=myAdPod.getId()%>" method="post">
		<input type="button" value="Save AdPod" onClick="javascript:saveAdPod()"/>
</form>
<br>
<div>
	<%
		out.print("<strong>AdPod: Id</strong> = " + myAdPod.getId() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>AdPod: Name</strong> = " + myAdPod.getName() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>AdPod: AdType</strong> = " + myAdPod.getAdType() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>AdPod: AdGroup Id</strong> = " + myAdPod.getAdGroupId() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>AdPod External Variable Targets: </strong> = " + "<br>");
		List<AdPodExternalVariableTarget> targetList = myAdPod.getExternalTargetsList();
		if(targetList!= null){
			for(AdPodExternalVariableTarget t: targetList){
				if(t!=null){
					out.print("Name = " + t.getName()+"<br>");				
					List<AdPodExternalVariableInfo> info = t.getExternalVariableInfoList();
					if(info != null){
						for(AdPodExternalVariableInfo i : info){
							if(i!=null){
								out.print("&nbsp &nbsp &nbsp &nbsp Value = " + i.getValue() + "<br>");
							}
						}
					}
				}
			}
		}

	%>
</div>
<br>
<div>
	<%
		out.print("<strong>Geo-Code(<i>Area-Code, City, Country, DMA-Code, State and Zip-Code</i>): </strong><br>");
		Geocode geoCode = myAdPod.getGeocode();

			if(geoCode != null){
				List<String> areaCodes0 = geoCode.getAreaCodes();
				List<String> cities0 = geoCode.getCities();
				List<String> countries0 = geoCode.getCountries();
				List<String> dmaCodes0 = geoCode.getDmaCodes();
				List<String> states0 = geoCode.getStates();
				List<String> zipCodes0 = geoCode.getZipcodes();


				if(areaCodes0 != null) {
					out.print("Area-Codes: " + areaCodes0.size() + "<br>");
					for(int j = 0; j < areaCodes0.size(); j++){
						String code = areaCodes0.get(j);
						if(code != null && !code.trim().equalsIgnoreCase("")){
							out.print("&nbsp &nbsp &nbsp &nbsp " + j + ": " + code.trim() + "<br>");
						}
					}
				} else if (cities0 != null){
					out.print("Cities: " + cities0.size() + "<br>");
					for(int j = 0; j < cities0.size(); j++){
						String city = cities0.get(j);
						if(city != null && !city.trim().equalsIgnoreCase("")){
							out.print("&nbsp &nbsp &nbsp &nbsp " + j + ": " + city.trim() + "<br>");
						}
					}
				} else if (countries0 != null){
					out.print("Countries: " + countries0.size() + "<br>");
					for(int j = 0; j < countries0.size(); j++){
						String country = countries0.get(j);
						if(country != null && !country.trim().equalsIgnoreCase("")){
							out.print("&nbsp &nbsp &nbsp &nbsp " + j + ": " + country.trim() + "<br>");
						}
					}
				} else if (dmaCodes0 != null){
					out.print("DMA-Codes: " + dmaCodes0.size() + "<br>");
					for(int j = 0; j < dmaCodes0.size(); j++){
						String code = dmaCodes0.get(j);
						if(code != null && !code.trim().equalsIgnoreCase("")){
							out.print("&nbsp &nbsp &nbsp &nbsp " + j + ": " + code.trim() + "<br>");
						}
					}
				} else if (states0 != null){
					out.print("States: " + states0.size() + "<br>");
					for(int j = 0; j < states0.size(); j++){
						String state = states0.get(j);
						if(state != null && !state.trim().equalsIgnoreCase("")){
							out.print("&nbsp &nbsp &nbsp &nbsp " + j + ": " + state.trim() + "<br>");
						}
					}
				} else if (zipCodes0 != null){
					out.print("Zip-Codes: " + zipCodes0.size() + "<br>");
					for(int j = 0; j < zipCodes0.size(); j++){
						String code = zipCodes0.get(j);
						if(code != null && !code.trim().equalsIgnoreCase("")){
							out.print("&nbsp &nbsp &nbsp &nbsp " + j + ": " + code.trim() + "<br>");
						}
					}
				}
			}


	%>
</div>
<br>
<div>
	<%
		List<Geocode> geo = myAdPod.getGeocodes();
		if (geo == null){
			geo = new ArrayList<Geocode>();
		}
		out.print("<strong>Total Number of Geo-Codes</strong> = " + geo.size() + "<br>");
		out.print("<strong>Geo-Code(<i>Area-Code, City, Country, DMA-Code, State and Zip-Code</i>): </strong><br>");
		for(int i = 0; i < geo.size(); i++){
			Geocode geo1 = geo.get(i);
			if(geo1 != null){
				List<String> areaCodes = geo1.getAreaCodes();
				List<String> cities = geo1.getCities();
				List<String> countries = geo1.getCountries();
				List<String> dmaCodes = geo1.getDmaCodes();
				List<String> states = geo1.getStates();
				List<String> zipCodes = geo1.getZipcodes();


				if(areaCodes != null) {
					out.print("Area-Codes: " + areaCodes.size() + "<br>");
					for(int j = 0; j < areaCodes.size(); j++){
						String code = areaCodes.get(j);
						if(code != null && !code.trim().equalsIgnoreCase("")){
							out.print("&nbsp &nbsp &nbsp &nbsp " + j + ": " + code.trim() + "<br>");
						}
					}
				} else if (cities != null){
					out.print("Cities: " + cities.size() + "<br>");
					for(int j = 0; j < cities.size(); j++){
						String city = cities.get(j);
						if(city != null && !city.trim().equalsIgnoreCase("")){
							out.print("&nbsp &nbsp &nbsp &nbsp " + j + ": " + city.trim() + "<br>");
						}
					}
				} else if (countries != null){
					out.print("Countries: " + countries.size() + "<br>");
					for(int j = 0; j < countries.size(); j++){
						String country = countries.get(j);
						if(country != null && !country.trim().equalsIgnoreCase("")){
							out.print("&nbsp &nbsp &nbsp &nbsp " + j + ": " + country.trim() + "<br>");
						}
					}
				} else if (dmaCodes != null){
					out.print("DMA-Codes: " + dmaCodes.size() + "<br>");
					for(int j = 0; j < dmaCodes.size(); j++){
						String code = dmaCodes.get(j);
						if(code != null && !code.trim().equalsIgnoreCase("")){
							out.print("&nbsp &nbsp &nbsp &nbsp " + j + ": " + code.trim() + "<br>");
						}
					}
				} else if (states != null){
					out.print("States: " + states.size() + "<br>");
					for(int j = 0; j < states.size(); j++){
						String state = states.get(j);
						if(state != null && !state.trim().equalsIgnoreCase("")){
							out.print("&nbsp &nbsp &nbsp &nbsp " + j + ": " + state.trim() + "<br>");
						}
					}
				} else if (zipCodes != null){
					out.print("Zip-Codes: " + zipCodes.size() + "<br>");
					for(int j = 0; j < zipCodes.size(); j++){
						String code = zipCodes.get(j);
						if(code != null && !code.trim().equalsIgnoreCase("")){
							out.print("&nbsp &nbsp &nbsp &nbsp " + j + ": " + code.trim() + "<br>");
						}
					}
				}

			}
		}

	%>
</div>
<br>
<div>
	<%
		List<Location> locations = myAdPod.getLocations();
		if (locations == null){
			locations = new ArrayList<Location>();
		}
		out.print("<strong>Total Number of Locations</strong> = " + locations.size() + "<br>");
		out.print("<strong>locations: ExternalId, ID, Name, ClientId</strong><br>");
		for(int i = 0; i < locations.size(); i++){
			Location loc = locations.get(i);
			if(loc!=null){
				out.print("&nbsp &nbsp &nbsp &nbsp " + loc.getExternalId() + ", " + loc.getId() + ", " + loc.getName() + ", " + loc.getClientId() + "<br>");
			}
		}

	%>
</div>
<br>
<div>
	<%
		List<GeoAdPodMapping> geoMap = myAdPod.getGeoAdPodMappings();
		if (geoMap == null){
			geoMap = new ArrayList<GeoAdPodMapping>();
		}
		out.print("<strong>Total Number of Geo AdPod Mappings</strong> = " + geoMap.size() + "<br>");
		out.print("<strong>GeoAdPodMapping: ID, Type, Value </strong><br>");
		for(int i = 0; i < geoMap.size(); i++){
			GeoAdPodMapping map = geoMap.get(i);
			if(map != null){
				out.print("&nbsp &nbsp &nbsp &nbsp " + map.getId() + ", " + map.getType() + ", " + map.getGeoValue() + "<br>");
			}
		}

	%>
</div>
<br>
<div>
	<%
		List<UrlAdPodMapping> urls = myAdPod.getAdpodUrls();
		if (urls == null){
			urls = new ArrayList<UrlAdPodMapping>();
		}
		out.print("<strong>Total Number of Associated URLs with this AdPod</strong> = " + urls.size() + "<br>");
		out.print("<strong>URLs: Name, ID</strong><br>");
		for(int i = 0; i < urls.size(); i++){
			if(urls.get(i) != null){
				out.print("&nbsp &nbsp &nbsp &nbsp " + urls.get(i).getName() + ", " + urls.get(i).getId() + "<br>");
			}
		}

	%>
</div>
<br>
<div>
	<%
		out.print("<strong>AdPod: Source</strong> = " + myAdPod.getSource() + "<br>");
	%>
</div>
<br>
<div>
	<form id="OSpecSelForm" action="/joz/jsp/oSpecSelection.jsp" method="post">
		<%
			OSpec tempOSpec = myAdPod.getOspec();
			int oSpecId=-1;
			String isDisabled = "";
			if(tempOSpec == null){
				out.print("<strong>AdPod: OSpec</strong> = " + tempOSpec+ "<br>");
				isDisabled = "disabled"; //if no OSpec, disable button
			} else {
				out.print("<strong>AdPod: OSpec</strong> = " + tempOSpec.getName() + "<br>");
				oSpecId = tempOSpec.getId();
			}

		%>
		<input type="hidden"  name="selOSpec"  id="selOSpec" value=""/>
		<input type="button" value="View OSpec" onClick="submitOSpecForm('<%=oSpecId%>')" <%=" " +isDisabled%>>
	</form>
</div>
<br>
<div>
	<%
		out.print("<strong>AdPod: Owner Id</strong> = " + myAdPod.getOwnerId() + "<br>");
	%>
</div>
<br>
<div>
	<form id="RecipeSelForm" action="/joz/jsp/recipeSelection.jsp" method="post">
		<div>
			<strong>Select Recipe</strong> (<%=recipes.size()%> Recipe(s)):
		</div>
		<select id="RecipeList">
			<%
				for(int i=0;i<recipes.size();i++) {
					if(recipes.get(i) != null){
						out.print("<option value=\""+recipes.get(i).getId()+"\">"+recipes.get(i).getName()+", " +recipes.get(i).getId()+"</option>");
					}
				}
			%>
		</select>
		<input type="hidden"  name="selRecipe"  id="selRecipe" value=""/>
		<input type="button" value="Get Recipe" onClick="javascript:submitRecipeForm()"/>
	</form>
</div>
<br>


</body>
</html>
