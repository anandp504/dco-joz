<%@ page language="java" import="com.tumri.joz.monitor.PerformanceMonitor" %>
<%@ page language="java" import="com.tumri.joz.monitor.PerformanceMonitorStatus" %>
<%@ page language="java" import="java.util.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <title>Joz Console Product Request Statistics Page</title>
</head>
<body>
	<%
		PerformanceMonitor performanceMonitor=PerformanceMonitor.getInstance();
		PerformanceMonitorStatus pms=(PerformanceMonitorStatus)performanceMonitor.getStatus("performance");
		long totalReqs=pms.getTotalRequestCount();
		long totalFailedReqs=pms.getFailedRequestCount();
		
		//Retrive Minimum Time Taken Request.
		String minTimedTspecName=pms.getMinTspec();
		long minRequestTime=pms.getMinTime();
		
		//Retrive Long Time Taken Request.
		String maxTimedTspecName=pms.getMaxTspec();
		long maxRequestTime=pms.getMaxTime();
		
		//Retrive average Time
		long aveReqTime=0;
		if(0!=totalReqs) {
			aveReqTime=(pms.getTotalTime()/(totalReqs-totalFailedReqs));
		}
	%>
	<div id="desc">
		<strong>Joz Console Ver 0.1</strong>
		<hr/>
		<div id="homelink" style="text-align: right">
			<a href="console.jsp">home</a>
		</div>
  	</div>
  	<br>
  	<div id="links">
		<strong>get-ad-data call statistics</strong> &nbsp;&nbsp;&nbsp since : <%=(pms.getStartDate()).toString()%>
	</div>
	<br>
	<div>
		Number of calls:&nbsp;&nbsp;&nbsp <%=totalReqs%>
	</div>
	<br>
	<div>
		Number of failed calls:&nbsp;&nbsp;&nbsp <%=totalFailedReqs%>
	</div>
	<br>
	<div>
		<strong>Best call performance: </strong> <br>
		Tspec Name = <%=(0!=totalReqs)?minTimedTspecName:""%><br>
		Time = <%=(0!=totalReqs)?(minRequestTime/1000000):0%> ms
	</div>
	<br>
	<div>
		<strong>Worst call performance: </strong> <br>
		Tspec Name = <%=(0!=totalReqs)?maxTimedTspecName:""%> <br>
		Time = <%=(0!=totalReqs)?(maxRequestTime/1000000):0%> ms
	</div>
	<br>
	<div>
		<strong>Average call performance:&nbsp;&nbsp;&nbsp <%=(0!=totalReqs)?(aveReqTime/1000000):0%> ms </strong><br>
	</div>
</body>
</html>
