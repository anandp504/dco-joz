<%@ page language="java" import="com.tumri.utils.stats.PerfStatInfo" %>
<%@ page import="com.tumri.utils.stats.PerformanceStats" %>
<%@ page import="java.util.*" %>
<div>
	Snapshot at : <%=((new Date()).toString())%>
</div>
<br>
<div>

	<%
		HashMap stats = PerformanceStats.getInstance().getStats();
		Set keySet = stats.keySet();
		Iterator keyIterator  = null;
		if(keySet != null){
			keyIterator = keySet.iterator();
		}
		if(keyIterator == null){
		  %> Error: no data to collect<%
		} else {
			while(keyIterator.hasNext()){
				String key = (String)keyIterator.next();
				PerfStatInfo perfStatInfo = (PerfStatInfo)stats.get(key);
				long maxRequestTime = perfStatInfo.getMaxTime();
				String maxRequestName = perfStatInfo.getMaxTimeRequest();
				String minRequestName = perfStatInfo.getMinTimeRequest();
				long minRequestTime = perfStatInfo.getMinTime();
				long totalReqs = perfStatInfo.getNumRequests();
				long totalFailedReqs = perfStatInfo.getFailedRequests();
				long totalTime = perfStatInfo.getTotalTimeElapsed();
                Map<String, Long> failedReqMap = perfStatInfo.getFailedRequestMap();
                long aveReqTime = 0;
				if(totalReqs != 0){
					aveReqTime = totalTime / totalReqs;
				}
	%>
	<table border="1" cellspacing="0" title="<%=key%>">
		<caption><%=key%></caption>
		<tr>
			<td>Number of calls</td>
			<td><%=totalReqs%></td>
		</tr>
		<tr>
			<td>Best call performance</td>
			<td>
				<table border="1" cellpadding="2" cellspacing="0">
					<tr>
						<td>Time:</td>
						<td><%=minRequestName!=null?minRequestName:""%></td>
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
                        <td><%=maxRequestName!=null?maxRequestName:""%></td>
						<td><%=(0!=totalReqs)?(maxRequestTime/1000):0%> micro seconds</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td>Average call performance</td>
			<td><%=(0!=totalReqs)?(aveReqTime/1000):0%> micro seconds</td>
		</tr>
        <% if (totalFailedReqs > 0) {%>
        <tr>
            <td>Number of Failed requests</td>
            <td><%=totalFailedReqs%></td>
        </tr>
        <% } %>
    </table>
	<br>
    <% if (failedReqMap!=null && !failedReqMap.isEmpty()) {  %>
        <table border="1" cellspacing="0" title="Failed requests info">
            <caption>Failed <%=key%> requests</caption>
        <%
            for (String s: failedReqMap.keySet()) {
        %>
        <tr>
            <td><%=s!=null?s:""%></td>
            <td><%=failedReqMap.get(s)!=null?failedReqMap.get(s).toString():0%></td>
        </tr>
        <%    } // end for %>
         </table>
    <% } %>

	<%
			}
		}
	%>
</div>