<%@ page language="java" import="com.tumri.joz.monitor.*" %>
<%@ page language="java" import="com.tumri.joz.campaign.*" %>
<%@ page language="java" import="com.tumri.joz.products.JOZTaxonomy" %>
<%@ page language="java" import="com.tumri.content.data.Category" %>
<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.tumri.cma.domain.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
	  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	  <title>Joz Console Products Page</title>
	  <style>
	    .floatingDiv{  position:absolute; border: solid 1px blue; display:none; width:250px; height:100px; }
	  </style>
	  <script>
	  function displayProductContent(productId, category, merchant, brand, price)
	  {
	     var elm = document.getElementById("productContent");
	     var val = 'Product Id =<strong>'+productId+'</strong>';
	     val = val + '<br> Category =<strong>'+category+'</strong>';
	     val = val + '<br> Merchant =<strong>'+merchant+'</strong>';
	     val = val + '<br> Brand =<strong>'+brand+'</strong>';
	     val = val + '<br> Price =<strong>'+price+'</strong>';
	     elm.innerHTML = val;
	     elm.style.top = "100px";
	     elm.style.left= "700px";
	     elm.style.display = "block";
	  };
	  
	  function hideProductContent()
	  {
	       var elm = document.getElementById("productContent");
	       elm.style.display = '';
	  };
	  </script>
  </head>
  <body>  
	<%CampaignDB campaignDB=CampaignDB.getInstance();
	  String tspecName = request.getParameter("selTspec");
      OSpec ospec = campaignDB.getOspec(tspecName);
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
	  String imageUrlPrefix="http://images.tumri.net/iCornerStore";
	  ProductQueryMonitor pqm=new ProductQueryMonitor();
	  List<Map<String,String>> products=pqm.getProducts(tspecName);
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
	  -->

	  <div id="top">
		<strong>Joz Console Ver 0.1</strong>
		<hr/>
		<div id="links" style="text-align: right">
			<a href="get-ad-data.jsp">back</a>
			<a href="console.jsp">home</a>
		</div>
	  </div>
	  <br>
	  <div id="links">
	  <table>
	  <tr style="text-align: center">
			<td>	
		  	<br>
		  	Products for Tspec: <strong><%=tspecName%></strong>
		  	<% 
		     	int dp=0;
		     	if (products.size() > 20) dp=20;
		     	else dp=products.size();
		  	%>
		  	[displaying <%=dp%> products out of a total of <%=products.size()%>]
		  	<br> <br> <br>
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
		     			for(int count=0;count<products.size();count++) {
		     				if(20==count) break;
		     				product=products.get(count);
							String productId=(String)product.get("id");
							String category=(String)product.get("display_category_name");
                        	category = category.replaceAll("&#39;", "\\\\\'");
							String brand=(String)product.get("brand");
							String price=(String)product.get("discount_price");
							String merchant=(String)product.get("merchant_id");
							String temp=(String)product.get("thumbnailraw");
							StringTokenizer attrToken=attrToken=new StringTokenizer(temp,"|");
							attrToken.nextToken();
							attrToken.nextToken();
							String thumbnailraw=attrToken.nextToken();
							if (0==count) out.print("<tr>");
							else if(count%4==0) out.print("</tr><tr>");
							out.print("<td><img border=\"1\" src=\""+imageUrlPrefix+"/"+thumbnailraw+"\" onMouseOver=\"javascript:displayProductContent('"+productId+"','"+category+"','"+merchant+"','"+brand+"','"+price+"');\" onMouseOut=\"javascript:hideProductContent()\";/></td>");
                
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
	  <div id="productContent"  class="floatingDiv" />
  </body>
</html>