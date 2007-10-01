<%@ page language="java" import="com.tumri.joz.monitor.*" %>
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
	  function displayProductContent(productId)
	  {
	     var elm = document.getElementById("productContent");
	     var val = '<strong>'+productId+'</strong>';
	     elm.innerHTML = val;
	     elm.style.top = "100px";
	     elm.style.left= "600px";
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
	  </br>
	  <div id="links">
		  Products for Tspec : <strong><%=request.getParameter("selTspec")%></strong>
		  <% 
		  	String imageUrlPrefix="http://images.tumri.net/iCornerStore";
		  	ProductQueryMonitor pqm=new ProductQueryMonitor();
		     	List<Map<String,String>> products=pqm.getProducts(request.getParameter("selTspec"));
		     	int tp=products.size();
		     	int dp=0;
		     	if (tp > 20) dp=20;
		     	else dp=tp;
			/*Map<String,String> product1=null;
			for(int i=0;i<products.size();i++) {
				product1=products.get(i);
				Set keySet1=product1.keySet();
				Iterator keys1=keySet1.iterator();
				String key1=null;
				String value1=null;
				out.print("<table>");
				while(keys1.hasNext()) {
					key1=(String)keys1.next();
					value1=(String)product1.get(key1);
					out.print("<tr><td>"+key1+"</td><td>"+value1+"</td></tr>");
				}
				out.print("</table>");

			}*/
		  %>
		  [<%=dp%> out of <%=tp%>]
		  </br></br></br>
		  <table>
		  <%
		     	Map<String,String> product=null;
		     	int count=0;
		     	for(int i=0;i<products.size();i++) {
		     		if(20==count) break;
		     		product=products.get(i);
				String productId=(String)product.get("id");
				String temp=(String)product.get("thumbnailraw");
				StringTokenizer attrToken=attrToken=new StringTokenizer(temp,"|");
				attrToken.nextToken();
				attrToken.nextToken();
				String thumbnailraw=attrToken.nextToken();
				if (0==count) out.print("<tr>");
				else if(count%4==0) out.print("</tr><tr>");
				out.print("<td><img border=\"1\" src=\""+imageUrlPrefix+"/"+thumbnailraw+"\" onMouseOver=\"javascript:displayProductContent('"+productId+"');\" onMouseOut=\"javascript:hideProductContent()\";/></td>");
			}
		  %>
		  </tr>
		  </table>
		  </br>
	  </div>
	  <div id="productContent"  class="floatingDiv" />
  </body>
</html>
