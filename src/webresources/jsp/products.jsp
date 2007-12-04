<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.tumri.joz.monitor.*" %>
<%@ page language="java" import="com.tumri.joz.campaign.*" %>
<%@ page language="java" import="com.tumri.joz.products.JOZTaxonomy" %>
<%@ page language="java" import="com.tumri.content.data.Category" %>
<%@ page language="java" import="com.tumri.cma.domain.*" %>
<%@ page language="java" import="com.tumri.joz.utils.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
	  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	  <title>Joz Console : Products</title>
	  
	  <style>
	    .floatingDiv { position:absolute; border: solid 1px blue; display:none; width:325px; height:300px; }
	  </style>
	  
	  <script>
	  function displayProductContent(pid, dcn, desc, mid, brand, ot, ccode, price, dprice, sp, provider, name) {
	     var val='';
	     
	     var e=document.getElementById("pid");
	     if (e.checked) val=val+'id=<strong>'+pid+'</strong><br>';
	     
	     e=document.getElementById("dcn");
	     if (e.checked) val=val+'display_category_name=<strong>'+dcn+'</strong><br>';

	     e=document.getElementById("description");
	     if (e.checked) val=val+'description=<strong>'+desc+'</strong><br>';
	     
	     e=document.getElementById("mid");
	     if(e.checked) val=val+'merchant_id=<strong>'+mid+'</strong><br>';
	     
	     e=document.getElementById("brand");
	     if(e.checked) val=val+'brand=<strong>'+brand+'</strong><br>';
	     
	     e=document.getElementById("ot");
	     if(e.checked) val=val+'offer_type=<strong>'+ot+'</strong><br>';
	     
	     e=document.getElementById("ccode");
	     if(e.checked) val=val+'c_code=<strong>'+ccode+'</strong><br>';
	     
	     e=document.getElementById("price");
	     if(e.checked) val=val+'price=<strong>'+price+'</strong><br>';
	     
	     e=document.getElementById("dprice");
	     if(e.checked) val=val+'dicounted_price=<strong>'+dprice+'</strong><br>';

	     e=document.getElementById("sp");
	     if(e.checked) val=val+'ship_promo=<strong>'+sp+'</strong><br>';

	     e=document.getElementById("provider");
	     if(e.checked) val=val+'provider=<strong>'+provider+'</strong><br>';

	     e=document.getElementById("name");
	     if(e.checked) val=val+'name=<strong>'+name+'</strong><br>';
	     
	     if ( val=='') val='Select attributes to display values';
	     var elm = document.getElementById("productContent");	     
	     elm.innerHTML = val;
	     elm.style.top = "310px";
	     elm.style.left= "640px";
	     elm.style.display = "block";
	  };
	  
	  function hideProductContent() {
	       var elm = document.getElementById("productContent");
	       elm.style.display = '';
	  };
	  
	  function selAllAttr(sel) {
	  	var value=false;
	  	if(1 == sel) value=true;
	  	var e=document.getElementById("pid");
	  	e.checked=value;
	  	e=document.getElementById("dcn");
	  	e.checked=value;
	  	e=document.getElementById("description");
	  	e.checked=value;
	  	e=document.getElementById("mid");
	  	e.checked=value;
	  	e=document.getElementById("brand");
	  	e.checked=value;
	  	e=document.getElementById("ot");
	  	e.checked=value;
	  	e=document.getElementById("ccode");
	  	e.checked=value;
	  	e=document.getElementById("price");
	  	e.checked=value;
	  	e=document.getElementById("dprice");
	  	e.checked=value;
	  	e=document.getElementById("sp");
	  	e.checked=value;
	  	e=document.getElementById("provider");
	  	e.checked=value;
	  	e=document.getElementById("name");
	  	e.checked=value;
	  }
	  </script>
  </head>
  <body>
  	<jsp:include page="header.jsp"/>
  	<div id="links" style="text-align: right">
  		<a href="get-ad-data.jsp">get-ad-data</a>
  		<a href="console.jsp">home</a>
  	</div>

	<%
	CampaignDB campaignDB=CampaignDB.getInstance();
	String tspecName = request.getParameter("selTspec");
	if (tspecName == null)
	tspecName = (String)session.getAttribute("selTspec");
	else
	session.setAttribute("selTspec", tspecName);
	OSpec ospec = campaignDB.getOspec(tspecName);
	if (null == ospec) {
		out.print("T-spec <strong>"+tspecName+"</strong> not found.");
		return;
	}
	List<TSpec> tspecs = ospec.getTspecs();
	TSpec tspec = tspecs.get(0);
	List<BrandInfo> excludedBrands = tspec.getExcludedBrands();
	List<BrandInfo> includedBrands = tspec.getIncludedBrands();
	List<MerchantInfo> excludedMerchants = tspec.getExcludedMerchants();
	List<MerchantInfo> includedMerchants = tspec.getIncludedMerchants();
	List<CategoryInfo> excludedCategories = tspec.getExcludedCategories();
	List<CategoryInfo> includedCategories = tspec.getIncludedCategories();
	List<ProviderInfo> excludedProviders = tspec.getExcludedProviders();
	List<ProviderInfo> includedProviders = tspec.getIncludedProviders();
	List<ProductInfo> excludedProducts = tspec.getExcludedProducts();
	List<ProductInfo> includedProducts = tspec.getIncludedProducts();
	List<KeywordInfo> includedKeywords = tspec.getIncludedKeywords();
	String imageUrlPrefix="http://images.tumri.net/iCornerStore"; //Default pointing to US.
	String region=((AppProperties.getInstance()).getProperty("com.tumri.campaign.data.region.name")).trim();
	if (region.equals("UK")) {
		imageUrlPrefix="http://images-emea.tumri.net/iCornerStore";
	}
	%>

	<!-- Sample Product Data
		display_category_name Plants & Shrubs 
		merchant_id REDENVELOPE 
		picture_url |177|150|source-images/shopping.com/images/di/6c/38/53/495131755145336347484170747966377a6741-177x150-0-0.jpg 
		thumbnailraw |090|076|thumbnails/shopping.com/images/di/6c/38/53/495131755145336347484170747966377a6741-177x150-0-0.jpg 
		merchantlogo |219|045|logos/merchants/redenvelope-logo.gif 
		product_url http://stat.dealtime.com/DealFrame/DealFrame.cmp?BEFID=41187&acode=793&code=793&aon=^&crawler_id=424771&dealId=l8SIQ1uQE3cGHAptyf7zgA%3D%3D&prjID=ds&url=http%3A%2F%2Fre14.cpcmanager.com%2F197%2F%3F27808391&DealName=Large%20Money%20Tree&MerchantID=424771&HasLink=yes&frameId=0&category=&MT=nyc-pmt1-3&DB=sdcprod&MN=MT&AR=6&RR=1&NG=5&GR=6&ND=1&FPT=DSP&NDS=5&NMS=5&NDP=5&MRS=5&CT=6&linkin_id=8002028&DMT=205&VK=&searchID=2ad3f5352bec883cf0a2057d&IsFtr=0&IsSmart=0&crn=USD&istrsmrc=0&isathrsl=0&dlprc=81.95 
		id _1293.US5840260 
		brand RedEnvelope 
		offer_type Product 
		c_code USD 
		price 65.00 
		discount_price 65.00 
		description Large Money Tree 
		ship_promo  
		provider REDENVELOPE
		name Plants
	-->

	<div id="productattr" style="text-align: left">
		<p>Select product attributes for display:&nbsp;&nbsp;&nbsp;
		<input id="selectAll" name="selectAll" type="radio" onClick="javascript:selAllAttr(1);"/>Select All
		<input id="selectAll" name="selectAll" type="radio" onClick="javascript:selAllAttr(0);"/>De-select All
		</p>
		<table>
		<tr>
		<td><input id="pid" name="pid" type="checkbox"/>id</td>
		<td><input id="dcn" name="dcn" type="checkbox"/>display_category_name</td>
		<td><input id="description" name="description" type="checkbox"/>description</td>
		<td><input id="mid" name="mid" type="checkbox"/>merchant_id</td>
		</tr>
		<tr>
		<td><input id="brand" name="brand" type="checkbox"/>brand</td>
		<td><input id="ot" name="ot" type="checkbox"/>offer_type</td>
		<td><input id="ccode" name="ccode" type="checkbox"/>c_code</td>
		<td><input id="price" name="price" type="checkbox"/>price</td>
		</tr>
		<tr>
		<td><input id="dprice" name="dprice" type="checkbox"/>discount_price</td>
		<td><input id="sp" name="sp" type="checkbox"/>ship_promo</td>
		<td><input id="provider" name="provider" type="checkbox"/>provider</td>
		<td><input id="name" name="name" type="checkbox"/>name</td>
		</tr>
		</table>
	</div>
	
	<form id="productPageForm" name="productPageForm" method="post" action="products.jsp">
	<input id="pageAction" type="hidden" name="pageAction" value=""/>
	<input id="selTspec" type="hidden" name="selTspec" value="<%=tspecName%>"/>	
	<% 
	String pageAction=request.getQueryString();
	boolean prevPage=false;
	boolean nextPage=false;
	List<Map<String,String>> products=null;
	int tp=0;
	int dp=0;
	int startIndex=0;
	int prevPageStartIndex=0;
	int nextPageStartIndex=0;
	ProductQueryMonitorStatus pqmstat=null;
	ProductQueryMonitor pqm=new ProductQueryMonitor();
	pqmstat=(ProductQueryMonitorStatus)pqm.getStatus(tspecName);
	if (null == pageAction) {
		products=pqmstat.getProducts();
		tp=products.size();
		dp=0;
		if (tp > 20) {
			dp=20;
			prevPage=false;
			nextPage=true;
		}
		else dp=tp;
		session=request.getSession(true);
		session.setAttribute("PQMS",products);
		session.setAttribute("PPSI",new Integer(0));
		session.setAttribute("NPSI",new Integer(20));
	}
	if ("previous".equals(pageAction)) {
		prevPageStartIndex=(Integer)session.getAttribute("PPSI");
		nextPageStartIndex=(Integer)session.getAttribute("NPSI");
		products=(List<Map<String,String>>)session.getAttribute("PQMS");
		tp=products.size();
		nextPage=true;
		if ((prevPageStartIndex-20) <= 0) {
			prevPage=false;
			prevPageStartIndex=0;
			session.setAttribute("PPSI",new Integer(prevPageStartIndex));
			session.setAttribute("NPSI",new Integer(20));
		}
		else {
			prevPageStartIndex=prevPageStartIndex-20;
			prevPage=true;
			session.setAttribute("PPSI",new Integer(prevPageStartIndex));
			session.setAttribute("NPSI",new Integer(nextPageStartIndex-20));
		}
		startIndex=prevPageStartIndex;
		if(startIndex+20 >= tp) dp=tp;
		else dp=startIndex+20;
	}
	if ("next".equals(pageAction)) {
		prevPageStartIndex=(Integer)session.getAttribute("PPSI");
		nextPageStartIndex=(Integer)session.getAttribute("NPSI");
		products=(List<Map<String,String>>)session.getAttribute("PQMS");
		tp=products.size();
		prevPage=true;
		startIndex=nextPageStartIndex;
		if ((nextPageStartIndex+20) >= tp) {
			nextPage=false;
			dp=tp;
			session.setAttribute("PPSI",new Integer(prevPageStartIndex+20));
			session.setAttribute("NPSI",new Integer(nextPageStartIndex+20));
		}
		else {
			session.setAttribute("PPSI",new Integer(prevPageStartIndex+20));
			session.setAttribute("NPSI",new Integer(nextPageStartIndex+20));
			dp=startIndex+20;
			nextPage=true;
		}
	}

	/* Code to display all the product attributes coming from joz server.
	// Enable only for debug
	Map<String,String> p=products.get(0);
	Set keySet=p.keySet();
	Iterator i2=keySet.iterator();
	while(i2.hasNext()){
		String key=(String)i2.next();
		String value=(String)p.get(key);
		out.print(key+":"+value+"<br />");
	}*/
	%>
	<table>
	<tr style="text-align: center">
	<td>	
	<br>
	Products for Tspec(search is limited to <%=tp%>): <strong><%=tspecName%></strong>
	[displaying <%=(tp==0)?startIndex:startIndex+1%>-<%=dp%> out of <%=tp%>] 
	<% 
	   if (prevPage) {
			out.print("&nbsp; <a href=\"products.jsp?previous\">previous</a> "); 
	   }
	   if (nextPage) {
			out.print("&nbsp; <a href=\"products.jsp?next\">next</a>"); 
	   }
	%>
	<br>
	</td>
	</tr>
	<tr>	 
	<table>
		<tr>
			<td valign="top" >
				<% 
				 out.print("<br>");
			 if (includedBrands != null) {
						out.print("<strong>Included Brands:</strong>");
						out.print("<br>");
						for (int i=0; i<includedBrands.size(); i++) {
							out.print("&nbsp;&nbsp;&nbsp;"+includedBrands.get(i).getName());
							out.print("<br>");
						}
						out.print("<br>");
				 }
				 if (excludedBrands != null) {		
					out.print("<strong>Excluded Brands:</strong>");
					out.print("<br>");
					for (int i=0; i<excludedBrands.size(); i++) {
						out.print("&nbsp;&nbsp;&nbsp;"+excludedBrands.get(i).getName());
						out.print("<br>");
					}
					out.print("<br>");
				  }
			      if (includedMerchants != null) {
					out.print("<strong>Included Merchants:</strong>");
					out.print("<br>");
					for (int i=0; i<includedMerchants.size(); i++) {
						out.print("&nbsp;&nbsp;&nbsp;"+includedMerchants.get(i).getName());
						out.print("<br>");
					}
					out.print("<br>");
				  }
				  if (excludedMerchants != null) {		
					out.print("<strong>Excluded Merchants:</strong>");
					out.print("<br>");
					for (int i=0; i<excludedMerchants.size(); i++) {
						out.print("&nbsp;&nbsp;&nbsp;"+excludedMerchants.get(i).getName());
						out.print("<br>");
					}
					out.print("<br>");
				  }
			      if (includedCategories != null) {
					out.print("<strong>Included Categories:</strong>");
					out.print("<br>");
					for (int i=0; i<includedCategories.size(); i++) {
						Category cat = null;
						String displayName = includedCategories.get(i).getDisplayName();
						try {
							cat = JOZTaxonomy.getInstance().getTaxonomy().getCategory(displayName);
						    displayName = cat.getName();
						} 
						catch (NullPointerException npe) {
							// do nothing
						}
						out.print("&nbsp;&nbsp;&nbsp;"+displayName);
						out.print("<br>");
					}
					out.print("<br>");
				  }
				  if (excludedCategories != null) {		
					out.print("<strong>Excluded Categories:</strong>");
					out.print("<br>");
					for (int i=0; i<excludedCategories.size(); i++) {
						Category cat = null;
						String displayName = excludedCategories.get(i).getDisplayName();
						try {
							cat = JOZTaxonomy.getInstance().getTaxonomy().getCategory(displayName);
						    displayName = cat.getName();
						} 
						catch (NullPointerException npe) {
							// do nothing
						}
						out.print("&nbsp;&nbsp;&nbsp;"+displayName);
						out.print("<br>");
					}
					out.print("<br>");
				  }
			      if (includedProviders != null) {
					out.print("<strong>Included Providers:</strong>");
					out.print("<br>");
					for (int i=0; i<includedProviders.size(); i++) {
						out.print("&nbsp;&nbsp;&nbsp;"+includedProviders.get(i).getName());
						out.print("<br>");
					}
					out.print("<br>");
				  }
				  if (excludedProviders != null) {		
					out.print("<strong>Excluded Providers:</strong>");
					out.print("<br>");
					for (int i=0; i<excludedProviders.size(); i++) {
						out.print("&nbsp;&nbsp;&nbsp;"+excludedProviders.get(i).getName());
						out.print("<br>");
					}
					out.print("<br>");
				  }
			      if (includedProducts != null) {
					out.print("<strong>Included Products:</strong>");
					out.print("<br>");
					for (int i=0; i<includedProducts.size(); i++) {
						out.print("&nbsp;&nbsp;&nbsp;"+includedProducts.get(i).getName());
						out.print("<br>");
					}
					out.print("<br>");
				  }
				  if (excludedProducts != null) {		
					out.print("<strong>Excluded Products:</strong>");
					out.print("<br>");
					for (int i=0; i<excludedProducts.size(); i++) {
						out.print("&nbsp;&nbsp;&nbsp;"+excludedProducts.get(i).getName());
						out.print("<br>");
					}
				  }
			      if (includedKeywords != null) {
					out.print("<strong>Included Keywords:</strong>");
					out.print("<br>");
					for (int i=0; i<includedKeywords.size(); i++) {
						out.print("&nbsp;&nbsp;&nbsp;"+includedKeywords.get(i).getName());
						out.print("<br>");
					}
					out.print("<br>");
				  }
			     %>
			</td>
			<td width="10">
			</td>
			<td>
				<table>
				<%
				Map<String,String> product=null;
				int count=0;
				for(int i=startIndex;;i++,count++) {
					if(i==tp) break;
					if(20==count) break;
					product=products.get(i);

					String pid=(String)product.get("id");
					String dcn=(String)product.get("display_category_name");
					dcn=dcn.replaceAll("&#39;", "\\\\\'");
					String desc=(String)product.get("description");
					desc=desc.replaceAll("&#39;", "\\\\\'");
					desc=desc.replaceAll("\"","\\\\\"");
					desc=desc.replaceAll("\"","\\\\\'");
					String mid=(String)product.get("merchant_id");

					String brand=(String)product.get("brand");
					String ot=(String)product.get("offer_type");
					String ccode=(String)product.get("c_code");
					String price=(String)product.get("price");

					String dprice=(String)product.get("discount_price");
					String sp=(String)product.get("ship_promo");
					String provider=(String)product.get("provider");
					String name=(String)product.get("name");
					name=name.replaceAll("&#39;", "\\\\\'");
					name=name.replaceAll("\"","\\\\\"");
					name=name.replaceAll("\'","\\\\\'");

					String temp=(String)product.get("thumbnailraw");
					StringTokenizer attrToken=attrToken=new StringTokenizer(temp,"|");
					attrToken.nextToken(); //Remove first integer.
					attrToken.nextToken(); //Remove second integer.
					String thumbnailraw=attrToken.nextToken(); //Thumbnail Image url

					if (0==count) out.println("<tr>");
					else if(count%4==0) out.println("</tr><tr>");
					out.println("<td><img border=\"1\" src=\""+imageUrlPrefix+"/"+thumbnailraw+"\" onMouseOver=\"javascript:displayProductContent('"+pid+"','"+dcn+"','"+desc+"','"+mid+"','"+brand+"','"+ot+"','"+ccode+"','"+price+"','"+dprice+"','"+sp+"','"+provider+"','"+name+"');\" onMouseOut=\"javascript:hideProductContent();\"/></td>");
				}
				%>
				</tr>
				</table>
			</td>
		</tr>
	</table>
	</tr>
	</table>
	</div>
	</form>
	
	<div id="debug_info" width="500px" border="1">
		<p>get-ad-data debug info:&nbsp;&nbsp;&nbsp;</p>
		<table border="1" cellspacing="0" cellpading="0" width="100%">
		<tr>
		<td width="20%" valign="top">input string</td>
		<td width="80%" valign="top"><%=pqmstat.getProductQuery()%></td>
		</tr>
		<tr>
		<td width="20%" valign="top">output string</td>
		<td width="80%" valign="top"><textarea readonly="readonly" rows=50" cols="100"><%=pqmstat.getProductRawData()%></textarea></td>
		</tr>
		</table>
	</div>
	<div id="productContent"  class="floatingDiv" />
  </body>
</html>
