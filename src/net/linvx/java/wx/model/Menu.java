package net.linvx.java.wx.model;

import java.util.List;

public class Menu {
//	public enum MenuType {
//		click,
//		view,
//		scancode_push,
//		scancode_waitmsg,
//		pic_sysphoto,
//		pic_photo_or_album,
//		pic_weixin,
//		location_select,
//		media_id,
//		view_limited
//	}

	private String name;

	private String type;

	private String key;

	private String url;

	private List<Menu> sub_button;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
		
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
		
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		
	}

	public List<Menu> getSub_button() {
		return sub_button;
	}

	public void setSub_button(List<Menu> sub_button) {
		this.sub_button = sub_button;
		
	}
}