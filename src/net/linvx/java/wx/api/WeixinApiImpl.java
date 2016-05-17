package net.linvx.java.wx.api;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import net.linvx.java.libs.http.HttpUrl;
import net.linvx.java.libs.tools.CommonAssistant;
import net.linvx.java.libs.tools.MyLog;
import net.linvx.java.libs.utils.MyStringUtils;
import net.linvx.java.wx.bo.BoOfficialAccount;
import net.linvx.java.wx.common.Consts;
import net.linvx.java.wx.common.DataProvider;
import net.linvx.java.wx.model.JsapiTicket;
import net.linvx.java.wx.model.Token;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 调用微信服务器api接口，比如获取token，拉取用户信息等
 * 
 * @author lizelin
 *
 */
public class WeixinApiImpl {
	private static Logger log = MyLog.getLogger(WeixinApiImpl.class);
	private String tokenFile = "";
	private net.linvx.java.wx.model.JsapiTicket jsapiTicket = null;

	private BoOfficialAccount account = null;

	public BoOfficialAccount getAccount() {
		return account;
	}

	/*
	 * 缓存cookie key string is account.vc2AccountCode
	 */
	private static Map<String, Token> tokens = new HashMap<String, Token>();

	private WeixinApiImpl(BoOfficialAccount _account) {
		this.account = _account;
		this.tokenFile = CommonAssistant.getResourceRootPath() + "/weixintoken_" + account.getNumAccountGuid() + ".txt";
	}

	/**
	 * 根据微信帐号CODE来创建实例
	 * 
	 * @param accountCode
	 *            - 例如测试帐号的code为“CODE”
	 * @return WeixinApiImpl实例
	 */
	public static WeixinApiImpl createApiToWxByAccountCode(String accountCode) {
		return new WeixinApiImpl(DataProvider.getWxOfficialAccountByAccountCode(accountCode));
	}

	/**
	 * 根据apiUrl（即接收微信服务器推送消息的api url）来创建实例
	 * 
	 * @param apiUrl
	 * @return WeixinApiImpl实例
	 */
	public static WeixinApiImpl createApiToWxByApiUrl(String apiUrl) {
		return new WeixinApiImpl(DataProvider.getWxOfficialAccountByApiUrl(apiUrl));
	}

	/**
	 * 获取token
	 * 
	 * @return
	 * @throws ApiException
	 */
	public Token getToken() throws ApiException {
		String accountCode = account.getVc2AccountCode();
		if (tokens.containsKey(accountCode) && !tokens.get(accountCode).isExpired())
			return tokens.get(accountCode);

		Token token = this.getTokenFromFile();
		if (token != null && !token.isExpired()) {
			return token;
		}
		String api = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
				+ account.getVc2AppId() + "&secret=" + account.getVc2AppSecret();
		JSONObject json = ApiUtils.httpGetJson(api);
		token = new Token(json.optString("access_token"), json.optInt("expires_in"));
		tokens.put(accountCode, token);
		this.saveTokenToFile(token.toString());
		return tokens.get(accountCode);
	}

	/**
	 * 获取token的json串，保持和weixin一样的格式
	 * 
	 * @return
	 * @throws ApiException
	 */
	public String getTokenJson() throws ApiException {
		return this.getToken().toString();
	}

	/**
	 * 获取token string
	 * 
	 * @return
	 * @throws ApiException
	 */
	public String getTokenString() throws ApiException {
		return this.getToken().getAccessToken();
	}

	/**
	 * 拉取用户信息
	 * 
	 * @param openId
	 * @return
	 * @throws ApiException
	 */
	public JSONObject getUserInfo(String openId) throws ApiException {
		String api = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + this.getTokenString() + "&openid="
				+ openId + "&lang=zh_CN";
		return ApiUtils.httpGetJson(api);
	}

	/**
	 * 创建自定义菜单
	 * 
	 * @param jsonStr
	 *            - 菜单数据 { "button": [ { "name": "扫码", "sub_button": [ { "type":
	 *            "scancode_waitmsg", "name": "扫码带提示", "key": "rselfmenu_0_0",
	 *            "sub_button": [ ] }, { "type": "scancode_push", "name":
	 *            "扫码推事件", "key": "rselfmenu_0_1", "sub_button": [ ] } ] }, {
	 *            "name": "发图", "sub_button": [ { "type": "pic_sysphoto",
	 *            "name": "系统拍照发图", "key": "rselfmenu_1_0", "sub_button": [ ] },
	 *            { "type": "pic_photo_or_album", "name": "拍照或者相册发图", "key":
	 *            "rselfmenu_1_1", "sub_button": [ ] }, { "type": "pic_weixin",
	 *            "name": "微信相册发图", "key": "rselfmenu_1_2", "sub_button": [ ] }
	 *            ] }, { "name": "发送位置", "type": "location_select", "key":
	 *            "rselfmenu_2_0" }, { "type": "media_id", "name": "图片",
	 *            "media_id": "MEDIA_ID1" }, { "type": "view_limited", "name":
	 *            "图文消息", "media_id": "MEDIA_ID2" } ] }
	 * 
	 * @return response string, like: {"errcode":0,"errmsg":"ok"}
	 * @throws ApiException
	 */
	public String createMenus(String jsonStr) throws ApiException {
		String api = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=" + this.getTokenString();
		JSONObject json = ApiUtils.httpPostJson(api, jsonStr);
		return json.toString();

	}

	/**
	 * 删除菜单
	 * 
	 * @return response string, like: {"errcode":0,"errmsg":"ok"}
	 * @throws ApiException
	 */
	public String deleteMenus() throws ApiException {
		String api = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=" + this.getTokenString();
		return ApiUtils.httpGetJson(api).toString();

	}

	/**
	 * 查询菜单
	 * 
	 * @return 菜单数据： {"menu":{"button":[{"type":"click","name":"今日歌曲","key":
	 *         "V1001_TODAY_MUSIC","sub_button":[]},{"type":"click","name":
	 *         "歌手简介","key":"V1001_TODAY_SINGER","sub_button":[]},{"name":"菜单",
	 *         "sub_button":[{"type":"view","name":"搜索","url":
	 *         "http://www.soso.com/","sub_button":[]},{"type":"view","name":
	 *         "视频","url":"http://v.qq.com/","sub_button":[]},{"type":"click",
	 *         "name":"赞一下我们","key":"V1001_GOOD","sub_button":[]}]}]}}
	 * @throws ApiException
	 */
	public String getMenus() throws ApiException {
		String api = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token=" + this.getTokenString();
		return ApiUtils.httpGetJson(api).toString();
	}

	/**
	 * 根据OAuth2的code获取openid
	 * 
	 * @param code
	 * @return json：{ "access_token":"ACCESS_TOKEN", "expires_in":7200,
	 *         "refresh_token":"REFRESH_TOKEN", "openid":"OPENID",
	 *         "scope":"SCOPE"}
	 * @throws ApiException
	 */
	public JSONObject getOpenIdByOAuth2Code(String code) throws ApiException {
		String api = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + account.getVc2AppId() + "&secret="
				+ account.getVc2AppSecret() + "&code=" + code + "&grant_type=authorization_code";
		return ApiUtils.httpGetJson(api);
	}

	/**
	 * 获取OAuth2的授权url。代理模式
	 * 
	 * @param req
	 * @param receiveCodeUrl
	 * @return
	 */
	public String getOAuth2UrlProxy(HttpServletRequest req, String receiveCodeUrl) {
		String temp = "";
		try {
			temp = new HttpUrl(req).getSchemaHostPortPath() + "?accountCode=" + account.getVc2AccountCode() + "&cmdAct="
					+ WeixinApiCmdAct.receiveOAuth2CodeProxy.name() + "&redirect_uri_proxy="
					+ URLEncoder.encode(receiveCodeUrl, Consts.DEFAULT_ENCODING);
			temp = URLEncoder.encode(temp, Consts.DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			log.error("", e);
			e.printStackTrace();
		}

		return "https://open.weixin.qq.com/connect/oauth2/authorize" + "?appid=" + account.getVc2AppId()
				+ "&redirect_uri=" + temp + "&response_type=code&scope=snsapi_base" + "&state=go#wechat_redirect";
	}

	/**
	 * 获取OAuth2的授权url。直连微信服务器模式
	 * 
	 * @param req
	 * @param receiveCodeUrl
	 * @return
	 */
	public String getOAuth2Url(HttpServletRequest req, String receiveCodeUrl) {
		String authDomains = account.getVc2JsApiDomain();
		if (MyStringUtils.isEmpty(authDomains))
			return "";
		List<String> domains = Arrays.asList(authDomains.split(","));
		boolean isAuthDomain = false;
		if (domains.contains(new HttpUrl(receiveCodeUrl).getUrl().getHost())) {
			isAuthDomain = true;
		}
		String redirectUri = "";
		try {
			if (isAuthDomain) {
				redirectUri = URLEncoder.encode(receiveCodeUrl, Consts.DEFAULT_ENCODING);
				return "https://open.weixin.qq.com/connect/oauth2/authorize" + "?appid=" + account.getVc2AppId()
						+ "&redirect_uri=" + redirectUri + "&response_type=code&scope=snsapi_base"
						+ "&state=go#wechat_redirect";
			} else {
				return this.getOAuth2UrlProxy(req, receiveCodeUrl);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			log.error("", e);
		}
		return "";
	}

	/**
	 * 将token json字符串保存在文件
	 * 
	 * @param tokenJson
	 */
	private void saveTokenToFile(String tokenJson) {
		File file = new File(tokenFile);
		if (file.exists())
			file.delete();
		try {
			FileUtils.writeStringToFile(file, tokenJson);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 从文件中加载token json string，并转成Token类实例
	 * 
	 * @return
	 */
	private Token getTokenFromFile() {
		Token token = null;
		File file = new File(tokenFile);
		try {
			if (file.exists()) {
				String tokenJson = FileUtils.readFileToString(file);
				token = Token.loadFromJsonString(tokenJson);
				if (token != null)
					return token;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return token;
	}

	/**
	 * 创建临时二维码
	 * 
	 * @param expire_seconds
	 *            - 最大：604800？？？
	 * @param scene_id
	 * @return {"ticket":
	 *         "gQH47joAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL2taZ2Z3TVRtNzJXV1Brb3ZhYmJJAAIEZ23sUwMEmm3sUw=="
	 *         ,"expire_seconds":60,"url":
	 *         "http:\/\/weixin.qq.com\/q\/kZgfwMTm72WWPkovabbI"}
	 * @throws ApiException
	 */
	public String createTempQrCode(int expire_seconds, int scene_id) throws ApiException {
		JSONObject json = new JSONObject();
		json.put("expire_seconds", expire_seconds);
		json.put("action_name", "QR_SCENE");
		JSONObject temp = new JSONObject(), temp1 = new JSONObject();
		temp.put("scene_id", scene_id);
		temp1.put("scene", temp);
		json.put("action_info", temp1);
		return this.createTempQrCode(json.toString());
	}

	/**
	 * 创建临时二维码
	 * 
	 * @param jsonStr
	 *            - {"expire_seconds": 604800, "action_name": "QR_SCENE",
	 *            "action_info": {"scene": {"scene_id": 123}}}
	 * @return {"ticket":
	 *         "gQH47joAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL2taZ2Z3TVRtNzJXV1Brb3ZhYmJJAAIEZ23sUwMEmm3sUw=="
	 *         ,"expire_seconds":60,"url":
	 *         "http:\/\/weixin.qq.com\/q\/kZgfwMTm72WWPkovabbI"}
	 * @throws ApiException
	 */
	public String createTempQrCode(String jsonStr) throws ApiException {
		String api = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + this.getTokenString();
		JSONObject json = JSONObject.fromObject(jsonStr);
		if (json != null && "QR_SCENE".equalsIgnoreCase(json.optString("action_name"))) {
			return ApiUtils.httpPostJson(api, jsonStr).toString();
		} else {
			return null;
		}
	}

	/**
	 * 创建永久二维码
	 * 
	 * @param scene_id
	 * @return {"ticket":
	 *         "gQH47joAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL2taZ2Z3TVRtNzJXV1Brb3ZhYmJJAAIEZ23sUwMEmm3sUw=="
	 *         ,"expire_seconds":60,"url":
	 *         "http:\/\/weixin.qq.com\/q\/kZgfwMTm72WWPkovabbI"}
	 * 
	 * @throws ApiException
	 */
	public String createLimitQrCode(int scene_id) throws ApiException {
		JSONObject json = new JSONObject();
		json.put("action_name", "QR_LIMIT_SCENE");
		JSONObject temp = new JSONObject(), temp1 = new JSONObject();
		temp.put("scene_id", scene_id);
		temp1.put("scene", temp);
		json.put("action_info", temp1);
		return this.createLimitQrCode(json.toString());
	}

	/**
	 * 创建永久二维码
	 * 
	 * @param jsonStr
	 *            - {"action_name": "QR_LIMIT_SCENE", "action_info": {"scene":
	 *            {"scene_id": 123}}}
	 * @return {"ticket":
	 *         "gQH47joAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL2taZ2Z3TVRtNzJXV1Brb3ZhYmJJAAIEZ23sUwMEmm3sUw=="
	 *         ,"expire_seconds":60,"url":
	 *         "http:\/\/weixin.qq.com\/q\/kZgfwMTm72WWPkovabbI"}
	 * @throws ApiException
	 */
	public String createLimitQrCode(String jsonStr) throws ApiException {
		String api = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + this.getTokenString();
		JSONObject json = JSONObject.fromObject(jsonStr);
		if (json != null && "QR_LIMIT_SCENE".equalsIgnoreCase(json.optString("action_name"))) {
			return ApiUtils.httpPostJson(api, jsonStr).toString();
		} else {
			return null;
		}
	}

	/**
	 * 长网址转短网址
	 * 
	 * @param longUrl
	 *            - 长网址
	 * @return 短网址，{"errcode":0,"errmsg":"ok","short_url":
	 *         "http:\/\/w.url.cn\/s\/AvCo6Ih"}
	 * @throws ApiException
	 * @throws UnsupportedEncodingException
	 */
	public String long2short(String longUrl) throws ApiException, UnsupportedEncodingException {
		JSONObject json = new JSONObject();
		json.put("action", "long2short");
		json.put("long_url", URLEncoder.encode(longUrl, Consts.DEFAULT_ENCODING));
		return this.longUrl2short(json.toString());
	}

	/**
	 * 长网址转短网址
	 * 
	 * @param jsonStr
	 *            - {"action":"long2short","long_url":
	 *            "http://wap.koudaitong.com/v2/showcase/goods?alias=128wi9shh&spm=h56083&redirect_count=1"}
	 * @return 短网址，{"errcode":0,"errmsg":"ok","short_url":
	 *         "http:\/\/w.url.cn\/s\/AvCo6Ih"}
	 * @throws ApiException
	 */
	public String longUrl2short(String jsonStr) throws ApiException {
		String api = "https://api.weixin.qq.com/cgi-bin/shorturl?access_token=" + this.getTokenString();
		return ApiUtils.httpPostJson(api, jsonStr).toString();
	}

	/**
	 * 获取JsapiTicket
	 * 
	 * @return
	 * @throws ApiException
	 */
	public String getJsapiTicket() throws ApiException {
		if (jsapiTicket != null && jsapiTicket.validate())
			return jsapiTicket.getTicket();

		return getJsapiTicketSyn();
	}

	private synchronized String getJsapiTicketSyn() throws ApiException {
		String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + this.getTokenString()
				+ "&type=jsapi";
		JSONObject jsonObject = ApiUtils.httpGetJson(url);
		/**
		 * { "errcode":0, "errmsg":"ok", "ticket":
		 * "bxLdikRXVbTPdHSM05e5u5sUoXNKd8-41ZO3MhKoyN5OfkWITDGgnr2fwJ0m9E8NYzWKVZvdVtaUgWvsdshFKA",
		 * "expires_in":7200 }
		 */
		if (!jsonObject.containsKey("ticket")) {
			throw new ApiException("返回结果有错误，不包含key值：ticket");
		}
		String ticket = jsonObject.getString("ticket");
		int expires = jsonObject.getInt("expires_in") - 60;
		log.info("ticket: " + ticket + "; expires: " + expires);
		jsapiTicket = new JsapiTicket();
		jsapiTicket.setTicket(ticket);
		jsapiTicket.setExpires(expires);
		jsapiTicket.setUpdateTime(System.currentTimeMillis());
		return ticket;
	}

	/**
	 * 获取jsapi签名
	 * 
	 * @param url
	 * @return {"timestamp":"1463475703","appid":"wx0b4d00f8f1bbde68","nonceStr"
	 *         :"88547bf6-e17d-4cb5-a920-c26d91ebbe64","jsapi_ticket":
	 *         "bxLdikRXVbTPdHSM05e5u_BlJVxejmnfHxXPhdAA6QD4iGYe_Akb8UoxKJ4O-iJdh8uLG-Bmk_Gu4MBLsdkVaA"
	 *         ,"signature":"5011c8a683331c53e66ff2179075e31828d1bddf","url":
	 *         "www.sina.com"}
	 * 
	 * @throws ApiException
	 */
	public JSONObject jsapiSign(String url) throws ApiException {
		String jsapi_ticket = this.getJsapiTicket();
		Map<String, String> ret = new HashMap<String, String>();
		String nonce_str = ApiUtils.create_nonce_str();
		String timestamp = ApiUtils.create_timestamp();
		String string1;
		String signature = "";

		// 注意这里参数名必须全部小写，且必须有序
		string1 = "jsapi_ticket=" + jsapi_ticket + "&noncestr=" + nonce_str + "&timestamp=" + timestamp + "&url=" + url;
		System.out.println(string1);

		try {
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(string1.getBytes("UTF-8"));
			signature = ApiUtils.byteToHex(crypt.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		ret.put("url", url);
		ret.put("jsapi_ticket", jsapi_ticket);
		ret.put("nonceStr", nonce_str);
		ret.put("timestamp", timestamp);
		ret.put("signature", signature);
		ret.put("appid", this.getAccount().getVc2AppId());
		return net.sf.json.JSONObject.fromObject(ret);
	}

	/**
	 * 发送文本客服消息
	 * 
	 * @param jsonStr
	 *            - json string, like: { "touser":"OPENID", "msgtype":"text",
	 *            "text": { "content": "Hello World" } }
	 * @return response string, like: {"errcode":0,"errmsg":"ok"}
	 * @throws ApiException
	 */
	public String sendCustomTextMessage(String jsonStr) throws ApiException {
		String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + this.getTokenString();
		JSONObject ret = ApiUtils.httpPostJson(url, jsonStr);
		return ret.toString();
	}

	/**
	 * 发送文本客服消息
	 * 
	 * @param toUser
	 *            - 用户的openid
	 * @param msg
	 *            - 发送的文本消息
	 * @return response string, like: {"errcode":0,"errmsg":"ok"}
	 * @throws ApiException
	 */
	public String sendCustomTextMessage(String toUser, String msg) throws ApiException {
		JSONObject json = new JSONObject();
		json.put("touser", toUser);
		json.put("msgtype", "text");
		JSONObject jsonContent = new JSONObject();
		jsonContent.put("content", msg);
		json.put("text", jsonContent);
		return this.sendCustomTextMessage(json.toString());
	}

	/**
	 * 发送文本客服消息给用户列表
	 * 
	 * @param toUsers
	 *            - array of users send to，item is openid
	 * @param msg
	 *            - 发送的文本消息
	 * @return 成功发送数量
	 */
	public int sendCustomTextMessageToUsers(String[] toUsers, String msg) {
		int successCount = 0;
		if (toUsers != null && toUsers.length > 0) {
			for (int i = 0; i < toUsers.length; i++) {
				try {
					this.sendCustomTextMessage(toUsers[i], msg);
					successCount++;
				} catch (ApiException e) {
					log.error("Errsend: sendCustomTextMessageToUsers call sendCustomTextMessage(" + toUsers[i] + ","
							+ msg + ")");
					log.error("" + e);
					e.printStackTrace();
				}
			}

		}
		return successCount;
	}

	/**
	 * 发送图文客服消息
	 * 
	 * @param jsonStr
	 *            - json string, like: { "touser":"OPENID", "msgtype":"news",
	 *            "news":{ "articles": [ { "title":"Happy Day", "description":
	 *            "Is Really A Happy Day", "url":"URL", "picurl":"PIC_URL" }, {
	 *            "title":"Happy Day", "description":"Is Really A Happy Day",
	 *            "url":"URL", "picurl":"PIC_URL" } ] } }
	 * @return response string, like: {"errcode":0,"errmsg":"ok"}
	 * @throws ApiException
	 */
	public String sendCustomNewsMessage(String jsonStr) throws ApiException {
		String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + this.getTokenString();
		JSONObject ret = ApiUtils.httpPostJson(url, jsonStr);
		return ret.toString();
	}

	/**
	 * 发送图文客服消息
	 * 
	 * @param toUser
	 *            - openid
	 * @param articles
	 *            - 图文消息JSON数组，like：[ { "title":"Happy Day", "description":
	 *            "Is Really A Happy Day", "url":"URL", "picurl":"PIC_URL" }, {
	 *            "title":"Happy Day", "description":"Is Really A Happy Day",
	 *            "url":"URL", "picurl":"PIC_URL" } ]
	 * @return response string, like: {"errcode":0,"errmsg":"ok"}
	 * @throws ApiException
	 */
	public String sendCustomNewsMessage(String toUser, JSONArray articles) throws ApiException {
		JSONObject json = new JSONObject();
		json.put("touser", toUser);
		json.put("msgtype", "news");
		JSONObject jsonContent = new JSONObject();
		jsonContent.put("articles", articles);
		json.put("news", jsonContent);
		return this.sendCustomNewsMessage(json.toString());
	}

	/**
	 * 发送图文客服消息到用户列表
	 * 
	 * @param toUsers
	 *            - array of users send to，item is openid
	 * @param articles
	 *            - 图文消息JSON数组，like：[ { "title":"Happy Day", "description":
	 *            "Is Really A Happy Day", "url":"URL", "picurl":"PIC_URL" }, {
	 *            "title":"Happy Day", "description":"Is Really A Happy Day",
	 *            "url":"URL", "picurl":"PIC_URL" } ]
	 * @return response string, like: {"errcode":0,"errmsg":"ok"}
	 */
	public int sendCustomNewsMessageToUsers(String[] toUsers, JSONArray articles) {
		int successCount = 0;
		if (toUsers != null && toUsers.length > 0) {
			for (int i = 0; i < toUsers.length; i++) {
				try {
					this.sendCustomNewsMessage(toUsers[i], articles);
					successCount++;
				} catch (ApiException e) {
					log.error("Errsend: sendCustomTextMessageToUsers call sendCustomTextMessage(" + toUsers[i] + ","
							+ articles.toString() + ")");
					log.error("" + e);
					e.printStackTrace();
				}
			}

		}
		return successCount;
	}

	/**
	 * 发送模版消息
	 * 
	 * @param jsonData
	 *            - 模版消息内容 { "touser":"OPENID",
	 *            "template_id":"ngqIpbwh8bUfcSsECmogfXcV14J0tQlEpBO27izEYtY",
	 *            "url":"http://weixin.qq.com/download", "data":{ "first": {
	 *            "value":"恭喜你购买成功！", "color":"#173177" }, "keynote1":{
	 *            "value":"巧克力", "color":"#173177" }, "keynote2": {
	 *            "value":"39.8元", "color":"#173177" }, "keynote3": {
	 *            "value":"2014年9月22日", "color":"#173177" }, "remark":{
	 *            "value":"欢迎再次购买！", "color":"#173177" } } }
	 * @return response string, like: { "errcode":0, "errmsg":"ok",
	 *         "msgid":200228332 }
	 * @throws ApiException
	 */
	public String sendTemplateMessage(String jsonData) throws ApiException {
		String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + this.getTokenString();
		JSONObject json = ApiUtils.httpPostJson(url, jsonData);
		return json.toString();
	}

	public static void main(String[] args) throws ApiException {
		WeixinApiImpl api = WeixinApiImpl.createApiToWxByAccountCode("CODE");
		String s = api.sendCustomTextMessage("owJ2MjibJT9j6VqKTsFe4x29gOhY", "test");
		System.out.println(s);
	}
}
