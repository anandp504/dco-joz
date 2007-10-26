<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>Joz Console : Service Multiplexer Test</title>
	</head>

	<body>
	<jsp:include page="header.jsp"/>
	<div id="homelink" style="text-align: right">
		<a href="console.jsp">home</a>
	</div>
	<br>
	<div id="links">
		  <strong>Service Multiplexer Test Input</strong>
	</div>
	<br>
	<form id="smform" name="smform" action="sm-test-output.jsp" method="post">
		<table>
			<tr>
				<td>Context:</td>
			</tr>
			<tr>
				<td>Store-ID:&nbsp</td>
				<td><input type="text" id="storeId" name="storeId" value="" size="50"/></td>
			</tr>
			<tr>
				<td>Theme:&nbsp</td>
				<td><input type="text" id="theme" name="theme" value="" size="50"/></td>
			</tr>
			<tr>
				<td>URL:&nbsp</td>
				<td><input type="text" id="url" name="url" value="" size="50"/></td>
			</tr>
			<tr>
				<td>Zip-code:&nbsp</td>
				<td><input type="text" id="zipcode" name="zipcode" value="" size="50"/></td>
			</tr>
			<tr>
				<td><br></td>
			</tr>
			<tr>
				<td>Test:&nbsp</td>
				<td><input type="text" id="testimp" name="testimp" value="100" size="50"/>&nbsp;&nbsp;&nbsp;impressions</td>
			</tr>
		</table>
		<input type="submit" value="  Go  "/> 
	</form
  </body>
</html>
