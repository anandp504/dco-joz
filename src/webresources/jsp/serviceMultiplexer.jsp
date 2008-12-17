<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.tumri.cma.domain.*" %>
<%@ page language="java" import="com.tumri.joz.campaign.CampaignDB" %>
<%@ page language="java" import="com.tumri.utils.strings.StringTokenizer" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Joz Console : Service Multiplexer</title>
	<script type="text/javascript">
		function submitForm(adPodId) {
			var selOSpec=document.getElementById("selAdPod");
			selOSpec.value = adPodId;
			var form=document.getElementById("AdPodSelForm");
			form.submit();
		};
	</script>
</head>
<style type="text/css">
	.table
	{
		font-family: Verdana, Arial, Helvetica, sans-serif;
        border-collapse: collapse;
	}
	.table_row
	{
		background-color: #ffffff;
		font-family: verdana, arial, helvetica, sans-serif;
		font-size: 80%;
		white-space: nowrap;
	}
	.table_header
  {
      background-color: #A52A2A;
      padding: 1px;
      color: #FFFFFF;
      font-family: verdana, arial, helvetica, sans-serif;
      font-weight: bold;
      padding: 1px;
      text-decoration: none;
  }


  .table_column_header
  {
      background-color: #FF7F24;
      text-align: center;
      padding: 1px;
      font-family: verdana, arial, helvetica, sans-serif;
      font-weight: bold;
      color: #000000;
      text-align: center;
      padding: 1px;
      text-decoration: none;
      font-size: 90%;
  }

</style>
<body>

<jsp:include page="header.jsp"/>
<div id="homelink" style="text-align: right">
	<a href="/joz/console">home</a>
</div>
<br>

<div id="links">
	<strong>Service Multiplexer</strong>
</div>
<br>
<%
	CampaignDB campaignDB=CampaignDB.getInstance();
	ArrayList<AdPod> adPodList = campaignDB.getAdPods();
%>
<table class="table" border="1">
	<tr class="table_header">
		<th>AdPod</th>
		<th>Location</th>
		<th>AdPod-Geo Mapping</th>
		<th>AdPod-Url Mapping</th>
		<th>AdPod-ExternalTargetingVariable Mapping</th>
	</tr>
	<tr class="table_column_header">
		<th>Id, Name</th>
		<th>Id, Name</th>
		<th>Type, Value(s)</th>
		<th>Url</th>
		<th>Name, Value(s)</th>
	</tr>
	<%
		for(AdPod adPod: adPodList) {

			if (null==adPod) continue;

			//Now print the data into html.
	%>
	<tr valign="middle">
		<td>
			<table>
				<tr class="table_row" align="center" valign="middle">
					<td align="left">
						<a href="/joz/jsp/adPodSelection.jsp?selAdPod=<%=adPod.getId()%>"><%=adPod.getName()%></a>
					</td>
					<td align="right">
						<%=adPod.getId()%>
					</td>
				</tr>
			</table>
		</td>
		<%
			List<GeoAdPodMapping> geoMappings = adPod.getGeoAdPodMappings();
			List<UrlAdPodMapping> urlMappings = adPod.getAdpodUrls();
			List<Location> LocList = adPod.getLocations();
			List<AdPodExternalVariableMapping> etvList = adPod.getExternalTargetingVariableList();
		%>
		<td><table cellspacing="0" cellpadding="5" valign="top">
			<%
				if(LocList!=null){
					for(Location loc: LocList){
						if(loc != null){
			%><tr class="table_row" valign="top"><td align="left"><%=loc.getName()%></td><td align="right"><%=loc.getId()%></td></tr><%
					}
				}
			}
		%>
		</table></td>

		<td><table cellspacing="0" cellpadding="5" valign="top">
			<%
				if(geoMappings!=null){
					for(GeoAdPodMapping mapping: geoMappings){
						if(mapping == null){
							continue;
						}
						String geoType = mapping.getType();
						List<String> geoValue = mapping.getGeoValue();
						if(geoValue != null && geoValue.size()!=0){
			%><tr class="table_row" valign="top"><td rowspan="<%=geoValue==null?0:geoValue.size()%>"><%=geoType%>:</td><%
			String first = null;
			if(geoValue == null){
				geoValue = new ArrayList<String>();
			} else {
			    first = geoValue.get(0);
			}
			if(first != null){
				%><td><%=first%></td><%
			}
			%></tr><%
			int i = 0;
			for(String nt: geoValue){
				if(nt != null && i>0){
					%><tr class="table_row" valign="top"><td><%=nt%></td></tr><%
							}
							i++;
						}
					}
				}
			}
		%>
		</table></td>

		<td><table cellspacing="0" cellpadding="5" valign="top">
			<%
				if(urlMappings!=null){
					for(UrlAdPodMapping mapping: urlMappings){
						if(mapping == null){
							continue;
						}
						String urlName = mapping.getName();
						if(urlName != null){
							%><tr class="table_row" valign="top"><td><%=urlName%></td></tr><%
					}
				}
			}
		%>
		</table></td>

		<td><table cellspacing="0" cellpadding="5" valign="top">
			<%
				if(etvList!=null){
					for(AdPodExternalVariableMapping etv: etvList){
						if(etv==null){
							continue;
						}
						String etvName = etv.getName();
						String etvValue = etv.getValue();
						ArrayList<String> sList;
						if(etvValue != null && !"".equals(etvValue.trim())){
							StringTokenizer st = new StringTokenizer(etvValue,',');
							sList = st.getTokens();
			%><tr class="table_row" valign="top"><td rowspan="<%=sList==null?0:sList.size()%>"><%=etvName%>:</td><%
			String first = null;
			if(sList == null){
				sList = new ArrayList<String>();
			} else {
				first = sList.get(0);
			}
			if(first != null){
				%><td><%=first%></td><%
			}
			%></tr><%
			int i = 0;
			for(String nt: sList){
				if(nt != null && i>0){
					%><tr class="table_row" valign="top"><td><%=nt%></td></tr><%
							}
							i++;
						}
					}
				}

			}
		%>
		</table></td>
	</tr>
	<%
		}
	%>
</table>


</body>
</html>