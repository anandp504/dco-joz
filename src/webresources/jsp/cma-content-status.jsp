<%@ page language="java" import="com.tumri.joz.campaign.CMAContentProviderStatus" %>
<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="java.text.SimpleDateFormat" %>
<%@ page import="com.tumri.joz.campaign.CMAContentRefreshMonitor" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
	  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	  <title>Joz Console : Campaign Content Status</title>
      <link rel="stylesheet" href="css/joz.css" type="text/css"/>
  </head>

  <body>
	<%
        String requestValue = request.getQueryString();
        CMAContentProviderStatus status = null;
        String errorMessage = "";
        try {
            if ((requestValue != null) && requestValue.equals("reload")) {
                CMAContentRefreshMonitor.getInstance().loadCampaignData();
            }
            status = CMAContentProviderStatus.getInstance();
        }
        catch (Exception ex) {
            errorMessage = ex.getMessage();
            ex.printStackTrace();
            status = null;
        }
        SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    %>
  	<jsp:include page="header.jsp"/>
	<div id="homelink" style="text-align: right">
		<a href="http://<%=request.getServerName()%>:<%=request.getServerPort()%>/joz/jsp/console.jsp">home</a>
	</div>
	<br>
	<div id="links">
		  <strong>Campaign Content Status</strong>
	</div>
	<br>
	<div>
         <% if (status == null) { %>
              Encountered internal error when displaying campaign content. Details : <%=errorMessage%>
         <% } else { %>
      <table class="table">
      <tr class="table_header">
          <td colspan=2>Campaign Data Refresh Summary</td>
      </tr>
      <tr>
          <td>CMA refresh interval:</td>
          <td><%=status.refreshInterval%>&nbsp;minutes</td>
      </tr>
      <tr>
          <td>Last refresh time:</td>
          <td><%=status.lastRefreshTime == -1 ? "not available" : TIME_FORMAT.format(status.lastRefreshTime)%></td>
      </tr>
      <tr>
          <td>Last run status:</td>
          <td><%=status.lastRunStatus==true?"SUCCESS":"FAILED"%></td>
      </tr>
      <tr>
          <td>Last successful refresh at:</td>
          <td><%=status.lastSuccessfulRefreshTime == -1 ? "not available" : TIME_FORMAT.format(status.lastSuccessfulRefreshTime)%></td>
      </tr>
      <tr>
          <td>Last error:</td>
          <td><%=status.lastError==null?"none":status.lastError.getMessage()%></td>
      </tr>
      <tr>
          <td>Time of last error:</td>
          <td><%=status.lastErrorRunTime == -1 ? "not available" : TIME_FORMAT.format(status.lastErrorRunTime)%></td>
      </tr>
      </table>
         <% } %>
        <br>
        <p class="label">
            <a href="http://<%=request.getServerName()%>:<%=request.getServerPort()%>/joz/jsp/cma-content-status.jsp?reload" onclick="return confirm('This will force a campaign data refresh. Do you want to continue?');">Click to force Reload</a>
        </p>
        <br>
        <% if (status.runHistory != null && status.runHistory.size() > 0) { %>
         <table class="table">
          <tr class="table_header">
            <td colspan=3>Campaign Data Refresh History</td>
          </tr>
          <tr class="table_column_header">
              <td>Refresh Time</td>
              <td>Status</td>
              <td>Details</td>
           </tr>
           <% for (CMAContentProviderStatus.CMAContentProviderStatusHistory hist : status.runHistory) { %>
           <tr class="table_row">
             <td><%=TIME_FORMAT.format(hist.refreshTime)%></td>
             <td><%=(hist.runStatus)?"SUCCESS":"FAILED"%></td>
             <td><span class="errorDetail"><%=hist.runDetailMessage%></span></td>  
           </tr>
           <% } //End for loop%>
            </table>
        <% } %>
	</div>
  </body>
</html>
