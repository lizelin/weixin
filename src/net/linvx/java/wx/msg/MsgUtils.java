package net.linvx.java.wx.msg;

import java.sql.Timestamp;

import net.linvx.java.libs.utils.MyStringUtils;
import net.linvx.java.wx.bo.BoOfficialAccount;
import net.linvx.java.wx.bo.BoReceivedMsg;

public class MsgUtils {
	

	public static BaseMsg createTextMsg(final BoOfficialAccount account, final BoReceivedMsg recvMsg, final String content) {
		BaseMsg replyMsg = new TextMsg(account.numAccountGuid, recvMsg.vc2ToUserName, recvMsg.vc2FromUserName)
				.setContent(content);
		recvMsg.vc2ReplyMsg = replyMsg.getCompleteMsgData();
		recvMsg.datReply = new Timestamp(System.currentTimeMillis());
		return replyMsg;
	}
	
	public static BaseMsg createWelcomeTextMsg(final BoOfficialAccount account, final BoReceivedMsg recvMsg) {
//		BaseMsg replyMsg = new TextMsg(account.numAccountGuid, recvMsg.vc2ToUserName, recvMsg.vc2FromUserName)
//				.setContent("Welcome to wx!");
//		return replyMsg;
		return MsgUtils.createTextMsg(account, recvMsg, "Welcome to wx!");
	}
	
	/**
	 * 是否推送消息（微信的）
	 * @param msg
	 * @return
	 */
	public static boolean isEventMsg(BoReceivedMsg msg) {
		return MsgEnums.MsgType.event.name().equalsIgnoreCase(msg.vc2MsgType);
	}
	
	
	public static boolean isSubscribeMsg(BoReceivedMsg msg) {
		return isEventMsg(msg) && MsgEnums.EventType.subscribe.name().equalsIgnoreCase(msg.attrs.get("Event"));
	}
	
	public static boolean isUnSubscribeMsg(BoReceivedMsg msg) {
		return isEventMsg(msg) && MsgEnums.EventType.unsubscribe.name().equalsIgnoreCase(msg.attrs.get("Event"));
	}
	
	public static boolean isSubscribeScanMsg(BoReceivedMsg msg) {
		return isSubscribeMsg(msg) && MyStringUtils.startWithIgnoreCase(msg.attrs.get("EventKey"), "qrscene_");
	}
	
	public static boolean isScanMsg(BoReceivedMsg msg) {
		return isEventMsg(msg) && MsgEnums.EventType.SCAN.name().equalsIgnoreCase(msg.attrs.get("Event"));
	}
	
	public static boolean isNeedReply(BoReceivedMsg msg) {
		return !MsgUtils.isEventMsg(msg)
				|| (MsgUtils.isEventMsg(msg) && (
						MsgEnums.EventType.CLICK.name().equalsIgnoreCase(msg.attrs.get("Event")) ||
						MsgEnums.EventType.SCAN.name().equalsIgnoreCase(msg.attrs.get("Event")) ||
						MsgEnums.EventType.subscribe.name().equalsIgnoreCase(msg.attrs.get("Event"))
						));
	}
}
