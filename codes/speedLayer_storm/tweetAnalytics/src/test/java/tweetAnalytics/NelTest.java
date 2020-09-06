package tweetAnalytics;

import org.apache.storm.shade.org.json.simple.JSONArray;
import org.apache.storm.shade.org.json.simple.JSONObject;

public class NelTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JSONObject jo = new JSONObject();
        jo.put("firstName", "John");
        jo.put("lastName", "Doe");
        
        JSONArray ja = new JSONArray();
        ja.add(jo);
        
        JSONObject ju = new JSONObject();
        ju.put("firstName", "gida");
        ju.put("lastName", "ful");;
        ja.add(ju);

        System.out.println(ja);

	}

}
