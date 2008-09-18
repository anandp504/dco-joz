<%@ page language="java" import="com.tumri.joz.server.domain.JozAdRequest" %>
<%@ page language="java" %>
<%@ page language="java" %>
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
		<i>
		(Keys- <%=(":"+ JozAdRequest.KEY_THEME)%> <%=(":"+JozAdRequest.KEY_AD_HEIGHT)%> <%=(":"+JozAdRequest.KEY_AD_TYPE)%> <%=(":"+JozAdRequest.KEY_AD_WIDTH)%> <%=
				(":"+JozAdRequest.KEY_AD_OFFER_TYPE)%> <%=(":"+JozAdRequest.KEY_ALLOW_TOO_FEW_PRODUCTS)%> <%=(":"+JozAdRequest.KEY_AREACODE)%> <%=(":"+JozAdRequest.KEY_CATEGORY)%> <%=
	(":"+JozAdRequest.KEY_CITY)%> <%=(":"+JozAdRequest.KEY_COUNTRY)%> <%=(":"+JozAdRequest.KEY_DMACODE)%> <%=(":"+JozAdRequest.KEY_KEYWORDS)%> <%=
	(":"+JozAdRequest.KEY_LATITUDE)%> <%=(":"+JozAdRequest.KEY_LOCATION_ID)%> <%=(":"+JozAdRequest.KEY_LONGITUDE)%> <%=
	(":"+JozAdRequest.KEY_MAX_PROD_DESC_LEN)%> <%=(":"+JozAdRequest.KEY_MIN_NUM_LEADGENS)%> <%=(":"+JozAdRequest.KEY_MULTI_VALUE_FIELD1)%> <%=(":"+JozAdRequest.KEY_MULTI_VALUE_FIELD2)%> <%=
	(":"+JozAdRequest.KEY_MULTI_VALUE_FIELD3)%> <%=(":"+JozAdRequest.KEY_MULTI_VALUE_FIELD4)%> <%=
	(":"+JozAdRequest.KEY_MULTI_VALUE_FIELD5)%> <%=(":"+JozAdRequest.KEY_NUM_PRODUCTS)%> <%=(":"+JozAdRequest.KEY_RECIPE_ID)%> <%=(":"+JozAdRequest.KEY_REGION)%> <%=
	(":"+JozAdRequest.KEY_REVERT_TO_DEFAULT_REALM)%> <%=(":"+ JozAdRequest.KEY_ROW_SIZE)%> <%=(":"+JozAdRequest.KEY_SCRIPT_KEYWORDS)%> <%=(":"+JozAdRequest.KEY_STORE_ID)%> <%=
	(":"+JozAdRequest.KEY_T_SPEC)%> <%=(":"+JozAdRequest.KEY_URL)%> <%=(":"+JozAdRequest.KEY_WHICH_ROW)%> <%=(":"+JozAdRequest.KEY_ZIP_CODE)%> )
			</i>
	</div>
	<br>
	<div>
		<form id="evalForm" action="/joz/console?mode=view&option=eval" method="post">
			Enter get-ad-data expression<br>
			<textarea id="text_eval_expr" name="text_eval_expr" style="width:100%;height:10cm" ></textarea><br>
			<input type="button" value="Go" onClick="javascript:submit_eval_form()"/>
		</form>
	</div>
	<script>
		document.getElementById("text_eval_expr").focus();
	</script>
  </body>
</html>
