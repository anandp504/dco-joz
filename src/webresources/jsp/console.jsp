<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Joz Console : Home</title>
  </head>

  <body>
  <jsp:include page="header.jsp"/>
  <br>

  <div id="links">
  	<strong>Commands</strong>
  </div>
  <br>

  <div>
  	<a href="get-ad-data.jsp">Get-ad-data</a>
  </div>
  <br>

  <div>
  	<a href="content-status.jsp">Content status</a>
  </div>
  <br>

  <div>
  	<a href="cma-content-status.jsp">Campaign Content status</a>
  </div>
  <br>


  <div>
    	<a href="performance-stat.jsp">Performance statistics</a>
  </div>
  <br>

  <div>
    	<a href="eval.jsp">Eval</a>
  </div>
  <br>
  
  <div>
  	<a href="service-multiplexer.jsp">Service multiplexer</a> (<a href="sm-test-input.jsp">test</a>)
  </div>
  <br>
  
  <div>
    	<a href="http://<%=request.getServerName()%>:<%=request.getServerPort()%>/joz/refresh?type=listing&jspMode=true" onclick="return confirm('This will force a data refresh. Do you want to continue?');">Refresh Data</a>
  </div>
   <br>
  
  <div>
    	<a href="http://<%=request.getServerName()%>:<%=request.getServerPort()%>/joz/refresh?type=campaign&jspMode=true" onclick="return confirm('This will force a cma data refresh. Do you want to continue?');">Refresh Campaign Data</a>
  </div>
  <br>

  </body>
</html>
