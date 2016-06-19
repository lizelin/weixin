package net.linvx.java.wx.po;

public class PoWxUserStatus extends net.linvx.java.libs.enhance.BaseBean {
	private java.lang.Integer numUserGuid;
	private java.lang.Integer numAccountGuid;
	private java.lang.String vc2OpenId;
	private java.lang.String vc2SubscribeFlag;
	private java.lang.String vc2FirstQRSceneId;
	private java.sql.Timestamp datFirstSubscribeTime;
	private java.sql.Timestamp datLastSubscribeTime;
	private java.sql.Timestamp datLastUnSubscribeTime;
	private java.sql.Timestamp datCreation;
	private java.sql.Timestamp datLastUpdate;
	private java.lang.String vc2EnabledFlag;

	public java.lang.Integer getNumUserGuid() {
		return numUserGuid;
	}
	public PoWxUserStatus setNumUserGuid(java.lang.Integer p) {
		this.numUserGuid = p;
		return this;
	}
	public java.lang.Integer getNumAccountGuid() {
		return numAccountGuid;
	}
	public PoWxUserStatus setNumAccountGuid(java.lang.Integer p) {
		this.numAccountGuid = p;
		return this;
	}
	public java.lang.String getVc2OpenId() {
		return vc2OpenId;
	}
	public PoWxUserStatus setVc2OpenId(java.lang.String p) {
		this.vc2OpenId = p;
		return this;
	}
	public java.lang.String getVc2SubscribeFlag() {
		return vc2SubscribeFlag;
	}
	public PoWxUserStatus setVc2SubscribeFlag(java.lang.String p) {
		this.vc2SubscribeFlag = p;
		return this;
	}
	public java.lang.String getVc2FirstQRSceneId() {
		return vc2FirstQRSceneId;
	}
	public PoWxUserStatus setVc2FirstQRSceneId(java.lang.String p) {
		this.vc2FirstQRSceneId = p;
		return this;
	}
	public java.sql.Timestamp getDatFirstSubscribeTime() {
		return datFirstSubscribeTime;
	}
	public PoWxUserStatus setDatFirstSubscribeTime(java.sql.Timestamp p) {
		this.datFirstSubscribeTime = p;
		return this;
	}
	public java.sql.Timestamp getDatLastSubscribeTime() {
		return datLastSubscribeTime;
	}
	public PoWxUserStatus setDatLastSubscribeTime(java.sql.Timestamp p) {
		this.datLastSubscribeTime = p;
		return this;
	}
	public java.sql.Timestamp getDatLastUnSubscribeTime() {
		return datLastUnSubscribeTime;
	}
	public PoWxUserStatus setDatLastUnSubscribeTime(java.sql.Timestamp p) {
		this.datLastUnSubscribeTime = p;
		return this;
	}
	public java.sql.Timestamp getDatCreation() {
		return datCreation;
	}
	public PoWxUserStatus setDatCreation(java.sql.Timestamp p) {
		this.datCreation = p;
		return this;
	}
	public java.sql.Timestamp getDatLastUpdate() {
		return datLastUpdate;
	}
	public PoWxUserStatus setDatLastUpdate(java.sql.Timestamp p) {
		this.datLastUpdate = p;
		return this;
	}
	public java.lang.String getVc2EnabledFlag() {
		return vc2EnabledFlag;
	}
	public PoWxUserStatus setVc2EnabledFlag(java.lang.String p) {
		this.vc2EnabledFlag = p;
		return this;
	}
}