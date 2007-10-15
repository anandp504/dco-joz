<%@ page language="java" import="com.tumri.joz.campaign.*" %>
<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.tumri.cma.domain.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
	  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	  <title>Joz Console : Get-ad-data</title>
	  <script type="text/javascript">
	  function submit1() {
		var obj=document.getElementById("tspecList");
		var selTspec=document.getElementById("selTspec");
		selTspec.value=obj.options[obj.selectedIndex].text;
		var form=document.getElementById("tspecSelForm");
		form.submit();
	  };
  	  </script>
  </head>
  <body>
	 <%CampaignDB campaignDB=CampaignDB.getInstance();
	  List<OSpec> oSpecList=campaignDB.getAllOSpecs();
        List<String> names = new ArrayList<String>();
	  for(int i=0;i<oSpecList.size();i++) {
          names.add(oSpecList.get(i).getName().trim());
	  }
        Collections.sort(names);
      %>
	  <div id="desc">
		<strong>Joz Console Ver 0.1</strong>
		<hr/>
		<div id="homelink" style="text-align: right">
			<a href="console.jsp">home</a>
		</div>
	  </div>
	  <br>
	  <div id="links">
		  <strong>get-ad-data</strong>
      </div>
	  <br>
      <div>
       total number of tspecs = <%=names.size()%> 
      </div>
      <br>
      <div>
		  <form id="tspecSelForm" action="products.jsp" method="post">
			  Select T-spec:
			  <select id="tspecList">
                               <%
				  for(int i=0;i<names.size();i++) {
					out.print("<option value=\""+i+"\">"+names.get(i)+"</option>");
				  }
				  %>
			  </select>
			  <input type="hidden"  name="selTspec"  id="selTspec" value=""/>
			  <input type="button" value="Get Products" onClick="javascript:submit1()";/>
		  </form>
	  </div>
  </body>
</html>
