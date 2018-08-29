package db.mysql;

import java.util.Map;

// Step 3, create MySQL version of DBConnection implementation

public class MySQLDBUtil {
	
	private static final String HOSTNAME = "localhost";
	private static final String PORT_NUM = "8889";// "3306""8889"// change it to your mysql port number
	public static final String DB_NAME = "laiproject";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";
	public static final String URL = "jdbc:mysql://"//jdbc连MySQL
			+ HOSTNAME + ":" + PORT_NUM + "/" + DB_NAME
			+ "?user=" + USERNAME + "&password=" + PASSWORD
			+ "&autoReconnect=true&serverTimezone=UTC";
// jdbc: mysql://localhost:8889/laiproject?user=root&password=root&autoReconnect=true&serverTimezone=UTC
	
	/*private static final Map<String, String> map;
	static {
		map = new HashMap<>();
		map.put("a", "a");
	}
	static 为了初始化static变量的
	load class MySQLDBUtil 的时候，static就可以被执行*/	

}
