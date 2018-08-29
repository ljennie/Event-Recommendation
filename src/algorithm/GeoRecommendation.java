package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

//Recommendation based on geo distance and similar categories.
public class GeoRecommendation {
	
	public List<Item> recommendItems(String userId, double lat, double lon) {//通过用户的位置来找相关的信息recommendation
		List<Item> recommendedItems = new ArrayList<>();//返回值
		DBConnection conn = DBConnectionFactory.getConnection();

		// Step 1 Get all favorite items
		Set<String> favoriteItemIds = conn.getFavoriteItemIds(userId);

		// Step 2 Get all categories of favorite items, sort by count
		Map<String, Integer> allCategories = new HashMap<>();
		for (String itemId : favoriteItemIds) {
			Set<String> categories = conn.getCategories(itemId);
			for (String category : categories) {
				if (allCategories.containsKey(category)) {
					allCategories.put(category, allCategories.get(category) + 1);
				} else {
					allCategories.put(category, 1);
				}
			}
//			for (String category : categories) {
//				allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
//			}
		}

		// 我们需要先推荐user用过最多的category
		List<Entry<String, Integer>> categoryList = new ArrayList<Entry<String, Integer>>(allCategories.entrySet());
		Collections.sort(categoryList, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return Integer.compare(o2.getValue(), o1.getValue());
			}
		});

		// Step 3, do search based on category, filter out favorited events, sort by distance
		Set<Item> visitedItems = new HashSet<>();//存之前访问过的值
		// visited set 要通过hashCode和equals来判断两个东西是不是一样,所以在list里添加了hashCode和equals

		for (Entry<String, Integer> category : categoryList) {
			List<Item> items = conn.searchItems(lat, lon, category.getKey());
			List<Item> filteredItems = new ArrayList<>();
			for (Item item : items) {
				if (!favoriteItemIds.contains(item.getItemId()) && !visitedItems.contains(item)) {
					//用户里没有favorite保存过或者用户没有访问过的内容
					filteredItems.add(item);
				}
			}

			Collections.sort(filteredItems, new Comparator<Item>() {// sort by distance
				@Override
				public int compare(Item item1, Item item2) {
					return Double.compare(item1.getDistance(), item2.getDistance());
				}
			});

			visitedItems.addAll(items);//visitedItems是所有搜索过的见过的items
			recommendedItems.addAll(filteredItems);//recommendedItems是filter完后排好序后的items
		}
		return recommendedItems;
	}
}
