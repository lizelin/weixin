package net.linvx.java.wx.msg;

import java.util.List;

import net.linvx.java.libs.utils.MyStringUtils;


public class ReplyNewsMsg extends ReplyMsgBase{
	public ReplyNewsMsg(int accountGuid, String fromUserName, String toUserName) {
		super(accountGuid, fromUserName, toUserName);
	}

	private List<NewsMsgArticle> articles;
	
	
	@Override
	protected String msgType() {
		return MsgEnums.MsgType.news.name();
	}

	@Override
	protected StringBuffer msgBody() {
		StringBuffer sb = new StringBuffer();
		sb.append("<ArticleCount>" + articles.size() + "</ArticleCount>");
		sb.append("<Articles>");
		for (NewsMsgArticle article : articles) {
			sb.append("<item>");
			sb.append("<Title><![CDATA[" + article.getTitle() + "]]></Title>");
			if (articles.size() == 1
					&& MyStringUtils.isNotEmpty(article.getDescription())) {
				sb.append("<Description><![CDATA[" + article.getDescription()
						+ "]]></Description>");
			}
			sb.append("<PicUrl><![CDATA[" + article.getPicUrl()
					+ "]]></PicUrl>");
			String linkUrl = article.getLinkUrl();
			if (MyStringUtils.isNotEmpty(linkUrl)) {			
				sb.append("<Url><![CDATA[" + linkUrl + "]]></Url>");
			} else {
				sb.append("<Url></Url>");
			}
			sb.append("</item>");
		}
		sb.append("</Articles>");
		return sb;
	}

	public List<NewsMsgArticle> getArticles() {
		return articles;
	}

	public void setArticles(List<NewsMsgArticle> articles) {
		this.articles = articles;
	}

}
