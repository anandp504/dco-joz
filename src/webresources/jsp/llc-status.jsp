<%@ page import="com.tumri.joz.utils.AppProperties" %>
<%@ page import="com.tumri.utils.nio.NioSocketChannelPool" %>
<%@ page import="com.tumri.utils.tcp.client.TcpSocketConnectionPool" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Joz Console : LLC Status</title>
</head>
<body>
<%
        HashMap<String, Integer> connMap = null;
        try {
            if (AppProperties.getInstance().isNioEnabled()) {
                connMap = NioSocketChannelPool.getInstance().getInfo();
            } else {
                connMap = TcpSocketConnectionPool.getInstance().getInfo();
            }
        } catch (Exception e) {
            //
        }
    %>
  	<jsp:include page="header.jsp"/>
	<div id="homelink" style="text-align: right">
		<a href="http://<%=request.getServerName()%>:<%=request.getServerPort()%>/joz/jsp/console.jsp">home</a>
	</div>
	<br>
    <div id="links">
		  <strong>LLC Status</strong>
	</div>
	<br>
	<div>
		<table>
            <tr><th><strong>IP ADDRESS</strong></th><th><strong>COUNT</strong></th></tr>
        <% if (connMap == null || connMap.isEmpty()) { %>
             <tr><td>LLC not configured properly, please check if LLS communication is enabled.</td></tr>
         <%} else {
             Iterator iter = connMap.keySet().iterator();
             while (iter.hasNext()) {
                 String ipAddress = (String)iter.next();
                 Integer count = connMap.get(ipAddress);
         %>
                <tr><td><%=ipAddress%></td><td><%=count%></td></tr>
         <%
             }
         %>
              </table>
           <%
             }
             %>
	</div>
    <p class="label">
<a href="http://<%=request.getServerName()%>:<%=request.getServerPort()%>/joz/refresh?type=socket&jspMode=true"
   onclick="return confirm('This will force a reset of socket connections. Do you want to continue?');">Reset Socket Pool</a>
    </p>


  </body>
</html>
