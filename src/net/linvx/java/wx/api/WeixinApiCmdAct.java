package net.linvx.java.wx.api;

/**
 * 对第三方开放的api：
 * 入口：http://wwww.**.com/wxnew/api/s.do?accountCode=CODE&cmdAct=[本enum类中定义的名称]
 * 
 * @author lizelin
 *
 */
public enum WeixinApiCmdAct {
	goOAuth2Proxy, // 以proxy模式跳转微信oauth2 url， 需要传递参数receiveCodeUrl
	receiveOAuth2CodeProxy, // 以proxy模式跳转原始的接收oauth2 code参数的url，
							// 由微信服务器跳转，需要传递参数：redirect_uri_proxy
	goOAuth2, // 跳转微信oauth2 url， 需要传递参数receiveCodeUrl
	getOpenIdByOAuth2Code, // 根据code获取openid（返回的是json），需要传递参数：code
	getTokenJson, // 获取json结构的token
	getTokenString, // 获取token字符串
	getUserInfo, // 获取用户信息，需要传递参数：openId
	createMenus, // 创建菜单，菜单的json数据必须以post形似传入
	getMenus, // 获取菜单的json
	createTempQrCode, // 创建临时二维码：需要传递参数：expire_seconds和scene_id
	createLimitQrCode, // 创建永久二维码：需要传递参数：scene_id
	long2short, // 长链接转短链接， 传递参数：long_url
	jsapiSign, // jsapi签名，需要传递参数 signurl
	sendCustomTextMessage, // 发送48小时客服文本消息， 需要传递toUser 和 msg（post传递）
	sendCustomNewMessage, // 发送48小时图文消息，需要传递toUser 和 jsonarray（articles，
							// post方式传入）
	sendTemplateMessage, // 发送模版消息，需要传递json data，post方式
}
