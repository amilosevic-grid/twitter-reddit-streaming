import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, from_json}
import org.apache.spark.sql.types.{StringType, StructField, StructType}

object TwitterSparkConsumer {
  private val spark: SparkSession = SparkSession.builder().master("local").getOrCreate()

  def main(args: Array[String]): Unit = {
    TwitterSparkConsumer.run()
  }

  def run(): Unit = {
    import spark.implicits._

    val tweetSchema = StructType(
      List(
        StructField("text", StringType, nullable = true),
        StructField("lang", StringType, nullable = true),
        StructField("user", StringType, nullable = true)
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
      .writeStream
      .format("console")
      .start()
    query1.awaitTermination()
  }
}
