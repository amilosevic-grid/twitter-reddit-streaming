
import Utils.tryOrNone
import com.google.gson.Gson
import com.twitter.clientlib.api.TwitterApi
import com.twitter.clientlib.{TwitterCredentialsBearer, TwitterCredentialsOAuth2}

import java.util.Properties
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerConfig}
import org.apache.kafka.common.serialization.{LongSerializer, StringSerializer}
import com.twitter.clientlib.model.StreamingTweet

import collection.JavaConverters._
import java.io.{BufferedReader, InputStreamReader}



class TwitterKafkaProducer(
                            val api: TwitterApi,
                            val gson: Gson,
                            val callback: Callback) {

  def getProducer: KafkaProducer[Long, String] = {
    val properties = new Properties()
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfigurations.SERVERS)
    properties.put(ProducerConfig.ACKS_CONFIG, "1")
    properties.put(ProducerConfig.LINGER_MS_CONFIG, "500")
    properties.put(ProducerConfig.RETRIES_CONFIG, "0")
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[LongSerializer].getName)
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)

    new KafkaProducer(properties)
  }

  def run(): Unit = {
    val expansions = new java.util.HashSet[String]
    expansions.add("author_id")
    expansions.add("geo.place_id")
    val tweetFields = new java.util.HashSet[String]
    tweetFields.add("text")
    tweetFields.add("author_id")
    tweetFields.add("lang")
    tweetFields.add("id")
    tweetFields.add("entities")
    tweetFields.add("geo")
    val userFields = new java.util.HashSet[String]
    userFields.add("location")
    val placeFields = new java.util.HashSet[String]
    placeFields.add("country")

    val producer = getProducer
    try while(true){
      val streamResult = api.tweets.sampleStream(expansions, tweetFields, userFields, null, placeFields, null,null)
      val reader = new BufferedReader(new InputStreamReader(streamResult))
      var line: String = reader.readLine()
      while(line != null){
        val tweet = gson.fromJson(line, classOf[StreamingTweet])
        try {
          val tweetData = tweet.getData
          val key = tweet.getData.getId.toLong
          val text = tweetData.getText
          val lang = tweetData.getLang
            val hashtags: Seq[String] = tryOrNone(tweetData.getEntities.getHashtags) match {
              case Some(x) => x.asScala.map(h => h.getTag)
              case None => Seq.empty[String]
          }

          val msg = Tweet(
            text,
            lang,
            User(tweetData.getAuthorId, tweet.getIncludes.getUsers.get(0).getLocation),
            hashtags)
          val record = new ProducerRecord[Long, String](KafkaConfigurations.TOPIC, key, gson.toJson(msg))
          producer.send(record, callback)
          line = reader.readLine()
        }
        catch {
          case e: NullPointerException =>
            println("ran into a empty line")
            e.printStackTrace()
            line = reader.readLine()
        }
      }
      streamResult.close()
      println("making a break! :)")
      Thread.sleep(18000) // to spread out requests
    }
    catch {
      case e: InterruptedException =>
        e.printStackTrace()
    } finally {
      if (producer != null)
        producer.close()
    }
  }
}

object TwitterKafkaProducer {

  def main(args: Array[String]): Unit = {
    TwitterKafkaProducer.apply().run()
  }

  def apply(): TwitterKafkaProducer = {
    val twitterConfig = ConfigManager.load
    val twitterAuth = new TwitterCredentialsOAuth2(
      twitterConfig.consumerKey,
      twitterConfig.consumerSecret,
      twitterConfig.accessToken,
      twitterConfig.tokenSecret
    )

    val apiInstance = new TwitterApi()
//    apiInstance.setTwitterCredentials(twitterAuth)
    apiInstance.setTwitterCredentials(new TwitterCredentialsBearer(twitterConfig.bearerToken))

    val gson = new Gson()
    val callback = BasicCallback
    new TwitterKafkaProducer(apiInstance, gson, callback)
  }
}