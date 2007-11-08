<%@ page language="java" import="java.io.*" %>
<%@ page language="java" import="java.net.*" %>
<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="java.util.zip.*" %>
<%@ page import="com.tumri.joz.utils.AppProperties" %>
<%
    String clientHostName = "";
    String buildVersion = "";
    String codeLabel = "";
    String releaseVersion = "";
    try {
        //Get the Joz version from the classpath
        buildVersion = AppProperties.getInstance().getJozBuildVersion();
        if (buildVersion==null) {
            buildVersion = "";
        }
        codeLabel = AppProperties.getInstance().getJozCodeLabel();
        if (codeLabel==null) {
            codeLabel = "";
        }
        releaseVersion = AppProperties.getInstance().getJozReleaseVersion();
        if (releaseVersion == null){
            releaseVersion = "";
        }

        clientHostName = request.getRemoteHost();//By default assign remote host value.
        java.net.InetAddress inetAdd = java.net.InetAddress.getByName(request.getRemoteHost());
        if (null != inetAdd) {
            clientHostName = inetAdd.getHostName();
            if (null == clientHostName) {
                clientHostName = request.getRemoteHost();
            }
        }
    }
    catch (Exception e) {
        e.printStackTrace();
    }
%>
<div id="desc">
	<strong>Joz Console</strong>
    <div>
    <table style="font-style: italic; font-size: 80%;">
        <tr>
            <td><strong>Build Version:</strong>&nbsp;<%=buildVersion%></td>
            <td><strong>Release Version:</strong>&nbsp;<%=releaseVersion%></td>
            <td><strong>Code Label:</strong>&nbsp;<%=codeLabel%></td>
        </tr>
    </table>
    </div>

	<div align="right">
	<table style="font-style: italic; font-size: 80%;">
		<tr>
			<td>Server:&nbsp;<%=request.getServerName()%></td>
			<td>&nbsp;&nbsp;&nbsp</td>
			<td>Client:&nbsp;<%=clientHostName%></td>
		</tr>
	</table>
	</div>
	<hr/>
</div>
