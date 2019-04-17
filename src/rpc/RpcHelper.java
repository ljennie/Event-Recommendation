package rpc;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;

public class RpcHelper {//RpcHelper为了帮助其他entry point来生成数据的
	
	// Writes a JSONObject to http response 
	public static void writeJsonObject(HttpServletResponse response, JSONObject obj) {
		// static? 为了使用，不需要先new一个RecHelper，再调用了，否则很麻烦。
		try {
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			PrintWriter out = response.getWriter();
			out.print(obj);
			out.close();
		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}
	
	// Write a JSONArray to http response
	public static void writeJsonArray(HttpServletResponse response, JSONArray array) {
		try {
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			PrintWriter out = response.getWriter();
			out.print(array);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Parses a JSONObject from http request.
	public static JSONObject readJsonObject(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = request.getReader();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			return new JSONObject(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; 
	}
	
	public static JSONArray getJSONArray(List<Item> items) {
		JSONArray result = new JSONArray();
		try {
			for (Item item : items) {
				result.put(item.toJSONObject());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	// Parses a JSONObject from http request.
		public static JSONObject readJSONObject(HttpServletRequest request) {
			StringBuilder sBuilder = new StringBuilder();
			try (BufferedReader reader = request.getReader()) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					sBuilder.append(line);
				}
				return new JSONObject(sBuilder.toString());

			} catch (Exception e) {
				e.printStackTrace();
			}
			return new JSONObject();
		}
	


}
