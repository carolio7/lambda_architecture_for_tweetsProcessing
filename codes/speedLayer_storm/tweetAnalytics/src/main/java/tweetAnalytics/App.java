package tweetAnalytics;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.mongodb.bolt.MongoUpdateBolt;
import org.apache.storm.mongodb.common.SimpleQueryFilterCreator;
import org.apache.storm.mongodb.common.mapper.SimpleMongoUpdateMapper;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseWindowedBolt;

/**
 * Hello world!
 *
 */
public class App 
{
	private static final String url = "mongodb://localhost:27017/twitter";
    private static final String collectionName = "speedView";
    
	public static void main( String[] args ) throws Exception, AlreadyAliveException, 
    	InvalidTopologyException, AuthorizationException, InterruptedException
    {
        
    	TopologyBuilder builder = new TopologyBuilder();
    	
    	KafkaSpoutConfig.Builder<String, String> spoutConfigBuilderTwit = KafkaSpoutConfig
    			.builder("localhost:9092", "tweetsTopic");
    	KafkaSpoutConfig<String, String> spoutConfigTwit = spoutConfigBuilderTwit.build();
    	builder.setSpout("tweetsKafkaSpout", new KafkaSpout<String, String>(spoutConfigTwit));
        
    	// Traitement des Tweets
        builder.setBolt("counterTweets", new StatTweetBolt().withTumblingWindow(BaseWindowedBolt.Duration.minutes(5)))
    		.shuffleGrouping("tweetsKafkaSpout");
        	//.fieldsGrouping("city-stats", new Fields("city"));
        /*
        builder.setBolt("printer-results",  new PrintResultsBolt())
        	.shuffleGrouping("counterTweets");
        */
        
        builder.setBolt (
				"mongoSender", 
				new MongoUpdateBolt(
										url, 
										collectionName,
										new SimpleQueryFilterCreator().withField("date_debut"),
										new SimpleMongoUpdateMapper().withFields("date_debut", "date_fin", "topDixHashtags", "nbMessagesPosted")
									)
									.withUpsert(true)
			)
			.shuffleGrouping("counterTweets");
        
        
        
    	StormTopology topology = builder.createTopology();
        Config config = new Config();
        //maximum time given to the topology to fully process a message emitted by a spout
        config.setMessageTimeoutSecs(60*70*3);
        config.put("topology.producer.batch.size", 1);
        config.put("topology.transfer.batch.size", 1);
        String topologyName = "tweetsRealTimeAnalysis";
        if (args.length > 0 && args[0].equals("remote")) {
        	StormSubmitter.submitTopology(topologyName, config, topology);
        } else {
        	LocalCluster cluster = new LocalCluster();
        	cluster.submitTopology(topologyName, config, topology);
        }
    }
}
