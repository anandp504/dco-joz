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
	<title>Joz Console : OSpec Information</title>

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
	String oSpecId = request.getParameter("selOSpec");
	if (oSpecId == null){
		oSpecId = (String)session.getAttribute("selOSpec");
	} else {
		session.setAttribute("selOSpec", oSpecId);
	}

	OSpec myOSpec = campaignDB.getOspec(Integer.parseInt(oSpecId));
	List<TSpec> tSpecs = myOSpec.getTspecs();
	if(tSpecs == null){
		tSpecs = new ArrayList<TSpec>();
	}

%>
<div>
	OSpec Information
</div>
<br>
<br>
<div>
	<%
		out.print("<strong> OSpec: Id</strong> = " + myOSpec.getId() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong> OSpec: Name</strong> = " + myOSpec.getName() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong> OSpec: Description</strong> = " + myOSpec.getDescription() + "<br>");
	%>
</div>
<br>
<div>
	<%
		Date creationDate = myOSpec.getCreationDate();
		if(creationDate == null){
			out.print("<strong>OSpec: Creation Date</strong> = " + creationDate + "<br>");
		} else {
			out.print("<strong>OSpec: Creation Date<strong> = " + creationDate.toString() + "<br>");
		}
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>OSpec: Source</strong> = " + myOSpec.getSource() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>OSpec: Owner Id</strong> = " + myOSpec.getOwnerId() + "<br>");
	%>
</div>
<br>
<div>
	<form id="TSpecSelForm" action="/joz/jsp/tSpecSelection.jsp" method="post">
		<div>
			<strong>Select TSpec (Name, Id)</strong> (<%=tSpecs.size()%> TSpec(s)): 
		</div>
		<select id="TSpecList">
			<%
				for(int i=0;i<tSpecs.size();i++) {
					if(tSpecs.get(i) == null){
						out.print("<option value=\""+i+"\">"+"Not a Valid TSpec"+"</option>");
					} else {
						out.print("<option value=\""+tSpecs.get(i).getId()+"\">"+tSpecs.get(i).getName()+ ", " +tSpecs.get(i).getId()+ "</option>");
					}

				}
			%>
		</select>
		<input type="hidden"  name="selTSpec"  id="selTSpec" value=""/>
		<input type="button" value="Get TSpec" onClick="javascript:submitForm()"/>
	</form>
</div>
<br>


</body>
</html>
