<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	String oriurl = request.getParameter("redirect_uri_proxy");
	if (net.linvx.java.libs.utils.MyStringUtils.isEmpty(oriurl)) {
		response.sendError(404);
		return;
	}
	String code = request.getParameter("code");
	if (net.linvx.java.libs.utils.MyStringUtils.isEmpty(code)) {
		response.sendError(404);
		return;
	}
	oriurl = net.linvx.java.libs.http.HttpUrl.addParam(oriurl, "code", code);
	oriurl = net.linvx.java.libs.http.HttpUrl.addParam(oriurl, "authdone", "y");
	
	response.sendRedirect(oriurl);
%>