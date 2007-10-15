<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.tumri.joz.jozMain.*" %>
<%@ page language="java" import="com.tumri.joz.monitor.EvalMonitor" %>
<%@ page language="java" import="com.tumri.joz.monitor.EvalMonitorStatus" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
	  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	  <title>Joz Console : Eval Output</title>
	  <script type="text/javascript">
  	  </script>
  </head>
  <body>
	<div id="desc">
		<strong>Joz Console Ver 0.1</strong>
		<hr/>
		<div id="homelink" style="text-align: right">
			<a href="eval.jsp">eval</a>
			<a href="console.jsp">home</a>
		</div>
	</div>
	<br>
	<div id="heading">
		<strong>Eval Expression</strong><br>
	</div>
	<br>
	<div id="Get-ad-data-expr">
		<%
			String getAdExpr=request.getParameter("text_eval_expr");
			EvalMonitor evalMonitor=new EvalMonitor();
			EvalMonitorStatus evalMonStatus=(EvalMonitorStatus)evalMonitor.getStatus(getAdExpr);
			if (evalMonStatus.getFailed() == true) {
				out.print("<p> <strong> There has been an error in the execution of this command. </strong> </p>"); 
				out.print("<p>"+evalMonStatus.getFailedMessage()+"</p>");
				return;
			}
			AdDataRequest adDataReq=evalMonStatus.getAdDataRequest();
			String storeId=adDataReq.get_store_id();
			String url=adDataReq.get_url();
			String theme=adDataReq.get_theme();
			String tspec=adDataReq.get_t_spec();
			String category=adDataReq.get_category();
			Integer numProducts=adDataReq.get_num_products();
		%>	
		<table border="1">
			<tr>
			<td>Stored Id</td>
			<td><%=(null==storeId)?null:storeId%></td>
			</tr>
			<tr>
			<td>Theme</td>
			<td><%=(null==theme)?null:theme%></td>
			</tr>
			<tr>
			<td>URL</td>
			<td><%=(null==url)?null:url%></td>
			</tr>
			<tr>
			<td>Tspec</td>
			<td><%=(null==tspec)?null:tspec%></td>
			</tr>
			<tr>
			<td>Category</td>
			<td><%=(null==category)?null:category%></td>
			</tr>
			<tr>
			<td>Number of Products</td>
			<td><%=(null==numProducts)?"t":numProducts.longValue()%></td>
			</tr>
		</table>
	</div>
	<br>
	<div id="total-products">
		<strong>Eval Execution Output</strong><br><br>
		Targeting OSpec:&nbsp;&nbsp;&nbsp;<%=evalMonStatus.getStrategy()%><br>
		<% 
			Long totalProductMatch=evalMonStatus.getTotalProductMatch();
		%>
		Total Products matched:&nbsp;&nbsp;&nbsp;<%=(null==totalProductMatch)?0:totalProductMatch.longValue()%>
	</div>
	<br>
	<div id="">
		<table border="1">
		<th>Category</th><th>Number of products</th>
		<%
			Map productMatchMap=evalMonStatus.getProductMatch();
			Set keySet=productMatchMap.keySet();
			Iterator it=keySet.iterator();
			String key=null;
			Long value=null;
			while(it.hasNext()) {
				key=(String)it.next();
				value=(Long)productMatchMap.get(key);
				out.print("<tr><td>"+key+"</td><td>"+value.longValue()+"</td></tr>");
			}
		%>
		</table>
	</div>
  </body>
</html>
