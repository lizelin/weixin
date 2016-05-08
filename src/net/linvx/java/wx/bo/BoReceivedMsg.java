package net.linvx.java.wx.bo;

import java.util.HashMap;
import java.util.Map;

import net.linvx.java.libs.enhance.BaseBean;

public class BoReceivedMsg extends BaseBean{
	public java.lang.Integer numMsgGuid;
	public java.lang.Integer numAccountGuid;
	public java.lang.String vc2ToUserName;
	public java.lang.String vc2FromUserName;
	public java.math.BigDecimal numWxCreateTime;
	public java.lang.String vc2MsgType;
	public java.sql.Timestamp datReceive;
	public java.lang.String vc2OriMsg;
	public java.sql.Timestamp datReply;
	public java.lang.String vc2ReplyMsg;
	public java.lang.String vc2MsgId;
	public Map<String, String> attrs = new HashMap<String, String>();
}
