<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@page
	import="com.frameworkset.platform.security.AccessControl,com.frameworkset.platform.security.authorization.AccessException"%>

<%@page import="com.SynPortalUser"%>


<%
	//System.out.println(session.getSessionContext());	
	AccessControl accesscontroler = AccessControl.getInstance();
	accesscontroler.checkAccess(request, response);

	response.setHeader("Cache-Control", "no-cache");
	response.setHeader("Pragma", "no-cache");
	response.setDateHeader("Expires", -1);
	response.setDateHeader("max-age", 0);

	String doIt = request.getParameter("doIt");
	String message = "注意isSynPortalUser值设置为true，才会进行同步";
	if (doIt != null && doIt.equals("add")) {
		
		long totalBegin = System.currentTimeMillis();

		SynPortalUser.addUser();

		long totalEnd = System.currentTimeMillis();
		message = ("批量同步用户操作完毕,共耗时:"
				+ (totalEnd - totalBegin) + "ms");		
	}
%>
<html>
	<head>
		<script type="text/javascript">
			function addBatch(){	
				document.forms[0].submit();	
				document.getElementById('message').innerHTML = "";					
				document.getElementById('span').innerHTML = "批量同步用户开始!请等待...";						
			}
		</script>
	</head>
	<body>
		<form action="synPortalUser.jsp" method="post">
			<center>
			<div id="message">
				<%=message%>
			</div>
			
			<br>
			
			<div id="span">
				批量用户同步到Portal &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<input type="button" value="启动" onclick="addBatch();" />
				<input type="hidden" name="doIt" value="add">
			</div>
			</center>
		</form>
	</body>
</html>

