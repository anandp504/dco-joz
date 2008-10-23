<%@ page import="com.tumri.joz.server.domain.*" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashSet" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Joz Console : JozQAReport</title>
	<script type="text/javascript">
	  function submit_eval_form() {
	  	var textArea=document.getElementById("advertisers");
		var form=document.getElementById("evalForm");
		form.submit();
	  };
  	  </script>
</head>

<body>
<jsp:include page="header.jsp"/>
<div id="links" style="text-align: right">
	<a href="/joz/console">home</a>
</div>
<br>
<%
	JozQAResponse adResp = (JozQAResponse)request.getAttribute("jozQAResp");
	JozQARequest adReq=(JozQARequest)request.getAttribute("jozQAReq");
%>
<br>
<b>JOZ QA REQUEST:</b>
<br>
<br>
	<div>
		<form id="evalForm" action="/joz/console?mode=qareport" method="post">
			<b>Enter JozQARequest:</b> Request should be in the form of a comma seperated list of advertisers/clientNames.
			If no advertisers are provided then Joz will run it's QA against all possible advertisers.<br>
			<i>(Example: Advertiser1, Advertiser2, ClientName3, ClientName4)</i><br>
			<br>
			<textarea id="advertisers" name="advertisers" style="width:100%;height:2cm" ><%=adReq.getAdvertisers()%></textarea><br>
			<input type="button" value="Go" onClick="javascript:submit_eval_form()"/>
		</form>
	</div>
	<script>
		document.getElementById("advertisers").focus();
	</script>


<br>
<br>
<b>JOZ QA RESPONSE:</b>
<br>

<h4>Overall-Response:</h4>
<%
	if(adResp!= null){
		ArrayList<QAAdvertiserResponse> advInfos= adResp.getAdvertiserInfos();
		%>
<ul>
	<li>All Recipes for All Advertisers Passed = <%=adResp.isSuccess()%></li>
	<li>Total Number of Failed Recipes = <%=adResp.getTotalNumFailedRecipes()%></li>
	<li>Total Number of Successful Recipes = <%=adResp.getTotalNumSuccessRecieps()%></li>
	<li>Total Number of Warned Recipes = <%=adResp.getTotalNumWarnRecipes()%></li>
	<li>details =
		<ul>
		<%
		for(String desc: adResp.getDetails()){
			%><li><%=desc%></li><%
		}
		%>
		</ul>
	</li>
</ul>
<table border="1" cellpadding="10">
<tr align="center">
    <th>Advertiser Name</th>
    <th>Advertiser Info</th>
    <th>Failed Recipe Info</th>
	<th>Error Info</th>
</tr>
<%
	for(QAAdvertiserResponse resp: advInfos){
		int numFailedRecipes = resp.getNumFailedRecipes();
		int numWarnedRecipes = resp.getNumWarnedRecipes();
		int numTotalRecipes = numFailedRecipes + numWarnedRecipes;
%>
<tr>
    <td valign="top" align="center" rowspan="<%=numTotalRecipes + 2%>"><%=resp.getAdvertiserName()%></td>

    <td valign="top" rowspan="<%=numTotalRecipes + 2%>">
	    <h4>Overall Advertiser Info:</h4>
	    <ul>
			<li>Complete Success = <%=resp.isCompleteSuccess()%></li>
			<li>Num Failed Recipes = <%=resp.getNumFailedRecipes()%></li>
			<li>Num Successful Recipes = <%=resp.getNumSuccessfulRecipes()%></li>
		    <li>Num Warned Recipes = <%=resp.getNumWarnedRecipes()%></li>
		</ul>
    </td>
	<td>
		Failed Recipes: <%=resp.getNumFailedRecipes()%>
	</td>
	<td>
		Failed TSpecs: <%=resp.getNumFailedTspecs()%>
	</td>
	</tr>
		<%
		HashSet<QARecipeResponse> failedRecipeResponses = resp.getFailedRecipeResponses();
		for(QARecipeResponse fRecResp: failedRecipeResponses){
		%>
	    <tr>
        <td valign="top">
			<ul>
				<li>Recipe Name = <a href="/joz/jsp/recipeSelection.jsp?selRecipe=<%=fRecResp.getRecipeId()%>"><%=fRecResp.getRecipeName()%></a></li>
				<li>Recipe Id = <a href="/joz/jsp/recipeSelection.jsp?selRecipe=<%=fRecResp.getRecipeId()%>"><%=fRecResp.getRecipeId()%></a></li>
				<li>Description =
					<ul>
					<%
					for(String desc: fRecResp.getDetails()){
						%><li><%=desc%></li><%
					}
					%>
					</ul>
				</li>
			</ul>
	     </td>
		    <td valign="top">
			    <%
				    if(fRecResp.getNumErrors() > 0){
			    %>
			    <h4>TSpec Errors: <%=fRecResp.getNumErrors()%></h4>
			<%
				} //end if around header
				HashSet<JozQAError> errors = fRecResp.getJozQAErrors();
				for(JozQAError error: errors){
				%>
					<ul>
						<li>TSpecName = <a href="/joz/jsp/tSpecSelection.jsp?selTSpec=<%=error.getTSpecId()%>"><%=error.getTSpecName()%></a></li>
						<li>TSpecId = <a href="/joz/jsp/tSpecSelection.jsp?selTSpec=<%=error.getTSpecId()%>"><%=error.getTSpecId()%></a></li>
						<li>Num Products Recieved = <%=error.getNumRecieved()%></li>
						<li>Num Products Requested = <%=error.getNumRequested()%></li>
						<li>Details: <%=error.getDetailsString()%></li>
					</ul>
				<%} //close error for%>
			    <%
				    if(fRecResp.getNumWarnings() > 0){
			    %>
			    <h4>TSpec Warnings: <%=fRecResp.getNumWarnings()%></h4>
			    <%
				    }//end if around header
			    HashSet<JozQAError> warnings = fRecResp.getJozQAWarnings();
				for(JozQAError warn: warnings){
				%>
					<ul>
						<li>TSpecName = <a href="/joz/jsp/tSpecSelection.jsp?selTSpec=<%=warn.getTSpecId()%>"><%=warn.getTSpecName()%></a></li>
						<li>TSpecId = <a href="/joz/jsp/tSpecSelection.jsp?selTSpec=<%=warn.getTSpecId()%>"><%=warn.getTSpecId()%></a></li>
						<li>Num Products Recieved = <%=warn.getNumRecieved()%></li>
						<li>Num Products Requested = <%=warn.getNumRequested()%></li>
						<li>Details: <%=warn.getDetailsString()%></li>
					</ul>
				<%} //close error for%>
			</td>
	   </tr>
    <%}//end recipe for%>

	<tr>
	<td>
		Warned Recipes: <%=resp.getNumWarnedRecipes()%>
	</td>
	<td>
		Warned TSpecs: <%=resp.getNumWarnTSpecs()%>
	</td>
	</tr>
		<%
		HashSet<QARecipeResponse> warnedRecipeResponses = resp.getWarnedRecipeResponses();
		for(QARecipeResponse wRecResp: warnedRecipeResponses){
		%>
	    <tr>
        <td valign="top">
			<ul>
				<li>Recipe Name = <a href="/joz/jsp/recipeSelection.jsp?selRecipe=<%=wRecResp.getRecipeId()%>"><%=wRecResp.getRecipeName()%></a></li>
				<li>Recipe Id = <a href="/joz/jsp/recipeSelection.jsp?selRecipe=<%=wRecResp.getRecipeId()%>"><%=wRecResp.getRecipeId()%></a></li>
				<li>Description =
					<ul>
					<%
					for(String desc: wRecResp.getDetails()){
						%><li><%=desc%></li><%
					}
					%>
					</ul>
				</li>
			</ul>
	     </td>
		    <td valign="top">
			    <%
				    if(wRecResp.getNumWarnings() > 0){
			    %>
			    <h4>TSpec Warnings: <%=wRecResp.getNumWarnings()%></h4>
			<%
				}//end if around header
				HashSet<JozQAError> warns = wRecResp.getJozQAWarnings();
				for(JozQAError warn: warns){
				%>
					<ul>
						<li>TSpecName = <a href="/joz/jsp/tSpecSelection.jsp?selTSpec=<%=warn.getTSpecId()%>"><%=warn.getTSpecName()%></a></li>
						<li>TSpecId = <a href="/joz/jsp/tSpecSelection.jsp?selTSpec=<%=warn.getTSpecId()%>"><%=warn.getTSpecId()%></a></li>
						<li>Num Products Recieved = <%=warn.getNumRecieved()%></li>
						<li>Num Products Requested = <%=warn.getNumRequested()%></li>
						<li>Details: <%=warn.getDetailsString()%></li>
					</ul>
				<%} //close error for%>
			</td>
	   </tr>

		<%}//end recipe for%>
<%} //close adv for%>
</table>
<%} //close if%>

</body>
</html>
