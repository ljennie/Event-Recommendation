package db;


import db.mongodb.MongoDBConnection;
import db.mysql.MySQLConnection;

//create another class called DBConnectionFactory, we’ll use it to create different db instances.

public class DBConnectionFactory {

	private static final String DEFAULT_DB = "mongodb";
	
	public static DBConnection getConnection(String db) {//getConnection获得具体的实现
		// 根据db不同来创建connection
		switch(db) {
			case "mysql":
				return new MySQLConnection();
			case "mongodb":
				return new MongoDBConnection();
			default:
				throw new IllegalArgumentException("Invalid db: " + db);
		}
		
	}
	
	public static DBConnection getConnection() {
		return getConnection(DEFAULT_DB);
	}
			
}
