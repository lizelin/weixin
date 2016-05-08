<%@page import="net.linvx.java.libs.utils.MyStringUtils"%>
<%@page import="net.linvx.java.wx.test.WxTest"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
String a = WxTest.test(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<div><%=MyStringUtils.encodeHtml(a) %></div>

</body>
</html>