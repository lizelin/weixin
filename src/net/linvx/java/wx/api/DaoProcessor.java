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
 * @author lizelin
 *
 */
public class DaoProcessor {
	/**
	 * 根据微信传来的xml，生成消息
	 * @param account
	 * @param rootElt
	 * @return
	 */
	public static BoReceivedMsg createReceivedMsgBo(BoOfficialAccount account, Element rootElt) {
		BoReceivedMsg recvMsg = new BoReceivedMsg();
		recvMsg.datReceive = new Timestamp(System.currentTimeMillis());
		recvMsg.datReply = null;
		recvMsg.numAccountGuid = account.numAccountGuid;
		recvMsg.numWxCreateTime = BigDecimal.valueOf(Long.parseLong(rootElt.elementText("CreateTime")));
		recvMsg.vc2FromUserName = rootElt.elementText("FromUserName");
		recvMsg.vc2MsgType = rootElt.elementText("MsgType");
		recvMsg.vc2OriMsg = rootElt.asXML();
		recvMsg.vc2ReplyMsg = null;
		recvMsg.vc2ToUserName = rootElt.elementText("ToUserName");
		String msgId = rootElt.elementText("MsgId");
		if (MyStringUtils.isEmpty(msgId)) {
			msgId = recvMsg.vc2FromUserName + rootElt.elementText("CreateTime");
		}
		recvMsg.vc2MsgId = msgId;

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
	 * @param numAccountGuid
	 * @param openId
	 * @return
	 */
	public static BoWeixinUser createNewWxUserBo(int numAccountGuid, String openId) {
		BoWeixinUser user1 = new BoWeixinUser();
		user1.datCreation = new Timestamp(System.currentTimeMillis());
		user1.datLastUpdate =  new Timestamp(System.currentTimeMillis());
		user1.numAccountGuid = numAccountGuid;
		user1.numUserGuid = 0;
		user1.vc2EnabledFlag = "Y";
		user1.vc2OpenId = openId;
		user1.vc2SubscribeFlag = "0";
		
		return user1;
	}
	
	/**
	 * 创建更新用户的bo，根据json（json是拉取用户接口获得的数据）
	 * @param numAccountGuid
	 * @param openid
	 * @param json
	 * @param isNewuser
	 * @param sceneId
	 * @return
	 */
	public static BoWeixinUser getUpdateUserInfoBo(int numAccountGuid, String openid, JSONObject json, boolean isNewuser, String sceneId) {
		BoWeixinUser user = DataProvider.getWxUserByOpenId(numAccountGuid, openid);
		if (user==null)
			return null;
		user.vc2SubscribeFlag = json.optString("subscribe", "0");
		if ("1".equals(user.vc2SubscribeFlag)) {
			user.vc2NickName = json.optString("nickname");
			user.vc2SexCode = json.optString("sex");
			user.vc2Language = json.optString("language");
			user.vc2CityName = json.optString("city");
			user.vc2ProvinceName = json.optString("province");
			user.vc2CountryName = json.optString("country");
			user.vc2HeadImgUrl = json.optString("headimgurl");
			user.datLastSubscribeTime = new Timestamp(Long.valueOf(json.optLong("subscribe_time"))*1000l);
			user.vc2UnionId = json.optString("unionid");
			user.vc2Remark = json.optString("remark");
			user.vc2GroupId = json.optString("groupid");
			user.datLastSyncFromWx = new Timestamp(System.currentTimeMillis());
			if (user.datFirstSubscribeTime==null)
				user.datFirstSubscribeTime = user.datLastSubscribeTime;
			if (isNewuser){
				user.vc2FirstQRSceneId = sceneId;
			} 
		}
		return user;
	}
	
	/**
	 * 将菜单json数组转为 menu类数组
	 * @param json
	 * @return
	 */
	public static Menu[] parseMenuJson(JSONObject json) {
		JSONArray arr = json.optJSONObject("menu").optJSONArray("button");
		Map<String, Class<?>> classMap = new HashMap<String, Class<?>>();
		classMap.put("sub_button", Menu.class);
		return (Menu[]) JSONArray.toArray(arr,
				Menu.class, classMap);
	}
	
	/**
	 * 转成微信要求的格式
	 * @param menus
	 * @return
	 */
	public static JSONObject toMenuJson(Menu[] menus) {
		JSONObject jsonData = new JSONObject();
		jsonData.put("button", JSONArray.fromObject(menus));
		return jsonData;
	}
	
}
