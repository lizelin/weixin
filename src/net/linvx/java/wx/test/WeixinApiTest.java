package net.linvx.java.wx.test;

import net.linvx.java.libs.utils.MyDateUtils;
import net.linvx.java.wx.api.ApiException;
import net.linvx.java.wx.api.BoProcessor;
import net.linvx.java.wx.api.WeixinApiImpl;
import net.linvx.java.wx.common.DataProvider;
import net.linvx.java.wx.model.Menu;
import net.sf.json.JSONObject;

public class WeixinApiTest {
	private WeixinApiImpl api = null;
	public WeixinApiTest(String code) {
		api = WeixinApiImpl.createApiToWxByAccountCode(code);
	}
	public WeixinApiImpl getApiInstance() {
		return api;
	}
	
	public JSONObject getMenuJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("     \"button\":[");
		sb.append("     {	");
		sb.append("          \"type\":\"click\",");
		sb.append("          \"name\":\"今日歌曲\",");
		sb.append("          \"key\":\"V1001_TODAY_MUSIC\"");
		sb.append("      },");
		sb.append("      {");
		sb.append("           \"name\":\"菜单\",");
		sb.append("           \"sub_button\":[");
		sb.append("           {	");
		sb.append("               \"type\":\"view\",");
		sb.append("               \"name\":\"搜索\",");
		sb.append("               \"url\":\"http://www.soso.com/\"");
		sb.append("            },");
		sb.append("            {");
		sb.append("               \"type\":\"view\",");
		sb.append("               \"name\":\"视频\",");
		sb.append("               \"url\":\"http://v.qq.com/\"");
		sb.append("            },");
		sb.append("            {");
		sb.append("               \"type\":\"click\",");
		sb.append("               \"name\":\"赞一下我们\",");
		sb.append("               \"key\":\"V1001_GOOD\"");
		sb.append("            }]");
		sb.append("       }]");
		sb.append(" }");
		return JSONObject.fromObject(sb.toString());
	}
	
	public static void main(String[] args) throws ApiException {
		WeixinApiTest apiTest = new WeixinApiTest("CODE");
		WeixinApiImpl api = apiTest.getApiInstance();
		int index = 1;
		System.out.println("test " + index++ +":");
		System.out.println(api.getTokenString());
		
		System.out.println("test " + index++ +":");
		System.out.println(api.getUserInfo("owJ2MjibJT9j6VqKTsFe4x29gOhY"));
		
		System.out.println("test " + index++ +":");
		System.out.println(api.createMenus(apiTest.getMenuJson()));
		
		System.out.println("test " + index++ +":");
		JSONObject menus = api.getMenus();
		System.out.println(menus.toString());
		
		System.out.println("test " + index++ +": 保存菜单");
		Menu[] marrs = BoProcessor.parseMenuJson(menus);
		if (marrs!=null && marrs.length>0) {
			marrs[0].setUrl(MyDateUtils.getTimeStamp());;
		}
		DataProvider.saveMenus(marrs, api.getAccount().getNumAccountGuid());
		
		System.out.println("test " + index++ +": 加载菜单");
		JSONObject json = BoProcessor.toMenuJson(DataProvider.loadMenus(api.getAccount().getNumAccountGuid()));
		
		api.createMenus(json);
		menus = api.getMenus();
		marrs = BoProcessor.parseMenuJson(menus);
		DataProvider.saveMenus(marrs, api.getAccount().getNumAccountGuid());
		
		System.out.println(api.createTempQrCode(600, 100001));
		
		System.out.println(api.createLimitQrCode(1));
		
		System.out.println("test " + index++ +": 发送客服消息");
		api.sendCustomTextMessage("owJ2MjibJT9j6VqKTsFe4x29gOhY", "hello li");
		System.out.println();
	}
	
}
