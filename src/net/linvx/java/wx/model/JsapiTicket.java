package net.linvx.java.wx.model;

public class JsapiTicket {
	/**
	 * 凭证
	 */
	private String ticket;

	/**
	 * 凭证有效时间，单位：秒
	 */
	private int expires;

	/**
	 * 更新时间
	 */
	private long updateTime;

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public int getExpires() {
		return expires;
	}

	public void setExpires(int expires) {
		this.expires = expires;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	public boolean validate(){
		return getLocalExpires() > 0;
	}
	
	public long getLocalExpires(){
		return expires - (System.currentTimeMillis() - updateTime)/1000;
	}
	
}