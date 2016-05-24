<%@page import="net.sf.json.JSON"%>
<%@page import="net.linvx.java.libs.http.HttpUrl"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="net.linvx.java.libs.http.HttpHelper"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="net.linvx.java.libs.utils.MyStringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	String openid = null, authdone = null, accountcode = null, code = null;
	accountcode = MyStringUtils.nvlString(request.getParameter("accountCode"), "CODE");
	
	// 先看是否有openid：
	openid = (String)request.getSession().getAttribute("openid");
	if (MyStringUtils.isNotEmpty(openid)) {
		// nothing to do;	
	}
	
	if (MyStringUtils.isEmpty(openid)) {
		authdone = request.getParameter("authdone");
		HttpUrl url = new HttpUrl(request);
		// 如果已经auth过了，则看看是否有code
		if (MyStringUtils.isNotEmpty(authdone) && "y".equalsIgnoreCase(authdone)) {
			code = request.getParameter("code");
			if (MyStringUtils.isEmpty(code)) {
				response.sendError(404);
				return;
			} else {
				String result = HttpHelper.httpGet(url.getSchemaHostPort() + "/" 
					+ request.getContextPath() + "/api/s.do?accountCode="+accountcode
					+"&cmdAct=getOpenIdByOAuth2Code&code="+code);
				if (MyStringUtils.isEmpty(result)) {
					response.sendError(404);
					return;
				} else {
					JSONObject json = JSONObject.fromObject(result);
					if (json!=null && MyStringUtils.isNotEmpty(json.optString("openid"))) {
						openid = json.optString("openid");
						request.getSession().setAttribute("openid", openid);
					}
				}
			}
		} else {
			// 没有auth过，则获取redirect
			String result = HttpHelper.httpGet(url.getSchemaHostPort() + "/" 
					+ request.getContextPath() + "/api/s.do?accountCode="+accountcode
					+"&cmdAct=getOAuth2RedirectUrlByProxy&receiveCodeUrl="+URLEncoder.encode(url.getUrlString()));
			response.sendRedirect(result);
			return;
		}
		
	}
	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<div>openid=<%=openid %><br/>|code=<%=code %><br/>|accountcode=<%=accountcode %><br/>|authdone=<%=authdone %></div>
</body>
</html>