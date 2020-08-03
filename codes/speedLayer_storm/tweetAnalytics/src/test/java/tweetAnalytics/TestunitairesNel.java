package tweetAnalytics;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class TestunitairesNel {

	public static void main(String[] args) {
		// String[] tab = {"toto", "tata", "titi", "tete"};
		 String[] tab = {};
		 for (int i=0; i < tab.length; i++) {
			 System.out.println("adjf");
			 System.out.println(tab[i]);
		 }
		 String unechaine = new String();
		 unechaine = "a";
		 //System.out.println(isNullOrEmpty(unechaine));
		 
		 // create a HashMap and add some values 
		 HashMap<String, Integer> map = new HashMap<>(); 
		 map.put("c", 30); 
		 map.put("a", 83); 
		 map.put("e", 85);
	     map.put("b", 55); 
	  
	     // print original map 
	     System.out.println("HashMap: " + map.toString());
	     
	     //map.putIfAbsent("c", 1);
	     map.merge("a", 1, Integer::sum);
	     System.out.println("HashMap2: " + map.toString());
	     Map<String, Integer> sortedMap = map.entrySet().stream()
	    		 .sorted(Entry.comparingByValue(Comparator.reverseOrder()))
	    		 .collect(Collectors
	    				 .toMap(Entry::getKey, Entry::getValue,(e1, e2) -> e1, 
	    						 LinkedHashMap::new));
	     
	     System.out.println("sortedMap: " + sortedMap.toString());
	     
	     System.out.println("cutted Map: " + selectNpremiers(sortedMap, 3).toString());
	     
	     LinkedHashMap<String, Integer> numbers = new LinkedHashMap<String, Integer>();
	     numbers.put("one", 1);
	     numbers.put("two", 2);
	     numbers.put("three", 3);
	     System.out.println("numbers: " + numbers.toString());
	}
	
	
	public static LinkedHashMap<String, Integer> selectNpremiers(Map<String, Integer> map, int taille) {
		LinkedHashMap<String,Integer> result= new LinkedHashMap<String,Integer>(); 
	    Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
	    int count = 0;
	    while (iterator.hasNext() && count < taille) {
	        Map.Entry<String, Integer> entry = iterator.next();
	        System.out.println(entry.getValue());
	        result.put(entry.getKey(), entry.getValue());
	        count++;
	    }
	    return result;
	}
	
	
	public static boolean isNullOrEmpty(String str) {
		/*
		 * Fonction qui dit si un String est vide
		 * Retourne un booléen
		 */
        if(str != null && !str.trim().isEmpty())
            return false;
        return true;
    }

}
