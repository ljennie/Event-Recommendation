package offline;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoDatabase;

import db.mongodb.MongoDBUtil;
//把每个时间点放在不同的bucket，也就是不同的区间
public class FindPeak{
	private static List<LocalTime> buckets = initBuckets();
	public static void main(String [] args) {
		// Init
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);

		// Construct mapper function
		// function() {
		// 	 if (this.url.startswith("/Titan")) {
		// 		emit(this.time.substring(0, 5), 1);
		//    }
		// }

		StringBuilder sb = new StringBuilder();
		sb.append("function() {");
		sb.append(" if (this.url.startsWith(\"/Titan\")) {");// 判断是不是以Titan开头，是的话就在我的分析范围之内
		sb.append(" emit(this.time.substring(0, 5), 1); }");//key is当前的时间点，value is 请求 //(0, 5)：00：37， 00:49
		sb.append("}");
		String map = sb.toString();

		// Construct a reducer function
		String reduce = "function(key, values) {return Array.sum(values)} ";//reduce

		// MapReduce
		MapReduceIterable<Document> results = db.getCollection("logs").mapReduce(map, reduce);

		// Save total count to each bucket
		//根据map reduce的结果，建立key value pair； key is 时间段， value is时间段所对应的请求数量
		Map<String, Double> timeMap = new HashMap<>(); //填充hash map
		results.forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				String time = findBucket(document.getString("_id"));
				Double count = document.getDouble("value");
				if (timeMap.containsKey(time)) {
					timeMap.put(time, timeMap.get(time) + count);
				} else {
					timeMap.put(time, count);
				}
			}
		});

		// Need a sorting here
		List<Map.Entry<String, Double>> timeList = new ArrayList<Map.Entry<String, Double>>(timeMap.entrySet());
		Collections.sort(timeList, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
				return Double.compare(o2.getValue(), o1.getValue());
			}
		});

		printList(timeList);
		mongoClient.close();	
	}


	

	private static void printList(List<Map.Entry<String, Double>> timeList) {//为了在console打印一个list
		for (Map.Entry<String, Double> entry : timeList) {
			System.out.println("time: " + entry.getKey() + " count: " + entry.getValue());
		}
	}

	private static List<LocalTime> initBuckets() {
		List<LocalTime> buckets = new ArrayList<>();
		LocalTime time = LocalTime.parse("00:00");
		for (int i = 0; i < 96; ++i) {
			buckets.add(time);
			time = time.plusMinutes(15);//每十五分钟做一个区间
		}
		return buckets;
	}

	//给定一个时间点具体属于哪个区间
    // Use LocalTime.isAfter/isBefore to compare to objects
	private static String findBucket(String currentTime) {
		LocalTime curr = LocalTime.parse(currentTime);
		int left = 0, right = buckets.size() - 1;
		while (left < right - 1) {
			int mid = (left + right) / 2;
			if (buckets.get(mid).isAfter(curr)) {
				right = mid - 1;
			} else {
				left = mid;
			}
		}
		if (buckets.get(right).isAfter(curr)) {
			return buckets.get(left).toString();
		}
		return buckets.get(right).toString();
	}
}
