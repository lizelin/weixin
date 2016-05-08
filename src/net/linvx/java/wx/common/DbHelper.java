/**
 * 
 */
package net.linvx.java.wx.common;

import java.sql.Connection;
import java.sql.SQLException;

import net.linvx.java.libs.db.MyDbHelper;

/**
 * @author lizelin
 *
 */
public class DbHelper {
	public static Connection getWxDb() throws SQLException{
		return MyDbHelper.getInstance().getConnection("wx");
	}
}
