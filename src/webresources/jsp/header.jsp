<%@ page language="java" import="java.io.*" %>
<%@ page language="java" import="java.net.*" %>
<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="java.util.zip.*" %>
<%@ page import="com.tumri.joz.utils.AppProperties" %>
<%
    String clientHostName = null;
    String version = null;
    try {
        //Get the Joz version from the classpath
        version = AppProperties.getInstance().getJozBuildVersion() + "&nbsp;Code&nbsp;Label:&nbsp;"
                + AppProperties.getInstance().getJozCodeLabel();

        if (version == null || "".equals(version)) {
            ZipFile zf = null;
            String catalinaHome = System.getProperty("catalina.home");
            if (null == catalinaHome) {
                zf = new ZipFile("../webapps/joz.war");
            } else {
                zf = new ZipFile(catalinaHome + File.separator + "webapps/joz.war");
            }
            Enumeration e = zf.entries();
            String verPropFileName = null;
            ZipEntry ze = null;
            while (e.hasMoreElements()) {
                ze = (ZipEntry) e.nextElement();
                verPropFileName = ze.getName();
                if (verPropFileName.indexOf("label_joz") != -1) break;
            }
            StringTokenizer st = new StringTokenizer(verPropFileName, "_");
            version = st.nextToken();//label
            version = st.nextToken();//joz
            version = st.nextToken();//version format(MjN.MnN.MaN.Bn);
            int index = version.indexOf(".version.properties");
            if (-1 != index) {
                version = version.substring(0, index);//Remove rest of the file name.
            }
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
	<strong>Console</strong> (Joz Version:&nbsp;<%=version%>)
	<div align="right">
	<table>
		<tr>
			<td>Server:&nbsp;<%=request.getServerName()%></td>
			<td>&nbsp;&nbsp;&nbsp</td>
			<td>Client:&nbsp;<%=clientHostName%></td>
		</tr>
	</table>
	</div>
	<hr/>
</div>
