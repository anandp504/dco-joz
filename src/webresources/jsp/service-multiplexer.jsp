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

	<jsp:include page="header.jsp"/>
	<div id="homelink" style="text-align: right">
		<a href="console.jsp">home</a>
	</div>
	<br>

	<div id="links">
		<strong>Service Multiplexer</strong>
	</div>
	<br>

	<div>
		<form id="tspecSelForm" name="tspecSelForm" method="post" action="products.jsp">
		<input type="hidden" id="selTspec" name="selTspec" value=""/>
		<table border="1" cellspacing="0" cellpading="0">
		<tr>
		<th align="left">Type</th>
		<th align="left">Site</th>
		<th align="left">Geo</th>
		<th align="left">Tspec</th>
		</tr>
		<%
			CampaignDB campaignDB=CampaignDB.getInstance();

			AtomicAdpodIndex urlAdPodIndex=campaignDB.getUrlAdPodMappingIndex();
			AtomicAdpodIndex geoCountryAdPodIndex=campaignDB.getAdpodGeoCountryIndex();
			AtomicAdpodIndex adpodGeoRegionIndex=campaignDB.getAdpodGeoRegionIndex();
			AtomicAdpodIndex adpodGeoCityIndex=campaignDB.getAdpodGeoCityIndex();
			AtomicAdpodIndex adpodGeoDmacodeIndex=campaignDB.getAdpodGeoDmacodeIndex();
			AtomicAdpodIndex adpodGeoAreacodeIndex=campaignDB.getAdpodGeoAreacodeIndex();
			AtomicAdpodIndex adpodGeoZipcodeIndex=campaignDB.getAdpodGeoZipcodeIndex();

			Set<String> urlKeys=urlAdPodIndex.getKeys();
			Set<String> countryKeys=geoCountryAdPodIndex.getKeys();
			Set<String> regionKeys=adpodGeoRegionIndex.getKeys();
			Set<String> cityKeys=adpodGeoCityIndex.getKeys();
			Set<String> dmaCodeKeys=adpodGeoDmacodeIndex.getKeys();
			Set<String> areaCodeKeys=adpodGeoAreacodeIndex.getKeys();
			Set<String> zipCodeKeys=adpodGeoZipcodeIndex.getKeys();

			Iterator it=urlKeys.iterator();
			String url=null;
			SortedSet<AdPodHandle> values=null;
			AdPodHandle adPodHandle=null;
			Iterator valuesIt=null;
			OSpec oSpec=null;
			List<String> urlList=new ArrayList<String>();
			while(it.hasNext()) {
				url=(String)it.next();
				urlList.add(url);
			}
			Collections.sort(urlList);
			for(int j=0;j<urlList.size();j++) {
				url=(String)urlList.get(j);
				values=urlAdPodIndex.get(url);
				valuesIt=values.iterator();
				List<String> geoList=new ArrayList<String>();
				Iterator geoIt=null;
				String geoStr=null;
				SortedSet<AdPodHandle> geoVal=null;
				while(valuesIt.hasNext()) {
					adPodHandle=(AdPodHandle)valuesIt.next();
					geoList.clear();

					geoIt=countryKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=geoCountryAdPodIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);
						}
						geoList.add(geoStr);
					}

					geoIt=regionKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=adpodGeoRegionIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);		
						}
					}

					geoIt=cityKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=adpodGeoCityIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);		
						}
					}

					geoIt=dmaCodeKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=adpodGeoDmacodeIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);		
						}
					}

					geoIt=areaCodeKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=adpodGeoAreacodeIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);		
						}
					}

					geoIt=zipCodeKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=adpodGeoZipcodeIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);		
						}
					}

					try{
						oSpec=campaignDB.getOSpecForAdPod(adPodHandle.getOid());
					}catch(Exception e){
						continue;
					}
					if (null == oSpec) continue;

					//Now print the data into html.
					out.print("<tr>");
					out.print("<td valign=\"top\">R</td>");
					out.print("<td valign=\"top\">"+url+"</td>");
					
					out.print("<td valign=\"top\"><table>");
					for(int i=0;i<geoList.size();i++) {
						out.print("<tr><td>"+((String)geoList.get(i))+"</td></tr>");
					}
					out.print("</table></td>");

					out.print("<td valign=\"top\"><a href=\"javascript:submitForm('"+oSpec.getName()+"')\">"+oSpec.getName()+"</td>");
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
			List<String> themeList=new ArrayList<String>();
			while(themeIt.hasNext()) {
				theme=(String)themeIt.next();
				themeList.add(theme);
			}
			Collections.sort(themeList);
			for(int j=0;j<themeList.size();j++) {
				theme=(String)themeList.get(j);
				values=themeAdPodIndex.get(theme);
				valuesIt=values.iterator();
				List<String> geoList=new ArrayList<String>();
				List<String> tspecList=new ArrayList<String>();
				Iterator geoIt=null;
				String geoStr=null;
				SortedSet<AdPodHandle> geoVal=null;
				while(valuesIt.hasNext()) {
					adPodHandle=(AdPodHandle)valuesIt.next();
					geoList.clear();

					geoIt=countryKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=geoCountryAdPodIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);
						}
						geoList.add(geoStr);
					}

					geoIt=regionKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=adpodGeoRegionIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);		
						}
					}

					geoIt=cityKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=adpodGeoCityIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);		
						}
					}

					geoIt=dmaCodeKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=adpodGeoDmacodeIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);		
						}
					}

					geoIt=areaCodeKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=adpodGeoAreacodeIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);		
						}
					}

					geoIt=zipCodeKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=adpodGeoZipcodeIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);		
						}
					}
					
					try{
						oSpec=campaignDB.getOSpecForAdPod(adPodHandle.getOid());
					}catch(Exception e){
						continue;
					}
					if (null==oSpec) continue;
					
					//Now print the data into html.
					out.print("<tr>");
					out.print("<td valign=\"top\">T</td>");
					out.print("<td valign=\"top\">"+theme+"</td>");

					out.print("<td valign=\"top\"><table>");
					for(int i=0;i<geoList.size();i++) {
						out.print("<tr><td>"+((String)geoList.get(i))+"</td></tr>");
					}
					out.print("</table></td>");

					out.print("<td valign=\"top\"><a href=\"javascript:submitForm('"+oSpec.getName()+"')\">"+oSpec.getName()+"</td>");
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
			List<Integer> locationList=new ArrayList<Integer>();
			while(locationIt.hasNext()) {
				location=(Integer)locationIt.next();
				locationList.add(location);
			}
			Collections.sort(locationList);
			for(int j=0;j<locationList.size();j++) {
				location=(Integer)locationList.get(j);
				values=themeAdPodIndex.get(location.intValue());
				valuesIt=values.iterator();
				List<String> geoList=new ArrayList<String>();
				List<String> tspecList=new ArrayList<String>();
				Iterator geoIt=null;
				String geoStr=null;
				SortedSet<AdPodHandle> geoVal=null;
				while(valuesIt.hasNext()) {
					adPodHandle=(AdPodHandle)valuesIt.next();
					geoList.clear();

					geoIt=countryKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=geoCountryAdPodIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);
						}
						geoList.add(geoStr);
					}

					geoIt=regionKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=adpodGeoRegionIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);		
						}
					}

					geoIt=cityKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=adpodGeoCityIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);		
						}
					}

					geoIt=dmaCodeKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=adpodGeoDmacodeIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);		
						}
					}

					geoIt=areaCodeKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=adpodGeoAreacodeIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);		
						}
					}

					geoIt=zipCodeKeys.iterator();
					while(geoIt.hasNext()) {
						geoStr=(String)geoIt.next();
						geoVal=adpodGeoZipcodeIndex.get(geoStr);
						if ((null !=geoVal) && (geoVal.contains(adPodHandle))) {
							geoList.add(geoStr);		
						}
					}
					
					try{
						oSpec=campaignDB.getOSpecForAdPod(adPodHandle.getOid());
					}catch(Exception e) {
						continue;
					}
					if (null==oSpec) continue;

					//Now print the data into html.
					out.print("<tr>");
					out.print("<td valign=\"top\">L</td>");
					out.print("<td valign=\"top\">"+location.toString()+"</td>");

					out.print("<td valign=\"top\"><table>");
					for(int i=0;i<geoList.size();i++) {
						out.print("<tr><td>"+((String)geoList.get(i))+"</td></tr>");
					}
					out.print("</table></td>");

					out.print("<td valign=\"top\"><a href=\"javascript:submitForm('"+oSpec.getName()+"')\">"+oSpec.getName()+"</td>");
					out.print("</tr>");
				}
			}

		%>
		</table>
		</form>
	</div>
</body>
</html>
