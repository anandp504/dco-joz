<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="org.apache.log4j.spi.LoggerRepository" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Enumeration" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Loggin Info</title>
	<style>
		.floatingDiv { position:absolute; border: solid 1px blue; display:none; width:325px; height:300px; }
	</style>
	<script  type="text/javascript">
		function submitLevel() {
			var obj=document.getElementById("LogList");
			var id=document.getElementById("id");
			id.value=obj.options[obj.selectedIndex].text;
			var obj2 = document.getElementById("LevelList");
			var option=document.getElementById("option");
			option.value=obj2.options[obj2.selectedIndex].text;
			var mode=document.getElementById("mode");
			mode.value = "log";
			var form=document.getElementById("LogLevelForm");
			form.submit();
		};


	</script>
</head>

<body>
<jsp:include page="header.jsp"/>
<div id="links" style="text-align: right">
	<a href="/joz/console">home</a>
</div>
<br>

<div>
	Logger Info(Name, Level):<br>
	<%
		Logger rootLogger = Logger.getRootLogger();
		ArrayList<Logger> logList = new ArrayList<Logger>();
		logList.add(rootLogger);
	%>
	<b>RootLogger:</b> <%=rootLogger.getName()%> <b>Level:</b> <%=rootLogger.getLevel()%><br>
	<%
		LoggerRepository repo = rootLogger.getLoggerRepository();
		Enumeration logEnum = repo.getCurrentLoggers();
		int i = 0;
		while(logEnum.hasMoreElements()){
			Logger log = (Logger)logEnum.nextElement();
			if(log.getLevel() != null){
				logList.add(log);
	%>
	<br><%=i%>. <b>Name:</b>  <%=log.getName()%>,  <b>Level:</b> <%=log.getLevel()%>
	<%
				i++;
			} //if
		}  //while
	%>
	<br>
	<br>
	<form id="LogLevelForm" action="/joz/console" method="post">
		<div>
			<strong>Select Log/Level</strong> (<%=logList.size()%> Logs(s)):
		</div>
		<select id="LogList">
			<%
				for(int j=0;j<logList.size();j++) {
					if(logList.get(j) != null){
						if(j == 0){
							out.print("<option selected value=\""+j+"\">"+logList.get(j).getName()+"</option>");
						} else {
							out.print("<option value=\""+j+"\">"+logList.get(j).getName()+"</option>");
						}
					}
				}
			%>
		</select>
		<input type="hidden" name="id"  id="id" value=""/>
		<select id="LevelList">
			<option selected value="debug">debug</option>
			<option value="info">info</option>
			<option value="warn">warn</option>
			<option value="error">error</option>
			<option value="fatal">fatal</option>
			<option value="off">off</option>
		</select>
		<input type="hidden"  name="option"  id="option" value=""/>
		<input type="hidden" name="mode" id="mode" value="log"/>
		<input type="button" value="Set Level" onClick="javascript:submitLevel()"/>
	</form>
</div>
</body>
</html>
