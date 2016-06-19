package net.linvx.java.wx.common;

import java.sql.Timestamp;

public class CommonUtils {
	public static Timestamp now() {
		return new Timestamp(System.currentTimeMillis());
	}
	
	public static Timestamp toTimestamp(long millis) {
		return new Timestamp(millis);
	}
}
