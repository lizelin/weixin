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
 * 
 * @author lizelin
 *
 */
public class WeixinApiServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7461167829576602905L;
	private static Logger log = MyLog.getLogger(WeixinApiServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.info("request url: " + new HttpUrl(req).getUrlString());
		String accountCode = req.getParameter("accountCode");
		if (MyStringUtils.isEmpty(accountCode)) {
			this.writeResp(resp, new ApiException("accountCode param is must pass to this api servlet!").toString());
			return;
		}
		String cmdAct = req.getParameter("cmdAct");
		if (MyStringUtils.isEmpty(cmdAct)) {
			this.writeResp(resp, new ApiException("cmdAct param is must pass to this api servlet!").toString());
			return;
		}

		WeixinApiImpl api = WeixinApiImpl.createApiToWxByAccountCode(accountCode);
		String reqData = ApiUtils.getRequestData(req);
		log.info("request data: " + reqData);
		if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.goOAuth2Proxy.name())) {
			this.goOAuth2Proxy(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.receiveOAuth2CodeProxy.name())) {
			this.receiveOAuth2CodeProxy(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.goOAuth2.name())) {
			this.goOAuth2(req, resp, api, reqData);
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
			this.sendCustomNewMessage(req, resp, api, reqData);
		} else if (cmdAct.equalsIgnoreCase(WeixinApiCmdAct.sendTemplateMessage.name())) {
			this.sendTemplateMessage(req, resp, api, reqData);
		}

	}

	private void sendTemplateMessage(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api,
			String reqData) {
		String msg = reqData;
		if (MyStringUtils.isEmpty(msg)) {
			this.writeAndLogException(resp, new ApiException("msg must not null!"));
			return;
		}
		JSONObject json = JSONObject.fromObject(msg);
		try {
			String data = api.sendTemplateMessage(json);
			this.writeResp(resp, data);
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		}
	}

	private void sendCustomNewMessage(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api,
			String reqData) {
		String toUser = req.getParameter("toUser");
		if (MyStringUtils.isEmpty(toUser)) {
			this.writeAndLogException(resp, new ApiException("toUser must pass to this api servlet!"));
			return;
		}
		String msg = reqData;
		if (MyStringUtils.isEmpty(msg)) {
			this.writeAndLogException(resp, new ApiException("msg must not null!"));
			return;
		}
		JSONArray arr = JSONArray.fromObject(msg);
		try {
			api.sendCustomNewsMessage(toUser, arr);
			this.writeResp(resp, success);
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		}

	}

	private void sendCustomTextMessage(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api,
			String reqData) {
		String toUser = req.getParameter("toUser");
		if (MyStringUtils.isEmpty(toUser)) {
			this.writeAndLogException(resp, new ApiException("toUser must pass to this api servlet!"));
			return;
		}
		String msg = reqData;
		if (MyStringUtils.isEmpty(msg)) {
			this.writeAndLogException(resp, new ApiException("msg must not null!"));
			return;
		}
		try {
			api.sendCustomTextMessage(toUser, msg);
			this.writeResp(resp, success);
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		}

	}

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

	private void long2short(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
		String long_url = req.getParameter("long_url");
		if (MyStringUtils.isEmpty(long_url)) {
			this.writeAndLogException(resp, new ApiException("long_url must pass to this api servlet!"));
			return;
		}
		try {
			String data = api.long2short(long_url).toString();
			this.writeResp(resp, data);
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		} catch (UnsupportedEncodingException e) {
			this.writeAndLogException(resp, e);
			return;
		}

	}

	private void createTempQrCode(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
		String expire_seconds = MyStringUtils.nvlString(req.getParameter("expire_seconds"), "0");

		String scene_id = MyStringUtils.nvlString(req.getParameter("scene_id"), "0");

		int expire = 2592000, scene = 0;
		if (Integer.parseInt(expire_seconds) > 0 && Integer.parseInt(expire_seconds) < 2592000)
			expire = Integer.parseInt(expire_seconds);
		scene = Integer.parseInt(scene_id);
		if (scene <= 0) {
			this.writeAndLogException(resp, new ApiException("scene_id must > 0!"));
			return;
		}

		try {
			String data = api.createTempQrCode(expire, scene).toString();
			this.writeResp(resp, data);
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		}
	}

	private void createLimitQrCode(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api,
			String reqData) {
		String scene_id = MyStringUtils.nvlString(req.getParameter("scene_id"), "0");

		int scene = Integer.parseInt(scene_id);
		if (scene <= 0 || scene > 100000) {
			this.writeAndLogException(resp, new ApiException("scene_id must < 100000!"));
			return;
		}
		try {
			String data = api.createLimitQrCode(scene).toString();
			this.writeResp(resp, data);
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		}
	}

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
			api.createMenus(json);
		} catch (ApiException e) {
			this.writeAndLogException(resp, e);
			return;
		}
		this.writeResp(resp, success);

	}

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

	private void goOAuth2(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
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

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
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

	private void getTokenJson(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
		String jsonStr = "";
		try {
			jsonStr = api.getTokenJson();
		} catch (ApiException e) {
			e.printStackTrace();
			log.error("", e);
			this.writeResp(resp, e.toString());
			return;
		}
		this.writeResp(resp, jsonStr);
	}

	private void getTokenString(HttpServletRequest req, HttpServletResponse resp, WeixinApiImpl api, String reqData) {
		String jsonStr = "";
		try {
			jsonStr = api.getTokenString();
		} catch (ApiException e) {
			e.printStackTrace();
			log.error("", e);
			this.writeResp(resp, e.toString());
			return;
		}
		this.writeResp(resp, jsonStr);
	}

	private void writeResp(HttpServletResponse resp, String data) {
		log.info("response data:" + data);
		ApiUtils.writeResp(resp, data);
	}

	private void writeAndLogException(HttpServletResponse resp, Exception e) {
		e.printStackTrace();
		log.error("", e);
		this.writeResp(resp, e.toString());
	}

	private static String success = new ApiException.ApiResult("0", "success").toJsonString();
}
