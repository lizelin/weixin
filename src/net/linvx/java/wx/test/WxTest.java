package net.linvx.java.wx.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import net.linvx.java.libs.conf.MyConfigReader;
import net.linvx.java.libs.db.MyDbHelper;
import net.linvx.java.libs.db.MyDbUtils;
import net.linvx.java.libs.enhance.BaseBean;
import net.linvx.java.libs.http.HttpUrl;
import net.linvx.java.libs.tools.CommonAssistant;
import net.linvx.java.libs.tools.MyLog;
import net.linvx.java.libs.utils.MyReflectUtils;
import net.linvx.java.wx.api.ApiUtils;
import net.linvx.java.wx.api.ProcessReceivedMsg;
import net.linvx.java.wx.bo.BoOfficialAccount;
import net.linvx.java.wx.common.DataProvider;
import net.linvx.java.wx.common.DbHelper;
import net.linvx.java.wx.msg.MsgEnums;
import net.sf.json.JSONObject;

public class WxTest {

	private static final Logger log = MyLog.getLogger(WxTest.class);

	public WxTest() {
		// TODO Auto-generated constructor stub
	}

	

	public static String test(HttpServletRequest r) throws Exception {
		return test6(r);
	}

	public static String test1(HttpServletRequest r) {
		String a = "";
		try {
			Connection db = DbHelper.getWxDb();
			a = MyDbUtils.genBO(db, "wx_sub_or_unsub_log", "net.linvx.java.wx.bo", "BoSubOrUnsubLog");
			MyDbUtils.closeConn(db);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return a;
	}

	@SuppressWarnings("deprecation")
	public static String test2(HttpServletRequest r) {
		log.debug("debug test");
		log.info("info test");
		log.error("error test");
		return r.getRealPath("/");
	}

	public static String test3(HttpServletRequest r) throws Exception {
		Class.forName("com.mysql.jdbc.Driver").newInstance(); // MYSQL驱动
		Connection conn = DriverManager.getConnection(
				"jdbc:mysql://127.0.0.1:3306/wx?generateSimpleParameterMetadata=true", "wxuser", "Mynormal12#");
		// 链接本地MYSQL
		MyDbUtils.closeConn(conn);
		return "OK";
	}

	public static String test4(HttpServletRequest r) {
		return new HttpUrl(r).getUrlString();
	}

	public static String test5(HttpServletRequest r) {
		return MsgEnums.MsgType.event.name();

	}

	public static String test6(HttpServletRequest r) {
		// 处理消息
		BoOfficialAccount account = DataProvider.getWxOfficialAccountByApiUrl("/wxnew/api/r.do");
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		sb.append(" <ToUserName><![CDATA[toUser]]></ToUserName>");
		sb.append(" <FromUserName><![CDATA[fromUser]]></FromUserName> ");
		sb.append(" <CreateTime>1348831860</CreateTime>");
		sb.append(" <MsgType><![CDATA[text]]></MsgType>");
		sb.append(" <Content><![CDATA[this is a test]]></Content>");
		sb.append(" <MsgId>1234567890123456</MsgId>");
		sb.append(" </xml>");
		String reqData = sb.toString();
		Element rootElt = ApiUtils.parseXml(reqData);
		ProcessReceivedMsg pm;
		try {
			pm = new ProcessReceivedMsg(account, rootElt).process();
			String reply = pm.getReplyData();
			// System.out.println(reply);
			System.out.println(MyReflectUtils.toJson(pm.getReceivedMsg(), false, true));
			return reply;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public static String test7(HttpServletRequest r){
		BoOfficialAccount account = DataProvider.getWxOfficialAccountByAccountCode("CODE");
		System.out.println(MyReflectUtils.toJson(account, false, true));
		return "";
	}
	
	public static String test8(HttpServletRequest r){
		BoOfficialAccount account = DataProvider.getWxOfficialAccountByAccountCode("CODE");
		System.out.println(JSONObject.fromObject(account).toString());
		return "";
	}
	
	public static String test9(HttpServletRequest r) {
		org.apache.commons.configuration.XMLConfiguration config = 
				MyConfigReader.getConfig("dbconfig");
		System.out.println(config);
		List<HierarchicalConfiguration> fields = 
			    config.configurationsAt("db");
			for(HierarchicalConfiguration sub : fields)
			{
				System.out.println(sub.getString("name"));
			}
		
		return "";
	}
	
	public static String test10(HttpServletRequest r) throws SQLException {
		System.out.println(MyDbHelper.getInstance().getConnection("wx"));
		return "";
	}
	public static void main(String[] args) throws SQLException {
		System.out.println(test10(null));
	}
}
