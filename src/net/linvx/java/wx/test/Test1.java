package net.linvx.java.wx.test;

import java.sql.Connection;
import java.sql.SQLException;

import net.linvx.java.libs.db.MyDbUtils;
import net.linvx.java.wx.common.DbHelper;

public class Test1 {

	public static void main(String[] args) {
		genPo();
	}
	
	public static void genPo() {
		try {
			Connection db = DbHelper.getWxDb();
			String a = MyDbUtils.genPO(db, "wx_user_status", "net.linvx.java.wx.po", "PoWxUserStatus");
			System.out.println(a);
			a = MyDbUtils.genPO(db, "wx_user_info", "net.linvx.java.wx.po", "PoWxUserInfo");
			System.out.println(a);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
