<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.tumri.joz.monitor.*" %>
<%@ page language="java" import="com.tumri.joz.campaign.*" %>
<%@ page language="java" import="com.tumri.content.data.Category" %>
<%@ page language="java" import="com.tumri.cma.domain.*" %>
<%@ page language="java" import="com.tumri.joz.utils.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Joz Console : Recipe Information</title>

	<style>
		.floatingDiv { position:absolute; border: solid 1px blue; display:none; width:325px; height:300px; }
	</style>

	<script type="text/javascript">
		function submitForm() {
			var obj=document.getElementById("TSpecList");
			var selTspec=document.getElementById("selTSpec");
			selTspec.value=obj.options[obj.selectedIndex].value;
			var form=document.getElementById("TSpecSelForm");
			form.submit();
		};
		function submitAdPodForm(adPodId) {
			var selTspec=document.getElementById("selAdPod");
			selTspec.value = adPodId;
			var form=document.getElementById("AdPodSelForm");
			form.submit();
		};
		function saveRecipe() {
			var perfForm=document.getElementById("SaveRecipe");
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
	CampaignDB campaignDB=CampaignDB.getInstance();
	String recipeId = request.getParameter("selRecipe");
	if (recipeId == null) {
		recipeId = (String)session.getAttribute("selRecipe");
	} else {
		session.setAttribute("selRecipe", recipeId);
	}

	Recipe myRecipe = campaignDB.getRecipe(Integer.parseInt(recipeId));
	List<RecipeTSpecInfo> recipeTSpecInfo = myRecipe.getTSpecInfo();
	if(recipeTSpecInfo == null){
		recipeTSpecInfo = new ArrayList<RecipeTSpecInfo>();
	}

%>
<div>
	Recipe Information
</div>
<br>
<form id="SaveRecipe" action="/joz/console?mode=dl&option=recipe&id=<%=myRecipe.getId()%>" method="post">
		<input type="button" value="Save Recipe" onClick="javascript:saveRecipe()"/>
</form>
<br>
<div>
	<%
		out.print("<strong>Recipe: Id</strong> = " + myRecipe.getId() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>Recipe: Name</strong> = " + myRecipe.getName() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>Recipe: Design</strong> = " + myRecipe.getDesign() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>Recipe: Description</strong> = " + myRecipe.getDescription() + "<br>");
	%>
</div>
<br>
<div>
	<%
		Date creationDate = myRecipe.getCreationDate();
		if(creationDate == null){
			out.print("<strong>Recipe: Creation Date</strong> = " + creationDate + "<br>");
		} else {
			out.print("<strong>Recipe: Creation Date</strong> = " + creationDate.toString() + "<br>");
		}
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>Recipe: Weight</strong> = " + myRecipe.getWeight() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>Recipe: Owner Id</strong> = " + myRecipe.getOwnerId() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>Recipe: AdPod Id</strong> = " + myRecipe.getAdpodId() + "<br>");
	%>
</div>
<br>
<div>
	<form id="AdPodSelForm" action="/joz/jsp/adPodSelection.jsp" method="post">
		<%
			AdPod myAdPod= campaignDB.getAdPod(myRecipe.getAdpodId());
			int adPodId = 0;
			String isDisabled = "";
			if (myAdPod != null){
				adPodId = myAdPod.getId();
			} else {
				isDisabled = "DISABLED";
			}
		%>
		<input type="hidden"  name="selAdPod"  id="selAdPod" value=""/>
		<input type="button" value="View AdPod" onClick="submitAdPodForm('<%=adPodId%>') " <%=isDisabled%>>
	</form>
</div>
<br>
<div>
	<%
		List<UIProperty> properties = myRecipe.getProperties();
		if (properties == null){
			properties = new ArrayList<UIProperty>();
		}
		out.print("<strong>Total Number of Associated UI Properties for this Recipe</strong> = " + properties.size() + "<br>");
		if(properties.size() > 0){
			out.print("<i>UI Properties (Name, ID, Description, Value):</i> <br>");
			for(int i = 0; i < properties.size(); i++){
				if(properties.get(i) != null){
					out.print("(" + properties.get(i).getName() + ", " + properties.get(i).getId() + ", " + properties.get(i).getDescription() + ", " + properties.get(i).getValue() + ")" + "<br>");
				}
			}
		}

	%>
</div>
<br>
<div>
	<form id="TSpecSelForm" action="/joz/jsp/tSpecSelection.jsp" method="post">
		<div>
			<strong>Select TSpec (Name, ID, NumProds, SlotId)</strong> (<%=recipeTSpecInfo.size()%> TSpec(s)):
		</div>
		<select id="TSpecList">
			<%
				String isTSpecDisabled = "DISABLED";
				for(int i=0;i<recipeTSpecInfo.size();i++) {
					if(recipeTSpecInfo.get(i) == null){
						out.print("<option value=\""+i+"\">"+recipeTSpecInfo.get(i)+"</option>");
					} else {
						TSpec tempTSpec = campaignDB.getTspec(recipeTSpecInfo.get(i).getTspecId());
						if(tempTSpec == null){
							out.print("<option value=\""+i+"\">"+tempTSpec+"</option>");
						} else {
							isTSpecDisabled = "";
							out.print("<option value=\""+tempTSpec.getId()+"\">"+tempTSpec.getName()+ ", " + tempTSpec.getId() + ", " + recipeTSpecInfo.get(i).getNumProducts() + ", " + recipeTSpecInfo.get(i).getSlotId() + "</option>");
						}

					}

				}
			%>
		</select>
		<input type="hidden"  name="selTSpec"  id="selTSpec" value=""/>
		<input type="button" value="Get TSpec" onClick="javascript:submitForm() " <%=isTSpecDisabled%>/>
	</form>
</div>
<br>


</body>
</html>
