package net.linvx.java.wx.api;

import net.linvx.java.libs.enhance.BaseBean;

/**
 * 自定义api exception类，主要为了一些可捕捉错误
 * @author lizelin
 *
 */
public class ApiException extends Exception{

	private static final long serialVersionUID = -5112824748477345576L;
	
	public ApiException(String message) {
		super(new ApiResult("unknown", message).toJsonString());
	}

	public ApiException(String errcode, String errmsg) {
		super(new ApiResult(errcode, errmsg).toJsonString());
	}

	public static class ApiResult extends BaseBean{
		public String errcode;
		public String errmsg;
		public ApiResult(String code, String msg) {
			this.errcode = code;
			this.errmsg = msg;
		}
	}
}
