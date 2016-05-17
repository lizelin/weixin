package net.linvx.java.wx.msg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.linvx.java.libs.utils.MyStringUtils;

public class ReplyTextMsg extends ReplyMsgBase {
	public ReplyTextMsg(int accountGuid, String fromUserName, String toUserName) {
		super(accountGuid, fromUserName, toUserName);
	}

	private String content;
	
	public String getContent() {
		return content;
	}

	public ReplyTextMsg setContent(String content) {
		this.content = content;
		return this;
	}

	
	@Override
	protected String msgType() {
		return MsgEnums.MsgType.text.name();
	}

	@Override
	protected StringBuffer msgBody() {
		String content1 = content;
		if (MyStringUtils.isNotEmpty(content1)) {
			// 替换换行
			String regex = "<( *br */)>";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(content1);
			content1 = matcher.replaceAll("\n");
		}
		return new StringBuffer("<Content><![CDATA[" + content1
				+ "]]></Content>");
	}

}
