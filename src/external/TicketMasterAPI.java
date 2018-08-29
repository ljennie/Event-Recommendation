package external;//external package是从外部（别人的api）获取数据的功能

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class TicketMasterAPI {
	public static void main(String[] args) {
		TicketMasterAPI tmApi = new TicketMasterAPI();
		// Mountain View, CA
		// tmApi.queryAPI(37.38, -122.08);
		// London, UK
		// tmApi.queryAPI(51.503364, -0.12);
		// Houston, TX
		tmApi.queryAPI(29.682684, -95.295410);
	}
	
//	 in TicketMasterAPI class, which will help us send HTTP request to TicketMaster API and get response, 
//	 add some constants. Replace “YOUR_API_KEY” with your own key.
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = ""; // no restriction, 默认keyword，我们不提供keyword
	private static final String API_KEY = "49mMxCZzss9E2ZXAZeL4eyPm6kjlJZ44";
	
	/**
	 * Helper methods
	 */

	//  {
	//    "name": "laioffer",
              //    "id": "12345",
              //    "url": "www.laioffer.com",
	//    ...
	//    "_embedded": {
	//	    "venues": [
	//	        {
	//		        "address": {
	//		           "line1": "101 First St,",
	//		           "line2": "Suite 101",
	//		           "line3": "...",
	//		        },
	//		        "city": {
	//		        	"name": "San Francisco"
	//		        }
	//		        ...
	//	        },
	//	        ...
	//	    ]
	//    }
	//    ...
	//  }
	private String getAddress(JSONObject event) throws JSONException {
		if (!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			
			if (!embedded.isNull("venues")) {//venues is array
				JSONArray venues = embedded.getJSONArray("venues");
				
				/* 如果我们只想获取一个的话
				 * if (venues.length() > 0) {
				 * 		return JSONObject venue = venues.getJSONObject(0);
				 * }
				 */
				
				for (int i = 0; i < venues.length(); ++i) {
					JSONObject venue = venues.getJSONObject(i);
					
					StringBuilder sb = new StringBuilder();
					
					if (!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						
						if (!address.isNull("line1")) {
							sb.append(address.getString("line1"));
						}
						if (!address.isNull("line2")) {
							sb.append(" ");
							sb.append(address.getString("line2"));
						}
						if (!address.isNull("line3")) {
							sb.append(" ");
							sb.append(address.getString("line3"));
						}
						// 三条line直接拼起来了会不会不美观？中间加个空格
					}
					
					if (!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						
						if (!city.isNull("name")) {
							sb.append(" ");
							sb.append(city.getString("name"));
						}
					}
					
					if (!sb.toString().equals("")) {
						return sb.toString();//把StringBuilder转化为string
					}
				}
			}
		}
		return "";
	}


	// {"images": [{"url": "www.example.com/my_image.jpg"}, ...]}
	private String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull("images")) {
			JSONArray images = event.getJSONArray("images");
			
			/* 如果我们只想获取一个的话
			 * if (images.length() > 0) {
			 * 		return JSONObject venue = venues.getJSONObject(0);
			 * }
			 */
			// 如果JSONObject在index为0的时候可能是个空的，所以我们为了要得到数据，我们需要用for loop遍历一遍找到第一个不为空的值
			for (int i = 0; i < images.length(); ++i) {
				JSONObject image = images.getJSONObject(i);
				
				if (!image.isNull("url")) {
					return image.getString("url");
				}
			}
		}
		return "";
	}

	// {"classifications" : [{"segment": {"name": "music"}}, ...]}
	private Set<String> getCategories(JSONObject event) throws JSONException {
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			
			for (int i = 0; i < classifications.length(); i++) {
				JSONObject classification = classifications.getJSONObject(i);
				
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					
					if (!segment.isNull("name")) {
						String name = segment.getString("name");
						categories.add(name);
						//为什么我们这里要放一个category？user可以通过搜索关键字找到
					}
				}
			}
		}
		return categories;
	}

	// Convert JSONArray to a list of item objects.
	// 为什么我们把这些get event放在getItemList而不是item里呢？
	// 因为item是用来存放数据的，是用来保存我们获取来的数据，而不是用来获取数据的
	private List<Item> getItemList(JSONArray events) throws JSONException {
		//getItemList所有数据中得到这八个想要的数据
		//因为传入的是JSONArray的event（也就是在search中我们所得到的event），我们需要把这些event改成一个list形式的
		List<Item> itemList = new ArrayList<>();

		//遍历这些events
		for (int i = 0; i < events.length(); ++i) {
			JSONObject event = events.getJSONObject(i);
			
			ItemBuilder builder = new ItemBuilder();
			// 前五个的数据在event的第一层，所以可以直接得到
			if (!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			
			if (!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			
			if (!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			
			//后三个比较藏的比较深，所以我们要用三个helper function来得到
			builder.setCategories(getCategories(event));
			builder.setAddress(getAddress(event));
			builder.setImageUrl(getImageUrl(event));
			
			itemList.add(builder.build());
		}

		return itemList;
	}

	// add a new search function in TicketMasterAPI, which will actually
	// send HTTP request and get response.
	/*
	 * Question: What kind of parameter you need to provide to search method?
	 * Latitude, longtitude and keyword. Question: What kind of return value you
	 * should set to search method? JSONArray because that’s the data we want from
	 * HTTP response.
	 */
	public List<Item> search(double lat, double lon, String keyword) {
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		try {// why using URLEncoder?因为数据最后发给ticketmaster的时候是用http 的形式来的，所以要encode成http的url
			keyword = java.net.URLEncoder.encode(keyword, "UTF-8");// keyword变成codedpoint的形式,可能会发生有特殊字符
			//UTF-8 每个character是个至少8位的字节
		} catch (Exception e) {
			e.printStackTrace();
		}
		String geoHash = GeoHash.encodeGeohash(lat, lon, 8);

		// Make your url query part like:
		// "apikey=12345&geoPoint=abcd&keyword=music&radius=50"
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY, geoHash, keyword, 50);
		//"apikey=" + API_KEY + "&geoPoint=" + geoHash + 
		// placeholder

		try {// connection 的作用：链接ticketMaster service 和程序
			HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection();
			// return URLConnection类型 所以要cast，这时候请求没有发出去
			// why not "HttpURLConnection connection = new HttpURLConnection"?
			// 因为HttpURLConnection的constructor不是public是protected的，所以要创建一个url的object
			int responseCode = connection.getResponseCode();// 打开这个程序发出这个请求并且获取这个请求的状态结果
	 		// responseCode就是200 400 404这类数字
			System.out.println("\nSending 'Get' request to URL: " + URL + "?" + query);
			System.out.println("Response code: " + responseCode);

//			if (responseCode != 200) {
//				// ...
//			}

			// if connection is ok, 那么我们就要从返回的请求里读取结果
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			// 先从connection里得到一个input string， 然后用string的reader读出来，然后再给个BufferedReader，一次只读一行
			// BufferedReader读取数据没有存在内存
			String inputLine;
			StringBuilder response = new StringBuilder();// 把每行读出来的string给拼接出来
			while ((inputLine = in.readLine()) != null) {// 一次读一行 //这时候数据被存在了内存
				response.append(inputLine);
			}
			in.close();
			/**try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
					String inputLine;
					StringBuilder response = new StringBuilder();
					while ((inputLine = in.readLine()) != null) {
							response.append(inputLine);
					}
				} */
			// convert response to JSON object
			JSONObject obj = new JSONObject(response.toString());
			// all Json object stored in embedded
			// check if embedded exist
			if (obj.isNull("_embedded")) {
				return new ArrayList<>();
//				return new JSONArray();
			}
			
			JSONObject embedded = obj.getJSONObject("_embedded");
			JSONArray events = embedded.getJSONArray("events");
			return getItemList(events);
			//return events;//最终我们想找到events的结果
			//这里return是在try里，所以说明如果有return结果，那么结果必须是成功的，说明找到了
			//return给了method的collar，之前调用的severlet
			
		} catch (Exception e) {
			e.printStackTrace();//这里catch住了就print出来，虽然没做什么处理，但是对于debug有用
			//这里的try catch是为了接住getItemList, getCategories, getImageUrl, 和 getAddress throw的exception
		}
		
		return new ArrayList<>();
		
	}

	// Step 4, before implement search function, we need some helper functions,
	// let’s add them first.
	void queryAPI(double lat, double lon) {// 用来debug，来看search获取的结果是不是正确
		List<Item> itemList = search(lat, lon, null);
		try {
			for (Item item : itemList) {
				JSONObject jsonObject = item.toJSONObject();// 获取到JSONObject
				System.out.println(jsonObject);// 然后内容输出出来
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
