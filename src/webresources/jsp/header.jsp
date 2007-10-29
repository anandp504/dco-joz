<%@ page language="java" import="java.net.*" %>
<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="java.util.zip.*" %>
<%@ page language="java" import="com.tumri.joz.monitor.*" %>
<%
	String clientHostName=null;
	String version=null;
	try
	{
		ZipFile zf=new ZipFile("../webapps/joz.war");
		Enumeration e=zf.entries();
		String verPropFileName=null;
		ZipEntry ze=null;
		while (e.hasMoreElements()) {
			ze=(ZipEntry)e.nextElement();
			verPropFileName=ze.getName();
			if (verPropFileName.indexOf("label_joz") != -1) break;
		}
		StringTokenizer st=new StringTokenizer(verPropFileName,"_");
		version=st.nextToken();//label
		version=st.nextToken();//joz
		version=st.nextToken();//version format(MjN.MnN.MaN.Bn);
		version = version.substring(0, version.indexOf(".version.properties"));

		java.net.InetAddress inetAdd=java.net.InetAddress.getByName(request.getRemoteHost());
		clientHostName=inetAdd.getHostName();
	}
	catch(Exception e)
	{
		;//handle exception
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
