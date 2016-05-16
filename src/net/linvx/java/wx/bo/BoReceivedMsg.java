package net.linvx.java.wx.bo;

import java.util.HashMap;
import java.util.Map;

public class BoReceivedMsg extends net.linvx.java.libs.enhance.BaseBean {
	private java.lang.Integer numMsgGuid;
	private java.lang.Integer numAccountGuid;
	private java.lang.String vc2ToUserName;
	private java.lang.String vc2FromUserName;
	private java.math.BigDecimal numWxCreateTime;
	private java.lang.String vc2MsgType;
	private java.sql.Timestamp datReceive;
	private java.lang.String vc2OriMsg;
	private java.sql.Timestamp datReply;
	private java.lang.String vc2ReplyMsg;
	private java.lang.String vc2MsgId;

	public Map<String, String> attrs = new HashMap<String, String>();

	public java.lang.Integer getNumMsgGuid() {
		return numMsgGuid;
	}

	public BoReceivedMsg setNumMsgGuid(java.lang.Integer p) {
		this.numMsgGuid = p;
		return this;
	}

	public java.lang.Integer getNumAccountGuid() {
		return numAccountGuid;
	}

	public BoReceivedMsg setNumAccountGuid(java.lang.Integer p) {
		this.numAccountGuid = p;
		return this;
	}

	public java.lang.String getVc2ToUserName() {
		return vc2ToUserName;
	}

	public BoReceivedMsg setVc2ToUserName(java.lang.String p) {
		this.vc2ToUserName = p;
		return this;
	}

	public java.lang.String getVc2FromUserName() {
		return vc2FromUserName;
	}

	public BoReceivedMsg setVc2FromUserName(java.lang.String p) {
		this.vc2FromUserName = p;
		return this;
	}

	public java.math.BigDecimal getNumWxCreateTime() {
		return numWxCreateTime;
	}

	public BoReceivedMsg setNumWxCreateTime(java.math.BigDecimal p) {
		this.numWxCreateTime = p;
		return this;
	}

	public java.lang.String getVc2MsgType() {
		return vc2MsgType;
	}

	public BoReceivedMsg setVc2MsgType(java.lang.String p) {
		this.vc2MsgType = p;
		return this;
	}

	public java.sql.Timestamp getDatReceive() {
		return datReceive;
	}

	public BoReceivedMsg setDatReceive(java.sql.Timestamp p) {
		this.datReceive = p;
		return this;
	}

	public java.lang.String getVc2OriMsg() {
		return vc2OriMsg;
	}

	public BoReceivedMsg setVc2OriMsg(java.lang.String p) {
		this.vc2OriMsg = p;
		return this;
	}

	public java.sql.Timestamp getDatReply() {
		return datReply;
	}

	public BoReceivedMsg setDatReply(java.sql.Timestamp p) {
		this.datReply = p;
		return this;
	}

	public java.lang.String getVc2ReplyMsg() {
		return vc2ReplyMsg;
	}

	public BoReceivedMsg setVc2ReplyMsg(java.lang.String p) {
		this.vc2ReplyMsg = p;
		return this;
	}

	public java.lang.String getVc2MsgId() {
		return vc2MsgId;
	}

	public BoReceivedMsg setVc2MsgId(java.lang.String p) {
		this.vc2MsgId = p;
		return this;
	}
}

// package net.linvx.java.wx.bo;
//
// import java.util.HashMap;
// import java.util.Map;
//
// import net.linvx.java.libs.enhance.BaseBean;
//
// public class BoReceivedMsg extends BaseBean{
// public java.lang.Integer numMsgGuid;
// public java.lang.Integer numAccountGuid;
// public java.lang.String vc2ToUserName;
// public java.lang.String vc2FromUserName;
// public java.math.BigDecimal numWxCreateTime;
// public java.lang.String vc2MsgType;
// public java.sql.Timestamp datReceive;
// public java.lang.String vc2OriMsg;
// public java.sql.Timestamp datReply;
// public java.lang.String vc2ReplyMsg;
// public java.lang.String vc2MsgId;
// public Map<String, String> attrs = new HashMap<String, String>();
// }
