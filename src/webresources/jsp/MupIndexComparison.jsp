<%@ page language="java" import="java.util.List" %>
<%@ page language="java" %>
<%@ page language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
	  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	  <title>Joz Console : MupIndexComparison</title>
  </head>
  <body>
	<jsp:include page="header.jsp"/>
	<div id="homelink" style="text-align: right">
		<a href="/joz/console">home</a>
	</div>
	<br>
	<div id="links">
		<strong>MupIndexComparison</strong>
	</div>
	<br>
	<div>
		<%
			try{
				List<String> infos = (List<String>)request.getAttribute("infos");
				%><ul><%
				for(String info: infos){
					%><li><%=info%></li><%
				}
				%></ul><%
			} catch (Throwable t){
				%><%=t%><%
			}
		%>
	</div>
  </body>
</html>
