<%@ page language="java" %>
<%@ page language="java" %>
<%@ page language="java" %>
<%@ page language="java" %>
<%@ page language="java" import="com.tumri.joz.campaign.CampaignDB" %>
<%@ page language="java" import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="com.tumri.cma.domain.*" %>
<%@ page import="com.tumri.utils.Pair" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Joz Console : Experience Information</title>

    <style>
        .floatingDiv {
            position: absolute;
            border: solid 1px blue;
            display: none;
            width: 325px;
            height: 300px;
        }
    </style>

    <script type="text/javascript">
        function submitTSpecForm() {
            var obj = document.getElementById("TSpecList");
            var selTspec = document.getElementById("selTSpec");
            selTspec.value = obj.options[obj.selectedIndex].value;
            var form = document.getElementById("TSpecSelForm");
            form.submit();
        }
        ;
        function submitCAMForm() {
            var obj = document.getElementById("RecipeList");
            var selTspec = document.getElementById("selRecipe");
            selTspec.value = obj.options[obj.selectedIndex].value;
            var form = document.getElementById("RecipeSelForm");
            form.submit();
        }
        ;
        function submitOSpecForm(oSpecId) {
            var selOSpec = document.getElementById("selOSpec");
            selOSpec.value = oSpecId;
            var form = document.getElementById("OSpecSelForm");
            form.submit();
        }
        ;

        function saveAdPod() {
            var perfForm = document.getElementById("SaveAdPod");
            perfForm.submit();
        }

    </script>
</head>
<body>
<jsp:include page="header.jsp"/>
<div id="links" style="text-align: right">
    <a href="/joz/console?mode=ad">get-ad-data</a>
    <a href="/joz/console">home</a>
</div>

<%
    //get AdPod from supplied adPodId
    CampaignDB campaignDB = CampaignDB.getInstance();
    String expId = request.getParameter("selExp");
    if (expId == null) {
        expId = (String) session.getAttribute("selExp");
    } else {
        session.setAttribute("selExp", expId);
    }

    Experience myExp = campaignDB.getExperience(Integer.parseInt(expId));
    CAM theCAM = myExp.getCam();
    List<ExperienceTSpecInfo> offers = myExp.getOfferLists();
    if (offers == null) {
        offers = new ArrayList<ExperienceTSpecInfo>();
    }
    List<ExperienceTSpecInfo> recipeTSpecInfo = myExp.getOfferLists();
    if (recipeTSpecInfo == null) {
        recipeTSpecInfo = new ArrayList<ExperienceTSpecInfo>();
    }
%>
<div>
    Experience Information
</div>
<br>

<div>
    <%
        out.print("<strong>Experience: Id</strong> = " + myExp.getId() + "<br>");
    %>
</div>
<br>

<div>
    <%
        out.print("<strong>Experience: Name</strong> = " + myExp.getName() + "<br>");
    %>
</div>
<br>

<div>
    <%
        out.print("<strong>Experience: AdType</strong> = " + myExp.getAdType() + "<br>");
    %>
</div>
<br>

<div>
    <%
        out.print("<strong>Experience: TemplateName</strong> = " + myExp.getTemplateName() + "<br>");
    %>
</div>
<br>

<div>
    <%
        out.print("<strong>Experience: Design</strong> = " + myExp.getDesign() + "<br>");
    %>
</div>
<br>

<div>
    <form id="TSpecSelForm" action="/joz/jsp/tSpecSelection.jsp" method="post">
        <div>
            <strong>Select TSpec (Name, ID, NumProds, SlotId)</strong> (<%=recipeTSpecInfo.size()%> TSpec(s)):
        </div>
        <select id="TSpecList">
            <%
                String isTSpecDisabled = "DISABLED";
                if (recipeTSpecInfo != null) {
                    for (int i = 0; i < recipeTSpecInfo.size(); i++) {
                        if (recipeTSpecInfo.get(i) == null) {
                            out.print("<option value=\"" + i + "\">" + recipeTSpecInfo.get(i) + "</option>");
                        } else {
                            TSpec tempTSpec = campaignDB.getTspec(recipeTSpecInfo.get(i).getTspecId());
                            if (tempTSpec == null) {
                                out.print("<option value=\"" + i + "\">" + tempTSpec + "</option>");
                            } else {
                                isTSpecDisabled = "";
                                out.print("<option value=\"" + tempTSpec.getId() + "\">" + tempTSpec.getName() + ", " + tempTSpec.getId() + ", " + recipeTSpecInfo.get(i).getNumProducts() + ", " + recipeTSpecInfo.get(i).getSlotId() + "</option>");
                            }

                        }

                    }
                }
            %>
        </select>
        <input type="hidden" name="selTSpec" id="selTSpec" value=""/>
        <input type="button" value="Get TSpec" onClick="javascript:submitTSpecForm() " <%=isTSpecDisabled%>/>
    </form>
</div>
<br>

<div>
    <%
        out.print("<strong>Experience: UIProperties</strong> : ");
        List<UIProperty> props = myExp.getProperties();
        if (props != null) {
            for (UIProperty prop : props) {
                String name = prop.getName();
                String value = prop.getValue();
                if (name != null && !name.equals("") && value != null && !value.equals("")) {
                    out.print(name + " = " + value + ",");
                }
                out.print("<br>");
            }
        }

    %>
</div>
<br>

<div>
    <%
        out.print("<strong>CAM Data: </strong>" + "<br><br>");
        CAMDimension[] dims = theCAM.getCamDimensions();
        ArrayList<String> dimNames = new ArrayList<String>();
        Map<String, ArrayList<Pair<String, String>>> dimInfo = new HashMap<String, ArrayList<Pair<String, String>>>();
        out.print("<table border=\"1\">");
        out.print("<caption>CAM Dimensions</caption>");
        out.print("<tr>");
        int largest = 0;
        for (CAMDimension dim : dims) {
            out.print(" <th colspan=\"2\">" + dim.getName() + "</th>");
            dimNames.add(dim.getName());
            List<CAMValue> values = dim.getCamValues();
            ArrayList<Pair<String, String>> tmpList = new ArrayList<Pair<String, String>>();
            int tmpLargest = 0;
            for (CAMValue v : values) {
                Pair<String, String> tmpPair = new Pair<String, String>(v.getIndex() + "", v.getValue());
                tmpList.add(tmpPair);
                tmpLargest++;
            }
            if (tmpLargest > largest) {
                largest = tmpLargest;
            }
            dimInfo.put(dim.getName(), tmpList);

        }
        out.print("</tr>");
        out.print("<tr>");
        for (int i = 0; i < dimNames.size(); i++) {
            out.print("<th>Index</th>");
            out.print("<th>Value</th>");
        }
        out.print("</tr>");
        for (int i = 0; i < largest; i++) {
            ArrayList<Pair<String, String>> row = new ArrayList<Pair<String, String>>();
            for (String dim : dimNames) {
                ArrayList<Pair<String, String>> tmpList = dimInfo.get(dim);
                if (i < tmpList.size()) {
                    row.add(tmpList.get(i));
                } else {
                    row.add(null);
                }
            }
            out.print("<tr>");
            for (Pair<String, String> v : row) {
                out.print("<td>");
                if (v != null) {
                    out.print(v.getFirst());
                }
                out.print("</td>");
                out.print("<td>");
                if (v != null) {
                    out.print(v.getSecond());
                }
                out.print("</td>");
            }
            out.print("</tr>");
        }
        out.print("</table><br>");
        out.println("<u>CAM Inclusions:</u><br>");

        List<String> inclusions = theCAM.getInclusions();
        for (String i : inclusions) {
            out.println(i + ",");
        }

        out.print("<br>");
        out.print("<br>");

        out.println("<u>CAM Exclusions:</u><br>");

        List<String> exclusions = theCAM.getExclusions();
        for (String i : exclusions) {
            out.println(i + ",");
        }

        out.print("<br>");

    %>
</div>
<br>

</body>
</html>
