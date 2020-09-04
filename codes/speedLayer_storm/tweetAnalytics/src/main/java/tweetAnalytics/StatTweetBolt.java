package tweetAnalytics;

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
		Long timestamp_debut = null, timestamp_fin = null; 
		for(Tuple input : inputWindow.get()) {
			try {
				//System.out.println(input);
				JSONParser jsonParser = new JSONParser();
				JSONObject jsonInput = (JSONObject)jsonParser.parse(input.getStringByField("value"));
				
				String timestamps_ms_String = (String)jsonInput.get("timestamp_ms");
				if (!isNullOrEmpty(timestamps_ms_String)) {
					Long timestamp_ms = Long.valueOf(timestamps_ms_String);
					timestamp_debut = minimal_timestamp(timestamp_ms, timestamp_debut);
					timestamp_fin = maximal_timestamp(timestamp_ms, timestamp_fin);
				}
				
				// On ne traite que les messages en anglais
				String langue = (String)jsonInput.get("lang");
				if (isNullOrEmpty(langue) || langue.equals("en")) {
					JSONObject entities = (JSONObject)jsonInput.get("entities");
					if (entities != null) {
						JSONArray hashtagArray = (JSONArray)entities.get("hashtags");
						if (!hashtagArray.isEmpty()) {
							System.out.println("La taille de hashtagArray est de : " + hashtagArray.size());
							for (int i=0; i < hashtagArray.size(); i++) {
								String unHashtag = new String();
								JSONObject unHashtagJson = (JSONObject)hashtagArray.get(i);
								unHashtag = (String)unHashtagJson.get("text");
								if (!isNullOrEmpty(unHashtag)) {
									System.out.println("-------> " + unHashtag);
									//if key do not exists, put 1 as value otherwise sum 1 to the value linked to key
									hashtagReceived.merge(unHashtag, 1, Integer::sum);
								}
							}
						}
					}
					
				}
				
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("Une exception trouvée, fais gaffe !! ");
			}
			
			outputCollector.ack(input);
			tupleCount += 1;
		}
		
		System.out.printf("====== StatTweetBolt: Received %d messages postés\n", tupleCount);
		//Trier les clé-valeur en décroissance et fonction de la valeur 
		Map<String, Integer> sortedMap = hashtagReceived.entrySet().stream()
	    		 .sorted(Entry.comparingByValue(Comparator.reverseOrder()))
	    		 .collect(Collectors
	    				 .toMap(Entry::getKey, Entry::getValue,(e1, e2) -> e1, 
	    						 LinkedHashMap::new));
		
		LinkedHashMap<String, Integer> topDixHashtags = selectNpremiers(sortedMap, 10);
		
		Date date_debut = new Date(timestamp_debut);
		Date date_fin = new Date(timestamp_fin);
		
		// Emettre les top 10 des hashtags
		outputCollector.emit(new Values(date_debut, date_fin, topDixHashtags, tupleCount));
		
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
	
	public Long minimal_timestamp(Long current_timestamp, Long current_minimum) {
		if (current_minimum == null) {
			current_minimum = current_timestamp;
		} else if (current_minimum > current_timestamp) {
			current_minimum = current_timestamp;
		}
		return current_minimum;
	}
	
	
	public Long maximal_timestamp(Long current_timestamp, Long current_maximum) {
		if (current_maximum == null) {
			current_maximum = current_timestamp;
		} else if (current_maximum < current_timestamp) {
			current_maximum = current_timestamp;
		}
		return current_maximum;
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
		declarer.declare(new Fields("date_debut", "date_fin", "topDixHashtags", "nbMessagesPosted"));
	}

}
