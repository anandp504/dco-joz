<%@ page language="java" %>
<%@ page language="java" %>
<%@ page language="java" %>
<%@ page language="java" %>
<%@ page language="java" import="com.tumri.joz.campaign.CampaignDB" %>
<%@ page language="java" import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="com.tumri.cma.domain.*" %>
<%@ page language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Joz Console : Experience Information</title>

	<style>
		.floatingDiv { position:absolute; border: solid 1px blue; display:none; width:325px; height:300px; }
	</style>

	<script  type="text/javascript">
		function submitCAMForm() {
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
	String expId = request.getParameter("selExp");
	if (expId == null){
		expId = (String)session.getAttribute("selExp");
	} else {
		session.setAttribute("selExp", expId);
	}

	Experience myExp = campaignDB.getExperience(Integer.parseInt(expId));
	CAM theCAM = myExp.getCam();
	List<ExperienceTSpecInfo> offers = myExp.getOfferLists();
    if(offers == null){
		offers = new ArrayList<ExperienceTSpecInfo>();
	}

%>
<div>
	Experience Information
</div>
<br>
<div>
	<%
		out.print("<strong>Experience: Id</strong> = " + myExp.getId() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>Experience: Name</strong> = " + myExp.getName() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>Experience: AdType</strong> = " + myExp.getAdType() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>Experience: TemplateName</strong> = " + myExp.getTemplateName() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>Experience: Design</strong> = " + myExp.getDesign() + "<br>");
	%>
</div>
<br>
<div>
	<%
		out.print("<strong>Experience: UIProperties</strong> : ");
        List<UIProperty> props = myExp.getProperties();
        if (props != null) {
            for (UIProperty prop : props) {
                String name = prop.getName();
                String value = prop.getValue();
                if (name != null && !name.equals("") && value != null && !value.equals("")) {
                    out.print(name + " = " + value + ",");
                }
                out.print("<br>");
            }
        }

	%>
</div>
<br>
<div>
	<%
		out.print("<strong>CAM Data: </strong>" + "<br>");
        CAMDimension[] dims = theCAM.getCamDimensions();
        out.println("<u>CAM Dimensions:</u><br>");
        for (CAMDimension dim: dims) {
           out.print(dim.getName() + " ==> ");
           List<CAMValue> values = dim.getCamValues();
           for (CAMValue v: values) {
               out.print(v.getIndex() + "=" + v.getValue() + ",");
           }
           out.println("<br>");
        }
        out.println("<u>CAM Inclusions:</u><br>");

        List<String> inclusions = theCAM.getInclusions();
        for (String i : inclusions) {
            out.println(i + ",");
        }

        out.print("<br>");

        out.println("<u>CAM Exclusions:</u><br>");

        List<String> exclusions = theCAM.getExclusions();
        for (String i : exclusions) {
            out.println(i + ",");
        }

        out.print("<br>");

	%>
</div>
<br>

</body>
</html>
