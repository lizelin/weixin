package net.linvx.java.wx.api;

import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import net.linvx.java.libs.tools.MyLog;
import net.linvx.java.libs.utils.MyStringUtils;
import net.linvx.java.wx.common.CommonUtils;
import net.linvx.java.wx.common.DataProvider;
import net.linvx.java.wx.msg.ReplyMsgUtils;
import net.linvx.java.wx.po.PoOfficialAccount;
import net.linvx.java.wx.po.PoReceivedMsg;
import net.linvx.java.wx.po.PoWeixinUser;
import net.linvx.java.wx.po.PoWxUserStatus;
import net.sf.json.JSONObject;

/**
 * 处理接收到的消息，包括微信服务器推送的，还有用户上行的
 * 这里也只是一个总入口，仅仅处理和关注用户相关的消息，其他消息转到真正的业务处理url去处理（http post过去）
 * 这个业务处理url在wx_official_account表中定义：vc2RouterUrl
 * 
 * @author lizelin
 *
 */
public class ProcessReceivedMsg {
	private static final Logger log = MyLog.getLogger(ProcessReceivedMsg.class);
	protected PoOfficialAccount account = null;
	protected Element rootElt = null;
	protected PoReceivedMsg recvMsg = null;

	public ProcessReceivedMsg(PoOfficialAccount _account, Element _rootElt) {
		this.rootElt = _rootElt;
		this.account = _account;
		recvMsg = BoProcessor.createReceivedMsgPo(account, rootElt);
	}

	public ProcessReceivedMsg process() throws ApiException {
		// 先保存一次原始数据，以防出错后错过入库时机
		saveReceivedMsg();

		// 该函数只处理关注和取消关注, 其他的调用业务处理url（account.vc2RouterUrl）处理
		if (ReplyMsgUtils.isSubscribeMsg(recvMsg)) {
			processSubscribeMsg();
		} else if (ReplyMsgUtils.isUnSubscribeMsg(recvMsg)) {
			processUnSubscribeMsg();
		}

		String reply = "";
		if (MyStringUtils.isNotEmpty(account.getVc2RouterUrl())) {
			try {
				reply = ApiUtils.post(account.getVc2RouterUrl(), recvMsg.getVc2OriMsg());
			} catch (ApiException e) {
				e.printStackTrace();
				log.error("post routerUrl return error: ", e);
			}
		}

		if (MyStringUtils.isEmpty(reply) && ReplyMsgUtils.isNeedReply(recvMsg))
			reply = ReplyMsgUtils.createWelcomeTextMsg(account, recvMsg).getCompleteMsgData();

		/*
		 * 如果处理后，reply不为空，则设置msg
		 */
		if (MyStringUtils.isNotEmpty(reply)) {
			recvMsg.setDatReply(new Timestamp(System.currentTimeMillis()));
			recvMsg.setVc2ReplyMsg(reply);
		}

		// 将自动回复的消息入库
		saveReceivedMsg();
		return this;
	}

	/**
	 * 处理取消关注消息
	 */
	private void processUnSubscribeMsg() {
		PoWeixinUser user1 = DataProvider.getWxUserByOpenId(account.getNumAccountGuid(), recvMsg.getVc2FromUserName());
		boolean isNewUser = (user1 == null);
		if (isNewUser) {
			user1 = BoProcessor.createNewWxUserBo(account.getNumAccountGuid(), recvMsg.getVc2FromUserName());
			user1.setNumUserGuid(DataProvider.newWxUser(user1));
		}
		DataProvider.unsub(user1);
		DataProvider.logUserSubOrUnSub(user1.getNumUserGuid(), "UNSUB", "");
	}

	/**
	 * 处理关注消息
	 * @throws ApiException
	 */
	private void processSubscribeMsg() throws ApiException {
		PoWeixinUser user1 = DataProvider.getWxUserByOpenId(account.getNumAccountGuid(), recvMsg.getVc2FromUserName());
		boolean isNewUser = (user1 == null);
		if (isNewUser) {
			String openId = recvMsg.getVc2FromUserName();
			Timestamp now = CommonUtils.now();
			user1 = new PoWeixinUser();
			user1.setDatCreation(now)
				.setDatFirstSubscribeTime(now)
				.setDatLastSubscribeTime(now)
				.setDatLastUnSubscribeTime(null)
				.setDatLastUpdate(now)
				.setNumAccountGuid(account.getNumAccountGuid())
				.setNumUserGuid(0)
				.setVc2EnabledFlag("Y")
				.setVc2FirstQRSceneId(recvMsg.attrs.get("EventKey"))
				.setVc2OpenId(openId)
				.setVc2SubscribeFlag("1");
			user1.setNumUserGuid(DataProvider.newWxUser(user1));
		}
		JSONObject json = WeixinApiImpl.createApiToWxByAccountCode(account.getVc2AccountCode())
				.getUserInfo(recvMsg.getVc2FromUserName());
		user1 = BoProcessor.getUpdateUserInfoBo(account.getNumAccountGuid(), recvMsg.getVc2FromUserName(), json,
				isNewUser, recvMsg.attrs.get("EventKey"));
		DataProvider.updateWxUser(user1);
		DataProvider.logUserSubOrUnSub(user1.getNumUserGuid(), "SUB", recvMsg.attrs.get("EventKey"));
	}

	public PoReceivedMsg getReceivedMsg() {
		return recvMsg;
	}

	private void saveReceivedMsg() {
		DataProvider.saveWxReceivedMsg(recvMsg);
	}

	/**
	 * 回复给用户的消息字符串
	 * @return
	 */
	public String getReplyData() {
		if (recvMsg == null || MyStringUtils.isEmpty(recvMsg.getVc2ReplyMsg()))
			return null;
		return recvMsg.getVc2ReplyMsg();
	}

	public static void main(String[] args) {
	}
}
