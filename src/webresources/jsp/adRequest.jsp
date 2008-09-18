<%@ page language="java" import="com.tumri.joz.server.domain.JozAdRequest" %>
<%@ page import="com.tumri.joz.server.domain.JozAdResponse" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<%@ page language="java" %>
<%@ page language="java" %>
<%@ page language="java" %>
<%@ page language="java" %>
<%@ page language="java" %>
<%@ page language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Joz Console : View Joz Request and Response</title>

	<style>
		.floatingDiv { position:absolute; border: solid 1px blue; display:none; width:325px; height:300px; }
	</style>

</head>
<body>
<jsp:include page="header.jsp"/>
<div id="links" style="text-align: right">
	<a href="/joz/console">home</a>
</div>


<%
	JozAdResponse adResp = (JozAdResponse)request.getAttribute("adResp");
	JozAdRequest adReq = (JozAdRequest)request.getAttribute("adReq");
%>

<br>
<b>JOZ AD REQUEST:</b>
<br>
<textarea id="text_eval_expr" name="text_eval_expr" style="width:100%;height:5cm"><%
	if(adReq != null){
		HashMap<String, String> requestMap = adReq.getRequestMap();
		Iterator<String> requestIter = requestMap.keySet().iterator();
		while(requestIter.hasNext()){
			String resultKey = requestIter.next();
			String resultVal = requestMap.get(resultKey);
			if(resultVal != null){
			%>:<%=resultKey%> <%=resultVal%> <%
			}//end if
		}//end while
	}//end if

%>
	</textarea>
<br>
<br>
<b>JOZ AD RESPONSE:</b>
<br>
<textarea id="text_eval_expr" name="text_eval_expr" style="width:100%;height:17cm">
<%

	if(adResp!= null){
		HashMap<String, String> resultMap = adResp.getResultMap();
		Iterator<String> resultIter = resultMap.keySet().iterator();
		while(resultIter.hasNext()){
			String resultKey = resultIter.next();
			String resultVal = resultMap.get(resultKey);
		%>
<%=resultKey%> = <%=resultVal%>
<%
		}//end while
	}//end if
%>

	</textarea>
</body>
</html>