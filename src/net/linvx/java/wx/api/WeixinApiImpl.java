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

	private BoOfficialAccount account = null;

	public BoOfficialAccount getAccount() {
		return account;
	}

	/*
	 * string is account.vc2AccountCode
	 */
	private static Map<String, Token> tokens = new HashMap<String, Token>();
	// private static final Logger log = MyLog.getLogger(WeixinApiImpl.class);

	private WeixinApiImpl(BoOfficialAccount _account) {
		this.account = _account;
		this.tokenFile = CommonAssistant.getResourceRootPath() + "/weixintoken_" + account.getNumAccountGuid() + ".txt";
	}

	public static WeixinApiImpl createApiToWxByAccountCode(String accountCode) {
		return new WeixinApiImpl(DataProvider.getWxOfficialAccountByAccountCode(accountCode));
	}

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
	 * 创建菜单
	 * 
	 * @param json
	 * @return
	 * @throws ApiException
	 */
	public boolean createMenus(JSONObject json) throws ApiException {
		String api = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=" + this.getTokenString();
		if (json == null)
			return false;
		ApiUtils.httpPostJson(api, json.toString());
		return true;

	}

	/**
	 * 删除菜单
	 * 
	 * @return
	 * @throws ApiException
	 */
	public boolean deleteMenus() throws ApiException {
		String api = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=" + this.getTokenString();
		ApiUtils.httpGetJson(api);
		return true;

	}

	/**
	 * 查询菜单
	 * 
	 * @return
	 * @throws ApiException
	 */
	public JSONObject getMenus() throws ApiException {
		String api = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token=" + this.getTokenString();
		return ApiUtils.httpGetJson(api);
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

	public static void main(String[] args) {

	}

	public JSONObject createTempQrCode(int expire_seconds, int scene_id) throws ApiException {
		String api = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + this.getTokenString();
		// {"expire_seconds": 604800, "action_name": "QR_SCENE", "action_info":
		// {"scene": {"scene_id": 123}}}
		JSONObject json = new JSONObject();
		json.put("expire_seconds", expire_seconds);
		json.put("action_name", "QR_SCENE");
		JSONObject temp = new JSONObject(), temp1 = new JSONObject();
		temp.put("scene_id", scene_id);
		temp1.put("scene", temp);
		json.put("action_info", temp1);
		return ApiUtils.httpPostJson(api, json.toString());
	}

	public JSONObject createLimitQrCode(int scene_id) throws ApiException {
		String api = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + this.getTokenString();
		// {"action_name": "QR_LIMIT_SCENE", "action_info": {"scene":
		// {"scene_id": 123}}}
		JSONObject json = new JSONObject();
		json.put("action_name", "QR_LIMIT_SCENE");
		JSONObject temp = new JSONObject(), temp1 = new JSONObject();
		temp.put("scene_id", scene_id);
		temp1.put("scene", temp);
		json.put("action_info", temp1);
		return ApiUtils.httpPostJson(api, json.toString());
	}

	public JSONObject long2short(String longUrl) throws ApiException, UnsupportedEncodingException {
		String api = "https://api.weixin.qq.com/cgi-bin/shorturl?access_token=" + this.getTokenString();
		JSONObject json = new JSONObject();
		json.put("action", "long2short");
		json.put("long_url", URLEncoder.encode(longUrl, Consts.DEFAULT_ENCODING));
		return ApiUtils.httpPostJson(api, json.toString());
	}

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

	private net.linvx.java.wx.model.JsapiTicket jsapiTicket = null;

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

	public String sendCustomTextMessage(String toUser, String msg) throws ApiException {
		String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + this.getTokenString();
		JSONObject json = new JSONObject();
		json.put("touser", toUser);
		json.put("msgtype", "text");
		JSONObject jsonContent = new JSONObject();
		jsonContent.put("content", msg);
		json.put("text", jsonContent);
		String data = json.toString();
		JSONObject ret = ApiUtils.httpPostJson(url, data);
		return ret.toString();
	}

	public String sendCustomNewsMessage(String toUser, JSONArray articles) throws ApiException {
		String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + this.getTokenString();
		JSONObject json = new JSONObject();
		json.put("touser", toUser);
		json.put("msgtype", "news");
		JSONObject jsonContent = new JSONObject();
		jsonContent.put("articles", articles);
		json.put("news", jsonContent);
		String data = json.toString();
		JSONObject ret = ApiUtils.httpPostJson(url, data);
		return ret.toString();
	}

	public String sendTemplateMessage(JSONObject jsonData) throws ApiException {
		// TODO Auto-generated method stub
		String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + this.getTokenString();
//		String toUser = jsonData.getString("touser");
		String data = jsonData.toString();
		JSONObject json = ApiUtils.httpPostJson(url, data);
		return json.toString();
	}

}
