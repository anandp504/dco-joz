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
		if ( (0 != (totalReqs-totalFailedReqs)) ) {
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
		Snapshot at : <%=((new Date()).toString())%>
	</div>
	<br>
	<div>
		<table border="1" cellspacing="0">
		<tr>
		<td>Number of calls</td>
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
		<td>Tspec Name:</td>
		<td><%=(0!=totalReqs)?minTimedTspecName:"Not available"%></td>
		</tr>
		<tr>
		<td>Time:</td>
		<td><%=(0!=totalReqs)?(minRequestTime/1000000):0%> ms</td>
		</tr>
		</table>
		</td>
		</tr>
		<tr>
		<td>Worst call performance</td>
		<td>
		<table border="1" cellpadding="2" cellspacing="0">
		<tr>
		<td>Tspec Name:</td>
		<td><%=(0!=totalReqs)?maxTimedTspecName:"Not available"%></td>
		</tr>
		<tr>
		<td>Time</td>
		<td><%=(0!=totalReqs)?(maxRequestTime/1000000):0%> ms </td>
		</tr>
		</table>
		</td>
		</tr>
		<tr>
		<td>Average call performance</td>
		<td><%=(0!=totalReqs)?(aveReqTime/1000000):0%> ms</td>
		</tr>
		</table>
	</div>
	<br>
	<div>
		<%
			Map<String, Long> failedTspecs=pms.getFailedTspecs();
			Set keySet=failedTspecs.keySet();
			Iterator it=keySet.iterator();
			String tspec=null;
			Long count=null;
			for(int i=0;it.hasNext();i++) {
				if(0==i) {
					out.print("<strong>Failed Tspecs</strong><br>");
					out.print("<table border=\"1\" cellspacing=\"0\"><tr><td>Tspec Name</td><td>Count</td></tr>");
				}
				tspec=(String)it.next();
				count=(Long)failedTspecs.get(tspec);
				out.print("<tr><td>"+tspec+"</td><td>"+count.longValue()+"</td></tr>");
			}
			out.print("</table>");
		%>
	</div>
</body>
</html>
