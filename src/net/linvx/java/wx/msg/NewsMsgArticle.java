package net.linvx.java.wx.msg;

/**
 * 图文消息条目
 * 
 * @author
 * 
 */
public class NewsMsgArticle {

	private String title;

	private String description;

	private String picUrl;

	private String linkUrl;

	public String getTitle() {
		return title;
	}

	public NewsMsgArticle setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public NewsMsgArticle setDescription(String description) {
		this.description = description;
		return this;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public NewsMsgArticle setPicUrl(String picUrl) {
		this.picUrl = picUrl;
		return this;
	}

	public String getLinkUrl() {
		return linkUrl;
	}

	public NewsMsgArticle setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
		return this;
	}

}
