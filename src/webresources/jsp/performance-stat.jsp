<%@ page language="java" import="com.tumri.joz.utils.LogUtils" %>
<%@ page language="java" import="com.tumri.utils.stats.PerformanceStats" %>
<%@ page language="java" import="com.tumri.utils.stats.PerfStatInfo" %>
<%@ page language="java" import="com.tumri.utils.tcp.server.monitor.PerformanceMonitor" %>
<%@ page language="java" import="java.util.Date" %>
<%@ page language="java" import="java.util.HashMap" %>
<%@ page language="java" import="com.tumri.joz.utils.AppProperties" %>
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
		} else if("Disable".equals(operation)){
			PerformanceStats.getInstance().disable();
		} else if("Enable".equals(operation)){
			PerformanceStats.getInstance().enable();			
		}
	}

	boolean isLogging = PerformanceStats.getInstance().isLogInAction();

	String isStartDisabled = "";
   	String isStopDisabled = "disabled";
	if(isLogging){
		isStopDisabled = "";
		isStartDisabled = "disabled";
	}

	boolean statsDisabled = PerformanceStats.getInstance().isDisabled();
	String isDisableStatsDisabled = "";
	String isEnableStatsDisabled = "";
	if(statsDisabled){
		isStartDisabled = "disabled";
		isDisableStatsDisabled = "disabled";
	} else {
		isEnableStatsDisabled = "disabled";
	}

	//

	PerformanceMonitor pm = PerformanceMonitor.getInstance();
    int maxThreads = Integer.parseInt(AppProperties.getInstance().getProperty("tcpServer.poolSize"));
    int activeThreads = pm.getActiveThreads();
    long totalReqs = -1;
    long totalFailedReqs = -1;
    long minRequestTime =-1;
    long maxRequestTime =-1;
    long totalTime = -1;
    long aveReqTime = -1;
	String TCP_CONNECTION= "TCN";
	HashMap statsMap = PerformanceStats.getInstance().getStats();
	if(statsMap != null){
		PerfStatInfo perfStatInfo = (PerfStatInfo)statsMap.get(TCP_CONNECTION);
	    if(perfStatInfo != null){
	    	totalReqs = perfStatInfo.getNumRequests();
	    	totalTime = perfStatInfo.getTotalTimeElapsed();
	    	totalFailedReqs = perfStatInfo.getFailedRequests();
		    minRequestTime =perfStatInfo.getMinTime();
		    maxRequestTime =perfStatInfo.getMaxTime();
		    if ((0 != (totalReqs - totalFailedReqs))) {
		        aveReqTime = (totalTime / (totalReqs - totalFailedReqs));
	}
	    }
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
        <input type="button" id="disableStats1" name="disableStats" value="DisableStats" <%=" "+ isDisableStatsDisabled +" "%> onclick="javascript:submitForm('Disable')"/>
		<input type="button" id="enableStats1" name="enableStats" value="EnableStats" <%=" "+ isEnableStatsDisabled +" "%> onclick="javascript:submitForm('Enable')"/>
</div>

<br>
<jsp:include page="stats.jsp"/>
	<div>
		<table border="1" cellspacing="0">
		<tr>
		<td>Max Threads supported</td>
		<td><%=maxThreads%></td>
		</tr>
		<tr>
		<td>Active Threads running</td>
		<td><%=activeThreads%></td>
		</tr>
		<tr>
		<td>Total number of requests</td>
		<td><%=totalReqs%></td>
		</tr>
		<tr>
		<td>Number of failed calls</td>
		<td><%=totalFailedReqs%></td>
		</tr>
		<tr>
		<td>Best call performance</td>
		<td>
		<table border="1" cellpadding="2" cellspacing="0">
		<tr>
		<td>Time:</td>
		<td><%=(0!=totalReqs)?(minRequestTime/1000):0%> micro seconds</td>
		</tr>
		</table>
		</td>
		</tr>
		<tr>
		<td>Worst call performance</td>
		<td>
		<table border="1" cellpadding="2" cellspacing="0">
		<tr>
		<td>Time</td>
		<td><%=(0!=totalReqs)?(maxRequestTime/1000):0%> micro seconds</td>
		</tr>
		</table>
		</td>
		</tr>
		<tr>
		<td>Average call performance</td>
		<td>
		<table border="1" cellpadding="2" cellspacing="0">
		<tr>
		<td>Time</td>
		<td><%=(0!=totalReqs)?(aveReqTime/1000):0%> micro seconds</td>
		</tr>
		</table>
		</td>
		</tr>
		</table>
	</div>
<br>

<div>
	<form id="perfForm" name="perfForm" method="post" action="/joz/console?mode=perf">
		<input type="hidden" id="operation" name="operation" value="Reset"/>
		<input type="button" id="refresh" name="refresh" value="Refresh" onClick="javascript:submitForm('Refresh')"/>
		<input type="button" id="reset" name="reset" value="Reset" onClick="javascript:submitForm('Reset')"/>
		<input type="button" id="startLogging" name="startLogging" value="StartLogging" <%=" "+ isStartDisabled +" "%> onclick="javascript:submitForm('Start')"/>
		<input type="button" id="stopLogging" name="stopLogging" value="StopLogging" <%=" "+ isStopDisabled +" "%> onclick="javascript:submitForm('Stop')"/>
	    <input type="button" id="disableStats" name="disableStats" value="DisableStats" <%=" "+ isDisableStatsDisabled +" "%> onclick="javascript:submitForm('Disable')"/>
		<input type="button" id="enableStats" name="enableStats" value="EnableStats" <%=" "+ isEnableStatsDisabled +" "%> onclick="javascript:submitForm('Enable')"/>
	</form>
</div>
</body>
</html>
