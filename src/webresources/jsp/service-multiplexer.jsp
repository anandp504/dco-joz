<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.tumri.cma.domain.*" %>
<%@ page language="java" import="com.tumri.joz.campaign.*" %>
<%@ page language="java" import="com.tumri.joz.index.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <title>Joz Console : Service Multiplexer</title>
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
<div id="desc">
	<strong>Joz Console Ver 0.1</strong>
	<hr/>
	<div id="homelink" style="text-align: right">
		<a href="console.jsp">home</a>
	</div>
</div>
<br>

<div id="links">
	<strong>Service Multiplexer</strong>
</div>
<br>

<div>
	<form id="tspecSelForm" name="tspecSelForm" method="post" action="products.jsp">
	<input type="hidden" id="selTspec" name="selTspec" value=""/>
	<table>
	<tr>
	<th align="left">Type</th>
	<th align="left">Site</th>
	<th align="left">Tspec</th>
	</tr>
	<%
		CampaignDB campaignDB=CampaignDB.getInstance();
		AtomicAdpodIndex urlAdPodIndex=campaignDB.getUrlAdPodMappingIndex();
		Set<String> urlKeys=urlAdPodIndex.getKeys();
		Iterator it=urlKeys.iterator();
		String url=null;
		SortedSet<AdPodHandle> values=null;
		AdPodHandle adPodHandle=null;
		Iterator valuesIt=null;
		OSpec oSpec=null;
		while(it.hasNext()) {
			url=UrlNormalizer.getNormalizedUrl((String)it.next());
			Url urlObj=campaignDB.getUrl(url);
			values=urlAdPodIndex.get(url);
			valuesIt=values.iterator();
			while(valuesIt.hasNext()) {
				adPodHandle=(AdPodHandle)valuesIt.next();
				try {
					oSpec=campaignDB.getOSpecForAdPod(adPodHandle.getOid());
				}catch(Exception e) {
					continue;
				}
				out.print("<tr>");
				out.print("<td>R</td>");
				out.print("<td>"+((null==urlObj)?url:urlObj.getName())+"</td>");
				out.print("<td><a href=\"javascript:submitForm('"+oSpec.getName()+"')\">"+oSpec.getName()+"</td>");
				out.print("</tr>");
			}
		}
		
		AtomicAdpodIndex themeAdPodIndex=campaignDB.getThemeAdPodMappingIndex();
		Set<String> themeKeys=themeAdPodIndex.getKeys();
		Iterator themeIt=themeKeys.iterator();
		String theme=null;
		values=null;
		adPodHandle=null;
		valuesIt=null;
		oSpec=null;
		while(themeIt.hasNext()) {
			theme=(String)themeIt.next();
			values=themeAdPodIndex.get(theme);
			valuesIt=values.iterator();
			while(valuesIt.hasNext()) {
				adPodHandle=(AdPodHandle)valuesIt.next();
				try {
					oSpec=campaignDB.getOSpecForAdPod(adPodHandle.getOid());
				}catch(Exception e) {
					continue;
				}
				out.print("<tr>");
				out.print("<td>T</td>");
				out.print("<td>"+theme+"</td>");
				out.print("<td><a href=\"javascript:submitForm('"+oSpec.getName()+"')\">"+oSpec.getName()+"</td>");
				out.print("</tr>");
			}
		}
		
		AtomicAdpodIndex locationAdPodIndex=campaignDB.getLocationAdPodMappingIndex();
		Set<Integer> locationKeys=locationAdPodIndex.getKeys();
		Iterator locationIt=locationKeys.iterator();
		Integer location=null;
		values=null;
		adPodHandle=null;
		valuesIt=null;
		oSpec=null;
		while(locationIt.hasNext()) {
			location=(Integer)locationIt.next();
			values=themeAdPodIndex.get(location.intValue());
			valuesIt=values.iterator();
			while(valuesIt.hasNext()) {
				adPodHandle=(AdPodHandle)valuesIt.next();
				try {
					oSpec=campaignDB.getOSpecForAdPod(adPodHandle.getOid());
				}catch(Exception e) {
					continue;
				}
				out.print("<tr>");
				out.print("<td>L</td>");
				out.print("<td>"+theme+"</td>");
				out.print("<td><a href=\"javascript:submitForm('"+oSpec.getName()+"')\">"+oSpec.getName()+"</td>");
				out.print("</tr>");
			}
		}

	%>
	</table>
	</form>
</div>
</body>
</html>
