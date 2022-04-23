import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, from_json}
import org.apache.spark.sql.streaming.{StreamingQueryListener, Trigger}
import org.apache.spark.sql.types.{StringType, StructField, StructType, ArrayType}

object TwitterSparkConsumer {
  private val spark: SparkSession = SparkSession.builder().master("local").getOrCreate()

  def main(args: Array[String]): Unit = {
    TwitterSparkConsumer.run()
  }

  def run(): Unit = {
    import org.apache.log4j.Logger
    import org.apache.log4j.Level

    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)
    import spark.implicits._

    spark.conf.set("spark.sql.streaming.metricsEnabled", "true")
    val tweetSchema = StructType(
      List(
        StructField("text", StringType, nullable = true),
        StructField("lang", StringType, nullable = true),
        StructField("user", StringType, nullable = true),
        StructField("hashtags", ArrayType(StringType, containsNull = true), nullable = true),
        StructField("country", StringType, nullable = true),
      )
    )
    val userSchema = StructType(
      List(
        StructField("name", StringType, nullable = true),
        StructField("location", StringType, nullable = true)
      )
    )
    val geoSchema = StructType(
      List(
        StructField("name", StringType, nullable = true),
        StructField("location", StringType, nullable = true)
      )
    )
    val df = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "bigdata-tweets")
      .option("startingOffsets", "latest")
      .load()
    val query1 = df
      .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)").as[(String, String)]
      .withColumn("tweet", from_json(col("value"), tweetSchema))
      .select(col("tweet.*"))
      .withColumn("user", from_json(col("user"), userSchema))
      .select("text", "lang", "hashtags", "country", "user.*")
      .writeStream
      .format("console")
      .trigger(Trigger.ProcessingTime("3 minutes"))
      .start()
    query1.awaitTermination()
  }
}
