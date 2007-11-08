<%@ page language="java" import="com.tumri.joz.campaign.CMAContentRefreshMonitor" %>
<%@ page language="java" import="com.tumri.joz.campaign.CMAContentProviderStatus" %>
<%@ page language="java" import="java.text.SimpleDateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
	  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	  <title>Joz Console : CMA-Refresh-Data</title>
  </head>
  <body>
	 <% 
		String success = null;
		String  datetime = null;
        CMAContentRefreshMonitor.getInstance().loadCampaignData();
        CMAContentProviderStatus status = CMAContentProviderStatus.getInstance();
        success = (status.lastRunStatus == true? "successful" : "failed");
        datetime = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS")).format(status.lastRefreshTime);
	%>
	<jsp:include page="header.jsp"/>
	<div id="homelink" style="text-align: right">
		<a href="console.jsp">home</a>
	</div>
	<br>
	<div>
		<p><%=success%>&nbsp;<a href="cma-content-status.jsp">View Details</a></p>
	</div>
	<br>
  </body>
</html>
