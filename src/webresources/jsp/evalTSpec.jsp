<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
	  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	  <title>Joz Console : TSpec-Executor</title>
	  <script type="text/javascript">
	  function submit_eval_form() {
	  	var textArea=document.getElementById("text_eval_expr");
		var textArea2=document.getElementById("text_eval_expr2");
		 if ('' == textArea.value) {
	  		alert('Enter ProductSelectionRequest expression');
	  		textArea.focus();
	  		return;
	  	} 
		if ('' == textArea2.value) {
	  		alert('Enter TSpec expression');
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
	<%
	String tSpecReq = (String)request.getAttribute("reqTSpec");
	String psrReq = (String)request.getAttribute("reqPSR");
	String resp = (String)request.getAttribute("resp");
    %>
	<div id="links">
		<h2>TSpec-Executor</h2>
	</div>
	<div>
		<form id="evalForm" action="/joz/console?mode=execute&option=tspec" method="post">
			<h4>Enter ProductSelectionRequest Info:</h4>
			<i>(Keys= :num_products :offertype :city :state :country :zipcode :dmacode :areacode
			:brandomize :bmineurls :bpaginate :bbackfill :requestkeywords :requestcategory :multivaluequery1
			:multivaluequery2 :multivaluequery3 :multivaluequery4 :multivaluequery5)</i>
			<br>
			<textarea id="text_eval_expr" name="text_eval_expr" style="width:100%;height:2cm" ><%
				if(psrReq != null){
					%><%=psrReq%><%
				}
			%></textarea><br>
			<br>
			<br>
			<h4>Enter TSpec Info:</h4>
			<i>(Keys = :allowexternalquery :useradiusquery :radius :minepuburl :lowprice
			:highprice :applygeofilter :applyurlfilter :applykeywordfilter :includedproviders
			:excludedproviders :excludedcategories :includedcategories :includedbrands :excludedbrands
			:excludedmerchants :includedmerchants :includedproducts :excludedproducts :includedkeywords)</i>
			<br>
			<i>Note: Keys that can take more than one value(includedproviders, excludedproviders, etc...) can be
			followed by a comma serated list of values.</i>
			<textarea id="text_eval_expr2" name="text_eval_expr2" style="width:100%;height:2cm" ><%
				if(tSpecReq != null){
					%><%=tSpecReq%><%
				}
			%></textarea><br>
			<input type="button" value="Go" onClick="javascript:submit_eval_form()"/>
		</form>
	</div>
	<script>
		document.getElementById("text_eval_expr").focus();
	</script>

    <br>
<br>
<h4>TSpec Execution Response:</h4>
<br>
<textarea id="text_eval_expr3" name="text_eval_expr" style="width:100%;height:17cm"><%
	if(resp!= null){
%><%=resp%>
<%
	}//end if
%></textarea>

  </body>
</html>
