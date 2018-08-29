package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

//implement DBConnection interface: 对数据库MySQL进行操作，插入，删除
//this is a singleton pattern
public class MySQLConnection implements DBConnection {
	private Connection conn;
	
	// 创建connection, java和MySQL的连接
	public MySQLConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();//java和jdbc连接
			//newInstance 用来调用 反射来生成 class.initalization
			conn = DriverManager.getConnection(MySQLDBUtil.URL);//jdbc和MySQL连接
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() {// close connection
		if (conn != null) {// 检测conn是否创建成功，如果创建成功那就不是null
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {//操作sql的history，向里面insert东西
		if (conn == null) {
			return;
		}
		
		try {
			String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?, ?)";
			// 虽然我们只插入了user_id和item_id，对于last_favor_time他会默认插入(详细见MySQLTableCreation)
			PreparedStatement stmt = conn.prepareStatement(sql);//要针对当前的connection来创建stmt
			for (String itemId : itemIds) {
				stmt.setString(1, userId);
				stmt.setString(2, itemId);
				// itemId 为什么设置为string？因为ticketmaster返回的itemId是一串hashValue(geoHash)，所以用string存比较方便
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {//操作sql的history，向里面delete东西
		if (conn == null) {
			return;
		}
		
		try {
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				stmt.setString(1, userId);
				stmt.setString(2, itemId);
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {//通过history表，history表可以通过user_id获得item_id
		if (conn == null) {
			return new HashSet<>();
		}
		
		Set<String> favoriteItemIds = new HashSet<>();
		
		try {
			String sql = "SELECT item_id from history where user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String itemId = rs.getString("item_id");
				favoriteItemIds.add(itemId);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return favoriteItemIds;

	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {//把用户添加过的setfavoriteItem内容给找出来
									// 一个userId可能对应多个itemId
		if (conn == null) {
			return new HashSet<>();
		}
		
		Set<Item> favoriteItems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);
		
		try {
			String sql = "SELECT * FROM items WHERE item_id = ?";
			// *表示对所有的column感兴趣
			// String sql = "SELECT item_id, rating FROM items WHERE item_id = ?";
			// String sql = "SELECT * FROM items WHERE item_id = ? AND rating > 4.0";
			// "?" 为了防止sql injection
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				stmt.setString(1, itemId);//itemId从TicketMaster里取出来的是一个string
				ResultSet rs = stmt.executeQuery();//select后我们关心返回的值了，executeQuery()返回ResultSet
				//ResultSet是可以用iterate来返回的数据结构
				
				ItemBuilder builder = new ItemBuilder();
				
				while (rs.next()) {//java iterator//rs逻辑上指向-1
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setCategories(getCategories(itemId));
					builder.setDistance(rs.getDouble("distance"));
					builder.setRating(rs.getDouble("rating"));
					
					favoriteItems.add(builder.build());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		if (conn == null) {
			return null;
		}
		Set<String> categories = new HashSet<>();
		try {
			String sql = "SELECT category from categories WHERE item_id = ? ";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				categories.add(rs.getString("category"));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return categories;

	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);//TicketMasterAPI里的search这个method，找到相应我们所需要的内容
		for (Item item : items) {
			saveItem(item);//保存
		}
		return items;

	}

	@Override
	// 上面searchItems调用了saveItem，也就是说把找到的数据放在了数据库里这个步骤
	public void saveItem(Item item) {//找到数据，在数据库里加入并保存数据
		if (conn == null) {//connection 建立失败
			return;
		} 
		try {
			// SQL Injection: 使用(?, ?, ?, ?, ?, ?, ?)可以防止sql injection
			// Example:
			// SELECT * FROM users WHERE username = '<username>' AND password = <'password'>;
			// sql = SELECT * FROM users WHERE username = '" + <username> + "' 
			// 				AND password = <'" + password + "'>;
			// username: abcd
			// password: 123456
			// SELECT * FROM users WHERE username = '<abcd>' AND password = <'123456'>;
			// username: abcd 
			// password: 123456 'OR '1' = '1
			// SELECT * FROM users WHERE username = '<abcd>' AND password = <'123456' OR '1' = '1'>;
			// 不用密码永远成功
			String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
			/* "IGNORE":INSERT是往数据库里放东西，如果数据库里已经有的话，例如primary key已经出现的话，再放进去一样的内容就会出错
			IGNORE就是帮助我们观察这个数据是不是已经在数据库里了，有的话，就不执行这条语句，就避免了错误 */
			//如果我们想覆盖原来的值的话，可以用UPDATE这条语句
			//String sql = "INSERT IGNORE INTO items VALUES (" + item.getItemId() + ", ")";
			
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, item.getItemId());//位置 值 //getItemId()在entity ltem里
			stmt.setString(2, item.getName());
			stmt.setDouble(3, item.getRating());
			stmt.setString(4, item.getAddress());
			stmt.setString(5, item.getImageUrl());
			stmt.setString(6, item.getUrl());
			stmt.setDouble(7, item.getDistance()); // 注入所有值
			stmt.execute();//return boolean, 检查上面注入语句是否成功
			//stmt.executeUpdate(); return int, update了多少条记录
			
			sql = "INSERT IGNORE INTO categories VALUES(?, ?)";//用stmt的话，这条语句只使用一次
			stmt = conn.prepareStatement(sql);
			for (String category : item.getCategories()) {
				stmt.setString(1, item.getItemId());
				stmt.setString(2, category);
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getFullname(String userId) {
		if (conn == null) {
			return null;
		}
		String name = "";
		try {
			String sql = "SELECT first_name, last_name from users WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				name = String.join(" ", rs.getString("first_name"), rs.getString("last_name"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			return false;
		}
		try {
			String sql = "SELECT user_id from users WHERE user_id = ? and password = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
