package db.mysql;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.cj.jdbc.Driver;
import com.mysql.cj.jdbc.NonRegisteringDriver;

import java.sql.Connection;

/*Step 4.3, create a new class called MySQLTableCreation.java to automatically reset our tables in our database. 
 * So in the future, you can run this function every time when you think the data stored in you DB is messed up.
 */

public class MySQLTableCreation {
	// MySQLTableCreation:帮助我们把数据库放在初始状态
	// 如果数据库里有许多杂乱无章的数据，它可以帮忙先清理，恢复到里面没有数据的状态，然后再把数据放进去
	// 好处：把数据放成初始状态，方便大家debug

	// Step 3.3.1, first let’s try to connect to MySQL through JDBC connection.
	// Be careful, always use java.sql.* when eclipse ask you to import DB related
	// packages.
	// Run this as Java application to reset db schema.
	public static void main(String[] args) {
		try {
			// This is java.sql.Connection. Not com.mysql.jdbc.Connection.
			Connection conn = null;

			// Step 1 Connect to MySQL.
			try {
				System.out.println("Connecting to " + MySQLDBUtil.URL);
				// com.mysql.jdbc.Driver
				// mysql的jdbc driver是如何注册自己的？
				Class.forName("com.mysql.jdbc.Driver").getConstructor().newInstance();
				// .getConstructor().newInstance(); 不用这个的话，就调用不了static initialization
				// 把driver注册好了，只要调用了driver static initialization
				/*
				 * public class Driver extends NonRegisteringDriver implements java.sql.Driver {
				 * static { try { java.sql.DriverManager.registerDriver(new Driver()); } catch
				 * (SQLException E) { throw new RuntimeException("Can't register driver!"); } }
				 */
				// forName是获得了一个叫com.mysql.jdbc.Driver的class
				// reflection,运行期间出现的值来创建class
				// Driver 是一套数据支持jdbc的api所使用的程序

				// 注册完了jdbc driver，如何获得connection？
				conn = DriverManager.getConnection(MySQLDBUtil.URL);
				// 我们所提供的url 是用来注册
				// jdbc:
				// mysql://localhost:8889/laiproject?user=root&password=root&autoReconnect=true&serverTimezone=UTC
				// 的信息
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if (conn == null) {
				return;
			}

			// step 2: Drop tables in case they exit
			Statement stmt = conn.createStatement();
			String sql = "DROP TABLE IF EXISTS categories";
			// drop: 连同table一起删掉，user item category history
			// "IF EXISTS" 是MySQL 支持的语句， “DROP TABLE categories” 是所有数据库支持的语句
			stmt.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS history";
			stmt.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS items";
			stmt.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS users";
			stmt.executeUpdate(sql);

			
			// Step 3: Create new tables
			sql = "CREATE TABLE items("// table name: items
					+ "item_id VARCHAR(255) NOT NULL,"// VARCHAR(255) 变长数据，NOT NULL我这个数据不能是空
					+ "name VARCHAR(255),"
					+ "rating FLOAT,"
					+ "address VARCHAR(255)," 
					+ "image_url VARCHAR(255),"
					+ "url VARCHAR(255)," 
					+ "distance FLOAT," 
					+ "PRIMARY KEY(item_id))";// item_id 是unique的
			stmt.executeUpdate(sql);

			sql = "CREATE TABLE categories" 
					+ "(item_id VARCHAR(255) NOT NULL,"// "item id"引用的是items表里的items
																				// id,所以要先出现item
					+ "category VARCHAR(255) NOT NULL," 
					+ "PRIMARY KEY (item_id, category),"// 多对多的关系
					+ "FOREIGN KEY (item_id) REFERENCES items(item_id))";
			stmt.executeUpdate(sql);

			sql = "CREATE TABLE users(" 
					+ "user_id VARCHAR(255) NOT NULL," 
					+ "password VARCHAR(255) NOT NULL,"
					+ "first_name VARCHAR(255)," 
					+ "last_name VARCHAR(255)," 
					+ "PRIMARY KEY (user_id))";
			stmt.executeUpdate(sql);

			sql = "CREATE TABLE history (" 
					+ "user_id VARCHAR(255) NOT NULL," 
					+ "item_id VARCHAR(255) NOT NULL,"
					+ "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
					// DEFAULT CURRENT_TIMESTAMP是mysql数据库所支持的一个扩展，因为这个值不能是null
					// 如果我没有选择last favor time，那这里就会自动用我所插入的时间
					+ "PRIMARY KEY (user_id, item_id)," + "FOREIGN KEY (item_id) REFERENCES items(item_id),"
					+ "FOREIGN KEY (user_id) REFERENCES users(user_id))";
			stmt.executeUpdate(sql);

			// Step 4: Insert data
			// create a fake user
			sql = "INSERT INTO users VALUES(" + "'1111', '3229c1097c00d497a0fd282d586be050', 'Jiani', 'Liu')";
			// debug
			System.out.println("Executing query: " + sql);
			stmt.executeUpdate(sql);

			System.out.println("Import is done successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
