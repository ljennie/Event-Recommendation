package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

/**
 * Servlet implementation class ItemHistory
 */
@WebServlet("/history")
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ItemHistory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userId = request.getParameter("user_id");// JSON用的是user_id(snake case)
		JSONArray array = new JSONArray();
		
		DBConnection conn = DBConnectionFactory.getConnection();
		Set<Item> items = conn.getFavoriteItems(userId);// 得到MySQL的内容
		
		for (Item item : items) {
			JSONObject obj = item.toJSONObject();//遍历一遍转化为JSONObject的形式
			
			try {
				obj.append("favorite", true);//加了这个到时候在前端可以显示
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			array.put(obj);//得到JSONArray
		}
		
		RpcHelper.writeJsonArray(response, array);//得到JSONArray,把数据返回给客户端
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	// 把favorite item提上来放入favoriate history表里
	// doPost对应MySQLConnection里的setFavoriteItems
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			/** {
			 * 		user_id = "1111",
			 * 		favorite = [
			 * 			"abcd",
			 * 			"efgh".
			 * 		]
			 * }
			 */
			JSONObject input = RpcHelper.readJsonObject(request);
			String userId = input.getString("user_id");//user_id在第一层
			
			JSONArray array = input.getJSONArray("favorite");
			//存favorite的string
			List<String> itemIds = new ArrayList<>();
			for (int i = 0; i < array.length(); ++i) {
				itemIds.add(array.get(i).toString());
			}
			
			DBConnection conn = DBConnectionFactory.getConnection();
			conn.setFavoriteItems(userId, itemIds);
			conn.close();
			
			//给客户返回一个值
			// JSONObject obj = new JSONbject();
			// obj.put("result", "SUCCESS");
			// RpcHelper.writeJsonObject(response, obj);
			RpcHelper.writeJsonObject(response,
					new JSONObject().put("result", "SUCCESS"));//发送给前端看是否result发送success
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	// doDelete对应MySQLConnection里的unsetFavoriteItems
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			JSONObject input = RpcHelper.readJsonObject(request);
			String userId = input.getString("user_id");
			
			JSONArray array = input.getJSONArray("favorite");
			List<String> itemIds = new ArrayList<>();
			for (int i = 0; i < array.length(); ++i) {
				itemIds.add(array.get(i).toString());
			}
			
			DBConnection conn = DBConnectionFactory.getConnection();
			conn.unsetFavoriteItems(userId, itemIds);
			conn.close();
			
			RpcHelper.writeJsonObject(response,
					new JSONObject().put("result", "SUCCESS"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
