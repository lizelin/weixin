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
import net.linvx.java.libs.utils.MyStringUtils;
import net.linvx.java.wx.api.ApiException;
import net.linvx.java.wx.model.Menu;
import net.linvx.java.wx.po.PoOfficialAccount;
import net.linvx.java.wx.po.PoReceivedMsg;
import net.linvx.java.wx.po.PoWeixinUser;
import net.linvx.java.wx.po.PoWxUserInfo;
import net.linvx.java.wx.po.PoWxUserStatus;

/**
 * 数据库所有操作类
 * 
 * @author lizelin
 *
 */
public class DataProvider {
	private final static Logger log = MyLog.getLogger(DataProvider.class);

	/**
	 * 根据微信的api地址（即接收微信服务器推送消息的api url），获取服务号信息 将来可做cache
	 * 
	 * @param apiUrl
	 * @return
	 */
	public static PoOfficialAccount getWxOfficialAccountByApiUrl(String apiUrl) {
		PoOfficialAccount account = null;
		Connection db = null;
		try {
			db = DbHelper.getWxDb();
			account = MyDbUtils.getRow(db, "select * from wx_official_account where vc2ApiUrl = ?",
					new String[] { apiUrl }, PoOfficialAccount.class);
		} catch (Exception e) {
			log.error("", e);
			e.printStackTrace();
		} finally {
			MyDbUtils.closeQuietly(db);
		}
		return account;
	}

	/**
	 * 根据微信帐号的vc2AccountCode（即每个公众号分配一个更直观的code），获取服务号信息 将来可做cache
	 * 
	 * @param accountCode
	 * @return
	 */
	public static PoOfficialAccount getWxOfficialAccountByAccountCode(String accountCode) {
		PoOfficialAccount account = null;
		Connection db = null;
		try {
			db = DbHelper.getWxDb();
			account = MyDbUtils.getRow(db, "select * from wx_official_account where vc2AccountCode = ?",
					new String[] { accountCode }, PoOfficialAccount.class);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		} finally {
			MyDbUtils.closeQuietly(db);
		}
		return account;
	}

	/**
	 * 保存消息，包括收到的和返回给用户的（一条记录）
	 * 
	 * @param msg
	 */
	public static void saveWxReceivedMsg(PoReceivedMsg msg) {
		Connection db = null;
		StringBuffer sb = new StringBuffer();
		String sqlSelect = "select numMsgGuid from wx_received_msg where vc2MsgId = ? and numAccountGuid = ?";
		try {
			db = DbHelper.getWxDb();
			Integer msgGuid = MyDbUtils.getOne(db, sqlSelect, new Object[] { msg.getVc2MsgId(), msg.getNumAccountGuid() });
			if (msgGuid == null) {
				sb.append("INSERT INTO `wx_received_msg`");
				sb.append("	(`numAccountGuid`, `vc2ToUserName`, `vc2FromUserName`, `numWxCreateTime`,");
				sb.append("  `vc2MsgType`, `datReceive`, `vc2OriMsg`,`datReply`, ");
				sb.append("	 `vc2ReplyMsg`, `vc2MsgId`)");
				sb.append("VALUES");
				sb.append("	(?,?,?,?,");
				sb.append("	 ?,?,?,?,");
				sb.append("	 ?,?)");
				msgGuid = MyDbUtils.insertReturnKey(db, sb.toString(),
						new Object[] { msg.getNumAccountGuid(), msg.getVc2ToUserName(), msg.getVc2FromUserName(),
								msg.getNumWxCreateTime(), msg.getVc2MsgType(), msg.getDatReceive(), msg.getVc2OriMsg(),
								msg.getDatReply(), msg.getVc2ReplyMsg(), msg.getVc2MsgId() });
				msg.setNumMsgGuid(msgGuid);
			} else {
				sb.append("update `wx_received_msg`");
				sb.append("	set `numAccountGuid`=?, `vc2ToUserName`=?, `vc2FromUserName`=?, `numWxCreateTime`=?,");
				sb.append("  `vc2MsgType`=?, `datReceive`=?, `vc2OriMsg`=?,`datReply`=?, ");
				sb.append("	 `vc2ReplyMsg`=? ");
				sb.append(" where vc2MsgId = ? and numAccountGuid = ? ");
				int rowcount = MyDbUtils.update(db, sb.toString(),
						new Object[] { msg.getNumAccountGuid(), msg.getVc2ToUserName(), msg.getVc2FromUserName(),
								msg.getNumWxCreateTime(), msg.getVc2MsgType(), msg.getDatReceive(), msg.getVc2OriMsg(),
								msg.getDatReply(), msg.getVc2ReplyMsg(), msg.getVc2MsgId(), msg.getNumAccountGuid() });
				if (rowcount <= 0) {
					log.error("update wx_received_msg error: msgid is " + msg.getVc2MsgId());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		} finally {
			MyDbUtils.closeQuietly(db);
		}
	}

	/**
	 * 获取用户Po
	 * 
	 * @param numAccountGuid
	 * @param openid
	 * @return
	 */
	public static PoWeixinUser getWxUserByOpenId(int numAccountGuid, String openid) {
		PoWeixinUser user = null;
		Connection db = null;
		try {
			db = DbHelper.getWxDb();
			user = MyDbUtils.getRow(db, "select * from wx_user where vc2OpenId = ? and numAccountGuid=?",
					new Object[] { openid, numAccountGuid }, PoWeixinUser.class);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		} finally {
			MyDbUtils.closeQuietly(db);
		}
		return user;
	}
	
//	public static PoWxUserStatus saveWxUserStatus(PoWxUserStatus user) throws ApiException {
//		if (user == null || MyStringUtils.isEmpty(user.getVc2OpenId()) || user.getNumAccountGuid() == null)
//			throw new ApiException("user is null or user openid is null or user accountguid is null");
//		PoWxUserStatus user1 = DataProvider.getWxUserStatusByOpenId(user.getNumAccountGuid(), user.getVc2OpenId());
//		/*
//		 *    numUserGuid          int(11) not null auto_increment comment '微信用户主键',
//   numAccountGuid       int(11) not null comment '微信服务号主键',
//   vc2OpenId            national varchar(64) not null comment '微信opened',
//   vc2SubscribeFlag     national varchar(1) not null comment '关注标识：0，未关注；1，已关注',
//   vc2FirstQRSceneId    national varchar(500) comment '首次关注来源qrscene，微信推送的格式为qrscene_***，该字段已经去掉了qrscene_，另外，该字段为首次关注来源',
//   datFirstSubscribeTime datetime comment '关注时间',
//   datLastSubscribeTime datetime,
//   datLastUnSubscribeTime datetime,
//   datCreation          datetime not null comment '创建时间',
//   datLastUpdate        datetime not null comment '最后修改时间',
//   vc2EnabledFlag       national varchar(1) not null comment '有效标识',
//
//		 * */
//		String insert = "insert into wx_user_status(numAccountGuid, vc2OpenId, vc2SubscribeFlag, vc2FirstQRSceneId,"
//				+ "datFirstSubscribeTime, datLastSubScribeTime, datLastUnSubscribeTime, datCreation,"
//				+ "datLastUpdate, vc2EnabledFlag) values ("
//				+ "?,?,?,?,"
//				+ "?,?,?,?"
//				+ "?,'?')";
//		String update = "update wx_user_status set vc2SubscribeFlag=?, "
//				+ "datLastSubScribeTime=?, "
//				+ "datLastUpdate=?, vc2EnabledFlag) values ("
//				+ "?,?,?,"
//				+ "?,?,?,?"
//				+ "?,'?')";
//		if (user1 == null) {
//			user1 = user;
//		}
//		return user;
//	}
//	
//	/**
//	 * 创建用户（注意，该函数未判断是否用户存在），最主要的是openid字段
//	 * 
//	 * @param user1
//	 * @return
//	 */
//	public static int newWxUserStatus(PoWxUserStatus user1) {
//		if (user1 == null)
//			return -1;
//		Connection db = null;
//		String sql = "insert into wx_user_status(datCreation, datLastUpdate, numAccountGuid, vc2EnabledFlag,"
//				+ "		vc2OpenId, vc2SubscribeFlag) values (" + "		?,?,?,?,?,?)";
//		try {
//			db = DbHelper.getWxDb();
//			return MyDbUtils.insertReturnKey(db, sql,
//					new Object[] { user1.getDatCreation(), user1.getDatLastUpdate(), user1.getNumAccountGuid(),
//							user1.getVc2EnabledFlag(), user1.getVc2OpenId(), user1.getVc2SubscribeFlag() });
//		} catch (Exception e) {
//			e.printStackTrace();
//			log.error("", e);
//		} finally {
//			MyDbUtils.closeQuietly(db);
//		}
//		return -1;
//	}


	/**
	 * 创建用户（注意，该函数未判断是否用户存在），最主要的是openid字段
	 * 
	 * @param user1
	 * @return
	 */
	public static int newWxUser(PoWeixinUser user1) {
		if (user1 == null)
			return -1;
		Connection db = null;
		String sql = "insert into wx_user(datCreation, datLastUpdate, numAccountGuid, vc2EnabledFlag,"
				+ "		vc2OpenId, vc2SubscribeFlag) values (" + "		?,?,?,?,?,?)";
		try {
			db = DbHelper.getWxDb();
			return MyDbUtils.insertReturnKey(db, sql,
					new Object[] { user1.getDatCreation(), user1.getDatLastUpdate(), user1.getNumAccountGuid(),
							user1.getVc2EnabledFlag(), user1.getVc2OpenId(), user1.getVc2SubscribeFlag() });
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		} finally {
			MyDbUtils.closeQuietly(db);
		}
		return -1;
	}

	/**
	 * 修改用户，根据拉取用户信息接口
	 * 
	 * @param user
	 */
	public static void updateWxUser(PoWeixinUser user) {
		if (user == null)
			return;
		Connection db = null;
		String sql = "update wx_user" + "	set vc2SubscribeFlag = ?," + "	vc2NickName = ?," + " vc2SexCode = ?,"
				+ " vc2Language = ?," + " vc2CityName = ?," + " vc2ProvinceName = ?," + " vc2CountryName = ?,"
				+ " vc2HeadImgUrl = ?," + " datLastSubscribeTime = ?," + " vc2UnionId = ?," + " vc2Remark = ?,"
				+ " vc2GroupId = ?," + " datLastSyncFromWx = ?," + " datFirstSubscribeTime = ?,"
				+ " vc2FirstQRSceneId = ?" + "	where numAccountGuid = ? and vc2OpenId = ? ";
		try {
			db = DbHelper.getWxDb();
			MyDbUtils.update(db, sql,
					new Object[] { user.getVc2SubscribeFlag(), user.getVc2NickName(), user.getVc2SexCode(),
							user.getVc2Language(), user.getVc2CityName(), user.getVc2ProvinceName(),
							user.getVc2CountryName(), user.getVc2HeadImgUrl(), user.getDatLastSubscribeTime(),
							user.getVc2UnionId(), user.getVc2Remark(), user.getVc2GroupId(),
							user.getDatLastSyncFromWx(), user.getDatFirstSubscribeTime(), user.getVc2FirstQRSceneId(),
							user.getNumAccountGuid(), user.getVc2OpenId() });
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		} finally {
			MyDbUtils.closeQuietly(db);
		}
	}

	/**
	 * 记录用户的关注取关操作日志
	 * 
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
			MyDbUtils.update(db, sql,
					new Object[] { numUserGuid, type, sceneId, new Timestamp(System.currentTimeMillis()) });
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		} finally {
			MyDbUtils.closeQuietly(db);
		}
	}

	/**
	 * 取消关注
	 * 
	 * @param user
	 */
	public static void unsub(PoWeixinUser user) {
		if (user == null)
			return;
		Connection db = null;

		String sql = "update wx_user" + "	set vc2SubscribeFlag = ?," + " datLastupdate = ?,"
				+ " datLastUnSubscribeTime = ? " + " where numUserGuid = ? ";
		try {
			db = DbHelper.getWxDb();
			MyDbUtils.update(db, sql, new Object[] { "0", new Timestamp(System.currentTimeMillis()),
					new Timestamp(System.currentTimeMillis()), user.getNumUserGuid() });
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		} finally {
			MyDbUtils.closeQuietly(db);
		}
	}

	/**
	 * 保存menus到数据库(注意，只支持两层！！！！)
	 * 
	 * @param menus
	 */
	public static void saveMenus(Menu[] menus, Integer numAccountGuid) {
		if (menus == null || menus.length == 0)
			return;
		Connection db = null;
		try {
			db = DbHelper.getWxDb();
			db.setAutoCommit(false);
			MyDbUtils.update(db, "delete from wx_menu where numAccountGuid = ?", new Object[] { numAccountGuid });
			String insertSql = "insert into wx_menu(`numAccountGuid`, `vc2MenuName`, `vc2MenuType`, "
					+ "`vc2MenuKey`, `vc2MenuUrl`, `numParentMenuGuid`, "
					+ "`datCreation`, `vc2CreatedBy`, `datLastUpdate`, "
					+ "`vc2LastUpdatedBy`, `vc2EnabledFlag`) values (" + "?,?,?," + "?,?,?," + "now(),'0',now(),"
					+ "'0','Y')";
			for (int i = 0; i < menus.length; i++) {
				Menu menu = menus[i];
				System.out.println(menu.getName());
				int menuGuid = MyDbUtils.insertReturnKey(db, insertSql, new Object[] { numAccountGuid, menu.getName(),
						menu.getType(), menu.getKey(), menu.getUrl(), 0 });
				if (menu.getSub_button() != null && menu.getSub_button().size() > 0) {
					for (int k = 0; k < menu.getSub_button().size(); k++) {
						Menu menusub = menu.getSub_button().get(k);
						MyDbUtils.insertReturnKey(db, insertSql, new Object[] { numAccountGuid, menusub.getName(),
								menusub.getType(), menusub.getKey(), menusub.getUrl(), menuGuid });
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
			MyDbUtils.closeQuietly(db);
		}

	}

	/**
	 * 获取菜单
	 * 
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
		sb.append("	and numParentMenuGuid = ? order by numOrder, numMenuGuid");
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
			MyDbUtils.closeQuietly(db);
		}

		return menus.toArray(new Menu[] {});
	}
}
