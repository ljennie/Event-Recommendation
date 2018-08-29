package offline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

import db.mongodb.MongoDBUtil;//自己创建的class

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

//把log文件import到mongo db 
public class Purify {
	public static void main(String[] args) {
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
                             // Switch to your own path
		String fileName = "/Users/Jennie/Downloads/tomcat_log.txt";

		try {
			db.getCollection("logs").drop();//如果之前有log文件，那就drop掉，一切重新开始
			
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {//一行行读里面的文件
				// Sample input: 
				// 73.223.210.212 - - [19/Aug/2017:22:00:24 +0000] "GET /Titan/history?user_id=1111 HTTP/1.1" 200 11410
                List<String> values = Arrays.asList(line.split(" "));
				
				String ip = values.size() > 0 ? values.get(0) : null;
				String timestamp = values.size() > 3 ? values.get(3) : null;
				String method = values.size() > 5 ? values.get(5) : null;
				String url = values.size() > 6 ? values.get(6) : null;
				String status = values.size() > 8 ? values.get(8) : null;

				// 19/Aug/2017:22:00:24 这一段时间比较重要，且后面那段时间比较重要，日期并不关注
				// 因为我们需要知道哪个时间段网站的访问量大
				Pattern pattern = Pattern.compile("\\[(.+?):(.+)");
				Matcher matcher = pattern.matcher(timestamp);
			    matcher.find();
				
				db.getCollection("logs")
						.insertOne(new Document().append("ip", ip).append("date", matcher.group(1))
								.append("time", matcher.group(2)).append("method", method.substring(1))
								.append("url", url).append("status", status));
			}
			System.out.println("Import Done!");
			bufferedReader.close();
			mongoClient.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
