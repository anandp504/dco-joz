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
	<title>Joz Console : Campaign Information</title>

	<style>
		.floatingDiv { position:absolute; border: solid 1px blue; display:none; width:325px; height:300px; }
	</style>

	<script type="text/javascript">
		function submitAdPodForm() {
			var obj=document.getElementById("AdPodList");
			var selTspec=document.getElementById("selAdPod");
			selTspec.value=obj.options[obj.selectedIndex].value;
			var form=document.getElementById("AdPodSelForm");
			form.submit();
		};
		function submitAdGroupForm() {
			var obj=document.getElementById("AdGroupList");
			var selTspec=document.getElementById("selAdGroup");
			selTspec.value=obj.options[obj.selectedIndex].value;
			var form=document.getElementById("AdGroupSelForm");
			form.submit();
		};
	</script>
</head>
<body>
<jsp:include page="header.jsp"/>
<div id="links" style="text-align: right">
    <a href="/joz/console?mode=ad">get-ad-data</a>
    <a href="/joz/console">home</a>
</div>

<%
	//get Campaign from supplied campaignID and extract a List of AdGroups and AdPods from it
	CampaignDB campaignDB=CampaignDB.getInstance();
	String campaignName = request.getParameter("selCampaign");

	if (campaignName == null){
		campaignName = (String)session.getAttribute("selCampaign");
	} else {
		session.setAttribute("selCampaign", campaignName);
	}

	Campaign myCampaign = campaignDB.getCampaign(Integer.parseInt(campaignName));
	List<AdGroup> adGroups = myCampaign.getAdGroups();
	List<AdPod> adPods = myCampaign.getAdpods();
	if(adGroups == null){
		adGroups = new ArrayList<AdGroup>();
	}
	if(adPods == null){
		adPods = new ArrayList<AdPod>();
	}

%>
<div>
	Campaign Information
</div>
<br>
<br>
<div>
	<%
		out.print("<strong> Campaign: Id </strong> = " + myCampaign.getId() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong> Campaign: Name </strong> = " + myCampaign.getName() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong> Campaign: Owner Id </strong> = " + myCampaign.getOwnerId() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong> Campaign: Client Id </strong> = " + myCampaign.getClientId() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong> Campaign: Client Name </strong> = " + myCampaign.getClientName() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong> Campaign: Source </strong> = " + myCampaign.getSource() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong> Campaign: Budget Allocated </strong> = " + myCampaign.getBudgetAllocated() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong> Campaign: Budget Consumed </strong> = " + myCampaign.getBudgetConsumed() + "<br>");
	%>
</div>
<br>
<div>
	<%
		List<GeoCampaignMapping> geo = myCampaign.getCampaignGeoMappings();
		if (geo == null){
			geo = new ArrayList<GeoCampaignMapping>();
		}
		out.print("<strong> Total Number of Geo-Campaign Mappings</strong> = " + geo.size() + "<br>");
		if(geo.size() > 0){
			out.print("&nbsp &nbsp &nbsp &nbsp <strong> Geo-Campaign Mapping: ID </strong> <br>");
			for(int i = 0; i < geo.size(); i++){
				out.print("&nbsp &nbsp &nbsp &nbsp " + geo.get(i).getId() + "<br>");
			}
		}
	%>
</div>
<br>
<div>
	<%
		List<UrlCampaignMapping> urls = myCampaign.getCampaignUrls();
		if (urls == null){
			urls = new ArrayList<UrlCampaignMapping>();
		}
		out.print("<strong> Total Number of Associated URLs with this Campaign</strong> = " + urls.size() + "<br>");
		if(urls.size() > 0){
			out.print("&nbsp &nbsp &nbsp &nbsp <strong> URLs: Name, ID </strong> <br>");
			for(int i = 0; i < urls.size(); i++){
				if(urls.get(i) != null){
					out.print("&nbsp &nbsp &nbsp &nbsp " + urls.get(i).getName() + ", " + urls.get(i).getId() + "<br>");
				}
			}
		}
	%>
</div>
<br>
<div>
	<form id="AdPodSelForm" action="/joz/jsp/adPodSelection.jsp" method="post">
		<div>
			<strong> Select AdPod</strong> (<%=adPods.size()%> AdPod(s)):
		</div>
		<select id="AdPodList">
			<%
				String display = "";
				if (adPods.size() == 0){
					display = "DISABLED";  //if there are no adpods don't all button to be clicked
				}
				for(int i=0;i<adPods.size();i++) {
					if(adPods.get(i) != null){
						out.print("<option value=\""+adPods.get(i).getId()+"\">"+adPods.get(i).getName()+"</option>");
					}
				}
			%>
		</select>
		<input type="hidden"  name="selAdPod"  id="selAdPod" value=""/>
		<input type="button" value="Get AdPod" onClick="javascript:submitAdPodForm()" <%=" " + display%>>
	</form>
</div>
<br>
<div>
	<form id="AdGroupSelForm" action="/joz/jsp/adGroupSelection.jsp" method="post">
		<div>
			<strong>Select AdGroup (Name, Id)</strong> (<%=adGroups.size()%> AdGroup(s)):
		</div>
		<select id="AdGroupList">
			<%
				for(int i=0;i<adGroups.size();i++) {
					if(adGroups.get(i) != null){
						out.print("<option value=\""+myCampaign.getId() + " " + adGroups.get(i).getId()+"\">"+adGroups.get(i).getName() + ", " + adGroups.get(i).getId() + "</option>");
					}
				}
			%>
		</select>
		<input type="hidden"  name="selAdGroup"  id="selAdGroup" value=""/>
		<input type="button" value="Get AdGroup" onClick="javascript:submitAdGroupForm()"/>
	</form>
</div>
<br>


</body>
</html>
