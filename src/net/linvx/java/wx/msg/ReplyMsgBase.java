package net.linvx.java.wx.msg;

import java.util.Date;

public abstract class ReplyMsgBase {
	private int accountGuid;
	private String fromUserName;
	private String toUserName;
	private long createTime;
	
	public ReplyMsgBase(int accountGuid, String fromUserName, String toUserName) {
		super();
		this.accountGuid = accountGuid;
		this.fromUserName = fromUserName;
		this.toUserName = toUserName;
		this.createTime = new Date().getTime();
	}
	
	public int getAccountGuid() {
		return accountGuid;
	}

	public String getFromUserName() {
		return fromUserName;
	}

	public String getToUserName() {
		return toUserName;
	}

	public long getCreateTime() {
		return createTime;
	}

	public String getCompleteMsgData() {
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		sb.append("<ToUserName><![CDATA[" + toUserName + "]]></ToUserName>");
		sb.append("<FromUserName><![CDATA[" + fromUserName
				+ "]]></FromUserName>");
		sb.append("<CreateTime>" + createTime + "</CreateTime>");
		sb.append("<MsgType><![CDATA[" + msgType() + "]]></MsgType>");
		sb.append(msgBody());
		sb.append("</xml>");
		return sb.toString();
	}
	
	protected abstract String msgType();

	protected abstract StringBuffer msgBody();
	
	
}
