package net.linvx.java.wx.model;

import java.util.Date;

import net.linvx.java.libs.utils.MyDateUtils;
import net.sf.json.JSONObject;

/**
 * token
 * @author lizelin
 * {"access_token":"ACCESS_TOKEN","expires_in":7200}
 */
public class Token {
	
	private String access_token;
	private int expires_in;
	private Date createDate;
	private Date expiresDate;
	public Token() {
		super();
	}
	
	public Token(String access_token, int expires_in) {
		super();
		this.access_token = access_token;
		this.expires_in = expires_in;
		this.createDate = new Date();
		this.expiresDate = MyDateUtils.millsLong2Date(this.createDate.getTime() + 1000l * (this.expires_in -120) );
	}
	
	public String getAccessToken() {
		return access_token;
	}
	
	public Token setAccessToken(String access_token) {
		this.access_token = access_token;
		return this;
	}

	public Token setExpiresIn(int expires_in) {
		this.expires_in = expires_in;
		return this;
	}

	public Token setCreateDate(Date createDate) {
		this.createDate = createDate;
		return this;
	}

	public Token setExpiresDate(Date expiresDate) {
		this.expiresDate = expiresDate;
		return this;
	}


	public boolean isExpired() {
		return this.expiresDate.before(new Date());
	}
	
	@Override
	public String toString() {
		//{"access_token":"ACCESS_TOKEN","expires_in":7200}
		JSONObject json = new JSONObject();
		json.put("access_token", access_token);
		json.put("expires_in", expires_in);
		json.put("expires_date", MyDateUtils.dateToString(expiresDate));
		json.put("create_date", MyDateUtils.dateToString(createDate));
		return json.toString();
	}

	public static void main(String[] args){
		System.out.println(new Token("aaaa",7200));
	}
	
	public static Token loadFromJsonString(String str) {
		Token token = new Token();
		JSONObject json = JSONObject.fromObject(str);
		if (json != null) {
			token.setAccessToken(json.optString("access_token"))
				.setCreateDate(MyDateUtils.stringToDate(json.optString("create_date")))
				.setExpiresDate(MyDateUtils.stringToDate(json.optString("expires_date")))
				.setExpiresIn(json.optInt("expires_in"));			
		}			
		return token;
	}
}
