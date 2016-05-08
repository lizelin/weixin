<%@page import="net.sf.json.JSONObject"%>
<%@page import="net.linvx.java.libs.http.HttpHelper"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="net.linvx.java.libs.utils.MyStringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	String code = request.getParameter("code");
	String state = request.getParameter("state");
	String openid = (String)request.getSession().getAttribute("openid");
	if (MyStringUtils.isEmpty(openid) && MyStringUtils.isEmpty(code)) {
		String r = URLEncoder.encode("http://www.checkoo.com/wxnew/test/oauthtest.jsp");
		response.sendRedirect("http://www.checkoo.com/wxnew/api/s.do?accountCode=CODE&cmdAct=goOAuth2&receiveCodeUrl="+r);
		return;
	}
	if (MyStringUtils.isNotEmpty(code)) {
		String result = HttpHelper.httpGet("http://www.checkoo.com/wxnew/api/s.do?accountCode=CODE&cmdAct=getOpenIdByOAuth2Code&code="+code);
		JSONObject json = JSONObject.fromObject(result);
		openid = json.optString("openid");
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<div><%=openid %><br/><%=code %><br/><%=state %></div>
</body>
</html>