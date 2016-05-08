package net.linvx.java.wx.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import net.linvx.java.libs.http.HttpHelper;
import net.linvx.java.libs.http.HttpResponse;
import net.linvx.java.libs.tools.MyLog;
import net.linvx.java.libs.utils.MyStringUtils;
import net.linvx.java.wx.bo.BoOfficialAccount;
import net.linvx.java.wx.common.Consts;
import net.sf.json.JSONObject;

/**
 * Api通用类，比如检查帐号、读取request流、http get／post，检查http response是否合法的xml或json等；
 * @author lizelin
 *
 */
public class ApiUtils {
	private static final Logger log = MyLog.getLogger(ApiUtils.class);
	public static final String ERRCODE = "errcode";
	public static final String ERRMSG = "errmsg";

	private ApiUtils() {

	}

	/**
	 * 检查服务号帐号信息
	 * 
	 * @param req
	 * @param account
	 * @return
	 */
	public static boolean checkOfficialAccount(HttpServletRequest req, BoOfficialAccount account) {
		if (account == null) {
			log.error("the request api uri [" + req.getRequestURI() + "] is not config in wx_official_account table");
			return false;
		}
		if (MyStringUtils.isEmpty(account.vc2ApiToken)) {
			log.error("the account [" + account.vc2AccountName
					+ "] don't have apiToken config in wx_official_account table");
			return false;
		}
		return true;
	}

	/**
	 * 检查签名
	 * 
	 * @param token
	 * @param req
	 * @return
	 */
	public static boolean checkSignOfMsgFromWx(HttpServletRequest req, String token) {
		String signature = req.getParameter("signature");
		String timestamp = req.getParameter("timestamp");
		String nonce = req.getParameter("nonce");
		if (MyStringUtils.isEmpty(signature) || MyStringUtils.isEmpty(timestamp) || MyStringUtils.isEmpty(nonce)) {
			log.error(String.format("signature [%s] or  timestamp [%s] or nonce [%s] is null ", signature, timestamp,
					nonce));
			return false;
		}

		// String echostr = req.getParameter("echostr");
		List<String> list = new ArrayList<String>();
		list.add(token);
		list.add(timestamp);
		list.add(nonce);
		Collections.sort(list);// 参数排序
		String str = "";
		for (String s : list) {
			str = str + s;
		}
		String mySign = DigestUtils.shaHex(str);
		if (!signature.equals(mySign)) {
			// 签名错误
			log.error("Sign error:signature=" + signature + ";mySign:" + mySign);
			return false;
		} else {
			log.info("Sign success!");
			return true;
		}

	}

	/**
	 * 获取request数据
	 * 
	 * @param request
	 * @return
	 */
	public static String getRequestData(HttpServletRequest request) {
		if (request.getMethod().equalsIgnoreCase("get"))
			return "";
		InputStream is = null;
		String requestData = null;
		try {
			is = request.getInputStream();
			// 取HTTP请求流长度
			int size = request.getContentLength();
			// 用于缓存每次读取的数据
			byte[] buffer = new byte[size];
			// 用于存放结果的数组
			byte[] dataByte = new byte[size];
			int count = 0;
			int rbyte = 0;
			// 循环读取
			while (count < size) {
				// 每次实际读取长度存于rbyte中
				rbyte = is.read(buffer);
				for (int i = 0; i < rbyte; i++) {
					dataByte[count + i] = buffer[i];
				}
				count += rbyte;
			}
			requestData = new String(dataByte, Consts.DEFAULT_ENCODING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return requestData;
	}

	/**
	 * 使用dom4j解析xml并返回根节点
	 * 
	 * @param reqData
	 * @return
	 */
	public static Element parseXml(String reqData) {
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(reqData);
		} catch (DocumentException e) {
			log.error("", e);
			return null;
		}
		return doc.getRootElement();
	}

	/**
	 * 返回数据
	 * 
	 * @param response
	 * @param data
	 */
	public static void writeResp(HttpServletResponse response, String data) {
		Writer writer = null;
		try {
			response.setCharacterEncoding("UTF-8");
			writer = response.getWriter();
			writer.write(data);
		} catch (IOException e) {
			log.error("", e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
		}
	}


	public static Element assertXml(String result) throws ApiException {
		if (MyStringUtils.isEmpty(result))
			throw new ApiException("unknown", "the response string is null!");
		Element rootEle = ApiUtils.parseXml(result);
		if (rootEle == null)
			throw new ApiException("unknown", "the expect response string is xml!");
		return rootEle;
	}

	public static JSONObject assertJson(String result) throws ApiException {
		if (MyStringUtils.isEmpty(result))
			throw new ApiException("unknown", "the response string is null!");
		JSONObject json = JSONObject.fromObject(result);
		if (json == null) {
			throw new ApiException("unknown", "the expect response string is json!");
		}
		if (MyStringUtils.isNotEmpty(json.optString(ApiUtils.ERRCODE)) && !"0".equals(json.optString(ApiUtils.ERRCODE))) {
			throw new ApiException(json.optString(ERRCODE), json.optString(ERRMSG));
		}
		return json;
	}

	static String get(String url) throws ApiException {
		log.info("request api url is :" + url);
		HttpResponse res = HttpHelper.httpGetResponse(url);
		String result = res.getResponseString();
		log.info("api response is :" + result);
		if (res.isSuccess())
			return result;
		else
			throw new ApiException(String.valueOf(res.getResponseCode()), result);
	}

	static String post(String url, String data) throws ApiException {
		log.info("request api url is :" + url);
		log.info("request data is:" + data);
		HttpResponse res = HttpHelper.httpPostResponse(url, MyStringUtils.getBytes(data));
		String result = res.getResponseString();
		log.info("api response is :" + result);
		if (res.isSuccess())
			return result;
		else
			throw new ApiException(String.valueOf(res.getResponseCode()), result);
	}

	public static JSONObject httpGetJson(String url) throws ApiException {
		String result = ApiUtils.get(url);
		return assertJson(result);
	}

	public static Element httpGetXml(String url) throws ApiException {
		String result = ApiUtils.get(url);
		return assertXml(result);
	}

	public static JSONObject httpPostJson(String url, String data) throws ApiException {
		String result = ApiUtils.post(url, data);
		return assertJson(result);
	}

	public static Element httpPostXml(String url, String data) throws ApiException {
		String result = ApiUtils.post(url, data);
		return assertXml(result);
	}
	
	public static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    public static String create_nonce_str() {
        return UUID.randomUUID().toString();
    }

    public static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
    
    
}
