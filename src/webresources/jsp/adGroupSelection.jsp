<%@ page language="java" import="com.tumri.cma.domain.*" %>
<%@ page import="com.tumri.joz.campaign.CampaignDB" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Joz Console : AdGroup Selection</title>

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
	</script>
</head>
<body>
<jsp:include page="header.jsp"/>
<div id="links" style="text-align: right">
    <a href="/joz/console?mode=ad">get-ad-data</a>
    <a href="/joz/console">home</a>
</div>

<%
	//Get AdGroup from provided CampaignId and AdGroupId
	CampaignDB campaignDB=CampaignDB.getInstance();
	String CampIdandAdGroupId = request.getParameter("selAdGroup");
	if (CampIdandAdGroupId == null){
		CampIdandAdGroupId = (String)session.getAttribute("selAdGroup");
	} else {
		session.setAttribute("selAdGroup", CampIdandAdGroupId);
	}
	StringTokenizer tokenizer = new StringTokenizer(CampIdandAdGroupId);

	String CampaignId = tokenizer.nextToken();
	String AdGroupId = tokenizer.nextToken();
	Campaign myCampaign = campaignDB.getCampaign(Integer.parseInt(CampaignId));
	AdGroup myAdGroup = myCampaign.getAdGroup(Integer.parseInt(AdGroupId));

	List<AdPod> adPods = myAdGroup.getAdPods();
	if(adPods == null){
		adPods = new ArrayList<AdPod>();
	}

%>
<div>
	AdGroup Information
</div>
<br>
<br>
<div>
		<strong>AdGroup: Campaign Id</strong> =  <%=myCampaign.getId()%> <br>
</div>
<br>
<div>
	<%
		out.print("<strong>AdGroup: Campaign Name</strong> = " + myCampaign.getName() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong> AdGroup: Id</strong> = " + myAdGroup.getId() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong> AdGroup: Name</strong> = " + myAdGroup.getName() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong> AdGroup: Source</strong> = " + myAdGroup.getSource() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>AdGroup: Owner Id</strong> = " + myAdGroup.getOwnerId() + "<br>");
	%>
</div>
<br>
<div>
	<%
		List<Url> urls = myAdGroup.getAdGroupUrls();
		if (urls == null){
			urls = new ArrayList<Url>();
		}
		out.print("<strong> Total Number of Associated URLs with this AdGroup</strong> = " + urls.size() + "<br>");
		out.print("&nbsp &nbsp &nbsp &nbsp <strong> URLs: Name, ID </strong> <br>");
		for(int i = 0; i < urls.size(); i++){
			out.print("&nbsp &nbsp &nbsp &nbsp " + urls.get(i).getName() + ", " + urls.get(i).getId() + "<br>");
		}

	%>
</div>
<br>
<div>
	<form id="AdPodSelForm" action="/joz/jsp/adPodSelection.jsp" method="post">
		<div>
			<strong>Total Number of AdPods for this AdGroup</strong> = <%=adPods.size()%>
		</div>
		<br>
		<div>
			<strong>Select AdPod (Name, Id)</strong> (<%=adPods.size()%> AdPod(s)):
		</div>
		<select id="AdPodList">
			<%
				for(int i=0;i<adPods.size();i++) {
					if(adPods.get(i) != null) {
						out.print("<option value=\""+adPods.get(i).getId()+"\">"+adPods.get(i).getName()+ ", " +adPods.get(i).getId()+ "</option>");
					}

				}
			%>
		</select>
		<input type="hidden"  name="selAdPod"  id="selAdPod" value=""/>
		<input type="button" value="Get AdPod" onClick="javascript:submitAdPodForm()"/>
	</form>
</div>
<br>


</body>
</html>
