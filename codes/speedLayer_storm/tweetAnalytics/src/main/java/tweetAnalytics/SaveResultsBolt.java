package tweetAnalytics;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

public class SaveResultsBolt extends BaseRichBolt {
	private OutputCollector outputCollector;
	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		outputCollector = collector;
	}
	
	@Override
	public void execute(Tuple input) {
		
		LinkedHashMap<String, Integer> topDixHashtag = 
				(LinkedHashMap<String, Integer>)input.getValueByField("topDixHashtags");
		System.out.print("===>Resultat : " + topDixHashtag.toString());
		// TODO Auto-generated method stub

	}
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub

	}

}
