<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.tumri.joz.monitor.*" %>
<%@ page language="java" import="com.tumri.joz.campaign.*" %>
<%@ page language="java" import="com.tumri.content.data.Category" %>
<%@ page language="java" import="com.tumri.cma.domain.*" %>
<%@ page language="java" import="com.tumri.joz.utils.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Joz Console : Index Debug Util</title>

	<style>
		.floatingDiv { position:absolute; border: solid 1px blue; display:none; width:325px; height:300px; }
	</style>

</head>
<body>
<jsp:include page="header.jsp"/>
<div id="links" style="text-align: right">
    <a href="/joz/console">home</a>
</div>
<%
	String commandLine = request.getParameter("indexDebug");
	boolean homeFlag = false;
	boolean saveDirFlag = false;
	boolean saveFileFlag = false;

	if (commandLine == null){
		commandLine = (String)session.getAttribute("indexDebug");
	} else {
		session.setAttribute("indexDebug", commandLine);
	}

	ArrayList<String>commandLineAL = new ArrayList<String>();

	StringTokenizer commandLineTokenizer = new StringTokenizer(commandLine);
	while(commandLineTokenizer.hasMoreTokens()){
		String tempArg = commandLineTokenizer.nextToken();
		if(tempArg.equals("console")){
			tempArg = "";
			commandLine = "";
			homeFlag = true;
		} else if(tempArg.equals("-saveDir")){
			saveDirFlag = true;
		} else if(tempArg.equals("-saveFile")){
			saveFileFlag = true;
		}
		commandLineAL.add(tempArg);
	}

%>
Index Debuging Util: <i>View Product Listings Accross *.bin Files<i>
<div>
	<br>
	<div>
		<form id="indexDebugInputForm" action="/joz/jsp/indexDebug.jsp" method="post">
			<strong>Input Index Debugging Command Line:</strong>
			<input type="text"  name="indexDebug"  id="indexDebugInput" value="<%=commandLine%>" size="75"/>
			<input type="submit" value="Get DebugInfo"/>
			<br>
			<br>
			<div>
				<strong>Input should follow the syntax below:</strong>

				<br>
				&nbsp; &nbsp; <i>Example:</i> "<strong>-saveDir</strong> /tmp/tmp2 <strong>-saveFile</strong> tmp.txt <strong>-binFile</strong> hello.bin <strong>-binFile</strong> bye.bin <strong>-binLoc</strong> /bin/bin2 <strong>-prodId</strong> 123456 <strong>-prodId</strong> 654321 <strong>-opt</strong>"
			</div>
			<div style="font-size:85%">

				&nbsp; &nbsp; &nbsp; &nbsp; If you omit any field a Default value will be assigned as follows:
				<br>
				&nbsp; &nbsp; &nbsp; &nbsp; If no bin directory is given via <strong>-binLoc</strong>, the default directory will be <strong>/opt/Tumri/joz/data/caa/current</strong>
				<br>
				&nbsp; &nbsp; &nbsp; &nbsp; If no bin Files are specified via <strong>-binFile</strong>, data will be collected across all *.bin files located in <strong>binLoc</strong>
				<br>
				&nbsp; &nbsp; &nbsp; &nbsp; If no Product IDs are specified via <strong>-prodId</strong>, data will be collected across all available Product IDs
				<br>
				&nbsp; &nbsp; &nbsp; &nbsp; If only a Save File, <strong>-saveFile</strong>, and no save Directory, <strong>-saveDir</strong>, is specified via, data will be saved to <strong>/tmp</strong>
				<br>
				&nbsp; &nbsp; &nbsp; &nbsp; If only a Save Directory, <strong>-saveDir</strong>, and no save File, <strong>-saveFile</strong>, is specified, data will be saved to <strong>jozIndexDebugFile.txt</strong>
				<br>
				&nbsp; &nbsp; &nbsp; &nbsp; If no Save File and no Save Directory are specified, data will be written <strong>Below</strong> to this <strong>Console</strong>
				<br>
				&nbsp; &nbsp; &nbsp; &nbsp; -opt indicates we are attempting to debug an optization index. <strong>Below</strong> to this <strong>Console</strong>
				<br>
			</div>
		</form>
	</div>
</div>
<br>
<strong>Compiled Data:</strong>
<br>
<%
	if(!homeFlag){
		String[] args = new String[commandLineAL.size()];
		for (int i = 0; i < commandLineAL.size(); i++){
			args[i] = commandLineAL.get(i).trim();
			//For Debuging: out.print("args[" + i + "] = " + args[i] + "<br>");
		}
		IndexDebugUtils debugUtil = new IndexDebugUtils();
		StringBuffer debugBuff = debugUtil.execute(args);
		if(saveDirFlag || saveFileFlag){
			out.print("<i>Data Written to File<i>");
		} else {
			out.print(debugBuff);
		}
	}

%>


</body>
</html>
