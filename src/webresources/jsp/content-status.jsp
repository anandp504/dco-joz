<%@ page language="java" import="com.tumri.content.data.ContentProviderStatus" %>
<%@ page language="java" import="com.tumri.content.ContentProviderFactory" %>
<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="java.text.SimpleDateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
	  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	  <title>Joz Console 'content-status' Page</title>
  </head>
  <body>
	 <%
		ContentProviderStatus status = null;
		try {
			status = ContentProviderFactory.getInstance().getContentProvider().getStatus();
		}
		catch (Exception ex) {
			status = null;
		}
		SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
      %>
	  <div id="desc">
		<strong>Joz Console Ver 0.1</strong>
		<hr/>
		<div id="homelink" style="text-align: right">
			<a href="console.jsp">back</a>
			<a href="console.jsp">home</a>
		</div>
	  </div>
	  <br>
	  <div id="links">
		  <strong>content status</strong>
      </div>
	  <br>
      <div>
		 <% if (status == null) {  
				out.print("Encountered internal error. No Information Available.");
			}
			else {
				out.print("<table>");
				out.print("<tr><td>");
				out.print("MUP enabled:");
				out.print("</td> <td>");
				out.print(status.mupDisabled==true?"false":"true");
				out.print("</td></tr>");
				out.print("<tr><td>");
				out.print("Taxonomy enabled:");
				out.print("</td> <td>");
				out.print(status.taxonomyDisabled==true?"false":"true");
				out.print("</td></tr>");
				out.print("<tr><td>");
				out.print("Merchant data enabled:");
				out.print("</td> <td>");
				out.print(status.merchantDataDisabled==true?"false":"true");
				out.print("</td></tr>");
				out.print("<tr><td>");
				out.print("Data refresh interval:");
				out.print("</td> <td>");
				out.print(status.refreshInterval+"&nbsp;minutes");
				out.print("</td></tr>");
				out.print("<tr><td>");
				out.print("Last refresh time:");
				out.print("</td> <td>");
				out.print(status.lastRefreshTime == -1 ? "not available" : TIME_FORMAT.format(status.lastRefreshTime));
				out.print("</td></tr>");
				out.print("<tr><td>");
				out.print("Last run status:");
				out.print("</td> <td>");
				out.print(status.lastRunStatus==true?"successful":"failed");
				out.print("</td></tr>");
				out.print("<tr><td>");
				out.print("Last successful refresh at:");
				out.print("</td> <td>");
				out.print(status.lastSuccessfulRefreshTime == -1 ? "not available" : TIME_FORMAT.format(status.lastSuccessfulRefreshTime));
				out.print("</td></tr>");
				out.print("<tr><td>");
				out.print("Cache enabled:");
				out.print("</td> <td>");
				out.print(status.cacheEnabled==true?"true":"false");
				out.print("</td></tr>");
				out.print("<tr><td>");
				out.print("Cache filename:");
				out.print("</td> <td>");
				out.print(status.cacheFilename);
				out.print("</td></tr>");
				out.print("<tr><td>");
				out.print("Auto refresh enabled:");
				out.print("</td> <td>");
				out.print(status.autoRefreshEnabled==true?"true":"false");
				out.print("</td></tr>");
				out.print("<tr><td>");
				out.print("Last error:");
				out.print("</td> <td>");
				out.print(status.lastError==null?"none":status.lastError.getMessage());
				out.print("</td></tr>");
				out.print("<tr><td>");
				out.print("Time of last error:");
				out.print("</td> <td>");
				out.print(status.lastErrorRunTime == -1 ? "not available" : TIME_FORMAT.format(status.lastErrorRunTime));
				out.print("</td></tr>");
				out.print("</table>");
			}
		 %>
      </div>
  </body>
</html>