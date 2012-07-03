<%@ page language="java" import="com.tumri.content.data.ContentProviderStatus" %>
<%@ page language="java" import="com.tumri.content.ContentProviderFactory" %>
<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="java.text.SimpleDateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
	  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	  <title>Joz Console : Content Status</title>
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
  	<jsp:include page="header.jsp"/>
	<div id="homelink" style="text-align: right">
		<a href="http://<%=request.getServerName()%>:<%=request.getServerPort()%>/joz/jsp/console.jsp">home</a>
	</div>
	<br>
	<div id="links">
		  <strong>Content Status</strong>
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
<%--change productproviderstatus to use map--%>
				if (!status.mupDisabled && status.productProviderStatusMap != null) {
                    for(ProductProviderStatus productProviderStatus:status.productProviderStatusMap.values()){
                        out.print("<tr><td>");
                        out.print("MUP Status:");
                        out.print("</td> <td>");
                        String s = status.productProviderStatus.toString();
                        s=s.replaceAll("\n","<BR>");
                        out.print(s);
                        out.print("</td></tr>");
                    }
				}

				out.print("<tr><td>");
				out.print("Taxonomy enabled:");
				out.print("</td> <td>");
				out.print(status.taxonomyDisabled==true?"false":"true");
				out.print("</td></tr>");

				if (!status.taxonomyDisabled && status.taxonomyProviderStatusMap != null) {
                    for(TaxonomyProviderStatus taxonomyProviderStatus:status.taxonomyProviderStatusMap.values()){
                        out.print("<tr><td>");
                        out.print("Taxonomy Status:");
                        out.print("</td> <td>");
                        String s = status.taxonomyProviderStatus.toString();
                        s=s.replaceAll("\n","<BR>");
                        out.print(s);
                        out.print("</td></tr>");
                    }
				}

				out.print("<tr><td>");
				out.print("Merchant data enabled:");
				out.print("</td> <td>");
				out.print(status.merchantDataDisabled==true?"false":"true");
				out.print("</td></tr>");

				if (!status.merchantDataDisabled && status.merchantDataProviderStatusMap != null) {
                    for(MerchantDataProviderStatus merchantDataProviderStatus:status.merchantDataProviderStatusMap.values()){
                        out.print("<tr><td>");
                        out.print("Merchant meta data Status:");
                        out.print("</td> <td>");
                        String s = status.merchantDataProviderStatus.toString();
                        s=s.replaceAll("\n","<BR>");
                        out.print(s);
                        out.print("</td></tr>");
                    }
                }

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
