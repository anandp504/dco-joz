<%@ page language="java" import="com.tumri.content.data.ContentProviderStatus" %>
<%@ page language="java" import="com.tumri.content.ContentProviderFactory" %>
<%@ page language="java" import="com.tumri.content.ContentProvider" %>
<%@ page language="java" import="java.text.SimpleDateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
	  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	  <title>Joz Console : Refresh-Data</title>
  </head>
  <body>
	 <% 
		String success = null;
		String  datetime = null;
		String requestValue = request.getQueryString();
		if ((requestValue == null) || requestValue.equals("qac")) {
			ContentProviderFactory f = ContentProviderFactory.getDefaultInitializedInstance();
			ContentProvider cp = f.getContentProvider();
			cp.refresh();
			ContentProviderStatus status = cp.getStatus();
			success = (status.lastRunStatus == true? "successful" : "failed");
			datetime = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS")).format(status.lastRefreshTime);
		}
     %>
	 <%
		if (requestValue == null) {
	 %>
	 	<div id="desc">
			<strong>Joz Console Ver 0.1</strong>
			<hr/>
			<div id="homelink" style="text-align: right">
				<a href="console.jsp">home</a>
			</div>
	  	</div>
	  	<br>
	  	<div>
		  	<p>Refresh status:&nbsp;&nbsp; <%=success%> </p>
		  	<p>Refresh time: &nbsp;&nbsp; <%=datetime%> </p>
      	</div>
	  	<br>
	  <% }
		 else if (requestValue.equals("qac")) {
			out.print(success);
		 }
	  %>
  </body>
</html>