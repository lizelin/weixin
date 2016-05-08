/**
 * 
 */
package net.linvx.java.wx.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import net.linvx.java.libs.http.HttpUrl;
import net.linvx.java.libs.tools.MyLog;
import net.linvx.java.libs.utils.MyStringUtils;
import net.linvx.java.wx.bo.BoOfficialAccount;
import net.linvx.java.wx.common.DataProvider;

/**
 * 接收微信服务器推送的消息的总入口
 * @author lizelin
 *
 */
public class MsgReceiverServlet extends javax.servlet.http.HttpServlet {
	
	private static final long serialVersionUID = 799971996100904594L;
	private static final Logger log = MyLog.getLogger(MsgReceiverServlet.class);
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		BoOfficialAccount account = DataProvider.getWxOfficialAccountByApiUrl(req.getRequestURI());
		// 检查服务号配置信息
		if (!ApiUtils.checkOfficialAccount(req, account))
			return;
		
		// 检查签名
		if (!ApiUtils.checkSignOfMsgFromWx(req, account.vc2ApiToken))
			return;
		
		// 处理微信服务器apiurl验证信息, 直接返回echostr，代表接口url有效
		if (MyStringUtils.isNotEmpty(req.getParameter("echostr"))){
			ApiUtils.writeResp(resp, req.getParameter("echostr"));
			return;
		}
		
		/**
		 * 获取post数据
		 */
		String reqData = ApiUtils.getRequestData(req);
		if (MyStringUtils.isEmpty(reqData)) {
			log.error("receive request data is empty!");
			return;
		}
		log.info("request uri is :" + new HttpUrl(req).getUrlString());
		log.info(reqData);
		
		/**
		 * 解析xml；
		 */
		Element rootElt = ApiUtils.parseXml(reqData);
		if (rootElt == null) {
			log.error("request xml is null");
			return;
		}
		
		//处理消息
		ProcessReceivedMsg pm = new ProcessReceivedMsg(account, rootElt);
		try {
			pm.process();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ProcessReceivedMsg.process error: ", e);
		}
		String reply = pm.getReplyData();
		
		//如果回复为空，则直接回复success
		if (MyStringUtils.isEmpty(reply)) {
			log.error("reply data is null and set reply = success");
			reply = "success";
		}
		
		log.info("response is : \n" + reply);
		
		// 返回消息处理结果
		ApiUtils.writeResp(resp, reply);

		return;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	
}
