<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
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
    <a href="/joz/console?mode=ad">Get-ad-data</a>
</div>
<br>

<div>
    <a href="/joz/console?mode=caa">Content status</a>
</div>
<br>

<div>
    <a href="/joz/console?mode=cma">Campaign Content status</a>
</div>
<br>

<div>
    <a href="/joz/console?mode=wmstatus">WM Content status</a>
</div>
<br>


<div>
    <a href="/joz/console?mode=perf">Performance statistics</a>
</div>
<br>

<div>
    <a href="/joz/console?mode=view&option=latest">Request and Response</a>
</div>
<br>

<div>
    <a href="/joz/console?mode=eval">Eval</a>
</div>
<br>

<div>
    <a href="/joz/console?mode=productdb">Query ProductDB</a>
</div>
<br>

<div>
    <a href="/joz/console?mode=sm">Service multiplexer</a>
</div>
<br>

<div>
    <a href="http://<%=request.getServerName()%>:<%=request.getServerPort()%>/joz/refresh?type=listing&jspMode=true"
       onclick="return confirm('This will force a data refresh. Do you want to continue?');">Refresh Data</a>
</div>
<br>

<div>
    <a href="http://<%=request.getServerName()%>:<%=request.getServerPort()%>/joz/refresh?type=campaign&jspMode=true"
       onclick="return confirm('This will force a cma data refresh. Do you want to continue?');">Refresh Campaign
        Data</a>
</div>
<br>

<div>
    <a href="/joz/console?mode=llc">LLC Status</a>
</div>
<br>

<div>
    <a href="/joz/console?mode=indexdebug">Index Debug Util</a>
</div>
<br>

<div>
    <a href="/joz/console?mode=log">Log Info</a>
</div>
<br>

<div>
    <a href="/joz/console?mode=qareport">Joz QA Report</a>
</div>
<br>

<div>
    <a href="/joz/console?mode=execute">TSpec Executor</a>
</div>
<br>

<div>
    <a href="/joz/console?mode=wm">Recipe Weight Matrix</a>
</div>
<br>

<div>
    <a href="/joz/refresh?type=validateIndex&jspMode=true">Validate Listings Index</a>
</div>
<br>

<div>
    <a href="/joz/console?mode=dictionaries">Vector Dictionaries</a>
</div>
<br>

<div>
    <a href="/joz/console?mode=caaDictionaries">Product Dictionaries</a>
</div>
<br>

<div>
    <a href="/joz/console?mode=gc">Collect Garbage</a>
</div>
<br>
</body>
</html>
