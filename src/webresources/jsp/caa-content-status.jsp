<%@ page language="java" import="com.tumri.content.ContentProviderFactory" %>
<%@ page language="java" import="com.tumri.content.data.ContentProviderStatus" %>
<%@ page language="java" import="java.text.SimpleDateFormat" %>
<%@ page import="com.tumri.utils.strings.StringTokenizer" %>
<%@ page import="java.util.ArrayList" %>
<%@ page language="java" %>
<%@ page language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Joz Console : Listing Content Status</title>
    <link rel="stylesheet" href="css/joz.css" type="text/css"/>
</head>

<body>
<%
    ContentProviderStatus status = null;
    String errorMessage = "";
    try {
        status = ContentProviderStatus.getInstance();
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
		  <strong>Listing Content Status</strong>
	</div>
	<br>
	<div>
         <% if (status == null) { %>
              Encountered internal error when displaying content status. Details : <%=errorMessage%>
         <% } else { %>
      <table class="table">
      <tr class="table_header">
          <td colspan=2>Content Data Refresh Summary</td>
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
      <tr>
          <td>Content Details</td>
          <td><a href="http://<%=request.getServerName()%>:<%=request.getServerPort()%>/joz/refresh?type=caadetails">Content Details</a></td>
      </tr>
      </table>
         <% } %>
        <br>
        <p class="label">
            <a href="http://<%=request.getServerName()%>:<%=request.getServerPort()%>/joz/refresh?type=listing&jspMode=true" onclick="return confirm('This will force a data refresh of all advertisers. Do you want to continue?');">Refresh Data</a>
        </p>
        <br>
        <% if (status.runHistory != null && status.runHistory.size() > 0) { %>
         <table class="table">
          <tr class="table_header">
            <td colspan=6>Content Refresh History</td>
          </tr>
          <tr class="table_column_header">
              <td>Publication Number</td>
              <td>Advertisers</td>
              <td>Tumri Taxonomy Version</td>
              <td>Refresh Time</td>
              <td>Status</td>
              <td>Details</td>
           </tr>
           <% for (ContentProviderStatus.ContentProviderStatusHistory hist : status.runHistory) { %>
           <tr class="table_row">
             <td><%=hist.contentPubNumber%></td>
             <td>
                 <%
                     String advDetStr = hist.getAdvDetails();
                     if (advDetStr!=null) {
                         StringTokenizer st = new StringTokenizer(advDetStr, ',');
                         ArrayList<String> advList = st.getTokens();
                         if (!advList.isEmpty()) {
                 %>
                 <table>
                 <%
                             for (String adv: advList) {
                                 %>
                     <tr><td><%=adv%></td></tr>
                                 <%
                             }
                         }
                     }
                 %>
                 </table>
             </td>
             <td><%=hist.getTumriTaxVersion()%></td>
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
