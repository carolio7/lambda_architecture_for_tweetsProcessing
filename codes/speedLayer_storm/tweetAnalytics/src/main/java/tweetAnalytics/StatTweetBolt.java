package tweetAnalytics;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.storm.shade.org.json.simple.JSONArray;
import org.apache.storm.shade.org.json.simple.JSONObject;
import org.apache.storm.shade.org.json.simple.parser.JSONParser;
import org.apache.storm.shade.org.json.simple.parser.ParseException;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.windowing.TupleWindow;

public class StatTweetBolt extends BaseWindowedBolt {
	private OutputCollector outputCollector;
	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		outputCollector = collector;
	}
	
	
	@Override
	public void execute(TupleWindow inputWindow) {
		HashMap<String, Integer> hashtagReceived = new HashMap<String, Integer>();
		
		// Collect stats for all tuples, city by city, station by station
		Integer tupleCount = 0;
		for(Tuple input : inputWindow.get()) {
			try {
				//System.out.println(input);
				JSONParser jsonParser = new JSONParser();
				JSONObject jsonInput = (JSONObject)jsonParser.parse(input.getStringByField("value"));
				
				JSONObject entities = (JSONObject)jsonInput.get("entities");
				JSONArray hashtagArray = (JSONArray)entities.get("hashtags");
				for (int i=0; i < hashtagArray.size(); i++) {
					String unHashtag = new String();
					JSONObject unHashtagJson = (JSONObject)hashtagArray.get(i);
					unHashtag = (String)unHashtagJson.get("text");
					if (!isNullOrEmpty(unHashtag)) {
						//if key do not exists, put 1 as value otherwise sum 1 to the value linked to key
						hashtagReceived.merge(unHashtag, 1, Integer::sum);
					}
				}
				
				outputCollector.ack(input);
				tupleCount += 1;
			} catch (ParseException e) {
				e.printStackTrace();
				outputCollector.fail(input);
			}
		}
		
		System.out.printf("====== StatTweetBolt: Received %d messages postés\n", tupleCount);
		//Trier les clé-valeur en décroissance et fonction de la valeur 
		Map<String, Integer> sortedMap = hashtagReceived.entrySet().stream()
	    		 .sorted(Entry.comparingByValue(Comparator.reverseOrder()))
	    		 .collect(Collectors
	    				 .toMap(Entry::getKey, Entry::getValue,(e1, e2) -> e1, 
	    						 LinkedHashMap::new));
		
		LinkedHashMap<String, Integer> topDixHashtags = selectNpremiers(sortedMap, 10);
		String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		
		// Emettre les top 10 des hashtags
		outputCollector.emit(new Values(now, topDixHashtags, tupleCount));
		
	}
	
	
	
	public boolean isNullOrEmpty(String str) {
		/*
		 * Fonction qui dit si un String est vide
		 * Retourne un booléen
		 */
        if(str != null && !str.trim().isEmpty())
            return false;
        return true;
    }
	
	
	public static LinkedHashMap<String, Integer> selectNpremiers(Map<String, Integer> map, int taille) {
		/*
		 * Fonction découpe une Map en taille définit tout en gardant leur ordre
		 * Retourne un type LinkedHashMap
		 */
		LinkedHashMap<String,Integer> result= new LinkedHashMap<String,Integer>(); 
	    Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
	    int count = 0;
	    while (iterator.hasNext() && count < taille) {
	        Map.Entry<String, Integer> entry = iterator.next();
	        result.put(entry.getKey(), entry.getValue());
	        count++;
	    }
	    return result;
	}
	
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("date", "topDixHashtags", "nbMessagesPosted"));
	}

}
