package net.linvx.java.wx.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import net.linvx.java.libs.http.HttpUrl;
import net.linvx.java.libs.tools.MyLog;
import net.linvx.java.libs.utils.MyStringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 微信api的入口servlet，包括调用微信服务器的api以及oauth2、jsapi参数获取等
 * 必须传入两个参数：accountCode：代表哪个公众号；cmdAct：代表请求的具体指令
 * /api/s.do
 * @author lizelin
 *
 */
public class WeixinApiServlet extends HttpServlet {
	
	private static final long serialVersionUID = 7461167829576602905L;
	private static Logger log = MyLog.getLogger(WeixinApiServlet.class);
	
	/**
	 * 执行成功时返回的json字符串：{"errcode":"0", errmsg:"ok"}
	 */
	private static String success = new ApiException.ApiResult("0", "success").toJsonString();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.info("request url: " + new HttpUrl(req).getUrlString());
		String accountCode = req.getParameter("accountCode");
		if (MyStringUtils.isEmpty(accountCode)) {
			this.writeAndLogException(resp, new ApiException("accountCode param is must pass to this api servlet!"));
			return;
		}
		
		String cmdAct = req.getParameter("cmdAct");
		if (MyStringUtils.isEmpty(cmdAct)) {
			this.writeAndLogException(resp, new ApiException("cmdAct param is must pass to this api servlet!"));
			return;
		}

		WeixinApiImpl api = WeixinApiImpl.createApiToWxByAccountCode(accountCode);
		String reqData = ApiUtils.getRequestData(req);
		log.info("request data: " + reqData);
		
		if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.getOAuth2RedirectUrlByProxy.name())) {
			this.getOAuth2RedirectUrlByProxy(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.getOAuth2RedirectUrl.name())) {
			this.getOAuth2RedirectUrl(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.getOpenIdByOAuth2Code.name())) {
			this.getOpenIdByOAuth2Code(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.getTokenJson.name())) {
			this.getTokenJson(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.getTokenString.name())) {
			this.getTokenString(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.getUserInfo.name())) {
			this.getUserInfo(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.getMenus.name())) {
			this.getMenus(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.createMenus.name())) {
			this.createMenus(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.createTempQrCode.name())) {
			this.createTempQrCode(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.createLimitQrCode.name())) {
			this.createLimitQrCode(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.long2short.name())) {
			this.long2short(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.jsapiSign.name())) {
			this.jsapiSign(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.sendCustomTextMessage.name())) {
			this.sendCustomTextMessage(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.sendCustomNewMessage.name())) {
			this.sendCustomNewsMessage(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.sendTemplateMessage.name())) {
			this.sendTemplateMessage(req, resp, api, reqData);
		}

	}

	/**
	 * 发送模版消息，post过来的数据（reqData）需要是完整的json数据，按照微信api接口规范
	 * @param req
	 * @param resp
	 * @param api
	 * @param reqData
	 */
	private void sendTemplateMessage(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api,
			String reqData) {
		String msg = reqData;
		if (MyStringUtils.isEmpty(msg)) {
			this.writeAndLogException(resp, new ApiException("msg must not null!"));
			return;
		}
		JSONObject json = JSONObject.fromObject(msg);
		try {
			String data = api.sendTemplateMessage(json.toString());
			this.writeResp(resp, data);
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		}
	}

	/**
	 * 发送客服图文消息，post过来的数据（reqData）需要是完整的json数据，按照微信api接口规范
	 * @param req
	 * @param resp
	 * @param api
	 * @param reqData
	 */
	private void sendCustomNewsMessage(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api,
			String reqData) {
		String msg = reqData;
		if (MyStringUtils.isEmpty(msg)) {
			this.writeAndLogException(resp, new ApiException("msg must not null!"));
			return;
		}
		//"touser":"OPENID", "msgtype":"news"
		JSONObject json = JSONObject.fromObject(msg);
		String toUser = json.optString("touser");
		String type = json.optString("msgtype");
		if (MyStringUtils.isEmpty(toUser)) {
			this.writeAndLogException(resp, new ApiException("touser must pass to this api servlet in post data!"));
			return;
		}
		if (MyStringUtils.isEmpty(type) || !type.equals("news")) {
			this.writeAndLogException(resp, new ApiException("msgtype must pass to this api servlet in post data!"));
			return;
		}
		try {
			api.sendCustomNewsMessage(msg);
			this.writeResp(resp, success);
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		}

	}

	/**
	 * 发送客服文本消息，post过来的数据（reqData）需要是完整的json数据，按照微信api接口规范
	 * @param req
	 * @param resp
	 * @param api
	 * @param reqData
	 */
	private void sendCustomTextMessage(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api,
			String reqData) {
		//  "touser":"OPENID", "msgtype":"text",
		String msg = reqData;
		if (MyStringUtils.isEmpty(msg)) {
			this.writeAndLogException(resp, new ApiException("msg must not null!"));
			return;
		}
		
		JSONObject json = JSONObject.fromObject(msg);
		String toUser = json.optString("touser");
		String type = json.optString("msgtype");
		if (MyStringUtils.isEmpty(toUser)) {
			this.writeAndLogException(resp, new ApiException("touser must pass to this api servlet in post data!"));
			return;
		}
		if (MyStringUtils.isEmpty(type) || !type.equalsIgnoreCase("text")) {
			this.writeAndLogException(resp, new ApiException("msgtype must pass to this api servlet in post data!"));
			return;
		}
		try {
			api.sendCustomTextMessage(msg);
			this.writeResp(resp, success);
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		}
	}

	/**
	 * 获取jsapi签名信息， post数据中或者url get参数需要包含signurl参数
	 * @param req
	 * @param resp
	 * @param api
	 * @param reqData
	 */
	private void jsapiSign(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
		String signurl = req.getParameter("signurl");
		if (MyStringUtils.isEmpty(signurl)) {
			this.writeAndLogException(resp, new ApiException("signurl must pass to this api servlet!"));
			return;
		}
		try {
			String data = api.jsapiSign(signurl).toString();
			this.writeResp(resp, data);
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		}
	}

	/**
	 * 长网址转短网址，post过来的数据（reqData）需要是完整的json数据，按照微信api接口规范
	 * @param req
	 * @param resp
	 * @param api
	 * @param reqData
	 */
	private void long2short(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
		//"action":"long2short","long_url"
		String msg = reqData;
		if (MyStringUtils.isEmpty(msg)) {
			this.writeAndLogException(resp, new ApiException("msg must not null!"));
			return;
		}
		JSONObject json = JSONObject.fromObject(msg);
		String action = json.optString("action");
		String long_url = json.optString("long_url");
		if (MyStringUtils.isEmpty(long_url) || MyStringUtils.isEmpty(action) || !"long2short".equals(action)) {
			this.writeAndLogException(resp, new ApiException("long_url or action must pass to this api servlet!"));
			return;
		}
		try {
			String data = api.longUrl2short(msg);
			this.writeResp(resp, data);
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		} 

	}

	/**
	 * 创建临时二维码，post过来的数据（reqData）需要是完整的json数据，按照微信api接口规范
	 * {"expire_seconds": 604800, "action_name": "QR_SCENE", "action_info": {"scene": {"scene_id": 123}}}
	 * @param req
	 * @param resp
	 * @param api
	 * @param reqData
	 */
	private void createTempQrCode(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
		String msg = reqData;
		if (MyStringUtils.isEmpty(msg)) {
			this.writeAndLogException(resp, new ApiException("msg must not null!"));
			return;
		}
		JSONObject json = JSONObject.fromObject(msg);
		String expire_seconds = json.optString("expire_seconds");
		String action_name = json.optString("action_name");
		if (MyStringUtils.isEmpty(expire_seconds) || MyStringUtils.isEmpty(action_name) || !"QR_SCENE".equals(action_name)) {
			this.writeAndLogException(resp, new ApiException("msg params incorrect!"));
			return;
		}
		try {
			String data = api.createTempQrCode(reqData);
			this.writeResp(resp, data);
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		}
	}

	/**
	 * 创建永久二维码，post过来的数据（reqData）需要是完整的json数据，按照微信api接口规范
	 * {"action_name": "QR_LIMIT_SCENE", "action_info": {"scene": {"scene_id": 123}}}
	 * @param req
	 * @param resp
	 * @param api
	 * @param reqData
	 */
	private void createLimitQrCode(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api,
			String reqData) {
		String msg = reqData;
		if (MyStringUtils.isEmpty(msg)) {
			this.writeAndLogException(resp, new ApiException("msg must not null!"));
			return;
		}
		JSONObject json = JSONObject.fromObject(msg);
		int scene_id = json.optJSONObject("action_info").optJSONObject("scene").optInt("scene_id");
		String action_name = json.optString("action_name");
		if (scene_id <= 0 || scene_id > 100000) {
			this.writeAndLogException(resp, new ApiException("scene_id must < 100000!"));
			return;
		}
		if (MyStringUtils.isEmpty(action_name) || !action_name.equals("QR_LIMIT_SCENE")) {
			this.writeAndLogException(resp, new ApiException("action_name incorrect!"));
			return;
		}
		try {
			String data = api.createLimitQrCode(msg);
			this.writeResp(resp, data);
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		}
	}

	/**
	 * 创建菜单：，post过来的数据（reqData）需要是完整的json数据，按照微信api接口规范
	 * @param req
	 * @param resp
	 * @param api
	 * @param reqData
	 */
	private void createMenus(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
		if (MyStringUtils.isEmpty(reqData)) {
			this.writeAndLogException(resp, new ApiException("reqData(is null) must pass to this api servlet!"));
			return;
		}
		JSONObject json = JSONObject.fromObject(reqData);
		if (json == null) {
			this.writeAndLogException(resp, new ApiException("reqData(is not json) must pass to this api servlet!"));
			return;
		}
		try {
			api.createMenus(json.toString());
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		}
		this.writeResp(resp, success);

	}

	/**
	 * 获取菜单
	 * @param req
	 * @param resp
	 * @param api
	 * @param reqData null
	 */
	private void getMenus(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
		String data = "";
		try {
			data = api.getMenus().toString();
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		}
		this.writeResp(resp, data);
	}

	/**
	 * 获取用户信息，openId通过get方式传入
	 * @param req
	 * @param resp
	 * @param api
	 * @param reqData
	 */
	private void getUserInfo(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
		String openId = req.getParameter("openId");
		if (MyStringUtils.isEmpty(openId)) {
			this.writeResp(resp, new ApiException("openId param is must pass to this api servlet!").toString());
			return;
		}
		String ret = "";
		try {
			ret = api.getUserInfo(openId).toString();
		} catch (ApiException e) {
			log.error("", e);
			e.printStackTrace();
			this.writeResp(resp, e.toString());
			return;
		}
		this.writeResp(resp, ret);
	}

	/**
	 * 根据code获取用户的openid，返回的数据是json string
	 * @param req
	 * @param resp
	 * @param api
	 * @param reqData
	 */
	private void getOpenIdByOAuth2Code(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api,
			String reqData) {
		String code = req.getParameter("code");
		if (MyStringUtils.isEmpty(code)) {
			this.writeResp(resp, new ApiException("code param is must pass to this api servlet!").toString());
			return;
		}
		String openIdJson = "";
		try {
			openIdJson = api.getOpenIdByOAuth2Code(code).toString();
		} catch (ApiException e) {
			e.printStackTrace();
			log.error("", e);
			this.writeResp(resp, e.toString());
			return;
		}
		this.writeResp(resp, openIdJson);
	}

	/**
	 * 获取跳转到oauth2的url（即如果尚未获取到用户的openid，则调用此cmd，获取跳转url），需要传递get参数：receiveCodeUrl
	 * @param req
	 * @param resp
	 * @param api
	 * @param reqData
	 */
	private void getOAuth2RedirectUrl(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
		String receiveCodeUrl = req.getParameter("receiveCodeUrl");
		if (MyStringUtils.isEmpty(receiveCodeUrl)) {
			this.writeResp(resp, new ApiException("receiveCodeUrl param is must pass to this api servlet!").toString());
			return;
		}
		String redirect_uri= null;
		try {
			redirect_uri = api.getOAuth2Url(req, receiveCodeUrl);
		} catch (UnsupportedEncodingException e) {
			this.writeAndLogException(resp, e);
		}
		this.writeResp(resp, redirect_uri);
	}
	
	/**
	 * 获取跳转到oauth2的url,proxy模式，应用于非授权域的应用（即如果尚未获取到用户的openid，则调用此cmd，获取跳转url），需要传递get参数：receiveCodeUrl
	 * @param req
	 * @param resp
	 * @param api
	 * @param reqData
	 */
	private void getOAuth2RedirectUrlByProxy(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
		String receiveCodeUrl = req.getParameter("receiveCodeUrl");
		if (MyStringUtils.isEmpty(receiveCodeUrl)) {
			this.writeResp(resp, new ApiException("receiveCodeUrl param is must pass to this api servlet!").toString());
			return;
		}
		String redirect_uri=null;
		try {
			redirect_uri = api.getOAuth2UrlProxy(req, receiveCodeUrl);
		} catch (UnsupportedEncodingException e) {
			this.writeAndLogException(resp, e);
		}
		this.writeResp(resp, redirect_uri);
	}
	
	/*
	private void getOAuth2RedirectUrl(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
		String receiveCodeUrl = req.getParameter("receiveCodeUrl");
		if (MyStringUtils.isEmpty(receiveCodeUrl)) {
			this.writeResp(resp, new ApiException("receiveCodeUrl param is must pass to this api servlet!").toString());
			return;
		}
		String redirect_uri = api.getOAuth2Url(req, receiveCodeUrl);
		try {
			log.info("redirect url is:" + redirect_uri);
			resp.sendRedirect(redirect_uri);
		} catch (IOException e) {
			e.printStackTrace();
			log.error("", e);
		}
	}

	private void goOAuth2Proxy(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
		String receiveCodeUrl = req.getParameter("receiveCodeUrl");
		if (MyStringUtils.isEmpty(receiveCodeUrl)) {
			this.writeResp(resp, new ApiException("receiveCodeUrl param is must pass to this api servlet!").toString());
			return;
		}
		String redirect_uri = api.getOAuth2UrlProxy(req, receiveCodeUrl);
		try {
			log.info("redirect url is:" + redirect_uri);
			resp.sendRedirect(redirect_uri);
		} catch (IOException e) {
			e.printStackTrace();
			log.error("", e);
		}
	}
	
	
	private void receiveOAuth2CodeProxy(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api,
			String reqData) {
		String redirect_uri_proxy = req.getParameter("redirect_uri_proxy");
		if (MyStringUtils.isEmpty(redirect_uri_proxy)) {
			this.writeResp(resp,
					new ApiException("redirect_uri_proxy param is must pass to this api servlet!").toString());
			return;
		}
		try {
			String code = req.getParameter("code");
			String state = req.getParameter("state");
			// code=CODE&state=STATE
			redirect_uri_proxy = HttpUrl.addParam(redirect_uri_proxy, "code", code);
			redirect_uri_proxy = HttpUrl.addParam(redirect_uri_proxy, "state", state);
			log.info("redirect url is:" + redirect_uri_proxy);
			resp.sendRedirect(redirect_uri_proxy);
		} catch (IOException e) {
			e.printStackTrace();
			log.error("", e);
		}
	}
	*/

	/**
	 * 获取token json string
	 * @param req
	 * @param resp
	 * @param api
	 * @param reqData
	 */
	private void getTokenJson(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
		String jsonStr = "";
		try {
			jsonStr = api.getTokenJson();
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		}
		this.writeResp(resp, jsonStr);
	}

	/**
	 * 获取token string，非json
	 * @param req
	 * @param resp
	 * @param api
	 * @param reqData
	 */
	private void getTokenString(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
		String jsonStr = "";
		try {
			jsonStr = api.getTokenString();
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		}
		this.writeResp(resp, jsonStr);
	}

	/**
	 * 正确时响应（http reponse）函数
	 * @param resp
	 * @param data
	 */
	private void writeResp(HttpServletResponse resp, String data) {
		log.info("response data:" + data);
		ApiUtils.writeResp(resp, data);
	}

	/**
	 * 错误响应（http reponse）函数
	 * @param resp
	 * @param e
	 */
	private void writeAndLogException(HttpServletResponse resp, Exception e) {
		e.printStackTrace();
		log.error("", e);
		this.writeResp(resp, e.toString());
	}

	
}
