package net.linvx.java.wx.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import net.linvx.java.libs.db.MyDbUtils;
import net.linvx.java.libs.tools.MyLog;
import net.linvx.java.wx.bo.BoOfficialAccount;
import net.linvx.java.wx.bo.BoReceivedMsg;
import net.linvx.java.wx.bo.BoWeixinUser;
import net.linvx.java.wx.model.Menu;

/**
 * 数据库所有操作类
 * @author lizelin
 *
 */
public class DataProvider {
	private final static Logger log = MyLog.getLogger(DataProvider.class);
	/**
	 * 根据微信的api地址（即接收微信服务器推送消息的api url），获取服务号信息
	 * 将来可做cache
	 * @param apiUrl
	 * @return
	 */
	public static BoOfficialAccount getWxOfficialAccountByApiUrl(String apiUrl){
		BoOfficialAccount account = null;
		Connection db = null;
		try {
			db = DbHelper.getWxDb();
			account = MyDbUtils.getRow(db, "select * from wx_official_account where vc2ApiUrl = ?", new String[]{apiUrl}, BoOfficialAccount.class);
		} catch (Exception e) {
			log.error("", e);
			e.printStackTrace();
		} finally {
			MyDbUtils.closeConn(db);
		}
		return account;
	}
	/**
	 * 根据微信帐号的vc2AccountCode（即每个公众号分配一个更直观的code），获取服务号信息
	 * 将来可做cache
	 * @param accountCode
	 * @return
	 */
	public static BoOfficialAccount getWxOfficialAccountByAccountCode(String accountCode){
		BoOfficialAccount account = null;
		Connection db = null;
		try {
			db = DbHelper.getWxDb();
			account = MyDbUtils.getRow(db, "select * from wx_official_account where vc2AccountCode = ?", new String[]{accountCode}, BoOfficialAccount.class);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		}finally {
			MyDbUtils.closeConn(db);
		}
		return account;
	}
	
	/**
	 * 保存消息，包括收到的和返回给用户的（一条记录）
	 * @param msg
	 */
	public static void saveWxReceivedMsg(BoReceivedMsg msg) {
		Connection db = null;
		StringBuffer sb = new StringBuffer();
		String sqlSelect = "select numMsgGuid from wx_received_msg where vc2MsgId = ?";
		try {
			db = DbHelper.getWxDb();
			Integer msgGuid = MyDbUtils.getOne(db, sqlSelect, new String[]{msg.vc2MsgId});
			if (msgGuid == null) {
				sb.append("INSERT INTO `wx`.`wx_received_msg`");
				sb.append("	(`numAccountGuid`, `vc2ToUserName`, `vc2FromUserName`, `numWxCreateTime`,");
				sb.append("  `vc2MsgType`, `datReceive`, `vc2OriMsg`,`datReply`, ");
				sb.append("	 `vc2ReplyMsg`, `vc2MsgId`)");
				sb.append("VALUES");
				sb.append("	(?,?,?,?,");
				sb.append("	 ?,?,?,?,");
				sb.append("	 ?,?)");
				msgGuid = MyDbUtils.insertReturnKey(db, sb.toString(), new Object[] {
						msg.numAccountGuid, msg.vc2ToUserName, msg.vc2FromUserName, msg.numWxCreateTime,
						msg.vc2MsgType, msg.datReceive, msg.vc2OriMsg, msg.datReply,
						msg.vc2ReplyMsg, msg.vc2MsgId
				});
				msg.numMsgGuid = msgGuid;
			} else {
				sb.append("update `wx`.`wx_received_msg`");
				sb.append("	set `numAccountGuid`=?, `vc2ToUserName`=?, `vc2FromUserName`=?, `numWxCreateTime`=?,");
				sb.append("  `vc2MsgType`=?, `datReceive`=?, `vc2OriMsg`=?,`datReply`=?, ");
				sb.append("	 `vc2ReplyMsg`=? ");
				sb.append(" where vc2MsgId = ?");
				int rowcount = MyDbUtils.update(db, sb.toString(), new Object[] {
						msg.numAccountGuid, msg.vc2ToUserName, msg.vc2FromUserName, msg.numWxCreateTime,
						msg.vc2MsgType, msg.datReceive, msg.vc2OriMsg, msg.datReply,
						msg.vc2ReplyMsg, msg.vc2MsgId
				});
				if (rowcount <= 0) {
					log.error("update wx_received_msg error: msgid is " + msg.vc2MsgId );
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		} finally {
			MyDbUtils.closeConn(db);
		}
	}
	
	/**
	 * 获取用户Bo
	 * @param numAccountGuid
	 * @param openid
	 * @return
	 */
	public static BoWeixinUser getWxUserByOpenId(int numAccountGuid, String openid) {
		BoWeixinUser user = null;
		Connection db = null;
		try {
			db = DbHelper.getWxDb();
			user = MyDbUtils.getRow(db, "select * from wx_user where vc2OpenId = ? and numAccountGuid=?", new Object[]{openid, numAccountGuid}, BoWeixinUser.class);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		}finally {
			MyDbUtils.closeConn(db);
		}
		return user;
	}
	
	/**
	 * 创建用户（注意，该函数未判断是否用户存在），最主要的是openid字段
	 * @param user1
	 * @return
	 */
	public static int newWxUser(BoWeixinUser user1) {
		if (user1==null)
			return -1;
		Connection db = null;
		String sql = "insert into wx_user(datCreation, datLastUpdate, numAccountGuid, vc2EnabledFlag,"
				+ "		vc2OpenId, vc2SubscribeFlag) values ("
				+ "		?,?,?,?,?,?)";
		try {
			db = DbHelper.getWxDb();
			return MyDbUtils.insertReturnKey(db, sql, new Object[]{
					user1.datCreation, user1.datLastUpdate, user1.numAccountGuid, user1.vc2EnabledFlag,
					user1.vc2OpenId, user1.vc2SubscribeFlag
			});
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		}	finally {
			MyDbUtils.closeConn(db);
		} 
		return -1;
	}

	/**
	 * 修改用户，根据拉取用户信息接口
	 * @param user
	 */
	public static void updateWxUser(BoWeixinUser user) {
		if (user==null)
			return;
		Connection db = null;
		String sql = "update wx_user"
				+ "	set vc2SubscribeFlag = ?,"
				+ "	vc2NickName = ?,"
				+ " vc2SexCode = ?,"
				+ " vc2Language = ?,"
				+ " vc2CityName = ?,"
				+ " vc2ProvinceName = ?,"
				+ " vc2CountryName = ?,"
				+ " vc2HeadImgUrl = ?,"
				+ " datLastSubscribeTime = ?,"
				+ " vc2UnionId = ?,"
				+ " vc2Remark = ?,"
				+ " vc2GroupId = ?,"
				+ " datLastSyncFromWx = ?,"
				+ " datFirstSubscribeTime = ?,"
				+ " vc2FirstQRSceneId = ?"
				+ "	where numAccountGuid = ? and vc2OpenId = ? ";
		try {
			db = DbHelper.getWxDb();
			MyDbUtils.update(db, sql, new Object[]{
					user.vc2SubscribeFlag, user.vc2NickName, user.vc2SexCode, user.vc2Language,
					user.vc2CityName, user.vc2ProvinceName, user.vc2CountryName, user.vc2HeadImgUrl,
					user.datLastSubscribeTime, user.vc2UnionId, user.vc2Remark, user.vc2GroupId,
					user.datLastSyncFromWx, user.datFirstSubscribeTime, user.vc2FirstQRSceneId,
					user.numAccountGuid, user.vc2OpenId
			});
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		} finally {
			MyDbUtils.closeConn(db);
		} 
	}
	
	/**
	 * 记录用户的关注取关操作日志
	 * @param numUserGuid
	 * @param type
	 * @param sceneId
	 */
	public static void logUserSubOrUnSub(Integer numUserGuid, String type, String sceneId) {
		Connection db = null;
		String sql = "insert into wx_sub_or_unsub_log(numUserGuid,vc2SubOrUnSubFlag, vc2QRSceneId, datCreation)"
				+ "values(?,?,?,?)";
		
		try {
			db = DbHelper.getWxDb();
			MyDbUtils.update(db, sql, new Object[]{
					numUserGuid, type, sceneId, new Timestamp(System.currentTimeMillis())
			});
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		} finally {
			MyDbUtils.closeConn(db);
		} 
	}
	
	/**
	 * 取消关注
	 * @param user
	 */
	public static void unsub(BoWeixinUser user)  {
		if (user==null)
			return;
		Connection db = null;
		
		String sql = "update wx_user"
				+ "	set vc2SubscribeFlag = ?,"
				+ " datLastupdate = ?,"
				+ " datLastUnSubscribeTime = ? "
				+ " where numUserGuid = ? ";
		try {
			db = DbHelper.getWxDb();
			MyDbUtils.update(db, sql, new Object[]{
				"0", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), user.numUserGuid	
			});
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		} finally {
			MyDbUtils.closeConn(db);
		} 
	}
	/**
	 * 保存menus到数据库(注意，只支持两层！！！！)
	 * @param menus
	 */
	public static void saveMenus(Menu[] menus, Integer numAccountGuid) {
		if (menus==null || menus.length==0)
			return;
		Connection db = null;
		try {
			db = DbHelper.getWxDb();
			db.setAutoCommit(false);
			MyDbUtils.update(db, "delete from wx_menu where numAccountGuid = ?", new Object[]{numAccountGuid});
			String insertSql = "insert into wx_menu(`numAccountGuid`, `vc2MenuName`, `vc2MenuType`, "
					+ "`vc2MenuKey`, `vc2MenuUrl`, `numParentMenuGuid`, "
					+ "`datCreation`, `vc2CreatedBy`, `datLastUpdate`, "
					+ "`vc2LastUpdatedBy`, `vc2EnabledFlag`) values ("
					+ "?,?,?,"
					+ "?,?,?,"
					+ "now(),'0',now(),"
					+ "'0','Y')";
			for (int i=0; i<menus.length; i++) {
				Menu menu = menus[i];
				System.out.println(menu.getName());
				int menuGuid = MyDbUtils.insertReturnKey(db, insertSql, new Object[]{
						numAccountGuid, menu.getName(), menu.getType(),
						menu.getKey(), menu.getUrl(), 0
				});
				if (menu.getSub_button()!=null && menu.getSub_button().size() > 0) {
					for (int k=0; k<menu.getSub_button().size(); k++) {
						Menu menusub = menu.getSub_button().get(k);
						MyDbUtils.insertReturnKey(db, insertSql, new Object[]{
								numAccountGuid, menusub.getName(), menusub.getType(),
								menusub.getKey(), menusub.getUrl(), menuGuid
						});
					}
				}
			}
			db.commit();
		} catch (InstantiationException e) {
			e.printStackTrace();
			log.error("", e);
		} catch (IllegalAccessException e) {
			log.error("", e);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			log.error("", e);
			e.printStackTrace();
		} catch (SQLException e) {
			log.error("", e);
			e.printStackTrace();
		} catch (NamingException e) {
			log.error("", e);
			e.printStackTrace();
		} catch (Exception e) {
			log.error("", e);
			e.printStackTrace();
		} finally {
			MyDbUtils.closeConn(db);
		}
		
	}
	
	/**
	 * 获取菜单
	 * @param numAccountGuid
	 * @return
	 */
	public static Menu[] loadMenus(Integer numAccountGuid) {
		List<Menu> menus = new ArrayList<Menu>();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT `numMenuGuid`,");
		sb.append("    `numAccountGuid`,");
		sb.append("    `vc2MenuName`,");
		sb.append("    `vc2MenuType`,");
		sb.append("    `vc2MenuKey`,");
		sb.append("    `vc2MenuUrl`,");
		sb.append("    `numParentMenuGuid` ");
		sb.append("	FROM `wx_menu`");
		sb.append("	where vc2EnabledFlag = 'Y' and numAccountGuid = ? ");
		sb.append("	and numParentMenuGuid = ?");
		Connection db = null;
		try {
			db = DbHelper.getWxDb();
			PreparedStatement ps = db.prepareStatement(sb.toString());
			ps.setInt(1, numAccountGuid.intValue());
			ps.setInt(2, 0);
			ResultSet rs = ps.executeQuery(); 
			while (rs.next()) {
				Menu m = new Menu();
				m.setKey(rs.getString("vc2MenuKey"));
				m.setName(rs.getString("vc2MenuName"));
				m.setType(rs.getString("vc2MenuType"));
				m.setUrl(rs.getString("vc2MenuUrl"));
				m.setSub_button(new ArrayList<Menu>());
				PreparedStatement pssub = db.prepareStatement(sb.toString());
				pssub.setInt(1, numAccountGuid.intValue());
				pssub.setInt(2, rs.getInt("numMenuGuid"));
				ResultSet rssub = pssub.executeQuery(); 
				while (rssub.next()) {
					Menu msub = new Menu();
					msub.setKey(rssub.getString("vc2MenuKey"));
					msub.setName(rssub.getString("vc2MenuName"));
					msub.setType(rssub.getString("vc2MenuType"));
					msub.setUrl(rssub.getString("vc2MenuUrl"));
					m.getSub_button().add(msub);
				}
				rssub.close();
				pssub.close();
				menus.add(m);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			log.error("", e);
			e.printStackTrace();
		} finally {
			MyDbUtils.closeConn(db);
		}
		
		return menus.toArray(new Menu[]{});
	}
}
