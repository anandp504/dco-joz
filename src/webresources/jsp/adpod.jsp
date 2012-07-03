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
	  <title>Joz Console : Adpods</title>

	  <style>
	    .floatingDiv { position:absolute; border: solid 1px blue; display:none; width:325px; height:300px; }
	  </style>

	  <script>
	  function displayProductContent(pid, dcn, desc, mid, brand, ot, ccode, price, dprice, sp, provider, name) {

	  };

	  function hideProductContent() {

	  };

	  function selAllAttr(sel) {

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
	String campaignName = request.getParameter("selCampaign");
	if (campaignName == null)
		campaignName = (String)session.getAttribute("selCampaign");
	else
		session.setAttribute("selCampaign", campaignName);

	Campaign myCampaign = campaignDB.getCampaign(Integer.parseInt(campaignName));
	List<AdPod> adPods = myCampaign.getAdpods();
	if(adPods == null){
		adPods = new ArrayList<AdPod>();
	}

	%>
	  <br>
	<div>
		CampaignName = <%=myCampaign.getName()%>
	</div>
	  <br>
	<div>
		total number of AdPods = <%=adPods.size()%>
	</div>
    <br>
	<div>
	  <form id="AdPodSelForm" action="/joz/jsp/products.jsp" method="post">
		  Select AdPod:
		  <select id="AdPodList">
		       <%
			    for(int i=0;i<adPods.size();i++) {
					out.print("<option value=\""+i+"\">"+adPods.get(i).getName()+"</option>");
				}
			  %>
		  </select>
		  <input type="hidden"  name="selAdPod"  id="selAdPod" value=""/>
		  <input type="button" value="Get Products" onClick="javascript:submitForm()"/>
	  </form>
	</div>
    <br>


  </body>
</html>
