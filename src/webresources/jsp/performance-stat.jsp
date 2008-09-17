<%@ page language="java" import="com.tumri.joz.utils.LogUtils" %>
<%@ page language="java" import="com.tumri.utils.stats.PerformanceStats" %>
<%@ page import="com.tumri.utils.stats.PerfStatException" %>
<%@ page language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Joz Console : Performance Statistics</title>
	<script type="text/javascript">
		function submitForm(opStr) {
			var operation=document.getElementById("operation");
			operation.value=opStr;
			var perfForm=document.getElementById("perfForm");
			perfForm.submit();
		}
	</script>


</head>
<body>
<%
	String operation=request.getParameter("operation");
	if (operation != null){
		if("Reset".equals(operation)) {
			PerformanceStats.getInstance().reset();
		} else if("Start".equals(operation)){
			try {
				PerformanceStats.getInstance().startLogging(LogUtils.getTimingLog(), 10*60*1000);
			} catch (PerfStatException e) {
				
			}
		} else if("Stop".equals(operation)){
			PerformanceStats.getInstance().endLogging();
		}
	}

	boolean isLogging = PerformanceStats.getInstance().isLogInAction();

	String isStartDisabled = "";
   	String isStopDisabled = "disabled";
	if(isLogging){
		isStopDisabled = "";
		isStartDisabled = "disabled";
	}
%>
<jsp:include page="header.jsp"/>

<div id="homelink" style="text-align: right">
	<a href="/joz/console">home</a>
</div>
<div id="links">
	<strong>Performance Statistics</strong> &nbsp;&nbsp;&nbsp;
</div>
<div>
		<input type="button" id="refresh1" name="refresh" value="Refresh" onClick="javascript:submitForm('Refresh')"/>
		<input type="button" id="reset1" name="reset" value="Reset" onClick="javascript:submitForm('Reset')"/>
		<input type="button" id="startLogging1" name="startLogging" value="StartLogging" <%=" "+ isStartDisabled +" "%> onclick="javascript:submitForm('Start')"/>
		<input type="button" id="stopLogging1" name="stopLogging" value="StopLogging" <%=" "+ isStopDisabled +" "%> onclick="javascript:submitForm('Stop')"/>
    
</div>

<br>
<jsp:include page="stats.jsp"/>
<br>

<div>
	<form id="perfForm" name="perfForm" method="post" action="/joz/console?mode=perf">
		<input type="hidden" id="operation" name="operation" value="Reset"/>
		<input type="button" id="refresh" name="refresh" value="Refresh" onClick="javascript:submitForm('Refresh')"/>
		<input type="button" id="reset" name="reset" value="Reset" onClick="javascript:submitForm('Reset')"/>
		<input type="button" id="startLogging" name="startLogging" value="StartLogging" <%=" "+ isStartDisabled +" "%> onclick="javascript:submitForm('Start')"/>
		<input type="button" id="stopLogging" name="stopLogging" value="StopLogging" <%=" "+ isStopDisabled +" "%> onclick="javascript:submitForm('Stop')"/>
	</form>
</div>
</body>
</html>
