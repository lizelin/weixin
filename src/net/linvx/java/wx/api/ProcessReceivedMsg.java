package net.linvx.java.wx.api;

import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import net.linvx.java.libs.tools.MyLog;
import net.linvx.java.libs.utils.MyStringUtils;
import net.linvx.java.wx.bo.BoOfficialAccount;
import net.linvx.java.wx.bo.BoReceivedMsg;
import net.linvx.java.wx.bo.BoWeixinUser;
import net.linvx.java.wx.common.DataProvider;
import net.linvx.java.wx.msg.MsgUtils;
import net.sf.json.JSONObject;

/**
 * 处理接收到的消息，包括微信服务器推送的，还有用户上行的
 * 这里也只是一个总入口，仅仅处理和关注用户相关的消息，其他消息转到真正的业务处理url去处理（http post过去）
 * 这个业务处理url在wx_official_account表中定义：vc2RouterUrl
 * @author lizelin
 *
 */
public class ProcessReceivedMsg {
	private static final Logger log = MyLog.getLogger(ProcessReceivedMsg.class);
	protected BoOfficialAccount account = null;
	protected Element rootElt = null;
	protected BoReceivedMsg recvMsg = null;
	
	public ProcessReceivedMsg(BoOfficialAccount _account, Element _rootElt) {
		this.rootElt = _rootElt;
		this.account = _account;
		recvMsg = DaoProcessor.createReceivedMsgBo(account, rootElt);
	}
	
	public ProcessReceivedMsg process() throws ApiException {
		// 先保存一次原始数据，以防出错后错过入库时机
		saveReceivedMsg();
		
		// 该函数只处理关注和取消关注, 其他的调用业务处理url（account.vc2RouterUrl）处理
		if (MsgUtils.isSubscribeMsg(recvMsg)) {
			processSubscribeMsg();
		} else if (MsgUtils.isUnSubscribeMsg(recvMsg)) {
			processUnSubscribeMsg();
		} 
		
		String reply = "";
		if (MyStringUtils.isNotEmpty(account.vc2RouterUrl)) {
			try {
				reply = ApiUtils.post(account.vc2RouterUrl, recvMsg.vc2OriMsg);
			} catch (ApiException e) {
				e.printStackTrace();
				log.error("post routerUrl return error: ", e);
			}
		}
		
		if (MyStringUtils.isEmpty(reply) && MsgUtils.isNeedReply(recvMsg))
			reply = MsgUtils.createWelcomeTextMsg(account, recvMsg).getCompleteMsgData();
		
		/*
		 * 如果处理后，reply不为空，则设置msg
		 */
		if (MyStringUtils.isNotEmpty(reply)) {
			recvMsg.datReply = new Timestamp(System.currentTimeMillis());
			recvMsg.vc2ReplyMsg = reply;
		}
		
		// 将自动回复的消息入库
		saveReceivedMsg();
		return this;
	}
	
	private void processUnSubscribeMsg() {
		BoWeixinUser user1 = DataProvider.getWxUserByOpenId(account.numAccountGuid, recvMsg.vc2FromUserName);
		boolean isNewUser = (user1 == null);
		if (isNewUser) {
			user1 = DaoProcessor.createNewWxUserBo(account.numAccountGuid, recvMsg.vc2FromUserName);
			user1.numUserGuid = DataProvider.newWxUser(user1);
		}
		DataProvider.unsub(user1);
		DataProvider.logUserSubOrUnSub(user1.numUserGuid, "UNSUB", "");
	}
	
	private void processSubscribeMsg() throws ApiException {
		BoWeixinUser user1 = DataProvider.getWxUserByOpenId(account.numAccountGuid, recvMsg.vc2FromUserName);
		boolean isNewUser = (user1 == null);
		if (isNewUser) {
			user1 = DaoProcessor.createNewWxUserBo(account.numAccountGuid, recvMsg.vc2FromUserName);
			user1.numUserGuid = DataProvider.newWxUser(user1);			
		}
		JSONObject json =  WeixinApiImpl.createApiToWxByAccountCode(account.vc2AccountCode).getUserInfo(recvMsg.vc2FromUserName);
		user1 = DaoProcessor.getUpdateUserInfoBo(account.numAccountGuid, recvMsg.vc2FromUserName, json, isNewUser, recvMsg.attrs.get("EventKey"));
		DataProvider.updateWxUser(user1);
		DataProvider.logUserSubOrUnSub(user1.numUserGuid, "SUB", recvMsg.attrs.get("EventKey"));
	}

	public BoReceivedMsg getReceivedMsg() {
		return recvMsg;
	}
	
	private void saveReceivedMsg(){
		DataProvider.saveWxReceivedMsg(recvMsg);
	}
	
	public String getReplyData() {
		if (recvMsg == null || MyStringUtils.isEmpty(recvMsg.vc2ReplyMsg))
			return null;
		return recvMsg.vc2ReplyMsg;
	}
	
	public static void main(String[] args) {			
	}
}