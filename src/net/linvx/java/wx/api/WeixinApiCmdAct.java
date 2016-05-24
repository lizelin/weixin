package net.linvx.java.wx.api;

/**
 * 对第三方开放的api：
 * 入口：http://wwww.**.com/wxnew/api/s.do?accountCode=CODE&cmdAct=[本enum类中定义的名称]
 * 
 * @author lizelin
 *
 */
public enum WeixinApiCmdAct {
	/**
	 * 获取以proxy模式跳转微信oauth2 url， 需要传递参数receiveCodeUrl
	 */
	getOAuth2RedirectUrlByProxy, 
	/**
	 * 获取跳转微信oauth2 url， 需要传递参数receiveCodeUrl
	 */
	getOAuth2RedirectUrl, 
	/**
	 * 根据code获取openid（返回的是json），需要传递参数：code
	 */
	getOpenIdByOAuth2Code, 
	/**
	 * 获取json结构的token
	 */
	getTokenJson, 
	/**
	 * 获取token字符串
	 */
	getTokenString, 
	/**
	 * 获取用户信息，需要传递参数：openId
	 */
	getUserInfo, 
	/**
	 * 创建菜单，菜单的json数据必须以post形似传入，POST数据要符合微信api的参数规范 
	 */
	createMenus, 
	/**
	 * 获取菜单的json，POST数据要符合微信api的参数规范
	 */
	getMenus, 
	/**
	 * 创建临时二维码，POST数据要符合微信api的参数规范
	 */
	createTempQrCode, 
	/**
	 * 创建永久二维码，POST数据要符合微信api的参数规范
	 */
	createLimitQrCode, 
	/**
	 * 长链接转短链接，POST数据要符合微信api的参数规范
	 */
	long2short, 
	/**
	 * jsapi签名，需要传递参数 signurl
	 */
	jsapiSign, 
	/**
	 * 发送48小时客服文本消息，POST数据要符合微信api的参数规范
	 */
	sendCustomTextMessage, 
	/**
	 * 发送48小时图文消息，POST数据要符合微信api的参数规范
	 */
	sendCustomNewMessage, 
	/**
	 * 发送模版消息，POST数据要符合微信api的参数规范
	 */
	sendTemplateMessage, 
}
