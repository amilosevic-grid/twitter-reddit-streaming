import sbt._
class Dependencies(versions: Versions) {
  lazy val spark: Seq[ModuleID] =
    "org.apache.spark" %% "spark-core" % versions.spark ::
    "org.apache.spark" %% "spark-sql" % versions.spark ::
    "org.apache.spark" %% "spark-streaming" % versions.spark ::
    "org.apache.spark" %% "spark-sql-kafka-0-10" % versions.spark :: Nil

  lazy val kafka: Seq[ModuleID] =
    "org.apache.kafka" % "kafka-clients" % versions.kafka ::
      "io.confluent" % "kafka-avro-serializer" % "6.0.0" :: Nil

  lazy val twitterAPIs: Seq[ModuleID] =
    "com.twitter" % "twitter-api-java-sdk" % versions.twitter :: Nil

  lazy val commonDeps: Seq[ModuleID] =
      "com.github.pureconfig" %% "pureconfig" % versions.pureconfig ::
        "com.sksamuel.avro4s" %% "avro4s-core" % "3.1.1" ::
        "com.nrinaudo" %% "kantan.csv" % "0.6.1" ::
        "com.nrinaudo" %% "kantan.csv-enumeratum" % "0.6.1" ::
        "com.google.code.gson" % "gson" % versions.gson :: Nil

}
