package net.linvx.java.wx.api;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Element;

import net.linvx.java.libs.utils.MyStringUtils;
import net.linvx.java.wx.bo.BoOfficialAccount;
import net.linvx.java.wx.bo.BoReceivedMsg;
import net.linvx.java.wx.bo.BoWeixinUser;
import net.linvx.java.wx.common.DataProvider;
import net.linvx.java.wx.model.Menu;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * BO的处理类，比如生成消息bo、用户bo等
 * 
 * @author lizelin
 *
 */
public class BoProcessor {
	/**
	 * 根据微信传来的xml，生成消息
	 * 
	 * @param account
	 * @param rootElt
	 * @return
	 */
	public static BoReceivedMsg createReceivedMsgBo(BoOfficialAccount account, Element rootElt) {
		BoReceivedMsg recvMsg = new BoReceivedMsg();
		recvMsg.setDatReceive(new Timestamp(System.currentTimeMillis()));
		recvMsg.setDatReply(null);
		recvMsg.setNumAccountGuid(account.getNumAccountGuid());
		recvMsg.setNumWxCreateTime(BigDecimal.valueOf(Long.parseLong(rootElt.elementText("CreateTime"))));
		recvMsg.setVc2FromUserName(rootElt.elementText("FromUserName"));
		recvMsg.setVc2MsgType(rootElt.elementText("MsgType"));
		recvMsg.setVc2OriMsg(rootElt.asXML());
		recvMsg.setVc2ReplyMsg(null);
		recvMsg.setVc2ToUserName(rootElt.elementText("ToUserName"));
		String msgId = rootElt.elementText("MsgId");
		if (MyStringUtils.isEmpty(msgId)) {
			msgId = recvMsg.getVc2FromUserName() + rootElt.elementText("CreateTime");
		}
		recvMsg.setVc2MsgId(msgId);

		Map<String, String> msgAttr = recvMsg.attrs;
		// if (MyStringUtils.isNotEmpty(rootElt.elementText("Event")))
		msgAttr.put("Event", rootElt.elementText("Event"));
		// if (MyStringUtils.isNotEmpty(rootElt.elementText("EventKey")))
		msgAttr.put("EventKey", rootElt.elementText("EventKey"));
		// if (MyStringUtils.isNotEmpty(rootElt.elementText("Content")))
		msgAttr.put("Content", rootElt.elementText("Content"));
		// if (MyStringUtils.isNotEmpty(rootElt.elementText("Ticket")))
		msgAttr.put("Ticket", rootElt.elementText("Ticket"));
		// if (MyStringUtils.isNotEmpty(rootElt.elementText("Latitude")))
		msgAttr.put("Latitude", rootElt.elementText("Latitude"));
		// if (MyStringUtils.isNotEmpty(rootElt.elementText("Longitude")))
		msgAttr.put("Longitude", rootElt.elementText("Longitude"));
		// if (MyStringUtils.isNotEmpty(rootElt.elementText("Precision")))
		msgAttr.put("Precision", rootElt.elementText("Precision"));
		// if (MyStringUtils.isNotEmpty(rootElt.elementText("OrderId")))
		msgAttr.put("OrderId", rootElt.elementText("OrderId"));
		// if (MyStringUtils.isNotEmpty(rootElt.elementText("OrderStatus")))
		msgAttr.put("OrderStatus", rootElt.elementText("OrderStatus"));
		// if (MyStringUtils.isNotEmpty(rootElt.elementText("ProductId")))
		msgAttr.put("ProductId", rootElt.elementText("ProductId"));
		// if (MyStringUtils.isNotEmpty(rootElt.elementText("SkuInfo")))
		msgAttr.put("SkuInfo", rootElt.elementText("SkuInfo"));
		return recvMsg;
	}

	/**
	 * 创建新微信用户bo，主要是openid字段
	 * 
	 * @param numAccountGuid
	 * @param openId
	 * @return
	 */
	public static BoWeixinUser createNewWxUserBo(int numAccountGuid, String openId) {
		BoWeixinUser user1 = new BoWeixinUser();
		user1.setDatCreation(new Timestamp(System.currentTimeMillis()));
		user1.setDatLastUpdate(new Timestamp(System.currentTimeMillis()));
		user1.setNumAccountGuid(numAccountGuid);
		user1.setNumUserGuid(0);
		user1.setVc2EnabledFlag("Y");
		user1.setVc2OpenId(openId);
		user1.setVc2SubscribeFlag("0");

		return user1;
	}

	/**
	 * 创建更新用户的bo，根据json（json是拉取用户接口获得的数据）
	 * 
	 * @param numAccountGuid
	 * @param openid
	 * @param json		从微信获取的用户信息
	 * @param isNewuser
	 * @param sceneId
	 * @return
	 */
	public static BoWeixinUser getUpdateUserInfoBo(int numAccountGuid, String openid, JSONObject json,
			boolean isNewuser, String sceneId) {
		BoWeixinUser user = DataProvider.getWxUserByOpenId(numAccountGuid, openid);
		if (user == null)
			return null;
		user.setVc2SubscribeFlag(json.optString("subscribe", "0"));
		if ("1".equals(user.getVc2SubscribeFlag())) {
			user.setVc2NickName(json.optString("nickname"));
			user.setVc2SexCode(json.optString("sex"));
			user.setVc2Language(json.optString("language"));
			user.setVc2CityName(json.optString("city"));
			user.setVc2ProvinceName(json.optString("province"));
			user.setVc2CountryName(json.optString("country"));
			user.setVc2HeadImgUrl(json.optString("headimgurl"));
			user.setDatLastSubscribeTime(new Timestamp(Long.valueOf(json.optLong("subscribe_time")) * 1000l));
			user.setVc2UnionId(json.optString("unionid"));
			user.setVc2Remark(json.optString("remark"));
			user.setVc2GroupId(json.optString("groupid"));
			user.setDatLastSyncFromWx(new Timestamp(System.currentTimeMillis()));
			if (user.getDatFirstSubscribeTime() == null)
				user.setDatFirstSubscribeTime(user.getDatLastSubscribeTime());
			if (isNewuser) {
				user.setVc2FirstQRSceneId(sceneId);
			}
		}
		return user;
	}

	/**
	 * 将菜单json数组转为 menu类数组
	 * 
	 * @param json
	 * @return
	 */
	public static Menu[] parseMenuJson(JSONObject json) {
		JSONArray arr = json.optJSONObject("menu").optJSONArray("button");
		Map<String, Class<?>> classMap = new HashMap<String, Class<?>>();
		classMap.put("sub_button", Menu.class);
		return (Menu[]) JSONArray.toArray(arr, Menu.class, classMap);
	}

	/**
	 * 转成微信要求的格式
	 * 
	 * @param menus
	 * @return
	 */
	public static JSONObject toMenuJson(Menu[] menus) {
		JSONObject jsonData = new JSONObject();
		jsonData.put("button", JSONArray.fromObject(menus));
		return jsonData;
	}

}
