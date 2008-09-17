<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.tumri.joz.campaign.*" %>
<%@ page language="java" import="com.tumri.cma.domain.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
	  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	  <title>Joz Console : Eval</title>
	  <script type="text/javascript">
	  function submit_eval_form() {
	  	var textArea=document.getElementById("text_eval_expr");
	  	if ('' == textArea.value) {
	  		alert('Enter get-ad-data expression');
	  		textArea.focus();
	  		return;
	  	}
		var form=document.getElementById("evalForm");
		form.submit();
	  };
  	  </script>
  </head>
  <body>
	<jsp:include page="header.jsp"/>
	<div id="homelink" style="text-align: right">
		<a href="/joz/console">home</a>
	</div>
	<br>
	<div id="links">
		<strong>Eval</strong>
	</div>
	<br>
	<div>
		<form id="evalForm" action="eval-output.jsp" method="post">
			Enter get-ad-data expression<br>
			<textarea id="text_eval_expr" name="text_eval_expr" rows="10" cols="75"></textarea><br>
			<input type="button" value="Go" onClick="javascript:submit_eval_form()";/>
		</form>
	</div>
	<script>
		document.getElementById("text_eval_expr").focus();
	</script>
  </body>
</html>
