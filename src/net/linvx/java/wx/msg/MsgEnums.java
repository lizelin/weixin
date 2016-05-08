package net.linvx.java.wx.msg;

public interface MsgEnums {
	/**
	 * 消息类型对应MsgType字段
	 * @author lizelin
	 *
	 */
	public enum MsgType {
		text,
		image,
		voice,
		video,
		shortvideo,
		location,
		link,
		event,	//推送消息
		news,	// 回复消息特有的类型，在推送的普通消息没有此类型
	}
	
	/**
	 * 推送消息类型
	 * @author lizelin
	 *
	 */
	public enum EventType {
		subscribe,
		unsubscribe,
		SCAN,
		LOCATION,
		// 以下是菜单的
		CLICK, 
		VIEW,
		scancode_push, //扫码推事件的事件推送 
		scancode_waitmsg, // 扫码推事件且弹出“消息接收中”提示框的事件推送 
		pic_sysphoto, //弹出系统拍照发图的事件推送
		pic_photo_or_album, // 弹出拍照或者相册发图的事件推送
		pic_weixin, // 弹出微信相册发图器的事件推送
		location_select // 弹出地理位置选择器的事件推送
	}
	 
}
