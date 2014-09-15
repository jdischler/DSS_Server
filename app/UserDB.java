package util;

import play.*;
import play.api.db.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.sql.*;

import com.fasterxml.jackson.core.*;

//------------------------------------------------------------------------------
public class UserDB
{
	private final static String USER_DB = "users";
	private final static String USER_TABLE = "users";
	private final static String DB_USER_ID_KEY = "user_ID";
	private final static String DB_USER_EMAIL_KEY = "user_email";
	
	//--------------------------------------------------------------------------
	// Public Access...
	//--------------------------------------------------------------------------
	public static void prepareDatabase() {
		
		Connection conn = play.db.DB.getConnection(USER_DB);
		
		String sqlCreate = "CREATE TABLE IF NOT EXISTS " + USER_TABLE
			+ " (id 				INT PRIMARY KEY AUTO_INCREMENT,"
			+ DB_USER_ID_KEY + "	VARCHAR(10),"
			+ "  user_name			VARCHAR(50),"
			+ DB_USER_EMAIL_KEY + "	VARCHAR(75),"
			+ "  registered_on		TIMESTAMP,"
			+ "  last_login			TIMESTAMP,"
			+ "  visit_count		INTEGER)";
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(sqlCreate);
		}
		catch (Exception ex) {
			Logger.info(ex.toString());
		}
		
		Integer userCount = getUserCount(conn);
		
		// Insert a fake user if we are in dev...
		if (!Play.isProd() && userCount <= 0) {
			Logger.info(" Adding a fake user - username, email: user, user@dot.com");
			addUser(conn, "user", "user@dot.com");
			
			userCount = getUserCount(conn);
			Logger.info(" Database user count - " + userCount.toString());
		}
		
		try {
			conn.close();
		}
		catch (Exception ex) {
			Logger.info(ex.toString());
		}
	}
	
	//--------------------------------------------------------------------------
	public static Integer getUserCount() {
		Connection conn = play.db.DB.getConnection(USER_DB);
		Integer res = getUserCount(conn);
		try {
			conn.close();
		}
		catch (Exception ex) {
			Logger.info(ex.toString());
		}
		return res;
	}

	// TODO: validate email and random string. Both must be unique?
	//--------------------------------------------------------------------------
	public static String addUser(String userName, String userEmail) {
		Connection conn = play.db.DB.getConnection(USER_DB);
		String res = addUser(conn, userName, userEmail);
		try {
			conn.close();
		}
		catch (Exception ex) {
			Logger.info(ex.toString());
		}
		return res;
	}
	
	//--------------------------------------------------------------------------
	public static boolean isEmailUnique(String value) {
		Connection conn = play.db.DB.getConnection(USER_DB);
		boolean res = isUnique(conn, DB_USER_EMAIL_KEY, value);
		try {
			conn.close();
		}
		catch (Exception ex) {
			Logger.info(ex.toString());
		}
		return res;
	}
	
	//--------------------------------------------------------------------------
	// Privates....	
	//--------------------------------------------------------------------------
	private static Integer getUserCount(Connection conn) {
		
		String sqlUsers = "SELECT COUNT(*) as rowcount FROM " + USER_TABLE;
		
		try {
			Statement stmt = conn.createStatement();
			ResultSet res = stmt.executeQuery(sqlUsers);
			res.next();
			Integer count = res.getInt("rowcount");
			Logger.info(" Users table has this many entries: " + count.toString());
			res.close();
			stmt.close();
			return count;
		}
		catch (Exception ex) {
			Logger.info(ex.toString());
		}
		
		return 0;
	}
	
	//--------------------------------------------------------------------------
	private static String addUser(Connection conn, String userName, String userEmail) {
		
		String uniqueUserID = RandomString.get(10);
		int tryCount = 0, MAX_TRIES = 20;
		
		while (!isUnique(conn, DB_USER_ID_KEY, uniqueUserID) && tryCount < MAX_TRIES) {
			Logger.info(" Trying to add user and random ID was not unique, trying again...");
			uniqueUserID = RandomString.get(10);
			tryCount++;
		}
		
		if (tryCount >= MAX_TRIES) {
			return null;
		}
		
		String sqlNewUser = "INSERT INTO " + USER_TABLE
			+ "(user_id, user_name, user_email, registered_on, last_login, visit_count) "
			+ "VALUES "
			+ "('" + uniqueUserID 
			+ "','" + userName 
			+ "','" + userEmail
			+ "',NOW()"
			+ ",NULL"
			+ ",NULL)";
			
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(sqlNewUser);
			stmt.close();
		}
		catch (Exception ex) {
			Logger.info(ex.toString());
			return null;
		}
		
		return uniqueUserID;
	}
	
	//--------------------------------------------------------------------------
	private static boolean isUnique(Connection conn, String key, String value) {
		
		String sqlCheckUnique = "SELECT COUNT(*) as rowcount FROM " + USER_TABLE
			+ " WHERE STRCMP(LOWER('" + key + "'),"
			+ "  LOWER('" + value + "'))"
			+ "  =0";
			
		try {
			Statement stmt = conn.createStatement();
			ResultSet res = stmt.executeQuery(sqlCheckUnique);
			res.next();
			Integer count = res.getInt("rowcount");
			res.close();
			stmt.close();
			return (count <= 0);
		}
		catch (Exception ex) {
			Logger.info(ex.toString());
		}
		
		return false;
	}
}

