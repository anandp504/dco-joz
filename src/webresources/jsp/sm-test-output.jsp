<%@ page language="java" import="java.io.*" %>
<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.tumri.utils.sexp.*" %>
<%@ page language="java" import="com.tumri.joz.targeting.*" %>
<%@ page language="java" import="com.tumri.joz.jozMain.*" %>
<%@ page language="java" import="com.tumri.cma.domain.OSpec" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>Joz Console : Service Multiplexer Test</title>
		  <script type="text/javascript">
			  function submitForm(tspec) {
				var selTspec=document.getElementById("selTspec");
				selTspec.value=tspec;
				var form=document.getElementById("tspecSelForm");
				form.submit();
			  };
		  </script>
	</head>

	<body>
	<jsp:include page="header.jsp"/>
	<div id="homelink" style="text-align: right">
		<a href="sm-test-input.jsp">test</a>
		<a href="console.jsp">home</a>
	</div>
	<br>
	<div id="links">
		<strong>Service Multiplexer Test Context</strong>
	</div>
	<br>
	<%
		String storeId=request.getParameter("storeId");
		String theme=request.getParameter("theme");
		String url=request.getParameter("url");
		String zipCode=request.getParameter("zipcode");
		String cmd="(get-ad-data)";
		if ( !(storeId.trim().equals("")) ) {
			cmd=new String("(get-ad-data :store-ID '|"+storeId+"|)");
		}
		else if ( !(theme.trim().equals("")) ) {
			cmd=new String("(get-ad-data :theme \""+theme+"\")");
		}
		else if ( !(url.trim().equals("")) ) {
			cmd=new String("(get-ad-data :url \""+url+"\")");
		}
		else if ( !(zipCode.trim().equals("")) ) {
			cmd=new String("(get-ad-data :zip-code \""+zipCode+"\")");
		}
		
		//Set impressions to test.
		long totalImpToTest=100L;
		String testImp=(request.getParameter("testimp").trim());
		try {
			totalImpToTest=Long.parseLong(testImp);
		}catch(NumberFormatException e){
			//nothing.
		}
		
		Reader r=new StringReader(cmd);
		SexpReader lr=new SexpReader(r);
		Sexp e = lr.read();
		OSpec targetSpec=null;
		String key;
		Long value;
		HashMap<String,Long> targetSpecMap=new HashMap<String,Long>();
		AdDataRequest adDataReq=new AdDataRequest(e);
		TargetingRequestProcessor trp=TargetingRequestProcessor.getInstance();
		for(long i=0;i<totalImpToTest;i++) {
			targetSpec=trp.processRequest(adDataReq);
			key=targetSpec.getName();
			value=targetSpecMap.get(key);
			if (null != value) {
				value=new Long(value.longValue()+1);
			}
			else {
				value=new Long(1);
			}
			targetSpecMap.put(key,value);
		}
	%>
	<div id="context">
	<table>
		<tr>
			<td>Store ID:&nbsp;</td>
			<td><%=storeId%></td>
		</tr>
		<tr>
			<td>Theme:&nbsp;</td>
			<td><%=theme%></td>
		</tr>
		<tr>
			<td>URL:&nbsp;</td>
			<td><%=url%></td>
		</tr>
		<tr>
			<td>Zip-code:&nbsp;</td>
			<td><%=zipCode%></td>
		</tr>
	</table>
	<br>
	<strong>Result for <%=totalImpToTest%> impressions</strong>
	<form id="tspecSelForm" name="tspecSelForm" method="post" action="products.jsp">
	<input type="hidden" id="selTspec" name="selTspec" value=""/>
	<table>
		<%
			Set<String> keys=(Set<String>)targetSpecMap.keySet();
			Iterator it=keys.iterator();
			while(it.hasNext()) {
				key=(String)it.next();
				value=(Long)targetSpecMap.get(key);
				out.print("<tr><td>"+(value.longValue()/totalImpToTest*100)+"%</td><td><a href=\"javascript:submitForm('"+key+"')\">"+key+"</a></td></tr>");
			}
		%>
	</table>
	</form>
  </body>
</html>
