<%@page import="net.linvx.java.libs.db.MyDbHelper"%>
<%@page import="java.sql.Connection"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
String A="test!";
net.linvx.java.libs.tools.MyLog.getLogger(Object.class).error(A);
/*Connection conn = MyDbHelper.getInstance().getConnection("wx");
String A = conn.toString();
conn.close();
conn = null;
conn = MyDbHelper.getInstance().getConnection("wx");
A = conn.toString();
conn.close();
conn = null;
net.linvx.java.libs.tools.MyLog.getLogger(Object.class).error(A);
*/
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<div><%=A %></div>
</body>
</html>